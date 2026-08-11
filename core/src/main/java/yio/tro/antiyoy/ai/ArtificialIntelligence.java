package yio.tro.antiyoy.ai;

import yio.tro.antiyoy.gameplay.*;
import yio.tro.antiyoy.gameplay.rules.GameRules;

import java.util.ArrayList;
import java.util.Random;


public abstract class ArtificialIntelligence extends AbstractAi{

    final Random random;
    protected ArrayList<Province> nearbyProvinces;
    protected ArrayList<Unit> unitsReadyToMove;
    private ArrayList<Hex> tempResultList;
    private ArrayList<Hex> junkList;
    int numberOfUnitsBuiltThisTurn;
    protected boolean invasionMode; // 登陆扩张状态：暴兵优先


    public ArtificialIntelligence(GameController gameController, int fraction) {
        super(gameController, fraction);
        random = gameController.random;
        nearbyProvinces = new ArrayList<>();
        unitsReadyToMove = new ArrayList<>();
        tempResultList = new ArrayList<>();
        junkList = new ArrayList<>();
    }


    void updateUnitsReadyToMove() {
        unitsReadyToMove.clear();
        // 直接从全局单位列表收集，避免游离单位（不在任何省份 hexList 中）被忽略
        for (Unit unit : gameController.unitList) {
            if (unit.getFraction() != fraction) continue;
            if (unit.currentHex.fraction != fraction) continue; // 防御：所在格已被移交/吞并的单位不算己方
            if (!unit.isReadyToMove()) continue;
            if (unitsReadyToMove.contains(unit)) continue;
            unitsReadyToMove.add(unit);
        }
    }


    private void searchForUnitsReadyToMoveInProvince(Province province) {
        for (int k = province.hexList.size() - 1; k >= 0; k--) {
            Hex hex = province.hexList.get(k);
            if (hex.containsUnit() && hex.unit.isReadyToMove() && !unitsReadyToMove.contains(hex.unit)) {
                unitsReadyToMove.add(hex.unit);
            }
        }
    }


    void moveUnits() {
        updateUnitsReadyToMove();
        for (Unit unit : unitsReadyToMove) {
            if (!unit.isReadyToMove()) {
                System.out.println("Problem in ArtificialIntelligence.moveUnits()");
                System.out.println(unit.strength+",("+unit.currentHex.index1+","+unit.currentHex.index2+"),"+unitsReadyToMove.toString());
                continue;
            }

            ArrayList<Hex> moveZone = gameController.detectMoveZone(unit.currentHex, unit.strength, GameRules.UNIT_MOVE_LIMIT);
            excludeFriendlyBuildingsFromMoveZone(moveZone);
            excludeFriendlyUnitsFromMoveZone(moveZone);
            if (moveZone.size() == 0) continue;
            Province provinceByHex = gameController.getProvinceByHex(unit.currentHex);
            if (provinceByHex == null) continue;
            decideAboutUnit(unit, moveZone, provinceByHex);
        }
    }


    void spendMoneyAndMergeUnits() {
        for (int i = 0; i < gameController.fieldManager.provinces.size(); i++) {
            Province province = gameController.fieldManager.provinces.get(i);
            if (province.getFraction() != fraction) continue;
            spendMoney(province);
            mergeUnits(province);
        }
    }


    void moveAfkUnits() {
        updateUnitsReadyToMove();
        for (Unit unit : unitsReadyToMove) {
            if (!unit.isReadyToMove()) continue;

            Province province = gameController.getProvinceByHex(unit.currentHex);
            moveAfkUnit(province, unit);
        }
    }


    @Override
    public void perform() {
        numberOfUnitsBuiltThisTurn = 0;
        makeMove();
    }


    public abstract void makeMove();


    void moveAfkUnit(Province province, Unit unit) {
        // 防御：陆地单位必须属于某个省份；海上单位不依赖省份，仍可远征
        if (province == null && !unit.currentHex.isSea()) return;

        ArrayList<Hex> moveZone = gameController.detectMoveZone(unit.currentHex, unit.strength, GameRules.UNIT_MOVE_LIMIT);
        excludeFriendlyUnitsFromMoveZone(moveZone);
        excludeFriendlyBuildingsFromMoveZone(moveZone);
        if (moveZone.size() == 0) return;

        // 已经在海上的单位：沿最短海路前往最近的非己方大陆，而不是盲目乱走/占领海洋
        if (unit.currentHex.isSea()) {
            Hex seaStep = getSeaStepTowardsEnemyLand(unit, moveZone);
            if (seaStep != null && !seaStep.isSea()) {
                // 可以直接登录目标：进攻
                gameController.moveUnit(unit, seaStep, province);
                return;
            }
            if (seaStep != null && seaStep.isSea()) {
                // 目标大陆存在但当前无法登录（久攻不下）：尝试海上合成更高等级单位
                if (tryToMergeWithSomeone(province, unit)) return;
                // 合成不成就继续沿海路朝目标前进
                gameController.moveUnit(unit, seaStep, province);
                return;
            }
            return; // 海路不通或没有目标：留在原地，避免乱走
        }

        // idle units go to sea whenever the sea is actually reachable
        Hex seaHex = getFarthestSeaHexFromMoveZone(moveZone, unit);
        if (seaHex != null) {
            // 只有存在海外目标且本大陆没有需要优先消灭的弱敌时才出海
            if (hasOverseasTarget() && !hasWeakEnemyOnContinent(province)) {
                gameController.moveUnit(unit, seaHex, province);
            }
            return;
        }

        // 无法下海：没有弱敌需要优先消灭时，才走向港口准备下海
        if (hasOverseasTarget() && !hasWeakEnemyOnContinent(province)) {
            Hex approachHex = findHexToApproachPort(unit, moveZone);
            if (approachHex != null && !approachHex.equals(unit.currentHex)) {
                gameController.moveUnit(unit, approachHex, province);
            }
        }
    }


    private Hex getFarthestSeaHexFromMoveZone(ArrayList<Hex> moveZone, Unit unit) {
        Hex bestHex = null;
        double maxDistance = 0;
        for (Hex hex : moveZone) {
            if (!hex.isSea()) continue;
            double distance = hex.pos.fastDistanceTo(unit.currentHex.pos);
            if (bestHex == null || distance > maxDistance) {
                bestHex = hex;
                maxDistance = distance;
            }
        }
        return bestHex;
    }


    void mergeUnits(Province province) {
        for (int i = 0; i < province.hexList.size(); i++) {
            Hex hex = province.hexList.get(i);
            if (hex.containsUnit() && hex.unit.isReadyToMove()) {
                tryToMergeWithSomeone(province, hex.unit);
            }
        }
    }


    private boolean tryToMergeWithSomeone(Province province, Unit unit) {
        ArrayList<Hex> moveZone = gameController.detectMoveZone(unit.currentHex, unit.strength, GameRules.UNIT_MOVE_LIMIT);
        if (moveZone.size() == 0) return false;
        for (Hex hex : moveZone) {
            if (!mergeConditions(province, unit, hex)) continue;
            gameController.moveUnit(unit, hex, province); // should not call mergeUnits() directly
            return true;
        }
        return false;
    }


    protected boolean mergeConditions(Province province, Unit unit, Hex hex) {
        if (unit.currentHex.isSea() != hex.isSea()) return false; // 陆海单位不混合合并
        if (!hex.sameFraction(unit.currentHex)) return false;
        if (!hex.containsUnit()) return false;
        if (!hex.unit.isReadyToMove()) return false;
        if (unit == hex.unit) return false;
        if (!gameController.ruleset.canMergeUnits(unit, hex.unit)) return false;
        if (!province.canAiAffordUnit(gameController.mergedUnitStrength(unit, hex.unit))) return false;
        return true;
    }


    protected void spendMoney(Province province) {
        tryToBuildEssentialPort(province); // 沿海省份至少一个港口，优先保障
        invasionMode = gameController.fieldManager.provinceHasMultipleContinents(province);
        if (invasionMode) {
            // 登陆扩张：暴兵优先，迅速打击对方核心区域
            tryToBuildUnits(province);
            tryToBuildTowers(province);
            tryToBuildPort(province);
        } else {
            tryToBuildTowers(province);
            tryToBuildUnits(province);
            tryToBuildPort(province); // 港口排在防御塔和单位之后，用盈余建造
        }
    }


    void tryToBuildEssentialPort(Province province) {
        gameController.fieldManager.aiTryToBuildEssentialPort(province);
    }


    void tryToBuildPort(Province province) {
        gameController.fieldManager.aiTryToBuildPort(province);
    }




    void tryToBuildTowers(Province province) {
        while (province.hasMoneyForTower()
                && province.money >= GameRules.PRICE_TOWER + province.getPortPrice() + GameRules.PRICE_UNIT) {
            int moneyBefore = province.money;
            Hex hex = findHexThatNeedsTower(province);
            if (hex == null) return;
            gameController.fieldManager.buildTower(province, hex);
            if (province.money == moneyBefore) return; // no progress -> avoid endless loop
        }
    }


    protected Hex findHexThatNeedsTower(Province province) {
        for (Hex hex : province.hexList) {
            if(hex.isSea()) continue;
            if (needTowerOnHex(hex)) return hex;
        }
        return null;
    }


    boolean needTowerOnHex(Hex hex) {
        if (!hex.active) return false;
        if (!hex.isFree()) return false;
        if ( hex.isSea()) return false;

        return getPredictedDefenseGainByNewTower(hex) >= 5;
    }


    protected int getPredictedDefenseGainByNewTower(Hex hex) {
        int c = 0;

        if (hex.active && !hex.isDefendedByTower()) c++;

        for (int i = 0; i < 6; i++) {
            Hex adjHex = hex.getAdjacentHex(i);
            if (adjHex.active && hex.sameFraction(adjHex) && !adjHex.isDefendedByTower()) c++;
            if (adjHex.containsTower()) c--;
        }

        return c;
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


    protected void updateNearbyProvinces(Hex srcHex) {
        nearbyProvinces.clear();

        int j;
        for (int i = 0; i < 6; i++) {
            Hex adjacentHex = srcHex.getAdjacentHex(i);
            if (!adjacentHex.active) continue;

            Hex adjacentHex2 = adjacentHex.getAdjacentHex(i);
            j = i + 1;
            if (j >= 6) j = 0;
            Hex adjacentHex3 = adjacentHex.getAdjacentHex(j);

            checkToAddNearbyProvince(srcHex, adjacentHex);
            checkToAddNearbyProvince(srcHex, adjacentHex2);
            checkToAddNearbyProvince(srcHex, adjacentHex3);
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


    boolean tryToBuiltUnitInsideProvince(Province province, int strength) {
        for (Hex hex : province.hexList) {
            if (!hex.nothingBlocksWayForUnit()) continue;
            if (!isAllowedToBuildNewUnit(province)) continue;
            if (hex.isSea()) continue;
            buildUnit(province, hex, strength);
            return true;
        }
        return false;
    }


    protected boolean isAllowedToBuildNewUnit(Province province) {
        if (!GameRules.diplomacyEnabled) return true;
        //if (gameController.playersNumber == 0) return true;
        if (numberOfUnitsBuiltThisTurn < getBuildLimitForProvince(province)) return true;
        return false;
    }


    private  int getBuildLimitForProvince(Province province) {
        int bottom = Math.max(3, (province.hexList.size()-province.getSeaHexesNum()) / 6);
        return Math.min(bottom, 20);
    }


    protected void buildUnit(Province province, Hex hex, int strength) {
        boolean success = false;

        if (isAllowedToBuildNewUnit(province)) {
            success = gameController.fieldManager.buildUnit(province, hex, strength);
        }

        if (success) {
            numberOfUnitsBuiltThisTurn++;
        }
    }


    boolean tryToAttackWithStrength(Province province, int strength) {
        if (!isAllowedToBuildNewUnit(province)) {
            return false;
        }

        if(strength>=5 && strength<=7){
            tryToBulidUnitInBorder(province);
        }

        ArrayList<Hex> moveZone = gameController.detectMoveZone(province.getCapital(), strength);
        ArrayList<Hex> attackableHexes = findAttackableHexes(province.getFraction(), moveZone);
        if (attackableHexes.size() == 0) {
            return false;
        }

        Hex bestHexForAttack = findMostAttractiveHex(attackableHexes, province, strength);
        if(bestHexForAttack.isSea()) {
            boolean success=tryToBuiltUnitInsideProvince(province,strength);
            if(!success){
                return false;
            }
        }else{
            buildUnit(province, bestHexForAttack, strength);
        }
        return true;
    }

    boolean tryToBulidUnitInBorder(Province province){
        Hex temp;

        for (Hex hex:province.getNeibourHexList()){
            if(hex.hasUnit() || hex.isSea()){
                break;
            }
            for(int i=0;i<6;i++){
                temp=hex.getAdjacentHex(i);
                if(temp.fraction!=hex.fraction){
                    if(temp.containsSiege()){
                        if(province.canAiAffordUnit(6,8)){
                            buildUnit(province,hex,6);
                            return true;
                        }
                    }
                    if(!temp.hasUnit()){
                        return false;
                    }
                    if(temp.unit.strength<=2){
                        if(province.canAiAffordUnit(6,5)){
                            buildUnit(province,hex,6);
                            return true;
                        }else if(province.canAiAffordUnit(5,5)){
                            buildUnit(province,hex,5);
                            return true;
                        }
                    }
                }
            }
            break;
        }

        return false;
    }


    void tryToBuildUnitsOnPalms(Province province) {
        if (!province.canAiAffordUnit(1)) return;

        while (canProvinceBuildUnit(province, 1)) {
            ArrayList<Hex> moveZone = gameController.detectMoveZone(province.getCapital(), 1);
            boolean killedPalm = false;
            for (Hex hex : moveZone) {
                if (hex.objectInside != Obj.PALM || !hex.sameFraction(province) || hex.isSea()) continue;
                buildUnit(province, hex, 1);
                killedPalm = true;
            }
            if (!killedPalm) break;
        }
    }


    protected boolean canProvinceBuildUnit(Province province, int strength) {
        if (!province.canBuildUnit(strength)) return false;
        if (!isAllowedToBuildNewUnit(province)) return false;
        if (invasionMode) {
            // 登陆暴兵：只保留一个单位的价格作为应急盈余
            return province.money >= getUnitPrice(strength) + GameRules.PRICE_UNIT;
        }
        // 正常扩张：至少保留下一个港口的价格作为合理盈余
        return province.money >= getUnitPrice(strength) + province.getPortPrice() + GameRules.PRICE_UNIT;
    }


    private int getUnitPrice(int strength) {
        int fix = strength;
        if (strength == 5) fix = 2;
        else if (strength == 6) fix = 12;
        else if (strength == 7) fix = 5;
        else if (strength == 8) fix = 4;
        else if (strength == 9) fix = 4;
        return GameRules.PRICE_UNIT * fix;
    }


    void tryToBuildUnits(Province province) {
        tryToBuildUnitsOnPalms(province);

        for (int i = 1; i <= 6; i++) {
            if(Math.random()>=0.6&& i==2){
                i=5;
            }

            if(Math.random()>=0.8 && i==3){
                i=8;
            }

            if (!province.canAiAffordUnit(i)) break;
            while (canProvinceBuildUnit(province, i)) {
                int moneyBefore = province.money;
                if (!tryToAttackWithStrength(province,i)) {
                    if (!tryToBuiltUnitInsideProvince(province, i)) {
                        break;
                    }
                }
                if (province.money == moneyBefore) break; // no progress -> avoid endless loop
            }
        }

        // this is to kick start province
        if (canProvinceBuildUnit(province, 1) && howManyUnitsInProvince(province) <= 1) {
            tryToAttackWithStrength(province, 1);
        }
    }


    boolean checkToCleanSomeTrees(Unit unit, ArrayList<Hex> moveZone, Province province) {
        for (Hex hex : moveZone) {
            if (hex.containsTree() && hex.sameFraction(unit.currentHex) && !hex.isNeutral()) {
                gameController.moveUnit(unit, hex, province);
                return true;
            }
        }
        return false;
    }

    boolean checkToPushToBorder(Unit unit, ArrayList<Hex> moveZone, Province province) {
        for (Hex startHex : province.hexList) {
            for (Hex endHex : province.getNeibourHexList()){
                if(startHex.hasUnit()){
                    if(moveZone.contains(gameController.fieldManager.findBestWayToHex(startHex,endHex,startHex.unit))){
                        gameController.moveUnit(unit,gameController.fieldManager.findBestWayToHex(startHex,endHex,startHex.unit),province);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean checkToCleanSomeRebels(Unit unit, ArrayList<Hex> moveZone, Province province) {
        for (Hex hex : moveZone) {
            if (hex.objectInside == Obj.REVOLT && hex.sameFraction(unit.currentHex)) {
                gameController.moveUnit(unit, hex, province);
                return true;
            }
        }
        return false;
    }

    boolean checkToCleanSomePalms(Unit unit, ArrayList<Hex> moveZone, Province province) {
        for (Hex hex : moveZone) {
            if (hex.objectInside == Obj.PALM && hex.sameFraction(unit.currentHex) && !hex.isNeutral()) {
                gameController.moveUnit(unit, hex, province);
                return true;
            }
        }
        return false;
    }


    void decideAboutUnit(Unit unit, ArrayList<Hex> moveZone, Province province) {

        // 海上单位交给海上远征逻辑：沿最短海路前往最近的非己方大陆，而不是乱走/占领海洋
        if (unit.currentHex.isSea()) {
            moveAfkUnit(province, unit);
            return;
        }

        // cleaning palms has highest priority
        if (unit.strength <= 2 && checkToCleanSomePalms(unit, moveZone, province)) return;
        ArrayList<Hex> attackableHexes = findAttackableHexes(unit.getFraction(), moveZone);
        if (attackableHexes.size() > 0) { // attack something
            Hex mostAttackableHex = findMostAttractiveHex(attackableHexes, province, unit.strength);
            gameController.moveUnit(unit, mostAttackableHex, province);
        } else { // nothing to attack
            if(Math.random()>=0){
                boolean cleanedTrees = checkToCleanSomeTrees(unit, moveZone, province);
                if (!cleanedTrees) {
                    if (unit.currentHex.isInPerimeter()) {
                        pushUnitToBetterDefense(unit, province);
                    }
                }
            }else{
                /*
                if (unit.currentHex.isInPerimeter()) {
                    pushUnitToBetterDefense(unit, province);
                }*/

                boolean moveToBorder = checkToPushToBorder(unit, moveZone, province);
                if (!moveToBorder) {
                    if (unit.currentHex.isInPerimeter()) {
                        pushUnitToBetterDefense(unit, province);
                    }
                }
            }

        }
    }


    boolean checkChance(double chance) {
        return random.nextDouble() < chance;
    }


    void pushUnitToBetterDefense(Unit unit, Province province) {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = unit.currentHex.getAdjacentHex(i);
            if (adjHex.active && adjHex.sameFraction(unit.currentHex) && adjHex.isFree() && adjHex.howManyEnemyHexesNear() == 0) {
                gameController.moveUnit(unit, adjHex, province);
                break;
            }
        }
    }


    int getAttackAllure(Hex hex, int fraction) {
        int c = 0;
        for (int i = 0; i < 6; i++) {
            Hex adjHex = hex.getAdjacentHex(i);
            if (adjHex.active && adjHex.sameFraction(fraction)) {
                c++;
            }
        }
        return c;
    }


    Hex findHexAttractiveToBaron(ArrayList<Hex> attackableHexes, int strength) {
        Hex temp;
        for (Hex attackableHex : attackableHexes) {
            if (!(attackableHex.objectInside == Obj.MOUNTAIN || attackableHex.isDefendedByMountain())){
            if (attackableHex.objectInside == Obj.TOWER) return attackableHex;
            if (strength == 4 && attackableHex.objectInside == Obj.STRONG_TOWER) return attackableHex;
            if ((strength == 3 || strength==4) && attackableHex.isDefendedBySiege()) return attackableHex;

            if(attackableHex.active && attackableHex.hasUnit()){
                if (strength >= 2 && attackableHex.unit.strength == 5) return attackableHex;
                if (strength >= 3 && attackableHex.unit.strength == 6) return attackableHex;
                if (strength >= 2 && attackableHex.unit.strength == 7) return attackableHex;
                if (strength >= 3 && attackableHex.unit.strength == 8) return attackableHex;
                if (strength >= 4 && attackableHex.unit.strength == 9) return attackableHex;
                }
            }


        }
        for (Hex attackableHex : attackableHexes) {
            if (attackableHex.isDefendedByTower()) return attackableHex;
        }
        return null;
    }

    Hex findHexAttractiveToRanged(ArrayList<Hex> attackableHexes, int strength) {
        Hex temp,result;
        for (Hex attackableHex : attackableHexes) {
            if (!(attackableHex.objectInside == Obj.MOUNTAIN || attackableHex.isDefendedByMountain() || attackableHex.isNullHex() || attackableHex.containsObject())){
            if(attackableHex.active && attackableHex.hasUnit()){
                if(strength==5 && attackableHex.unit.strength<=2){
                    temp=attackableHex.getAdjacentHex((int)(Math.random()*(6-0)+0));
                    if(temp.index1<0 || temp.index2<0){
                        continue;
                    }
                    result=attackableHex.getAdjacentHex((int)(Math.random()*(6-0)+0));
                    if(result.index1<0 || result.index2<0){
                        continue;
                    }
                    if(!(result.hasUnit() || result.containsObject() || result.isNullHex())){
                        return result;
                    }else{
                        continue;
                    }
                }
                if(strength==6 && attackableHex.unit.strength<=2){
                    temp=attackableHex.getAdjacentHex((int)(Math.random()*(6-0)+0)).getAdjacentHex((int)(Math.random()*(6-0)+0)).getAdjacentHex((int)(Math.random()*(6-0)+0));
                    if(!(temp.hasUnit() || temp.containsObject() || temp.isNullHex())){
                        return temp;
                    }else{
                        continue;
                    }
                }
                if(strength==7 && attackableHex.unit.strength<=4){
                    temp=attackableHex.getAdjacentHex((int)(Math.random()*(6-0)+0));
                    if(temp.index1<0 || temp.index2<0){
                        continue;
                    }
                    temp=temp.getAdjacentHex((int)(Math.random()*(6-0)+0));
                    if(temp.index1<0 || temp.index2<0){
                        continue;
                    }
                    result=temp.getAdjacentHex((int)(Math.random()*(6-0)+0));
                    if(result.index1<0 || result.index2<0){
                        continue;
                    }
                    //temp=attackableHex.getAdjacentHex((int)(Math.random()*(6-0)+0)).getAdjacentHex((int)(Math.random()*(6-0)+0)).getAdjacentHex((int)(Math.random()*(6-0)+0));
                    if(!(result.hasUnit() || result.containsObject() || result.isNullHex())){
                        return result;
                    }else{
                        continue;
                    }
                }
            }
            }
        }
        for (Hex attackableHex : attackableHexes) {
            if (strength == 7 && attackableHex.isDefendedBySiege()) return attackableHex;
        }
        return null;
    }


    Hex findMostAttractiveHex(ArrayList<Hex> attackableHexes, Province province, int strength) {
        if (strength == 3 || strength==4) {
            Hex hex = findHexAttractiveToBaron(attackableHexes, strength);
            if (hex != null) return hex;
        }
        if (strength == 5 || strength==6 || strength==7) {
            Hex hex = findHexAttractiveToRanged(attackableHexes, strength);
            if (hex != null) return hex;
        }

        Hex result = null;
        int currMax = -1;
        for (Hex attackableHex : attackableHexes) {
            int currNum = getAttackAllure(attackableHex, province.getFraction());
            if (currNum > currMax) {
                currMax = currNum;
                result = attackableHex;
            }
        }
        return result;
    }


    ArrayList<Hex> findAttackableHexes(int attackerFraction, ArrayList<Hex> moveZone) {
        tempResultList.clear();
        for (Hex hex : moveZone) {
            if (hex.fraction == attackerFraction) continue;
            if (hex.isDefendedByMountain()) continue;
            tempResultList.add(hex);
        }
        return tempResultList;
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


    int numberOfFriendlyHexesNearby(Hex hex) {
        return hex.numberOfFriendlyHexesNearby();
    }


    int howManyUnitsInProvince(Province province) {
        int c = 0;
        for (Hex hex : province.hexList) {
            if (hex.containsUnit()) c++;
        }
        return c;
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
