package common.src.main.client;

import common.src.util.SpaceManager;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;

public class ClientLogic implements Runnable{

    //TODO host local player

    //TODO host local gamestate

    private Space gameSpace;
    private Space userSpace;
    private String uname;

    public ClientLogic(String uname){
        this.uname = uname;
    }

    @Override
    public void run() {
        init();
    }

    private void init(){
        try {
            userSpace = new SequentialSpace();
            SpaceManager.exposePublicSpace(userSpace,"localGame");

            gameSpace = SpaceManager.getHostSpace("game");
            gameSpace.put(uname,"joined");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loop(){

        boolean stop = false;

        while(!stop){


        }
    }
}
