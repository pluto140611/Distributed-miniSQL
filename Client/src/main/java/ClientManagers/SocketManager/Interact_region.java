package ClientManagers.SocketManager;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import utils.SocketFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class Interact_region implements Runnable {
    public Socket socket;
    private String ip = null;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean isRunning;
    private Thread infoListener;

    private static final int port = 15551;

    public Interact_region(String ip) throws Exception {
        this.ip = ip;
//        infoListener = new InfoListener();
        this.socket = new Socket(ip, this.port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.isRunning = true;
        log.info("connected with" + ip + " success!");
    }

//    public void setRegionIP(String ip) {
//        this.region = ip;
//    }

//    public void connectRegionServer(String ip, int PORT) throws IOException {
//        establishConnection(ip, PORT);
//        System.out.println(">>客户端已与从节点" + PORT + "成功建立连接");
//    }

//    public void connectRegionServer(String ip) throws IOException {
//        System.out.println("connectRegionServer : " + ip);
//        establishConnection(ip, 22222);
//        System.out.println(">>客户端已与从节点" + ip + "成功建立连接");
//    }

//    public void establishConnection(String ip) throws IOException {
//        socket = new Socket(ip, this.port);
//        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        output = new PrintWriter(socket.getOutputStream(), true);
//        isRunning = true;
//        log.info("connected with" + ip + " success!");
////        infoListener.start();
//    }

    public String receiveFromRegion() throws IOException {
        String line = "";
        if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
//            System.out.println(">>Socket连接已经关闭!");
            log.info("Socket connect with region break");
            return line;
        } else {
            line = input.readLine();
            SocketFormat res = JSON.parseObject(line, SocketFormat.class);
            log.info("repy: " + res.getContent());
//            System.out.println(""res.getContent());
        }
        if (line != null) {
//            System.out.println(">>服务端发出消息：" + line);
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
        SocketFormat temp = new SocketFormat("client", 3, info);
        output.println(JSON.toJSONString(temp));
    }

    @Override
    public void run() {
        String line = "";
        try {
            while (isRunning) {
                Thread.sleep(1000);
//                if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
//                    isRunning = false;
//                    break;
//                }
//                    receiveFromRegion();
                line = input.readLine();
                if (line != null) {
                    log.info(line);
                    SocketFormat socketFormat = JSON.parseObject(line, SocketFormat.class);
//                    log.info("reply: " + socketFormat.getContent());
                    System.out.println(socketFormat.getContent());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }
//    class InfoListener extends Thread {
//        @Override
//        public void run() {
////            System.out.println(">>客户端与从服务器监听线程启动成功");
////            while (isRunning) {
////                if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
////                    isRunning = false;
////                    break;
////                }
////
////                try {
////                    receiveFromRegion();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////
////                try {
////                    sleep(100);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////            }
//        }
//    }
}