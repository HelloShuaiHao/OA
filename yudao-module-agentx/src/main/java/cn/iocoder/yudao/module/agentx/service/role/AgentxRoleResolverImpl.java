package cn.iocoder.yudao.module.agentx.service.role;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.config.AgentxRoleResolverProperties;
import cn.iocoder.yudao.module.agentx.service.metrics.AgentxMetricsService;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Service
public class AgentxRoleResolverImpl implements RoleResolver {

    private static final String CACHE_KEY_PREFIX = "agentx:role:resolve:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private DeptService deptService;
    @Resource
    private AgentxRoleResolverProperties roleResolverProperties;
    @Autowired(required = false)
    private AgentxMetricsService metricsService;

    @Override
    public List<Long> resolveRole(String roleType, Map<String, Object> context) {
        String normalizedType = StrUtil.blankToDefault(roleType, "custom_role");
        String cacheKey = CACHE_KEY_PREFIX + normalizedType + ":" + JsonUtils.toJsonString(context);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cached)) {
            List<Long> result = JsonUtils.parseArray(cached, Long.class);
            if (CollUtil.isNotEmpty(result)) {
                recordCacheMetric(true);
                return result;
            }
        }
        recordCacheMetric(false);
        List<Long> result = doResolve(normalizedType, context == null ? Collections.emptyMap() : context);
        if (CollUtil.isEmpty(result)) {
            result = Collections.singletonList(roleResolverProperties.getDefaultApproverId());
        }
        stringRedisTemplate.opsForValue().set(cacheKey, JsonUtils.toJsonString(result),
                Duration.ofMinutes(roleResolverProperties.getCacheMinutes()));
        return result;
    }

    private void recordCacheMetric(boolean hit) {
        if (metricsService != null) {
            metricsService.recordRoleResolveCache(hit);
        }
    }

    private List<Long> doResolve(String roleType, Map<String, Object> context) {
        switch (roleType) {
            case "direct_manager":
                return resolveDirectManager(context);
            case "department_head":
                return resolveDepartmentHead(context);
            case "department_vp":
                return resolveDepartmentVp(context);
            case "hr_manager":
                return queryUsersByKeyword("hr");
            case "finance_approver":
                return queryUsersByKeyword("finance");
            case "fixed_role":
                return parseLongList(context.get("roleUserIds"));
            case "custom_role":
                return parseLongList(context.get("customUserIds"));
            default:
                return Collections.emptyList();
        }
    }

    private List<Long> resolveDirectManager(Map<String, Object> context) {
        Long userId = parseLong(context.get("userId"));
        if (userId == null) {
            return Collections.emptyList();
        }
        AdminUserDO user = adminUserService.getUser(userId);
        if (user == null || user.getDeptId() == null) {
            return Collections.emptyList();
        }
        DeptDO dept = deptService.getDept(user.getDeptId());
        if (dept == null || dept.getLeaderUserId() == null || Objects.equals(dept.getLeaderUserId(), userId)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(dept.getLeaderUserId());
    }

    private List<Long> resolveDepartmentHead(Map<String, Object> context) {
        Long deptId = parseLong(context.get("deptId"));
        if (deptId == null) {
            Long userId = parseLong(context.get("userId"));
            if (userId != null) {
                AdminUserDO user = adminUserService.getUser(userId);
                deptId = user == null ? null : user.getDeptId();
            }
        }
        if (deptId == null) {
            return Collections.emptyList();
        }
        DeptDO dept = deptService.getDept(deptId);
        if (dept == null || dept.getLeaderUserId() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(dept.getLeaderUserId());
    }

    private List<Long> resolveDepartmentVp(Map<String, Object> context) {
        Long deptId = parseLong(context.get("deptId"));
        if (deptId == null) {
            return Collections.emptyList();
        }
        DeptDO dept = deptService.getDept(deptId);
        if (dept == null || dept.getParentId() == null || DeptDO.PARENT_ID_ROOT.equals(dept.getParentId())) {
            return Collections.emptyList();
        }
        DeptDO parent = deptService.getDept(dept.getParentId());
        if (parent == null || parent.getLeaderUserId() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(parent.getLeaderUserId());
    }

    private List<Long> queryUsersByKeyword(String keyword) {
        List<AdminUserDO> users = adminUserService.getUserListByNickname(keyword);
        if (CollUtil.isEmpty(users)) {
            return Collections.emptyList();
        }
        return convertList(users, AdminUserDO::getId);
    }

    @SuppressWarnings("unchecked")
    private List<Long> parseLongList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof Collection) {
            return convertList((Collection<Object>) value, this::parseLong);
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (StrUtil.startWith(str, "[")) {
                List<Long> list = JsonUtils.parseArray(str, Long.class);
                return list == null ? Collections.emptyList() : list;
            }
            List<String> split = StrUtil.split(str, ',');
            return convertList(split, this::parseLong);
        }
        return Collections.emptyList();
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

}
