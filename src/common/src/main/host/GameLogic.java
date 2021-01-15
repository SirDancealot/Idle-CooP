package common.src.main.host;

import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.util.SpaceManager;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GameLogic implements Runnable{

    private Map <String,Space> unameToSpace = new HashMap<>();
    private Map <String, PlayerState> unameToPlayerState = new HashMap<>();
    private Space clients;
    private Space gameSpace;
    private GameState gameState;
    private boolean communicating;

    private Space forest;
    private Space mine;
    private Space huntingGrounds;
    private Space field;
    private Space constructionSite;

    public GameLogic(Boolean communicating){

        this.communicating = communicating;

        gameSpace = new SequentialSpace();
        SpaceManager.exposeHostSpace(gameSpace, "game");
    }

    @Override
    public void run() {

        if(communicating){
            initCom();
            loadAllPlayers();
            loopCom();
        } else {
            initWork();
            loadAllPlayers();
            loopWork();
            saveAllPlayers();
            exit();
        }
    }

    private void initCom() {

        new Thread(new GameLogic(false));

        forest = new SequentialSpace();
        mine = new SequentialSpace();
        huntingGrounds = new SequentialSpace();
        field = new SequentialSpace();
        constructionSite = new SequentialSpace();

        SpaceManager.addLocalSpace(forest,"forest");
        SpaceManager.addLocalSpace(mine,"mine");
        SpaceManager.addLocalSpace(huntingGrounds,"huntingGrounds");
        SpaceManager.addLocalSpace(field,"field");
        SpaceManager.addLocalSpace(constructionSite,"constructionSite");
    }

    private void loopCom(){

        Object[] data;
        String uname, action;

        boolean stop = false;

        while(!stop){
            try {
                data = gameSpace.get(new FormalField(String.class), new FormalField(String.class));
                uname = data[0].toString();
                action = data[1].toString();
                switch (action){
                    case "joined":
                        data = clients.query(new FormalField(String.class), new FormalField(String.class), new ActualField(uname));
                        unameToSpace.put(uname,SpaceManager.getRemoteSpace(data[0].toString(),data[1].toString(),"localGame"));

                        if(!unameToPlayerState.containsKey(uname))
                            unameToPlayerState.put(uname,new PlayerState());
                        unameToSpace.get(uname).put("playerState",unameToPlayerState.get(uname));
                        break;

                        //TODO update working thread with new player

                    case "work":
                        //req job
                        writeToUser(uname, "jobReq");
                        //get job
                        data = gameSpace.get(new ActualField(uname), new ActualField("job"), new FormalField(String.class));

                        switch (data[2].toString()){
                            case "woodCutting":
                                SpaceManager.getLocalSpace("forest").put(data[0]);
                                break;
                            case "mining":
                                SpaceManager.getLocalSpace("mine").put(data[0]);
                                break;
                            case "hunting":
                                SpaceManager.getLocalSpace("huntingGrounds").put(data[0]);
                                break;
                            case "farm":
                                SpaceManager.getLocalSpace("field").put(data[0]);
                                break;
                            case "construction":
                                SpaceManager.getLocalSpace("constructionSite").put(data[0]);
                                break;
                        }


                        break;

                    case "stop":

                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loopWork(){

        boolean stop = false;

        long lastNs = System.nanoTime();
        double dt = 1.0;
        int missingTicks = 0;
        double nsPerTick = 100000;


        while(!stop){

            long nowNs = System.nanoTime();
            long deltaNs = nowNs - lastNs;
            lastNs = nowNs;

            dt += deltaNs / nsPerTick;
            missingTicks = (int)dt;

            if (missingTicks > 0) {
                tick();
                dt--;
            }
        }
    }

    private void writeToUser(String uname,String req){

        try {
            unameToSpace.get(uname).put(req);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void exit(){

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("/data/GameState.ser");
            ObjectOutput oos = new ObjectOutputStream(fos);
            oos.writeObject(gameState);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initWork() {

        try {
            clients = SpaceManager.getLocalSpace("clients");
        } catch (IOException e) {
            System.out.println("Client space could not be found.");
            e.printStackTrace();
        }

        try {
            FileInputStream fis = new FileInputStream("/data/GameState.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            gameState = (GameState) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            gameState = new GameState();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }

        try {
            forest = SpaceManager.getLocalSpace("forest");
            mine = SpaceManager.getLocalSpace("mine");
            huntingGrounds = SpaceManager.getLocalSpace("huntingGrounds");
            field = SpaceManager.getLocalSpace("field");
            constructionSite = SpaceManager.getLocalSpace("constructionSite");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Wood stats
    private final int woodHP = 30;
    private int woodDmg = 0;

    //Stone stats
    private final int stoneHP = 50;
    private int stoneDmg = 0;

    //Animal stats
    private final int animalHP = 600;
    private int animalDmg = 0;

    //Field stats
    private final int wheatHP = 6000;
    private int wheatDmg = 0;

    //Construction site
    private final int houseHP = 30;
    private int houseDmg = 0;

    private void tick(){

        //Forest
        woodDmg += workingOn(forest);

        if(woodDmg >= woodHP){

            gameState.addWood(1);
            woodDmg %= woodHP;
        }
        //Mine
        for (int i = 1; i <= workingOn(mine); i++){
            stoneDmg += i;
        }

        if(stoneDmg >= stoneHP){

            gameState.addStone(1);
            stoneDmg %= stoneHP;
        }
        //Hunting Grounds
        if(workingOn(huntingGrounds) >= 2){
            animalDmg += workingOn(huntingGrounds);
        }

        if(animalDmg >= animalHP){

            gameState.addMeat(1);
            animalDmg %= animalHP;
        }
        //Fields
        wheatDmg += workingOn(field);

        if(wheatDmg >= wheatHP){
            gameState.addWheat(6*workingOn(field));
            wheatDmg %= wheatHP;
        }
        //Construction Site
        houseDmg += workingOn(constructionSite);

        if(houseDmg >= houseHP){
            if(gameState.getWood() > 0 && gameState.getStone() > 0 && (gameState.getMeat() > 0 || gameState.getWheat() > 0)){
                gameState.addHouses(1);
                gameState.addWood(-1);
                gameState.addStone(-1);
                if(gameState.getWheat() > 0)
                    gameState.addWheat(-1);
                else
                    gameState.addMeat(-1);

                animalDmg %= animalHP;
            }
        }
    }

    private int workingOn(Space workers){

        try {
            return workers.queryAll(new FormalField(String.class)).size();
        } catch (InterruptedException e) {
            System.out.println("No workers could be found");
            e.printStackTrace();
            return 0;
        }
    }

    private void loadAllPlayers(){

        try {

            File dir = new File("/data/players/");
            for (File file : dir.listFiles()) {
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutput oos = new ObjectOutputStream(fos);
                PlayerState ps = new PlayerState();
                oos.writeObject(ps);
                unameToPlayerState.put(file.getName().split(".")[0],ps);
                oos.close();
                fos.close();
            }
            
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    private void saveAllPlayers(){

        Map<String, String> m = new HashMap<>();
        m.forEach((String key, String value) -> {
            try{
                FileOutputStream fos = new FileOutputStream("/data/player/" + key + ".ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(value);
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
    }
    
}
