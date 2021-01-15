package common.src.main.host;

import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.util.SpaceManager;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameLogic implements Runnable{

    private Map <String,Space> unameToSpace = new HashMap<>();
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
            loopCom();
            exit();
        } else {
            initWork();
            loopWork();
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
                        unameToSpace.put(uname,SpaceManager.getRemoteSpace(data[0].toString(),data[1].toString(),"localgame"));
                        //TODO load user
                        break;
                    case "work":
                        //req job
                        writeToUser(uname, "jobReq");
                        //get job
                        data = gameSpace.get(new ActualField(uname), new ActualField("job"), new FormalField(String.class));
                        //start job
                        //TODO start job
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

    Space[] workspaces;
    private enum JOBS {
        WOODCUTTING(0), MINING(1), HUNTING(2), FARMING(3), CONSTRUCTION(4);
        private final int value;

        JOBS(int val) {
            this.value = val;
        }

        public int toInt() {
            return value;
        }
    };
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
            workspaces = new Space[] {forest, mine, huntingGrounds, field, constructionSite};
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
        woodDmg += workingOn(JOBS.WOODCUTTING);

        if(woodDmg >= woodHP){

            gameState.addWood(1);
            woodDmg %= woodHP;
        }
        //Mine
        for (int i = 1; i <= workingOn(JOBS.MINING); i++){
            stoneDmg += i;
        }

        if(stoneDmg >= stoneHP){

            gameState.addStone(1);
            stoneDmg %= stoneHP;
        }
        //Hunting Grounds
        int huntersWorking = workingOn(JOBS.HUNTING);
        if(huntersWorking >= 2){
            animalDmg += huntersWorking;
        }

        if(animalDmg >= animalHP){

            gameState.addMeat(1);
            animalDmg %= animalHP;
        }
        //Fields
        int farmersWorking = workingOn(JOBS.FARMING);
        wheatDmg += farmersWorking;

        if(wheatDmg >= wheatHP){
            gameState.addWheat(6*farmersWorking);
            wheatDmg %= wheatHP;
        }
        //Construction Site
        houseDmg += workingOn(JOBS.CONSTRUCTION);

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

    private int workingOn(JOBS job){
        Space workers = workspaces[job.toInt()];
        try {
            return workers.queryAll(new FormalField(String.class)).size();
        } catch (InterruptedException e) {
            System.out.println("No workers could be found");
            e.printStackTrace();
            return 0;
        }
    }


    private int extraLoot(JOBS job) {
        int mult = 1;
        Space workers = workspaces[job.toInt()];
        try {
            Map<String, PlayerState> unameToPlayerState = new HashMap<>();
            List<Object[]> playersWorking = workers.queryAll(new FormalField(String.class));
            for (Object[] o : playersWorking) {
                unameToPlayerState.get(o[0].toString());
            }

        } catch (InterruptedException e) {
            return 1;
        }
        return mult;
    }
}
