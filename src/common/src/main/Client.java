package common.src.main;

import org.jspace.RemoteSpace;
import java.io.IOException;
import java.util.Scanner;


public class Client implements Runnable {

    String ip;
    String spaceName;
    int port;

    public Client (String ip, String spaceName, int port){

      this.ip = ip;
      this.spaceName = spaceName;
      this.port = port;

    }

    @Override
    public void run() {

        RemoteSpace outbox = null;
        try {
            outbox = new RemoteSpace("tcp://" + ip + ":" + port + "/"+ spaceName + "?keep" );

            while(true){

                Scanner scan = new Scanner(System.in);

                outbox.put(scan.nextLine());

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
