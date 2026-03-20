package cn.iocoder.yudao.module.agentx.scenario.leave;

import java.util.Arrays;
import java.util.List;

/**
 * 请假场景 Tool 集声明。
 */
public class LeaveToolSet {

    public List<String> toolCodes() {
        return Arrays.asList("bpm_query_tasks", "bpm_approve");
    }

}
