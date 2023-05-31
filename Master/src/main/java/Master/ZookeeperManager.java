package Master;

import Master.Service.CuratorHolder;
import Master.Strategy.Monitor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: LENOVO
 * @since: 2023/5/28  00:38
 * @description:
 */
@Slf4j
public class ZookeeperManager implements Runnable{
    private Table tableManger;
    //ZooKeeper集群访问的端口
    public static final String ZK_HOST = "192.168.43.27:2181";
    //    public static final String ZK_HOST = "172.20.10.10:2181";
    //ZooKeeper会话超时时间
    public static final Integer ZK_SESSION_TIMEOUT = 3000;
    //ZooKeeper连接超时时间
    public static final Integer ZK_CONNECTION_TIMEOUT = 3000;
    //ZooKeeper集群内各个服务器注册的节点路径
    public static final String ZNODE = "/db";
    //ZooKeeper集群内各个服务器注册自身信息的节点名前缀
    public static final String HOST_NAME_PREFIX = "Region_";

    public ZookeeperManager(Table tableManger) {
        this.tableManger = tableManger;
    }

    @Override
    public void run() {
        this.startMonitor();
    }

    // 一些zookeeper的测试代码
    public void startMonitor(){
        try {
            // 开启一个连接
            CuratorHolder curatorClientHolder = new CuratorHolder(ZK_HOST);
            // 创建服务器主目录
            if (!curatorClientHolder.checkNodeExist(ZNODE)){
                curatorClientHolder.createNode(ZNODE,"服务器主目录");
            }

            // 开始监听服务器目录，如果有节点的变化，则处理相应事件
            curatorClientHolder.monitorChildrenNodes(ZNODE, new Monitor(curatorClientHolder,tableManger));
        } catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
}
