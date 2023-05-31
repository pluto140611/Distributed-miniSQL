package Master.Strategy;

import Master.Socket.SocketFormat;
import Master.Socket.SocketThread;
import Master.Table;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: LENOVO
 * @since: 2023/5/28  11:27
 * @description:
 */
@Slf4j
public class StrategyProcessor {

    final int DISCOVER = 1;
    final int RECOVER = 2;
    final int INVALID = 3;

    private Table table;

    public StrategyProcessor(Table table) {
        this.table = table;
    }

    public boolean existServer(String hostUrl) {
        return table.existServer(hostUrl);
    }

    public void execStrategy(String hostUrl, int type) {
        try {
            switch (type) {
                case RECOVER:
                    execRecoverStrategy(hostUrl);
                    break;
                case DISCOVER:
                    execDiscoverStrategy(hostUrl);
                    break;
                case INVALID:
                    execInvalidStrategy(hostUrl);
                    break;
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
/*
* 失效策略
* 读取iplog.txt中的sql语句
*
* 追加iplog.txt中的sql语句到bestInet.txt
* */
    private void execInvalidStrategy (String hostUrl) throws IOException{
         hostUrl = table.ip_format.get(hostUrl);
        String bestInet = table.getBestServer(hostUrl);
        log.warn("bestInet:"+bestInet);
        File file_2 = new File(bestInet+"log.txt");
        BufferedReader reader;

      //  FileWriter fw = null;
        FileWriter fw_1 = null;
        fw_1 = new FileWriter(file_2,true);
       String str = "";
       String ip_table = bestInet ;
        String json = "";
        try {
            reader = new BufferedReader(new FileReader(
                    hostUrl+"log.txt"));

            String line = reader.readLine();
            int num = 0;
            while (line != null) {
      //          log.warn(line);
                if(num==1){
                    str+="|";
                }
               str+=line;
                fw_1.write(line);
                num = 1;
                line = reader.readLine();
            }
            SocketFormat send = new SocketFormat("master",2,str);
             json = JSON.toJSONString(send);
            fw_1.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.warn(bestInet+"-----"+hostUrl);
       ip_table += table.exchangeTable(bestInet,hostUrl);
        SocketFormat send2 = new SocketFormat("master",4,ip_table);
       String json_2 = JSON.toJSONString(send2);
        SocketThread socketThread = table.getSocketThread(bestInet);
  //      log.warn(json);
        socketThread.sendToRegion(json);

        List<SocketThread> list = table.getClientAll();
        for(SocketThread item  : list){
            socketThread = item;
            socketThread.sendToRegion(json_2);
        }
    }

    private void execDiscoverStrategy(String hostUrl) throws IOException {


    }

    private void execRecoverStrategy(String hostUrl) throws IOException {



    }
}
