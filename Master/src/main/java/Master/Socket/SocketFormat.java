package Master.Socket;

import lombok.Data;

/**
 * @author: LENOVO
 * @since: 2023/5/28  19:13
 * @description:
 */
@Data
public class SocketFormat {

    public SocketFormat(String sender, int type , String content){
        this.sender = sender;
        this.type = type;
        this.content = content;
    }


    private String sender;
    private int type;
    private String content;
}
