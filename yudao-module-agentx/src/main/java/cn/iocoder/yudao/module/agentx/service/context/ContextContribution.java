package cn.iocoder.yudao.module.agentx.service.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ContextContribution {

    private ContextSource source;
    private ContextLayer layer;
    private Map<String, Object> values = Collections.emptyMap();

}
