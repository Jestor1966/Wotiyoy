package yio.tro.antiyoy.gameplay;

import yio.tro.antiyoy.YioGdxGame;
import yio.tro.antiyoy.ai.master.AiData;
import yio.tro.antiyoy.gameplay.data_storage.EncodeableYio;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.factor_yio.FactorYio;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.object_pool.ReusableYio;

import javax.management.ObjectInstance;


public class Hex implements ReusableYio, EncodeableYio{

    public boolean active, selected, changingFraction, flag, inMoveZone, genFlag, ignoreTouch;
    public int index1, index2, moveZoneNumber, genPotential, visualDiversityIndex,visualDiversityIndex2,visualDiversityIndex3;
    public boolean sea;
    public PointYio pos, fieldPos;
    private GameController gameController;
    public FieldManager fieldManager;
    float cos60, sin60;
    public int fraction, previousFraction, objectInside;
    long animStartTime;
    boolean blockToTreeFromExpanding, canContainObjects, blockToRevoltFromExpanding;
    public FactorYio animFactor, selectionFactor;
    public Unit unit;
    public Hex algoLink;
    public int algoValue;
    public AiData aiData;



    public Hex(int index1, int index2, PointYio fieldPos, FieldManager fieldManager) {
        this(index1, index2, fieldPos, fieldManager, false);
    }


    public Hex(int index1, int index2, PointYio fieldPos, FieldManager fieldManager, boolean snapshot) {
        this.index1 = index1;
        this.index2 = index2;
        this.fieldPos = fieldPos;
        this.fieldManager = fieldManager;
        if (fieldManager == null) return;
        if (!snapshot) {
            aiData = new AiData(this);
        }

        gameController = fieldManager.gameController;
        active = false;
        sea = false;
        pos = new PointYio();
        cos60 = (float) Math.cos(Math.PI / 3d);
        sin60 = (float) Math.sin(Math.PI / 3d);
        animFactor = new FactorYio();
        selectionFactor = new FactorYio();
        unit = null;
        visualDiversityIndex = (101 * index1 * index2 + 7 * index2) % 6;
        visualDiversityIndex2 = (1919 * index1 * index2 + 7 * index2) % 3;
        visualDiversityIndex3 = (810 * index1 * index2 + 7 * index2) % 2;
        canContainObjects = true;
        algoLink = null;
        algoValue = 0;
        updatePos();
    }


    @Override
    public void reset() {
        // this is just blank method, don't use it
    }


    void updateCanContainsObjects() {
        canContainObjects = fieldManager.gameController.levelSizeManager.isPointInsideLevelBoundsHorizontally(pos);
    }


    void updatePos() {
        pos.x = fieldPos.x + fieldManager.hexStep2 * index2 * sin60;
        pos.y = fieldPos.y + fieldManager.hexStep1 * index1 + fieldManager.hexStep2 * index2 * cos60;
    }


    boolean isInProvince() { // can cause bugs if province not detected right
        Hex adjHex;
        for (int i = 0; i < 6; i++) {
            adjHex = getAdjacentHex(i);
            if (adjHex.active && adjHex.sameFraction(this)) return true;
        }
        return false;
    }


    public boolean isNearWater() {
        if (!this.active) return false;
        for (int i = 0; i < 6; i++) {
            if (!gameController.fieldManager.adjacentHex(this, i).active || gameController.fieldManager.adjacentHex(this, i).sea) return true;
        }
        return false;
    }

    public boolean isNearSea() {
        if (!this.active) return false;
        if (this.isSea()) return true;
        for (int i = 0; i < 6; i++) {
            if (gameController.fieldManager.adjacentHex(this, i).sea) return true;
        }
        return false;
    }

    public boolean isNearLand() {
        if (!this.active) return false;
        if (!this.sea) return true;
        for (int i = 0; i < 6; i++) {
            if (!gameController.fieldManager.adjacentHex(this, i).sea) return true;
        }
        return false;
    }

    public boolean isSea() {
        return this.sea;
    }

    public void setFraction(int fraction) {
        previousFraction = this.fraction;
        this.fraction = fraction;
        animFactor.appear(1, 1);
        animFactor.setValues(0, 0);
    }


    void move() {
        animFactor.move();
        if (selected) {
            selectionFactor.move();
        }
//        if (unit != null) unit.move();
    }


    void addUnit(int strength) {
        unit = new Unit(gameController, this, strength);
        gameController.unitList.add(unit);
        gameController.matchStatistics.onUnitProduced();
    }


    public boolean isFree() {
        return !containsObject() && !containsUnit();
    }

    public boolean isRevoltCan() {
        return !containsSiege() && !containsTowerCantRevolt() &&!(fraction==7) && containsUnitCanRevolt() && !containsMountain() ;
    }


    public boolean isEmpty() {
        return isFree();
    }


    public boolean nothingBlocksWayForUnit() {
        return !containsUnit() && !containsBuilding();
    }


    public boolean containsTree() {
        return objectInside == Obj.PALM || objectInside == Obj.PINE || objectInside==Obj.REVOLT;
    }

    public boolean containsRevolt(){
        return objectInside == Obj.REVOLT;
    }


    public boolean containsObject() {
        return objectInside > 0;
    }


    public boolean containsTower() {
        return objectInside == Obj.TOWER || objectInside == Obj.STRONG_TOWER || objectInside == Obj.FORT;
    }

    public boolean containsNormalTower() {
        return objectInside == Obj.TOWER || objectInside == Obj.STRONG_TOWER;
    }

    public boolean containsTowerCantRevolt() {
        return objectInside == Obj.STRONG_TOWER;
    }

    public boolean containsSiege() {
        if(GameRules.captainAsCityRules){
            return objectInside == Obj.FORT || objectInside == Obj.CITY || objectInside == Obj.TOWN;
        }
        return objectInside == Obj.FORT || objectInside == Obj.CITY;
    }

    public boolean containsMountain() {
        return objectInside == Obj.HILL ||  objectInside == Obj.MOUNTAIN;
    }


    public boolean containsBuilding() {
        return objectInside == Obj.TOWN
                || objectInside == Obj.TOWER
                || objectInside == Obj.FARM
                || objectInside == Obj.STRONG_TOWER
                || objectInside == Obj.MOUNTAIN
                || objectInside == Obj.HILL
                || objectInside == Obj.FORT
                || objectInside == Obj.CITY;
    }

    public boolean containRevoltCantDestory(){
        return objectInside == Obj.TOWN
                && objectInside == Obj.STRONG_TOWER;
    }


    public Hex getSnapshotCopy() {
        Hex record = new Hex(index1, index2, fieldPos, fieldManager, true);
        record.active = active;
        record.fraction = fraction;
        record.objectInside = objectInside;
        record.selected = selected;
        record.sea = sea;
        if (unit != null) {
            record.unit = unit.getSnapshotCopy();
        }
        return record;
    }


    public void setObjectInside(int objectInside) {
        this.objectInside = objectInside;
        //YioGdxGame.say(""+objectInside);
    }


    public boolean containsUnit() {
        return unit != null;
    }

    public boolean containsUnitCanRevolt() {
        if(!containsUnit()) return true;
        if (unit.strength<=2) return true;
        return false;
    }


    public boolean hasUnit() {
        return containsUnit();
    }


    public int numberOfActiveHexesNearby() {
        return numberOfFriendlyHexesNearby() + howManyEnemyHexesNear();
    }


    public boolean noProvincesNearby() {

        if (numberOfFriendlyHexesNearby() > 0) return false;
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (adjHex.active && adjHex.numberOfFriendlyHexesNearby() > 0) return false;
        }
        return true;
    }


    public int numberOfFriendlyHexesNearby() {
        int c = 0;
        for (int dir = 0; dir < 6; dir++) {
            Hex adjHex = getAdjacentHex(dir);
            if (adjHex == null) continue;
            if (adjHex.isNullHex()) continue;
            if (!adjHex.active) continue;
            if (adjHex.isNeutral()) continue;
            if (!adjHex.sameFraction(this)) continue;
            c++;
        }
        return c;
    }


    public int getDefenseNumber() {
        return getDefenseNumber(null);
    }


    public int getDefenseNumber(Unit ignoreUnit) {
        int defenseNumber = 0;
        if (this.objectInside == Obj.TOWN){
            if(GameRules.captainAsCityRules){
                defenseNumber = 10;
            }else{
                defenseNumber = 1;
            }
        }
        if (this.objectInside == Obj.TOWER) defenseNumber = 2;
        if (this.objectInside == Obj.STRONG_TOWER) defenseNumber = 3;
        if (this.objectInside == Obj.MOUNTAIN) defenseNumber = 10;
        if (this.objectInside == Obj.HILL) defenseNumber = 10;
        if (this.objectInside == Obj.FORT) defenseNumber = 10;
        if (this.objectInside == Obj.CITY) defenseNumber = 10;
        if (this.objectInside == Obj.REVOLT) defenseNumber = 2;

        if (this.containsUnit() && unit != ignoreUnit) {
            if(this.unit.strength==5){
                defenseNumber = Math.max(defenseNumber, 1);
            }else if(this.unit.strength==6){
                defenseNumber = Math.max(defenseNumber, 2);
            }else if(this.unit.strength==7){
                defenseNumber = Math.max(defenseNumber, 1);
            }else if(this.unit.strength==8){
                defenseNumber = Math.max(defenseNumber, 2);
            }else if(this.unit.strength==9){
                defenseNumber = Math.max(defenseNumber, 4);
            }else{
                defenseNumber = Math.max(defenseNumber, this.unit.strength);
            }
        }

        Hex neighbour;
        for (int i = 0; i < 6; i++) {
            neighbour = getAdjacentHex(i);
            if (!(neighbour.active && neighbour.sameFraction(this))) continue;
            if (neighbour.objectInside == Obj.TOWN){
                if(GameRules.captainAsCityRules){
                    defenseNumber = Math.max(defenseNumber, 2);
                }else{
                    defenseNumber = Math.max(defenseNumber, 1);
                }
            }
            if (neighbour.objectInside == Obj.TOWER) defenseNumber = Math.max(defenseNumber, 2);
            if (neighbour.objectInside == Obj.STRONG_TOWER) defenseNumber = Math.max(defenseNumber, 3);
            if (neighbour.objectInside == Obj.MOUNTAIN) defenseNumber = Math.max(defenseNumber, 10);
            if (neighbour.objectInside == Obj.FORT) defenseNumber = Math.max(defenseNumber, 3);
            if (neighbour.objectInside == Obj.CITY) defenseNumber = Math.max(defenseNumber, 2);
            if (neighbour.objectInside == Obj.REVOLT) defenseNumber = Math.max(defenseNumber, 1);

            if (neighbour.containsUnit() && neighbour.unit != ignoreUnit) {
                if(neighbour.unit.strength==5){
                    defenseNumber = Math.max(defenseNumber, 0);
                }else if(neighbour.unit.strength==6){
                    defenseNumber = Math.max(defenseNumber, 1);
                }else if(neighbour.unit.strength==7){
                    defenseNumber = Math.max(defenseNumber, 0);
                }else if(neighbour.unit.strength==8){
                    defenseNumber = Math.max(defenseNumber, 1);
                }else if(neighbour.unit.strength==9){
                    defenseNumber = Math.max(defenseNumber, 2);
                }else{
                    defenseNumber = Math.max(defenseNumber, neighbour.unit.strength);
                }
            }


        }
        return defenseNumber;
    }


    public boolean isNearHouse() {
        Hex adjHex;
        for (int i = 0; i < 6; i++) {
            adjHex = getAdjacentHex(i);
            if (adjHex.active && adjHex.sameFraction(this) && adjHex.objectInside == Obj.CITY) return true;
            if (adjHex.active && adjHex.sameFraction(this) && adjHex.objectInside == Obj.TOWN) return true;
        }
        return false;
    }


    public void forAdjacentHexes(HexActionPerformer hexActionPerformer) {
        Hex adjHex;
        for (int i = 0; i < 6; i++) {
            adjHex = getAdjacentHex(i);
            hexActionPerformer.doAction(this, adjHex);
        }
    }


    public boolean isInPerimeter() {
        Hex adjHex;
        for (int i = 0; i < 6; i++) {
            adjHex = getAdjacentHex(i);
            if (adjHex.active && !adjHex.sameFraction(this) && adjHex.isInProvince()) return true;
        }
        return false;
    }


    public boolean hasThisSupportiveObjectNearby(int objectIndex) {
        if (objectInside == objectIndex) return true;
        for (int dir = 0; dir < 6; dir++) {
            Hex adjacentHex = getAdjacentHex(dir);
            if (adjacentHex == null) continue;
            if (adjacentHex.isNullHex()) continue;
            if (!adjacentHex.active) continue;
            if (adjacentHex.fraction != fraction) continue;
            if (adjacentHex.objectInside != objectIndex) continue;
            return true;
        }
        return false;
    }


    public boolean hasPalmReadyToExpandNearby() {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (!adjHex.blockToTreeFromExpanding && adjHex.objectInside == Obj.PALM) return true;
        }
        return false;
    }


    public boolean hasPineReadyToExpandNearby() {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (!adjHex.blockToTreeFromExpanding && adjHex.objectInside == Obj.PINE) return true;
        }
        return false;
    }

    public boolean hasRevoltReadyToExpandNearby() {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (!adjHex.blockToRevoltFromExpanding && adjHex.objectInside == Obj.REVOLT && !(objectInside==Obj.TOWN)) return true;
        }
        return false;
    }


    public boolean sameFraction(int fraction) {
        return this.fraction == fraction;
    }


    public boolean sameFraction(Province province) {
        return fraction == province.getFraction();
    }


    public boolean sameFraction(Hex hex) {
        return fraction == hex.fraction;
    }


    public int howManyEnemyHexesNear() {
        int c = 0;
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (adjHex.active && !adjHex.sameFraction(this) && !adjHex.isNeutral()) c++;
        }
        return c;
    }


    public void set(Hex hex) {
        index1 = hex.index1;
        index2 = hex.index2;
        sea = hex.sea;
    }


    public boolean equals(Hex hex) {
        return hex.index1 == index1 && hex.index2 == index2;
    }


    public boolean isDefendedByTower() {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (adjHex.active && adjHex.sameFraction(this) && adjHex.containsTower()) return true;
        }
        return false;
    }

    public boolean isDefendedBySiege() {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (adjHex.active && adjHex.sameFraction(this) && adjHex.containsSiege()) return true;
        }
        return false;
    }

    public boolean isDefendedByMountain() {
        if(objectInside==Obj.MOUNTAIN) return true;
        for (int i = 0; i < 6; i++) {
            Hex adjHex = getAdjacentHex(i);
            if (adjHex.active && adjHex.sameFraction(this) && adjHex.objectInside==Obj.MOUNTAIN) return true;
        }
        return false;
    }


    public Hex getAdjacentHex(int direction) {
        return gameController.fieldManager.adjacentHex(this, direction);
    }


    public boolean isAdjacentTo(Hex hex) {
        for (int dir = 0; dir < 6; dir++) {
            Hex adjacentHex = getAdjacentHex(dir);
            if (adjacentHex == null) continue;
            if (adjacentHex.isNullHex()) continue;
            if (adjacentHex != hex) continue;
            return true;
        }
        return false;
    }


    public void setIgnoreTouch(boolean ignoreTouch) {
        this.ignoreTouch = ignoreTouch;
    }


    public boolean isNullHex() {
        return index1 == -1 && index2 == -1;
    }


    void select() {
        if (!selected) {
            selected = true;
            selectionFactor.setValues(0, 0);
            selectionFactor.appear(3, 1.5);
        }
    }


    public boolean isSelected() {
        return selected;
    }


    public PointYio getPos() {
        return pos;
    }


    public boolean isNeutral() {
        return fraction == GameRules.NEUTRAL_FRACTION;
    }


    public boolean canBeAttackedBy(Unit unit) {
        if (unit == null) return false; // normally this shouldn't happen, but it happened once in replay

        boolean canUnitAttackHex = gameController.canUnitAttackHex(unit.strength, unit.getFraction(), this);

        if (GameRules.replayMode) {
            if (!canUnitAttackHex) {
                System.out.println("Problem in Hex.canBeAttackedBy(): " + this);
            }
            return true;
        }

        return canUnitAttackHex;
    }


    public boolean isInMoveZone() {
        return inMoveZone;
    }


    void close() {
        gameController = null;
    }


    @Override
    public String toString() {
        if (!active) {
            return "[Hex (not active): f" + fraction + " (" + index1 + ", " + index2 + ")]";
        }
        return "[Hex: f" + fraction + " (" + index1 + ", " + index2 + ")]";
    }


    @Override
    public String encode() {
        return index1 + " " + index2 + " " + fraction + " " + objectInside + " "+ (sea ? 1 : 0);
    }


    @Override
    public void decode(String source) {
        String[] split = source.split(" ");

        int obj = Integer.valueOf(split[3]);
        if (obj > 0) {
            fieldManager.addSolidObject(this, obj);
        }
        if(split.length>4){
            this.sea=Integer.valueOf(split[4])!=0;
        }else{
            this.sea=false;
        }
    }
}
