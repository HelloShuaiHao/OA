UPDATE `system_menu`
SET `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` IN (8802, 8803, 8804, 8805)
  AND `deleted` = b'0';
