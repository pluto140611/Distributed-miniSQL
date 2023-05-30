package RegionManagers;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

@Slf4j
public class ClientManager implements Runnable {

    private ServerSocket serverSocket;//作为服务器监听客户端的请求
    private final int port = 15551;  //服务器监听在15551端口
    private HashMap<Socket,Thread>clientMap = new HashMap<Socket,Thread>();
    private MasterSocketManager masterSocketManager = null;
    public ClientManager(MasterSocketManager masterSocketManager) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.masterSocketManager = masterSocketManager;

    }
    @Override
    public void run(){
        try{
            while(true){
                Thread.sleep(1000);
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket, this.masterSocketManager);
                Thread thread = new Thread(clientThread);
                thread.start();
                this.clientMap.put(socket,thread);
                log.info("client " + socket.getRemoteSocketAddress().toString() + " has connected");

            }

        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
