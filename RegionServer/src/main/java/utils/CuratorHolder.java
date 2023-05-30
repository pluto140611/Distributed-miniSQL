package utils;

import RegionManagers.ZkServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.*;

@Slf4j
public class CuratorHolder {

    private String host = null;
    private CuratorFramework client = null;

    public CuratorHolder(String ZK_HOST) {
        //连接ZooKeeper
        setUpConnection(ZK_HOST);
    }


    public void setUpConnection(String ZK_HOST) {
        this.host = ZK_HOST;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        if (client == null) {
            synchronized (this) {
                // 创建连接
                client = CuratorFrameworkFactory.builder()
                        .connectString(host)
                        .connectionTimeoutMs(ZkServiceManager.ZK_CONNECTION_TIMEOUT)
                        .sessionTimeoutMs(ZkServiceManager.ZK_SESSION_TIMEOUT)
                        .retryPolicy(retryPolicy)   // 重试策略：初试时间为1s 重试10次
                        .build();

                // 开启连接
                client.start();
//                log.info(client.toString());

            }
        }
    }

    public boolean checkNodeExist(String targetPath) throws Exception {
        if (client == null) {
            this.setUpConnection(ZkServiceManager.ZK_HOST);
        }
        Stat s = client.checkExists().forPath(targetPath);
        return s == null ? false : true;

    }

//    public String createNode(String targetPath, String value) throws Exception {
//        return client.create().creatingParentsIfNeeded().forPath(targetPath, value.getBytes());
//    }

    public List<String> getChildren(String path) throws Exception {
        if (client == null) {
            this.setUpConnection(ZkServiceManager.ZK_HOST);
        }
        return client.getChildren().forPath(path);
    }

    public String createNode(String path, String value, CreateMode nodeType) throws Exception {
        if (client == null) {
            this.setUpConnection(ZkServiceManager.ZK_HOST);
        }
        if (nodeType == null) {
            throw new RuntimeException("创建节点类型不合法");
        } else if (CreateMode.PERSISTENT.equals(nodeType)) {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, value.getBytes());
        } else if (CreateMode.EPHEMERAL.equals(nodeType)) {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, value.getBytes());
        } else if (CreateMode.PERSISTENT_SEQUENTIAL.equals(nodeType)) {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, value.getBytes());
        } else if (CreateMode.EPHEMERAL_SEQUENTIAL.equals(nodeType)) {
            return client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, value.getBytes());
        } else {
            throw new RuntimeException("创建节点类型不被采纳");
        }
    }
}
