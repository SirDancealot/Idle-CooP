package common.src.main;

import org.jspace.RemoteSpace;
import java.io.IOException;
import java.util.Scanner;


public class Client implements Runnable {

    private String ip;
    private String spaceName;
    private int port;
    private RemoteSpace outbox;

    public Client (String ip, int port, String spaceName){
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
