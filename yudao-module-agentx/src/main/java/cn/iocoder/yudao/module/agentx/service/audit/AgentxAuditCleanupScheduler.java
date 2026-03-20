package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AgentxAuditCleanupScheduler {

    private static final int RETENTION_DAYS = 180;

    @Resource
    private AgentxAuditEventMapper auditEventMapper;

    @Scheduled(initialDelay = 10, fixedRate = 24, timeUnit = TimeUnit.HOURS)
    public void cleanupExpiredEvents() {
        TenantUtils.executeIgnore(() -> {
            LocalDateTime deadline = LocalDateTime.now().minusDays(RETENTION_DAYS);
            int deleted = auditEventMapper.deleteByCreateTimeBefore(deadline);
            if (deleted > 0) {
                log.info("[cleanupExpiredEvents][deleted={}][deadline={}]", deleted, deadline);
            }
        });
    }

}
