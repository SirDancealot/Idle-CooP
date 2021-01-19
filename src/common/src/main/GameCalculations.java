package common.src.main;

import common.src.UI.GameGUI;
import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.util.PropManager;
import org.jspace.FormalField;
import org.jspace.Space;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameCalculations {

    private final Space forest;
    private final Space mine;
    private final Space huntingGrounds;
    private final Space field;
    private final Space constructionSite;
    private final GameState gameState;
    private final Map<String, PlayerState> unameToPlayerState;
    private boolean updateGUI;
    Space[] workspaces;

    long ticks = 0;
    long startTime = System.nanoTime();
    final double nsPerSec = 1000000000;
    final double TPS = 10;
    long lastNs = System.nanoTime();
    double dt = 0.0;
    int missingTicks;
    double nsPerTick = nsPerSec/TPS;

    public GameCalculations(Space forest, Space mine, Space huntingGrounds, Space field, Space constructionSite, GameState gameState, Map <String, PlayerState> unameToPlayerState, boolean updateGUI){
        this.forest = forest;
        this.mine = mine;
        this.huntingGrounds = huntingGrounds;
        this.field = field;
        this.constructionSite = constructionSite;
        this.gameState = gameState;
        this.unameToPlayerState = unameToPlayerState;
        this.updateGUI = updateGUI;
        workspaces =  new Space[] {forest, mine, huntingGrounds, field, constructionSite};
    }

    boolean updateGuiRequired = true;
    int lastWood = 0, nowWood = 0;
    int lastStone = 0, nowStone = 0;
    int lastAnimal = 0, nowAnimal = 0;
    int lastWheat = 0, nowWheat = 0;
    int lastHouse = 0, nowHouse = 0;
    public void update(){

        long nowNs = System.nanoTime();
        long deltaNs = nowNs - lastNs;
        lastNs = nowNs;

        dt += deltaNs / nsPerTick;
        missingTicks = (int)dt;

        if (missingTicks > 0) {
            long tickStart = System.nanoTime();
            ticks++;
            tick();
            if (updateGUI) {
                SwingUtilities.invokeLater(GameGUI.getInstance().new setProgress(
                        gameState.getWoodDmgP(),
                        gameState.getStoneDmgP(),
                        gameState.getAnimalDmgP(),
                        gameState.getWheatDmgP(),
                        gameState.getHouseDmgP(),
                        "setHP"));
                PlayerState ps = unameToPlayerState.get(PropManager.getProperty("username"));
                SwingUtilities.invokeLater(GameGUI.getInstance().new setProgress(
                        ps.getWoodcuttingProgress(),
                        ps.getMiningProgress(),
                        ps.getHuntingProgress(),
                        ps.getFarmingProgress(),
                        ps.getConstructionProgress(),
                        "setXP"
                ));

                if (updateGuiRequired) {
                    SwingUtilities.invokeLater(GameGUI.getInstance().new setProgress(
                            gameState.getWood(),
                            gameState.getStone(),
                            gameState.getMeat(),
                            gameState.getWheat(),
                            gameState.getHouses(),
                            "setRes"
                    ));
                }

                SwingUtilities.invokeLater(GameGUI.getInstance().new setProgress(
                        ps.getWoodcuttingLevel(),
                        ps.getMiningLevel(),
                        ps.getHunntingLevel(),
                        ps.getFarmingLevel(),
                        ps.getConstructionLevel(),
                        "setLvl"
                ));

                nowWood = workingOn(JOBS.WOODCUTTING);
                nowStone = workingOn(JOBS.MINING);
                nowAnimal = workingOn(JOBS.HUNTING);
                nowWheat = workingOn(JOBS.FARMING);
                nowHouse = workingOn(JOBS.CONSTRUCTION);
                if (lastWood != nowWood ||
                    lastStone != nowStone ||
                    lastAnimal != nowAnimal ||
                    lastWheat != nowWheat ||
                    lastHouse != nowHouse) {

                    List<String>[] workerList = new List[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
                    for (int i = 0; i < 5; i++) {
                        List<Object[]> data = workers[i];
                        for (Object[] o : data) {
                            workerList[i].add(o[0].toString());
                        }
                    }

                    SwingUtilities.invokeLater(GameGUI.getInstance().new setWorkers(workerList));
                }

                lastWood = nowWood;
                lastStone = nowStone;
                lastAnimal = nowAnimal;
                lastWheat = nowWheat;
                lastHouse = nowHouse;
            }
            long tickEnd = System.nanoTime();
            dt -= 1.0;
            String debug = PropManager.getProperty("debug");
            if (debug != null) {
                if (debug.equals("true")) {
                    System.out.println("Tick: " + ticks);
                    System.out.println("TPS: " + (ticks/((nowNs-startTime)/nsPerSec)));
                    System.out.println("Tick-time: " + ((tickEnd - tickStart)/nsPerSec));
                }
            }
        }
    }

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

    List<Object[]>[] workers;
    private void tick(){
        try {
            workers = new List[]{
            		forest.queryAll(new FormalField(String.class)),
                    mine.queryAll(new FormalField(String.class)),
                    huntingGrounds.queryAll(new FormalField(String.class)),
                    field.queryAll(new FormalField(String.class)),
                    constructionSite.queryAll(new FormalField(String.class))
            };
            for (int i = 0; i < 5; i++) {
                if (workers[i] == null)
                    workers[i] = new ArrayList<>();
            }
        } catch (InterruptedException e) {
        	workers = new List[]{new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList()};
        }
        //Forest
        int woodDmg = gameState.getWoodDmg();
        int woodHP = GameState.woodHP;
        woodDmg += workingOn(GameCalculations.JOBS.WOODCUTTING);
        if(woodDmg >= woodHP){
            gameState.addWood(1);
            addExp(GameCalculations.JOBS.WOODCUTTING);
            woodDmg %= woodHP;
            updateGuiRequired = true;
        }
        gameState.setWoodDmg(woodDmg);

        //Mine
	    int stoneDmg = gameState.getStoneDmg();
	    int stoneHP = GameState.stoneHP;
        for (int i = 1; i <= workingOn(GameCalculations.JOBS.MINING); i++){
            stoneDmg += i;
        }
        if(stoneDmg >= stoneHP){
            gameState.addStone(1);
            addExp(GameCalculations.JOBS.MINING);
            stoneDmg %= stoneHP;
            updateGuiRequired = true;
        }
        gameState.setStoneDmg(stoneDmg);

        //Hunting Grounds
        int animalDmg = gameState.getAnimalDmg();
        int animalHP = GameState.animalHP;
        int huntersWorking = workingOn(GameCalculations.JOBS.HUNTING);
        if(huntersWorking >= 2){
            animalDmg += huntersWorking;
        }
        if(animalDmg >= animalHP){
            gameState.addMeat(1);
            addExp(GameCalculations.JOBS.HUNTING);
            animalDmg %= animalHP;
            updateGuiRequired = true;
        }
        gameState.setAnimalDmg(animalDmg);

        //Fields
        int wheatDmg = gameState.getWheatDmg();
        int wheatHP = GameState.wheatHP;
        int farmersWorking = workingOn(GameCalculations.JOBS.FARMING);
        wheatDmg += farmersWorking;

        if(wheatDmg >= wheatHP){
            gameState.addWheat(6*farmersWorking);
            addExp(GameCalculations.JOBS.FARMING);
            wheatDmg %= wheatHP;
            updateGuiRequired = true;
        }
        gameState.setWheatDmg(wheatDmg);

        //Construction Site
        int houseDmg = gameState.getHouseDmg();
        int houseHP = GameState.houseHP;
        houseDmg += workingOn(GameCalculations.JOBS.CONSTRUCTION);
        if(houseDmg >= houseHP){
            if(gameState.getWood() > 0 && gameState.getStone() > 0 && (gameState.getMeat() > 0 || gameState.getWheat() > 0)){
                gameState.addHouses(1);
                gameState.addWood(-1);
                gameState.addStone(-1);
                if(gameState.getWheat() > 0)
                    gameState.addWheat(-1);
                else
                    gameState.addMeat(-1);

                addExp(GameCalculations.JOBS.CONSTRUCTION);
                houseDmg %= houseHP;
                updateGuiRequired = true;
            } else
                houseDmg = houseHP - 1;
        }
        gameState.setHouseDmg(houseDmg);
    }

    private int workingOn(JOBS job){
        List<Object[]> workerList = workers[job.toInt()];
        return workerList.size();
    }

    private int extraLoot(JOBS job) {
        int mult = 1;
        List<Object[]> playersWorking = workers[job.toInt()];
        for (Object[] o : playersWorking) {
            //TODO fix extra loot for client-side
            PlayerState ps = unameToPlayerState.get(o[0].toString());
            if (ps == null)
                continue;
            int level = 0;
            double random = Math.random() * 100;
            switch (job) {
                case WOODCUTTING:
                    level = ps.getWoodcuttingLevel();
                    break;
                case MINING:
                    level = ps.getMiningLevel();
                    break;
                case HUNTING:
                    level = ps.getHunntingLevel();
                    break;
                case FARMING:
                    level = ps.getFarmingLevel();
                    break;
                case CONSTRUCTION:
                    level = ps.getConstructionLevel();
                    break;
            }
            if (random < level)
                mult *= 2;
        }
        return mult;
    }

    private void addExp(JOBS job) {
        List<Object[]> data = workers[job.toInt()];
        for (Object[] o : data) {
            PlayerState ps = unameToPlayerState.get(o[0].toString());
            if (ps == null)
                continue;
            switch (job) {
                case WOODCUTTING:
                    ps.addWoodcuttingExp(1);
                    break;
                case MINING:
                    ps.addMiningExp(1);
                    break;
                case HUNTING:
                    ps.addHuntingExp(1);
                    break;
                case FARMING:
                    ps.addFarmingExp(1);
                    break;
                case CONSTRUCTION:
                    ps.addConstructionExp(1);
                    break;
            }
        }
    }
}
