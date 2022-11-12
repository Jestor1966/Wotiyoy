package yio.tro.antiyoy.gameplay.rules;

import yio.tro.antiyoy.gameplay.*;

import static yio.tro.antiyoy.gameplay.rules.GameRules.*;

public class RulesetGeneric extends Ruleset{


    public RulesetGeneric(GameController gameController) {
        super(gameController);
    }


    @Override
    public boolean canSpawnPineOnHex(Hex hex) {
        return hex.isFree() && howManyTreesNearby(hex) >= 2 && hex.hasPineReadyToExpandNearby() && gameController.getRandom().nextDouble() < 0.2;
    }


    @Override
    public boolean canSpawnPalmOnHex(Hex hex) {
        return hex.isFree() && hex.isNearWater() && hex.hasPalmReadyToExpandNearby() && gameController.getRandom().nextDouble() < 0.5;
    }

    @Override
    public boolean canSpawnRevoltOnHex(Hex hex) {
        return hex.isRevoltCan() && hex.hasRevoltReadyToExpandNearby() && gameController.getRandom().nextDouble() < 0.3;
    }


    @Override
    public void onUnitAdd(Hex hex) {
        if (!hex.containsTree()) {
            return;
        }

        Province provinceByHex = gameController.fieldManager.getProvinceByHex(hex);
        if (provinceByHex != null) {
            provinceByHex.money += GameRules.CLEAR_REVOLT_REWARD;
            provinceByHex.money += GameRules.TREE_CUT_REWARD;

        }
    }


    @Override
    public void onTurnEnd() {

    }


    @Override
    public boolean canMergeUnits(Unit unit1, Unit unit2) {
        int mergedUnitStrength = gameController.mergedUnitStrength(unit1, unit2);

        return mergedUnitStrength <= 4;
    }


    @Override
    public int getHexIncome(Hex hex) {
        if (hex.containsTree()) {
            return 0;
        }

        if (hex.containsRevolt()) {
            return -1;
        }

        if (hex.objectInside == Obj.FARM) {
            return GameRules.FARM_INCOME + 1;
        }

        if (hex.objectInside == Obj.CITY) {
            return GameRules.CITY_INCOME + 1;
        }

        if (captainAsCityRules) {
            if(hex.objectInside==Obj.TOWN){
                return GameRules.CITY_INCOME + 1;
            }
        }

        return 1;
    }


    @Override
    public int getHexTax(Hex hex) {
        if (hex.containsUnit()) {
            return getUnitTax(hex.unit.strength);
        }

        if (hex.objectInside == Obj.TOWER) return GameRules.TAX_TOWER;
        if (hex.objectInside == Obj.STRONG_TOWER) return GameRules.TAX_STRONG_TOWER;
        if (hex.objectInside == Obj.MOUNTAIN) return GameRules.TAX_MOUNTAIN;
        if (hex.objectInside == Obj.FORT) return GameRules.TAX_FORT;

        return 0;
    }


    @Override
    public int getUnitTax(int strength) {
        switch (strength) {
            default:
            case 1:
                return TAX_UNIT_GENERIC_1;
            case 2:
                return TAX_UNIT_GENERIC_2;
            case 3:
                return TAX_UNIT_GENERIC_3;
            case 4:
                return TAX_UNIT_GENERIC_4;
            case 5:
                return TAX_UNIT_GENERIC_5;
            case 6:
                return TAX_UNIT_GENERIC_6;
            case 7:
                return TAX_UNIT_GENERIC_7;
            case 8:
                return TAX_UNIT_GENERIC_8;
            case 9:
                return TAX_UNIT_GENERIC_9;
        }

    }


    @Override
    public boolean canBuildUnit(Province province, int strength) {
        int strengthTemp=strength;
        if(strength==5){
            strengthTemp=2;
        }else if(strength==6){
            strengthTemp=12;
        }else if(strength==7){
            strengthTemp=5;
        }else if(strength==8){
            strengthTemp=4;
        }else if(strength==9){
            strengthTemp=4;
        }
        return province.money >= GameRules.PRICE_UNIT * strengthTemp;
    }


    @Override
    public void onUnitMoveToHex(Unit unit, Hex hex) {
        if (!hex.containsTree()) return;

        Province provinceByHex = gameController.getProvinceByHex(hex);
        if (provinceByHex != null) {
            provinceByHex.money += GameRules.TREE_CUT_REWARD;
        }
    }

    @Override
    public void onRevoltWin(Province province) {

    }

    @Override
    public boolean canUnitAttackHex(int unitStrength, Hex hex) {
        //System.out.println(unitStrength);
        int fix=unitStrength;
        if (unitStrength == 10) return true;
        if (unitStrength == 4 || unitStrength==9){
            if(hex.getDefenseNumber()==4){
                return true;
            }
        }
        if (unitStrength == 8){
            if(hex.getDefenseNumber()==3){
                if(hex.objectInside==Obj.STRONG_TOWER){
                    return false;
                }
                return true;
            }
        }
        //if(hex.isNullHex()) return true;


        if(unitStrength==5) {
            fix=1;
        }else if(unitStrength==6) {
            fix=2;
        }else if(unitStrength==7) {
            fix=1;
        }else if(unitStrength==8) {
            fix=3;
        }else if(unitStrength==9) {
            fix=4;
        }

        return fix > hex.getDefenseNumber();
    }


}
