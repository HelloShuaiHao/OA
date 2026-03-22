package cn.iocoder.yudao.module.agentx.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.*;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelAgentDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelConfigDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxUserChannelBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxChannelAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxChannelConfigMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxUserChannelBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.instance.AgentxOpenfangInstanceMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.agentx.service.instance.OpenfangApiKeyCrypto;
import cn.iocoder.yudao.module.agentx.service.metrics.AgentxMetricsService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Service
@Validated
public class AgentxChannelServiceImpl implements AgentxChannelService {

    private static final Set<String> VALID_CHANNEL_TYPES = new HashSet<>(Arrays.asList("telegram", "wecom", "dingtalk"));

    @Resource
    private AgentxChannelConfigMapper channelConfigMapper;
    @Resource
    private AgentxChannelAgentMapper channelAgentMapper;
    @Resource
    private AgentxUserChannelBindingMapper userChannelBindingMapper;
    @Resource
    private OpenfangApiKeyCrypto openfangApiKeyCrypto;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AgentxOpenfangInstanceMapper openfangInstanceMapper;
    @Resource
    private AgentxMetricsService metricsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChannelConfig(AgentxChannelConfigSaveReqVO reqVO) {
        validateChannelType(reqVO.getChannelType());
        if (StrUtil.isBlank(reqVO.getBotToken())) {
            throw exception(ErrorCodeConstants.CHANNEL_BOT_TOKEN_REQUIRED);
        }
        AgentxChannelConfigDO channel = new AgentxChannelConfigDO()
                .setChannelType(reqVO.getChannelType())
                .setChannelName(reqVO.getChannelName())
                .setBotTokenEncrypted(openfangApiKeyCrypto.encrypt(reqVO.getBotToken()))
                .setConfig(buildConfigJson(reqVO))
                .setStatus(reqVO.getStatus());
        channelConfigMapper.insert(channel);
        replaceChannelAgents(channel.getId(), reqVO.getAgentIds());
        syncChannelToOpenfang(channel.getId());
        return channel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannelConfig(AgentxChannelConfigSaveReqVO reqVO) {
        AgentxChannelConfigDO channel = validateChannelExists(reqVO.getId());
        validateChannelType(reqVO.getChannelType());
        AgentxChannelConfigDO update = new AgentxChannelConfigDO()
                .setId(channel.getId())
                .setChannelType(reqVO.getChannelType())
                .setChannelName(reqVO.getChannelName())
                .setConfig(buildConfigJson(reqVO))
                .setStatus(reqVO.getStatus());
        if (StrUtil.isNotBlank(reqVO.getBotToken())) {
            update.setBotTokenEncrypted(openfangApiKeyCrypto.encrypt(reqVO.getBotToken()));
        }
        channelConfigMapper.updateById(update);
        replaceChannelAgents(channel.getId(), reqVO.getAgentIds());
        syncChannelToOpenfang(channel.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChannelConfig(Long id) {
        validateChannelExists(id);
        channelConfigMapper.deleteById(id);
        channelAgentMapper.deleteByChannelId(id);
    }

    @Override
    public AgentxChannelConfigRespVO getChannelConfig(Long id) {
        return convert(channelConfigMapper.selectById(id));
    }

    @Override
    public PageResult<AgentxChannelConfigRespVO> getChannelConfigPage(AgentxChannelConfigPageReqVO pageReqVO) {
        PageResult<AgentxChannelConfigDO> page = channelConfigMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(page, AgentxChannelConfigRespVO.class, resp -> {
            AgentxChannelConfigDO channel = page.getList().stream()
                    .filter(item -> ObjectUtil.equal(item.getId(), resp.getId()))
                    .findFirst().orElse(null);
            if (channel != null) {
                fillRespDetails(channel, resp);
            }
        });
    }

    @Override
    public AgentxChannelTestRespVO testChannelConnection(AgentxChannelTestReqVO reqVO) {
        validateChannelType(reqVO.getChannelType());
        String authToken = reqVO.getBotToken();
        if (StrUtil.isBlank(authToken) && reqVO.getChannelId() != null) {
            AgentxChannelConfigDO channel = channelConfigMapper.selectById(reqVO.getChannelId());
            if (channel != null && StrUtil.isNotBlank(channel.getBotTokenEncrypted())) {
                authToken = openfangApiKeyCrypto.decrypt(channel.getBotTokenEncrypted());
            }
        }
        if (StrUtil.isBlank(authToken)) {
            throw exception(ErrorCodeConstants.CHANNEL_BOT_TOKEN_REQUIRED);
        }
        AgentxChannelTestRespVO respVO = new AgentxChannelTestRespVO();
        try {
            if ("telegram".equals(reqVO.getChannelType())) {
                String url = "https://api.telegram.org/bot" + authToken + "/getMe";
                return buildRespByOkField(respVO, restTemplate.exchange(url, HttpMethod.GET, null, Map.class));
            }
            if ("wecom".equals(reqVO.getChannelType())) {
                String[] auth = splitAuthPair(authToken);
                String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + auth[0] + "&corpsecret=" + auth[1];
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
                Object errCode = response.getBody() == null ? null : response.getBody().get("errcode");
                if ("0".equals(String.valueOf(errCode))) {
                    respVO.setSuccess(true);
                    respVO.setMessage("✓ 连接成功");
                    return respVO;
                }
                respVO.setSuccess(false);
                respVO.setMessage("✗ 连接失败：配置无效");
                return respVO;
            }
            String[] auth = splitAuthPair(authToken);
            String url = "https://oapi.dingtalk.com/gettoken?appkey=" + auth[0] + "&appsecret=" + auth[1];
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
            Object errCode = response.getBody() == null ? null : response.getBody().get("errcode");
            if ("0".equals(String.valueOf(errCode))) {
                respVO.setSuccess(true);
                respVO.setMessage("✓ 连接成功");
                return respVO;
            }
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：配置无效");
            return respVO;
        } catch (Exception ex) {
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：" + ex.getMessage());
            return respVO;
        }
    }

    @Override
    public List<AgentxUserChannelBindingRespVO> getMyBindings(Long userId) {
        return BeanUtils.toBean(userChannelBindingMapper.selectListByUserId(userId), AgentxUserChannelBindingRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long id, Long userId) {
        AgentxUserChannelBindingDO binding = userChannelBindingMapper.selectById(id);
        if (binding == null || !ObjectUtil.equal(binding.getUserId(), userId)) {
            return;
        }
        userChannelBindingMapper.updateById(new AgentxUserChannelBindingDO()
                .setId(id)
                .setStatus(0)
                .setUnbindTime(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUnbind(Long id) {
        AgentxUserChannelBindingDO binding = userChannelBindingMapper.selectById(id);
        if (binding == null || !ObjectUtil.equal(binding.getStatus(), 1)) {
            return;
        }
        userChannelBindingMapper.updateById(new AgentxUserChannelBindingDO()
                .setId(id)
                .setStatus(0)
                .setUnbindTime(LocalDateTime.now()));
    }

    @Override
    public PageResult<AgentxUserChannelBindingRespVO> getBindingPage(AgentxUserChannelBindingPageReqVO pageReqVO) {
        return BeanUtils.toBean(userChannelBindingMapper.selectPage(pageReqVO), AgentxUserChannelBindingRespVO.class);
    }

    private String buildConfigJson(AgentxChannelConfigSaveReqVO reqVO) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("accessControlType", reqVO.getAccessControlType());
        config.put("deptIds", reqVO.getDeptIds() == null ? Collections.emptyList() : reqVO.getDeptIds());
        config.put("userIds", reqVO.getUserIds() == null ? Collections.emptyList() : reqVO.getUserIds());
        return JsonUtils.toJsonString(config);
    }

    private AgentxChannelConfigRespVO convert(AgentxChannelConfigDO channel) {
        if (channel == null) {
            return null;
        }
        AgentxChannelConfigRespVO respVO = BeanUtils.toBean(channel, AgentxChannelConfigRespVO.class);
        fillRespDetails(channel, respVO);
        return respVO;
    }

    private void fillRespDetails(AgentxChannelConfigDO channel, AgentxChannelConfigRespVO respVO) {
        List<AgentxChannelAgentDO> channelAgents = channelAgentMapper.selectListByChannelId(channel.getId());
        respVO.setAgentIds(convertList(channelAgents, AgentxChannelAgentDO::getAgentId));
        Map<String, Object> config = JsonUtils.parseObject(channel.getConfig(), Map.class);
        if (config == null) {
            return;
        }
        respVO.setAccessControlType((String) config.get("accessControlType"));
        respVO.setDeptIds(convertIdList(config.get("deptIds")));
        respVO.setUserIds(convertIdList(config.get("userIds")));
    }

    private List<Long> convertIdList(Object value) {
        if (!(value instanceof Collection)) {
            return Collections.emptyList();
        }
        return convertList((Collection<Object>) value, item -> Long.valueOf(String.valueOf(item)));
    }

    private AgentxChannelConfigDO validateChannelExists(Long id) {
        AgentxChannelConfigDO channel = channelConfigMapper.selectById(id);
        if (channel == null) {
            throw exception(ErrorCodeConstants.CHANNEL_CONFIG_NOT_EXISTS);
        }
        return channel;
    }

    private void validateChannelType(String channelType) {
        if (!VALID_CHANNEL_TYPES.contains(channelType)) {
            throw exception(ErrorCodeConstants.CHANNEL_TYPE_INVALID);
        }
    }

    private void replaceChannelAgents(Long channelId, List<Long> agentIds) {
        channelAgentMapper.deleteByChannelId(channelId);
        if (CollUtil.isEmpty(agentIds)) {
            return;
        }
        channelAgentMapper.insertBatch(convertList(agentIds,
                agentId -> new AgentxChannelAgentDO().setChannelId(channelId).setAgentId(agentId).setEnabled(true)));
    }

    private void syncChannelToOpenfang(Long channelId) {
        AgentxChannelConfigDO channel = channelConfigMapper.selectById(channelId);
        if (channel == null) {
            return;
        }
        List<AgentxOpenfangInstanceDO> instances = openfangInstanceMapper.selectListByStatus(1);
        if (CollUtil.isEmpty(instances)) {
            metricsService.recordOpenfangCall(false);
            return;
        }
        AgentxOpenfangInstanceDO instance = instances.get(0);
        String endpoint = StrUtil.removeSuffix(instance.getEndpoint(), "/") + "/api/channels/sync";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String apiKey = openfangApiKeyCrypto.decrypt(instance.getApiKeyEncrypted());
            if (StrUtil.isNotBlank(apiKey)) {
                headers.setBearerAuth(apiKey);
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("channelId", channel.getId());
            payload.put("channelType", channel.getChannelType());
            payload.put("channelName", channel.getChannelName());
            payload.put("status", channel.getStatus());
            payload.put("config", JsonUtils.parseObject(channel.getConfig(), Map.class));
            restTemplate.exchange(endpoint, HttpMethod.POST, new HttpEntity<>(payload, headers), Object.class);
            metricsService.recordOpenfangCall(true);
        } catch (Exception ex) {
            metricsService.recordOpenfangCall(false);
        }
    }

    private AgentxChannelTestRespVO buildRespByOkField(AgentxChannelTestRespVO respVO, ResponseEntity<Map> response) {
        Object ok = response.getBody() == null ? null : response.getBody().get("ok");
        if (Boolean.TRUE.equals(ok)) {
            respVO.setSuccess(true);
            respVO.setMessage("✓ 连接成功");
            return respVO;
        }
        respVO.setSuccess(false);
        respVO.setMessage("✗ 连接失败：Token 无效");
        return respVO;
    }

    private String[] splitAuthPair(String token) {
        String[] values = StrUtil.splitToArray(token, ':');
        if (values == null || values.length != 2 || StrUtil.hasBlank(values[0], values[1])) {
            throw exception(ErrorCodeConstants.CHANNEL_TEST_CONNECT_FAILED, "认证参数格式需为 key:secret");
        }
        return values;
    }

}
