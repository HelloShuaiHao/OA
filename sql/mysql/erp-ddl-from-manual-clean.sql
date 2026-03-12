-- ERP DDL extracted from local manual HTML on 2026-03-12
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `erp_product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort` int DEFAULT '0',
  `status` tinyint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_product_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` tinyint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `bar_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `category_id` bigint NOT NULL,
  `unit_id` int NOT NULL,
  `status` tinyint NOT NULL,
  `standard` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expiry_day` int DEFAULT NULL,
  `weight` decimal(24,6) DEFAULT NULL,
  `purchase_price` decimal(24,6) DEFAULT NULL,
  `sale_price` decimal(24,6) DEFAULT NULL,
  `min_price` decimal(24,6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort` bigint NOT NULL,
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `principal` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `warehouse_price` decimal(24,6) DEFAULT NULL,
  `truckage_price` decimal(24,6) DEFAULT NULL,
  `status` tinyint NOT NULL,
  `default_status` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `count` decimal(24,6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  
  `count` decimal(24,6) NOT NULL,
  `total_count` decimal(24,6) NOT NULL,
  
  `biz_type` tinyint NOT NULL,
  `biz_id` bigint NOT NULL,
  `biz_item_id` bigint NOT NULL,
  `biz_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_in` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `supplier_id` bigint DEFAULT NULL,
  `in_time` datetime NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  
  `status` tinyint NOT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_in_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `in_id` bigint NOT NULL,
  
  `warehouse_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) DEFAULT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) DEFAULT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_out` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `customer_id` bigint DEFAULT NULL,
  `out_time` datetime NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  
  `status` tinyint NOT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_out_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `out_id` bigint NOT NULL,
  
  `warehouse_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) DEFAULT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) DEFAULT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_move` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `move_time` datetime NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `status` tinyint NOT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_move_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `move_id` bigint NOT NULL,
 
  `from_warehouse_id` bigint NOT NULL,
  `to_warehouse_id` bigint NOT NULL,
   
  `product_id` bigint NOT NULL,
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) DEFAULT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) DEFAULT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_check` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `check_time` datetime NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  
  `status` tinyint NOT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_stock_check_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `check_id` bigint NOT NULL,
  
  `warehouse_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) DEFAULT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) DEFAULT NULL,
  
  `stock_count` decimal(24,6) NOT NULL,
  `actual_count` decimal(24,6) NOT NULL,
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telephone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fax` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL,
  `sort` int NOT NULL,
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bank_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `status` tinyint NOT NULL,
  `order_time` datetime NOT NULL,
  
  `supplier_id` bigint NOT NULL,
  `account_id` bigint DEFAULT NULL,

  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `total_product_price` decimal(24,6) NOT NULL,
  `total_tax_price` decimal(24,6) NOT NULL,
  `discount_percent` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `deposit_price` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,

  `in_count` decimal(24,6) NOT NULL DEFAULT '0.000000',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000',

  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_purchase_order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `order_id` bigint NOT NULL,
  
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) NOT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `tax_price` decimal(24,6) DEFAULT NULL,
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  
  `in_count` decimal(24,6) NOT NULL DEFAULT '0.000000',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_purchase_in` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `status` tinyint NOT NULL,
  `in_time` datetime NOT NULL,
  
  `supplier_id` bigint NOT NULL,
  `account_id` bigint NOT NULL,

  `order_id` bigint NOT NULL,
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `total_product_price` decimal(24,6) NOT NULL,
  `total_tax_price` decimal(24,6) NOT NULL,
  `discount_percent` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  `payment_price` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_purchase_in_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `in_id` bigint NOT NULL,
  
  `order_item_id` bigint NOT NULL,
  
  `warehouse_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) NOT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `tax_price` decimal(24,6) DEFAULT NULL,

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_purchase_return` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `status` tinyint NOT NULL,
  `return_time` datetime NOT NULL,
  
  `supplier_id` bigint NOT NULL,
  `account_id` bigint NOT NULL,
  
  `order_id` bigint NOT NULL,
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `total_product_price` decimal(24,6) NOT NULL,
  `total_tax_price` decimal(24,6) NOT NULL,
  `discount_percent` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000',

  `refund_price` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_purchase_return_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `return_id` bigint NOT NULL,
  
  `order_item_id` bigint NOT NULL,
  
  `warehouse_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) NOT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `tax_price` decimal(24,6) DEFAULT NULL,

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telephone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fax` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL,
  `sort` int NOT NULL,
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bank_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_sale_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `status` tinyint NOT NULL,
  `order_time` datetime NOT NULL,
  
  `customer_id` bigint NOT NULL,
  `account_id` bigint DEFAULT NULL,
  `sale_user_id` bigint DEFAULT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `total_product_price` decimal(24,6) NOT NULL,
  `total_tax_price` decimal(24,6) NOT NULL,
  `discount_percent` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `deposit_price` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  
  `out_count` decimal(24,6) NOT NULL DEFAULT '0.000000',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_sale_order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `order_id` bigint NOT NULL,
  
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) NOT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `tax_price` decimal(24,6) DEFAULT NULL,
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  
  `out_count` decimal(24,6) NOT NULL DEFAULT '0.000000',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_sale_out` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
 
  `status` tinyint NOT NULL,
  `out_time` datetime NOT NULL,

  `customer_id` bigint NOT NULL,
  `account_id` bigint NOT NULL,
  `sale_user_id` bigint DEFAULT NULL,

  `order_id` bigint NOT NULL,
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `total_product_price` decimal(24,6) NOT NULL,
  `total_tax_price` decimal(24,6) NOT NULL,
  `discount_percent` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000',

  `receipt_price` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_sale_out_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `out_id` bigint NOT NULL,
  
  `order_item_id` bigint NOT NULL,
  
  `warehouse_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) NOT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `tax_price` decimal(24,6) DEFAULT NULL,

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_sale_return` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `status` tinyint NOT NULL,
  `return_time` datetime NOT NULL,

  `customer_id` bigint NOT NULL,
  `account_id` bigint NOT NULL,
  `sale_user_id` bigint DEFAULT NULL,

  `order_id` bigint NOT NULL,
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `total_count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `total_product_price` decimal(24,6) NOT NULL,
  `total_tax_price` decimal(24,6) NOT NULL,
  `discount_percent` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000',

  `refund_price` decimal(24,6) NOT NULL DEFAULT '0.000000',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_sale_return_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `return_id` bigint NOT NULL,
  
  `order_item_id` bigint NOT NULL,
  
  `warehouse_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  
  `product_unit_id` bigint NOT NULL,
  `product_price` decimal(24,6) NOT NULL,
  `count` decimal(24,6) NOT NULL,
  `total_price` decimal(24,6) NOT NULL,
  `tax_percent` decimal(24,6) DEFAULT NULL,
  `tax_price` decimal(24,6) DEFAULT NULL,

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL,
  `sort` int NOT NULL,
  `default_status` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_finance_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `status` tinyint NOT NULL,
  `payment_time` datetime NOT NULL,
  
  `supplier_id` bigint NOT NULL,
  `finance_user_id` bigint DEFAULT NULL,
  `account_id` bigint NOT NULL,
  
  `total_price` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `payment_price` decimal(24,6) NOT NULL,
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_finance_payment_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `payment_id` bigint NOT NULL,
  
  `biz_type` tinyint NOT NULL,
  `biz_id` bigint NOT NULL,
  `biz_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `total_price` decimal(24,6) NOT NULL,
  `paid_price` decimal(24,6) NOT NULL,
  `payment_price` decimal(24,6) NOT NULL,
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_finance_receipt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `status` tinyint NOT NULL,
  `receipt_time` datetime NOT NULL,
  
  `customer_id` bigint NOT NULL,
  `account_id` bigint NOT NULL,
  `finance_user_id` bigint DEFAULT NULL,
  
  `total_price` decimal(24,6) NOT NULL,
  `discount_price` decimal(24,6) NOT NULL,
  `receipt_price` decimal(24,6) NOT NULL,
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_finance_receipt_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  
  `receipt_id` bigint NOT NULL,
  
  `biz_type` tinyint NOT NULL,
  `biz_id` bigint NOT NULL,
  `biz_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  
  `total_price` decimal(24,6) NOT NULL,
  `receipted_price` decimal(24,6) NOT NULL,
  `receipt_price` decimal(24,6) NOT NULL,
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
