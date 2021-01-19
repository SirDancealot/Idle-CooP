package common.src.main.client;

import common.src.util.PropManager;
import common.src.util.SpaceManager;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import java.io.IOException;


public class Client implements Runnable {

    private String username;
    private Space lobby;

    public Client(String username) {
        this.username = username;
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
            lobby.put("joinReq", PropManager.getProperty("externalIP"), PropManager.getProperty("localPort"), username);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SpaceManager.addClientExitEvent(() -> {
            try {
                lobby.put("exitReq", PropManager.getProperty("externalIP"), PropManager.getProperty("localPort"), username);
                ((RemoteSpace)lobby).close();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        Thread t = new Thread(new Chat(username, false));
        Chat.setWriter(t);
        t.start();

        new Thread(new ClientLogic(username,true, null, null)).start();
    }
}
