-- ERP DDL extracted from local manual HTML on 2026-03-12
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Source: 开发手册/ERP手册/【产品】产品信息、分类、单位 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_20_57).html
CREATE TABLE IF NOT EXISTS `erp_product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
  `parent_id` bigint NOT NULL COMMENT '父分类编号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编� �',
  `sort` int DEFAULT '0' COMMENT '分类排序',
  `status` tinyint NOT NULL COMMENT '开启状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品分类';

-- Source: 开发手册/ERP手册/【产品】产品信息、分类、单位 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_20_57).html
CREATE TABLE IF NOT EXISTS `erp_product_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '单位编号',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位名字',
  `status` tinyint NOT NULL COMMENT '单位状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品单位表';

-- Source: 开发手册/ERP手册/【产品】产品信息、分类、单位 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_20_57).html
CREATE TABLE IF NOT EXISTS `erp_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `bar_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品条� �',
  `category_id` bigint NOT NULL COMMENT '产品分类编号',
  `unit_id` int NOT NULL COMMENT '单位编号',
  `status` tinyint NOT NULL COMMENT '产品状态',
  `standard` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品规� �',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品备注',
  `expiry_day` int DEFAULT NULL COMMENT '保质期天数',
  `weight` decimal(24,6) DEFAULT NULL COMMENT '基础重量（kg）',
  `purchase_price` decimal(24,6) DEFAULT NULL COMMENT '采购价� �，单位：元',
  `sale_price` decimal(24,6) DEFAULT NULL COMMENT '销售价� �，单位：元',
  `min_price` decimal(24,6) DEFAULT NULL COMMENT '最低价� �，单位：元',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品表';

-- Source: 开发手册/ERP手册/【库存】产品库存、库存明细 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_05).html
CREATE TABLE IF NOT EXISTS `erp_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '仓库编号',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '仓库地址',
  `sort` bigint NOT NULL COMMENT '排序',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `principal` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '负责人',
  `warehouse_price` decimal(24,6) DEFAULT NULL COMMENT '仓储费，单位：元',
  `truckage_price` decimal(24,6) DEFAULT NULL COMMENT '搬运费，单位：元',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `default_status` bit(1) DEFAULT b'0' COMMENT '是否默认',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 仓库表';

-- Source: 开发手册/ERP手册/【库存】产品库存、库存明细 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_05).html
CREATE TABLE IF NOT EXISTS `erp_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `count` decimal(24,6) NOT NULL COMMENT '库存数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品库存表';

-- Source: 开发手册/ERP手册/【库存】产品库存、库存明细 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_05).html
CREATE TABLE IF NOT EXISTS `erp_stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  
  `count` decimal(24,6) NOT NULL COMMENT '出入库数量',
  `total_count` decimal(24,6) NOT NULL COMMENT '总库存量',
  
  `biz_type` tinyint NOT NULL COMMENT '业务类型',
  `biz_id` bigint NOT NULL COMMENT '业务编号',
  `biz_item_id` bigint NOT NULL COMMENT '业务项编号',
  `biz_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品库存明细表';

-- Source: 开发手册/ERP手册/【库存】其它入库、其它出库 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_17).html
CREATE TABLE IF NOT EXISTS `erp_stock_in` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库编号',
  `no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '入库单号',
  
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商编号',
  `in_time` datetime NOT NULL COMMENT '入库时间',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
  
  `status` tinyint NOT NULL COMMENT '状态',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它入库单表';

-- Source: 开发手册/ERP手册/【库存】其它入库、其它出库 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_17).html
CREATE TABLE IF NOT EXISTS `erp_stock_in_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库项编号',
  
  `in_id` bigint NOT NULL COMMENT '入库编号',
  
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
  `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '产品数量',
  `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它入库单项表';

-- Source: 开发手册/ERP手册/【库存】其它入库、其它出库 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_17).html
CREATE TABLE IF NOT EXISTS `erp_stock_out` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出库单号',
  
  `customer_id` bigint DEFAULT NULL COMMENT '客户编号',
  `out_time` datetime NOT NULL COMMENT '出库时间',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
  
  `status` tinyint NOT NULL COMMENT '状态',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它入库单表';

-- Source: 开发手册/ERP手册/【库存】其它入库、其它出库 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_17).html
CREATE TABLE IF NOT EXISTS `erp_stock_out_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库项编号',
  `out_id` bigint NOT NULL COMMENT '出库编号',
  
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
  `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '产品数量',
  `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它出库单项表';

-- Source: 开发手册/ERP手册/【库存】库存调拨、库存盘点 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_38).html
CREATE TABLE IF NOT EXISTS `erp_stock_move` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '调拨编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调拨单号',
  `move_time` datetime NOT NULL COMMENT '调拨时间',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
  `status` tinyint NOT NULL COMMENT '状态',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存调拨单表';

-- Source: 开发手册/ERP手册/【库存】库存调拨、库存盘点 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_38).html
CREATE TABLE IF NOT EXISTS `erp_stock_move_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '调拨项编号',
  
  `move_id` bigint NOT NULL COMMENT '调拨编号',
 
  `from_warehouse_id` bigint NOT NULL COMMENT '调出仓库编号',
  `to_warehouse_id` bigint NOT NULL COMMENT '调入仓库编号',
   
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
  `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '产品数量',
  `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存调拨项表';

-- Source: 开发手册/ERP手册/【库存】库存调拨、库存盘点 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_38).html
CREATE TABLE IF NOT EXISTS `erp_stock_check` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '盘点编号',
  
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '盘点单号',
  `check_time` datetime NOT NULL COMMENT '盘点时间',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
  
  `status` tinyint NOT NULL COMMENT '状态',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存盘点单表';

-- Source: 开发手册/ERP手册/【库存】库存调拨、库存盘点 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_38).html
CREATE TABLE IF NOT EXISTS `erp_stock_check_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '调拨项编号',
  
  `check_id` bigint NOT NULL COMMENT '调拨编号',
  
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
  `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '盈亏数量',
  `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
  
  `stock_count` decimal(24,6) NOT NULL COMMENT '账面数量（当前库存）',
  `actual_count` decimal(24,6) NOT NULL COMMENT '实际数量（实际库存）',
  
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存盘点项表';

-- Source: 开发手册/ERP手册/【采购】采购订单、入库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_49).html
CREATE TABLE IF NOT EXISTS `erp_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商编号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名称',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人',
  `mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号� �',
  `telephone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子邮箱',
  `fax` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '� 真',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `sort` int NOT NULL COMMENT '排序',
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '纳税人识别号',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率',
  `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户行',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户账号',
  `bank_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户地址',
  PRIMARY KEY (`id` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 供应商表';

-- Source: 开发手册/ERP手册/【采购】采购订单、入库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_49).html
CREATE TABLE IF NOT EXISTS `erp_purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购单编号',
  
  `status` tinyint NOT NULL COMMENT '采购状态',
  `order_time` datetime NOT NULL COMMENT '采购时间',
  
  `supplier_id` bigint NOT NULL COMMENT '供应商编号',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户编号',

  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价� �，单位：元',
  `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24,6) NOT NULL COMMENT '优� 率，百分比',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `deposit_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '定金金额，单位：元',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',

  `in_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购入库数量',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购退货数量',

  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购订单表';

-- Source: 开发手册/ERP手册/【采购】采购订单、入库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_49).html
CREATE TABLE IF NOT EXISTS `erp_purchase_order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `order_id` bigint NOT NULL COMMENT '采购订单编号',
  
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
  `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '总价',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  
  `in_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购入库数量',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购退货数量',
  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 采购订单项表';

-- Source: 开发手册/ERP手册/【采购】采购订单、入库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_49).html
CREATE TABLE IF NOT EXISTS `erp_purchase_in` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购入库编号',
  
  `status` tinyint NOT NULL COMMENT '采购状态',
  `in_time` datetime NOT NULL COMMENT '入库时间',
  
  `supplier_id` bigint NOT NULL COMMENT '供应商编号',
  `account_id` bigint NOT NULL COMMENT '结算账户编号',

  `order_id` bigint NOT NULL COMMENT '采购订单编号',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购订单号',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价� �，单位：元',
  `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24,6) NOT NULL COMMENT '优� 率，百分比',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',
  
  `payment_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已付款金额，单位：元',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购入库表';

-- Source: 开发手册/ERP手册/【采购】采购订单、入库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_49).html
CREATE TABLE IF NOT EXISTS `erp_purchase_in_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `in_id` bigint NOT NULL COMMENT '采购入库编号',
  
  `order_item_id` bigint NOT NULL COMMENT '采购订单项编号',
  
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
  `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '总价',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 销售入库项表';

-- Source: 开发手册/ERP手册/【采购】采购订单、入库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_49).html
CREATE TABLE IF NOT EXISTS `erp_purchase_return` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购退货编号',
  
  `status` tinyint NOT NULL COMMENT '退货状态',
  `return_time` datetime NOT NULL COMMENT '退货时间',
  
  `supplier_id` bigint NOT NULL COMMENT '供应商编号',
  `account_id` bigint NOT NULL COMMENT '结算账户编号',
  
  `order_id` bigint NOT NULL COMMENT '采购订单编号',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购订单号',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价� �，单位：元',
  `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24,6) NOT NULL COMMENT '优� 率，百分比',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',

  `refund_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已退款金额，单位：元',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购退货表';

-- Source: 开发手册/ERP手册/【采购】采购订单、入库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_21_49).html
CREATE TABLE IF NOT EXISTS `erp_purchase_return_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `return_id` bigint NOT NULL COMMENT '采购退货编号',
  
  `order_item_id` bigint NOT NULL COMMENT '采购订单项编号',
  
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
  `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '总价',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 采购退货项表';

-- Source: 开发手册/ERP手册/【销售】销售订单、出库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_01).html
CREATE TABLE IF NOT EXISTS `erp_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户编号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人',
  `mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号� �',
  `telephone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子邮箱',
  `fax` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '� 真',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `sort` int NOT NULL COMMENT '排序',
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '纳税人识别号',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率',
  `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户行',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户账号',
  `bank_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户地址',
  PRIMARY KEY (`id` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 客户表';

-- Source: 开发手册/ERP手册/【销售】销售订单、出库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_01).html
CREATE TABLE IF NOT EXISTS `erp_sale_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售单编号',
  
  `status` tinyint NOT NULL COMMENT '销售状态',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  
  `customer_id` bigint NOT NULL COMMENT '客户编号',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户编号',
  `sale_user_id` bigint DEFAULT NULL COMMENT '销售用户编号',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价� �，单位：元',
  `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24,6) NOT NULL COMMENT '优� 率，百分比',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `deposit_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '定金金额，单位：元',
  
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  
  `out_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售出库数量',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售退货数量',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售订单表';

-- Source: 开发手册/ERP手册/【销售】销售订单、出库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_01).html
CREATE TABLE IF NOT EXISTS `erp_sale_order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `order_id` bigint NOT NULL COMMENT '销售订单编号',
  
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
  `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '总价',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  
  `out_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售出库数量',
  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售退货数量',
  
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 销售订单项表';

-- Source: 开发手册/ERP手册/【销售】销售订单、出库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_01).html
CREATE TABLE IF NOT EXISTS `erp_sale_out` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售出库编号',
 
  `status` tinyint NOT NULL COMMENT '出库状态',
  `out_time` datetime NOT NULL COMMENT '出库时间',

  `customer_id` bigint NOT NULL COMMENT '客户编号',
  `account_id` bigint NOT NULL COMMENT '结算账户编号',
  `sale_user_id` bigint DEFAULT NULL COMMENT '销售用户编号',

  `order_id` bigint NOT NULL COMMENT '销售订单编号',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售订单号',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价� �，单位：元',
  `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24,6) NOT NULL COMMENT '优� 率，百分比',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',

  `receipt_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已收款金额，单位：元',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售出库表';

-- Source: 开发手册/ERP手册/【销售】销售订单、出库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_01).html
CREATE TABLE IF NOT EXISTS `erp_sale_out_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `out_id` bigint NOT NULL COMMENT '销售出库编号',
  
  `order_item_id` bigint NOT NULL COMMENT '销售订单项编号',
  
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
  `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '总价',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 销售出库项表';

-- Source: 开发手册/ERP手册/【销售】销售订单、出库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_01).html
CREATE TABLE IF NOT EXISTS `erp_sale_return` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售退货编号',
  
  `status` tinyint NOT NULL COMMENT '退货状态',
  `return_time` datetime NOT NULL COMMENT '退货时间',

  `customer_id` bigint NOT NULL COMMENT '客户编号',
  `account_id` bigint NOT NULL COMMENT '结算账户编号',
  `sale_user_id` bigint DEFAULT NULL COMMENT '销售用户编号',

  `order_id` bigint NOT NULL COMMENT '销售订单编号',
  `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售订单号',
  
  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价� �，单位：元',
  `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
  `discount_percent` decimal(24,6) NOT NULL COMMENT '优� 率，百分比',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',

  `refund_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已退款金额，单位：元',
  
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `no` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售退货表';

-- Source: 开发手册/ERP手册/【销售】销售订单、出库、退货 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_01).html
CREATE TABLE IF NOT EXISTS `erp_sale_return_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `return_id` bigint NOT NULL COMMENT '销售退货编号',
  
  `order_item_id` bigint NOT NULL COMMENT '销售订单项编号',
  
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  
  `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
  `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
  `count` decimal(24,6) NOT NULL COMMENT '数量',
  `total_price` decimal(24,6) NOT NULL COMMENT '总价',
  `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
  `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',

  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 销售退货项表';

-- Source: 开发手册/ERP手册/【财务】采购付款、销售收款 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_16).html
CREATE TABLE IF NOT EXISTS `erp_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算账户编号',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账户名称',
  `no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账户编� �',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `sort` int NOT NULL COMMENT '排序',
  `default_status` bit(1) DEFAULT b'0' COMMENT '是否默认',
  PRIMARY KEY (`id` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 结算账户';

-- Source: 开发手册/ERP手册/【财务】采购付款、销售收款 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_16).html
CREATE TABLE IF NOT EXISTS `erp_finance_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '付款单号',
  
  `status` tinyint NOT NULL COMMENT '状态',
  `payment_time` datetime NOT NULL COMMENT '付款时间',
  
  `supplier_id` bigint NOT NULL COMMENT '供应商编号',
  `finance_user_id` bigint DEFAULT NULL COMMENT '财务人员编号',
  `account_id` bigint NOT NULL COMMENT '付款账户编号',
  
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `payment_price` decimal(24,6) NOT NULL COMMENT '实付金额，单位：分',
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 付款单表';

-- Source: 开发手册/ERP手册/【财务】采购付款、销售收款 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_16).html
CREATE TABLE IF NOT EXISTS `erp_finance_payment_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `payment_id` bigint NOT NULL COMMENT '付款单编号',
  
  `biz_type` tinyint NOT NULL COMMENT '业务类型',
  `biz_id` bigint NOT NULL COMMENT '业务编号',
  `biz_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
  
  `total_price` decimal(24,6) NOT NULL COMMENT '应付� 款，单位：分',
  `paid_price` decimal(24,6) NOT NULL COMMENT '已付� 款，单位：分',
  `payment_price` decimal(24,6) NOT NULL COMMENT '本次付款，单位：分',
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 付款项表';

-- Source: 开发手册/ERP手册/【财务】采购付款、销售收款 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_16).html
CREATE TABLE IF NOT EXISTS `erp_finance_receipt` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收款单号',
  
  `status` tinyint NOT NULL COMMENT '状态',
  `receipt_time` datetime NOT NULL COMMENT '收款时间',
  
  `customer_id` bigint NOT NULL COMMENT '客户编号',
  `account_id` bigint NOT NULL COMMENT '收款账户编号',
  `finance_user_id` bigint DEFAULT NULL COMMENT '财务人员编号',
  
  `total_price` decimal(24,6) NOT NULL COMMENT '合计价� �，单位：元',
  `discount_price` decimal(24,6) NOT NULL COMMENT '优� 金额，单位：元',
  `receipt_price` decimal(24,6) NOT NULL COMMENT '实收金额，单位：分',
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 收款单表';

-- Source: 开发手册/ERP手册/【财务】采购付款、销售收款 _ ruoyi-vue-pro 开发指南 (2024_4_20 14_22_16).html
CREATE TABLE IF NOT EXISTS `erp_finance_receipt_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  
  `receipt_id` bigint NOT NULL COMMENT '收款单编号',
  
  `biz_type` tinyint NOT NULL COMMENT '业务类型',
  `biz_id` bigint NOT NULL COMMENT '业务编号',
  `biz_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
  
  `total_price` decimal(24,6) NOT NULL COMMENT '应收金额，单位：分',
  `receipted_price` decimal(24,6) NOT NULL COMMENT '已收金额，单位：分',
  `receipt_price` decimal(24,6) NOT NULL COMMENT '本次收款，单位：分',
  
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 收款项表';

SET FOREIGN_KEY_CHECKS = 1;
