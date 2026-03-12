SET NAMES utf8mb4;

INSERT INTO bpm_category (`id`, `name`, `code`, `description`, `status`, `sort`,
                          `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 9001, 'OA 审批', 'oa', '单人演示用流程分类', 0, 1,
       'admin', NOW(), 'admin', NOW(), b'0', 1
WHERE NOT EXISTS (
    SELECT 1 FROM bpm_category WHERE code = 'oa' AND deleted = b'0' AND tenant_id = 1
);

UPDATE act_re_model
SET CATEGORY_ = 'oa'
WHERE KEY_ = 'oa_leave' AND TENANT_ID_ = '1';

UPDATE act_re_procdef
SET CATEGORY_ = 'oa'
WHERE KEY_ = 'oa_leave' AND TENANT_ID_ = '1';
