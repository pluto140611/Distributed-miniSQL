package Master;

import Master.Socket.SocketManager;

import java.io.IOException;

/**
 * @author: LENOVO
 * @since: 2023/5/28  00:37
 * @description:
 */

public class Master {
    private ZookeeperManager zookeeperManager;
    private SocketManager socketManager;
    private Table tableManger;

    private final int PORT = 15552;

    public Master() throws IOException, InterruptedException {
        tableManger = new Table();
        zookeeperManager = new ZookeeperManager(tableManger);
        socketManager = new SocketManager(PORT,tableManger);
    }

    public void initialize() throws InterruptedException, IOException {
        // 第一个线程在启动时向ZooKeeper发送请求，获得ZNODE目录下的信息并且持续监控，如果发生了目录的变化则执行回调函数，处理相应策略。
        Thread zkThread = new Thread(zookeeperManager);
        zkThread.start();

        // 第二个线程负责处理与从节点之间的通信，以及响应客户端的请求
        socketManager.startService();
    }
}
