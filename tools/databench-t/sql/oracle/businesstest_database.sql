-- 建立网点表-- 
create table branch(
branch_id varchar2(10) not null primary key,
branch_seq number,
branch_dacno varchar2(20),
branch_add varchar2(200),
branch_tel varchar2(50),
branch_fld1 varchar2(200),
branch_fld2 varchar2(200),
branch_fld3 number(19,2),
branch_fld4 number(19,2)
);

-- 建立科目表--
create table sjno(
sjno_id varchar2(5) not null,
sjno_branchid varchar2(10),
sjno_bale number(19,2),
sjno_name varchar2(200),
sjno_fld1 varchar2(200),
sjno_fld2 varchar2(200),
sjno_fld3 number(19,2),
sjno_fld4 number(19,2),
primary key(sjno_id,sjno_branchid)
);

-- 建立客户表--
create table customer(
customer_id varchar2(15) not null primary key,
customer_idtype varchar2(1),
customer_idno varchar2(100),
customer_name varchar2(200),
customer_add varchar2(200),
customer_birthday date,
customer_tel varchar2(50),
customer_cdate date,
customer_cbrhid varchar2(10),
customer_bale number(19,2),
customer_mdate date,
customer_mtime timestamp,
customer_fld1 varchar2(200),
customer_fld2 varchar2(200),
customer_fld3 number(19,2),
customer_fld4 number(19,2)
);

-- 建立账户表-- 
create table account(
account_id varchar2(20) not null primary key,
account_sjnoid varchar2(5),
account_stat varchar2(1),
account_custid varchar2(15),
account_bale number(16,2),
account_pswd varchar2(16),
account_name varchar2(200),
account_amrz number(19,2),
account_inmod varchar2(1),
account_itrz number(7,5),
account_branchid varchar2(10),
account_mdate date,
account_mtime timestamp,
account_fld1 varchar2(200),
account_fld2 varchar2(200),
account_fld3 number(19,2),
account_fld4 number(19,2)
);

-- 建立交易流水表--
create table tranlist(
tranlist_date date not null,
tranlist_branchid varchar2(10) not null,
tranlist_seq number not null,
tranlist_time timestamp,
tranlist_accountid1 varchar2(20),
tranlist_bale number(16,2),
tranlist_dcdir varchar2(1),
tranlist_accountid2 varchar2(20),
tranlist_fld1 varchar2(200),
tranlist_fld2 varchar2(200),
tranlist_fld3 number(19,2),
tranlist_fld4 number(19,2)
);

-- 建立交易配置表--
create table trancfg(
trancfg_testid varchar2(20) not null,
trancfg_id varchar2(4) not null,
trancfg_name varchar2(100),
trancfg_procnum number,
trancfg_runnum number,
trancfg_trannum number,
trancfg_brkpot varchar2(1),
trancfg_brktime number,
trancfg_fld1 varchar2(200),
trancfg_fld2 varchar2(200),
trancfg_fld3 number(19,2),
trancfg_fld4 number(19,2),
primary key (trancfg_testid,trancfg_id)
);

-- 建立数据配置表 --
create table datacfg(
datacfg_id varchar2(4) not null primary key,
datacfg_brhnum number,
datacfg_sjnonum number,
datacfg_accnum number,
datacfg_cusnum number,
datacfg_fld1 varchar2(200),
datacfg_fld2 varchar2(200),
datacfg_fld3 number(19,2),
datacfg_fld4 number(19,2)
);

-- 建立工资单--
create table salarylist(
salarylist_branchid varchar2(10) not null,
salarylist_accountid varchar2(20) not null,
salarylist_bale number(16,2),
salarylist_fld1 varchar2(200),
salarylist_fld2 varchar2(200),
salarylist_fld3 number(19,2),
salarylist_fld4 number(19,2)
);

-- 建立对账记录--
create table chkuplog(
chkuplog_type varchar2(1) not null,
chkuplog_id varchar2(50) not null,
chkuplog_success varchar2(4),
chkuplog_note varchar2(1000),
chkuplog_fld1 varchar2(200),
chkuplog_fld2 varchar2(200),
chkuplog_fld3 number(19,2),
chkuplog_fld4 number(19,2)
);

-- 建立交易日志表--
create table tranlog(
tranlog_date date not null,
tranlog_branchid varchar2(10) not null,
tranlog_branchseq number not null,
tranlog_trancfgid varchar2(4),
tranlog_strtime number(20) DEFAULT NULL,
tranlog_endtime number(20) DEFAULT NULL,
tranlog_tmcost number(11) DEFAULT NULL,
tranlog_success varchar2(4),
tranlog_note varchar2(2000),
tranlog_fld1 varchar2(200),
tranlog_fld2 varchar2(200),
tranlog_fld3 number(19,2),
tranlog_fld4 number(19,2),
primary key(tranlog_date,tranlog_branchid,tranlog_branchseq)
);

CREATE TABLE tranlog_history (
  tranlog_date date NOT NULL,
  tranlog_branchid varchar2(10) NOT NULL,
  tranlog_branchseq number(20) NOT NULL,
  tranlog_trancfgid varchar2(4) DEFAULT NULL,
  tranlog_strtime number(20) DEFAULT NULL,
  tranlog_endtime number(20) DEFAULT NULL,
  tranlog_tmcost number(11) DEFAULT NULL,
  tranlog_success varchar2(4) DEFAULT NULL,
  tranlog_note varchar2(2000) DEFAULT NULL,
  tranlog_fld1 varchar2(200) DEFAULT NULL,
  tranlog_fld2 varchar2(200) DEFAULT NULL,
  tranlog_fld3 number(19,2) DEFAULT NULL,
  tranlog_fld4 number(19,2) DEFAULT NULL,
  PRIMARY KEY (tranlog_date,tranlog_branchid,tranlog_branchseq)
);

-- 建立评测记录-- 
create table runinfo(
runinfo_id varchar2(20) not null,
process_id varchar(32),
datacfg_id char(4),
runinfo_strtm varchar2(50),
runinfo_endtm varchar2(50),
runinfo_costtime number(11),
runinfo_cfg varchar2(1000),
primary key(runinfo_id,process_id)
);

-- 建立index--
create index idx_customer on customer(customer_idtype,customer_idno,customer_name);
create index idx_salarylist on salarylist(salarylist_branchid);
create index idx_chkuplog on chkuplog(chkuplog_type,chkuplog_id);
create index idx_tranlist1 on tranlist(tranlist_accountid1);
create index idx_tranlist2 on tranlist(tranlist_accountid2);
create index idx_trancfg on trancfg(trancfg_id);


--初始化数据---
-- 初始化 datacfg
insert all
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1001',100,10,10000,2000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1002',200,10,30000,5000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1003',2000,10,100000,20000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1004',2000,10,300000,50000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1005',2000,10,1000000,200000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1006',2000,10,5000000,500000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1007',2000,10,10000000,1000000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1008',2000,10,20000000,2000000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1009',2000,10,30000000,2000000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1010',2000,10,50000000,5000000)
into datacfg (datacfg_id,datacfg_brhnum,datacfg_sjnonum,datacfg_accnum,datacfg_cusnum) values ('1011',2000,10,100000000,5000000)
SELECT 1 FROM DUAL;

-- 初始化 trancfg  
INSERT ALL   
INTO  trancfg  VALUES ('1', '1001', '转账', '10', '5000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('1', '1002', '查询', '10', '5000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('1', '1003', '代发工资', '5', '10', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('1', '1004', '存款', '10', '500', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('1', '1005', '取款', '10', '500', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('1', '1006', '资产盘点', '1', '1', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('2', '1001', '转账', '20', '20000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('2', '1002', '查询', '20', '20000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('2', '1003', '代发工资', '5', '10', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('2', '1004', '存款', '20', '2000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('2', '1005', '取款', '20', '2000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('2', '1006', '资产盘点', '1', '1', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('3', '1001', '转账', '50', '50000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('3', '1002', '查询', '50', '50000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('3', '1003', '代发工资', '5', '10', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('3', '1004', '存款', '50', '5000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('3', '1005', '取款', '50', '5000', '1', 'N', '0', null, null, null, null)
INTO  trancfg  VALUES ('3', '1006', '资产盘点', '1', '1', '1', 'N', '0', null, null, null, null)
SELECT 1 FROM DUAL;
INSERT ALL  
INTO customer (customer_id,customer_idtype,customer_idno,customer_name,customer_add,customer_birthday,customer_tel,customer_cdate,customer_bale,customer_mdate,customer_mtime,customer_fld1,customer_fld2,customer_fld3,customer_fld4) values ('1111111111','1','1101054271888381','张小三','北京市海淀区彩和坊路海淀北二街路口西500米',sysdate,'15800000000',sysdate,NULL,sysdate,sysdate,'VXc7EcJe3TRV5VGpfGH6K85SqKtEj0gLoar4Sl8YkQmp9rTneVXMnuwf5S7PL4Bmg49P9BNnIOAkSrlieFh8A2DNSSMyqXL13st5ZhGaU7iCURpzR5Fpty48m7QDscBgCileG8lUYbac9yI7Nbbvip','eLTm6somc1bXEnROuWvsPxi1iDgbxA0BTvtMAoZP96YBkr0iuRawRj8oslk0RfQxTHrmZVj8pkgERDfzSOPWDrO7VlChSjuJziR0xAxU5BRCN6IHHdc8oBtJUB6S2Sr1UYaY5QCOjHTr95HQDbfLaG',88888888.88,88888888.88)
SELECT 1 FROM DUAL;
commit;

