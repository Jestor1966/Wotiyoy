package yio.tro.antiyoy.ai;

import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Obj;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.gameplay.rules.GameRules;

import java.util.ArrayList;

public abstract class ArtificialIntelligenceGeneric extends ArtificialIntelligence{


    public static final int MAX_EXTRA_FARM_COST = 160;


    ArtificialIntelligenceGeneric(GameController gameController, int fraction) {
        super(gameController, fraction);
    }


    @Override
    protected void spendMoney(Province province) {
        tryToBuildEssentialPort(province); // 沿海省份至少一个港口，优先保障
        invasionMode = gameController.fieldManager.provinceHasMultipleContinents(province);
        if (invasionMode) {
            // 登陆扩张：暴兵优先，迅速打击对方核心区域
            tryToBuildUnits(province);
            tryToBuildTowers(province);
            tryToBuildFarms(province);
            tryToBuildPort(province);
        } else {
            tryToBuildTowers(province);
            tryToBuildFarms(province);
            tryToBuildUnits(province);
            tryToBuildPort(province); // 港口排在防御塔、农场、单位之后，用盈余建造
        }
    }


    protected void tryToBuildFarms(Province province) {
//        if (province.getExtraFarmCost() > province.getIncome()) return;
        if (province.getExtraFarmCost() > MAX_EXTRA_FARM_COST) return;

        while (province.hasMoneyForFarm()
                && province.money >= province.getCurrentFarmPrice() + province.getPortPrice() + GameRules.PRICE_UNIT) {
            int moneyBefore = province.money;
            if (!isOkToBuildNewFarm(province)) return;
            Hex hex = findGoodHexForFarm(province);
            if (hex == null) return;
            gameController.fieldManager.buildFarm(province, hex);
            if (province.money == moneyBefore) return; // no progress -> avoid endless loop
        }
    }


    protected boolean isOkToBuildNewFarm(Province srcProvince) {
        if (srcProvince.money > 1.2 * srcProvince.getCurrentFarmPrice()) return true;
        if (findHexThatNeedsTower(srcProvince) != null) return false;

        return true;
    }


    protected int getArmyStrength(Province province) {
        int sum = 0;
        int temp = 0;
        for (Hex hex : province.hexList) {
            if (hex.containsUnit()) {
                temp=hex.unit.strength;
                if(temp==5){
                    temp=1;
                }else if(temp==6){
                    temp=2;
                }else if(temp==7){
                    temp=1;
                }else if(temp==8){
                    temp=3;
                }else if(temp==9){
                    temp=4;
                }
                sum += temp;
            }
        }
        return sum;
    }


    protected Hex findGoodHexForFarm(Province province) {
        boolean provinceHasPort = gameController.fieldManager.provinceHasUsablePort(province);
        ArrayList<Hex> candidates = new ArrayList<>();
        for (Hex hex : province.hexList) {
            if (!isHexGoodForFarm(hex)) continue;
            // 省份还没有可用港口时，沿海陆格留给港口，避免单位无法下海
            if (!provinceHasPort && !hex.isSea() && gameController.fieldManager.hasSeaNeighbor(hex)) continue;
            candidates.add(hex);
        }
        if (candidates.size() == 0) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }


    protected boolean hasProvinceGoodHexForFarm(Province province) {
        for (Hex hex : province.hexList) {
            if (!isHexGoodForFarm(hex)) continue;
            return true;
        }
        return false;
    }


    protected boolean isHexGoodForFarm(Hex hex) {
        if (!hex.isFree()) return false;
        if (hex.isSea() && !hex.inPortRange) return false;
        if (portWaterWouldBeBlocked(hex)) return false; // keep at least one free port-water hex for units
        if (!hex.isSea() && gameController.fieldManager.isAdjacentToOwnPortWater(hex, hex.fraction)) return false; // 港口落点保留空地
        if (!hex.isSea() && gameController.fieldManager.hasSeaNeighbor(hex)) return false; // 沿海陆格永远留给港口
        if (!hex.hasThisSupportiveObjectNearby(Obj.TOWN) && !hex.hasThisSupportiveObjectNearby(Obj.FARM)
                && !hex.hasThisSupportiveObjectNearby(Obj.CITY) && !hex.hasThisSupportiveObjectNearby(Obj.PORT)) return false;
        return true;
    }


    private boolean portWaterWouldBeBlocked(Hex hex) {
        if (hex.isSea() && hex.inPortRange && portWaterFreeSpaces(hex) <= 1) return true;
        return false;
    }


    private int portWaterFreeSpaces(Hex hex) {
        int freeSpaces = 0;
        for (int i = 0; i < 6; i++) {
            Hex adjHex = hex.getAdjacentHex(i);
            if (adjHex == null || adjHex.isNullHex()) continue;
            if (!adjHex.active || !adjHex.sea || !adjHex.inPortRange) continue;
            if (!adjHex.sameFraction(hex)) continue;
            if (adjHex.isFree()) freeSpaces++;
        }
        return freeSpaces;
    }
}
