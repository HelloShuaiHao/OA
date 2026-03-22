package cn.iocoder.yudao.module.agentx.service.role;

import cn.iocoder.yudao.module.agentx.config.AgentxRoleResolverProperties;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AgentxRoleResolverImplTest {

    private final StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
    private final AdminUserService userService = Mockito.mock(AdminUserService.class);
    private final DeptService deptService = Mockito.mock(DeptService.class);
    private final AgentxRoleResolverProperties properties = new AgentxRoleResolverProperties();
    private final AgentxRoleResolverImpl service = buildService();

    @Test
    void shouldResolveDirectManagerAndCacheForFiveMinutes() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        AdminUserDO user = new AdminUserDO();
        user.setId(10L);
        user.setDeptId(20L);
        DeptDO dept = new DeptDO();
        dept.setId(20L);
        dept.setLeaderUserId(99L);
        when(userService.getUser(10L)).thenReturn(user);
        when(deptService.getDept(20L)).thenReturn(dept);

        List<Long> result = service.resolveRole("direct_manager", Map.of("userId", 10L));
        assertEquals(List.of(99L), result);

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(anyString(), valueCaptor.capture(), durationCaptor.capture());
        assertEquals("[99]", valueCaptor.getValue());
        assertEquals(Duration.ofMinutes(5), durationCaptor.getValue());
    }

    @Test
    void shouldUseFallbackApproverWhenNoApproverResolved() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        List<Long> result = service.resolveRole("custom_role", Map.of());
        assertEquals(List.of(200L), result);
    }

    @Test
    void shouldReturnCachedResultWhenCacheHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("[101,102]");

        List<Long> result = service.resolveRole("custom_role", Map.of("customUserIds", List.of(1L)));
        assertEquals(List.of(101L, 102L), result);
        verifyNoInteractions(userService, deptService);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    private AgentxRoleResolverImpl buildService() {
        properties.setDefaultApproverId(200L);
        properties.setCacheMinutes(5);
        AgentxRoleResolverImpl impl = new AgentxRoleResolverImpl();
        try {
            setField(impl, "stringRedisTemplate", redisTemplate);
            setField(impl, "adminUserService", userService);
            setField(impl, "deptService", deptService);
            setField(impl, "roleResolverProperties", properties);
            return impl;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
