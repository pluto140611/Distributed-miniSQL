package utils;

import lombok.Data;

@Data
public class SocketFormat {
    private String sender;   //发送者
    private int type;        //该数据包类型
    private String content;  //数据包具体内容

    public SocketFormat(String sender,int type, String content){
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    public SocketFormat(){
        this.sender = "";
        this.type = 0;
        this.content = "";
    }
}