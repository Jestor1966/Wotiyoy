package yio.tro.antiyoy.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import yio.tro.antiyoy.*;
import yio.tro.antiyoy.factor_yio.FactorYio;
import yio.tro.antiyoy.gameplay.data_storage.EncodeManager;
import yio.tro.antiyoy.gameplay.data_storage.EncodeableYio;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomacyManager;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomaticEntity;
import yio.tro.antiyoy.gameplay.editor.EditorProvinceData;
import yio.tro.antiyoy.gameplay.fog_of_war.FogOfWarManager;
import yio.tro.antiyoy.gameplay.game_view.GameView;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.diplomatic_dialogs.EventDialog;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.stuff.Yio;

import java.lang.reflect.Array;
import java.util.*;

public class FieldManager implements EncodeableYio {

    public final GameController gameController;
    public boolean letsCheckAnimHexes;
    public int landHexesQuantity;
    public int[] fractionLandHexes;
    public ArrayList<ArrayList<Hex>> continents = new ArrayList<>();
    private ArrayList<Integer> continentTotalLand = new ArrayList<>();
    private ArrayList<int[]> continentFractionLand = new ArrayList<>();
    private String[] fractionTitles = new String[0];
    public float hexSize;
    public float hexStep1;
    public float hexStep2;
    public Hex field[][];
    public ArrayList<Hex> activeHexes;
    public ArrayList<Hex> selectedHexes;
    public ArrayList<Hex> animHexes;
    public int fWidth;
    public int fHeight;
    public PointYio fieldPos;
    public float cos60;
    public float sin60;
    public Hex focusedHex;
    public Hex nullHex;
    public Hex responseAnimHex;
    public Hex defTipHex;
    public ArrayList<Hex> solidObjects;
    public ArrayList<Hex> defenseTips;
    public FactorYio responseAnimFactor;
    public FactorYio defenseTipFactor;
    public ArrayList<Province> provinces;
    public Province selectedProvince;
    public long timeToCheckAnimHexes;
    public int[] playerHexCount;
    public float compensatoryOffset; // fix for widescreen
    public FogOfWarManager fogOfWarManager;
    public DiplomacyManager diplomacyManager;
    public String initialLevelString;
    public MoveZoneManager moveZoneManager;
    private ArrayList<Hex> tempList;
    private ArrayList<Hex> propagationList;
    public MassMarchManager massMarchManager;
    public AutomaticTransitionWorker automaticTransitionWorker;


    public FieldManager(GameController gameController) {
        this.gameController = gameController;

        cos60 = (float) Math.cos(Math.PI / 3d);
        sin60 = (float) Math.sin(Math.PI / 3d);
        fieldPos = new PointYio();
        compensatoryOffset = 0;

        updateFieldPos();
        hexSize = 0.05f * Gdx.graphics.getWidth(); // radius
        hexStep1 = (float) Math.sqrt(3) * hexSize; // height
        hexStep2 = (float) Yio.distance(0, 0, 1.5 * hexSize, 0.5 * hexStep1);
        fWidth = 200;//110
        fHeight = 200;//70
        activeHexes = new ArrayList<>();
        selectedHexes = new ArrayList<>();
        animHexes = new ArrayList<>();
        solidObjects = new ArrayList<>();
        moveZoneManager = new MoveZoneManager(this);
        field = new Hex[fWidth][fHeight];
        responseAnimFactor = new FactorYio();
        provinces = new ArrayList<>();
        nullHex = new Hex(-1, -1, new PointYio(), this);
        nullHex.active = false;
        defenseTipFactor = new FactorYio();
        defenseTips = new ArrayList<>();
        fogOfWarManager = new FogOfWarManager(this);
        diplomacyManager = new DiplomacyManager(this);
        initialLevelString = null;
        tempList = new ArrayList<>();
        propagationList = new ArrayList<>();
        massMarchManager = new MassMarchManager(this);
        automaticTransitionWorker = new AutomaticTransitionWorker(this);
    }


    private void updateFieldPos() {
        fieldPos.y = -2.1f * GraphicsYio.height + compensatoryOffset;
    }


    public void updateHexInsideLevelStatuses() {
        for (int i = 0; i < fWidth; i++) {
            for (int j = 0; j < fHeight; j++) {
                field[i][j].updateCanContainsObjects();
            }
        }
    }


    public void clearField() {
        gameController.selectionManager.setSelectedUnit(null);
        solidObjects.clear();
        gameController.getUnitList().clear();
        clearProvincesList();
        moveZoneManager.clear();
        clearActiveHexesList();
    }


    public void cleanOutAllHexesInField() {
        for (int i = 0; i < fWidth; i++) {
            for (int j = 0; j < fHeight; j++) {
                if (!gameController.fieldManager.field[i][j].active) continue;
                gameController.cleanOutHex(gameController.fieldManager.field[i][j]);
            }
        }
    }


    public void clearProvincesList() {
        provinces.clear();
    }


    public void defaultValues() {
        selectedProvince = null;
        moveZoneManager.defaultValues();
        compensatoryOffset = 0;
    }


    public void clearActiveHexesList() {
        ListIterator listIterator = activeHexes.listIterator();
        while (listIterator.hasNext()) {
            listIterator.next();
            listIterator.remove();
        }
    }


    public void createField() {
        clearField();
        updateFieldPos();
    }


    public void generateMap() {
        generateMap(GameRules.slayRules);
    }


    public void generateMap(boolean slayRules) {
        if (slayRules) {
            gameController.getMapGeneratorSlay().generateMap(gameController.getPredictableRandom(), field);
        } else {
            gameController.getMapGeneratorGeneric().generateMap(gameController.getPredictableRandom(), field);
        }

        detectProvinces();
        gameController.selectionManager.deselectAll();
        detectNeutralLands();
        gameController.takeAwaySomeMoneyToAchieveBalance();
    }


    public void detectNeutralLands() {
        if (GameRules.slayRules) return;

        for (Hex activeHex : activeHexes) {
            activeHex.genFlag = false;
        }

        for (Province province : provinces) {
            for (Hex hex : province.hexList) {
                hex.genFlag = true;
            }
        }

        for (Hex activeHex : activeHexes) {
            if (activeHex.genFlag) continue;

            activeHex.setFraction(GameRules.NEUTRAL_FRACTION);
        }
    }


    public int[] getIncomeArray() {
        int[] array = new int[GameRules.fractionsQuantity];

        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }

        for (Province province : gameController.fieldManager.provinces) {
            int fraction = province.getFraction();
            if (fraction >= array.length) continue;
            array[fraction] += province.getIncome();
        }

        return array;
    }


    public void killUnitByStarvation(Hex hex) {
        cleanOutHex(hex);
        addSolidObject(hex, Obj.GRAVE);
        hex.animFactor.appear(1, 2);

        gameController.replayManager.onUnitDiedFromStarvation(hex);
    }


    public void killEveryoneByStarvation(Province province) {
        for (Hex hex : province.hexList) {
            if (hex.containsUnit()) {
                killUnitByStarvation(hex);
            }
        }
    }


    public void moveResponseAnimHex() {
        if (responseAnimHex != null) {
            responseAnimFactor.move();
            if (responseAnimFactor.get() < 0.01) responseAnimHex = null;
        }
    }


    public void move() {
        moveAnimHexes();
        automaticTransitionWorker.move();
    }


    private void moveAnimHexes() {
        for (Hex hex : animHexes) {
            if (!hex.selected) hex.move(); // to prevent double call of move()
            if (!letsCheckAnimHexes && hex.animFactor.get() > 0.99) {
                letsCheckAnimHexes = true;
            }

            // animation is off because it's buggy
            if (hex.animFactor.get() < 1) hex.animFactor.setValues(1, 0);
        }
    }


    public boolean isThereOnlyOneKingdomOnMap() {
        // kingdom can be multiple provinces of same fraction
        int fraction = -1;
        for (Province province : provinces) {
            if (province.hexList.size() == 0) continue; // 防御：跳过空省份
            if (province.hexList.get(0).isNeutral()) continue;

            if (fraction == -1) {
                fraction = province.getFraction();
                continue;
            }

            if (province.getFraction() != fraction) {
                return false;
            }
        }

        return true;
    }


    public int numberOfDifferentActiveProvinces() {
        int c = 0;
        for (Province province : provinces) {
            if (province.hexList.size() == 0) continue; // 防御：跳过空省份
            if (province.hexList.get(0).isNeutral()) continue;
            c++;
        }
        return c;
    }


    public int[] getPlayerHexCount() {
        for (int i = 0; i < playerHexCount.length; i++) {
            playerHexCount[i] = 0;
        }

        for (Hex activeHex : activeHexes) {
            if (activeHex.isNeutral()) continue;
            if (activeHex.isInProvince() && activeHex.fraction >= 0 && activeHex.fraction < playerHexCount.length) {
                playerHexCount[activeHex.fraction]++;
            }
        }

        return playerHexCount;
    }


    public int getLevelSize() {
        return gameController.levelSizeManager.levelSize;
    }


    private boolean checkRefuseStatistics() {
        RefuseStatistics instance = RefuseStatistics.getInstance();

        int sum = instance.refusedEarlyGameEnd + instance.acceptedEarlyGameEnd;
        if (sum < 5) return true;

        double ratio = (double) instance.acceptedEarlyGameEnd / (double) sum;

        if (ratio < 0.1) return false;

        return true;
    }


    public int possibleWinner() {
        if (!checkRefuseStatistics()) return -1;

        int numberOfAllHexes = activeHexes.size();

        int playerHexCount[] = getPlayerHexCount();
        for (int i = 0; i < playerHexCount.length; i++) {
            if (playerHexCount[i] > 0.7 * numberOfAllHexes) {
                return i;
            }
        }

        return -1;
    }


    public boolean hasAtLeastOneProvince() {
        return provinces.size() > 0;
    }


    public int numberOfProvincesWithFraction(int fraction) {
        int count = 0;
        for (Province province : provinces) {
            if (province.getFraction() != fraction) continue;
            count++;
        }
        return count;
    }


    public void transformGraves() {
        for (Hex hex : activeHexes) {
            if (gameController.isCurrentTurn(hex.fraction) && hex.objectInside == Obj.GRAVE) {
                if(hex.isSea()){
                    cleanOutHex(hex);
                }else{
                    cleanOutHex(hex);
                    //hex.blockToTreeFromExpanding = true;
                    //hex.blockToRevoltFromExpanding = true;
                }
            }
        }
    }

    public void transformRevolt() {
        for (Hex hex : activeHexes) {
            if (gameController.isCurrentTurn(hex.fraction) && hex.objectInside == Obj.FARM && gameController.getRandom().nextDouble() < 0.8) {
                spawnRevolt(hex);
                hex.blockToTreeFromExpanding = true;
                hex.blockToRevoltFromExpanding = true;
            }
        }
    }


    public void detectProvinces() {
        if (gameController.isInEditorMode()) return;

        clearProvincesList();
        MoveZoneDetection.unFlagAllHexesInArrayList(activeHexes);
        tempList.clear();
        propagationList.clear();

        for (Hex hex : activeHexes) {
            if (hex.isNeutral()) continue;
            if (hex.flag) continue;

            tempList.clear();
            propagationList.clear();
            propagationList.add(hex);
            hex.flag = true;
            propagateHex(tempList, propagationList);
            if (tempList.size() >= 2) {
                Province province = new Province(gameController, tempList);
                addProvince(province);
            }
        }

        for (Province province : provinces) {
            if (province.hasCapital()) continue;
            if(GameRules.captainAsCityRules){
                province.placeCapitalInRandomCity(gameController.predictableRandom);
            }else{
                province.placeCapitalInRandomPlace(gameController.predictableRandom);
            }
        }
    }


    public void tryToDetectAdditionalProvinces() {
        // this method doesn't erase already existing provinces, it just adds new ones

        if (gameController.isInEditorMode()) return;

        MoveZoneDetection.unFlagAllHexesInArrayList(activeHexes);
        tempList.clear();
        propagationList.clear();

        for (Hex hex : activeHexes) {
            if (hex.isNeutral()) continue;
            if (hex.flag) continue;
            if (getProvinceByHex(hex) != null) continue;

            tempList.clear();
            propagationList.clear();
            propagationList.add(hex);
            hex.flag = true;
            propagateHex(tempList, propagationList);
            if (tempList.size() >= 2) {
                applyAdditionalProvince(tempList);
            }
        }

        for (Province province : provinces) {
            if (province.hasCapital()) continue;
            province.placeCapitalInRandomPlace(gameController.predictableRandom);
        }
    }


    private void applyAdditionalProvince(ArrayList<Hex> list) {
        Province intersectedProvince = getIntersectedProvince(list);
        if (intersectedProvince != null) {
            for (Hex hex : list) {
                if (intersectedProvince.containsHex(hex)) continue;
                addHexToProvince(intersectedProvince, hex);
            }
            return;
        }

        Province province = new Province(gameController, list);
        addProvince(province);
    }


    private Province getIntersectedProvince(ArrayList<Hex> list) {
        if (list.size() == 0) return null;
        int fraction = list.get(0).fraction;
        for (Hex hex : list) {
            for (Province province : provinces) {
                if (province.getFraction() != fraction) continue;
                if (!province.containsHex(hex)) continue;
                return province;
            }
        }
        return null;
    }


    private void propagateHex(ArrayList<Hex> tempList, ArrayList<Hex> propagationList) {
        Hex tempHex;
        Hex adjHex;
        while (propagationList.size() > 0) {
            tempHex = propagationList.get(0);
            tempList.add(tempHex);
            propagationList.remove(0);
            for (int dir = 0; dir < 6; dir++) {
                adjHex = tempHex.getAdjacentHex(dir);

                if (!adjHex.active) continue;
                if (!adjHex.sameFraction(tempHex)) continue;
                if (adjHex.flag) continue;

                propagationList.add(adjHex);
                adjHex.flag = true;
            }
        }
    }


    public void forceAnimEndInHex(Hex hex) {
        hex.animFactor.setValues(1, 0);
    }


    public int howManyPalms() {
        int c = 0;
        for (Hex activeHex : activeHexes) {
            if (activeHex.objectInside == Obj.PALM) c++;
        }
        return c;
    }


    public void expandTrees() {
        if (GameRules.replayMode) return;

        ArrayList<Hex> newPalmsList = getNewPalmsList();
        ArrayList<Hex> newPinesList = getNewPinesList();
        ArrayList<Hex> newRevoltList = getNewRevoltList();
        ArrayList<Hex> newRovoltNewList = getNewRevoltNewList();

        for (int i = newPalmsList.size() - 1; i >= 0; i--) {
            spawnPalm(newPalmsList.get(i));
        }

        for (int i = newPinesList.size() - 1; i >= 0; i--) {
            spawnPine(newPinesList.get(i));
        }

        for (int i = newRevoltList.size() - 1; i >= 0; i--) {
            spawnRevolt(newRevoltList.get(i));
        }

        for (Hex hex : newRovoltNewList) {
            if (Math.random() <= 0.01 && GameRules.enableRebel) {
                spawnRevolt(hex);
            }
        }

        for (Hex activeHex : activeHexes) {
            if (activeHex.containsTree() && activeHex.blockToTreeFromExpanding) {
                activeHex.blockToTreeFromExpanding = false;
                activeHex.blockToRevoltFromExpanding = false;
            }
            if (activeHex.containsTree() && activeHex.blockToRevoltFromExpanding) {
                activeHex.blockToTreeFromExpanding = false;
                activeHex.blockToRevoltFromExpanding = false;
            }
        }
    }


    private ArrayList<Hex> getNewPinesList() {
        ArrayList<Hex> newPinesList = new ArrayList<Hex>();

        for (Hex hex : activeHexes) {
            if (gameController.ruleset.canSpawnPineOnHex(hex)) {
                newPinesList.add(hex);
            }
        }

        return newPinesList;
    }

    private ArrayList<Hex> getNewRevoltList() {
        ArrayList<Hex> newRevoltList = new ArrayList<Hex>();

        for (Hex hex : activeHexes) {
            if (gameController.ruleset.canSpawnRevoltOnHex(hex)) {
                newRevoltList.add(hex);
            }
        }

        return newRevoltList;
    }

    private ArrayList<Hex> getNewRevoltNewList() {
        ArrayList<Hex> newRevoltList = new ArrayList<Hex>();

        for (Hex hex : activeHexes) {
            if (hex.objectInside == Obj.FARM) {
                newRevoltList.add(hex);
            }
        }

        return newRevoltList;
    }


    private ArrayList<Hex> getNewPalmsList() {
        ArrayList<Hex> newPalmsList = new ArrayList<Hex>();

        for (Hex hex : activeHexes) {
            if (gameController.ruleset.canSpawnPalmOnHex(hex)) {
                newPalmsList.add(hex);
            }
        }

        return newPalmsList;
    }


    private void spawnPine(Hex hex) {
        if (!hex.canContainObjects) return;

        addSolidObject(hex, Obj.PINE);
        addAnimHex(hex);
        hex.animFactor.setValues(1, 0);
        gameController.replayManager.onPineSpawned(hex);
    }

    private void spawnRevolt(Hex hex) {
        if (!hex.canContainObjects) return;
        Province province = getProvinceByHex(hex);
        //Random random = new Random();

        if (province == null) {
            cleanOutHex(hex);
            addSolidObject(hex, Obj.REVOLT);
            addAnimHex(hex);
            hex.animFactor.setValues(1, 0);
            gameController.replayManager.onRevoltSpawned(hex);
            return;
        }
        if (province.hexList.size() <= 4) {
            if (hex.blockToRevoltFromExpanding) return;
            hex.blockToTreeFromExpanding = true;
            hex.blockToRevoltFromExpanding = true;
            cleanOutHex(hex);
            addSolidObject(hex, Obj.REVOLT);
            addAnimHex(hex);
            hex.animFactor.setValues(1, 0);
            gameController.replayManager.onRevoltSpawned(hex);
        } else {
            float result = ((float) province.countObjects(Obj.REVOLT)) / ((float) (province.hexList.size()));
            if (result >= 0.6) {
                for (Hex hexs : province.hexList) {
                    if (hexs.objectInside == Obj.REVOLT) {
                        cleanOutHex(hexs);
                    }
                    province.money = province.money / 2;
                }
            }
            else {
                if (hex.blockToRevoltFromExpanding) return;
                hex.blockToTreeFromExpanding = true;
                hex.blockToRevoltFromExpanding = true;
                cleanOutHex(hex);
                addSolidObject(hex, Obj.REVOLT);
                addAnimHex(hex);
                hex.animFactor.setValues(1, 0);
                gameController.replayManager.onRevoltSpawned(hex);
            }
        }
    }
    /*
    private void spawnRevolt(Hex hex) {
        int revoltNum=0;
        if (!hex.canContainObjects) return;
        Province province = getProvinceByHex(hex);

        if (province == null) {
            cleanOutHex(hex);
            addSolidObject(hex, Obj.REVOLT);
            addAnimHex(hex);
            hex.animFactor.setValues(1, 0);
            gameController.replayManager.onRevoltSpawned(hex);
            return;
        }
        float result = ((float) province.countObjects(Obj.REVOLT)) / ((float) (province.hexList.size()));

        if (result >= 0.4) {
            province.money = province.money / 3;
            for (Hex hexs : province.hexList) {
                if(hexs.objectInside==Obj.REVOLT){
                    cleanOutHex(hexs);
                }
            }
            if(Math.random()<0){
                int temp;
                for(;;){
                    temp=(int)(Math.random()*((GameRules.MAX_FRACTIONS_QUANTITY+1)-1)+1);
                    if(temp==7){
                        continue;
                    }else{
                        break;
                    }
                }
                for (Hex hexs : province.hexList) {
                    setHexFraction(hexs,temp);
                }
                removeProvince(province);
                addProvince(province);
                province.money=province.money+revoltNum*2;
            }else{
                   if(province.hexList.size()<=10) return;
                   int tempNumRebel,tempFraction = 0,splitNum;
                   ArrayList<Hex> rebelHex=new ArrayList<>();
                   ArrayList<Integer> splitNums = new ArrayList<>();
                   Hex tempHex,hex1;
                   tempNumRebel=(int)(Math.random()*((9+3)-3)+3);
                   splitNums.add(0)
                   splitNum=(int)(Math.random()*((province.hexList.size()-9)));
                   splitNums.add(splitNum);

                   for(int i=0;i<=tempNumRebel;i++){
                       splitNum=(int)(Math.random()*((province.hexList.size()-9)-splitNum)+splitNum);
                       splitNums.add(splitNum);
                   }
                   removeProvince(province);
                    for(int n=1;n<=splitNums.size();n++){
                        for(;;){
                            tempFraction=(int)(Math.random()*((GameRules.MAX_FRACTIONS_QUANTITY+1)-1)+1);
                            if(tempFraction==7){
                                continue;
                            }else{
                                break;
                            }
                        }
                        for(int i=splitNums.get(n-1);i<=n;i++){
                            province.hexList.get(i).fraction=tempFraction;
                            rebelHex.add(province.hexList.get(i));
                        }
                        applyAdditionalProvince(rebelHex);

                    }
                }
            }
            else {
                cleanOutHex(hex);
                addSolidObject(hex, Obj.REVOLT);
                addAnimHex(hex);
                hex.animFactor.setValues(1, 0);
                gameController.replayManager.onRevoltSpawned(hex);
            }
        }*/


    private void spawnPalm(Hex hex) {
        if (!hex.canContainObjects) return;

        addSolidObject(hex, Obj.PALM);
        addAnimHex(hex);
        hex.animFactor.setValues(1, 0);
        gameController.replayManager.onPalmSpawned(hex);
    }


    public void createPlayerHexCount() {
        playerHexCount = new int[GameRules.fractionsQuantity];
    }


    public void checkAnimHexes() {
        // important
        // this fucking anims hexes have to live long enough
        // if killed too fast, graphic bugs will show
        if (gameController.isSomethingMoving()) {
            timeToCheckAnimHexes = gameController.getCurrentTime() + 100;
            return;
        }
        letsCheckAnimHexes = false;
        ListIterator iterator = animHexes.listIterator();
        while (iterator.hasNext()) {
            Hex h = (Hex) iterator.next();
            if (h.animFactor.get() > 0.99
                    && !(h.containsUnit() && !h.isSea() && h.unit.moveFactor.get() < 1)
                    && System.currentTimeMillis() > h.animStartTime + 250) {
                h.changingFraction = false;
                iterator.remove();
            }
        }
    }


    public boolean atLeastOneUnitIsReadyToMove() {
        for (Unit unit : gameController.getUnitList()) {
            if (unit.isReadyToMove()) return true;
        }
        return false;
    }


    public int getPredictionForWinner() {
        int numbers[] = new int[GameRules.fractionsQuantity];
        for (Hex activeHex : activeHexes) {
            if (activeHex.isNeutral()) continue;
            numbers[activeHex.fraction]++;
        }

        int max = numbers[0];
        int maxIndex = 0;
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > max) {
                max = numbers[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }


    public boolean areConditionsGoodForPlayer() {
        int numbers[] = new int[GameRules.fractionsQuantity];
        for (Hex activeHex : activeHexes) {
            if (activeHex.isNeutral()) continue;
            numbers[activeHex.fraction]++;
        }

        int max = GameController.maxNumberFromArray(numbers);
        return max - numbers[0] < 2;
    }


    public void onEndCreation() {
        clearAnims();
        updateHexInsideLevelStatuses();
        defenseTips.clear();

        diplomacyManager.onEndCreation();
        fogOfWarManager.onEndCreation();
        updateInitialLevelString();
    }


    private void updateInitialLevelString() {
        initialLevelString = gameController.gameSaver.legacyExportManager.getFullLevelString();
    }


    public void onUserLevelLoaded() {
        updateInitialLevelString();
    }


    public void clearAnims() {
        ListIterator iterator = animHexes.listIterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }


    public void createFieldMatrix() {
        for (int i = 0; i < fWidth; i++) {
            field[i] = new Hex[fHeight];
            for (int j = 0; j < fHeight; j++) {
                field[i][j] = new Hex(i, j, fieldPos, this);
                field[i][j].ignoreTouch = false;
            }
        }
    }


    public void marchUnitsToHex(Hex target) {
        if (!gameController.selectionManager.isSomethingSelected()) return;
        if (!target.isSelected()) return;

        MassMarchManager massMarchManager = gameController.fieldManager.massMarchManager;
        massMarchManager.clearChosenUnits();
        if (selectedProvince.hasSomeoneReadyToMove()) {
            gameController.takeSnapshot();
            for (Hex hex : selectedProvince.hexList) {
                if (hex.containsUnit() && hex.unit.isReadyToMove()) {
                    massMarchManager.addChosenUnit(hex.unit);
                }
            }
            massMarchManager.performMarch(target);
        }

        setResponseAnimHex(target);
        SoundManagerYio.playSound(SoundManagerYio.soundHoldToMarch);
    }


    public void setResponseAnimHex(Hex hex) {
        responseAnimHex = hex;
        responseAnimFactor.setValues(1, 0.07);
        responseAnimFactor.destroy(1, 2);
    }


    public void selectAdjacentHexes(Hex startHex) {
        setSelectedProvince(startHex);
        if (selectedProvince == null) return;

        ListIterator listIterator = selectedHexes.listIterator();
        for (Hex hex : selectedProvince.hexList) {
            hex.select();
            if (!selectedHexes.contains(hex)) listIterator.add(hex);
        }
        showBuildOverlay();
    }


    public void showBuildOverlay() {
        if (SettingsManager.fastConstructionEnabled) {
            Scenes.sceneFastConstructionPanel.create();
        } else {
            Scenes.sceneSelectionOverlay.create();
        }
        Scenes.sceneFinances.create();
    }


    public void setSelectedProvince(Hex hex) {
        selectedProvince = getProvinceByHex(hex);
        if (selectedProvince == null) return;

        gameController.selectionManager.getSelMoneyFactor().setDy(0);
        gameController.selectionManager.getSelMoneyFactor().appear(3, 2);
    }


    public void updateHexPositions() {
        updateFieldPos();

        for (int i = 0; i < fWidth; i++) {
            for (int j = 0; j < fHeight; j++) {
                Hex hex = field[i][j];

                hex.updatePos();
                if (hex.containsUnit()) {
                    hex.unit.updateCurrentPos();
                }
            }
        }
    }


    public boolean isCityNameUsed(String string) {
        for (Province province : provinces) {
            if (province.name == null) continue;
            if (province.name.equals(string)) return true;
        }
        for (EditorProvinceData editorProvinceData : gameController.levelEditorManager.editorProvinceManager.provincesList) {
            if (editorProvinceData.name == null) continue;
            if (editorProvinceData.name.equals(string)) return true;
        }
        if (GameRules.diplomacyEnabled) {
            for (DiplomaticEntity entity : diplomacyManager.entities) {
                if (entity.capitalName == null) continue;
                if (entity.capitalName.equals(string)) return true;
            }
        }
        return gameController.namingManager.isNameUsed(string);
    }


    public Hex getHexByPos(double x, double y) {
        int j = (int) ((x - fieldPos.x) / (hexStep2 * sin60));
        int i = (int) ((y - fieldPos.y - hexStep2 * j * cos60) / hexStep1);
        if (i < 0 || i > fWidth - 1 || j < 0 || j > fHeight - 1) return null;

        Hex adjHex, resHex = field[i][j];
        x -= gameController.getYioGdxGame().gameView.hexViewSize;
        y -= gameController.getYioGdxGame().gameView.hexViewSize;

        double currentDistance, minDistance = Yio.distance(resHex.pos.x, resHex.pos.y, x, y);
        for (int k = 0; k < 6; k++) {
            adjHex = adjacentHex(field[i][j], k);
            if (adjHex == null || !adjHex.active) continue;
            currentDistance = Yio.distance(adjHex.pos.x, adjHex.pos.y, x, y);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                resHex = adjHex;
            }
        }

        return resHex;
    }


    public Hex getHex(int i, int j) {
        if (i < 0 || i > fWidth - 1 || j < 0 || j > fHeight - 1) return null;

        return field[i][j];
    }


    public Hex adjacentHex(int i, int j, int direction) {
        if (i < 0 || i >= fWidth || j < 0 || j >= fHeight) return nullHex;

        switch (direction) {
            case 0:
                if (i >= fWidth - 1) return nullHex;
                return field[i + 1][j];
            case 1:
                if (j >= fHeight - 1) return nullHex;
                return field[i][j + 1];
            case 2:
                if (i <= 0 || j >= fHeight - 1) return nullHex;
                return field[i - 1][j + 1];
            case 3:
                if (i <= 0) return nullHex;
                return field[i - 1][j];
            case 4:
                if (j <= 0) return nullHex;
                return field[i][j - 1];
            case 5:
                if (i >= fWidth - 1 || j <= 0) return nullHex;
                return field[i + 1][j - 1];
            default:
                return nullHex;
        }
    }


    public void spawnTree(Hex hex) {
        if (!hex.active) return;
        hex.blockToTreeFromExpanding = true;
        hex.blockToRevoltFromExpanding = true;
        if (hex.isNearWater()) addSolidObject(hex, Obj.PALM);
        else addSolidObject(hex, Obj.PINE);
    }

    public void checkRevoltWin(Province province) {
        if ((province.countObjects(Obj.REVOLT) / province.hexList.size() >= 0.6)) {
            if(Math.random()>=0){
                int temp;
                for(;;){
                    temp=(int)(Math.random()*((GameRules.MAX_FRACTIONS_QUANTITY+1)-1)+1);
                    if(temp==7){
                        continue;
                    }else{
                        break;
                    }
                }
                for (Hex hex:province.hexList){
                    hex.fraction=temp;
                }
                removeProvince(province);
                addProvince(province);
            }
            province.money = province.money / 3;
            for (Hex hex : province.hexList) {
                if (hex.objectInside == Obj.REVOLT) {
                    cleanOutHex(hex);
                }
            }
        }
    }


    public void addSolidObject(Hex hex, int type) {
        if (hex == null || !hex.active) return;
        if (hex.sea && isBuildingType(type)) return;
        if (hex.objectInside == type) return;
        if (!hex.canContainObjects) return;

        if (solidObjects.contains(hex)) {
            cleanOutHex(hex);
        }

        hex.setObjectInside(type);
        solidObjects.listIterator().add(hex);
    }


    private boolean isBuildingType(int type) {
        switch (type) {
            case Obj.TOWN:
            case Obj.TOWER:
            case Obj.FARM:
            case Obj.STRONG_TOWER:
            case Obj.FORT:
            case Obj.CITY:
            case Obj.PORT:
                return true;
            default:
                return false;
        }
    }

    public void controlAll(Province province){
        for(Hex hex:province.hexList){
            hex.fraction=gameController.turn;
            hex.changingFraction = true;
            hex.animFactor.setValues(0, 0);
            hex.animFactor.appear(1, 1);

            gameController.replayManager.onHexChangedFractionWithoutProvince(hex);

            if (!gameController.isPlayerTurn()) {
                forceAnimEndInHex(hex);
            }
            if(hex.hasUnit()){
                hex.unit.setReadyToMove(false);
                gameController.replayManager.onUnitBuilt(province,hex,hex.unit.strength);
            }
        }
        //gameController.fieldManager.removeProvince(province);
        province.setHexList(province.hexList);

    }

    public void cleanOutHex(Hex hex) {
        boolean wasPort = hex.objectInside == Obj.PORT;
        if (hex.containsUnit()) {
            gameController.getMatchStatistics().onUnitKilled();
            gameController.getUnitList().remove(hex.unit);
            hex.unit = null;
        }
        hex.setObjectInside(0);
        if (wasPort) {
            markPortRange(hex, false);
        }
        addAnimHex(hex);
        ListIterator iterator = solidObjects.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next() == hex) {
                iterator.remove();
                return;
            }
        }
    }

    public void siege(Hex hex) {
        //GameController.maxNumberFromArraySiege(fractions);
        //setHexFraction(hex, gameController.turn);
        hex.setFraction(gameController.turn);
        hex.changingFraction = true;
    }

    public void archerShot(Hex targetHex) {
        if (targetHex == null) return;
        gameController.replayManager.onArcherShot(targetHex);
        cleanOutHex(targetHex);
        addSolidObject(targetHex, Obj.GRAVE);
        gameController.updateCacheOnceAfterSomeTime();
    }


    // archers ignore protection provided by towers
    public boolean isArcherAbleToHit(Hex target) {
        if (target == null) return false;
        return 2 > target.getDefenseNumberWithoutTowers();
    }


    public void updateArcherRange(Unit archer) {
        clearArcherRange();
        if (archer == null) return;
        if (archer.strength == 6) {
            updateCannonRange(archer);
            return;
        }
        if (archer.strength != 5) return;

        for (int m = 0; m < 6; m++) {
            Hex temp = archer.currentHex.getAdjacentHex(m);
            checkArcherTarget(archer, temp);
            if (temp.isNullHex()) continue;
            for (int n = 0; n < 6; n++) {
                checkArcherTarget(archer, temp.getAdjacentHex(n));
            }
        }
    }


    private void updateCannonRange(Unit cannon) {
        for (int m = 0; m < 6; m++) {
            Hex temp = cannon.currentHex.getAdjacentHex(m);
            // 1 hex: units, sieges, towers and farms
            checkCannonTarget(cannon, temp, true, true);
            if (temp.isNullHex()) continue;
            for (int n = 0; n < 6; n++) {
                Hex temp2 = temp.getAdjacentHex(n);
                // 2 hex: units, normal towers, farms
                checkCannonTarget(cannon, temp2, false, true);
                if (temp2.isNullHex()) continue;
                for (int b = 0; b < 6; b++) {
                    // 3 hex: units only
                    checkCannonTarget(cannon, temp2.getAdjacentHex(b), false, false);
                }
            }
        }
    }


    private void checkCannonTarget(Unit cannon, Hex target, boolean allowSiege, boolean allowBuildings) {
        if (target == null || target.isNullHex()) return;
        if (!target.active) return;
        if (target.fraction == cannon.getFraction()) return;
        if (!diplomacyManager.isWar(cannon.currentHex, target) && !target.isNeutral()) return;

        if (target.containsSiege()) {
            if (allowSiege) target.inArcherRange = true;
            return;
        }
        if (target.hasUnit()) {
            if (gameController.canUnitAttackHex(4, cannon.getFraction(), target)) {
                target.inArcherRange = true;
            }
            return;
        }
        if (allowBuildings && (target.containsNormalTower() || target.objectInside == Obj.FARM)) {
            target.inArcherRange = true;
        }
    }


    public void cannonShot(Hex targetHex) {
        if (targetHex == null) return;
        gameController.replayManager.onCannonShot(targetHex);
        if (targetHex.containsSiege()) {
            setHexFraction(targetHex, gameController.turn); // full capture (province joining etc.)
            gameController.updateCacheOnceAfterSomeTime();
            return;
        }

        boolean wasUnit = targetHex.hasUnit();
        cleanOutHex(targetHex);
        if (wasUnit) {
            addSolidObject(targetHex, Obj.GRAVE);
        }
        gameController.updateCacheOnceAfterSomeTime();
    }


    private void checkArcherTarget(Unit archer, Hex target) {
        if (target == null || target.isNullHex()) return;
        if (!target.active || !target.hasUnit()) return;
        if (target.unit.getFraction() == archer.getFraction()) return;
        if (!diplomacyManager.isWar(archer.currentHex, target)) return;
        if (!isArcherAbleToHit(target)) return;
        target.inArcherRange = true;
    }


    public void clearArcherRange() {
        for (Hex hex : activeHexes) {
            hex.inArcherRange = false;
        }
    }


    public static int findMostInArray(int[] a){
        int[] b=new int[50];
        for(int i=0;i<a.length;i++){
            if(a[i]==0){
                continue;
            }
            b[a[i]]++;
        }
        int max=b[0];
        int value=0;
        for(int i=1;i<b.length;i++){
            if(b[i]>max){
                max=b[i];
                value=i;
            }
        }
        return value;
        }

    public void destroyBuildingsOnHex(Hex hex) {
        boolean hadHouse = (hex.objectInside == Obj.TOWN);
        boolean hadHill = (hex.objectInside == Obj.HILL);
        if (hex.containsBuilding() && hex.containsSiege()) {
            siege(hex);
        }else if(hex.containsBuilding() && hadHill){
            siege(hex);
        }else {
            cleanOutHex(hex);
            }

        if (hadHouse) {
            if(GameRules.captainAsCityRules){
                siege(hex);
                addSolidObject(hex,Obj.CITY);
                gameController.replayManager.onHexChangedFractionWithoutProvince(hex);
                gameController.replayManager.onCityBuilt(hex);
            }else{
                spawnTree(hex);
            }
            }
        }


    public boolean buildUnit(Province province, Hex hex, int strength) {
        int moneyFix=0;
        if (province == null || hex == null) return false;
        if (hex.sea && (!hex.inPortRange || hex.portOwnerFraction != province.getFraction())) return false;
        // check for unmergeable situation
        if (isUnmergeableSituationDetected(province, hex, strength)) return false;

        gameController.takeSnapshot();
        if(strength>=5){
            if(strength==5){
                moneyFix=2;
            }else if(strength==6){
                moneyFix=12;
            }else if(strength==7){
                moneyFix=5;
            }else if(strength==8){
                moneyFix=4;
            }else if(strength==9){
                moneyFix=4;
            }
            if (!province.canBuildUnit(strength)) {
                tickleMoneySign();
                return false;
            }
            province.money -= GameRules.PRICE_UNIT * moneyFix;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_UNIT * moneyFix);
        }else{
            if (!province.canBuildUnit(strength)) {
                tickleMoneySign();
                return false;
            }
            province.money -= GameRules.PRICE_UNIT * strength;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_UNIT * strength);
        }
        gameController.replayManager.onUnitBuilt(province, hex, strength);



        if (canUnitBeBuiltPeacefully(province, hex)) {
            buildUnitPeacefully(hex, strength);
        } else {
            buildUnitByAttack(province, hex, strength);
        }
        return true;
    }


    private void buildUnitByAttack(Province province, Hex hex, int strength) {
        setHexFraction(hex, province.getFraction()); // must be called before object in hex destroyed
        addUnit(hex, strength);
        hex.unit.setReadyToMove(false);
        hex.unit.stopJumping();
        addHexToProvince(province, hex);
        addAnimHex(hex);
        gameController.updateCacheOnceAfterSomeTime();
    }


    private void buildUnitPeacefully(Hex hex, int strength) {
        if (!hex.containsUnit()) {
            addUnit(hex, strength);
            return;
        }

        // merge units

        if(strength>=5){
            if(hex.hasUnit()){
                return;
            }
        }
        Unit newUnit = new Unit(gameController, hex, strength);
        newUnit.setReadyToMove(true);
        gameController.matchStatistics.unitsDied++;
        gameController.mergeUnits(hex, newUnit, hex.unit);
    }


    private boolean isUnmergeableSituationDetected(Province province, Hex hex, int strength) {
        return hex.sameFraction(province) && hex.containsUnit() && !gameController.canMergeUnits(strength, hex.unit.strength);
    }


    private void tickleMoneySign() {
        if (!gameController.isPlayerTurn()) return;
        gameController.tickleMoneySign();
    }


    private boolean canUnitBeBuiltPeacefully(Province province, Hex hex) {
        return hex.sameFraction(province);
    }


    public boolean buildTower(Province province, Hex hex) {
        if (province == null) return false;
        if (hex.sea) return false;
        if (province.hasMoneyForTower()) {
            gameController.takeSnapshot();
            gameController.replayManager.onTowerBuilt(hex, false);
            addSolidObject(hex, Obj.TOWER);
            addAnimHex(hex);
            province.money -= GameRules.PRICE_TOWER;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_TOWER);
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build tower
        tickleMoneySign();
        return false;
    }


    public boolean buildStrongTower(Province province, Hex hex) {
        if (province == null) return false;
        if (hex.sea) return false;
        if (province.hasMoneyForStrongTower()) {
            gameController.takeSnapshot();
            gameController.replayManager.onTowerBuilt(hex, true);
            addSolidObject(hex, Obj.STRONG_TOWER);
            addAnimHex(hex);
            province.money -= GameRules.PRICE_STRONG_TOWER;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_STRONG_TOWER);
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build tower
        tickleMoneySign();
        return false;
    }


    public boolean buildFarm(Province province, Hex hex) {
        if (province == null) return false;
        if (hex.sea && (!hex.inPortRange || hex.portOwnerFraction != province.getFraction())) return false; // farms only in own port water
        if (!hex.hasThisSupportiveObjectNearby(Obj.TOWN) && !hex.hasThisSupportiveObjectNearby(Obj.FARM)
                && !hex.hasThisSupportiveObjectNearby(Obj.CITY) && !hex.hasThisSupportiveObjectNearby(Obj.PORT)) {
            return false;
        }

        if (province.hasMoneyForFarm()) {
            gameController.takeSnapshot();
            gameController.replayManager.onFarmBuilt(hex);
            province.money -= province.getCurrentFarmPrice();
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, province.getCurrentFarmPrice());
            addSolidObject(hex, Obj.FARM);
            addAnimHex(hex);
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build farm
        tickleMoneySign();
        return false;
    }


    public boolean buildPort(Province province, Hex hex) {
        if (province == null || hex == null) return false;
        if (hex.sea) return false; // ports are built on coastal land
        if (!hex.isFree()) return false;
        if (!hasSeaNeighbor(hex)) return false;

        int price = province.getPortPrice();
        if (province.money < price) {
            tickleMoneySign();
            return false;
        }

        gameController.takeSnapshot();
        addSolidObject(hex, Obj.PORT);
        addAnimHex(hex);
        province.money -= price;
        gameController.getMatchStatistics().onMoneySpent(gameController.turn, price);
        markPortRange(hex, true);
        gameController.updateCacheOnceAfterSomeTime();
        return true;
    }


    public boolean hasSeaNeighbor(Hex hex) {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = hex.getAdjacentHex(i);
            if (adjHex == null || adjHex.isNullHex()) continue;
            if (adjHex.active && adjHex.sea) return true;
        }
        return false;
    }


    public boolean aiTryToBuildPort(Province province) {
        if (province == null) return false;

        ArrayList<ArrayList<Hex>> continents = getContinentsInProvince(province);
        boolean builtAny = false;

        while (true) {
            // 财政允许且有合理盈余：建完这座港后至少还能保留一个单位的价格
            if (province.money < province.getPortPrice() + GameRules.PRICE_UNIT) break;

            // 覆盖整条海岸线：不限单位 3 格内可达，只要沿海空地、水域不重叠
            Hex portHex = findBestPortHex(province, continents, null);
            if (portHex == null) break;

            if (buildPort(province, portHex)) {
                builtAny = true;
            } else {
                break; // 建港失败（例如财政突变），停止本轮
            }
        }
        return builtAny;
    }


    /**
     * 确保省份至少拥有一个可用港口：
     * 沿海省份还没有港口时优先建造（只要买得起），不要求额外盈余。
     */
    public boolean aiTryToBuildEssentialPort(Province province) {
        if (province == null) return false;
        ArrayList<ArrayList<Hex>> continents = getContinentsInProvince(province);
        boolean builtAny = false;

        // 每个大陆（包括跨海新登录的大陆）都保证至少一个可用港口
        for (ArrayList<Hex> continent : continents) {
            if (continentHasUsablePort(continent, province.getFraction())) continue; // 该大陆已有可用港口
            if (province.money < province.getPortPrice()) break;

            ArrayList<ArrayList<Hex>> singleContinent = new ArrayList<>();
            singleContinent.add(continent);
            // 第一个港口不要求单位 3 格内可达（单位之后会走向沿海），只要求是沿海空地；树格永远不用
            Hex portHex = findBestPortHex(province, singleContinent, null);
            if (portHex == null) continue;
            if (buildPort(province, portHex)) builtAny = true;
        }
        return builtAny;
    }


    /**
     * 在所有大陆中挑选下一个港口位置：
     * 优先东西南北向的沿海地块，其次斜向；同优先级优先靠近首都。
     */
    private Hex findBestPortHex(Province province, ArrayList<ArrayList<Hex>> continents, HashSet<Hex> reachableLand) {
        int fraction = province.getFraction();
        Hex capital = province.getCapital();
        Hex best = null;
        int bestPriority = Integer.MAX_VALUE;
        double bestDist = Double.MAX_VALUE;

        for (ArrayList<Hex> continent : continents) {
            for (Hex hex : continent) {
                if (hex.sea) continue;
                if (hex.containsUnit() || hex.containsBuilding()) continue;
                if (hex.containsTree()) continue; // 永远不允许使用树格
                if (reachableLand != null && !reachableLand.contains(hex)) continue; // 扩展港口必须建在单位可达区域
                if (!hasSeaNeighbor(hex)) continue;
                if (isPortTooClose(hex, fraction)) continue; // 港口之间保持松散间隔

                int priority = getPortDirectionPriority(hex);
                double dist = capital == null ? 0 : hex.pos.fastDistanceTo(capital.pos);
                if (priority < bestPriority || (priority == bestPriority && dist < bestDist)) {
                    bestPriority = priority;
                    bestDist = dist;
                    best = hex;
                }
            }
        }
        return best;
    }


    /**
     * 沿海地块的方向优先级：
     * 海邻居包含东西南北（方向 0/1/3/4）为 0（优先建港），
     * 只有斜向（方向 2/5）为 1（其次）。
     */
    private int getPortDirectionPriority(Hex hex) {
        for (int dir = 0; dir < 6; dir++) {
            Hex adj = hex.getAdjacentHex(dir);
            if (adj == null || adj.isNullHex()) continue;
            if (!adj.active || !adj.sea) continue;
            if (dir == 0 || dir == 1 || dir == 3 || dir == 4) return 0;
        }
        return 1;
    }


    /**
     * 港口之间保持松散间隔：候选格两圈范围内不允许存在己方港口，
     * 避免港口一个挨一个、海域过度重叠。
     */
    private boolean isPortTooClose(Hex hex, int fraction) {
        for (int dir = 0; dir < 6; dir++) {
            Hex adj = hex.getAdjacentHex(dir);
            if (adj == null || adj.isNullHex()) continue;
            if (adj.containsPort() && adj.fraction == fraction) return true;
            for (int dir2 = 0; dir2 < 6; dir2++) {
                Hex adj2 = adj.getAdjacentHex(dir2);
                if (adj2 == null || adj2.isNullHex()) continue;
                if (adj2.containsPort() && adj2.fraction == fraction) return true;
            }
        }
        return false;
    }


    /**
     * 大陆上是否存在"可用"的己方港口：
     * 港口必须仍然覆盖着至少一格属于自己的水域
     * 且水域旁有己方空地落点（被农场/塔等围死则不可用，需要另建）。
     */
    private boolean continentHasUsablePort(ArrayList<Hex> continent, int fraction) {
        for (Hex hex : continent) {
            if (!hex.containsPort() || hex.fraction != fraction) continue;
            for (int i = 0; i < 6; i++) {
                Hex adjHex = hex.getAdjacentHex(i);
                if (adjHex == null || adjHex.isNullHex()) continue;
                if (!adjHex.active || !adjHex.sea) continue;
                if (!adjHex.inPortRange || adjHex.portOwnerFraction != fraction) continue;
                // 水域必须仍有单位可以落脚的己方空地，否则判定不可用（被农场/塔等挡路）
                if (portWaterHasFreeLanding(adjHex, fraction)) return true;
            }
        }
        return false;
    }


    /**
     * 港口覆盖的水域旁是否有己方空地（单位可以从这里进入水域）。
     * 农场、防御塔、山地、港口本身等建筑占据的格子不算落点。
     */
    private boolean portWaterHasFreeLanding(Hex water, int fraction) {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = water.getAdjacentHex(i);
            if (adjHex == null || adjHex.isNullHex()) continue;
            if (!adjHex.active || adjHex.isSea()) continue;
            if (adjHex.fraction != fraction) continue;
            if (adjHex.containsBuilding() || adjHex.containsUnit()) continue;
            return true;
        }
        return false;
    }


    /**
     * 己方陆格是否紧邻己方港口水域（该格是单位下海的落点，应保留空地）。
     */
    public boolean isAdjacentToOwnPortWater(Hex hex, int fraction) {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = hex.getAdjacentHex(i);
            if (adjHex == null || adjHex.isNullHex()) continue;
            if (!adjHex.active || !adjHex.sea) continue;
            if (adjHex.inPortRange && adjHex.portOwnerFraction == fraction) return true;
        }
        return false;
    }


    /**
     * 省份是否已有至少一个可用港口（水域未被敌方覆盖）。
     */
    public boolean provinceHasUsablePort(Province province) {
        if (province == null) return false;
        return provinceHasUsablePort(province, getContinentsInProvince(province));
    }


    /**
     * 省份是否横跨多个大陆（登录扩张状态：海军已在新大陆建立桥头堡）。
     */
    public boolean provinceHasMultipleContinents(Province province) {
        if (province == null) return false;
        return getContinentsInProvince(province).size() >= 2;
    }


    /**
     * 本大陆是否存在接壤的弱势力（己方收入至少是对方的两倍）。
     * 存在时应优先在陆地上摧毁该势力，而不是把兵力投入下海。
     */
    public boolean hasWeakEnemyOnContinent(Province province) {
        if (province == null) return false;
        if (province.hexList.size() == 0) return false;
        double myIncome = province.getIncome();
        int myFraction = province.getFraction();

        for (ArrayList<Hex> continent : getContinentsInProvince(province)) {
            for (Hex hex : continent) {
                for (int dir = 0; dir < 6; dir++) {
                    Hex adj = hex.getAdjacentHex(dir);
                    if (adj == null || adj.isNullHex()) continue;
                    if (!adj.active || adj.sea) continue;
                    if (adj.fraction == myFraction) continue;
                    if (adj.fraction == GameRules.NEUTRAL_FRACTION) continue;
                    Province enemyProvince = getProvinceByHex(adj);
                    if (enemyProvince == null) continue;
                    if (enemyProvince.getFraction() != adj.fraction) continue;
                    if (myIncome >= enemyProvince.getIncome() * 2) return true;
                }
            }
        }
        return false;
    }


    private boolean provinceHasUsablePort(Province province, ArrayList<ArrayList<Hex>> continents) {
        for (ArrayList<Hex> continent : continents) {
            if (continentHasUsablePort(continent, province.getFraction())) return true;
        }
        return false;
    }


    /**
     * 判断港口可用性的可达区域：
     * 把每个己方单位所在格和每个农场周围的己方空地格都视作单位位置，
     * 从这些位置出发按最大移动距离（UNIT_MOVE_LIMIT）扩散，
     * 覆盖范围内可以抵达港口周围水域；超出范围视为不可达。
     */
    public HashSet<Hex> getReachableLandFromUnits(Province province) {
        HashSet<Hex> visited = new HashSet<>();
        ArrayList<Hex> queue = new ArrayList<>();
        int moveLimit = GameRules.UNIT_MOVE_LIMIT;

        // 起点：己方单位所在格
        for (Hex hex : province.hexList) {
            if (hex.isSea() || hex.fraction != province.getFraction()) continue;
            if (!hex.containsUnit()) continue;
            if (visited.add(hex)) queue.add(hex);
        }

        // 起点：每个己方农场周围的己方空地格（单位可能从那里出发去下海）
        for (Hex hex : province.hexList) {
            if (hex.isSea() || hex.fraction != province.getFraction()) continue;
            if (hex.objectInside != Obj.FARM) continue;
            for (int dir = 0; dir < 6; dir++) {
                Hex adj = hex.getAdjacentHex(dir);
                if (adj == null || adj.isNullHex()) continue;
                if (!adj.active || adj.isSea()) continue;
                if (adj.fraction != province.getFraction()) continue;
                if (adj.containsBuilding() || adj.containsUnit()) continue; // 起点必须是可站立的空地
                if (visited.add(adj)) queue.add(adj);
            }
        }

        // BFS 沿己方陆地扩散（机制上可以穿过建筑），限制最大移动距离
        int distance = 0;
        int index = 0;
        while (index < queue.size()) {
            if (distance >= moveLimit) break;
            int layerEnd = queue.size();
            for (; index < layerEnd; index++) {
                Hex hex = queue.get(index);
                for (int dir = 0; dir < 6; dir++) {
                    Hex adj = hex.getAdjacentHex(dir);
                    if (adj == null || adj.isNullHex()) continue;
                    if (!adj.active || adj.isSea()) continue;
                    if (adj.fraction != province.getFraction()) continue;
                    if (visited.contains(adj)) continue;
                    visited.add(adj);
                    queue.add(adj);
                }
            }
            distance++;
        }
        return visited;
    }


    // a continent is a connected group of at least two land hexes
    public ArrayList<ArrayList<Hex>> getContinentsInProvince(Province province) {
        ArrayList<ArrayList<Hex>> continents = new ArrayList<>();
        ArrayList<Hex> tempList = new ArrayList<>();
        ArrayList<Hex> propagationList = new ArrayList<>();
        HashSet<Hex> visited = new HashSet<>();
        HashSet<Hex> provinceSet = new HashSet<>(province.hexList);

        for (Hex startHex : province.hexList) {
            if (startHex.sea) continue;
            if (visited.contains(startHex)) continue;

            tempList.clear();
            propagationList.clear();
            propagationList.add(startHex);
            visited.add(startHex);

            int index = 0;
            while (index < propagationList.size()) {
                Hex hex = propagationList.get(index);
                index++;
                tempList.add(hex);
                for (int dir = 0; dir < 6; dir++) {
                    Hex adjHex = hex.getAdjacentHex(dir);
                    if (adjHex == null || adjHex.isNullHex()) continue;
                    if (!adjHex.active || adjHex.sea) continue;
                    if (visited.contains(adjHex)) continue;
                    if (!provinceSet.contains(adjHex)) continue;
                    visited.add(adjHex);
                    propagationList.add(adjHex);
                }
            }

            if (tempList.size() >= 2) {
                continents.add(new ArrayList<>(tempList));
            }
        }
        return continents;
    }


    public boolean isContinent(ArrayList<Hex> hexes) {
        if (hexes == null) return false;
        if (hexes.size() < 2) return false;
        for (Hex hex : hexes) {
            if (hex.sea) return false;
        }

        ArrayList<Hex> tempList = new ArrayList<>();
        ArrayList<Hex> propagationList = new ArrayList<>();
        HashSet<Hex> visited = new HashSet<>();
        Hex firstHex = hexes.get(0);
        propagationList.add(firstHex);
        visited.add(firstHex);
        while (propagationList.size() > 0) {
            Hex hex = propagationList.remove(0);
            tempList.add(hex);
            for (int dir = 0; dir < 6; dir++) {
                Hex adjHex = hex.getAdjacentHex(dir);
                if (adjHex == null || adjHex.isNullHex()) continue;
                if (!adjHex.active || adjHex.sea) continue;
                if (visited.contains(adjHex)) continue;
                if (!hexes.contains(adjHex)) continue;
                visited.add(adjHex);
                propagationList.add(adjHex);
            }
        }

        return tempList.size() == hexes.size();
    }


    public boolean isProvinceContinentSecured(Province province) {
        for (ArrayList<Hex> continent : getContinentsInProvince(province)) {
            boolean secured = true;
            for (Hex hex : continent) {
                if (hex.isNeutral()) continue;
                if (hex.sameFraction(province)) continue;
                secured = false;
                break;
            }
            if (secured) return true; // at least one fully owned continent
        }
        return false;
    }


    private boolean portRangeOverlaps(Hex hex, int fraction) {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = hex.getAdjacentHex(i);
            if (adjHex == null || adjHex.isNullHex()) continue;
            if (!adjHex.active || !adjHex.sea) continue;
            if (adjHex.inPortRange && adjHex.portOwnerFraction == fraction) return true; // own water already covered by another port
        }
        return false;
    }


    public void updateAllPortRanges() {
        for (Hex hex : activeHexes) {
            hex.inPortRange = false;
            hex.portOwnerFraction = -1;
        }
        for (Hex hex : activeHexes) {
            if (!hex.containsPort()) continue;
            markPortRange(hex, true);
        }
    }


    public void updateLandStatistics() {
        // 防御：清理失去所有地块的省份，避免空省份引发崩溃
        for (int i = provinces.size() - 1; i >= 0; i--) {
            Province province = provinces.get(i);
            if (province.hexList.size() > 0) continue;
            if (selectedProvince == province) selectedProvince = null;
            provinces.remove(i);
        }

        landHexesQuantity = 0;
        for (Hex hex : activeHexes) {
            if (hex.sea) continue;
            landHexesQuantity++;
        }

        fractionLandHexes = new int[GameRules.MAX_FRACTIONS_QUANTITY];
        for (Province province : provinces) {
            int fraction = province.getFraction();
            if (fraction < 0 || fraction >= fractionLandHexes.length) continue;
            fractionLandHexes[fraction] += province.getLandHexesCount();
        }

        updateContinentsStatistics();
    }


    /**
     * 统计每个大陆（陆地连通块，至少 2 格）的地块数量，
     * 以及各势力在每个大陆上的格子数量，并据此计算头衔：
     * 势力在任意一个大陆上达到比例阈值即晋升头衔。
     */
    private void updateContinentsStatistics() {
        continents.clear();
        continentTotalLand.clear();
        continentFractionLand.clear();

        HashSet<Hex> visited = new HashSet<>();
        for (Hex hex : activeHexes) {
            if (hex.sea) continue;
            if (visited.contains(hex)) continue;

            ArrayList<Hex> continent = new ArrayList<>();
            ArrayList<Hex> queue = new ArrayList<>();
            queue.add(hex);
            visited.add(hex);
            int index = 0;
            while (index < queue.size()) {
                Hex h = queue.get(index);
                index++;
                continent.add(h);
                for (int dir = 0; dir < 6; dir++) {
                    Hex adj = h.getAdjacentHex(dir);
                    if (adj == null || adj.isNullHex()) continue;
                    if (!adj.active || adj.sea) continue;
                    if (visited.contains(adj)) continue;
                    visited.add(adj);
                    queue.add(adj);
                }
            }

            if (continent.size() < 2) continue; // 单格孤岛不算大陆

            int[] fractionCounts = new int[GameRules.MAX_FRACTIONS_QUANTITY];
            for (Hex h : continent) {
                if (h.fraction >= 0 && h.fraction < fractionCounts.length) {
                    fractionCounts[h.fraction]++;
                }
            }
            continents.add(continent);
            continentTotalLand.add(continent.size());
            continentFractionLand.add(fractionCounts);
        }

        fractionTitles = new String[GameRules.MAX_FRACTIONS_QUANTITY];
        for (int f = 0; f < fractionTitles.length; f++) {
            fractionTitles[f] = computeTitleForFraction(f);
        }
    }


    private String computeTitleForFraction(int fraction) {
        double bestRatio = 0;
        boolean canBeEmpire = false;
        for (int i = 0; i < continents.size(); i++) {
            int total = continentTotalLand.get(i);
            if (total <= 0) continue;
            int count = continentFractionLand.get(i)[fraction];
            double ratio = (double) count / total;
            // 称帝要求：至少在一个大陆拥有超过 8 个地块且达到帝国比例
            if (count > 8 && ratio >= 0.34) canBeEmpire = true;
            if (ratio > bestRatio) bestRatio = ratio;
        }
        if (bestRatio <= 0) return "";
        String title = getTitleKeyByRatio(bestRatio);
        if (title.equals("title_empire") && !canBeEmpire) {
            return "title_kingdom"; // 占领的地块不够多，不能称帝
        }
        return title;
    }


    public String getProvinceTitle(int fraction) {
        if (!SettingsManager.dynamicTitlesEnabled) return "";
        if (fraction < 0 || fraction >= fractionTitles.length) return "";
        String title = fractionTitles[fraction];
        return title == null ? "" : title;
    }


    private String getTitleKeyByRatio(double ratio) {
        if (ratio >= 0.34) return "title_empire";
        if (ratio >= 0.24) return "title_kingdom";
        if (ratio >= 0.16) return "title_duchy";
        if (ratio >= 0.09) return "title_marquis";
        if (ratio >= 0.02) return "title_county";
        return "title_city";
    }


    private void markPortRange(Hex portHex, boolean value) {
        for (int i = 0; i < 6; i++) {
            Hex adjHex = portHex.getAdjacentHex(i);
            if (adjHex == null || adjHex.isNullHex()) continue;
            if (!adjHex.active || !adjHex.sea) continue;
            adjHex.inPortRange = value;
            adjHex.portOwnerFraction = value ? portHex.fraction : -1;
        }
    }


    public boolean buildTree(Province province, Hex hex) {
        if (province == null) return false;
        if (hex.sea) return false;
        if (province.hasMoneyForTree()) {
            gameController.takeSnapshot();
            spawnTree(hex);
            addAnimHex(hex);
            province.money -= GameRules.PRICE_TREE;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_TREE);
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build tree
        tickleMoneySign();
        return false;
    }

    public Unit addUnit(Hex hex, int strength) {
        if (hex == null) return null;
        if (hex.containsObject()) {
            gameController.ruleset.onUnitAdd(hex);
            cleanOutHex(hex);
            gameController.updateCacheOnceAfterSomeTime();
            hex.addUnit(strength);
        } else {
            hex.addUnit(strength);
            checkToPrepareNewlyAddedUnitForMovement(hex.unit);
        }
        return hex.unit;
    }


    private void checkToPrepareNewlyAddedUnitForMovement(Unit unit) {
        if (!gameController.isUnitValidForMovement(unit)) return;
        unit.setReadyToMove(true);
        unit.startJumping();
    }


    public void addProvince(Province province) {
        if (provinces.contains(province)) return;
        if (containsEqualProvince(province)) {
            System.out.println("Problem in FieldController.addProvince()");
            System.out.println(province);
            Yio.printStackTrace();
            Province existing = getEqualProvince(province);
            if (existing != null) {
                // make sure every hex is owned by the existing province, so no hex is left ownerless
                for (Hex hex : new ArrayList<>(province.hexList)) {
                    if (!existing.containsHex(hex)) {
                        addHexToProvince(existing, hex);
                    }
                }
            }
            province.hexList.clear();
            return;
        }

        provinces.add(province);
    }


    private Province getEqualProvince(Province province) {
        for (Province p : provinces) {
            if (p.equals(province)) return p;
        }
        return null;
    }


    public boolean containsEqualProvince(Province province) {
        for (Province p : provinces) {
            if (p.equals(province)) {
                return true;
            }
        }

        return false;
    }


    public Hex adjacentHex(Hex hex, int direction) {
        return adjacentHex(hex.index1, hex.index2, direction);
    }


    public boolean hexHasSelectedNearby(Hex hex) {
        for (int i = 0; i < 6; i++)
            if (hex.getAdjacentHex(i).selected) return true;
        return false;
    }


    public static float distanceBetweenHexes(Hex one, Hex two) {
        PointYio pOne = one.getPos();
        PointYio pTwo = two.getPos();
        return (float) pOne.distanceTo(pTwo);
    }


    public boolean isSomethingSelected() {
        return selectedHexes.size() > 0;
    }


    public void giveMoneyToPlayerProvinces(int amount) {
        for (Province province : provinces) {
            if (province.getFraction() == 0) {
                province.money += amount;
            }
        }
    }


    public boolean hexHasNeighbourWithFraction(Hex hex, int fraction) {
        Hex neighbour;
        for (int i = 0; i < 6; i++) {
            neighbour = hex.getAdjacentHex(i);
            if (neighbour != null && neighbour.active && neighbour.sameFraction(fraction)) return true;
        }
        return false;
    }


    public void addAnimHex(Hex hex) {
        if (animHexes.contains(hex)) return;
        if (DebugFlags.testMode) return;

        animHexes.listIterator().add(hex);

        hex.animFactor.setValues(0, 0);
        hex.animFactor.appear(1, 1);
        hex.animStartTime = System.currentTimeMillis();

        gameController.updateCacheOnceAfterSomeTime();
    }


    public Province findProvinceCopy(Province src) {
        Province result;
        for (Hex hex : src.hexList) {
            result = getProvinceByHex(hex);
            if (result == null) continue;
            return result;
        }
        return null;
    }


    public Province findProvince(int fraction) {
        for (Province province : provinces) {
            if (province.getFraction() != fraction) continue;
            return province;
        }

        return null;
    }


    public Province getRandomProvince() {
        int index = YioGdxGame.random.nextInt(provinces.size());
        return provinces.get(index);
    }


    public void checkToFocusCameraOnCurrentPlayer() {
        if (!gameController.isInMultiplayerMode()) return;
        if (!gameController.isPlayerTurn()) return;

        Province province = findProvince(gameController.turn);
        if (province == null) return;

        province.focusCameraOnThis();
    }


    public Province getBiggestProvince(int fraction) {
        Province bestProvince = null;
        for (Province province : provinces) {
            if (province.getFraction() != fraction) continue;
            if (bestProvince == null || province.hexList.size() > bestProvince.hexList.size()) {
                bestProvince = province;
            }
        }
        return bestProvince;
    }


    public boolean isOnlyOneFractionAlive(int fraction) {
        boolean detected = false;
        for (Province province : provinces) {
            if (province.getFraction() != fraction) return false;
            detected = true;
        }
        return detected;
    }


    public Province getProvinceByHex(Hex hex) {
        for (Province province : provinces) {
            if (!province.containsHex(hex)) continue;
            return province;
        }

        return null;
    }


    public Hex getRandomActiveHex() {
        int index = YioGdxGame.random.nextInt(activeHexes.size());
        return activeHexes.get(index);
    }


    public Province getMaxProvinceFromList(ArrayList<Province> list) {
        if (list.size() == 0) return null;
        Province max, temp;
        max = list.get(0);
        for (int k = list.size() - 1; k >= 0; k--) {
            temp = list.get(k);
            if (temp.hexList.size() > max.hexList.size()) max = temp;
        }
        return max;
    }


    public void splitProvince(Hex hex, int fraction, int previousObject) {
        Province oldProvince = getProvinceByHex(hex);
        if (oldProvince == null) return;
        MoveZoneDetection.unFlagAllHexesInArrayList(oldProvince.hexList);
        tempList.clear();
        propagationList.clear();
        ArrayList<Province> provincesAdded = new ArrayList<>();
        Hex startHex, tempHex, adjHex;
        hex.flag = true;
        gameController.getPredictableRandom().setSeed(hex.index1 + hex.index2);
        for (int dir = 0; dir < 6; dir++) {
            startHex = hex.getAdjacentHex(dir);
            if (!startHex.active || startHex.fraction != fraction || startHex.flag) continue;
            if (!oldProvince.containsHex(startHex)) continue; // don't absorb neighbouring provinces
            tempList.clear();
            propagationList.clear();
            propagationList.add(startHex);
            startHex.flag = true;
            while (propagationList.size() > 0) {
                tempHex = propagationList.get(0);
                //if(tempHex.containsSiege()){continue;}
                tempList.add(tempHex);
                propagationList.remove(0);
                for (int i = 0; i < 6; i++) {
                    adjHex = tempHex.getAdjacentHex(i);
                    if (adjHex.active && adjHex.sameFraction(tempHex) && !adjHex.flag && oldProvince.containsHex(adjHex)) {
                        propagationList.add(adjHex);
                        adjHex.flag = true;
                    }
                }
            }
            if (tempList.size() >= 2) {
                Province province = new Province(gameController, tempList);
                province.money = 0;
                if (!province.hasCapital()) {
                    if(GameRules.captainAsCityRules) {
                        province.placeCapitalInRandomCity(gameController.getPredictableRandom());
                        gameController.namingManager.checkForCapitalRelocate(previousObject, hex, province);
                    }else{
                        province.placeCapitalInRandomPlace(gameController.getPredictableRandom());
                        gameController.namingManager.checkForCapitalRelocate(previousObject, hex, province);
                    }
                }
                if (GameRules.keepDifferentProvinceNames) {
                    province.setName(oldProvince.getName());
                }
                addProvince(province);
                provincesAdded.add(province);
            } else {
                destroyBuildingsOnHex(startHex);
            }
        }
        if (provincesAdded.size() > 0 && !(hex.objectInside == Obj.TOWN)) {
            getMaxProvinceFromList(provincesAdded).money = oldProvince.money;
        }
        removeProvince(oldProvince);
        oldProvince.hexList.clear(); // prevent stale references from holding onto the same hexes
        diplomacyManager.updateEntityAliveStatus(fraction);
    }


    public void checkToUniteProvinces(Hex hex) {
        ArrayList<Province> adjacentProvinces = new ArrayList<Province>();
        Province p;
        for (int i = 0; i < 6; i++) {
            p = getProvinceByHex(hex.getAdjacentHex(i));
            if (p != null && hex.sameFraction(p) && !adjacentProvinces.contains(p)) adjacentProvinces.add(p);
        }
        if (adjacentProvinces.size() >= 2) {
            if (GameRules.keepDifferentProvinceNames) {
                String firstName = adjacentProvinces.get(0).getName();
                for (Province province : adjacentProvinces) {
                    if (!province.getName().equals(firstName)) return;
                }
            }
            int sum = 0;
            Hex capital = getMaxProvinceFromList(adjacentProvinces).getCapital();
            ArrayList<Hex> hexArrayList = new ArrayList<Hex>();
//            YioGdxGame.say("uniting provinces: " + adjacentProvinces.size());
            for (Province province : adjacentProvinces) {
                sum += province.money;
                hexArrayList.addAll(province.hexList);
                removeProvince(province);
                province.hexList.clear(); // prevent stale references from holding onto the same hexes
            }
            Province unitedProvince = new Province(gameController, hexArrayList);
            unitedProvince.money = sum;
            unitedProvince.setCapital(capital);
            addProvince(unitedProvince);
        }
    }


    private void removeProvince(Province province) {
        provinces.remove(province);
        if (selectedProvince == province) {
            selectedProvince = null; // don't keep a stale reference to a removed province
        }
    }


    public void checkToNeutralizeAllSeaProvinces() {
        ArrayList<Province> seaProvinces = new ArrayList<>();
        for (Province province : provinces) {
            if (!province.isAllSea()) continue;
            seaProvinces.add(province);
        }

        if (seaProvinces.size() == 0) return;

        for (Province province : seaProvinces) {
            Province conqueror = getAdjacentNonSeaProvince(province, gameController.turn);
            if (conqueror != null) {
                // a faction conquered this province and turned it into water:
                // hand the province over to that faction instead of neutralizing it
                ArrayList<Hex> hexesToHandOver = new ArrayList<>(province.hexList);
                for (Hex hex : hexesToHandOver) {
                    if (hex.containsUnit()) {
                        killUnitByStarvation(hex); // 被吞并海域困死的舰队直接变墓地，不移交给新阵营
                    }
                    if (hex.isNeutral()) continue;
                    hex.setFraction(conqueror.getFraction());
                    addHexToProvince(conqueror, hex);
                }
                province.hexList.clear();
                removeProvince(province);
                continue;
            }

            // no neighbouring faction to take it over: kill the units (they become
            // graves), neutralize the hexes, dissolve the province
            for (Hex hex : province.hexList) {
                if (hex.containsUnit()) {
                    killUnitByStarvation(hex);
                }
                if (hex.isNeutral()) continue;
                hex.setFraction(GameRules.NEUTRAL_FRACTION);
            }
            province.hexList.clear();
            removeProvince(province);
        }

        // stray sea hexes that don't belong to any province are also restored to neutral
        for (Hex hex : activeHexes) {
            if (!hex.sea) continue;
            if (hex.isNeutral()) continue;
            if (getProvinceByHex(hex) != null) continue;

            Province adjacent = getAdjacentNonSeaProvinceForHex(hex, gameController.turn);
            if (adjacent != null) {
                hex.setFraction(adjacent.getFraction());
                addHexToProvince(adjacent, hex);
                continue;
            }

            if (hex.containsUnit()) {
                killUnitByStarvation(hex); // kill the unit, it becomes a grave
            }
            cleanOutHex(hex);
            hex.setFraction(GameRules.NEUTRAL_FRACTION);
        }
    }


    private Province getAdjacentNonSeaProvinceForHex(Hex hex, int fraction) {
        Province sameFactionResult = null;
        for (int dir = 0; dir < 6; dir++) {
            Hex adjHex = hex.getAdjacentHex(dir);
            if (adjHex == null || adjHex.isNullHex()) continue;
            Province adjacent = getProvinceByHex(adjHex);
            if (adjacent == null) continue;
            if (adjacent.getFraction() != fraction) continue;
            if (adjacent.isAllSea()) continue;
            if (sameFactionResult == null || adjacent.hexList.size() > sameFactionResult.hexList.size()) {
                sameFactionResult = adjacent;
            }
        }
        return sameFactionResult;
    }


    private Province getAdjacentNonSeaProvince(Province province, int fraction) {
        Province sameFactionResult = null;
        for (Hex hex : province.hexList) {
            for (int dir = 0; dir < 6; dir++) {
                Hex adjHex = hex.getAdjacentHex(dir);
                if (adjHex == null || adjHex.isNullHex()) continue;
                Province adjacent = getProvinceByHex(adjHex);
                if (adjacent == null || adjacent == province) continue;
                if (adjacent.getFraction() != fraction) continue;
                if (adjacent.isAllSea()) continue;
                if (sameFactionResult == null || adjacent.hexList.size() > sameFactionResult.hexList.size()) {
                    sameFactionResult = adjacent;
                }
            }
        }
        return sameFactionResult;
    }


    public void joinHexToAdjacentProvince(Hex hex) {
        Province p;
        for (int i = 0; i < 6; i++) {
            p = getProvinceByHex(hex.getAdjacentHex(i));
            if (p != null && hex.sameFraction(p)) {
                addHexToProvince(p, hex);
                Hex h;
                for (int j = 0; j < 6; j++) {
                    h = adjacentHex(hex, j);
                    if (h.active && h.sameFraction(hex) && getProvinceByHex(h) == null) p.addHex(h);
                }
                return;
            }
        }
    }


    public void addHexToProvince(Province province, Hex hex) {
        if (province == null || hex == null) return;
        for (Province p : provinces) {
            if (p == province) continue;
            p.removeHex(hex);
        }
        province.addHex(hex);
    }


    public void updatePointByHexIndexes(PointYio pointYio, int index1, int index2) {
        pointYio.x = fieldPos.x + hexStep2 * index2 * sin60;
        pointYio.y = fieldPos.y + hexStep1 * index1 + hexStep2 * index2 * cos60;
    }


    public void setHexFraction(Hex hex, int fraction) {
        int previousObject = hex.objectInside;
        if(!hex.containsSiege()) {
            cleanOutHex(hex);
        }
        int previousFraction = hex.fraction;
        hex.setFraction(fraction);
        if (hex.containsPort()) {
            markPortRange(hex, true); // update port water ownership after capture
        }
        splitProvince(hex, previousFraction, previousObject);
        checkToUniteProvinces(hex);
        joinHexToAdjacentProvince(hex);
        ListIterator animIterator = animHexes.listIterator();

        for (int dir = 0; dir < 6; dir++) {
            Hex adj = hex.getAdjacentHex(dir);
            if (adj != null && adj.active && adj.sameFraction(hex)) {
                if (!animHexes.contains(adj)) {
                    animIterator.add(adj);
                }
                if (!adj.changingFraction) {
                    adj.animFactor.setValues(1, 0);
                }
            }
        }
        hex.changingFraction = true;
        if (!animHexes.contains(hex)) animIterator.add(hex);
        hex.animFactor.setValues(0, 0);
        hex.animFactor.appear(1, 1);

        if (!gameController.isPlayerTurn()) {
            forceAnimEndInHex(hex);
        }
    }


    public void updateFocusedHex() {
        updateFocusedHex(gameController.touchPoint.x, gameController.touchPoint.y);
    }


    public void updateFocusedHex(float screenX, float screenY) {
        OrthographicCamera orthoCam = gameController.cameraController.orthoCam;
        SelectionManager selectionManager = gameController.selectionManager;

        selectionManager.selectX = (screenX - 0.5f * GraphicsYio.width) * orthoCam.zoom + orthoCam.position.x;
        selectionManager.selectY = (screenY - 0.5f * GraphicsYio.height) * orthoCam.zoom + orthoCam.position.y;
        gameController.convertedTouchPoint.set(selectionManager.selectX, selectionManager.selectY);

        GameView gameView = gameController.getYioGdxGame().gameView;
        float x = selectionManager.selectX + gameView.hexViewSize;
        float y = selectionManager.selectY + gameView.hexViewSize;

        focusedHex = getHexByPos(x, y);
    }


    public boolean isAtLeastOneCurrentFractionProvinceAlive() {
        for (Province province : provinces) {
            if (province.getFraction() != gameController.turn) continue;
            if (province.hexList.size() == 0) continue;
            return true;
        }
        return false;
    }

    public Hex findBestWayToHex(Hex startHex,Hex endHex,Unit unit){
        int width=Math.abs(startHex.index1-endHex.index1);
        int height=Math.abs(startHex.index2-endHex.index2);
        int hypotenuse = (int)Math.sqrt(width*width+height*height);
        int moveFix=0,moveResult;
        int x=0,y=0,hypotenuseEvery=0;

        if(unit.strength>=6 && unit.strength != 10){
            if(unit.strength==8){
                moveFix=2;
            }
            else if(unit.strength==9){
                moveFix=2;
            }
            else{
                moveFix=-1;
            }
        }
        if(unit.currentHex.isSea()){
            moveFix=moveFix+2;
        }
        moveResult=GameRules.UNIT_MOVE_LIMIT+moveFix;

        if(moveResult>hypotenuse){
            for(int i=0;i<=hypotenuse;i++){
                x++;
                y++;
                hypotenuseEvery=hypotenuseEvery+(int)Math.sqrt(x*x+y*y);
                if(hypotenuseEvery>=moveResult){
                    if(startHex.index1<=endHex.index1) x=-x;
                    if(startHex.index2<=endHex.index2) y=-y;
                    return getHex(startHex.index1+x,startHex.index2+y);
                }
            }
        }else if(moveResult<hypotenuse){
            for(int i=0;i<=moveResult;i++){
                x++;
                y++;
            }
            if(startHex.index1<=endHex.index1) x=-x;
            if(startHex.index2<=endHex.index2) y=-y;
            return getHex(startHex.index1+x,startHex.index2+y);
        }
        return endHex;
    }

    public boolean moveUnitToBorder(Hex focusedHex){
        updateFocusedHex();
        if (focusedHex != null && focusedHex.active) {
            marchUnitsToHex(focusedHex);
            return true;
        }
        return false;
    }


    @Override
    public String encode() {
        StringBuilder builder = new StringBuilder();
        for (Hex activeHex : activeHexes) {
            builder.append(activeHex.encode()).append(",");
        }
        return builder.toString();
    }


    @Override
    public void decode(String source) {
        for (String token : source.split(",")) {
            String[] split = token.split(" ");
            int index1 = Integer.valueOf(split[0]);
            int index2 = Integer.valueOf(split[1]);
            try {
                Hex hex = field[index1][index2];
                hex.active = true;
                setHexFraction(hex, Integer.valueOf(split[2]));
                hex.decode(token);
                activeHexes.add(0, hex);
            }catch (ArrayIndexOutOfBoundsException e){
                EncodeManager encodeManager = new EncodeManager(gameController);
                encodeManager.performToClipboard();
                Yio.printStackTrace();
            }

        }
    }
}
