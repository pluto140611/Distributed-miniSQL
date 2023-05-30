package ClientManagers.SocketManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Interact_region {
    public Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean isRunning;
    private Thread infoListener;

    private String region = "localhost";

    public Interact_region() {
        infoListener = new InfoListener();
    }

    public void setRegionIP(String ip) {
        this.region = ip;
    }

    public void connectRegionServer(int PORT) throws IOException {
        establishConnection(region, PORT);
        System.out.println(">>客户端已与从节点" + PORT + "成功建立连接");
    }

    public void connectRegionServer(String ip) throws IOException {
        System.out.println("connectRegionServer : " + ip);
        establishConnection(ip, 22222);
        System.out.println(">>客户端已与从节点" + ip + "成功建立连接");
    }

    private void establishConnection(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        isRunning = true;
        infoListener.start();
    }

    public String receiveFromRegion() throws IOException {
        String line = "";
        if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            System.out.println(">>Socket连接已经关闭!");
            return line;
        } else {
            line = input.readLine();
        }
        if (line != null) {
            System.out.println(">>服务端发出消息：" + line);
        }
        return line;
    }

    public void closeRegionSocket() throws IOException {
        isRunning = false;
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }

    public void sendToRegion(String info) {
        output.println(info);
    }

    class InfoListener extends Thread {
        @Override
        public void run() {
            System.out.println(">>客户端与从服务器监听线程启动成功");
            while (isRunning) {
                if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
                    isRunning = false;
                    break;
                }

                try {
                    receiveFromRegion();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}