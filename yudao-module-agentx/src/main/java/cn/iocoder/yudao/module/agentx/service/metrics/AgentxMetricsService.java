package cn.iocoder.yudao.module.agentx.service.metrics;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AgentxMetricsService {

    private static final int DECISION_APPROVED = 10;
    private static final int DECISION_REJECTED = 20;

    private final MeterRegistry meterRegistry;
    @Resource
    private AgentxAgentMapper agentMapper;
    @Resource
    private AgentxTaskProjectionMapper taskProjectionMapper;
    @Resource
    private AgentxApprovalBindingMapper approvalBindingMapper;

    private final AtomicLong openfangCallTotal = new AtomicLong(0);
    private final AtomicLong openfangCallSuccess = new AtomicLong(0);
    private final AtomicLong apiCallTotal = new AtomicLong(0);
    private final AtomicLong apiCallSuccess = new AtomicLong(0);
    private final AtomicLong roleResolveCacheTotal = new AtomicLong(0);
    private final AtomicLong roleResolveCacheHit = new AtomicLong(0);
    private final AtomicLong contextAssemblyTotal = new AtomicLong(0);
    private final AtomicLong contextAssemblySuccess = new AtomicLong(0);
    private final Timer apiDurationTimer;
    private final Timer contextAssemblyDurationTimer;

    public AgentxMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.apiDurationTimer = Timer.builder("agentx_api_duration_seconds")
                .description("AgentX API 响应时长（秒）")
                .register(meterRegistry);
        this.contextAssemblyDurationTimer = Timer.builder("agentx_context_assembly_duration_seconds")
                .description("上下文组装时长（秒）")
                .register(meterRegistry);
    }

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("agentx_agent_total", agentMapper, mapper -> mapper.selectCount())
                .description("Agent 总数")
                .register(meterRegistry);
        Gauge.builder("agentx_task_total", taskProjectionMapper, mapper -> mapper.selectCount())
                .description("任务总数")
                .register(meterRegistry);
        Gauge.builder("agentx_task_success_rate", this, AgentxMetricsService::taskSuccessRate)
                .description("任务成功率")
                .register(meterRegistry);
        Gauge.builder("agentx_approval_total", approvalBindingMapper, mapper -> mapper.selectCount())
                .description("审批总数")
                .register(meterRegistry);
        Gauge.builder("agentx_approval_pass_rate", this, AgentxMetricsService::approvalPassRate)
                .description("审批通过率")
                .register(meterRegistry);
        Gauge.builder("agentx_openfang_call_total", openfangCallTotal, AtomicLong::get)
                .description("OpenFang 调用次数")
                .register(meterRegistry);
        Gauge.builder("agentx_openfang_call_success_rate", this, AgentxMetricsService::openfangSuccessRate)
                .description("OpenFang 调用成功率")
                .register(meterRegistry);
        Gauge.builder("agentx_api_availability", this, AgentxMetricsService::apiAvailabilityRate)
                .description("AgentX API 可用性（成功率）")
                .register(meterRegistry);
        Gauge.builder("agentx_role_resolve_cache_hit_rate", this, AgentxMetricsService::roleResolveCacheHitRate)
                .description("角色解析缓存命中率")
                .register(meterRegistry);
        Gauge.builder("agentx_context_assembly_success_rate", this, AgentxMetricsService::contextAssemblySuccessRate)
                .description("上下文组装成功率")
                .register(meterRegistry);
    }

    public void recordOpenfangCall(boolean success) {
        openfangCallTotal.incrementAndGet();
        if (success) {
            openfangCallSuccess.incrementAndGet();
        }
    }

    public void recordApiDuration(long durationNanos) {
        apiDurationTimer.record(durationNanos, TimeUnit.NANOSECONDS);
    }

    public void recordApiResult(int statusCode) {
        apiCallTotal.incrementAndGet();
        if (statusCode >= 200 && statusCode < 400) {
            apiCallSuccess.incrementAndGet();
        }
    }

    public void recordRoleResolveCache(boolean hit) {
        roleResolveCacheTotal.incrementAndGet();
        if (hit) {
            roleResolveCacheHit.incrementAndGet();
        }
    }

    public void recordContextAssembly(long durationNanos, boolean success) {
        contextAssemblyTotal.incrementAndGet();
        if (success) {
            contextAssemblySuccess.incrementAndGet();
        }
        contextAssemblyDurationTimer.record(durationNanos, TimeUnit.NANOSECONDS);
    }

    private double openfangSuccessRate() {
        long total = openfangCallTotal.get();
        if (total == 0) {
            return 1D;
        }
        return (double) openfangCallSuccess.get() / total;
    }

    private double taskSuccessRate() {
        long total = taskProjectionMapper.selectCount();
        if (total == 0) {
            return 1D;
        }
        long success = taskProjectionMapper.selectCount(new LambdaQueryWrapperX<AgentxTaskProjectionDO>()
                .eq(AgentxTaskProjectionDO::getProjectionStatus, AgentxTaskProjectionStatusEnum.SUCCEEDED.getStatus()));
        return (double) success / total;
    }

    private double apiAvailabilityRate() {
        long total = apiCallTotal.get();
        if (total == 0) {
            return 1D;
        }
        return (double) apiCallSuccess.get() / total;
    }

    private double roleResolveCacheHitRate() {
        long total = roleResolveCacheTotal.get();
        if (total == 0) {
            return 1D;
        }
        return (double) roleResolveCacheHit.get() / total;
    }

    private double contextAssemblySuccessRate() {
        long total = contextAssemblyTotal.get();
        if (total == 0) {
            return 1D;
        }
        return (double) contextAssemblySuccess.get() / total;
    }

    private double approvalPassRate() {
        long total = approvalBindingMapper.selectCount(new LambdaQueryWrapperX<AgentxApprovalBindingDO>()
                .in(AgentxApprovalBindingDO::getDecisionStatus, DECISION_APPROVED, DECISION_REJECTED));
        if (total == 0) {
            return 1D;
        }
        long pass = approvalBindingMapper.selectCount(new LambdaQueryWrapperX<AgentxApprovalBindingDO>()
                .eq(AgentxApprovalBindingDO::getDecisionStatus, DECISION_APPROVED));
        return (double) pass / total;
    }

}
