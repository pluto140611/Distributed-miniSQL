import ClientManagers.ClientManager;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        ClientManager clientManager = new ClientManager();
        clientManager.run();
    }

}
