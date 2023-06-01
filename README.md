# Distributed-miniSQL

## 本项目使用的通信数据包

| sender | type | content            | 备注（不用写在通信里）                                 | to（不用写在通信里） |
| ------ | ---- | ------------------ | ------------------------------------------------------ | -------------------- |
| master | 0    | success/fail       | 来自Master的响应数据包                                 |                      |
| master | 1    | Region ip          | 告诉client数据表位置，返回IP                           | tablename            |
| master | 2    | 日志               | 将一个Region的日志更新到其他Region                     | Region               |
| master | 3    | 上线时需要删除的表 | 从节点上线，获取已有数据表的副本，将节点更新至最新状态 | Region               |
| master | 4    | 更新cache          | 通知client更新cache                                    | client               |
| region | 0    | success/fail       | 来自Region的响应数据包                                 |                      |
| region | 1    | 数据表查询结构     | 告诉client minisql操作结果                             | client               |
| region | 2    | 日志内容           | 将日志上传至主服务器                                   | Master               |
| region | 3    | 从节点上线         | 无                                                     | Master               |
| client | 0    | success/fail       | 将自己的信息上传至服务器                               |                      |
| client | 1    | 新建表格           | 询问Master新的表格建在哪里                             | Master               |
| client | 2    | 查询一个表格的ip   | 查询一个表格由哪个Region进行维护                       | Master               |
| client | 3    | sql语句            | 请求Region执行SQL语句                                  | Region               |
| client | 4    | 客户端上线         | 向客户端发送IP                                         | Master               |

Region给client提供的端口是15551

Master给Region提供的端口是15552
