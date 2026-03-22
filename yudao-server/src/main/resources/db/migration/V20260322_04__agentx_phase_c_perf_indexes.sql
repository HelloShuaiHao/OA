-- Phase C 性能优化索引

-- 任务投影：优化按状态统计与时间范围分页查询
CREATE INDEX `idx_projection_status_deleted`
    ON `agentx_task_projection` (`projection_status`, `deleted`);

CREATE INDEX `idx_tenant_create_time_deleted`
    ON `agentx_task_projection` (`tenant_id`, `create_time`, `deleted`);

-- 审批绑定：优化按决策状态统计查询
CREATE INDEX `idx_decision_status_deleted`
    ON `agentx_approval_binding` (`decision_status`, `deleted`);
