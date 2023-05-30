package ClientManagers.SocketManager;

import ClientManagers.ClientManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Interact_master {

    private static final String MASTER = "localhost";
    private static final int PORT = 12345;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean isRunning;
    private InfoListener infoListener;

    private ClientManager clientManager;
    private Map<String, String> commandMap = new HashMap<>();

    public Interact_master(ClientManager clientManager) throws IOException {
        this.clientManager = clientManager;
        connectToMaster();
    }

    private void connectToMaster() throws IOException {
        socket = new Socket(MASTER, PORT);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        isRunning = true;
        startInfoListener();
    }

    private void startInfoListener() {
        infoListener = new InfoListener();
        infoListener.start();
    }

    public void sendToMaster(String info) {
        output.println("<client>[1]" + info);
    }

    public void sendToMasterCreate(String info) {
        output.println("<client>[2]" + info);
    }

    public void process(String sql, String table) {
        commandMap.put(table, sql);
        sendToMaster(table);
    }

    public void processCreate(String sql, String table) {
        commandMap.put(table, sql);
        sendToMasterCreate(table);
    }

    public void closeMasterSocket() throws IOException {
        isRunning = false;
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
        infoListener.interrupt();
    }

    public void receiveFromMaster() throws IOException, InterruptedException {
        String line = input.readLine();
        if (line != null) {
            System.out.println(">>服务器发出的消息：" + line);
            if (line.startsWith("<table>")) {
                handleTableMessage(line);
            } else if (line.startsWith("<master>[1]") || line.startsWith("<master>[2]")) {
                handleMasterMessage(line);
            }
        }
    }

    private void handleTableMessage(String message) {
        String[] args = message.substring(7).split(" ");
        String sql = commandMap.get(args[0]);
        System.out.println(sql);
        if (sql != null) {
            int port = Integer.parseInt(args[1]);
            commandMap.remove(args[0]);
            clientManager.cacheManager.setCache(args[0], String.valueOf(port));
            clientManager.connectToRegion(port, sql);
        }
    }

    private void handleMasterMessage(String message) {
        String[] args = message.substring(11).split(" ");
        String ip = args[0], table = args[1];
        clientManager.cacheManager.setCache(table, ip);
        clientManager.connectToRegion(ip, commandMap.get(table));
    }

    private class InfoListener extends Thread {
        @Override
        public void run() {
            System.out.println(">>客户端与主服务器监听线程启动成功");
            while (isRunning) {
                if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
                    isRunning = false;
                    break;
                }

                try {
                    receiveFromMaster();
                    sleep(100);
                } catch (IOException | InterruptedException e) {
                    isRunning = false;
                    e.printStackTrace();
                }
            }
        }
    }
}
