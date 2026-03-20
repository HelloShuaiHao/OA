ALTER TABLE `agentx_approval_binding`
    ADD COLUMN IF NOT EXISTS `callback_retry_count` INT NOT NULL DEFAULT 0 COMMENT '回调重试次数' AFTER `decision_status`,
    ADD COLUMN IF NOT EXISTS `callback_failed` BIT(1) NOT NULL DEFAULT b'0' COMMENT '回调是否失败' AFTER `callback_retry_count`,
    ADD COLUMN IF NOT EXISTS `callback_last_error` VARCHAR(512) NULL DEFAULT NULL COMMENT '回调最后一次错误' AFTER `callback_failed`;
