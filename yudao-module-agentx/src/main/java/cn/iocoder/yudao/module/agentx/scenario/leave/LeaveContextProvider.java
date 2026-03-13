package cn.iocoder.yudao.module.agentx.scenario.leave;

import java.util.Arrays;
import java.util.List;

/**
 * 请假场景上下文声明。
 */
public class LeaveContextProvider {

    public List<String> requiredContextKeys() {
        return Arrays.asList("leave.form", "leave.balance", "applicant.profile");
    }

}
