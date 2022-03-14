show variables like '%max_allowed_packet%';

set global max_allowed_packet =10*1024*1024;

show variables like "%_buffer%";

SET GLOBAL innodb_buffer_pool_size=16*1024*1024;