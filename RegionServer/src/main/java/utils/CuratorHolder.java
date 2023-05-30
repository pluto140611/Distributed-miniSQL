package utils;

import RegionManagers.ZkServiceManager;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

public class CuratorHolder {

    private String host = null;
    private CuratorFramework client = null;

    public CuratorHolder(String ZK_HOST){
    //连接ZooKeeper
        setUpConnection(ZK_HOST);
    }


    public void setUpConnection(String ZK_HOST) {
        this.host = ZK_HOST;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
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

            }
        }
    }

    public boolean checkNodeExist(String targetPath) throws Exception {
        if(client == null) {
            this.setUpConnection(ZkServiceManager.ZK_HOST);
        }
        Stat s = client.checkExists().forPath(targetPath);
        return s == null ? false : true;

    }

    public String createNode(String targetPath, String value) throws Exception {
        return client.create().creatingParentsIfNeeded().forPath(targetPath, value.getBytes());
    }
}
