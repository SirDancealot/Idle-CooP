package common.src.main.Data;

import java.io.Serializable;

public class PlayerState implements Serializable {

    private int woodcuttingExp = 0;
    private int miningExp = 0;
    private int huntingExp = 0;
    private int farmingExp = 0;
    private int constructionExp = 0;

    public PlayerState() {

    }

    public int getWoodcuttingExp() {
        return woodcuttingExp;
    }

    public void setWoodcuttingExp(int woodcuttingExp) {
        this.woodcuttingExp = woodcuttingExp;
    }

    public void addWoodcuttingExp(int woodcuttingExp) {
        this.woodcuttingExp += woodcuttingExp;
    }

    public int getMiningExp() {
        return miningExp;
    }

    public void setMiningExp(int miningExp) {
        this.miningExp = miningExp;
    }

    public void addMiningExp(int miningExp) {
        this.miningExp += miningExp;
    }

    public int getHuntingExp() {
        return huntingExp;
    }

    public void setHuntingExp(int huntingExp) {
        this.huntingExp = huntingExp;
    }

    public void addHuntingExp(int huntingExp) {
        this.huntingExp += huntingExp;
    }

    public int getFarmingExp() {
        return farmingExp;
    }

    public void setFarmingExp(int farmingExp) {
        this.farmingExp = farmingExp;
    }

    public void addFarmingExp(int farmingExp) {
        this.farmingExp += farmingExp;
    }

    public int getConstructionExp() {
        return constructionExp;
    }

    public void setConstructionExp(int constructionExp) {
        this.constructionExp = constructionExp;
    }

    public void addConstructionExp(int constructionExp) {
        this.constructionExp += constructionExp;
    }

    public int getWoodcuttingLevel() {
        return xpToLevel(woodcuttingExp);
    }

    public int getMiningLevel() {
        return xpToLevel(miningExp);
    }

    public int getHunntingLevel() {
        return xpToLevel(huntingExp);
    }

    public int getFarmingLevel() {
        return xpToLevel(farmingExp);
    }

    public int getConstructionLevel() {
        return xpToLevel(constructionExp);
    }

    private static int xpToLevel(int xp){
        return Math.min((int) Math.floor(Math.sqrt(xp)), 100);
    }
}
