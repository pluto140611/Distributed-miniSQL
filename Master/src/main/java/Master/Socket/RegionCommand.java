package Master.Socket;

import Master.Service.SocketService;
import Master.Table;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: LENOVO
 * @since: 2023/5/28  11:18
 * @description:
 */
@Slf4j
public class RegionCommand {
    private Table table;
    private Socket socket;

    public RegionCommand(Table tableManger, Socket socket) {
        this.table = tableManger;
        this.socket = socket;
    }

   // public String processRegionCommand(String cmd) {
    public String processRegionCommand(SocketFormat send) throws IOException {
        String result = "";
        String ip_jvm =send.getContent();
        String ipAddress = socket.getInetAddress().toString();
        if(ipAddress.equals("127.0.0.1"))
            ipAddress = SocketService.getHostAddress();
        ipAddress=ipAddress.substring(1);
        if (send.getType()==3 ) {

        //    log.warn("从节点上线咯！^^");
        //    log.warn("--->"+ipAddress+"----"+ip_jvm);

            table.addip_format(ip_jvm,ipAddress);
            File file = new File(ipAddress+"log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            if(!table.existServer(ipAddress)) table.addServer(ipAddress);


            BufferedReader reader;
            reader = new BufferedReader(new FileReader(ipAddress+"log.txt"));
            String line = reader.readLine();
            Set<String> set = new HashSet<>();
            while (line != null) {
                String[] str = line.split(" ");
                if(str[0].equals("create")){
                    set.add(str[2]);
                }
                else if(str[0].equals("drop")){
                    set.remove(str[2]);
                }
                line = reader.readLine();
            }
            reader.close();
            for (String item : set) {
                result=result+item;
                result+="|";
            }


            if (file.exists() && file.isFile()){
                file.delete();}

            if (!file.exists()) {
                file.createNewFile();
            }

       }

        else if (send.getType()==2) {

            String sql = send.getContent();

            String ip = socket.getInetAddress().toString();
            ip=ip.substring(1);
           // log.warn("---->ip === "+ip);
            File file = new File(ip+"log.txt");
            FileWriter fw = null;
            fw = new FileWriter(file,true);
            fw.write(sql+"\n");
            fw.close();

            String[] line = sql.split(" ");
            if(line[0].equals("drop")){
                table.deleteTable(line[2],ip);
            }
            else if(line[0].equals("create")){
                table.addTable(line[2],ip);
            }
        }


        return result;
    }
}
