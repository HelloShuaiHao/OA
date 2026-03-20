package cn.iocoder.yudao.module.agentx.service.instance;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * OpenFang 实例健康检查任务。
 */
@Component
@Slf4j
public class OpenfangInstanceHealthScheduler {

    @Resource
    private OpenfangInstanceService openfangInstanceService;

    @Scheduled(initialDelay = 120, fixedRate = 300, timeUnit = TimeUnit.SECONDS)
    public void refreshHealth() {
        TenantUtils.executeIgnore(() -> {
            try {
                openfangInstanceService.refreshHealthStatus();
            } catch (Exception ex) {
                log.warn("[refreshHealth][刷新 OpenFang 实例健康状态失败]", ex);
            }
        });
    }

}
