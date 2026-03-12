SET NAMES utf8mb4;

ALTER TABLE `erp_account`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_customer`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_finance_payment`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_finance_payment_item`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_finance_receipt`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_finance_receipt_item`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_product`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_product_category`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_product_unit`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_purchase_in`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_purchase_in_items`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_purchase_order`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_purchase_order_items`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_purchase_return`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_purchase_return_items`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_sale_order`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_sale_order_items`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_sale_out`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_sale_out_items`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_sale_return`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_sale_return_items`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_check`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_check_item`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_in`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_in_item`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_move`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_move_item`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_out`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_out_item`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_stock_record`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_supplier`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

ALTER TABLE `erp_warehouse`
  ADD COLUMN `creator` varchar(64) DEFAULT '',
  ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `updater` varchar(64) DEFAULT '',
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0';

