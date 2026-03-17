-- 准备测试数据：创建3条请假申请

-- 用户 104: 请假 2 天（应该自动通过）
INSERT INTO bpm_oa_leave (user_id, type, reason, start_time, end_time, day, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (104, '2', '家里有事', '2026-03-20 09:00:00', '2026-03-21 18:00:00', 2, 1, '104', NOW(), '104', NOW(), 0, 1);

-- 用户 105: 请假 5 天（需要确认）
INSERT INTO bpm_oa_leave (user_id, type, reason, start_time, end_time, day, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (105, '1', '感冒发烧', '2026-03-22 09:00:00', '2026-03-26 18:00:00', 5, 1, '105', NOW(), '105', NOW(), 0, 1);

-- 用户 106: 请假 1 天（应该自动通过）
INSERT INTO bpm_oa_leave (user_id, type, reason, start_time, end_time, day, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (106, '2', '办理证件', '2026-03-25 09:00:00', '2026-03-25 18:00:00', 1, 1, '106', NOW(), '106', NOW(), 0, 1);

-- 查询创建的请假
SELECT id, user_id, day, reason, status FROM bpm_oa_leave WHERE user_id IN (104, 105, 106) ORDER BY id DESC;
