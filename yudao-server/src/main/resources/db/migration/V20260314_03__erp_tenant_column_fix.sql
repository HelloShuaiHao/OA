SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_tenant_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_tenant_column_if_missing(IN p_table VARCHAR(64))
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = p_table
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = 'tenant_id'
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 AFTER `id`');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL add_tenant_column_if_missing('erp_account');
CALL add_tenant_column_if_missing('erp_customer');
CALL add_tenant_column_if_missing('erp_finance_payment');
CALL add_tenant_column_if_missing('erp_finance_payment_item');
CALL add_tenant_column_if_missing('erp_finance_receipt');
CALL add_tenant_column_if_missing('erp_finance_receipt_item');
CALL add_tenant_column_if_missing('erp_product');
CALL add_tenant_column_if_missing('erp_product_category');
CALL add_tenant_column_if_missing('erp_product_unit');
CALL add_tenant_column_if_missing('erp_purchase_in');
CALL add_tenant_column_if_missing('erp_purchase_in_items');
CALL add_tenant_column_if_missing('erp_purchase_order');
CALL add_tenant_column_if_missing('erp_purchase_order_items');
CALL add_tenant_column_if_missing('erp_purchase_return');
CALL add_tenant_column_if_missing('erp_purchase_return_items');
CALL add_tenant_column_if_missing('erp_sale_order');
CALL add_tenant_column_if_missing('erp_sale_order_items');
CALL add_tenant_column_if_missing('erp_sale_out');
CALL add_tenant_column_if_missing('erp_sale_out_items');
CALL add_tenant_column_if_missing('erp_sale_return');
CALL add_tenant_column_if_missing('erp_sale_return_items');
CALL add_tenant_column_if_missing('erp_stock');
CALL add_tenant_column_if_missing('erp_stock_check');
CALL add_tenant_column_if_missing('erp_stock_check_item');
CALL add_tenant_column_if_missing('erp_stock_in');
CALL add_tenant_column_if_missing('erp_stock_in_item');
CALL add_tenant_column_if_missing('erp_stock_move');
CALL add_tenant_column_if_missing('erp_stock_move_item');
CALL add_tenant_column_if_missing('erp_stock_out');
CALL add_tenant_column_if_missing('erp_stock_out_item');
CALL add_tenant_column_if_missing('erp_stock_record');
CALL add_tenant_column_if_missing('erp_supplier');
CALL add_tenant_column_if_missing('erp_warehouse');

DROP PROCEDURE IF EXISTS add_tenant_column_if_missing;
