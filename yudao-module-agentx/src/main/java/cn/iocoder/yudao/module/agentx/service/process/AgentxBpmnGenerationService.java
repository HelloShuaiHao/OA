package cn.iocoder.yudao.module.agentx.service.process;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxBpmnGenerateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxBpmnGenerateRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.process.vo.AgentxBpmnPreviewRespVO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class AgentxBpmnGenerationService {

    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com";

    private final RestTemplate llmRestTemplate;

    @Value("${spring.ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${spring.ai.deepseek.base-url:}")
    private String deepseekBaseUrl;

    @Value("${spring.ai.deepseek.chat.options.model:deepseek-chat}")
    private String deepseekModel;

    public AgentxBpmnGenerationService(RestTemplateBuilder restTemplateBuilder) {
        this.llmRestTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(45))
                .build();
    }

    public AgentxBpmnGenerateRespVO generate(AgentxBpmnGenerateReqVO reqVO) {
        String processName = StrUtil.blankToDefault(reqVO.getProcessName(), "AI 生成流程");
        String processKey = normalizeKey(reqVO.getProcessKey());
        GeneratedPlan plan = generatePlan(processName, reqVO.getDescription());
        String bpmnXml = buildBpmnXml(processName, processKey, plan);

        AgentxBpmnPreviewRespVO preview = preview(bpmnXml);
        AgentxBpmnGenerateRespVO respVO = new AgentxBpmnGenerateRespVO();
        respVO.setProcessName(processName);
        respVO.setProcessKey(processKey);
        respVO.setBpmnXml(bpmnXml);
        respVO.setValid(preview.getValid());
        respVO.setMessage(preview.getMessage());
        return respVO;
    }

    public AgentxBpmnPreviewRespVO preview(String bpmnXml) {
        AgentxBpmnPreviewRespVO respVO = new AgentxBpmnPreviewRespVO();
        try {
            BpmnModel model = parseModel(bpmnXml);
            Process process = model.getMainProcess();
            if (process == null) {
                respVO.setValid(false);
                respVO.setMessage("BPMN 缺少主流程定义");
                return respVO;
            }

            int start = 0;
            int end = 0;
            int userTask = 0;
            int gateway = 0;
            for (FlowElement element : process.getFlowElements()) {
                if (element instanceof StartEvent) {
                    start++;
                } else if (element instanceof EndEvent) {
                    end++;
                } else if (element instanceof UserTask) {
                    userTask++;
                } else if (element instanceof Gateway) {
                    gateway++;
                }
            }
            respVO.setStartEventCount(start);
            respVO.setEndEventCount(end);
            respVO.setUserTaskCount(userTask);
            respVO.setGatewayCount(gateway);
            if (start < 1 || end < 1 || userTask < 1) {
                respVO.setValid(false);
                respVO.setMessage("BPMN 缺少必需元素（startEvent/endEvent/userTask）");
                return respVO;
            }
            respVO.setValid(true);
            respVO.setMessage("BPMN 校验通过");
            return respVO;
        } catch (Exception ex) {
            respVO.setValid(false);
            respVO.setMessage("BPMN 解析失败：" + ex.getMessage());
            return respVO;
        }
    }

    private BpmnModel parseModel(String bpmnXml) throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(
                new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));
        return new BpmnXMLConverter().convertToBpmnModel(streamReader);
    }

    private String normalizeKey(String processKey) {
        String key = StrUtil.blankToDefault(processKey, "agentx_ai_" + UUID.randomUUID().toString().replace("-", ""));
        key = key.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
        if (Character.isDigit(key.charAt(0))) {
            key = "p_" + key;
        }
        return key;
    }

    private GeneratedPlan generatePlan(String processName, String description) {
        GeneratedPlan plan = callLlmForPlan(processName, description);
        if (isUsablePlan(plan)) {
            return normalizePlan(plan);
        }
        return buildHeuristicPlan(description);
    }

    private GeneratedPlan callLlmForPlan(String processName, String description) {
        if (StrUtil.isBlank(deepseekApiKey)) {
            log.warn("[callLlmForPlan][missing deepseek api key, use heuristic fallback]");
            return null;
        }
        try {
            Map<String, Object> request = new LinkedHashMap<String, Object>();
            request.put("model", StrUtil.blankToDefault(deepseekModel, "deepseek-chat"));
            request.put("temperature", 0);
            request.put("max_tokens", 500);
            request.put("enable_thinking", false);
            List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
            messages.add(buildMessage("system", buildSystemPrompt()));
            messages.add(buildMessage("user", buildUserPrompt(processName, description)));
            request.put("messages", messages);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey.trim());
            org.springframework.http.HttpEntity<String> entity =
                    new org.springframework.http.HttpEntity<String>(JsonUtils.toJsonString(request), headers);
            String endpoint = StrUtil.blankToDefault(deepseekBaseUrl, DEEPSEEK_BASE_URL).replaceAll("/$", "")
                    + "/chat/completions";
            String body = llmRestTemplate.postForObject(endpoint, entity, String.class);
            if (StrUtil.isBlank(body)) {
                return null;
            }
            JsonNode root = JsonUtils.parseTree(body);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (StrUtil.isBlank(content)) {
                log.warn("[callLlmForPlan][empty content][body={}]", body);
                return null;
            }
            return JsonUtils.parseObject(cleanJsonContent(content), GeneratedPlan.class);
        } catch (Exception ex) {
            log.warn("[callLlmForPlan][llm request failed, use heuristic fallback]", ex);
            return null;
        }
    }

    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> message = new LinkedHashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String buildSystemPrompt() {
        return "你是 BPMN 流程设计助手。"
                + "只输出紧凑 JSON，不要 markdown，不要解释。"
                + "格式:"
                + "{\"nodes\":[{\"id\":\"start\",\"type\":\"startEvent\",\"name\":\"开始\"}],"
                + "\"flows\":[{\"from\":\"a\",\"to\":\"b\",\"name\":\"\"}]}. "
                + "约束："
                + "type 只能是 startEvent、userTask、exclusiveGateway、endEvent；"
                + "必须恰好一个 startEvent；至少一个 userTask；至少一个 endEvent；"
                + "如果有通过/拒绝分支就用 exclusiveGateway；"
                + "节点名用简短中文；id 用英文下划线；"
                + "生成 4 到 6 个节点。";
    }

    private String buildUserPrompt(String processName, String description) {
        return "流程名称：" + processName + "\n"
                + "流程描述：" + description + "\n"
                + "请输出最终 JSON。";
    }

    private String cleanJsonContent(String content) {
        String text = StrUtil.trim(content);
        if (StrUtil.startWith(text, "```")) {
            text = text.replaceFirst("^```json", "");
            text = text.replaceFirst("^```", "");
            text = text.replaceFirst("```$", "");
        }
        return StrUtil.trim(text);
    }

    private boolean isUsablePlan(GeneratedPlan plan) {
        if (plan == null || plan.getNodes() == null || plan.getFlows() == null) {
            return false;
        }
        int start = 0;
        int end = 0;
        int userTask = 0;
        Set<String> ids = new HashSet<String>();
        for (PlanNode node : plan.getNodes()) {
            if (node == null || StrUtil.hasBlank(node.getId(), node.getType())) {
                return false;
            }
            ids.add(node.getId());
            if ("startEvent".equals(node.getType())) {
                start++;
            } else if ("endEvent".equals(node.getType())) {
                end++;
            } else if ("userTask".equals(node.getType())) {
                userTask++;
            }
        }
        if (start != 1 || end < 1 || userTask < 1) {
            return false;
        }
        for (PlanFlow flow : plan.getFlows()) {
            if (flow == null || StrUtil.hasBlank(flow.getFrom(), flow.getTo())) {
                return false;
            }
            if (!ids.contains(flow.getFrom()) || !ids.contains(flow.getTo())) {
                return false;
            }
        }
        return true;
    }

    private GeneratedPlan normalizePlan(GeneratedPlan rawPlan) {
        GeneratedPlan plan = new GeneratedPlan();
        List<PlanNode> nodes = new ArrayList<PlanNode>();
        Map<String, String> idMap = new LinkedHashMap<String, String>();
        Set<String> used = new HashSet<String>();
        for (PlanNode rawNode : rawPlan.getNodes()) {
            if (rawNode == null) {
                continue;
            }
            PlanNode node = new PlanNode();
            String safeId = normalizeNodeId(rawNode.getId(), used);
            idMap.put(rawNode.getId(), safeId);
            node.setId(safeId);
            node.setType(normalizeNodeType(rawNode.getType()));
            node.setName(defaultNodeName(node.getType(), rawNode.getName()));
            nodes.add(node);
        }
        List<PlanFlow> flows = new ArrayList<PlanFlow>();
        int seq = 1;
        for (PlanFlow rawFlow : rawPlan.getFlows()) {
            if (rawFlow == null) {
                continue;
            }
            String from = idMap.get(rawFlow.getFrom());
            String to = idMap.get(rawFlow.getTo());
            if (StrUtil.hasBlank(from, to)) {
                continue;
            }
            PlanFlow flow = new PlanFlow();
            flow.setId("flow_" + seq++);
            flow.setFrom(from);
            flow.setTo(to);
            flow.setName(StrUtil.blankToDefault(rawFlow.getName(), ""));
            flows.add(flow);
        }
        plan.setNodes(nodes);
        plan.setFlows(flows);
        return plan;
    }

    private String normalizeNodeId(String rawId, Set<String> used) {
        String id = StrUtil.blankToDefault(rawId, "node_" + UUID.randomUUID().toString().replace("-", ""));
        id = id.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        if (id.isEmpty() || Character.isDigit(id.charAt(0))) {
            id = "n_" + id;
        }
        String candidate = id;
        int suffix = 2;
        while (used.contains(candidate)) {
            candidate = id + "_" + suffix++;
        }
        used.add(candidate);
        return candidate;
    }

    private String normalizeNodeType(String type) {
        if ("startEvent".equals(type) || "endEvent".equals(type)
                || "userTask".equals(type) || "exclusiveGateway".equals(type)) {
            return type;
        }
        return "userTask";
    }

    private String defaultNodeName(String type, String name) {
        if (StrUtil.isNotBlank(name)) {
            return StrUtil.trim(name);
        }
        if ("startEvent".equals(type)) {
            return "开始";
        }
        if ("endEvent".equals(type)) {
            return "结束";
        }
        if ("exclusiveGateway".equals(type)) {
            return "条件判断";
        }
        return "待处理";
    }

    private GeneratedPlan buildHeuristicPlan(String description) {
        String clean = StrUtil.blankToDefault(description, "").replaceAll("\\s+", " ").trim();
        GeneratedPlan plan = new GeneratedPlan();
        List<PlanNode> nodes = new ArrayList<PlanNode>();
        List<PlanFlow> flows = new ArrayList<PlanFlow>();
        nodes.add(node("start", "startEvent", "开始"));
        nodes.add(node("submit", "userTask", inferSubmitTaskName(clean)));
        nodes.add(node("review", "userTask", inferReviewTaskName(clean)));

        boolean hasApprovalBranch = containsAny(clean, "通过", "拒绝", "驳回", "同意", "不同意");
        boolean hasArchiveTask = containsAny(clean, "备案", "归档", "通知", "抄送", "人事");
        if (hasApprovalBranch) {
            nodes.add(node("decision", "exclusiveGateway", "审批结果"));
            flows.add(flow("flow_1", "start", "submit", ""));
            flows.add(flow("flow_2", "submit", "review", ""));
            flows.add(flow("flow_3", "review", "decision", ""));
            if (hasArchiveTask) {
                nodes.add(node("archive", "userTask", inferArchiveTaskName(clean)));
                nodes.add(node("end_pass", "endEvent", "结束"));
                nodes.add(node("end_reject", "endEvent", "结束"));
                flows.add(flow("flow_4", "decision", "archive", "通过"));
                flows.add(flow("flow_5", "archive", "end_pass", ""));
                flows.add(flow("flow_6", "decision", "end_reject", "拒绝"));
            } else {
                nodes.add(node("end_pass", "endEvent", "结束"));
                nodes.add(node("end_reject", "endEvent", "结束"));
                flows.add(flow("flow_4", "decision", "end_pass", "通过"));
                flows.add(flow("flow_5", "decision", "end_reject", "拒绝"));
            }
        } else {
            nodes.add(node("finish", "endEvent", "结束"));
            flows.add(flow("flow_1", "start", "submit", ""));
            if (hasArchiveTask) {
                nodes.add(node("archive", "userTask", inferArchiveTaskName(clean)));
                flows.add(flow("flow_2", "submit", "review", ""));
                flows.add(flow("flow_3", "review", "archive", ""));
                flows.add(flow("flow_4", "archive", "finish", ""));
            } else {
                flows.add(flow("flow_2", "submit", "review", ""));
                flows.add(flow("flow_3", "review", "finish", ""));
            }
        }
        plan.setNodes(nodes);
        plan.setFlows(flows);
        return plan;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (StrUtil.contains(text, keyword)) {
                return true;
            }
        }
        return false;
    }

    private PlanNode node(String id, String type, String name) {
        PlanNode node = new PlanNode();
        node.setId(id);
        node.setType(type);
        node.setName(name);
        return node;
    }

    private PlanFlow flow(String id, String from, String to, String name) {
        PlanFlow flow = new PlanFlow();
        flow.setId(id);
        flow.setFrom(from);
        flow.setTo(to);
        flow.setName(name);
        return flow;
    }

    private String inferSubmitTaskName(String description) {
        if (StrUtil.containsAny(description, "请假", "外出", "报销", "采购")) {
            return "提交申请";
        }
        return "发起流程";
    }

    private String inferReviewTaskName(String description) {
        if (StrUtil.contains(description, "主管")) {
            return "直属主管审批";
        }
        if (StrUtil.contains(description, "经理")) {
            return "经理审批";
        }
        return "审批处理";
    }

    private String inferArchiveTaskName(String description) {
        if (StrUtil.contains(description, "人事")) {
            return "人事备案";
        }
        if (StrUtil.contains(description, "通知")) {
            return "通知执行";
        }
        return "结果处理";
    }

    private String buildBpmnXml(String processName, String processKey, GeneratedPlan plan) {
        Map<String, PlanNode> nodeMap = new LinkedHashMap<String, PlanNode>();
        for (PlanNode node : plan.getNodes()) {
            nodeMap.put(node.getId(), node);
        }
        Map<String, List<PlanFlow>> outgoingMap = buildOutgoingMap(plan.getFlows());
        Map<String, NodeLayout> layoutMap = buildLayout(plan, nodeMap, outgoingMap);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" ")
                .append("xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" ")
                .append("xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" ")
                .append("targetNamespace=\"http://flowable.org/processdef\">\n")
                .append("  <process id=\"").append(processKey).append("\" name=\"")
                .append(xmlEscape(processName)).append("\" isExecutable=\"true\">\n");

        for (PlanNode node : plan.getNodes()) {
            xml.append("    <").append(node.getType()).append(" id=\"").append(node.getId())
                    .append("\" name=\"").append(xmlEscape(node.getName())).append("\"/>\n");
        }
        for (PlanFlow flow : plan.getFlows()) {
            xml.append("    <sequenceFlow id=\"").append(flow.getId())
                    .append("\" sourceRef=\"").append(flow.getFrom())
                    .append("\" targetRef=\"").append(flow.getTo()).append("\"");
            if (StrUtil.isNotBlank(flow.getName())) {
                xml.append(" name=\"").append(xmlEscape(flow.getName())).append("\"");
            }
            xml.append("/>\n");
        }
        xml.append("  </process>\n")
                .append("  <bpmndi:BPMNDiagram id=\"BPMNDiagram_").append(processKey).append("\">\n")
                .append("    <bpmndi:BPMNPlane id=\"BPMNPlane_").append(processKey)
                .append("\" bpmnElement=\"").append(processKey).append("\">\n");

        for (PlanNode node : plan.getNodes()) {
            NodeLayout layout = layoutMap.get(node.getId());
            xml.append("      <bpmndi:BPMNShape id=\"Shape_").append(node.getId())
                    .append("\" bpmnElement=\"").append(node.getId()).append("\">\n")
                    .append("        <omgdc:Bounds x=\"").append(layout.getX())
                    .append("\" y=\"").append(layout.getY())
                    .append("\" width=\"").append(layout.getWidth())
                    .append("\" height=\"").append(layout.getHeight()).append("\"/>\n")
                    .append("      </bpmndi:BPMNShape>\n");
        }
        for (PlanFlow flow : plan.getFlows()) {
            List<int[]> waypoints = buildWaypoints(layoutMap.get(flow.getFrom()), layoutMap.get(flow.getTo()));
            xml.append("      <bpmndi:BPMNEdge id=\"Edge_").append(flow.getId())
                    .append("\" bpmnElement=\"").append(flow.getId()).append("\">\n");
            for (int[] waypoint : waypoints) {
                xml.append("        <omgdi:waypoint x=\"").append(waypoint[0])
                        .append("\" y=\"").append(waypoint[1]).append("\"/>\n");
            }
            xml.append("      </bpmndi:BPMNEdge>\n");
        }
        xml.append("    </bpmndi:BPMNPlane>\n")
                .append("  </bpmndi:BPMNDiagram>\n")
                .append("</definitions>");
        return xml.toString();
    }

    private Map<String, List<PlanFlow>> buildOutgoingMap(List<PlanFlow> flows) {
        Map<String, List<PlanFlow>> outgoingMap = new HashMap<String, List<PlanFlow>>();
        for (PlanFlow flow : flows) {
            List<PlanFlow> list = outgoingMap.get(flow.getFrom());
            if (list == null) {
                list = new ArrayList<PlanFlow>();
                outgoingMap.put(flow.getFrom(), list);
            }
            list.add(flow);
        }
        for (List<PlanFlow> list : outgoingMap.values()) {
            Collections.sort(list, new Comparator<PlanFlow>() {
                @Override
                public int compare(PlanFlow o1, PlanFlow o2) {
                    return StrUtil.blankToDefault(o1.getName(), "").compareTo(StrUtil.blankToDefault(o2.getName(), ""));
                }
            });
        }
        return outgoingMap;
    }

    private Map<String, NodeLayout> buildLayout(GeneratedPlan plan, Map<String, PlanNode> nodeMap,
                                                Map<String, List<PlanFlow>> outgoingMap) {
        Map<String, NodeLayout> layoutMap = new LinkedHashMap<String, NodeLayout>();
        String startId = null;
        for (PlanNode node : plan.getNodes()) {
            if ("startEvent".equals(node.getType())) {
                startId = node.getId();
                break;
            }
        }
        if (startId == null) {
            return layoutMap;
        }
        Deque<TraversalState> queue = new ArrayDeque<TraversalState>();
        queue.add(new TraversalState(startId, 0, 0));
        Set<String> visited = new HashSet<String>();
        while (!queue.isEmpty()) {
            TraversalState state = queue.pollFirst();
            PlanNode node = nodeMap.get(state.getNodeId());
            if (node == null) {
                continue;
            }
            NodeLayout current = layoutMap.get(node.getId());
            if (current == null || state.getDepth() < current.getDepth()) {
                current = createNodeLayout(node, state.getDepth(), state.getLane());
                layoutMap.put(node.getId(), current);
            }
            String visitKey = node.getId() + "_" + state.getDepth() + "_" + state.getLane();
            if (!visited.add(visitKey)) {
                continue;
            }
            List<PlanFlow> outgoing = outgoingMap.get(node.getId());
            if (outgoing == null || outgoing.isEmpty()) {
                continue;
            }
            int size = outgoing.size();
            for (int i = 0; i < size; i++) {
                PlanFlow flow = outgoing.get(i);
                int nextLane = state.getLane();
                if ("exclusiveGateway".equals(node.getType()) && size > 1) {
                    nextLane = state.getLane() + (i == 0 ? -1 : i);
                }
                queue.addLast(new TraversalState(flow.getTo(), state.getDepth() + 1, nextLane));
            }
        }
        for (PlanNode node : plan.getNodes()) {
            if (!layoutMap.containsKey(node.getId())) {
                layoutMap.put(node.getId(), createNodeLayout(node, layoutMap.size(), 0));
            }
        }
        return layoutMap;
    }

    private NodeLayout createNodeLayout(PlanNode node, int depth, int lane) {
        int width = "userTask".equals(node.getType()) ? 140 : ("exclusiveGateway".equals(node.getType()) ? 50 : 36);
        int height = "userTask".equals(node.getType()) ? 80 : ("exclusiveGateway".equals(node.getType()) ? 50 : 36);
        int x = 120 + depth * 180;
        int y = 120 + lane * 140;
        NodeLayout layout = new NodeLayout();
        layout.setDepth(depth);
        layout.setLane(lane);
        layout.setX(x);
        layout.setY(y);
        layout.setWidth(width);
        layout.setHeight(height);
        return layout;
    }

    private List<int[]> buildWaypoints(NodeLayout source, NodeLayout target) {
        List<int[]> points = new ArrayList<int[]>();
        int sourceX = source.getX() + source.getWidth();
        int sourceY = source.getY() + source.getHeight() / 2;
        int targetX = target.getX();
        int targetY = target.getY() + target.getHeight() / 2;
        points.add(new int[]{sourceX, sourceY});
        if (sourceY == targetY) {
            points.add(new int[]{targetX, targetY});
            return points;
        }
        int midX = sourceX + Math.max(40, (targetX - sourceX) / 2);
        points.add(new int[]{midX, sourceY});
        points.add(new int[]{midX, targetY});
        points.add(new int[]{targetX, targetY});
        return points;
    }

    private String xmlEscape(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Data
    public static class GeneratedPlan {
        private List<PlanNode> nodes;
        private List<PlanFlow> flows;
    }

    @Data
    public static class PlanNode {
        private String id;
        private String type;
        private String name;
    }

    @Data
    public static class PlanFlow {
        private String id;
        private String from;
        private String to;
        private String name;
    }

    @Data
    private static class NodeLayout {
        private int depth;
        private int lane;
        private int x;
        private int y;
        private int width;
        private int height;
    }

    private static class TraversalState {
        private final String nodeId;
        private final int depth;
        private final int lane;

        private TraversalState(String nodeId, int depth, int lane) {
            this.nodeId = nodeId;
            this.depth = depth;
            this.lane = lane;
        }

        public String getNodeId() {
            return nodeId;
        }

        public int getDepth() {
            return depth;
        }

        public int getLane() {
            return lane;
        }
    }

}
