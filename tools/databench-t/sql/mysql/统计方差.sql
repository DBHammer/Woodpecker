说明：tranlog_fld1为每次业务测试的流水号，业务测试完成后,本地库执行select tranlog_fld1 from tranlog order by tranlog_strtime desc limit 1
就可取到

select case 
    when tranlog_trancfgid = '1001' then '转账'
    when tranlog_trancfgid = '1002' then '查询'
    when tranlog_trancfgid = '1003' then '代发工资'
    when tranlog_trancfgid = '1004' then '存款'
    when tranlog_trancfgid = '1005' then '取款'
 end '业务名称',
format(var_pop(tranlog_tmcost),2) as '方差',
format(stddev_samp(tranlog_tmcost),2) as '标准差',
format(var_samp(tranlog_tmcost),2) as '样本方差' 
from tranlog  where tranlog_fld1 = '9cf45414655b4a63a789a968ad002460' group by tranlog_trancfgid