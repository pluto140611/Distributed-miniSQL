package ClientManagers;

import java.util.HashMap;
import java.util.Map;


public class CacheManager {
    private Map<String, String> cache;

    public CacheManager() {
        this.cache = new HashMap<>();
    }
    public String getCache(String table) {
        if (this.cache.containsKey(table)) {
            return this.cache.get(table);
        }
        return null;
    }
    public void setCache(String table, String server) {
        cache.put(table, server);
//        System.out.println("已将此表存入Cache：Table：" + table + " Port：" + table);
    }
}
