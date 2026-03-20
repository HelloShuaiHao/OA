SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_scenario_config'
          AND COLUMN_NAME = 'tenant_id'
    ),
    'SELECT 1',
    'ALTER TABLE `agentx_scenario_config` ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 AFTER `id`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_scenario_config'
          AND INDEX_NAME = 'uk_scenario_code'
    ),
    'ALTER TABLE `agentx_scenario_config` DROP INDEX `uk_scenario_code`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_scenario_config'
          AND INDEX_NAME = 'uk_tenant_scenario_code'
    ),
    'SELECT 1',
    'ALTER TABLE `agentx_scenario_config` ADD UNIQUE KEY `uk_tenant_scenario_code` (`tenant_id`, `scenario_code`, `deleted`)'
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
          AND COLUMN_NAME = 'tenant_id'
    ),
    'SELECT 1',
    'ALTER TABLE `agentx_approval_binding` ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 AFTER `id`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_approval_binding'
          AND INDEX_NAME = 'uk_openfang_approval_id'
    ),
    'ALTER TABLE `agentx_approval_binding` DROP INDEX `uk_openfang_approval_id`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'agentx_approval_binding'
          AND INDEX_NAME = 'uk_tenant_openfang_approval_id'
    ),
    'SELECT 1',
    'ALTER TABLE `agentx_approval_binding` ADD UNIQUE KEY `uk_tenant_openfang_approval_id` (`tenant_id`, `openfang_approval_id`, `deleted`)'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
