import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import Master.Master;
/**
 * @author: LENOVO
 * @since: 2023/5/28  00:34
 * @description:
 */

public class MasterServer {
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        Master masterManager = new Master();
        masterManager.initialize();
    }
}
