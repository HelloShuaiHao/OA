package cn.iocoder.yudao.module.agentx.service.context.provider;

import cn.iocoder.yudao.module.agentx.service.context.ContextProvider;
import cn.iocoder.yudao.module.agentx.service.context.ContextRequest;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户信息上下文 Provider。
 */
@Component
public class UserContextProvider implements ContextProvider {

    @Resource
    private AdminUserService adminUserService;

    @Override
    public String getType() {
        return "user_profile";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> provide(ContextRequest request) {
        if (request.getUserId() == null) {
            return new HashMap<>();
        }
        AdminUserDO user = adminUserService.getUser(request.getUserId());
        if (user == null) {
            return new HashMap<>();
        }

        List<String> fields = new ArrayList<>();
        Object rawFields = request.getParams().get("fields");
        if (rawFields instanceof List) {
            for (Object field : (List<Object>) rawFields) {
                fields.add(String.valueOf(field));
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (fields.isEmpty() || fields.contains("id")) {
            result.put("id", user.getId());
        }
        if (fields.isEmpty() || fields.contains("username")) {
            result.put("username", user.getUsername());
        }
        if (fields.isEmpty() || fields.contains("nickname") || fields.contains("name")) {
            result.put("name", user.getNickname());
        }
        if (fields.isEmpty() || fields.contains("dept")) {
            result.put("deptId", user.getDeptId());
        }
        if (fields.isEmpty() || fields.contains("mobile")) {
            result.put("mobile", user.getMobile());
        }
        if (fields.isEmpty() || fields.contains("email")) {
            result.put("email", user.getEmail());
        }
        return result;
    }

}
