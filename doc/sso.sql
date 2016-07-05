
CREATE TABLE IF NOT EXISTS `accounts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `name` varbinary(128) NOT NULL COMMENT '账号名',
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '账号类型：0 - 用户，1 - 应用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='账号表';


CREATE TABLE IF NOT EXISTS `security_credentials` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `account_id` bigint(20) NOT NULL COMMENT '账号',
  `access_key` varbinary(128) NOT NULL COMMENT 'AccessKey',
  `secret_key` varbinary(128) DEFAULT NULL COMMENT 'SecretKey',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_access` (`access_key`),
  KEY `idx_account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='安全凭证表';


CREATE TABLE IF NOT EXISTS `user_info` (
  `user_id` bigint(20) NOT NULL COMMENT '账号表主键',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `password` varbinary(64) DEFAULT NULL COMMENT '用户密码',
  `misc` varbinary(1024) DEFAULT NULL COMMENT '自定义信息',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0 - 未验证，1 - 已验证',
  `birthplace` varchar(256) DEFAULT NULL COMMENT '出生地',
  `location` varchar(256) DEFAULT NULL COMMENT '所在地',
  `level` tinyint(4) NOT NULL DEFAULT '0' COMMENT '等级',
  `loan_point` float DEFAULT NULL COMMENT '贷款额度',
  `lend_point` float DEFAULT NULL COMMENT '放款额度',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户信息表';

