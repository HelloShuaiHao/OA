package cn.iocoder.yudao.module.agentx.service.channel;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.config.AgentxBindProperties;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxUserChannelBindingDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxUserChannelBindingMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentxBindServiceImplTest {

    private final AgentxUserChannelBindingMapper bindingMapper = Mockito.mock(AgentxUserChannelBindingMapper.class);
    private final AgentxChannelService channelService = Mockito.mock(AgentxChannelService.class);
    private final AgentxBindProperties bindProperties = new AgentxBindProperties();
    private final AgentxBindServiceImpl service = buildService();

    @Test
    void shouldGenerateSignedBindTokenWithRequiredClaims() {
        AgentxBindGenerateReqVO req = new AgentxBindGenerateReqVO();
        req.setChannelType("telegram");
        req.setChannelUserId("123456");
        req.setChannelUsername("alice");

        AgentxBindGenerateRespVO resp = service.generateBindLink(req);
        assertTrue(resp.getBindUrl().startsWith("https://oa.company.com/agentx/bind?token="));
        assertNotNull(resp.getToken());

        JWT jwt = JWTUtil.parseToken(resp.getToken());
        assertEquals("telegram", String.valueOf(jwt.getPayload("channel_type")));
        assertEquals("123456", String.valueOf(jwt.getPayload("channel_user_id")));
        assertEquals("alice", String.valueOf(jwt.getPayload("channel_username")));
        assertTrue(Long.parseLong(String.valueOf(jwt.getPayload("exp"))) > System.currentTimeMillis());
    }

    @Test
    void shouldCreateBindingWhenTokenValidAndNotBoundBefore() {
        when(bindingMapper.selectByChannelIdentity("telegram", "10001")).thenReturn(null);
        when(bindingMapper.selectLatestByChannelIdentity("telegram", "10001")).thenReturn(null);

        String token = tokenOf("telegram", "10001", "tom", System.currentTimeMillis() + 60_000L);
        service.confirmBind(token, 99L);

        ArgumentCaptor<AgentxUserChannelBindingDO> captor = ArgumentCaptor.forClass(AgentxUserChannelBindingDO.class);
        verify(bindingMapper).insert(captor.capture());
        AgentxUserChannelBindingDO saved = captor.getValue();
        assertEquals(99L, saved.getUserId());
        assertEquals("telegram", saved.getChannelType());
        assertEquals("10001", saved.getChannelUserId());
        assertEquals(1, saved.getStatus());
        verify(channelService).refreshRuntimeAccessByChannelType("telegram");
    }

    @Test
    void shouldReactivateHistoricalBindingWhenExists() {
        when(bindingMapper.selectByChannelIdentity("telegram", "10001")).thenReturn(null);
        AgentxUserChannelBindingDO history = new AgentxUserChannelBindingDO();
        history.setId(123L);
        history.setStatus(0);
        when(bindingMapper.selectLatestByChannelIdentity("telegram", "10001")).thenReturn(history);

        String token = tokenOf("telegram", "10001", "tom", System.currentTimeMillis() + 60_000L);
        service.confirmBind(token, 99L);

        ArgumentCaptor<AgentxUserChannelBindingDO> captor = ArgumentCaptor.forClass(AgentxUserChannelBindingDO.class);
        verify(bindingMapper).updateById(captor.capture());
        verify(bindingMapper, never()).insert(any(AgentxUserChannelBindingDO.class));
        AgentxUserChannelBindingDO updated = captor.getValue();
        assertEquals(123L, updated.getId());
        assertEquals(99L, updated.getUserId());
        assertEquals(1, updated.getStatus());
        assertNull(updated.getUnbindTime());
        verify(channelService).refreshRuntimeAccessByChannelType("telegram");
    }

    @Test
    void shouldReturnFriendlyMessageWhenAlreadyBoundByCurrentUser() {
        AgentxUserChannelBindingDO existed = new AgentxUserChannelBindingDO();
        existed.setUserId(99L);
        existed.setStatus(1);
        when(bindingMapper.selectByChannelIdentity("telegram", "10001")).thenReturn(existed);

        String token = tokenOf("telegram", "10001", "tom", System.currentTimeMillis() + 60_000L);
        assertEquals("您已绑定，无需重复操作", service.confirmBind(token, 99L).getMessage());
        verify(bindingMapper, never()).insert(any(AgentxUserChannelBindingDO.class));
        verify(channelService).refreshRuntimeAccessByChannelType("telegram");
    }

    @Test
    void shouldThrowExpiredWhenTokenExpired() {
        String token = tokenOf("telegram", "10001", "tom", System.currentTimeMillis() - 1_000L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.confirmBind(token, 99L));
        assertEquals(ErrorCodeConstants.CHANNEL_BIND_TOKEN_EXPIRED.getCode(), ex.getCode());
    }

    private String tokenOf(String type, String channelUserId, String channelUsername, long expMillis) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("channel_type", type);
        payload.put("channel_user_id", channelUserId);
        payload.put("channel_username", channelUsername);
        payload.put("iat", System.currentTimeMillis());
        payload.put("exp", expMillis);
        return JWTUtil.createToken(payload, bindProperties.getSecret().getBytes());
    }

    private AgentxBindServiceImpl buildService() {
        bindProperties.setBaseUrl("https://oa.company.com");
        bindProperties.setSecret("agentx-bind-secret-ut");
        bindProperties.setExpireMinutes(10);
        AgentxBindServiceImpl impl = new AgentxBindServiceImpl();
        try {
            java.lang.reflect.Field bindPropertiesField = AgentxBindServiceImpl.class.getDeclaredField("bindProperties");
            bindPropertiesField.setAccessible(true);
            bindPropertiesField.set(impl, bindProperties);
            java.lang.reflect.Field mapperField = AgentxBindServiceImpl.class.getDeclaredField("userChannelBindingMapper");
            mapperField.setAccessible(true);
            mapperField.set(impl, bindingMapper);
            java.lang.reflect.Field channelServiceField = AgentxBindServiceImpl.class.getDeclaredField("channelService");
            channelServiceField.setAccessible(true);
            channelServiceField.set(impl, channelService);
            return impl;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
