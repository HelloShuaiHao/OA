INSERT INTO `agentx_scenario_config` (
    `scenario_code`, `scenario_name`, `openfang_workflow_id`, `workflow_version`, `enabled`, `config`
)
SELECT
    'oa.leave.approval',
    '请假审批助手',
    'leave-approval-assistant',
    '1.0.0',
    1,
    JSON_OBJECT(
        'autoApproveMaxDays', 2,
        'contextProviders', JSON_ARRAY(
            JSON_OBJECT('type', 'bpm_tasks', 'filter', JSON_OBJECT('processKey', 'leave', 'status', 'pending')),
            JSON_OBJECT('type', 'user_profile', 'fields', JSON_ARRAY('name', 'dept', 'position'))
        )
    )
WHERE NOT EXISTS (
    SELECT 1 FROM `agentx_scenario_config` WHERE `scenario_code` = 'oa.leave.approval' AND `deleted` = b'0'
);
