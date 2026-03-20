CREATE TABLE IF NOT EXISTS `agentx_approval_binding` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `scenario_code` VARCHAR(64) NOT NULL COMMENT '场景编码',
    `business_key` VARCHAR(128) NOT NULL COMMENT '业务主键',
    `openfang_task_run_id` VARCHAR(64) NOT NULL COMMENT 'OpenFang 任务运行 ID',
    `openfang_approval_id` VARCHAR(64) NOT NULL COMMENT 'OpenFang 审批 ID',
    `bpm_process_instance_id` VARCHAR(64) DEFAULT NULL COMMENT 'OA BPM 流程实例 ID',
    `risk_level` TINYINT DEFAULT NULL COMMENT '风险等级',
    `decision_status` TINYINT DEFAULT NULL COMMENT '审批决策状态',
    `callback_retry_count` INT NOT NULL DEFAULT 0 COMMENT '回调重试次数',
    `callback_failed` BIT(1) NOT NULL DEFAULT b'0' COMMENT '回调是否失败',
    `callback_last_error` VARCHAR(512) DEFAULT NULL COMMENT '回调最后一次错误',
    `action_summary` VARCHAR(512) DEFAULT NULL COMMENT '审批动作摘要',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_openfang_approval_id` (`tenant_id`, `openfang_approval_id`, `deleted`),
    KEY `idx_tenant_openfang_task_run_id` (`tenant_id`, `openfang_task_run_id`, `deleted`),
    KEY `idx_tenant_business_key` (`tenant_id`, `business_key`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AgentX 审批绑定';

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_approval_binding'
          AND COLUMN_NAME = 'callback_retry_count'
    ),
    'SELECT 1',
    'ALTER TABLE `agentx_approval_binding` ADD COLUMN `callback_retry_count` INT NOT NULL DEFAULT 0 COMMENT ''回调重试次数'' AFTER `decision_status`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_approval_binding'
          AND COLUMN_NAME = 'callback_failed'
    ),
    'SELECT 1',
    'ALTER TABLE `agentx_approval_binding` ADD COLUMN `callback_failed` BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''回调是否失败'' AFTER `callback_retry_count`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_approval_binding'
          AND COLUMN_NAME = 'callback_last_error'
    ),
    'SELECT 1',
    'ALTER TABLE `agentx_approval_binding` ADD COLUMN `callback_last_error` VARCHAR(512) NULL DEFAULT NULL COMMENT ''回调最后一次错误'' AFTER `callback_failed`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
