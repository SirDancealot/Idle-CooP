package common.src.main.host;


import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.main.GameCalculations;
import common.src.util.FileManager;
import common.src.util.SpaceManager;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import java.io.*;
import java.util.*;

public class HostLogic implements Runnable{

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

    public HostLogic(Boolean communicating){
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
        new Thread(new HostLogic(false)).start();

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean stopWork = false;
    private void loopWork(){
        SpaceManager.addHostExitEvent(() -> stopWork = true);

        GameCalculations gameCalculations =  new GameCalculations(forest,mine,huntingGrounds,field,constructionSite,gameState,unameToPlayerState);
        while(!stopWork){

            if (jobs.size() > 0) {
                jobs.remove(jobs.size() - 1).run();
            }

            gameCalculations.update();
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
}
