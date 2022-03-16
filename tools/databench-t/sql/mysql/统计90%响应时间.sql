说明：tranlog_fld1为每次业务测试的流水号，业务测试完成后,本地库执行select tranlog_fld1 from tranlog order by tranlog_strtime desc limit 1
就可取到
1001为业务类型 0.9为90%的响应时间，该值每次执行sql时需要修改
1001	转账
1002	查询
1003	代发工资
1004	存款
1005	取款

SET @ID=0;
select a.* from (
SELECT @ID:=@ID+1 AS ID , tranlog_tmcost FROM tranlog WHERE tranlog_trancfgid = '1001' and tranlog_fld1 = '9cf45414655b4a63a789a968ad002460' ORDER BY `tranlog_tmcost` asc) a 
where a.ID = round((select count(1) from tranlog WHERE tranlog_trancfgid = '1001' and tranlog_fld1 = 
'9cf45414655b4a63a789a968ad002460')*0.9 );
