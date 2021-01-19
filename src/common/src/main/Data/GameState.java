package common.src.main.Data;

import java.io.Serializable;

public class GameState implements Serializable {

    private int wood;
    private int stone;
    private int meat;
    private int wheat;
    private int houses;

    //Wood stats
    public static final int woodHP = 30;
    private int woodDmg = 0;
    //Stone stats
    public static final int stoneHP = 50;
    private int stoneDmg = 0;
    //Animal stats
    public static final int animalHP = 600;
    private int animalDmg = 0;
    //Field stats
    public static final int wheatHP = 6000;
    private int wheatDmg = 0;
    //Construction site
    public static final int houseHP = 30;
    private int houseDmg = 0;

    public GameState(){
        wood = 0;
        stone = 0;
        meat = 0;
        wheat = 0;
        houses = 0;
    }

    public int getWood() {
        return wood;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }

    public void addWood(int wood){
        this.wood += wood;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }

    public void addStone(int stone){
        this.stone += stone;
    }

    public int getMeat() {
        return meat;
    }

    public void setMeat(int meat) {
        this.meat = meat;
    }

    public void addMeat(int meat){
        this.meat += meat;
    }

    public int getWheat() {
        return wheat;
    }

    public void setWheat(int wheat) {
        this.wheat = wheat;
    }

    public void addWheat(int wheat){
        this.wheat += wheat;
    }

    public int getHouses() {
        return houses;
    }

    public void setHouses(int houses) {
        this.houses = houses;
    }

    public void addHouses(int houses){
        this.houses += houses;
    }

    public int getWoodDmg() {
        return woodDmg;
    }

    public void setWoodDmg(int woodDmg) {
        this.woodDmg = woodDmg;
    }

    public int getStoneDmg() {
        return stoneDmg;
    }

    public void setStoneDmg(int stoneDmg) {
        this.stoneDmg = stoneDmg;
    }

    public int getAnimalDmg() {
        return animalDmg;
    }

    public void setAnimalDmg(int animalDmg) {
        this.animalDmg = animalDmg;
    }

    public int getWheatDmg() {
        return wheatDmg;
    }

    public void setWheatDmg(int wheatDmg) {
        this.wheatDmg = wheatDmg;
    }

    public void setHouseDmg(int houseDmg) {
        this.houseDmg = houseDmg;
    }

    public int getHouseDmg() {
        return houseDmg;
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
    public int getHouseDmgP(){
        return (int)((double)houseDmg*100/houseHP);
    }
}
