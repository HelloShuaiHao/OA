SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- AgentX 顶层目录（管理后台）
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8800, 'AgentX', '', 1, 90, 0, 'agentx', 'ep:cpu', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 页面菜单
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8801, 'OpenFang 实例', '', 2, 10, 8800, 'instance', 'ep:connection', 'agentx/instance/index', 'AgentxInstance',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8802, '场景配置', '', 2, 20, 8800, 'scenario', 'ep:setting', 'agentx/scenario/index', 'AgentxScenario',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8803, '任务运行', '', 2, 30, 8800, 'task', 'ep:operation', 'agentx/task/index', 'AgentxTask',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8804, '审批绑定', '', 2, 40, 8800, 'approval', 'ep:checked', 'agentx/approval/index', 'AgentxApproval',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8805, '审计日志', '', 2, 50, 8800, 'audit', 'ep:document', 'agentx/audit/index', 'AgentxAudit',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8806, '数字员工', '', 2, 5, 8800, 'agent', 'ep:user', 'agentx/agent/index', 'AgentxAgentList',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8807, '渠道配置', '', 2, 60, 8800, 'channel', 'ep:chat-dot-round', 'agentx/channel/index', 'AgentxChannelConfig',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8808, '绑定管理', '', 2, 61, 8800, 'channel/binding', 'ep:link', 'agentx/channel/binding', 'AgentxChannelBinding',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(88061, '创建数字员工', '', 2, 1, 8806, 'create', 'ep:plus', 'agentx/agent/create', 'AgentxAgentCreate',
 0, b'0', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(88062, '数字员工详情', '', 2, 2, 8806, 'detail', 'ep:view', 'agentx/agent/detail', 'AgentxAgentDetail',
 0, b'0', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- OpenFang 实例按钮权限
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8810, '实例查询', 'agentx:instance:query', 3, 1, 8801, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8811, '实例创建', 'agentx:instance:create', 3, 2, 8801, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8812, '实例修改', 'agentx:instance:update', 3, 3, 8801, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8813, '实例删除', 'agentx:instance:delete', 3, 4, 8801, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8814, '实例测试连接', 'agentx:instance:test', 3, 5, 8801, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 场景配置按钮权限
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8820, '场景查询', 'agentx:scenario:query', 3, 1, 8802, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8821, '场景创建', 'agentx:scenario:create', 3, 2, 8802, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8822, '场景修改', 'agentx:scenario:update', 3, 3, 8802, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8823, '场景删除', 'agentx:scenario:delete', 3, 4, 8802, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 任务运行按钮权限（当前 create 接口复用 query 权限）
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8830, '任务查询', 'agentx:task:query', 3, 1, 8803, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 审批绑定按钮权限
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8840, '审批查询', 'agentx:approval:query', 3, 1, 8804, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 审计日志按钮权限
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8850, '审计查询', 'agentx:audit:query', 3, 1, 8805, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 数字员工按钮权限
INSERT IGNORE INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(8860, '员工查询', 'agentx:agent:query', 3, 1, 8806, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8861, '员工创建', 'agentx:agent:create', 3, 2, 8806, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8862, '员工修改', 'agentx:agent:update', 3, 3, 8806, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8863, '员工删除', 'agentx:agent:delete', 3, 4, 8806, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8870, '渠道查询', 'agentx:channel:query', 3, 1, 8807, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8871, '渠道创建', 'agentx:channel:create', 3, 2, 8807, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8872, '渠道修改', 'agentx:channel:update', 3, 3, 8807, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(8873, '渠道删除', 'agentx:channel:delete', 3, 4, 8807, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

SET FOREIGN_KEY_CHECKS = 1;
