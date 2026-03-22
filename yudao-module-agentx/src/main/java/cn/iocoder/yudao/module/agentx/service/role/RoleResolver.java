package cn.iocoder.yudao.module.agentx.service.role;

import java.util.List;
import java.util.Map;

public interface RoleResolver {

    List<Long> resolveRole(String roleType, Map<String, Object> context);

}
