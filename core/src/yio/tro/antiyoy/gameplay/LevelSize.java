package yio.tro.antiyoy.gameplay;

import yio.tro.antiyoy.stuff.LanguagesManager;

public class LevelSize {

    public static final int SMALL = 1;
    public static final int MEDIUM = 2;
    public static final int BIG = 4;
    public static final int HUGE = 9;
    public static final int GIANT = 16;
    public static final int WORLD = 25;


    public static String convertToString(int index) {
        switch (index) {
            default:
            case 0:
                return LanguagesManager.getInstance().getString("small");
            case 1:
                return LanguagesManager.getInstance().getString("medium");
            case 2:
                return LanguagesManager.getInstance().getString("big");
            case 3:
                return LanguagesManager.getInstance().getString("huge");
            case 4:
                return LanguagesManager.getInstance().getString("giant");
            case 5:
                return LanguagesManager.getInstance().getString("world");
        }
    }
}
