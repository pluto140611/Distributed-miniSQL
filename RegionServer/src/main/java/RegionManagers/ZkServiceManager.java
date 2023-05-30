package RegionManagers;

import lombok.extern.slf4j.Slf4j;
import utils.CuratorHolder;

@Slf4j
public class ZkServiceManager implements Runnable {

    public static final String ZK_HOST = "127.0.0.1:2181";

    public static final Integer ZK_SESSION_TIMEOUT = 3000;  //会话超时时间

    public static final Integer ZK_CONNECTION_TIMEOUT = 3000; //连接超时时间

    public static final String ZNODE = "/db";

    public static final String HOST_NAME_PREFIX = "Region_";



    @Override
    public void run() {
        try{
            // 连接Zookeeper
            CuratorHolder curatorHolder = new CuratorHolder(ZK_HOST);
//            if(!curatorHolder.checkNodeExist(ZNODE)) {
//                curatorHolder.createNode(ZNODE,"服务器主目录");
//            }

        }catch(Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage());
        }
    }

}
