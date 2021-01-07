package common.src.main;

import common.src.util.PropManager;
import org.jspace.RemoteSpace;
import java.io.IOException;
import java.util.Scanner;


public class Client implements Runnable {

    private String ip;
    private String spaceName;
    private String port;
    private RemoteSpace outbox;

    public Client (String ip, String port, String spaceName){
      this.ip = ip;
      this.spaceName = spaceName;
      this.port = port;
    }

    @Override
    public void run() {
        try {
            init();
            loop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        outbox = new RemoteSpace("tcp://" + ip + ":" + port + "/"+ spaceName + "?keep" );
    }

    private void loop() throws InterruptedException {
        Scanner scan = new Scanner(System.in);
        while(true){
            outbox.put(scan.nextLine());
        }
    }
}
