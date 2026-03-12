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

-- Generated from sql/mysql/ruoyi-vue-pro.sql on 2026-03-12
-- Note: this repository does not include ERP table DDL; this file only contains ERP menu and dict data.

-- ERP dict types
INSERT IGNORE INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES (611, 'ERP 库存明细的业务类型', 'erp_stock_record_biz_type', 0, 'ERP 库存明细的业务类型', '1', '2024-02-05 18:07:02', '1', '2024-02-05 18:07:02', b'0', '1970-01-01 00:00:00');
INSERT IGNORE INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES (612, 'ERP 审批状态', 'erp_audit_status', 0, '', '1', '2024-02-06 00:00:07', '1', '2024-02-06 00:00:07', b'0', '1970-01-01 00:00:00');

-- ERP dict data
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1486, 10, '其它入库', '10', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-05 18:07:25', '1', '2024-02-05 18:07:43', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1487, 11, '其它入库（作废）', '11', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-05 18:08:07', '1', '2024-02-05 19:20:16', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1488, 20, '其它出库', '20', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-05 18:08:51', '1', '2024-02-05 18:08:51', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1489, 21, '其它出库（作废）', '21', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-05 18:09:00', '1', '2024-02-05 19:20:10', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1490, 10, '未审核', '10', 'erp_audit_status', 0, 'default', '', '', '1', '2024-02-06 00:00:21', '1', '2024-02-06 00:00:21', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1491, 20, '已审核', '20', 'erp_audit_status', 0, 'success', '', '', '1', '2024-02-06 00:00:35', '1', '2024-02-06 00:00:35', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1492, 30, '调拨入库', '30', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-07 20:34:19', '1', '2024-02-07 12:36:31', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1493, 31, '调拨入库（作废）', '31', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-07 20:34:29', '1', '2024-02-07 20:37:11', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1494, 32, '调拨出库', '32', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-07 20:34:38', '1', '2024-02-07 12:36:33', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1495, 33, '调拨出库（作废）', '33', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-07 20:34:49', '1', '2024-02-07 20:37:06', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1496, 40, '盘盈入库', '40', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-08 08:53:00', '1', '2024-02-08 08:53:09', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1497, 41, '盘盈入库（作废）', '41', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-08 08:53:39', '1', '2024-02-16 19:40:54', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1498, 42, '盘亏出库', '42', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-08 08:54:16', '1', '2024-02-08 08:54:16', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1499, 43, '盘亏出库（作废）', '43', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-08 08:54:31', '1', '2024-02-16 19:40:46', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1500, 50, '销售出库', '50', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-11 21:47:25', '1', '2024-02-11 21:50:40', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1501, 51, '销售出库（作废）', '51', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-11 21:47:37', '1', '2024-02-11 21:51:12', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1502, 60, '销售退货入库', '60', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-12 06:51:05', '1', '2024-02-12 06:51:05', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1503, 61, '销售退货入库（作废）', '61', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-12 06:51:18', '1', '2024-02-12 06:51:18', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1504, 70, '采购入库', '70', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-16 13:10:02', '1', '2024-02-16 13:10:02', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1505, 71, '采购入库（作废）', '71', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-16 13:10:10', '1', '2024-02-16 19:40:40', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1506, 80, '采购退货出库', '80', 'erp_stock_record_biz_type', 0, '', '', '', '1', '2024-02-16 13:10:17', '1', '2024-02-16 13:10:17', b'0');
INSERT IGNORE INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1507, 81, '采购退货出库（作废）', '81', 'erp_stock_record_biz_type', 0, 'danger', '', '', '1', '2024-02-16 13:10:26', '1', '2024-02-16 19:40:33', b'0');

INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2563, 'ERP 系统', '', 1, 300, 0, '/erp', 'simple-icons:erpnext', '', '', 0, b'1', b'1', b'1', '1', '2024-02-04 15:37:25', '1', '2025-04-19 18:56:15', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2564, '产品管理', '', 1, 40, 2563, 'product', 'fa:product-hunt', '', '', 0, b'1', b'1', b'1', '1', '2024-02-04 15:38:43', '1', '2024-02-04 15:38:43', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2565, '产品信息', '', 2, 0, 2564, 'product', 'fa-solid:apple-alt', 'erp/product/product/index', 'ErpProduct', 0, b'1', b'1', b'1', '', '2024-02-04 07:52:15', '1', '2024-02-05 14:42:11', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2566, '产品查询', 'erp:product:query', 3, 1, 2565, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-02-04 07:52:15', '1', '2024-02-04 17:21:57', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2567, '产品创建', 'erp:product:create', 3, 2, 2565, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-02-04 07:52:15', '1', '2024-02-04 17:22:12', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2568, '产品更新', 'erp:product:update', 3, 3, 2565, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-02-04 07:52:15', '1', '2024-02-04 17:22:16', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2569, '产品删除', 'erp:product:delete', 3, 4, 2565, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-02-04 07:52:15', '1', '2024-02-04 17:22:22', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2570, '产品导出', 'erp:product:export', 3, 5, 2565, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-02-04 07:52:15', '1', '2024-02-04 17:22:26', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2571, '产品分类', '', 2, 1, 2564, 'product-category', 'fa:certificate', 'erp/product/category/index', 'ErpProductCategory', 0, b'1', b'1', b'1', '', '2024-02-04 09:21:04', '1', '2024-02-04 17:24:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2572, '分类查询', 'erp:product-category:query', 3, 1, 2571, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 09:21:04', '', '2024-02-04 09:21:04', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2573, '分类创建', 'erp:product-category:create', 3, 2, 2571, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 09:21:04', '', '2024-02-04 09:21:04', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2574, '分类更新', 'erp:product-category:update', 3, 3, 2571, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 09:21:04', '', '2024-02-04 09:21:04', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2575, '分类删除', 'erp:product-category:delete', 3, 4, 2571, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 09:21:04', '', '2024-02-04 09:21:04', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2576, '分类导出', 'erp:product-category:export', 3, 5, 2571, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 09:21:04', '', '2024-02-04 09:21:04', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2577, '产品单位', '', 2, 2, 2564, 'unit', 'ep:opportunity', 'erp/product/unit/index', 'ErpProductUnit', 0, b'1', b'1', b'1', '', '2024-02-04 11:54:08', '1', '2024-02-04 19:54:37', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2578, '单位查询', 'erp:product-unit:query', 3, 1, 2577, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 11:54:08', '', '2024-02-04 11:54:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2579, '单位创建', 'erp:product-unit:create', 3, 2, 2577, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 11:54:08', '', '2024-02-04 11:54:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2580, '单位更新', 'erp:product-unit:update', 3, 3, 2577, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 11:54:08', '', '2024-02-04 11:54:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2581, '单位删除', 'erp:product-unit:delete', 3, 4, 2577, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 11:54:08', '', '2024-02-04 11:54:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2582, '单位导出', 'erp:product-unit:export', 3, 5, 2577, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 11:54:08', '', '2024-02-04 11:54:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2583, '库存管理', '', 1, 30, 2563, 'stock', 'fa:window-restore', '', '', 0, b'1', b'1', b'1', '1', '2024-02-05 00:29:37', '1', '2024-02-05 00:29:37', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2584, '仓库信息', '', 2, 0, 2583, 'warehouse', 'ep:house', 'erp/stock/warehouse/index', 'ErpWarehouse', 0, b'1', b'1', b'1', '', '2024-02-04 17:12:09', '1', '2024-02-05 01:12:53', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2585, '仓库查询', 'erp:warehouse:query', 3, 1, 2584, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 17:12:09', '', '2024-02-04 17:12:09', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2586, '仓库创建', 'erp:warehouse:create', 3, 2, 2584, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 17:12:09', '', '2024-02-04 17:12:09', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2587, '仓库更新', 'erp:warehouse:update', 3, 3, 2584, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 17:12:09', '', '2024-02-04 17:12:09', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2588, '仓库删除', 'erp:warehouse:delete', 3, 4, 2584, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 17:12:09', '', '2024-02-04 17:12:09', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2589, '仓库导出', 'erp:warehouse:export', 3, 5, 2584, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-04 17:12:09', '', '2024-02-04 17:12:09', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2590, '产品库存', '', 2, 1, 2583, 'stock', 'ep:coffee', 'erp/stock/stock/index', 'ErpStock', 0, b'1', b'1', b'1', '', '2024-02-05 06:40:50', '1', '2024-02-05 14:42:44', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2591, '库存查询', 'erp:stock:query', 3, 1, 2590, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 06:40:50', '', '2024-02-05 06:40:50', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2592, '库存导出', 'erp:stock:export', 3, 5, 2590, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 06:40:50', '', '2024-02-05 06:40:50', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2593, '出入库明细', '', 2, 2, 2583, 'record', 'fa-solid:blog', 'erp/stock/record/index', 'ErpStockRecord', 0, b'1', b'1', b'1', '', '2024-02-05 10:27:21', '1', '2024-02-06 17:26:11', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2594, '库存明细查询', 'erp:stock-record:query', 3, 1, 2593, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 10:27:21', '', '2024-02-05 10:27:21', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2595, '库存明细导出', 'erp:stock-record:export', 3, 5, 2593, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 10:27:21', '', '2024-02-05 10:27:21', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2596, '其它入库', '', 2, 3, 2583, 'in', 'ep:zoom-in', 'erp/stock/in/index', 'ErpStockIn', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-07 19:06:51', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2597, '其它入库单查询', 'erp:stock-in:query', 3, 1, 2596, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-05 16:08:56', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2598, '其它入库单创建', 'erp:stock-in:create', 3, 2, 2596, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-05 16:08:56', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2599, '其它入库单更新', 'erp:stock-in:update', 3, 3, 2596, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-05 16:08:56', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2600, '其它入库单删除', 'erp:stock-in:delete', 3, 4, 2596, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-05 16:08:56', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2601, '其它入库单导出', 'erp:stock-in:export', 3, 5, 2596, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-05 16:08:56', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2602, '采购管理', '', 1, 10, 2563, 'purchase', 'fa:buysellads', '', '', 0, b'1', b'1', b'1', '1', '2024-02-06 16:01:01', '1', '2024-02-06 16:01:23', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2603, '供应商信息', '', 2, 4, 2602, 'supplier', 'fa:superpowers', 'erp/purchase/supplier/index', 'ErpSupplier', 0, b'1', b'1', b'1', '', '2024-02-06 08:21:55', '1', '2024-02-06 16:22:25', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2604, '供应商查询', 'erp:supplier:query', 3, 1, 2603, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-06 08:21:55', '', '2024-02-06 08:21:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2605, '供应商创建', 'erp:supplier:create', 3, 2, 2603, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-06 08:21:55', '', '2024-02-06 08:21:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2606, '供应商更新', 'erp:supplier:update', 3, 3, 2603, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-06 08:21:55', '', '2024-02-06 08:21:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2607, '供应商删除', 'erp:supplier:delete', 3, 4, 2603, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-06 08:21:55', '', '2024-02-06 08:21:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2608, '供应商导出', 'erp:supplier:export', 3, 5, 2603, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-06 08:21:55', '', '2024-02-06 08:21:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2609, '其它入库单审批', 'erp:stock-in:update-status', 3, 6, 2596, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-05 16:08:56', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2610, '其它出库', '', 2, 4, 2583, 'out', 'ep:zoom-out', 'erp/stock/out/index', 'ErpStockOut', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-07 19:06:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2611, '其它出库单查询', 'erp:stock-out:query', 3, 1, 2610, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 06:43:39', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2612, '其它出库单创建', 'erp:stock-out:create', 3, 2, 2610, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 06:43:42', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2613, '其它出库单更新', 'erp:stock-out:update', 3, 3, 2610, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 06:43:44', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2614, '其它出库单删除', 'erp:stock-out:delete', 3, 4, 2610, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 06:43:56', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2615, '其它出库单导出', 'erp:stock-out:export', 3, 5, 2610, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 06:43:57', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2616, '其它出库单审批', 'erp:stock-out:update-status', 3, 6, 2610, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 06:43:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2617, '销售管理', '', 1, 20, 2563, 'sale', 'fa:sellsy', '', '', 0, b'1', b'1', b'1', '1', '2024-02-07 15:12:32', '1', '2024-02-07 15:12:32', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2618, '客户信息', '', 2, 4, 2617, 'customer', 'ep:avatar', 'erp/sale/customer/index', 'ErpCustomer', 0, b'1', b'1', b'1', '', '2024-02-07 07:21:45', '1', '2024-02-07 15:22:25', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2619, '客户查询', 'erp:customer:query', 3, 1, 2618, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-07 07:21:45', '', '2024-02-07 07:21:45', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2620, '客户创建', 'erp:customer:create', 3, 2, 2618, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-07 07:21:45', '', '2024-02-07 07:21:45', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2621, '客户更新', 'erp:customer:update', 3, 3, 2618, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-07 07:21:45', '', '2024-02-07 07:21:45', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2622, '客户删除', 'erp:customer:delete', 3, 4, 2618, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-07 07:21:45', '', '2024-02-07 07:21:45', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2623, '客户导出', 'erp:customer:export', 3, 5, 2618, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-07 07:21:45', '', '2024-02-07 07:21:45', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2624, '库存调拨', '', 2, 5, 2583, 'move', 'ep:folder-remove', 'erp/stock/move/index', 'ErpStockMove', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-16 18:53:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2625, '库存调度单查询', 'erp:stock-move:query', 3, 1, 2624, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:49', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2626, '库存调度单创建', 'erp:stock-move:create', 3, 2, 2624, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:52', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2627, '库存调度单更新', 'erp:stock-move:update', 3, 3, 2624, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2628, '库存调度单删除', 'erp:stock-move:delete', 3, 4, 2624, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:57', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2629, '库存调度单导出', 'erp:stock-move:export', 3, 5, 2624, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:59', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2630, '库存调度单审批', 'erp:stock-move:update-status', 3, 6, 2624, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:13:03', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2631, '库存盘点', '', 2, 6, 2583, 'check', 'ep:circle-check-filled', 'erp/stock/check/index', 'ErpStockCheck', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-08 08:31:09', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2632, '库存盘点单查询', 'erp:stock-check:query', 3, 1, 2631, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:49', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2633, '库存盘点单创建', 'erp:stock-check:create', 3, 2, 2631, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:52', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2634, '库存盘点单更新', 'erp:stock-check:update', 3, 3, 2631, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2635, '库存盘点单删除', 'erp:stock-check:delete', 3, 4, 2631, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:57', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2636, '库存盘点单导出', 'erp:stock-check:export', 3, 5, 2631, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:59', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2637, '库存盘点单审批', 'erp:stock-check:update-status', 3, 6, 2631, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:13:03', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2638, '销售订单', '', 2, 1, 2617, 'order', 'fa:first-order', 'erp/sale/order/index', 'ErpSaleOrder', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-10 21:59:20', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2639, '销售订单查询', 'erp:sale-order:query', 3, 1, 2638, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:49', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2640, '销售订单创建', 'erp:sale-order:create', 3, 2, 2638, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:52', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2641, '销售订单更新', 'erp:sale-order:update', 3, 3, 2638, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2642, '销售订单删除', 'erp:sale-order:delete', 3, 4, 2638, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:57', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2643, '销售订单导出', 'erp:sale-order:export', 3, 5, 2638, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:59', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2644, '销售订单审批', 'erp:sale-order:update-status', 3, 6, 2638, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:13:03', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2645, '财务管理', '', 1, 50, 2563, 'finance', 'ep:money', '', '', 0, b'1', b'1', b'1', '1', '2024-02-10 08:05:58', '1', '2024-02-10 08:06:07', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2646, '结算账户', '', 2, 10, 2645, 'account', 'fa:universal-access', 'erp/finance/account/index', 'ErpAccount', 0, b'1', b'1', b'1', '', '2024-02-10 00:15:07', '1', '2024-02-14 08:24:31', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2647, '结算账户查询', 'erp:account:query', 3, 1, 2646, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-10 00:15:07', '', '2024-02-10 00:15:07', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2648, '结算账户创建', 'erp:account:create', 3, 2, 2646, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-10 00:15:07', '', '2024-02-10 00:15:07', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2649, '结算账户更新', 'erp:account:update', 3, 3, 2646, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-10 00:15:07', '', '2024-02-10 00:15:07', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2650, '结算账户删除', 'erp:account:delete', 3, 4, 2646, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-10 00:15:07', '', '2024-02-10 00:15:07', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2651, '结算账户导出', 'erp:account:export', 3, 5, 2646, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-10 00:15:07', '', '2024-02-10 00:15:07', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2652, '销售出库', '', 2, 2, 2617, 'out', 'ep:sold-out', 'erp/sale/out/index', 'ErpSaleOut', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-10 22:02:07', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2653, '销售出库查询', 'erp:sale-out:query', 3, 1, 2652, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:49', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2654, '销售出库创建', 'erp:sale-out:create', 3, 2, 2652, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:52', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2655, '销售出库更新', 'erp:sale-out:update', 3, 3, 2652, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2656, '销售出库删除', 'erp:sale-out:delete', 3, 4, 2652, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:57', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2657, '销售出库导出', 'erp:sale-out:export', 3, 5, 2652, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:59', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2658, '销售出库审批', 'erp:sale-out:update-status', 3, 6, 2652, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:13:03', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2659, '销售退货', '', 2, 3, 2617, 'return', 'fa-solid:bone', 'erp/sale/return/index', 'ErpSaleReturn', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-12 06:12:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2660, '销售退货查询', 'erp:sale-return:query', 3, 1, 2659, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:49', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2661, '销售退货创建', 'erp:sale-return:create', 3, 2, 2659, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:52', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2662, '销售退货更新', 'erp:sale-return:update', 3, 3, 2659, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:55', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2663, '销售退货删除', 'erp:sale-return:delete', 3, 4, 2659, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:57', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2664, '销售退货导出', 'erp:sale-return:export', 3, 5, 2659, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:12:59', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2665, '销售退货审批', 'erp:sale-return:update-status', 3, 6, 2659, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-07 11:13:03', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2666, '采购订单', '', 2, 1, 2602, 'order', 'fa-solid:border-all', 'erp/purchase/order/index', 'ErpPurchaseOrder', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-12 08:51:49', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2667, '采购订单查询', 'erp:purchase-order:query', 3, 1, 2666, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:17', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2668, '采购订单创建', 'erp:purchase-order:create', 3, 2, 2666, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:54', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2669, '采购订单更新', 'erp:purchase-order:update', 3, 3, 2666, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2670, '采购订单删除', 'erp:purchase-order:delete', 3, 4, 2666, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:00', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2671, '采购订单导出', 'erp:purchase-order:export', 3, 5, 2666, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:05', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2672, '采购订单审批', 'erp:purchase-order:update-status', 3, 6, 2666, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2673, '采购入库', '', 2, 2, 2602, 'in', 'fa-solid:gopuram', 'erp/purchase/in/index', 'ErpPurchaseIn', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-12 11:19:27', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2674, '采购入库查询', 'erp:purchase-in:query', 3, 1, 2673, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:17', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2675, '采购入库创建', 'erp:purchase-in:create', 3, 2, 2673, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:54', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2676, '采购入库更新', 'erp:purchase-in:update', 3, 3, 2673, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2677, '采购入库删除', 'erp:purchase-in:delete', 3, 4, 2673, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:00', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2678, '采购入库导出', 'erp:purchase-in:export', 3, 5, 2673, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:05', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2679, '采购入库审批', 'erp:purchase-in:update-status', 3, 6, 2673, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2680, '采购退货', '', 2, 3, 2602, 'return', 'ep:minus', 'erp/purchase/return/index', 'ErpPurchaseReturn', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-12 20:51:02', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2681, '采购退货查询', 'erp:purchase-return:query', 3, 1, 2680, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:17', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2682, '采购退货创建', 'erp:purchase-return:create', 3, 2, 2680, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:54', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2683, '采购退货更新', 'erp:purchase-return:update', 3, 3, 2680, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2684, '采购退货删除', 'erp:purchase-return:delete', 3, 4, 2680, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:00', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2685, '采购退货导出', 'erp:purchase-return:export', 3, 5, 2680, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:05', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2686, '采购退货审批', 'erp:purchase-return:update-status', 3, 6, 2680, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2687, '付款单', '', 2, 1, 2645, 'payment', 'ep:caret-right', 'erp/finance/payment/index', 'ErpFinancePayment', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-14 08:24:23', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2688, '付款单查询', 'erp:finance-payment:query', 3, 1, 2687, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:17', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2689, '付款单创建', 'erp:finance-payment:create', 3, 2, 2687, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:54', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2690, '付款单更新', 'erp:finance-payment:update', 3, 3, 2687, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2691, '付款单删除', 'erp:finance-payment:delete', 3, 4, 2687, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:00', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2692, '付款单导出', 'erp:finance-payment:export', 3, 5, 2687, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:05', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2693, '付款单审批', 'erp:finance-payment:update-status', 3, 6, 2687, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2694, '收款单', '', 2, 2, 2645, 'receipt', 'ep:expand', 'erp/finance/receipt/index', 'ErpFinanceReceipt', 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '1', '2024-02-15 19:35:45', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2695, '收款单查询', 'erp:finance-receipt:query', 3, 1, 2694, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:17', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2696, '收款单创建', 'erp:finance-receipt:create', 3, 2, 2694, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:54', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2697, '收款单更新', 'erp:finance-receipt:update', 3, 3, 2694, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:44:58', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2698, '收款单删除', 'erp:finance-receipt:delete', 3, 4, 2694, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:00', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2699, '收款单导出', 'erp:finance-receipt:export', 3, 5, 2694, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:05', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2700, '收款单审批', 'erp:finance-receipt:update-status', 3, 6, 2694, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-02-05 16:08:56', '', '2024-02-12 00:45:08', b'0');
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2702, 'ERP 首页', 'erp:statistics:query', 2, 0, 2563, 'home', 'ep:home-filled', 'erp/home/index.vue', 'ErpHome', 0, b'1', b'1', b'1', '1', '2024-02-18 16:49:40', '1', '2024-02-26 21:12:18', b'0');


