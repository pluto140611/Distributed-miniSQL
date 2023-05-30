package RegionManagers;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import miniSQL.API;
import miniSQL.BUFFERMANAGER.BufferManager;
import miniSQL.Interpreter;
import utils.SocketFormat;

import java.io.*;
import java.net.Socket;

@Slf4j
public class ClientThread implements Runnable {
    private Socket socket;
    private BufferedReader input = null;
    private BufferedWriter bufferedWriter = null;
    private PrintWriter output = null;
    private PrintStream printStream = null;  //暂时未用到
    private Boolean isRunning = false;
    private MasterSocketManager masterSocketManager = null;

    public ClientThread(Socket socket, MasterSocketManager masterSocketManager) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        this.output = new PrintWriter(this.socket.getOutputStream(), true);
        this.printStream = new PrintStream(this.socket.getOutputStream());
        this.masterSocketManager = masterSocketManager;
        this.isRunning = true;
    }

    @Override
    public void run() {
        String command = null;
        try {
            while (isRunning) {
                Thread.sleep(1000);
                command = input.readLine();
                if (command != null) {
                    log.info(this.socket.getPort() + " : " + command);
                    SocketFormat sql = JSON.parseObject(command, SocketFormat.class);
                    if (this.processCommand(sql.getContent(), this.socket.getInetAddress().toString())) {
                        SocketFormat regionLog = new SocketFormat("region",2, sql.getContent());
                        masterSocketManager.sendToMaster(sql.getContent(), 2);
                    }

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Boolean processCommand(String cmd, String ip) {
        try {
            String result = Interpreter.interpret(cmd);
            API.store();
            SocketFormat res = new SocketFormat("region", 1, result);
            String socketRes = JSON.toJSONString(res);
            sendToClient(socketRes);
            String[] parts = result.split(" ");
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public void sendToClient(String info) throws Exception {
//        output.println("result: " + info);
        bufferedWriter.write(info);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
}
