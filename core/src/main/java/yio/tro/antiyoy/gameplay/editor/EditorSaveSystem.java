package yio.tro.antiyoy.gameplay.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import yio.tro.antiyoy.YioGdxGame;
import yio.tro.antiyoy.ai.Difficulty;
import yio.tro.antiyoy.gameplay.FinishGameManager;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.HexLabel;
import yio.tro.antiyoy.gameplay.loading.LoadingManager;
import yio.tro.antiyoy.gameplay.loading.LoadingParameters;
import yio.tro.antiyoy.gameplay.loading.LoadingType;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.menu.color_picking.ColorHolderElement;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.stuff.LanguagesManager;

import java.util.ArrayList;

public class EditorSaveSystem {

    public static final String EDITOR_PREFS = "editor";
    public static final String SLOT_NAME = "slot";
    GameController gameController;


    public EditorSaveSystem(GameController gameController) {
        this.gameController = gameController;
    }


    public void createNewLevel(int levelSize) {
        LoadingParameters instance = LoadingParameters.getInstance();
        instance.loadingType = LoadingType.editor_new;
        instance.levelSize = levelSize;
        instance.playersNumber = 1;
        instance.fractionsQuantity = 5;
        instance.colorOffset = 0;
        instance.difficulty = Difficulty.BALANCER;

        int slotNumber = getNewSlotNumber();
        LoadingManager.getInstance().startGame(instance, new Runnable() {
            @Override
            public void run() {
                GameRules.editorChosenColor = 1; // green
                GameRules.eventEnabled = false;
                GameRules.editorFog = false;
                GameRules.editorDiplomacy = false;
                GameRules.editorSlotNumber = slotNumber;

                gameController.getLevelEditor().resetInputMode();
            }
        });
    }


    public void onLevelImported(String levelCode, int slotNumber) {
        GameRules.editorSlotNumber = slotNumber;
        gameController.levelEditorManager.onLevelImported(levelCode);

        String mapName = gameController.decodeManager.extractMapName(levelCode);
        if (mapName != null && !mapName.equals("unknown")) {
            Preferences prefs = getPreferences();
            prefs.putString(SLOT_NAME + slotNumber + ":name", mapName);
            prefs.flush();
        }
    }


    public int getNewSlotNumber() {
        int maxSlotNumber = -1;
        int index = 0;
        Preferences preferences = getPreferences();
        while (true) {
            String key = LevelEditorManager.SLOT_NAME + index;
            if (index > 8 && !preferences.contains(key)) break;
            if (isEmpty(key)) break;

            maxSlotNumber = index;
            index++;
        }

        return maxSlotNumber + 1;
    }


    public void playLevel(int slotNumber) {
        Preferences prefs = getPreferences();
        String fullLevel = prefs.getString(SLOT_NAME + slotNumber, "");
        if (fullLevel.length() < 10) return; // empty slot

        LoadingParameters instance = LoadingParameters.getInstance();
        instance.loadingType = LoadingType.editor_play;
        gameController.gameSaver.legacyImportManager.applyFullLevel(instance, fullLevel);
        GameRules.editorChosenColor = prefs.getInteger("chosen_color" + slotNumber, GameRules.editorChosenColor);
        GameRules.editorFog = prefs.getBoolean("editor_fog" + slotNumber, GameRules.editorFog);
        instance.fogOfWar = GameRules.editorFog;
        checkToApplyPlayersNumberFix(instance);
        GameRules.editorDiplomacy = prefs.getBoolean("editor_diplomacy" + slotNumber, GameRules.editorDiplomacy);
        GameRules.landscape = prefs.getBoolean("landscape" + slotNumber, GameRules.landscape);
        GameRules.provinceMaxMoney = prefs.getInteger("province_max_money" + slotNumber, GameRules.provinceMaxMoney);
        GameRules.enableRebel = prefs.getBoolean("enable_rebel" + slotNumber, GameRules.enableRebel);
        GameRules.keepDifferentProvinceNames = prefs.getBoolean("keep_different_province_names" + slotNumber, GameRules.keepDifferentProvinceNames);
        GameRules.diplomaticRelationsLocked = prefs.getBoolean("lock_relations" + slotNumber, GameRules.diplomaticRelationsLocked);
        GameRules.eventEnabled = prefs.getBoolean("event_enable" + slotNumber, GameRules.eventEnabled);
        GameRules.captainAsCityRules = prefs.getBoolean("captain_as_city" + slotNumber, GameRules.captainAsCityRules);
        instance.diplomacy = GameRules.editorDiplomacy;
        instance.captainAsCity = GameRules.captainAsCityRules;
        instance.colorOffset = getColorOffsetForPlayLevelProcess();
        LevelEditorManager levelEditorManager = gameController.levelEditorManager;
        instance.editorProvincesData = levelEditorManager.editorProvinceManager.encode();
        instance.editorRelationsData = levelEditorManager.editorRelationsManager.encode();
        instance.editorCoalitionsData = levelEditorManager.coalitionsManager.encode();
        instance.preparedMessagesData = gameController.messagesManager.encode();
        instance.preparedEventData = gameController.eventManager.encode();
        instance.goalData = gameController.finishGameManager.encode();
        instance.diplomaticRelationsLocked = GameRules.diplomaticRelationsLocked;

        LoadingManager.getInstance().startGame(instance, new Runnable() {
            @Override
            public void run() {
                applySlotRules(slotNumber);
                checkToApplyGoalColorFix();
                String labelsString = prefs.getString("labels" + slotNumber, "");
                if (labelsString.length() > 1) {
                    HexLabel.decodeLabels(labelsString, gameController.fieldManager);
                }
            }
        });
    }


    public void checkToApplyGoalColorFix() {
        FinishGameManager finishGameManager = gameController.finishGameManager;
        if (!finishGameManager.isColorIconNeeded(finishGameManager.goalType)) return;
        finishGameManager.arg1 = gameController.colorsManager.getFractionByColor(finishGameManager.arg1);
        gameController.initialParameters.goalData = finishGameManager.encode(); // for restart
        Scenes.sceneGoalView.sync();
    }


    private int getColorOffsetForPlayLevelProcess() {
        if (GameRules.editorChosenColor == 0) {
            return getRandomValidPlayerColor();
        }
        return ColorHolderElement.getColor(GameRules.editorChosenColor, GameRules.MAX_FRACTIONS_QUANTITY);
    }


    private int getRandomValidPlayerColor() {
        ArrayList<Integer> availableColors = new ArrayList<>();
        for (Hex activeHex : gameController.fieldManager.activeHexes) {
            if (activeHex.isNeutral()) continue;
            int color = activeHex.fraction; // in case of editor it's the same thing
            if (availableColors.contains(color)) continue;
            availableColors.add(color);
        }

        if (availableColors.size() == 0) return 0;

        int index = YioGdxGame.random.nextInt(availableColors.size());
        return availableColors.get(index);
    }


    private void checkToApplyPlayersNumberFix(LoadingParameters instance) {
        if (instance.playersNumber <= GameRules.NEUTRAL_FRACTION) return;
        instance.playersNumber++;
    }


    private boolean isEmpty(String key) {
        String fullLevelString = getPreferences().getString(key);
        return (fullLevelString.length() < 10);
    }


    public int getSlotNumberByKey(String key) {
        return Integer.valueOf(key.substring(4));
    }


    public void loadSlot(int slotNumber) {
        Preferences prefs = getPreferences();
        String fullLevel = prefs.getString(SLOT_NAME + slotNumber, "");

        LoadingParameters instance = LoadingParameters.getInstance();

        instance.loadingType = LoadingType.editor_load;
        gameController.gameSaver.legacyImportManager.applyFullLevel(instance, fullLevel);
        instance.colorOffset = 0;

        LoadingManager.getInstance().startGame(instance, new Runnable() {
            @Override
            public void run() {
                applySlotData(slotNumber);
            }
        });
    }


    private void applySlotData(int slotNumber) {
        Preferences prefs = getPreferences();
        applySlotRules(slotNumber);
        GameRules.editorSlotNumber = slotNumber;
        LevelEditorManager levelEditorManager = gameController.levelEditorManager;
        String editorProvincesString = prefs.getString("editor_provinces" + slotNumber, "");
        if (editorProvincesString.length() > 4) {
            levelEditorManager.editorProvinceManager.decode(editorProvincesString);
        }
        String editorRelationsString = prefs.getString("editor_relations" + slotNumber, "");
        if (editorRelationsString.length() > 4) {
            levelEditorManager.editorRelationsManager.decode(editorRelationsString);
        }
        String editorCoalitionsString = prefs.getString("coalitions" + slotNumber, "");
        if (editorCoalitionsString.length() > 4) {
            levelEditorManager.coalitionsManager.decode(editorCoalitionsString);
        }
        String preparedMessagesString = prefs.getString("prepared_messages" + slotNumber, "");
        if (preparedMessagesString.length() > 1) {
            gameController.messagesManager.decode(preparedMessagesString);
        }
        String preparedEventString = prefs.getString("prepared_event" + slotNumber, "");
        if (preparedEventString.length() > 1) {
            //System.out.println("Event Load");
            gameController.eventManager.decode(preparedEventString);
        }
        String goalString = prefs.getString("goal" + slotNumber, "");
        if (goalString.length() > 1) {
            gameController.finishGameManager.decode(goalString);
        }
        String labelsString = prefs.getString("labels" + slotNumber, "");
        if (labelsString.length() > 1) {
            HexLabel.decodeLabels(labelsString, gameController.fieldManager);
        }

        gameController.getLevelEditor().resetInputMode();
    }


    private void applySlotRules(int slotNumber) {
        Preferences prefs = getPreferences();
        GameRules.editorChosenColor = prefs.getInteger("chosen_color" + slotNumber, GameRules.editorChosenColor);
        GameRules.editorFog = prefs.getBoolean("editor_fog" + slotNumber, GameRules.editorFog);
        GameRules.editorDiplomacy = prefs.getBoolean("editor_diplomacy" + slotNumber, GameRules.editorDiplomacy);
        GameRules.landscape = prefs.getBoolean("landscape" + slotNumber, GameRules.landscape);
        GameRules.provinceMaxMoney = prefs.getInteger("province_max_money" + slotNumber, GameRules.provinceMaxMoney);
        GameRules.enableRebel = prefs.getBoolean("enable_rebel" + slotNumber, GameRules.enableRebel);
        GameRules.keepDifferentProvinceNames = prefs.getBoolean("keep_different_province_names" + slotNumber, GameRules.keepDifferentProvinceNames);
        GameRules.diplomaticRelationsLocked = prefs.getBoolean("lock_relations" + slotNumber, GameRules.diplomaticRelationsLocked);
        GameRules.captainAsCityRules = prefs.getBoolean("captain_as_city" + slotNumber, GameRules.captainAsCityRules);
        GameRules.eventEnabled = prefs.getBoolean("event_enable" + slotNumber, GameRules.eventEnabled);
    }


    public void loadTopSlot() {
        int newSlotNumber = getNewSlotNumber();
        if (newSlotNumber < 2) return;
        loadSlot(newSlotNumber - 1);
    }


    public void saveSlot(int slotNumber) {
        String fullLevel = getFullLevelString();
        Preferences prefs = getPreferences();
        LevelEditorManager levelEditorManager = gameController.levelEditorManager;
        prefs.putString(SLOT_NAME + slotNumber, fullLevel);
        prefs.putInteger("chosen_color" + slotNumber, GameRules.editorChosenColor);
        prefs.putBoolean("editor_fog" + slotNumber, GameRules.editorFog);
        prefs.putBoolean("editor_diplomacy" + slotNumber, GameRules.editorDiplomacy);
        prefs.putBoolean("landscape" + slotNumber, GameRules.landscape);
        prefs.putInteger("province_max_money" + slotNumber, GameRules.provinceMaxMoney);
        prefs.putBoolean("enable_rebel" + slotNumber, GameRules.enableRebel);
        prefs.putBoolean("keep_different_province_names" + slotNumber, GameRules.keepDifferentProvinceNames);
        prefs.putBoolean("event_enable" + slotNumber, GameRules.eventEnabled);
        prefs.putBoolean("lock_relations" + slotNumber, GameRules.diplomaticRelationsLocked);
        prefs.putBoolean("captain_as_city" + slotNumber, GameRules.captainAsCityRules);
        prefs.putString("editor_provinces" + slotNumber, levelEditorManager.editorProvinceManager.encode());
        prefs.putString("editor_relations" + slotNumber, levelEditorManager.editorRelationsManager.encode());
        prefs.putString("coalitions" + slotNumber, levelEditorManager.coalitionsManager.encode());
        prefs.putString("prepared_messages" + slotNumber, gameController.messagesManager.encode());
        prefs.putString("prepared_event" + slotNumber, gameController.eventManager.encode());
        prefs.putString("goal" + slotNumber, gameController.finishGameManager.encode());
        prefs.putString("labels" + slotNumber, HexLabel.encodeLabels(gameController.fieldManager.activeHexes));
        String preview = EditorPreviewManager.encodePreview(gameController);
        if (preview != null) {
            prefs.putString("preview" + slotNumber, preview);
        }
        prefs.flush();
    }


    public String getFullLevelString() {
        GameRules.setFractionsQuantity(gameController.getLevelEditor().countUpFractionsQuantity());
        return gameController.gameSaver.legacyExportManager.getFullLevelString();
    }


    private Preferences getPreferences() {
        return Gdx.app.getPreferences(EDITOR_PREFS);
    }
}
