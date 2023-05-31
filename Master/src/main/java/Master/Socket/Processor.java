package Master.Socket;

import Master.Table;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

/**
 * @author: LENOVO
 * @since: 2023/5/28  11:09
 * @description:
 */
@Slf4j
public class Processor {
    private Table table;
    private Socket socket;
    private RegionCommand regionCommand;
   private  ClientCommand clientCommand;
    public Processor(Table table, Socket socket){
        this.clientCommand = new ClientCommand(table,socket);
        this.regionCommand = new RegionCommand(table,socket);
        this.table = table;
        this.socket = socket;
    }
    public String commandProcess(String cmd) throws IOException {
        SocketFormat send = JSON.parseObject(cmd,SocketFormat.class);
     String sender =   send.getSender();
     int type =  send.getType();
      String content =  send.getContent();
      //  log.warn(cmd);
    //    log.warn(sender+"---"+type+"---"+content);
        String result = "";
//        if (cmd.startsWith("<client>")) {
        if (sender.equals("client")) {
            // 去掉前缀之后开始处理

            result = clientCommand.processClientCommand(send);
            //result = clientCommand.processClientCommand(cmd.substring(8));
        } else  {
           result = regionCommand.processRegionCommand(send);
          // result = regionCommand.processRegionCommand(cmd.substring(8));
        }

            return result;
    }
}
