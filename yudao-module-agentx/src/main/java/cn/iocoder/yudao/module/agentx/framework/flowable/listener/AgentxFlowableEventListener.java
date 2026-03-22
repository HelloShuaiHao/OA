package cn.iocoder.yudao.module.agentx.framework.flowable.listener;

import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;

/**
 * Flowable 事件桥接监听器，写入 AgentX 审计事件，便于统一检索流程执行轨迹。
 */
@Component
@Slf4j
public class AgentxFlowableEventListener implements FlowableEventListener {

    @Resource
    private AgentxAuditEventMapper auditEventMapper;

    @Override
    public void onEvent(FlowableEvent event) {
        if (event == null || event.getType() == null) {
            return;
        }
        String type = event.getType().name();
        log.debug("[AgentxFlowableEventListener] receive event type={}", type);
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setEventType("FLOWABLE_" + type)
                .setResultSummary("flowable-event=" + type));
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }

    @Override
    public Collection<? extends FlowableEventType> getTypes() {
        return Arrays.asList(
                FlowableEngineEventType.PROCESS_STARTED,
                FlowableEngineEventType.PROCESS_COMPLETED,
                FlowableEngineEventType.TASK_CREATED,
                FlowableEngineEventType.TASK_COMPLETED
        );
    }

}
