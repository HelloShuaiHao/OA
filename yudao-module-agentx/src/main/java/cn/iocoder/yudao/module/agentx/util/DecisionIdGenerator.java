package cn.iocoder.yudao.module.agentx.util;

import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

@Component
public class DecisionIdGenerator {

    public String generate() {
        return "dec_" + IdUtil.fastSimpleUUID();
    }

}
