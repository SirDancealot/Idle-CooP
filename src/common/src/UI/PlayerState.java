package common.src.UI;

import java.io.Serializable;

public class PlayerState implements Serializable {
    private static PlayerState ourInstance = new PlayerState();

    public static PlayerState getInstance() {
        return ourInstance;
    }

    private int woodcuttingLevel = 0;
    private int miningLevel = 0;
    private int huntingLevel = 0;
    private int farmingLevel = 0;
    private int constructionLevel = 0;

    private PlayerState() {

    }

    public int getWoodcuttingLevel() {
        return woodcuttingLevel;
    }

    public void setWoodcuttingLevel(int woodcuttingLevel) {
        this.woodcuttingLevel = woodcuttingLevel;
    }

    public int getMiningLevel() {
        return miningLevel;
    }

    public void setMiningLevel(int miningLevel) {
        this.miningLevel = miningLevel;
    }

    public int getHunntingLevel() {
        return huntingLevel;
    }

    public void setHunntingLevel(int hunntingLevel) {
        this.huntingLevel = hunntingLevel;
    }

    public int getFarmingLevel() {
        return farmingLevel;
    }

    public void setFarmingLevel(int farmingLevel) {
        this.farmingLevel = farmingLevel;
    }

    public int getConstructionLevel() {
        return constructionLevel;
    }

    public void setConstructionLevel(int constructionLevel) {
        this.constructionLevel = constructionLevel;
    }
}
