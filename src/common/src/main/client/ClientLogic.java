package common.src.main.client;

import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.main.GameCalculations;
import common.src.util.SpaceManager;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.util.Map;

public class ClientLogic implements Runnable{

    private Space gameSpace;
    private Space userSpace;
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

            gameSpace = SpaceManager.getHostSpace("game");
            gameSpace.put(uname,"joined");

            forest = SpaceManager.getHostSpace("forest");
            mine = SpaceManager.getHostSpace("mine");
            huntingGrounds = SpaceManager.getHostSpace("huntingGrounds");
            field = SpaceManager.getHostSpace("field");
            constructionSite = SpaceManager.getHostSpace("constructionSite");

            Object[] data;
            data = userSpace.get(new ActualField("gameState"), new FormalField(GameState.class));
            gameState = (GameState) data[1];
            data = userSpace.get(new ActualField("playerState"), new FormalField(PlayerState.class));
            playerState = (PlayerState) data[1];

        } catch (Exception e) {
            e.printStackTrace();
        }

        //new Thread(new ClientLogic(uname,false, gameState, playerState)).start();
    }

    private void loopCom(){

        Object[] data;
        String uname;
        boolean stop = false;

        while(!stop){

            // if player press start state
            if(false){

                String job;
                //startWorkAtHost(job);

            }
        }
    }

    private void startWorkAtHost(String job){
        try {
            gameSpace.put(uname,"state");
            userSpace.get(new ActualField("jobReq"));
            gameSpace.put(uname,"job",job);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initWork(){

        unameToPlayerState.put(uname,playerState);
    }

    boolean stop = false;
    private void loopWork(){

        GameCalculations gameCalculations =  new GameCalculations(forest,mine,huntingGrounds,field,constructionSite,gameState,unameToPlayerState,true);
        SpaceManager.addClientExitEvent(() -> { stop = true; });
        while (!stop){

            gameCalculations.update();
        }
    }
}
