package common.src.main.client;

import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.main.GameCalculations;
import common.src.util.SpaceManager;
import org.jspace.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClientLogic implements Runnable{

    private Space gameSpace;
    private Space userSpace;
    private Space GUIjob;
    private GameState gameState;
    private PlayerState playerState;
    private String uname;
    private boolean state;
    private String currentWork;
    private Map<String, PlayerState> unameToPlayerState;


    private Space forest;
    private Space mine;
    private Space huntingGrounds;
    private Space field;
    private Space constructionSite;

    public ClientLogic(String uname, boolean state, GameState gameState, PlayerState playerState){
        this.uname = uname;
        this.state = state;
        this.gameState = gameState;
        this.playerState = playerState;
    }

    @Override
    public void run() {
        if(state){
            initCom();
            loopCom();
        } else{
            initWork();
            loopWork();
        }
    }

    private void initCom(){
        try {
            userSpace = new SequentialSpace();
            SpaceManager.exposePublicSpace(userSpace,"localGame");
            GUIjob = new SequentialSpace();
            SpaceManager.addLocalSpace(GUIjob,"GUIjob");

            gameSpace = SpaceManager.getHostSpace("game");
            gameSpace.put(uname,"joined");
            SpaceManager.addClientExitEvent(() -> {
                try {
                    gameSpace.put(uname,"disconnect");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopCom = true;
            });

            Object[] data;
            data = userSpace.get(new ActualField("gameState"), new FormalField(GameState.class));
            gameState = (GameState) data[1];
            data = userSpace.get(new ActualField("playerState"), new FormalField(PlayerState.class));
            playerState = (PlayerState) data[1];




        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new ClientLogic(uname,false, gameState, playerState)).start();
    }
    boolean stopCom = false;
    private void loopCom(){
        Object[] data;
        while(!stopCom){
            try {
                data = GUIjob.get(new FormalField(String.class));
                startWorkAtHost(data[0].toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startWorkAtHost(String job){
        try {
            gameSpace.put(uname,"work");
            boolean jobGiven = false;
            if (!jobGiven) {
                userSpace.get(new ActualField("jobReq"));
                gameSpace.put(uname,"job",job);
                Object[] data = userSpace.get(new ActualField("job"), new FormalField(String.class));
                if (data[1].toString().equals(job)) {
                    gameSpace.put(uname, true);
                    jobGiven = true;
                } else {
                    gameSpace.put(uname, false);
                    jobGiven = false;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initWork(){
        unameToPlayerState = new HashMap<>();
        unameToPlayerState.put(uname,playerState);
        try {
            forest = SpaceManager.getHostSpace("forest");
            mine = SpaceManager.getHostSpace("mine");
            huntingGrounds = SpaceManager.getHostSpace("huntingGrounds");
            field = SpaceManager.getHostSpace("field");
            constructionSite = SpaceManager.getHostSpace("constructionSite");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean stopWork = false;
    private void loopWork(){

        GameCalculations gameCalculations =  new GameCalculations(forest,mine,huntingGrounds,field,constructionSite,gameState,unameToPlayerState,true);
        SpaceManager.addClientExitEvent(() -> stopWork = true);
        while (!stopWork){
            gameCalculations.update();
        }
    }
}
