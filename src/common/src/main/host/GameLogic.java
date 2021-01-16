package common.src.main.host;

import common.src.UI.GameGUI;
import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.util.FileManager;
import common.src.util.PropManager;
import common.src.util.SpaceManager;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class GameLogic implements Runnable{

    private static Vector<Runnable> jobs = new Vector<>();
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
    }

    @Override
    public void run() {
        if(communicating){
            initCom();
            //loadAllPlayers();
            loopCom();
        } else {
            initWork();
            //loadAllPlayers();
            loopWork();
            saveAllPlayers();
            exit();
        }
    }

    private void initCom() {
        new Thread(new GameLogic(false)).start();

        gameSpace = new SequentialSpace();
        SpaceManager.exposeHostSpace(gameSpace, "game");

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
                String finalUname = uname;
                switch (action){
                    case "joined":
                        data = clients.query(new FormalField(String.class), new FormalField(String.class), new ActualField(uname));
                        unameToSpace.put(uname,SpaceManager.getRemoteSpace(data[0].toString(),data[1].toString(),"localGame"));
                        unameToPlayerState.put(uname, loadPlayer(uname));
                        unameToSpace.get(uname).put("playerState",unameToPlayerState.get(uname));
                        jobs.add(() -> {
                            unameToPlayerState.put(finalUname, loadPlayer(finalUname));
                        });
                        break;

                        //TODO update working thread with new player

                    case "work":
                        //req job
                        writeToUser(uname, "jobReq");
                        //get job
                        data = gameSpace.get(new ActualField(uname), new ActualField("job"), new FormalField(String.class));

                        switch (data[2].toString()){
                            case "woodCutting":
                                forest.put(uname);
                                break;
                            case "mining":
                                mine.put(uname);
                                break;
                            case "hunting":
                                huntingGrounds.put(uname);
                                break;
                            case "farm":
                                field.put(uname);
                                break;
                            case "construction":
                                constructionSite.put(uname);
                                break;
                        }

                        break;
                    case "disconnect":
                        jobs.add(() -> {
                            savePlayer(finalUname);
                        });
                        unameToSpace.remove(finalUname);
                    case "stop":
                        forest.getp(new ActualField(uname));
                        mine.getp(new ActualField(uname));
                        huntingGrounds.getp(new ActualField(uname));
                        field.getp(new ActualField(uname));
                        constructionSite.getp(new ActualField(uname));
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private PlayerState loadPlayer (String username) {
        PlayerState ps = FileManager.loadObject("./data/players/" + username + ".ser");
        return (ps != null ? ps : new PlayerState());
    }

    private void loadAllPlayers(){
        File dir = new File("./data/players");
        if (!dir.exists())
            dir.mkdir();
        for (File file : dir.listFiles()) {
            PlayerState ps = FileManager.loadObject("./data/players" + file.getName());
            unameToPlayerState.put(file.getName().split(".")[0],ps);
        }
    }

    private void savePlayer(String username) {
        FileManager.saveObject("./data/player" + username + ".ser", unameToPlayerState.get(username));
    }

    private void saveAllPlayers(){
        unameToPlayerState.forEach((String key, PlayerState value) -> {
            FileManager.saveObject("./data/player/" + key + ".ser", value);
        });
    }

    private void writeToUser(String uname, String req){
        try {
            unameToSpace.get(uname).put(req);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initWork() {
        System.out.println("init work");

        try {
            clients = SpaceManager.getLocalSpace("clients");
        } catch (IOException e) {
            System.out.println("Client space could not be found.");
            e.printStackTrace();
        }

        try {
            File gameStateFile = new File("./data/GameState.ser");
            if (gameStateFile.exists()) {
                FileInputStream fis = new FileInputStream("./data/GameState.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                gameState = (GameState) ois.readObject();
                ois.close();
                fis.close();
            } else
                gameState = new GameState();
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

    private boolean stopWork = false;
    private void loopWork(){
        SpaceManager.addHostExitEvent(() -> stopWork = true);

        long ticks = 0;
        long startTime = System.nanoTime();
        final double nsPerSec = 1000000000;
        final double TPS = 10;
        long lastNs = System.nanoTime();
        double dt = 1.0;
        int missingTicks;
        double nsPerTick = nsPerSec/TPS;

        while(!stopWork){
            long nowNs = System.nanoTime();
            long deltaNs = nowNs - lastNs;
            lastNs = nowNs;

            dt += deltaNs / nsPerTick;
            missingTicks = (int)dt;

            if (missingTicks > 0) {
                ticks++;
                tick();
                dt -= 1.0;
                String debug = PropManager.getProperty("debug");
                if (debug != null) {
                    if (debug.equals("true")) {
                        System.out.println("Tick: " + ticks);
                        System.out.println("TPS: " + (ticks/((nowNs-startTime)/nsPerSec)));
                    }
                }
            }
        }
    }

    private void exit(){
        try {
            FileOutputStream fos = new FileOutputStream("./data/GameState.ser");
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

    	if (jobs.size() > 0) {
    	    jobs.remove(jobs.size() - 1).run();
        }

        //Forest
        woodDmg += workingOn(JOBS.WOODCUTTING);
        if(woodDmg >= woodHP){
            gameState.addWood(extraLoot(JOBS.WOODCUTTING));
            addExp(JOBS.WOODCUTTING);
            woodDmg %= woodHP;
        }

        //Mine
        for (int i = 1; i <= workingOn(JOBS.MINING); i++){
            stoneDmg += i;
        }
        if(stoneDmg >= stoneHP){
            gameState.addStone(extraLoot(JOBS.MINING));
            addExp(JOBS.MINING);
            stoneDmg %= stoneHP;
        }

        //Hunting Grounds
        int huntersWorking = workingOn(JOBS.HUNTING);
        if(huntersWorking >= 2){
            animalDmg += huntersWorking;
        }
        if(animalDmg >= animalHP){
            gameState.addMeat(extraLoot(JOBS.HUNTING));
            addExp(JOBS.HUNTING);
            animalDmg %= animalHP;
        }

        //Fields
        int farmersWorking = workingOn(JOBS.FARMING);
        wheatDmg += farmersWorking;

        if(wheatDmg >= wheatHP){
            gameState.addWheat(6*farmersWorking*extraLoot(JOBS.FARMING));
            addExp(JOBS.FARMING);
            wheatDmg %= wheatHP;
        }

        //Construction Site
        houseDmg += workingOn(JOBS.CONSTRUCTION);
        if(houseDmg >= houseHP){
            if(gameState.getWood() > 0 && gameState.getStone() > 0 && (gameState.getMeat() > 0 || gameState.getWheat() > 0)){
                gameState.addHouses(extraLoot(JOBS.CONSTRUCTION));
                gameState.addWood(-1);
                gameState.addStone(-1);
                if(gameState.getWheat() > 0)
                    gameState.addWheat(-1);
                else
                    gameState.addMeat(-1);

                addExp(JOBS.CONSTRUCTION);
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
            List<Object[]> playersWorking = workers.queryAll(new FormalField(String.class));
            for (Object[] o : playersWorking) {
                int level = 0;
                double random = Math.random() * 100;
                switch (job) {
                    case WOODCUTTING:
                        level = unameToPlayerState.get(o[0].toString()).getWoodcuttingLevel();
                        break;
                    case MINING:
                        level = unameToPlayerState.get(o[0].toString()).getMiningLevel();
                        break;
                    case HUNTING:
                        level = unameToPlayerState.get(o[0].toString()).getHunntingLevel();
                        break;
                    case FARMING:
                        level = unameToPlayerState.get(o[0].toString()).getFarmingLevel();
                        break;
                    case CONSTRUCTION:
                        level = unameToPlayerState.get(o[0].toString()).getConstructionLevel();
                        break;
                }
                if (random < level)
                    mult *= 2;
            }
        } catch (InterruptedException ignore) { }
        return mult;
    }

    private void addExp(JOBS job) {
        Space workSpace = workspaces[job.toInt()];
        try {
            List<Object[]> workers = workSpace.queryAll(new FormalField(String.class));
            for (Object[] o : workers) {
                switch (job) {
                    case WOODCUTTING:
                        unameToPlayerState.get(o[0].toString()).addWoodcuttingExp(1);
                        break;
                    case MINING:
                        unameToPlayerState.get(o[0].toString()).addMiningExp(1);
                        break;
                    case HUNTING:
                        unameToPlayerState.get(o[0].toString()).addHuntingExp(1);
                        break;
                    case FARMING:
                        unameToPlayerState.get(o[0].toString()).addFarmingExp(1);
                        break;
                    case CONSTRUCTION:
                        unameToPlayerState.get(o[0].toString()).addConstructionExp(1);
                        break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
