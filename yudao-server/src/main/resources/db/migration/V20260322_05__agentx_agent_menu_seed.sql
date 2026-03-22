INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8806, '数字员工', '', 2, 5, 8800, 'agent', 'ep:user', 'agentx/agent/index', 'AgentxAgentList',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8807, '渠道配置', '', 2, 60, 8800, 'channel', 'ep:chat-dot-round', 'agentx/channel/index', 'AgentxChannelConfig',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8808, '绑定管理', '', 2, 61, 8800, 'channel/binding', 'ep:link', 'agentx/channel/binding', 'AgentxChannelBinding',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(88061, '创建数字员工', '', 2, 1, 8806, 'create', 'ep:plus', 'agentx/agent/create', 'AgentxAgentCreate',
 0, b'0', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(88062, '数字员工详情', '', 2, 2, 8806, 'detail', 'ep:view', 'agentx/agent/detail', 'AgentxAgentDetail',
 0, b'0', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8860, '员工查询', 'agentx:agent:query', 3, 1, 8806, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8861, '员工创建', 'agentx:agent:create', 3, 2, 8806, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8862, '员工修改', 'agentx:agent:update', 3, 3, 8806, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8863, '员工删除', 'agentx:agent:delete', 3, 4, 8806, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8870, '渠道查询', 'agentx:channel:query', 3, 1, 8807, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8871, '渠道创建', 'agentx:channel:create', 3, 2, 8807, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8872, '渠道修改', 'agentx:channel:update', 3, 3, 8807, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8873, '渠道删除', 'agentx:channel:delete', 3, 4, 8807, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 1, seeded.menu_id, '1', NOW(), '1', NOW(), b'0', 1
FROM (
    SELECT 8806 AS menu_id UNION ALL
    SELECT 8807 UNION ALL
    SELECT 8808 UNION ALL
    SELECT 88061 UNION ALL
    SELECT 88062 UNION ALL
    SELECT 8860 UNION ALL
    SELECT 8861 UNION ALL
    SELECT 8862 UNION ALL
    SELECT 8863 UNION ALL
    SELECT 8870 UNION ALL
    SELECT 8871 UNION ALL
    SELECT 8872 UNION ALL
    SELECT 8873
) seeded
WHERE NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` existing
    WHERE existing.role_id = 1
      AND existing.menu_id = seeded.menu_id
      AND existing.deleted = b'0'
      AND existing.tenant_id = 1
);
