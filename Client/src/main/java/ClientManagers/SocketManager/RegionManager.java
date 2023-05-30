package ClientManagers.SocketManager;

import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RegionManager {
    //    private Socket socket;
    private HashMap<String, Thread> regionMap;
    private HashMap<Thread, Socket> socketMap;
    private HashMap<String, Interact_region> regionHashMap;

    public RegionManager() {
        this.regionMap = new HashMap<>();
        this.socketMap = new HashMap<>();
        this.regionHashMap = new HashMap<>();
    }

    public Thread getThread(String ip) {
        return regionMap.get(ip);
    }

    public Interact_region getRegion(String ip) {
        if (regionMap.containsKey(ip)) {
            return regionHashMap.get(ip);
        }
        return null;
    }

    public void closeThread() throws Exception {
        for (Map.Entry<String, Interact_region> entry : regionHashMap.entrySet()) {
            entry.getValue().closeRegionSocket();
        }
    }

    public Interact_region setThread(String ip) {
        try {
            Interact_region newThread = new Interact_region(ip);
            Thread thread = new Thread(newThread);
            thread.start();
            this.regionMap.put(ip, thread);
            this.socketMap.put(thread, newThread.socket);
            this.regionHashMap.put(ip, newThread);
            return newThread;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
