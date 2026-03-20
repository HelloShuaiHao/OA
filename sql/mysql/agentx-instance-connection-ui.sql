CREATE TABLE IF NOT EXISTS `agentx_openfang_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `instance_name` VARCHAR(64) NOT NULL,
    `endpoint` VARCHAR(255) NOT NULL,
    `api_key_encrypted` VARCHAR(512) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `version` VARCHAR(32) DEFAULT NULL,
    `last_heartbeat` DATETIME DEFAULT NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    KEY `idx_tenant` (`tenant_id`),
    UNIQUE KEY `uk_tenant_instance_name` (`tenant_id`, `instance_name`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AgentX OpenFang 实例配置';
