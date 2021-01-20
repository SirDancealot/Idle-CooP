package common.src.main.host;

import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.main.GameCalculations;
import common.src.util.FileManager;
import common.src.util.PropManager;
import common.src.util.SpaceManager;
import org.jspace.*;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HostLogic implements Runnable{

    private static Vector<String> jobs = new Vector<>();
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

    public HostLogic(Boolean communicating){
        this.communicating = communicating;
    }

    @Override
    public void run() {
        if(communicating){
            initCom();
            loopCom();
        } else {
            initWork();
            loopWork();
            saveAll();
        }
    }

    private void initCom() {
        new Thread(new HostLogic(false)).start();

        try {
            clients = SpaceManager.getLocalSpace("clients");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gameSpace = new SequentialSpace();
        SpaceManager.exposeHostSpace(gameSpace, "game");

        forest = new SequentialSpace();
        mine = new SequentialSpace();
        huntingGrounds = new SequentialSpace();
        field = new SequentialSpace();
        constructionSite = new SequentialSpace();

        SpaceManager.exposeHostSpace(forest,"forest");
        SpaceManager.exposeHostSpace(mine,"mine");
        SpaceManager.exposeHostSpace(huntingGrounds,"huntingGrounds");
        SpaceManager.exposeHostSpace(field,"field");
        SpaceManager.exposeHostSpace(constructionSite,"constructionSite");
    }

    private AtomicBoolean stopCom = new AtomicBoolean(false);
    private void loopCom(){

        Object[] data;
        String uname, action;

        SpaceManager.addHostExitEvent(() -> {
            jobs.add("save:" + PropManager.getProperty("username"));
            stopCom.set(true);
        });

        while(!stopCom.get()){
            try {
                data = gameSpace.get(new FormalField(String.class), new FormalField(String.class));
                uname = data[0].toString();
                action = data[1].toString();
                String finalUname = uname;
                switch (action){
                    case "joined":
                        data = clients.query(new FormalField(String.class), new FormalField(String.class), new ActualField(uname));
                        unameToSpace.put(uname,SpaceManager.getRemoteSpace(data[0].toString(),data[1].toString(),"localGame"));
                        PlayerState ps = loadPlayer(uname);
                        unameToPlayerState.put(uname, ps);
                        unameToSpace.get(uname).put("playerState",unameToPlayerState.get(uname));

                        jobs.add("gameState:" + uname);
                        jobs.add("playerState:" + uname);
                        break;

                        //TODO update working thread with new player

                    case "work":
                        boolean jobCorrect = false;
                        String job = "";
                        while (!jobCorrect) {
                            //req job
                            writeToUser(uname, "jobReq");
                            //get job
                            data = gameSpace.get(new ActualField(uname), new ActualField("job"), new FormalField(String.class));
                            job = data[2].toString();
                            writeToUser(uname, "job", job);
                            data = gameSpace.get(new ActualField(uname), new FormalField(Boolean.class));
                            jobCorrect = (Boolean)data[1];
                        }

                        stopWork(uname);
                        switch (job){
                            case "Woodcutting":
                                forest.put(uname);
                                break;
                            case "Mining":
                                mine.put(uname);
                                break;
                            case "Hunting":
                                huntingGrounds.put(uname);
                                break;
                            case "Farming":
                                field.put(uname);
                                break;
                            case "Construction":
                                constructionSite.put(uname);
                                break;
                        }

                        break;
                    case "disconnect":
                    	jobs.add("save:" + uname);
                        unameToSpace.remove(finalUname);
                    case "stop":
                    	stopWork(uname);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        unameToSpace.forEach((String key, Space value) -> {
            writeToUser(key, "exit");
            try {
                ((RemoteSpace)value).close();
            } catch (IOException ignore) { }
        });
    }

    private void stopWork(String player) {
        try {
            forest.getp(new ActualField(player));
            mine.getp(new ActualField(player));
            huntingGrounds.getp(new ActualField(player));
            field.getp(new ActualField(player));
            constructionSite.getp(new ActualField(player));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private PlayerState loadPlayer (String username) {
        PlayerState ps = FileManager.loadObject("./data/players/" + username + ".ser");
        return (ps != null ? ps : new PlayerState());
    }

    private void savePlayer(String username) {
        File f = new File("./data/players");
        if (!f.exists())
            f.mkdir();
        FileManager.saveObject("./data/players/" + username + ".ser", unameToPlayerState.get(username));
    }

    private void saveAll(){
        unameToPlayerState.forEach((String key, PlayerState value) -> {
            File f = new File("./data/players");
            if (!f.exists())
                f.mkdir();
            FileManager.saveObject("./data/players/" + key + ".ser", value);
        });
        FileManager.saveObject("./data/GameState.ser", gameState);
    }

    private void writeToUser(String uname, String... req){
        try {
            unameToSpace.get(uname).put(req);
        } catch (InterruptedException e) {
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
        gameState = FileManager.loadObject("./data/GameState.ser");
        if (gameState == null)
            gameState = new GameState();

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

    private AtomicBoolean stopWork = new AtomicBoolean(false);
    private void loopWork(){
        SpaceManager.addHostExitEvent(() -> stopWork.set(true));

        GameCalculations gameCalculations =  new GameCalculations(forest,mine,huntingGrounds,field,constructionSite,gameState,unameToPlayerState,false);

        while (!stopWork.get()) {
            if (jobs.size() > 0) {
                doJob(jobs.remove(jobs.size() - 1));
            }
            gameCalculations.update();
        }
    }

    private void doJob(String job) {
        String username = job.split(":")[1];
        if (job.startsWith("save")) {
            savePlayer(job.split(":")[1]);
        } else if (job.startsWith("gameState")) {
            try {
            	Object[] data = clients.queryp(new FormalField(String.class), new FormalField(String.class), new ActualField(username));
            	Space space = SpaceManager.getRemoteSpace(data[0].toString(), data[1].toString(), "localGame");
                space.put("gameState", gameState);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        } else if (job.startsWith("playerState")) {
            unameToPlayerState.put(username, loadPlayer(username));
        }
    }
}
