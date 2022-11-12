package yio.tro.antiyoy.gameplay.rules;

public class GameRules {

    public static final int MAX_FRACTIONS_QUANTITY = 36;
    public static final int NEUTRAL_FRACTION = 7;
    public static final int UNIT_MOVE_LIMIT = 2;

    public static final int PRICE_UNIT = 10;
    public static final int PRICE_TOWER = 15;
    public static final int PRICE_FARM = 12;
    public static final int PRICE_STRONG_TOWER = 35;
    public static final int PRICE_MOUNTAIN = 0;

    public static final int PRICE_TREE = 10;
    public static final int FARM_INCOME = 4;
    public static final int CITY_INCOME = 6;
    public static final int TREE_CUT_REWARD = 3;
    public static final int CLEAR_REVOLT_REWARD = 1;

    public static final int TAX_TOWER = 1;
    public static final int TAX_STRONG_TOWER = 6;
    public static final int TAX_FORT = 4;
    public static final int TAX_MOUNTAIN = 0;
    public static final int TAX_UNIT_GENERIC_1 = 2;
    public static final int TAX_UNIT_GENERIC_2 = 6;
    public static final int TAX_UNIT_GENERIC_3 = 18;
    public static final int TAX_UNIT_GENERIC_4 = 36;
    public static final int TAX_UNIT_GENERIC_5 = 10;
    public static final int TAX_UNIT_GENERIC_6 = 45;
    public static final int TAX_UNIT_GENERIC_7 = 20;
    public static final int TAX_UNIT_GENERIC_8 = 12;
    public static final int TAX_UNIT_GENERIC_9 = 36;

    public static int fractionsQuantity = 7;
    public static boolean slayRules = false;
    public static boolean captainAsCityRules = false;
    public static boolean tutorialMode;
    public static boolean campaignMode;
    public static boolean inEditorMode;
    public static int difficulty;
    public static boolean aiOnlyMode;
    public static boolean replayMode;
    public static int editorChosenColor;
    public static boolean fogOfWarEnabled;
    public static boolean diplomacyEnabled;
    public static boolean eventEnabled;
    public static boolean userLevelMode;
    public static String ulKey;
    public static boolean editorFog;
    public static boolean editorDiplomacy;
    public static boolean editorColorFixApplied;
    public static int editorSlotNumber;
    public static int genProvinces;
    public static double treesSpawnChance;
    public static boolean diplomaticRelationsLocked;
    public static boolean canCityDestory;
    public static boolean rebelCanCreateNew;
    public static boolean enableRebel;


    public static void defaultValues() {
        tutorialMode = false;
        campaignMode = false;
        inEditorMode = false;
        aiOnlyMode = false;
        replayMode = false;
        fogOfWarEnabled = false;
        diplomacyEnabled = false;
        eventEnabled = false;
        userLevelMode = false;
        editorChosenColor = 1;
        ulKey = null;
        editorFog = false;
        editorDiplomacy = false;
        editorColorFixApplied = false;
        diplomaticRelationsLocked = false;
        canCityDestory = false;
        rebelCanCreateNew=false;
        enableRebel=false;
        editorSlotNumber = -1;
        genProvinces = 0;
        treesSpawnChance = 0.1;
    }


    public static void setFractionsQuantity(int fractionsQuantity) {
        if (fractionsQuantity < 0) {
            fractionsQuantity = 0;
        }

        GameRules.fractionsQuantity = fractionsQuantity;
    }


    public static void setDifficulty(int difficulty) {
        GameRules.difficulty = difficulty;
    }


    public static void setSlayRules(boolean slay_rules) {
        GameRules.slayRules = slay_rules;
    }


    public static void setEditorChosenColor(int editorChosenColor) {
        GameRules.editorChosenColor = editorChosenColor;
    }


    public static void setFogOfWarEnabled(boolean fogOfWarEnabled) {
        GameRules.fogOfWarEnabled = fogOfWarEnabled;
    }


    public static void setDiplomacyEnabled(boolean diplomacyEnabled) {
        GameRules.diplomacyEnabled = diplomacyEnabled;
    }


    public static void setDiplomaticRelationsLocked(boolean diplomaticRelationsLocked) {
        GameRules.diplomaticRelationsLocked = diplomaticRelationsLocked;
    }

    public static void setCaptainAsCityRules(boolean captainAsCityRules) {
        GameRules.diplomaticRelationsLocked = captainAsCityRules;
    }
}
