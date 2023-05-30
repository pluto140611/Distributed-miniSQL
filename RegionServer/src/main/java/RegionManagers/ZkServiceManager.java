package RegionManagers;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import utils.CuratorHolder;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
public class ZkServiceManager implements Runnable {

    public static final String ZK_HOST = "192.168.43.27:2181";

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
            int nChildren = curatorHolder.getChildren(this.ZNODE).size();
            if(nChildren==0)
                curatorHolder.createNode(getRegisterPath() + nChildren, this.getHostAddress(), CreateMode.EPHEMERAL);
            else{
                String index = String.valueOf(Integer.parseInt((curatorHolder.getChildren(ZNODE)).get(nChildren - 1).substring(7)) + 1);
                curatorHolder.createNode(getRegisterPath() + index, this.getHostAddress(), CreateMode.EPHEMERAL);
            }


            // 阻塞该线程，直到发生异常或者主动退出
            synchronized (this) {
                wait();
            }
        }catch(Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage());
        }
    }

    public String getRegisterPath() {
        return ZNODE + "/" + HOST_NAME_PREFIX;
    }

    public static String getHostAddress() {
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip != null
                            && ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                            && ip.getHostAddress().indexOf(":")==-1){
                        return ip.getHostAddress();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
