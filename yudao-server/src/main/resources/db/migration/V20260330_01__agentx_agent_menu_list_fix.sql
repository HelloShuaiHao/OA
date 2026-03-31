INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(88060, '员工列表', '', 2, 1, 8806, 'list', 'ep:list', 'agentx/agent/index', 'AgentxAgentList',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

UPDATE `system_menu`
SET `visible` = b'0'
WHERE `id` = 88061
  AND `visible` = b'1';

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 1, 88060, '1', NOW(), '1', NOW(), b'0', 1
WHERE NOT EXISTS (
    SELECT 1
    FROM `system_role_menu`
    WHERE `role_id` = 1
      AND `menu_id` = 88060
      AND `deleted` = b'0'
      AND `tenant_id` = 1
);
