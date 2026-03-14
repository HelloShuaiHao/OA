SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_definition VARCHAR(255)
)
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = p_table
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_column
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL add_column_if_missing('erp_account', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_account', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_account', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_account', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_account', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_customer', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_customer', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_customer', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_customer', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_customer', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_finance_payment', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_payment', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_payment', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_payment', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_payment', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_finance_payment_item', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_payment_item', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_payment_item', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_payment_item', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_payment_item', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_finance_receipt', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_receipt', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_receipt', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_receipt', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_receipt', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_finance_receipt_item', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_receipt_item', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_receipt_item', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_finance_receipt_item', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_finance_receipt_item', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_product', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_product', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_product', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_product', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_product', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_product_category', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_product_category', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_product_category', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_product_category', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_product_category', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_product_unit', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_product_unit', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_product_unit', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_product_unit', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_product_unit', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_purchase_in', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_in', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_in', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_in', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_in', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_purchase_in_items', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_in_items', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_in_items', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_in_items', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_in_items', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_purchase_order', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_order', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_order', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_order', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_order', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_purchase_order_items', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_order_items', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_order_items', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_order_items', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_order_items', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_purchase_return', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_return', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_return', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_return', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_return', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_purchase_return_items', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_return_items', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_return_items', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_purchase_return_items', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_purchase_return_items', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_sale_order', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_order', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_order', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_order', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_order', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_sale_order_items', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_order_items', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_order_items', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_order_items', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_order_items', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_sale_out', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_out', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_out', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_out', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_out', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_sale_out_items', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_out_items', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_out_items', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_out_items', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_out_items', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_sale_return', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_return', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_return', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_return', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_return', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_sale_return_items', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_return_items', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_return_items', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_sale_return_items', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_sale_return_items', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_check', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_check', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_check', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_check', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_check', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_check_item', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_check_item', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_check_item', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_check_item', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_check_item', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_in', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_in', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_in', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_in', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_in', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_in_item', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_in_item', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_in_item', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_in_item', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_in_item', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_move', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_move', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_move', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_move', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_move', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_move_item', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_move_item', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_move_item', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_move_item', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_move_item', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_out', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_out', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_out', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_out', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_out', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_out_item', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_out_item', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_out_item', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_out_item', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_out_item', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_stock_record', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_record', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_record', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_stock_record', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_stock_record', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_supplier', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_supplier', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_supplier', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_supplier', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_supplier', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

CALL add_column_if_missing('erp_warehouse', 'creator', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_warehouse', 'create_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_warehouse', 'updater', 'varchar(64) DEFAULT ''''');
CALL add_column_if_missing('erp_warehouse', 'update_time', 'datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_missing('erp_warehouse', 'deleted', 'bit(1) NOT NULL DEFAULT b''0''');

DROP PROCEDURE IF EXISTS add_column_if_missing;
