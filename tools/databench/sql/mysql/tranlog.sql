
-- 建立交易库--
drop database if exists Tranlog;
create database Tranlog;


use Tranlog;

-- 建立交易日志表--
drop table if exists tranlog;
create table tranlog(
tranlog_date date not null,
tranlog_branchid char(10) not null,
tranlog_branchseq bigint not null,
tranlog_trancfgid char(4),
tranlog_strtime timestamp,
tranlog_endtime timestamp,
tranlog_tmcost int,
tranlog_success char(4),
tranlog_note varchar(2000),
tranlog_fld1 varchar(200),
tranlog_fld2 varchar(200),
tranlog_fld3 decimal(19,2),
tranlog_fld4 decimal(19,2),
primary key(tranlog_date,tranlog_branchid,tranlog_branchseq)
) engine = InnoDB;
