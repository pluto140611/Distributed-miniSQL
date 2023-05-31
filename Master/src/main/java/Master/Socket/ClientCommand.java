package Master.Socket;

import Master.Table;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * @author: LENOVO
 * @since: 2023/5/28  11:18
 * @description:
 */
@Slf4j
public class ClientCommand {
    private Table table;
    private Socket socket;

    public ClientCommand(Table table, Socket socket){
        this.table = table;
        this.socket = socket;
    }
    public String processClientCommand(SocketFormat send) throws IOException {
    //public String processClientCommand(String cmd) {
        String result = "";

        String tablename = send.getContent();
        if (send.getType()==2) {
            result = table.getInetAddress(tablename)+"|"+tablename ;
        } else if (send.getType()==1) {
            result = table.getBestServer()+"|"+tablename ;
        } else if (send.getType()==4){
           // SocketThread socketThread = new SocketThread(socket,table);
           // table.addClientThread(send.getContent(),socketThread);
        }
        log.warn(result);
        return result;

    }

}
