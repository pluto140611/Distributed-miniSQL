package RegionManagers;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import miniSQL.Interpreter;
import utils.SocketFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;

@Slf4j
public class MasterSocketManager implements  Runnable{

    private Socket socket;
    private BufferedReader input = null;
    private PrintWriter output = null;
    private Boolean isRunning = false;
    public final String master = "192.168.43.27";
    public final int port = 15552;
    public final String table_catalog = "table_catalog";
    public final String index_catalog = "index_catalog";

    public MasterSocketManager()throws IOException{
        this.socket = new Socket(master,port);
        input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        output = new PrintWriter(this.socket.getOutputStream(),true);
        isRunning = true;
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
//        SocketFormat init = new SocketFormat("region",3,""); //通知主节点从节点上线
        this.sendToMaster(ipAddress,3);
    }

    public void sendToMaster(String msg, int type) {

        SocketFormat socketMsg = new SocketFormat("region",type,msg);
        String sendMsg = JSON.toJSONString(socketMsg);
        output.println(sendMsg);
    }
    @Override
    public void run() {
        try{
            String command = null;
            if(socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()){
                log.info("connection with Master break");
            }
            while(isRunning) {
                Thread.sleep(1000);
                command = input.readLine();
                if (command != null) {
                    log.info(command);
                    SocketFormat recoverCommand = JSON.parseObject(command,SocketFormat.class);
                    if (recoverCommand.getType() == 2) {
                        //进行容错容灾恢复
                        String content = recoverCommand.getContent();
                        if(content!=""){
                            String []commands = content.split("\\|");
                            for(int i = 0; i < commands.length; i++) {
                                Interpreter.interpret(commands[i]);
                            }
                        }

                    }
                    else if(recoverCommand.getType() == 3) {
                        //删除已经分配给其他region的表
                        String content = recoverCommand.getContent();
                        String []tables = content.split("\\|");
                        for (int i = 0; i < tables.length; i++) {
                            String tmpCommand = "drop table " + tables[i] + " ;";
                            Interpreter.interpret(tmpCommand);
                        }
                    }
                }


            }
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }

    }

}
