package ClientManagers;

import ClientManagers.SocketManager.Interact_master;
import ClientManagers.SocketManager.Interact_region;
import ClientManagers.SocketManager.RegionManager;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Slf4j
/**
 * 客户端管理程序
 */
public class ClientManager {

    private static final String QUIT_COMMAND = "quit;";
    private static final String CREATE_COMMAND = "create";
    private static final String DROP_COMMAND = "drop";
    private static final String INSERT_COMMAND = "insert";
    private static final String DELETE_COMMAND = "delete";
    private static final String SELECT_COMMAND = "select";
    private static final String FROM_COMMAND = "from";
    private static final String NAME_KEY = "name";
    private static final String ERROR_KEY = "error";
    private static final String KIND_KEY = "kind";
    private static final String CACHE_KEY = "cache";
    private static final String TRUE_VALUE = "true";
    private static final String FALSE_VALUE = "false";

    public CacheManager cacheManager;
    public Interact_master Interact_master;
    //    public Interact_region Interact_region;
    public RegionManager regionManager;


    public ClientManager() throws IOException {
        // 绑定一个cacheManager
        this.cacheManager = new CacheManager();
        this.Interact_master = new Interact_master(this);
        this.regionManager = new RegionManager();
//        this.Interact_region = new Interact_region();
        log.info("Connect to Master success!");
        log.warn("Init");
    }

    public void run() throws Exception {

        Thread masterThread = new Thread(this.Interact_master);
        masterThread.start();

//        System.out.println(">>客户端启动成功");
        Scanner input = new Scanner(System.in);
        String line = "";
        while (true) {
            String sql = readSqlStatement(input);
            if (sql.trim().equals(QUIT_COMMAND)) {
                closeConnections();
                regionManager.closeThread();
                break;
            }
            Map<String, String> commandInfo = interpretCommand(sql);
            if (commandInfo.containsKey(ERROR_KEY)) {
//                System.out.println(">>输入的SQL语句有误 \n>>请重新输入:");
                log.error("输入的SQL语句有误，请重新输入");
                continue;
            }
            executeCommand(sql, commandInfo);
        }
    }

    private String readSqlStatement(Scanner input) {
        StringBuilder sql = new StringBuilder();
        String line = "";
        // 读入一句完整的SQL语句
        System.out.println("Input Sql:");
        while (line.isEmpty() || line.charAt(line.length() - 1) != ';') {
            line = input.nextLine();
            if (!line.isEmpty()) {
                sql.append(line).append(' ');
            }
        }
        System.out.println(sql.toString());
        return sql.toString();
    }

    private void closeConnections() throws IOException {
        this.Interact_master.closeMasterSocket();

    }

    private void executeCommand(String sql, Map<String, String> commandInfo) throws IOException, InterruptedException {
        String table = commandInfo.get(NAME_KEY);
//        System.out.println(">>输入的的表名：" + table);
        log.info("table: " + table);
        //如果是创建表格
        if (CREATE_COMMAND.equals(commandInfo.get(KIND_KEY))) {
            this.Interact_master.createTable(table);
            //阻塞直到cache中查询IP信息。
            while (true) {
                Thread.sleep(1000);
                if (this.cacheManager.getCache(table) != null) {
                    break;
                }
            }
            executeCachedCommand(sql, table);
        } else {
            if (TRUE_VALUE.equals(commandInfo.get(CACHE_KEY))) {
                executeCachedCommand(sql, table);
            }
        }
    }

    private void executeCachedCommand(String sql, String table) throws IOException, InterruptedException {
        String cache = cacheManager.getCache(table);
        if (cache == null) {
            log.info("This table isn‘in cache");
//            this.Interact_master.process(sql, table);
            this.Interact_master.findTable(table);
            while (true) {
                Thread.sleep(1000);
                if (this.cacheManager.getCache(table) != null) {
                    log.info("This table is in IP: " + this.cacheManager.getCache(table));
                    break;
                }
            }
            connectToRegion(this.cacheManager.getCache(table), sql);
        } else {
            // 如果查到了端口号就直接在Interact_region中进行连接
//            System.out.println(">>该表在客户端Cache中！\n>>其对应的服务器：" + cache);
            log.info("This table is in IP: " + cache);
            connectToRegion(cache, sql);
        }
    }

    public void connectToRegion(String ip, String sql) throws IOException, InterruptedException {
        /*        this.Interact_region.connectRegionServer(ip);*/
//        this.Interact_region.establishConnection(ip);
//        Thread.sleep(100);
//        this.Interact_region.sendToRegion(sql);
        Interact_region temp_region = regionManager.getRegion(ip);
        if(temp_region == null ){
            temp_region = this.regionManager.setThread(ip);
        }

        if (temp_region == null) {
            log.error("Connect to region error！");
            return;
        }
        this.regionManager.getRegion(ip).sendToRegion(sql);

    }

    private Map<String, String> interpretCommand(String sql) {
        Map<String, String> result = new HashMap<>();
        result.put(CACHE_KEY, TRUE_VALUE);
        sql = sql.replaceAll("\\s+", " ");
        String[] words = sql.split(" ");

        result.put(KIND_KEY, words[0]);
        switch (words[0]) {
            case CREATE_COMMAND:
                handleCreateCommand(result, words);
                break;
            case DROP_COMMAND:
            case INSERT_COMMAND:
            case DELETE_COMMAND:
                handleTableOperationCommand(result, words);
                break;
            case SELECT_COMMAND:
                handleSelectCommand(result, words);
                break;
            default:
                result.put(ERROR_KEY, TRUE_VALUE);
        }
        return result;
    }

    private void handleCreateCommand(Map<String, String> result, String[] words) {
        result.put(CACHE_KEY, FALSE_VALUE);
        result.put(NAME_KEY, words[2]);
    }

    private void handleTableOperationCommand(Map<String, String> result, String[] words) {
        String name = words[2].replace("(", "")
                .replace(")", "").replace(";", "");
        result.put(NAME_KEY, name);
    }

    private void handleSelectCommand(Map<String, String> result, String[] words) {
        for (int i = 0; i < words.length; i++) {
            if (FROM_COMMAND.equals(words[i]) && i != words.length - 1) {
                result.put(NAME_KEY, words[i + 1]);
                break;
            }
        }
        if (!result.containsKey(NAME_KEY)) {
            result.put(ERROR_KEY, TRUE_VALUE);
        }
    }
}
