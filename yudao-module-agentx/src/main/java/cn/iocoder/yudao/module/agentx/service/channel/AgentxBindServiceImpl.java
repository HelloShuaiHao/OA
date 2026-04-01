package cn.iocoder.yudao.module.agentx.service.channel;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.module.agentx.config.AgentxBindProperties;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindConfirmRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxUserChannelBindingDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxUserChannelBindingMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class AgentxBindServiceImpl implements AgentxBindService {

    @Resource
    private AgentxBindProperties bindProperties;
    @Resource
    private AgentxUserChannelBindingMapper userChannelBindingMapper;
    @Resource
    @Lazy
    private AgentxChannelService channelService;
    @Resource
    private WebProperties webProperties;

    @Override
    public AgentxBindGenerateRespVO generateBindLink(AgentxBindGenerateReqVO reqVO) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("channel_type", reqVO.getChannelType());
        payload.put("channel_user_id", reqVO.getChannelUserId());
        payload.put("channel_username", reqVO.getChannelUsername());
        payload.put("exp", DateUtil.offset(new Date(), DateField.MINUTE, bindProperties.getExpireMinutes()).getTime());
        payload.put("iat", System.currentTimeMillis());
        String token = JWTUtil.createToken(payload, bindProperties.getSecret().getBytes());

        AgentxBindGenerateRespVO respVO = new AgentxBindGenerateRespVO();
        respVO.setToken(token);
        respVO.setBindUrl(StrUtil.removeSuffix(resolveBindBaseUrl(), "/") + "/agentx/bind?token=" + token);
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentxBindConfirmRespVO confirmBind(String token, Long userId) {
        if (StrUtil.isBlank(token) || !JWTUtil.verify(token, bindProperties.getSecret().getBytes())) {
            throw exception(ErrorCodeConstants.CHANNEL_BIND_TOKEN_INVALID);
        }
        JWT jwt = JWTUtil.parseToken(token);
        Object exp = jwt.getPayload("exp");
        if (exp == null || Long.parseLong(String.valueOf(exp)) < System.currentTimeMillis()) {
            throw exception(ErrorCodeConstants.CHANNEL_BIND_TOKEN_EXPIRED);
        }
        String channelType = String.valueOf(jwt.getPayload("channel_type"));
        String channelUserId = String.valueOf(jwt.getPayload("channel_user_id"));
        String channelUsername = String.valueOf(jwt.getPayload("channel_username"));
        AgentxUserChannelBindingDO activeBinding = userChannelBindingMapper.selectByChannelIdentity(channelType, channelUserId);
        if (activeBinding != null) {
            if (activeBinding.getUserId().equals(userId) && activeBinding.getStatus() == 1) {
                AgentxBindConfirmRespVO respVO = new AgentxBindConfirmRespVO();
                respVO.setBound(true);
                respVO.setMessage("您已绑定，无需重复操作");
                refreshRuntimeAccess(channelType);
                return respVO;
            }
            throw exception(ErrorCodeConstants.CHANNEL_BIND_ALREADY_EXISTS);
        }
        AgentxUserChannelBindingDO latestBinding = userChannelBindingMapper
                .selectLatestByChannelIdentity(channelType, channelUserId);
        if (latestBinding != null) {
            // 复用历史解绑记录，避免唯一索引冲突导致“解绑后无法再次绑定”。
            userChannelBindingMapper.updateById(new AgentxUserChannelBindingDO()
                    .setId(latestBinding.getId())
                    .setUserId(userId)
                    .setChannelUsername(channelUsername)
                    .setBindTime(LocalDateTime.now())
                    .setUnbindTime(null)
                    .setStatus(1));
        } else {
            AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO()
                    .setUserId(userId)
                    .setChannelType(channelType)
                    .setChannelUserId(channelUserId)
                    .setChannelUsername(channelUsername)
                    .setBindTime(LocalDateTime.now())
                    .setStatus(1);
            userChannelBindingMapper.insert(binding);
        }
        AgentxBindConfirmRespVO respVO = new AgentxBindConfirmRespVO();
        respVO.setBound(true);
        respVO.setMessage("✓ 绑定成功！现在可以返回渠道继续对话");
        refreshRuntimeAccess(channelType);
        return respVO;
    }

    private void refreshRuntimeAccess(String channelType) {
        channelService.refreshRuntimeAccessByChannelType(channelType);
    }

    private String resolveBindBaseUrl() {
        String baseUrl = bindProperties.getBaseUrl();
        if (StrUtil.isBlank(baseUrl)
                && webProperties != null
                && webProperties.getAdminUi() != null) {
            baseUrl = webProperties.getAdminUi().getUrl();
        }
        return StrUtil.blankToDefault(baseUrl, "http://localhost:48080");
    }

}
