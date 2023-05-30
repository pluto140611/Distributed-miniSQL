package RegionManagers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import miniSQL.API;

@Data
@Slf4j
public class RegionManager {
    // Zookeerper管理工具类
    private ZkServiceManager zkServiceManager;

    private ClientManager clientManager;

    private MasterSocketManager masterSocketManager;


    public RegionManager() throws Exception{
        zkServiceManager = new ZkServiceManager();
        masterSocketManager = new MasterSocketManager();
        clientManager = new ClientManager(masterSocketManager);
        API.initial();
        log.info("init success!");
    }

    public void start() {
        try{

            Thread mainThread = new Thread(clientManager);
            mainThread.start();

            Thread masterThread = new Thread(masterSocketManager);
            masterThread.start();


        }catch(Exception e ){
            log.error(e.getMessage());
        }

    }

}
