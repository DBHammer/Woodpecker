
-- 建立测试库-- 
drop database if exists findptlocal;
create database findptlocal;


use findptlocal;
-- 建立交易日志表--
DROP TABLE IF EXISTS `tranlog`;
CREATE TABLE `tranlog` (
  `tranlog_date` date NOT NULL,
  `tranlog_branchid` char(10) NOT NULL,
  `tranlog_branchseq` bigint(20) NOT NULL,
  `tranlog_trancfgid` char(4) DEFAULT NULL,
  `tranlog_strtime` bigint(20) DEFAULT NULL,
  `tranlog_endtime` bigint(20) DEFAULT NULL,
  `tranlog_tmcost` int(11) DEFAULT NULL,
  `tranlog_success` char(4) DEFAULT NULL,
  `tranlog_note` varchar(2000) DEFAULT NULL,
  `tranlog_fld1` varchar(200) DEFAULT NULL,
  `tranlog_fld2` varchar(200) DEFAULT NULL,
  `tranlog_fld3` decimal(19,2) DEFAULT NULL,
  `tranlog_fld4` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`tranlog_date`,`tranlog_branchid`,`tranlog_branchseq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `paramcfg`;
CREATE TABLE `paramcfg` (
  `param_id` char(4) NOT NULL,
  `param_name` varchar(200) DEFAULT NULL,
  `param_value` varchar(1000) DEFAULT NULL,
  `param_desc` varchar(1000) DEFAULT NULL,
  `param_fld1` varchar(200) DEFAULT NULL,
  `param_fld2` varchar(200) DEFAULT NULL,
  `param_fld3` decimal(19,2) DEFAULT NULL,
  `param_fld4` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`param_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of paramcfg
-- ----------------------------
INSERT INTO `paramcfg` VALUES ('1001', 'init_page_size', '5000', '数据初始化时customer表和account表分页大小', null, null, null, null);
INSERT INTO `paramcfg` VALUES ('1002', 'update_customer_bale', '100000', '数据初始化更新汇总账户余额时分页大小', null, null, null, null);
INSERT INTO `paramcfg` VALUES ('1003', 'move_tranlog_size', '100000', '定时任务迁移tranlog表到tranloghistory数量', null, null, null, null);
INSERT INTO `paramcfg` VALUES ('1004', 'start_uniformity', '1', '是否开启一致性检测1是0否', null, null, null, null);


DROP TABLE IF EXISTS `clustercfg`;
CREATE TABLE `clustercfg` (
  `cluster_id` char(4) NOT NULL,
  `cluster_type` varchar(20) DEFAULT NULL,
  `cluster_url` varchar(1000) DEFAULT NULL,
  `cluster_stat` char(1) DEFAULT NULL,
  `cluster_finish` char(1) DEFAULT NULL,
  `cluster_fld1` varchar(200) DEFAULT NULL,
  `cluster_fld2` varchar(200) DEFAULT NULL,
  `cluster_fld3` decimal(19,2) DEFAULT NULL,
  `cluster_fld4` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of clustercfg
-- ----------------------------
INSERT INTO `clustercfg` VALUES ('1001', 'master', 'http://localhost:8090/', '0', '0', null, null, null, null);
INSERT INTO `clustercfg` VALUES ('1002', 'slave', 'http://localhost:8090/', '0', '0', null, null, null, null);


DROP TABLE IF EXISTS `processlist`;
CREATE TABLE `processlist` (
  `process_id` varchar(32) NOT NULL,
  `datacfg_id` char(4) DEFAULT NULL,
  `trancfg_id` char(4) DEFAULT NULL,
  `process_perc` tinyint(4) DEFAULT NULL,
  `process_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `process_content` varchar(2000) DEFAULT NULL,
  `process_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `tranlog_history`;
CREATE TABLE `tranlog_history` (
  `tranlog_date` date NOT NULL,
  `tranlog_branchid` char(10) NOT NULL,
  `tranlog_branchseq` bigint(20) NOT NULL,
  `tranlog_trancfgid` char(4) DEFAULT NULL,
  `tranlog_strtime` bigint(20) DEFAULT NULL,
  `tranlog_endtime` bigint(20) DEFAULT NULL,
  `tranlog_tmcost` int(11) DEFAULT NULL,
  `tranlog_success` char(4) DEFAULT NULL,
  `tranlog_note` varchar(2000) DEFAULT NULL,
  `tranlog_fld1` varchar(200) DEFAULT NULL,
  `tranlog_fld2` varchar(200) DEFAULT NULL,
  `tranlog_fld3` decimal(19,2) DEFAULT NULL,
  `tranlog_fld4` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`tranlog_date`,`tranlog_branchid`,`tranlog_branchseq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;