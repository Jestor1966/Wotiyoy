package yio.tro.antiyoy.ai.aiRepair;

import yio.tro.antiyoy.ai.AbstractAi;
import yio.tro.antiyoy.gameplay.*;
import yio.tro.antiyoy.gameplay.rules.GameRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public abstract class AiNewRules extends AbstractAi {

    final Random random;
    protected ArrayList<Province> nearbyProvinces;

    protected ArrayList<Unit> unitsList;
    protected ArrayList<Hex> farmsList;
    protected ArrayList<Hex> cityList;
    protected ArrayList<Hex> couldBuildList;

    protected ArrayList<Province> provinces;


    private ArrayList<Hex> tempResultList;
    private ArrayList<Hex> junkList;

    private float[] provinceAiPara;
    int numberOfUnitsBuiltThisTurn;


    public AiNewRules(GameController gameController, int fraction) {
        super(gameController, fraction);
        random = gameController.random;
        provinces = new ArrayList<>();
        unitsList = new ArrayList<>();
        tempResultList = new ArrayList<>();
        junkList = new ArrayList<>();

        provinceAiPara = new float[3];//Ai Parameters in province
    }

    void turnStart(){
        updateProvinces();
        refreshDatas();
        for(Province province:provinces){
            updateDatas(province);
        }
    }

    void moveUnits(){
        for(Unit unit:unitsList){
            if(unit.isReadyToMove()){
                ArrayList<Hex> moveZone = gameController.detectMoveZone(unit.currentHex, unit.strength, GameRules.UNIT_MOVE_LIMIT);
                excludeFriendlyBuildingsFromMoveZone(moveZone);
                excludeFriendlyUnitsFromMoveZone(moveZone);
                if (moveZone.size() == 0) continue;
                Province provinceByHex = gameController.getProvinceByHex(unit.currentHex);
                if (provinceByHex == null) continue;
                unitDecide(unit);
            }
        }
    }

    boolean unitDecide(Unit unit){
        return false;
    }

    private void excludeFriendlyBuildingsFromMoveZone(ArrayList<Hex> moveZone) {
        junkList.clear();
        for (Hex hex : moveZone) {
            if (hex.sameFraction(fraction)) {
                if (hex.containsBuilding()) junkList.add(hex);
            }
        }
        moveZone.removeAll(junkList);
    }


    private void excludeFriendlyUnitsFromMoveZone(ArrayList<Hex> moveZone) {
        junkList.clear();
        for (Hex hex : moveZone) {
            if (hex.sameFraction(fraction)) {
                if (hex.containsUnit()) junkList.add(hex);
            }
        }
        moveZone.removeAll(junkList);
    }

    void refreshDatas(){
        numberOfUnitsBuiltThisTurn=0;
    }

    //update ai datas
    void updateDatas(Province province) {
        unitsList.clear();
        if (province.getFraction() == fraction) {
            provinceAiPara=province.aiPara;//ai parameters
            farmsList=province.getFarms();//farms
            for(Hex hex: province.hexList){
                unitsList.add(hex.unit);
                updateCouldBuild(hex);
            }
        }
    }

    void updateCouldBuild(Hex hex){
        if(hex.isFree()){
            couldBuildList.add(hex);
        }
    }

    void updateProvinces(){
        for (Province province : gameController.fieldManager.provinces) {
            if (province.getFraction() == fraction) {
                provinces.add(province);
            }
        }
    }

    protected void updateNearbyProvinces(Province srcProvince) {
        nearbyProvinces.clear();

        for (Hex hex : srcProvince.hexList) {
            for (int i = 0; i < 6; i++) {
                Hex adjacentHex = hex.getAdjacentHex(i);
                checkToAddNearbyProvince(hex, adjacentHex);
            }
        }
    }

    private void checkToAddNearbyProvince(Hex srcHex, Hex adjacentHex) {
        if (!adjacentHex.active) return;
        if (adjacentHex.isNeutral()) return;
        if (adjacentHex.sameFraction(srcHex)) return;

        Province provinceByHex = gameController.fieldManager.getProvinceByHex(adjacentHex);
        addProvinceToNearbyProvines(provinceByHex);
    }


    private void addProvinceToNearbyProvines(Province province) {
        if (province == null) return;
        if (nearbyProvinces.contains(province)) return;

        nearbyProvinces.listIterator().add(province);
    }

    public int getFraction() {
        return fraction;
    }


    public void setFraction(int fraction) {
        this.fraction = fraction;
    }


    @Override
    public String toString() {
        return "[AI: " + fraction + "]";
    }
}

