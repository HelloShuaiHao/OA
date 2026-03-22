# Phase C Task 7: AI 辅助生成 BPMN（补充）

**工作量：** 5 天
**优先级：** P1（严重缺失项）
**目标：** 实现自然语言描述到 BPMN XML 的自动生成

---

## 背景

设计文档 4.3.2 明确要求支持 AI 辅助生成 BPMN：
- 用户输入自然语言描述
- AI 生成 BPMN XML
- 自动导入到 Flowable 流程设计器
- 用户可以查看和调整

---

## 验收标准

- [ ] 用户可以输入自然语言描述（200-2000 字）
- [ ] 系统可以生成合法的 BPMN 2.0 XML（通过 Flowable 验证）
- [ ] 生成的流程包含：开始节点、结束节点、至少 1 个任务节点
- [ ] 支持生成：用户任务、服务任务、网关（排他/并行）、AgentX 扩展节点
- [ ] 生成时间 < 30 秒
- [ ] 生成成功率 > 80%（基于测试用例）
- [ ] 生成的流程可以在 Flowable 设计器中打开和编辑
- [ ] 有错误处理和用户友好的错误提示

---

## 子任务

### 7.1 后端服务实现

**工作量：** 3 天

#### 7.1.1 BpmnGenerationService

```java
@Service
public class BpmnGenerationService {
    
    @Autowired
    private LlmService llmService;
    
    @Autowired
    private RepositoryService repositoryService;
    
    /**
     * 根据自然语言描述生成 BPMN XML
     */
    public BpmnGenerationResult generateBpmn(BpmnGenerationRequest request) {
        // 1. 构建 Prompt
        String prompt = buildPrompt(request.getDescription(), request.getContext());
        
        // 2. 调用 LLM 生成 BPMN XML
        String bpmnXml = llmService.generate(prompt, 30000);
        
        // 3. 验证 BPMN XML
        ValidationResult validation = validateBpmn(bpmnXml);
        if (!validation.isValid()) {
            throw new BpmnValidationException(validation.getErrors());
        }
        
        // 4. 部署到 Flowable（可选）
        String processDefinitionId = null;
        if (request.isAutoDeploy()) {
            processDefinitionId = deployBpmn(bpmnXml, request.getProcessName());
        }
        
        return BpmnGenerationResult.builder()
            .bpmnXml(bpmnXml)
            .processDefinitionId(processDefinitionId)
            .build();
    }
    
    /**
     * 构建 Prompt
     */
    private String buildPrompt(String description, Map<String, Object> context) {
        return """
            你是一个 BPMN 2.0 流程设计专家。根据用户的自然语言描述，生成合法的 BPMN XML。
            
            要求：
            1. 必须包含 startEvent 和 endEvent
            2. 使用标准的 BPMN 2.0 元素
            3. 支持的节点类型：
               - userTask: 人工任务
               - serviceTask: 服务任务（可以是 AgentX 扩展节点）
               - exclusiveGateway: 排他网关
               - parallelGateway: 并行网关
            4. AgentX 扩展节点使用 serviceTask + delegateExpression：
               - AI 决策：${aiDecisionDelegate}
               - Tool 调用：${toolCallDelegate}
               - 数据查询：${dataQueryDelegate}
               - API 调用：${apiCallDelegate}
            5. 节点之间必须用 sequenceFlow 连接
            6. 网关必须有条件表达式
            7. 只输出 XML，不要有任何解释
            
            用户描述：
            %s
            
            上下文信息：
            %s
            
            请生成 BPMN XML：
            """.formatted(description, formatContext(context));
    }
    
    /**
     * 验证 BPMN XML
     */
    private ValidationResult validateBpmn(String bpmnXml) {
        try {
            // 使用 Flowable 的 BpmnXMLConverter 验证
            BpmnXMLConverter converter = new BpmnXMLConverter();
            BpmnModel model = converter.convertToBpmnModel(
                new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)),
                false, false
            );
            
            // 检查必需元素
            List<String> errors = new ArrayList<>();
            if (model.getStartEvents().isEmpty()) {
                errors.add("缺少开始节点");
            }
            if (model.getEndEvents().isEmpty()) {
                errors.add("缺少结束节点");
            }
            if (model.getFlowElements().size() < 3) {
                errors.add("流程节点太少，至少需要：开始节点 + 任务节点 + 结束节点");
            }
            
            return ValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .build();
                
        } catch (Exception e) {
            return ValidationResult.builder()
                .valid(false)
                .errors(List.of("BPMN XML 格式错误: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 部署到 Flowable
     */
    private String deployBpmn(String bpmnXml, String processName) {
        Deployment deployment = repositoryService.createDeployment()
            .name(processName)
            .addString(processName + ".bpmn20.xml", bpmnXml)
            .deploy();
            
        ProcessDefinition definition = repositoryService
            .createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult();
            
        return definition.getId();
    }
}
```

#### 7.1.2 Controller

```java
@RestController
@RequestMapping("/admin-api/agentx/bpmn")
public class BpmnGenerationController {
    
    @Autowired
    private BpmnGenerationService bpmnGenerationService;
    
    /**
     * 生成 BPMN
     */
    @PostMapping("/generate")
    public CommonResult<BpmnGenerationVO> generateBpmn(
        @RequestBody @Valid BpmnGenerationRequest request
    ) {
        BpmnGenerationResult result = bpmnGenerationService.generateBpmn(request);
        return success(convertToVO(result));
    }
    
    /**
     * 预览生成的流程图
     */
    @GetMapping("/preview")
    public void previewBpmn(
        @RequestParam String bpmnXml,
        HttpServletResponse response
    ) throws IOException {
        // 生成流程图 PNG
        byte[] diagram = bpmnGenerationService.generateDiagram(bpmnXml);
        
        response.setContentType("image/png");
        response.getOutputStream().write(diagram);
    }
}
```

#### 7.1.3 DTO

```java
@Data
public class BpmnGenerationRequest {
    
    @NotBlank(message = "流程描述不能为空")
    @Size(min = 20, max = 2000, message = "描述长度必须在 20-2000 字之间")
    private String description;
    
    @NotBlank(message = "流程名称不能为空")
    private String processName;
    
    private Map<String, Object> context;
    
    private Boolean autoDeploy = false;
}

@Data
@Builder
public class BpmnGenerationResult {
    private String bpmnXml;
    private String processDefinitionId;
}

@Data
@Builder
public class ValidationResult {
    private Boolean valid;
    private List<String> errors;
}
```

**验收标准：**
- [ ] 可以成功调用 LLM 生成 BPMN XML
- [ ] 生成的 XML 通过 Flowable 验证
- [ ] 验证逻辑覆盖所有必需元素
- [ ] 有完整的错误处理
- [ ] 有单元测试（覆盖率 > 70%）

---

### 7.2 前端 UI 实现

**工作量：** 2 天

#### 7.2.1 AI 生成流程页面

```vue
<template>
  <div class="bpmn-generation">
    <el-card>
      <template #header>
        <span>AI 辅助生成流程</span>
      </template>
      
      <!-- 步骤 1：输入描述 -->
      <el-form v-if="step === 1" :model="form" label-width="120px">
        <el-form-item label="流程名称" required>
          <el-input v-model="form.processName" placeholder="请输入流程名称" />
        </el-form-item>
        
        <el-form-item label="流程描述" required>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="10"
            placeholder="请详细描述流程，例如：&#10;&#10;这是一个请假审批流程：&#10;1. 员工提交请假申请&#10;2. 如果请假天数 <= 2 天，自动通过&#10;3. 如果请假天数 > 2 天，需要直属上级审批&#10;4. 如果请假天数 > 5 天，还需要部门负责人审批&#10;5. 审批通过后，发送通知给员工"
            show-word-limit
            maxlength="2000"
          />
          <div class="tip">
            提示：描述越详细，生成的流程越准确。建议包含：流程步骤、判断条件、审批角色等。
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="generateBpmn" :loading="generating">
            生成流程
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 步骤 2：预览和调整 -->
      <div v-if="step === 2">
        <el-alert
          title="流程生成成功！"
          type="success"
          :closable="false"
          style="margin-bottom: 20px"
        />
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="流程图预览" name="diagram">
            <div class="diagram-preview">
              <img :src="diagramUrl" alt="流程图" />
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="BPMN XML" name="xml">
            <el-input
              v-model="bpmnXml"
              type="textarea"
              :rows="20"
              readonly
            />
          </el-tab-pane>
        </el-tabs>
        
        <div class="actions">
          <el-button @click="step = 1">重新生成</el-button>
          <el-button type="primary" @click="openInDesigner">
            在设计器中打开
          </el-button>
          <el-button type="success" @click="deployBpmn">
            部署流程
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { generateBpmnApi, deployBpmnApi } from '@/api/agentx/bpmn'
import { ElMessage } from 'element-plus'

const step = ref(1)
const generating = ref(false)
const activeTab = ref('diagram')

const form = ref({
  processName: '',
  description: ''
})

const bpmnXml = ref('')
const diagramUrl = ref('')

const generateBpmn = async () => {
  if (!form.value.processName || !form.value.description) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (form.value.description.length < 20) {
    ElMessage.warning('流程描述至少需要 20 个字')
    return
  }
  
  generating.value = true
  try {
    const res = await generateBpmnApi(form.value)
    bpmnXml.value = res.data.bpmnXml
    diagramUrl.value = `/admin-api/agentx/bpmn/preview?bpmnXml=${encodeURIComponent(res.data.bpmnXml)}`
    step.value = 2
    ElMessage.success('流程生成成功')
  } catch (error) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const openInDesigner = () => {
  // 跳转到 Flowable 设计器
  window.open(`/flowable-modeler?bpmn=${encodeURIComponent(bpmnXml.value)}`, '_blank')
}

const deployBpmn = async () => {
  try {
    await deployBpmnApi({
      processName: form.value.processName,
      bpmnXml: bpmnXml.value
    })
    ElMessage.success('部署成功')
  } catch (error) {
    ElMessage.error(error.message || '部署失败')
  }
}
</script>

<style scoped>
.tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.diagram-preview {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.diagram-preview img {
  max-width: 100%;
  border: 1px solid #dcdfe6;
}

.actions {
  margin-top: 20px;
  text-align: right;
}
</style>
```

#### 7.2.2 API

```javascript
// src/api/agentx/bpmn.js
import request from '@/utils/request'

export function generateBpmnApi(data) {
  return request({
    url: '/admin-api/agentx/bpmn/generate',
    method: 'post',
    data
  })
}

export function deployBpmnApi(data) {
  return request({
    url: '/admin-api/agentx/bpmn/deploy',
    method: 'post',
    data
  })
}
```

**验收标准：**
- [ ] 用户可以输入流程描述（20-2000 字）
- [ ] 点击生成后显示 loading 状态
- [ ] 生成成功后显示流程图预览
- [ ] 可以查看 BPMN XML
- [ ] 可以在设计器中打开
- [ ] 可以一键部署
- [ ] 有友好的错误提示

---

### 7.3 测试用例

**工作量：** 1 天

#### 测试用例 1：简单审批流程

**输入：**
```
这是一个请假审批流程：
1. 员工提交请假申请
2. 直属上级审批
3. 审批通过后结束
```

**预期输出：**
- 包含 startEvent, userTask, endEvent
- userTask 的 assignee 使用角色解析：${roleResolver.resolve('direct_manager', execution)}

#### 测试用例 2：带条件判断的流程

**输入：**
```
报销审批流程：
1. 员工提交报销申请
2. 如果金额 <= 1000，自动通过
3. 如果金额 > 1000，需要财务审批
4. 审批通过后结束
```

**预期输出：**
- 包含 exclusiveGateway
- 网关有两个分支，带条件表达式
- 一个分支直接到 endEvent
- 另一个分支到 userTask（财务审批）

#### 测试用例 3：使用 AgentX 扩展节点

**输入：**
```
智能客户跟进流程：
1. 接收客户消息
2. AI 判断客户意图
3. 如果是咨询问题，调用知识库查询
4. 如果是投诉，转人工处理
5. 发送回复给客户
```

**预期输出：**
- 包含 serviceTask，delegateExpression="${aiDecisionDelegate}"
- 包含 serviceTask，delegateExpression="${toolCallDelegate}"
- 包含 exclusiveGateway 根据 AI 决策结果路由

**验收标准：**
- [ ] 所有测试用例通过
- [ ] 生成成功率 > 80%
- [ ] 生成的流程可以在 Flowable 中执行

---

## 技术要点

### Prompt 工程

关键点：
1. 明确输出格式（只要 XML，不要解释）
2. 提供 BPMN 2.0 规范约束
3. 提供 AgentX 扩展节点的使用方式
4. 提供示例（few-shot learning）

### BPMN 验证

使用 Flowable 的 BpmnXMLConverter：
```java
BpmnXMLConverter converter = new BpmnXMLConverter();
BpmnModel model = converter.convertToBpmnModel(inputStream, false, false);
```

### 流程图生成

使用 Flowable 的 ProcessDiagramGenerator：
```java
ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
InputStream diagram = generator.generateDiagram(
    model, 
    "png",
    Collections.emptyList(),
    Collections.emptyList(),
    "宋体", "宋体", "宋体"
);
```

---

## 风险和缓解

### 风险 1：LLM 生成的 XML 不合法

**缓解措施：**
- 严格的 Prompt 约束
- 后端验证 + 自动修复
- 失败后允许重试（最多 3 次）

### 风险 2：生成时间过长

**缓解措施：**
- 设置 30 秒超时
- 使用异步处理 + 轮询
- 显示进度提示

### 风险 3：生成的流程不符合预期

**缓解措施：**
- 提供流程图预览
- 允许在设计器中调整
- 不自动部署，需要用户确认

---

## 总结

**工作量：** 5 天
- 后端服务：3 天
- 前端 UI：2 天
- 测试：1 天（并行）

**完成后：**
- 用户可以用自然语言描述生成流程
- 降低流程设计门槛
- 提升配置效率
- 对齐设计文档要求

**对齐度提升：** 87% → 95%
