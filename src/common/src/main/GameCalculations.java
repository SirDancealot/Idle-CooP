package common.src.main;

import common.src.UI.GameGUI;
import common.src.main.Data.GameState;
import common.src.main.Data.PlayerState;
import common.src.util.PropManager;
import org.jspace.FormalField;
import org.jspace.Space;

import javax.swing.*;
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
    double dt = 1.0;
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

    public void update(){

        long nowNs = System.nanoTime();
        long deltaNs = nowNs - lastNs;
        lastNs = nowNs;

        dt += deltaNs / nsPerTick;
        missingTicks = (int)dt;

        if (missingTicks > 0) {
            ticks++;
            tick();

            if (updateGUI)
                SwingUtilities.invokeLater(GameGUI.getInstance().new setProgress(getWoodDmgP(),getStoneDmgP(),getAnimalDmgP(),getWheatDmgP(),getHouseDmg(),true));

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

        //Forest
        woodDmg += workingOn(GameCalculations.JOBS.WOODCUTTING);
        if(woodDmg >= woodHP){
            gameState.addWood(extraLoot(GameCalculations.JOBS.WOODCUTTING));
            addExp(GameCalculations.JOBS.WOODCUTTING);
            woodDmg %= woodHP;
        }

        //Mine
        for (int i = 1; i <= workingOn(GameCalculations.JOBS.MINING); i++){
            stoneDmg += i;
        }
        if(stoneDmg >= stoneHP){
            gameState.addStone(extraLoot(GameCalculations.JOBS.MINING));
            addExp(GameCalculations.JOBS.MINING);
            stoneDmg %= stoneHP;
        }

        //Hunting Grounds
        int huntersWorking = workingOn(GameCalculations.JOBS.HUNTING);
        if(huntersWorking >= 2){
            animalDmg += huntersWorking;
        }
        if(animalDmg >= animalHP){
            gameState.addMeat(extraLoot(GameCalculations.JOBS.HUNTING));
            addExp(GameCalculations.JOBS.HUNTING);
            animalDmg %= animalHP;
        }

        //Fields
        int farmersWorking = workingOn(GameCalculations.JOBS.FARMING);
        wheatDmg += farmersWorking;

        if(wheatDmg >= wheatHP){
            gameState.addWheat(6*farmersWorking*extraLoot(GameCalculations.JOBS.FARMING));
            addExp(GameCalculations.JOBS.FARMING);
            wheatDmg %= wheatHP;
        }

        //Construction Site
        houseDmg += workingOn(GameCalculations.JOBS.CONSTRUCTION);
        if(houseDmg >= houseHP){
            if(gameState.getWood() > 0 && gameState.getStone() > 0 && (gameState.getMeat() > 0 || gameState.getWheat() > 0)){
                gameState.addHouses(extraLoot(GameCalculations.JOBS.CONSTRUCTION));
                gameState.addWood(-1);
                gameState.addStone(-1);
                if(gameState.getWheat() > 0)
                    gameState.addWheat(-1);
                else
                    gameState.addMeat(-1);

                addExp(GameCalculations.JOBS.CONSTRUCTION);
                animalDmg %= animalHP;
            }
        }
    }

    private int workingOn(JOBS job){
        Space workers = workspaces[job.toInt()];
        try {
            List<Object[]> workerList = workers.queryAll(new FormalField(String.class));
            return workerList.size();
        } catch (Exception e) {
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
        } catch (InterruptedException ignore) { }
        return mult;
    }

    private void addExp(JOBS job) {
        Space workSpace = workspaces[job.toInt()];
        try {
            List<Object[]> workers = workSpace.queryAll(new FormalField(String.class));
            for (Object[] o : workers) {
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getWoodDmgP(){
        return (int)((double)woodDmg*100/woodHP);
    }
    public int getStoneDmgP(){
        return (int)((double)stoneDmg*100/stoneHP);
    }
    public int getAnimalDmgP(){
        return (int)((double)animalDmg*100/animalHP);
    }
    public int getWheatDmgP(){
        return (int)((double)wheatDmg*100/wheatHP);
    }
    public int getHouseDmg(){
        return (int)((double)houseDmg*100/houseHP);
    }
}
