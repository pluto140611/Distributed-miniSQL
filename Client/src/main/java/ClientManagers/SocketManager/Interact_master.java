package ClientManagers.SocketManager;

import ClientManagers.ClientManager;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import utils.SocketFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class Interact_master implements Runnable {
    private static final String MASTER = "192.168.43.27";
    private static final int PORT = 15552;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean isRunning;
    //    private InfoListener infoListener;
    private ClientManager clientManager;
    private Map<String, String> commandMap = new HashMap<>();

    public Interact_master(ClientManager clientManager) throws IOException {
        this.clientManager = clientManager;
        socket = new Socket(MASTER, PORT);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        isRunning = true;
        if (socket != null) {
            log.info("Master connect success");
//            System.out.println("Master connect success");
        }
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        SocketFormat temp = new SocketFormat("client",4,ipAddress);
        output.println(JSON.toJSONString(temp));
//        connectToMaster();
    }

    public void closeMasterSocket() throws IOException {
        isRunning = false;
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
//        infoListener.interrupt();
    }

    @Override
    public void run() {
        try{
            String line = "";
            while(isRunning) {
                Thread.sleep(1000);
                line = input.readLine();
                if(line != null) {
                    log.info(line);
                    SocketFormat socketFormat = JSON.parseObject(line,SocketFormat.class);
                    if (socketFormat.getType() ==1 ) {
                        String [ ]contents = socketFormat.getContent().split("\\|");
                        clientManager.cacheManager.setCache(contents[1],contents[0]);
                    }
                    else if (socketFormat.getType()==4){
                        String []contents = socketFormat.getContent().split("\\|");
                        clientManager.cacheManager.setCache(contents[1],contents[0]);
                        log.info("cache update success");
                    }
                }


            }
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    public void createTable(String table) {
        SocketFormat temp = new SocketFormat("client",1,table);
        output.println(JSON.toJSONString(temp));
    }

    public void findTable(String table) {
        SocketFormat temp = new SocketFormat("client",2,table);
        output.println(JSON.toJSONString(temp));
    }

//    private void connectToMaster() throws IOException {
//
////        startInfoListener();
//    }

//    private void startInfoListener() {
//        infoListener = new InfoListener();
//        infoListener.start();
//    }

//    public void sendToMaster(String info) {
//        output.println("<client>[1]" + info);
//    }
//
//    public void sendToMasterCreate(String info) {
//        output.println("<client>[2]" + info);
//    }
//
//    public void process(String sql, String table) {
//        commandMap.put(table, sql);
//        sendToMaster(table);
//    }
//
//    public void processCreate(String sql, String table) {
//        commandMap.put(table, sql);
//        sendToMasterCreate(table);
//    }



//    public void receiveFromMaster() throws IOException, InterruptedException {
//        try {
//            String line = input.readLine();
//            if (line != null) {
////                System.out.println(">>服务器发出的消息：" + line);
////                if (line.startsWith("<table>")) {
////                    handleTableMessage(line);
////                } else if (line.startsWith("<master>[1]") || line.startsWith("<master>[2]")) {
////                    handleMasterMessage(line);
////                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



//    private void handleTableMessage(String message) throws Exception{
//        String[] args = message.substring(7).split(" ");
//        String sql = commandMap.get(args[0]);JDK虚拟机ip地址和无线局域网ip地址不同
//        System.out.println(sql);
//        if (sql != null) {
//            int port = Integer.parseInt(args[1]);
//            commandMap.remove(args[0]);
//            clientManager.cacheManager.setCache(args[0], String.valueOf(port));
//            clientManager.connectToRegion(port, sql);
//        }
//    }
//
//    private void handleMasterMessage(String message) throws Exception {
//        String[] args = message.substring(11).split(" ");
//        String ip = args[0], table = args[1];
//        clientManager.cacheManager.setCache(table, ip);
//        clientManager.connectToRegion(ip, commandMap.get(table));
//    }

//    private class InfoListener extends Thread {
//        @Override
//        public void run() {
////            System.out.println(">>客户端与主服务器监听线程启动成功");
//            log.info("connect with Master success");
//            while (isRunning) {
//                if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
//                    isRunning = false;
//                    break;
//                }
//                try {
//                    receiveFromMaster();
//                    sleep(100);
//                } catch (IOException | InterruptedException e) {
//                    isRunning = false;
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}
