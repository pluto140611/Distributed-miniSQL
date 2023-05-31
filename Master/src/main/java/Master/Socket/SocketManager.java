package Master.Socket;

import Master.Service.SocketService;
import Master.Table;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * @author: LENOVO
 * @since: 2023/5/28  11:04
 * @description:
 */

public class SocketManager {
    private ServerSocket serverSocket;
    private Table table;
    public SocketManager(int port,Table table) throws IOException, InterruptedException {
        this.table = table;
        this.serverSocket = new ServerSocket(port);
    }
    public void startService() throws InterruptedException, IOException {
        while (true) {
            Thread.sleep(200);
            // 等待与之连接的客户端
            Socket socket = serverSocket.accept();
            // 建立子线程并启动
            SocketThread socketThread = new SocketThread(socket,table);
            String ipAddress = socket.getInetAddress().toString();
            if(ipAddress.equals("127.0.0.1"))
                ipAddress = SocketService.getHostAddress();
           // table.addSocketThread(ipAddress,socketThread);
            Thread thread = new Thread(socketThread);
            thread.start();
        }
    }
}

