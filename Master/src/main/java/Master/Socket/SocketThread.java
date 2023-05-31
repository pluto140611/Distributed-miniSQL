package Master.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Master.Table;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
/**
 * @author: LENOVO
 * @since: 2023/5/28  10:51
 * @description:
 */
@Slf4j
public class SocketThread implements Runnable{
    private boolean isRunning = false;
    private Processor processor;
    public BufferedReader input = null;
    public PrintWriter output = null;

    public Table table;

    public SocketThread(Socket socket, Table table) throws IOException {
        this.processor = new Processor(table,socket);
        this.table = table;
        this.isRunning = true;
        // 基于Socket建立输入输出流
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("服务端建立了新的客户端子线程:" + socket.getInetAddress() +":"+ socket.getPort());
    }

    @Override
    public void run() {
        String line;
        try {
            while (isRunning) {
                Thread.sleep(Long.parseLong("1000"));
                line = input.readLine();
                if (line != null) {
                 String str =   processor.commandProcess(line);
                    SocketFormat send = JSON.parseObject(line,SocketFormat.class);
                    String sender =   send.getSender();
                    /*
                    * 新的从节点上线，返回要删除的表名
                    * */
                    int type =  send.getType();
                        if(sender.equals("region")&&type==3){
                            table.addSocketThread(send.getContent(),this);
                            SocketFormat info = new SocketFormat("master",3,str);
                            String jsonsend = JSON.toJSONString(info);
                            this.sendToRegion(jsonsend);
                        }
                        else if(sender.equals("client")){
                            if(type==4){
                                table.addClientThread(send.getContent(),this);
                            }
                          else{SocketFormat info = new SocketFormat("master",1,str);
                                String jsonsend = JSON.toJSONString(info);
                                this.sendToRegion(jsonsend);}
                        }

                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }

    public void sendToRegion(String info) {
        output.println(info);
    }

}
