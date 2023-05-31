package Master;

import Master.Socket.SocketThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @author: LENOVO
 * @since: 2023/5/28  00:38
 * @description:
 */

public class Table {
    public Map<String,String> ip_format;
    private Map<String, String> tableInfo;
    //一个用于记录所有连接过的从节点ip的list
    private List<String> serverList;
    //一个用于记录当前活跃的从节点ip，以及每个从节点ip对应的table list
    private Map<String,List<String>> aliveServer;

    // ip地址与相对应的socket
    private Map<String, SocketThread> socketThreadMap;

    private Map<String, SocketThread> clientThreadMap;

    public Table() throws IOException {
        serverList = new ArrayList<>();
        tableInfo = new HashMap<>();
        aliveServer = new HashMap<>();
        socketThreadMap = new HashMap<>();
        ip_format =new HashMap<>();
        clientThreadMap = new HashMap<>();
    }

    public void addClientThread(String hostUrl, SocketThread socketThread){

        clientThreadMap.put(ip_format.get(hostUrl),socketThread);

    }

    public List<SocketThread> getClientAll(){
        List<SocketThread> res = new ArrayList<>();
        for(Map.Entry<String, SocketThread> entry : clientThreadMap.entrySet()){
            res.add(entry.getValue());
        }
        return res;
    }
    public void addip_format(String jvm , String real){
        ip_format.put(jvm,real);
    }
    public void addTable(String table, String inetAddress) {
        tableInfo.put(table, inetAddress);
        if(aliveServer.containsKey(inetAddress)){
            aliveServer.get(inetAddress).add(table);
        }
        else{
            List<String> temp = new ArrayList<>();
            temp.add(table);
            aliveServer.put(inetAddress,temp);
        }
    }

    public void deleteTable(String table, String inetAddress) {
        tableInfo.remove(table);
        aliveServer.get(inetAddress).removeIf(table::equals);

    }

    public String getInetAddress(String table){
        for(Map.Entry<String, String> entry : tableInfo.entrySet()){
            if(entry.getKey().equals(table)){
                return entry.getValue();
            }
        }
        return null;
    }

    public String getBestServer(){
        Integer min = Integer.MAX_VALUE;
        String result = "";
        for(Map.Entry<String, List<String>> entry : aliveServer.entrySet()){
            if(entry.getValue().size()<min){
                min = entry.getValue().size();
                result = entry.getKey();
            }
        }

        return result;
    }
    public String getBestServer(String hostUrl){
        Integer min = Integer.MAX_VALUE;
        String result = "";
        for(Map.Entry<String, List<String>> entry : aliveServer.entrySet()){
            if(!entry.getKey().equals(hostUrl) && entry.getValue().size()<min){
                min = entry.getValue().size();
                result = entry.getKey();
            }
        }
        return result;
    }

    public void addServer(String hostUrl) {
        if(!existServer(hostUrl))
            serverList.add(hostUrl);
        List<String> temp = new ArrayList<>();
        aliveServer.put(hostUrl,temp);
    }

    public boolean existServer(String hostUrl) {
        for(String s : serverList){
            if(s.equals(ip_format.get(hostUrl)))
                return true;
        }
        return false;
    }

    public List<String> getTableList(String hostUrl) {
        for(Map.Entry<String, List<String>> entry : aliveServer.entrySet()){
            if(entry.getKey().equals(hostUrl)){
                return  entry.getValue();
            }
        }
        return null;
    }

    public void addSocketThread(String hostUrl, SocketThread socketThread) {

        socketThreadMap.put(ip_format.get(hostUrl),socketThread);
    }

    public SocketThread getSocketThread(String hostUrl) {
        for(Map.Entry<String, SocketThread> entry : socketThreadMap.entrySet()){
            if(entry.getKey().equals(hostUrl))
                return entry.getValue();
        }
        return null;
    }

    public List<SocketThread> getAllThread(){
        List<SocketThread> res = new ArrayList<>();
        for(Map.Entry<String, SocketThread> entry : socketThreadMap.entrySet()){
            res.add(entry.getValue());
        }
        return res;
    }

    public String exchangeTable(String bestInet, String hostUrl) {
        String res ="";
        List <String> tableList = getTableList(hostUrl);
        for(String table : tableList){
            tableInfo.put(table,bestInet);
            res+="|"+table;
        }
        List <String> bestInetTable = aliveServer.get(bestInet);
        bestInetTable.addAll(tableList);
        aliveServer.put(bestInet,bestInetTable);
        aliveServer.remove(hostUrl);
        return res;
    }

    public void recoverServer(String hostUrl) {
        List<String> temp = new ArrayList<>();
        aliveServer.put(hostUrl,temp);
    }

}
