package common.src.main.client;

import common.src.util.PropManager;
import common.src.util.SpaceManager;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.io.IOException;
import java.util.Scanner;


public class Client implements Runnable {

    private String ip;
    private String port;
    private Space lobby;
    private RemoteSpace outbox;

    public Client (String ip, String port){
      this.ip = ip;
      this.port = port;
    }

    @Override
    public void run() {
        try {
            init();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        lobby = SpaceManager.getHostSpace("lobby");
        try {
            lobby.put("joinReq", PropManager.getProperty("externalIP"), PropManager.getProperty("localPort"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(new Chat(false));
        Chat.setWriter(t);
        t.start();
        //outbox = new RemoteSpace("tcp://" + ip + ":" + port + "/"+ spaceName + "?keep" );
    }

    private void loop() throws InterruptedException {
        Scanner scan = new Scanner(System.in);
        while(true){
            outbox.put(scan.nextLine());
        }
    }
}
