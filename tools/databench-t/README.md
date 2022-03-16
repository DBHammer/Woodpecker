# 事务型数据库测试工具

#### 介绍
中国信通院自研，事务型数据库测试工具

业务流程图：

![输入图片说明](%E8%AF%B4%E6%98%8E%E6%96%87%E6%A1%A3/%E5%9B%BE%E7%89%870.png)

按照业务流程顺序分为六大模块：
1.数据模型定义
2.业务逻辑定义
3.数据生成
4.业务执行
5.运行监测
6.结果校验

#### 软件架构
![输入图片说明](%E8%AF%B4%E6%98%8E%E6%96%87%E6%A1%A3/%E5%9B%BE%E7%89%871.png)

#### 安装教程

1.  环境准备
1）硬件配置：一般情况下，按照用户的业务量需求，根据业务场景和数据量来计算所需服务器的配置。一般单节点部署要求运行内存为4G及以上。
2）软件配置： JDK 1.8+ ，Mysql 5+

2. 目录介绍
1）sql (包含本地应用库myql脚本,业务测试库mysql脚本,业务测试库oracle脚本)；
2）说明文档 (包含数据库说明文档，mapper说明文档，部署文档)；
3）soft (包含运行软件ftdb.jar、application.properties、mapperConfig、localMapperConfig)；
4）oracle业务库替换\mapperConfig (包含业务测试库oralce所要替换的mapper文件)；
5）oracle业务库替换\sql (包含业务测试库oralce所需要的sql脚本)。

3. 注意事项
1）系统部署过程中，ftdb.jar、application.properties、mapperConfig、localMapperConfig需要在同一个目录下。
2）本软件分为2个库，业务测试库和本地应用库：
3）本地应用库为系统运行基本数据库为mysql类型；
4）业务测试库目前提供2种数据库类型：mysql和oracle，oracle需要替换的文件在"oracle业务库替换"目录中，只需要执行oracle的建库脚本，修改application.properties数据库连接，替换mapperConfig目录即可。
5）使用JAVA开发工具(IDEA/Eclipse)导入源码，使用开发工具中maven进行打包，即可生成ftdb.jar。


#### 使用说明

1. 数据库脚本执行
1）执行业务测试数据库脚本：
source sql/businesstest_database.sql;
2）执行本地应用数据库脚本：
source sql/local_database.sql;

2. 应用配置文件修改
1) 数据库连接修改，配置文件名称：application.properties
2) 系统参数修改表paramcfg
init_page_size(数据初始化时customer表和account表在多线程并发插入时每线程一次插入的条数,默认5000)
update_customer_bale(数据初始化更新汇总账户余额时分页大小,默认100000)

3. 常用命令
1) master/slave节点启动命令
nohup java -Dfile.encoding=utf-8 -jar ftdb.jar master --spring.config.location = ./application.properties > ./nohup.out 2>&1 &
2) 停止应用节点命令
ps -ef|grep ftdb (查询进程号)
kill -9 进程号
3) 初始化数据命令
java -Dfile.encoding=utf-8 -jar ftdb.jar init 1001 --spring.config.location = ./application.properties
参数一:为数据库中datacfg数据规模表中的数据规模id
4) 业务运行命令
java -Dfile.encoding=utf-8 -jar ftdb.jar test 1001 1 RR --spring.config.location = ./application.properties
注意(此第一个参数数据规模必须和初始化数据时选择的数据规模一致)
参数一为数据库中datacfg数据规模表中的数据规模id
参数二为数据库中trancfg运行配置表中的配置id
参数三为数据库的隔离级别，目前支持RR和RC两种模式

4. 常见问题处理
1) 每次想要重新初始化数据，需要重新建立业务库和本地库
2) oracle会有sql语句长度限制，当使用oracle时可根据实际情况修改paramcfg表中init_page_size参数
3) 业务测试如遇到死锁的情况，是因为数据初始化规模设置过小，datacfg表总datacfg_id为1001仅为功能测试使用的测试集，网点数目较少，容易出现死锁情况，真正业务测试时可选择datacfg表中更大数据规模。
4) 应用默认端口是8080，如果要修改，可修改application.properties配置文件中server.port的值。


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

