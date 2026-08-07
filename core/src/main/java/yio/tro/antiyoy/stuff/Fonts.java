package yio.tro.antiyoy.stuff;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import yio.tro.antiyoy.CustomLanguageLoader;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.YioGdxGame;

public class Fonts {

    public static final String SPECIAL_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"´`'<^>ö";
    public static BitmapFont buttonFont;
    public static BitmapFont biggerButtonFont;
    public static BitmapFont gameFont;
    public static BitmapFont microFont;
    public static BitmapFont mapFont;
    public static BitmapFont smallerMenuFont;
    public static int FONT_SIZE;
    private static FreeTypeFontGenerator generator; // kept alive: incremental fonts need it to generate missing glyphs on demand


    public static void initFonts() {
        CustomLanguageLoader.loadLanguage();

        long time1 = System.currentTimeMillis();
        FileHandle fontFile = loadFontFile();
        generator = new FreeTypeFontGenerator(fontFile);

        if (SettingsManager.textSize > 0) {
            FONT_SIZE = SettingsManager.textSize;
        } else {
            FONT_SIZE = getDefaultFontSize();
        }

        String characters = getAllCharacters();

        buttonFont = generateFont(characters, true, 0, null);
        buttonFont.setColor(Color.BLACK);

        biggerButtonFont = generateFont(characters, true, 0, null, (int) (1.5f * FONT_SIZE));
        biggerButtonFont.setColor(Color.BLACK);

        gameFont = generateFont(characters, false, 0, null);
        gameFont.setColor(Color.WHITE);

        microFont = generateFont(characters, false, 0, null);
        microFont.setColor(Color.WHITE);

        smallerMenuFont = generateFont(characters, false, 0, null);
        smallerMenuFont.setColor(Color.BLACK);

        mapFont = generateFont(characters, false, (int) (0.01f * FONT_SIZE), Color.BLACK);
        mapFont.setColor(Color.WHITE);

        // initFontChinese();

        YioGdxGame.say("time to generate fonts: " + (System.currentTimeMillis() - time1));
    }


    private static BitmapFont generateFont(String characters, boolean flip, int borderWidth, Color borderColor) {
        return generateFont(characters, flip, borderWidth, borderColor, (int) (0.8f * FONT_SIZE));
    }


    private static BitmapFont generateFont(String characters, boolean flip, int borderWidth, Color borderColor, int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.characters = characters;
        parameter.size = size;
        parameter.flip = flip;
        parameter.incremental = true; // generate missing glyphs on demand instead of showing garbage
        if (borderWidth > 0) {
            parameter.borderWidth = borderWidth;
            parameter.borderColor = borderColor;
        }

        return generator.generateFont(parameter);
    }


    private static FileHandle loadFontFile() {
        /*
        if (LanguagesManager.getInstance().getLanguage().equals("el_GR")) {
            System.out.println("Greek font loaded");
            return Gdx.files.internal("font_greek.ttf");
        }*/

        //if (LanguagesManager.getInstance().getLanguage().equals("cn_CN")) {
       //     System.out.println("Chinese font loaded");
       //     return Gdx.files.internal("font_cn.ttf");
       // }


        return Gdx.files.internal("font.otf");
    }


    public static int getDefaultFontSize() {
        if (LanguagesManager.getInstance().getLanguage().equals("cn_CN")) {
            return 21;
        }
        return (int) (0.024 * Gdx.graphics.getHeight());
    }


    public static String getAllCharacters() {
        String langChars="";
        langChars = LanguagesManager.getInstance().getString("lang_characters");
        return langChars + SPECIAL_CHARACTERS;
    }


    private static void initFontChinese() {
        if (!LanguagesManager.getInstance().getLanguage().equals("cn_CN")) return;

        getAllCharacters();

        FileHandle fontFile1 = Gdx.files.internal("chinese/Chinese.fnt");
        FileHandle fontFile2 = Gdx.files.internal("chinese/Chinese.png");
        buttonFont = new BitmapFont(fontFile1, fontFile2, true);
        gameFont = new BitmapFont(fontFile1, fontFile2, false);
        microFont = new BitmapFont(fontFile1, fontFile2, false);
        microFont.getData().setScale(0.5f);

        float resFactor = (float) Gdx.graphics.getWidth() / 450f;
        buttonFont.getData().setScale(resFactor);
        gameFont.getData().setScale(resFactor);
        microFont.getData().setScale(resFactor);
    }
}
