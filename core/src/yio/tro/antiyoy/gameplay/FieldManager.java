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
                intersectedProvince.addHex(hex);
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
            if (h.animFactor.get() > 0.99 && !(h.containsUnit() && h.unit.moveFactor.get() < 1) && System.currentTimeMillis() > h.animStartTime + 250) {
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
        switch (direction) {
            case 0:
                if (i >= fWidth - 1) return nullHex;

                try{
                    return field[i + 1][j];
                }catch (ArrayIndexOutOfBoundsException e){
                    System.out.println(i+","+j);
                }
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
        if (hex.objectInside == type) return;
        if (!hex.canContainObjects) return;

        if (solidObjects.contains(hex)) {
            cleanOutHex(hex);
        }

        hex.setObjectInside(type);
        solidObjects.listIterator().add(hex);
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
        if (hex.containsUnit()) {
            gameController.getMatchStatistics().onUnitKilled();
            gameController.getUnitList().remove(hex.unit);
            hex.unit = null;
        }
        hex.setObjectInside(0);
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

    public void archerKill() {
        Hex temp, temp2;
        boolean attacked = false;
        for (Hex hex : activeHexes) {
            if (hex.hasUnit()) {
                if(hex.unit.strength==5){
                    for (int m = 0; m < 6; m++) {
                        if(attacked){
                            break;
                        }
                        temp = hex.getAdjacentHex(m);
                        if(temp.index1<0 || temp.index2<0){
                            continue;
                        }
                        for (int n = 0; n < 6; n++) {
                            if (attacked){
                                break;
                            }
                            temp2 = temp.getAdjacentHex(n);
                            if(temp2.index1<0 || temp2.index2<0){
                                continue;
                            }
                            if(temp2.active && temp2.hasUnit() && !(temp2.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp2) && gameController.canUnitAttackHex(2,hex.fraction,temp) && gameController.predictableRandom.nextInt(10)>=4){
                                cleanOutHex(temp2);
                                addSolidObject(temp2,Obj.GRAVE);
                                attacked=true;
                                break;
                            }
                        }

                        if(temp.active && temp.hasUnit() && !(temp.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp) && gameController.canUnitAttackHex(2,hex.fraction,temp) && gameController.predictableRandom.nextInt(10)>=2){
                            cleanOutHex(temp);
                            addSolidObject(temp,Obj.GRAVE);
                            attacked=true;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void crossbowKill() {
        Hex temp, temp2, temp3;
        int attacked = 0;
        for (Hex hex : activeHexes) {
            if (hex.hasUnit()) {
                if(hex.unit.strength==7){
                    for (int m = 0; m < 6; m++) {
                        if(attacked==3){
                            break;
                        }
                        temp = hex.getAdjacentHex(m);
                        if(temp.index1<0 || temp.index2<0){
                            continue;
                        }
                        for (int n = 0; n < 6; n++) {
                            if (attacked==3){
                                break;
                            }

                            temp2 = temp.getAdjacentHex(n);
                            if(temp2.index1<0 || temp2.index2<0){
                                continue;
                            }
                            if(temp2.active && temp2.hasUnit() && !(temp2.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp2)&& gameController.canUnitAttackHex(2,hex.fraction,temp2) && Math.random()>=0.6){
                                cleanOutHex(temp2);
                                addSolidObject(temp2,Obj.GRAVE);
                                attacked+=1;
                                break;
                            }
                            for (int b = 0; b < 6; b++) {
                                if (attacked==3){
                                    break;
                                }
                                temp3 = temp2.getAdjacentHex(n);
                                if(temp3.index1<0 || temp3.index2<0){
                                    continue;
                                }
                                if(temp3.active && temp3.hasUnit() && !(temp3.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp3)&&gameController.canUnitAttackHex(2,hex.fraction,temp3)&& Math.random()>=0.8){
                                    cleanOutHex(temp3);
                                    addSolidObject(temp3,Obj.GRAVE);
                                    attacked+=1;
                                    break;
                                }
                            }
                        }

                        if(temp.active && temp.hasUnit() && !(temp.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp)&& gameController.canUnitAttackHex(2,hex.fraction,temp) &&Math.random()>=0.4){
                            cleanOutHex(temp);
                            addSolidObject(temp,Obj.GRAVE);
                            attacked+=1;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void gunboatKill() {
        Hex temp, temp2, temp3;
        int attacked = 0;
        int max=(int)(Math.random()*(6-2)+2);
        for (Hex hex : activeHexes) {
            if (hex.hasUnit()) {
                if(hex.unit.strength==6){
                    for (int m = 0; m < 6; m++) {
                        if(attacked==max){
                            break;
                        }
                        temp = hex.getAdjacentHex(m);
                        if(temp.index1<0 || temp.index2<0){
                            continue;
                        }
                        for (int n = 0; n < 6; n++) {
                            if (attacked==max){
                                break;
                            }
                            temp2 = temp.getAdjacentHex(n);
                            if(temp2.index1<0 || temp2.index2<0){
                                continue;
                            }
                            if(temp2.active && temp2.hasUnit() && !(temp2.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp2)&& gameController.canUnitAttackHex(4,hex.fraction,temp2)&& gameController.predictableRandom.nextInt(10)>=3){
                                cleanOutHex(temp2);
                                addSolidObject(temp2,Obj.GRAVE);
                                attacked+=1;
                                break;
                            }
                            if(temp2.active && temp2.containsNormalTower() && !(temp2.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp2)&& gameController.predictableRandom.nextInt(10)>=3){
                                cleanOutHex(temp2);
                                break;
                            }
                            if(temp2.active && temp2.objectInside==Obj.FARM && !(temp2.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp2)&& gameController.predictableRandom.nextInt(10)>=6){
                                cleanOutHex(temp2);
                                break;
                            }
                            for (int b = 0; b < 6; b++) {
                                if (attacked==max){
                                    break;
                                }
                                temp3 = temp2.getAdjacentHex(n);
                                if(temp3.index1<0 || temp3.index2<0){
                                    continue;
                                }
                                if(temp3.active && temp3.hasUnit() && !(temp3.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp3)&& gameController.canUnitAttackHex(4,hex.fraction,temp3)&& gameController.predictableRandom.nextInt(10)>=5){
                                    cleanOutHex(temp3);
                                    addSolidObject(temp3,Obj.GRAVE);
                                    attacked+=1;
                                    break;
                                }
                            }
                        }
                        if(temp.active && temp.containsSiege() && !(temp.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp)&& gameController.predictableRandom.nextInt(10)>=4){
                            temp.setFraction(hex.fraction);
                            temp.changingFraction = true;
                            break;
                        }
                        if(temp.active && temp.hasUnit() && !(temp.fraction==hex.fraction) && diplomacyManager.isWar(hex, temp)&& gameController.canUnitAttackHex(4,hex.fraction,temp) && gameController.predictableRandom.nextInt(10)>=5){
                            cleanOutHex(temp);
                            addSolidObject(temp,Obj.GRAVE);
                            attacked+=1;
                            break;
                        }
                    }
                }
            }
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
        province.addHex(hex);
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
        if (!hex.hasThisSupportiveObjectNearby(Obj.TOWN) && !hex.hasThisSupportiveObjectNearby(Obj.FARM) && !hex.hasThisSupportiveObjectNearby(Obj.CITY)) {
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
            return;
        }

        provinces.add(province);
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
                    if (adjHex.active && adjHex.sameFraction(tempHex) && !adjHex.flag) {
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
            int sum = 0;
            Hex capital = getMaxProvinceFromList(adjacentProvinces).getCapital();
            ArrayList<Hex> hexArrayList = new ArrayList<Hex>();
//            YioGdxGame.say("uniting provinces: " + adjacentProvinces.size());
            for (Province province : adjacentProvinces) {
                sum += province.money;
                hexArrayList.addAll(province.hexList);
                removeProvince(province);
            }
            Province unitedProvince = new Province(gameController, hexArrayList);
            unitedProvince.money = sum;
            unitedProvince.setCapital(capital);
            addProvince(unitedProvince);
        }
    }


    private void removeProvince(Province province) {
        provinces.remove(province);
    }


    public void joinHexToAdjacentProvince(Hex hex) {
        Province p;
        for (int i = 0; i < 6; i++) {
            p = getProvinceByHex(hex.getAdjacentHex(i));
            if (p != null && hex.sameFraction(p)) {
                p.addHex(hex);
                Hex h;
                for (int j = 0; j < 6; j++) {
                    h = adjacentHex(hex, j);
                    if (h.active && h.sameFraction(hex) && getProvinceByHex(h) == null) p.addHex(h);
                }
                return;
            }
        }
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
                moveFix=3;
            }
            else if(unit.strength==9){
                moveFix=3;
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