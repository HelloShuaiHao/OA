CREATE TABLE IF NOT EXISTS `agentx_scenario_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `scenario_code` VARCHAR(64) NOT NULL,
    `scenario_name` VARCHAR(128) NOT NULL,
    `openfang_workflow_id` VARCHAR(64) NOT NULL,
    `workflow_version` VARCHAR(32) DEFAULT NULL,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `config` JSON DEFAULT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_scenario_code` (`tenant_id`, `scenario_code`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AgentX 场景配置';
