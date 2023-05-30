# Distributed-miniSQL

## 本项目使用的通信数据包

| sender | type | content                | 备注（不用写在通信里）     | to（不用写在通信里） |
| ------ | ---- | ---------------------- | -------------------------- | -------------------- |
| master | 0    | success/fail           | 来自Master的响应数据包     |                      |
| master | 1    | Region ip              | 告诉client某个表建在哪里   | client               |
| master | 2    | 日志                   | 告诉一个Region，同伴挂了， | Region               |
| master | 3    | 需要删除的表           | 从节点上线清除缓存         | Region               |
| region | 0    | success/fail           | 来自Region的响应数据包     |                      |
| region | 1    | 数据表查询结构         | 告诉client minisql操作结果 | client               |
| region | 2    | 日志内容               | 将日志上传至主服务器       | Master               |
| region | 3    | 从节点上线             | 无                         | Master               |
| client | 0    | success/fail           | 来自client的响应数据包     |                      |
| client | 1    | 新建表格               | 告诉Master要新建表格       | Master               |
| client | 2    | 找不到表格，表格在哪里 | 问Master表格在哪里         | Master               |
| client | 3    | sql语句                | 想要执行sql语句            | Region               |

Region给client提供的端口是15551

Master给Region提供的端口是15552
