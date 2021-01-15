package common.src.main.Data;

import java.io.Serializable;

public class GameState implements Serializable {

    private int wood;
    private int stone;
    private int meat;
    private int wheat;
    private int houses;

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
}
