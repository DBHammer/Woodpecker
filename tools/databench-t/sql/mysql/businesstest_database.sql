-- 建立测试库-- 
drop database if exists findpt;
create database findpt;


use findpt;

-- 建立网点表-- 
drop table if exists branch;
create table branch(
branch_id char(10) not null,
branch_seq bigint,
branch_dacno char(20),
branch_add varchar(200),
branch_tel varchar(50),
branch_fld1 varchar(200),
branch_fld2 varchar(200),
branch_fld3 decimal(19,2),
branch_fld4 decimal(19,2),
primary key(branch_id)
) engine = InnoDB DEFAULT CHARSET=utf8;

-- 建立科目表--
drop table if exists sjno;
create table sjno(
sjno_id char(5) not null,
sjno_branchid char(10),
sjno_bale decimal(19,2),
sjno_name varchar(200),
sjno_fld1 varchar(200),
sjno_fld2 varchar(200),
sjno_fld3 decimal(19,2),
sjno_fld4 decimal(19,2),
primary key(sjno_id,sjno_branchid)
) engine = InnoDB DEFAULT CHARSET=utf8;

-- 建立客户表--
drop table if exists customer;
create table customer(
customer_id char(15) not null,
customer_idtype char(1),
customer_idno varchar(100),
customer_name varchar(200),
customer_add varchar(200),
customer_birthday date,
customer_tel varchar(50),
customer_cdate date,
customer_cbrhid char(10),
customer_bale decimal(19,2),
customer_mdate date,
customer_mtime time,
customer_fld1 varchar(200),
customer_fld2 varchar(200),
customer_fld3 decimal(19,2),
customer_fld4 decimal(19,2),
primary key(customer_id)
) engine = InnoDB DEFAULT CHARSET=utf8;

-- 建立账户表-- 
drop table if exists account;
create table account(
account_id char(20) not null,
account_sjnoid char(5),
account_stat char(1),
account_custid char(15),
account_bale decimal(16,2),
account_pswd char(16),
account_name varchar(200),
account_amrz decimal(19,2),
account_inmod char(1),
account_itrz decimal(7,5),
account_branchid char(10),
account_mdate date,
account_mtime time,
account_fld1 varchar(200),
account_fld2 varchar(200),
account_fld3 decimal(19,2),
account_fld4 decimal(19,2),
primary key(account_id)
) engine = InnoDB DEFAULT CHARSET=utf8;

-- 建立交易流水表--
drop table if exists tranlist;
create table tranlist(
tranlist_date date not null,
tranlist_branchid char(10) not null,
tranlist_seq bigint not null,
tranlist_time time,
tranlist_accountid1 char(20),
tranlist_bale decimal(16,2),
tranlist_dcdir char(1),
tranlist_accountid2 char(20),
tranlist_fld1 varchar(200),
tranlist_fld2 varchar(200),
tranlist_fld3 decimal(19,2),
tranlist_fld4 decimal(19,2)
) engine = InnoDB DEFAULT CHARSET=utf8;


-- 建立交易配置表--
drop table if exists trancfg;
create table trancfg(
trancfg_testid varchar(20) not null,
trancfg_id char(4) not null,
trancfg_name varchar(100),
trancfg_procnum int,
trancfg_runnum int,
trancfg_trannum int,
trancfg_brkpot char(1),
trancfg_brktime smallint,
trancfg_fld1 varchar(200),
trancfg_fld2 varchar(200),
trancfg_fld3 decimal(19,2),
trancfg_fld4 decimal(19,2),
primary key(trancfg_testid,trancfg_id)
) engine = InnoDB DEFAULT CHARSET=utf8;

-- 建立数据配置表 --
drop table if exists datacfg;
create table datacfg(
datacfg_id char(4) not null,
datacfg_brhnum int,
datacfg_sjnonum int,
datacfg_accnum int,
datacfg_cusnum int,
datacfg_fld1 varchar(200),
datacfg_fld2 varchar(200),
datacfg_fld3 decimal(19,2),
datacfg_fld4 decimal(19,2),
primary key(datacfg_id)
) engine = InnoDB DEFAULT CHARSET=utf8;

-- 建立工资单-- 
drop table if exists salarylist;
create table salarylist(
salarylist_branchid char(10) not null,
salarylist_accountid char(20) not null,
salarylist_bale decimal(16,2),
salarylist_fld1 varchar(200),
salarylist_fld2 varchar(200),
salarylist_fld3 decimal(19,2),
salarylist_fld4 decimal(19,2)
) engine = InnoDB DEFAULT CHARSET=utf8;

-- 建立对账记录--
drop table if exists chkuplog;
create table chkuplog(
chkuplog_type char(1) not null,
chkuplog_id varchar(50) not null,
chkuplog_success char(4),
chkuplog_note varchar(1000),
chkuplog_fld1 varchar(200),
chkuplog_fld2 varchar(200),
chkuplog_fld3 decimal(19,2),
chkuplog_fld4 decimal(19,2)
) engine = InnoDB DEFAULT CHARSET=utf8;

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

-- 建立评测记录-- 
DROP TABLE IF EXISTS `runinfo`;
CREATE TABLE `runinfo` (
  `runinfo_id` varchar(20) NOT NULL,
  `process_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `datacfg_id` char(4) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `runinfo_strtm` varchar(50) DEFAULT NULL,
  `runinfo_endtm` varchar(50) DEFAULT NULL,
  `runinfo_costtime` int(11) DEFAULT NULL,
  `runinfo_cfg` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 建立index--
create index idx_customer on customer(customer_idtype,customer_idno,customer_name);
create index idx_salarylist on salarylist(salarylist_branchid);
create index idx_chkuplog on chkuplog(chkuplog_type,chkuplog_id);
create index idx_tranlist1 on tranlist(tranlist_accountid1);
create index idx_tranlist2 on tranlist(tranlist_accountid2);
create index idx_trancfg on trancfg(trancfg_id);

-- 建立constraint--

 -- 初始化 datacfg
INSERT INTO `datacfg` VALUES ('1001', '100', '10', '10000', '2000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1002', '200', '10', '30000', '5000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1003', '2000', '10', '100000', '20000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1004', '2000', '10', '300000', '50000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1005', '2000', '10', '1000000', '200000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1006', '2000', '10', '5000000', '500000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1007', '2000', '10', '10000000', '1000000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1008', '2000', '10', '20000000', '2000000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1009', '2000', '10', '30000000', '2000000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1010', '2000', '10', '50000000', '5000000', null, null, null, null);
INSERT INTO `datacfg` VALUES ('1011', '2000', '10', '100000000', '5000000', null, null, null, null);


-- 初始化 trancfg
INSERT INTO `trancfg` VALUES ('1', '1001', '转账', '10', '5000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('1', '1002', '查询', '10', '5000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('1', '1003', '代发工资', '5', '10', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('1', '1004', '存款', '10', '500', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('1', '1005', '取款', '10', '500', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('1', '1006', '资产盘点', '1', '1', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('2', '1001', '转账', '20', '20000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('2', '1002', '查询', '20', '20000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('2', '1003', '代发工资', '5', '10', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('2', '1004', '存款', '20', '2000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('2', '1005', '取款', '20', '2000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('2', '1006', '资产盘点', '1', '1', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('3', '1001', '转账', '50', '50000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('3', '1002', '查询', '50', '50000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('3', '1003', '代发工资', '5', '10', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('3', '1004', '存款', '50', '5000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('3', '1005', '取款', '50', '5000', '1', 'N', '0', null, null, null, null);
INSERT INTO `trancfg` VALUES ('3', '1006', '资产盘点', '1', '1', '1', 'N', '0', null, null, null, null);
-- 初始化公司客户
insert into customer(customer_id, customer_idtype, customer_idno, customer_name, customer_add, customer_birthday, customer_tel, customer_cdate,  customer_bale, customer_mdate, customer_mtime,customer_fld1,customer_fld2,customer_fld3,customer_fld4)
values ('1111111111', '1', '1101054271888381', '张小三', '北京市海淀区彩和坊路海淀北二街路口西500米', '2019-07-01', '15800000000', '2019-07-01', NULL, '2019-07-01', '20:55:37','VXc7EcJe3TRV5VGpfGH6K85SqKtEj0gLoar4Sl8YkQmp9rTneVXMnuwf5S7PL4Bmg49P9BNnIOAkSrlieFh8A2DNSSMyqXL13st5ZhGaU7iCURpzR5Fpty48m7QDscBgCileG8lUYbac9yI7Nbbvip','eLTm6somc1bXEnROuWvsPxi1iDgbxA0BTvtMAoZP96YBkr0iuRawRj8oslk0RfQxTHrmZVj8pkgERDfzSOPWDrO7VlChSjuJziR0xAxU5BRCN6IHHdc8oBtJUB6S2Sr1UYaY5QCOjHTr95HQDbfLaG',88888888.88,88888888.88);
