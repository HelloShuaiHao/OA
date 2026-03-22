ALTER TABLE `system_users`
    ADD COLUMN `user_type` VARCHAR(16) NOT NULL DEFAULT 'human' COMMENT '用户类型：human=真实员工,agent=数字员工' AFTER `status`,
    ADD COLUMN `agent_id` BIGINT NULL COMMENT '关联 Agent ID（仅数字员工）' AFTER `user_type`;

CREATE INDEX `idx_system_users_user_type` ON `system_users` (`user_type`);
CREATE INDEX `idx_system_users_tenant_type` ON `system_users` (`tenant_id`, `user_type`);
CREATE INDEX `idx_system_users_agent_id` ON `system_users` (`agent_id`);

ALTER TABLE `agentx_agent`
    ADD COLUMN `config_version` INT NOT NULL DEFAULT 1 COMMENT '配置版本号' AFTER `status`,
    ADD COLUMN `last_sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态：0=未同步,1=成功,2=失败' AFTER `config_version`,
    ADD COLUMN `last_sync_time` DATETIME NULL COMMENT '最近同步时间' AFTER `last_sync_status`,
    ADD COLUMN `last_sync_message` VARCHAR(255) NULL COMMENT '最近同步结果摘要' AFTER `last_sync_time`;

CREATE TABLE IF NOT EXISTS `agentx_agent_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `template_type` VARCHAR(64) NOT NULL,
    `template_name` VARCHAR(64) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `icon` VARCHAR(64) DEFAULT NULL,
    `default_capabilities` JSON NULL,
    `recommended_process_keys` JSON NULL,
    `default_rules` JSON NULL,
    `enabled` BIT(1) NOT NULL DEFAULT b'1',
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agentx_template_type` (`tenant_id`, `template_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AgentX 模板库';

CREATE TABLE IF NOT EXISTS `agentx_agent_config_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    `agent_id` BIGINT NOT NULL,
    `version_no` INT NOT NULL,
    `snapshot` LONGTEXT NOT NULL COMMENT '配置快照 JSON',
    `sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待同步,1=成功,2=失败',
    `sync_message` VARCHAR(255) NULL,
    `sync_time` DATETIME NULL,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agentx_agent_version` (`tenant_id`, `agent_id`, `version_no`, `deleted`),
    KEY `idx_agentx_agent_version_agent` (`tenant_id`, `agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AgentX 配置版本';

INSERT INTO `agentx_agent_template`
(`tenant_id`, `template_type`, `template_name`, `description`, `icon`, `default_capabilities`, `recommended_process_keys`, `default_rules`)
VALUES
(0, 'leave', '请假审批助手', '自动处理员工请假申请', 'ep:calendar',
 JSON_ARRAY('approve_task', 'query_task', 'workflow_route', 'approval_record'),
 JSON_ARRAY('leave_approval_standard', 'leave_approval_executive'),
 JSON_ARRAY(
     JSON_OBJECT('field', 'leaveDays', 'operator', '<=', 'value', '2', 'processDefinitionKey', 'leave_approval_standard'),
     JSON_OBJECT('field', 'leaveDays', 'operator', '>', 'value', '2', 'processDefinitionKey', 'leave_approval_executive')
 )),
(0, 'expense', '报销审批助手', '自动处理报销审批单据', 'ep:money',
 JSON_ARRAY('approve_task', 'query_task', 'workflow_route', 'approval_record'),
 JSON_ARRAY('expense_approval_standard', 'expense_approval_high_amount'),
 JSON_ARRAY(
     JSON_OBJECT('field', 'amount', 'operator', '<=', 'value', '5000', 'processDefinitionKey', 'expense_approval_standard'),
     JSON_OBJECT('field', 'amount', 'operator', '>', 'value', '5000', 'processDefinitionKey', 'expense_approval_high_amount')
 )),
(0, 'procurement', '采购审批助手', '处理采购申请与预算校验', 'ep:goods',
 JSON_ARRAY('approve_task', 'query_task', 'workflow_route', 'query_form'),
 JSON_ARRAY('procurement_approval_standard'),
 JSON_ARRAY()),
(0, 'crm', '客户跟进助手', '跟进客户并推动转化流程', 'ep:user',
 JSON_ARRAY('query_user', 'query_form', 'notify_im'),
 JSON_ARRAY('customer_followup'),
 JSON_ARRAY()),
(0, 'analysis', '数据分析助手', '进行数据查询与分析汇总', 'ep:data-analysis',
 JSON_ARRAY('query_report', 'query_form', 'notify_mail'),
 JSON_ARRAY('analysis_report'),
 JSON_ARRAY()),
(0, 'doc', '文档处理助手', '文档解析、归档与流转', 'ep:document',
 JSON_ARRAY('query_form', 'notify_im'),
 JSON_ARRAY('document_process'),
 JSON_ARRAY()),
(0, 'custom', '自定义', '完全自定义能力与流程', 'ep:setting',
 JSON_ARRAY(),
 JSON_ARRAY(),
 JSON_ARRAY());
