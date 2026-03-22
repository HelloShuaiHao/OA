CREATE TABLE IF NOT EXISTS `agentx_channel_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `channel_type` VARCHAR(32) NOT NULL,
    `channel_name` VARCHAR(128) NOT NULL,
    `bot_token_encrypted` VARCHAR(512) NOT NULL,
    `config` TEXT,
    `status` TINYINT NOT NULL DEFAULT 1,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    KEY `idx_channel_type` (`tenant_id`, `channel_type`),
    KEY `idx_channel_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AgentX 渠道配置';

CREATE TABLE IF NOT EXISTS `agentx_channel_agent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `channel_id` BIGINT NOT NULL,
    `agent_id` BIGINT NOT NULL,
    `enabled` BIT(1) NOT NULL DEFAULT b'1',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_channel_agent` (`tenant_id`, `channel_id`, `agent_id`, `deleted`),
    KEY `idx_channel_id` (`tenant_id`, `channel_id`),
    KEY `idx_agent_id` (`tenant_id`, `agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道-数字员工关联';

CREATE TABLE IF NOT EXISTS `agentx_user_channel_binding` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `user_id` BIGINT NOT NULL,
    `channel_type` VARCHAR(32) NOT NULL,
    `channel_user_id` VARCHAR(128) NOT NULL,
    `channel_username` VARCHAR(128) DEFAULT NULL,
    `bind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `unbind_time` DATETIME DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_channel_user` (`tenant_id`, `channel_type`, `channel_user_id`, `deleted`),
    KEY `idx_user` (`tenant_id`, `user_id`),
    KEY `idx_user_channel` (`tenant_id`, `user_id`, `channel_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户渠道绑定';
