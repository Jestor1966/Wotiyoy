package yio.tro.antiyoy.menu.scenes;

import com.badlogic.gdx.Gdx;
import yio.tro.antiyoy.MusicManager;
import yio.tro.antiyoy.PlatformType;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.menu.*;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.YioGdxGame;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.stuff.Fonts;

public class SceneSettings extends AbstractScene{


    private double labelHeight;
    private double labelTopY;
    private ButtonYio label;
    private CheckButtonYio chkSound;
    private CheckButtonYio chkAutosave;
    private CheckButtonYio chkMusic;
    private CheckButtonYio chkTurnEnd;
    private CheckButtonYio chkFastConstruction;
    private CheckButtonYio chkExportTxt;
    private Reaction rbBack;
    private Reaction rbTextSizeMinus;
    private Reaction rbTextSizePlus;
    private ButtonYio textSizeTitle;
    private ButtonYio btnTextSizeMinus;
    private ButtonYio btnTextSizeValue;
    private ButtonYio btnTextSizePlus;
    boolean initialized;
    boolean loadingCurrently;


    public SceneSettings(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        initialized = false;
        chkSound = null;
        initReactions();
    }


    private void initReactions() {
        rbBack = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onDestroy();
                Scenes.sceneMainMenu.create();
            }
        };
        rbTextSizeMinus = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                changeTextSize(-1);
            }
        };
        rbTextSizePlus = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                changeTextSize(1);
            }
        };
    }


    public void onDestroy() {
        applyValues();
        SettingsManager.getInstance().saveMainSettings();
    }


    @Override
    public void create() {
        menuControllerYio.beginMenuCreation();
        menuControllerYio.getYioGdxGame().beginBackgroundChange(1, false, true);
        menuControllerYio.spawnBackButton(190, rbBack);

        createInfoButton();
        createLabel();
        createTextSizeControls();
        createCheckButtons();
        createMoreSettingsButton();
        createLanguageButton();
        createCityNamesButton();

        loadValues();
        initialized = true;

        menuControllerYio.endMenuCreation();
    }


    private void createLanguageButton() {
        ButtonYio languageButton = buttonFactory.getButton(generateRectangle(0.08, labelTopY - labelHeight, 0.45, 0.05), 195, getString("language"));
        languageButton.setReaction(Reaction.rbLanguageMenu);
        languageButton.setAnimation(Animation.from_center);
        languageButton.setShadow(false);
        languageButton.setVisualHook(label);
        languageButton.setTouchOffset(0.05f * Gdx.graphics.getHeight());
    }


    private void createCityNamesButton() {
        ButtonYio cityNamesButton = buttonFactory.getButton(generateRectangle(0.1, 0.06, 0.8, 0.07), 198, getString("city_names"));
        cityNamesButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                applyValues();
                Scenes.sceneCityNames.create();
            }
        });
        cityNamesButton.setAnimation(Animation.down);
    }


    private void createTextSizeControls() {
        textSizeTitle = buttonFactory.getButton(generateRectangle(0.2, 0.82, 0.6, 0.06), 193, getString("text_size"));
        textSizeTitle.setTouchable(false);
        textSizeTitle.setAnimation(Animation.from_center);
        textSizeTitle.setShadow(false);

        btnTextSizeMinus = buttonFactory.getButton(generateSquare(0.21, 0.72, 0.08), 194, "-");
        btnTextSizeMinus.setReaction(rbTextSizeMinus);
        btnTextSizeMinus.setAnimation(Animation.from_center);
        btnTextSizeMinus.setTouchOffset(0.05f * Gdx.graphics.getHeight());

        btnTextSizeValue = buttonFactory.getButton(generateRectangle(0.37, 0.72, 0.26, 0.08), 196, getTextSizeString());
        btnTextSizeValue.setTouchable(false);
        btnTextSizeValue.setAnimation(Animation.from_center);
        btnTextSizeValue.setShadow(false);

        btnTextSizePlus = buttonFactory.getButton(generateSquare(0.66, 0.72, 0.08), 197, "+");
        btnTextSizePlus.setReaction(rbTextSizePlus);
        btnTextSizePlus.setAnimation(Animation.from_center);
        btnTextSizePlus.setTouchOffset(0.05f * Gdx.graphics.getHeight());
    }


    private String getTextSizeString() {
        return "" + getCurrentTextSize();
    }


    private int getCurrentTextSize() {
        if (SettingsManager.textSize > 0) {
            return SettingsManager.textSize;
        }
        return Fonts.getDefaultFontSize();
    }


    private void changeTextSize(int delta) {
        int currentSize = getCurrentTextSize();
        int newSize = Math.max(12, Math.min(40, currentSize + delta));
        if (newSize == currentSize) return;

        SettingsManager.textSize = newSize;
        SettingsManager.getInstance().saveMainSettings();
        Fonts.initFonts();

        // rebuild the menu so all buttons are re-rendered with the new font
        menuControllerYio.clear();
        Scenes.createScenes(menuControllerYio);
        Scenes.sceneSettings.create();
    }


    private void createMoreSettingsButton() {
        ButtonYio moreSettingsButton = buttonFactory.getButton(generateRectangle(0.62, labelTopY - labelHeight, 0.3, 0.05), 199, getString("more"));
        moreSettingsButton.setReaction(Reaction.rbMoreSettings);
        moreSettingsButton.setAnimation(Animation.from_center);
        moreSettingsButton.setShadow(false);
        moreSettingsButton.setVisualHook(label);
        moreSettingsButton.setTouchOffset(0.05f * Gdx.graphics.getHeight());
    }


    private void createCheckButtons() {
        initChecks();
        chkSound.appear();
        chkMusic.appear();
        chkAutosave.appear();
        chkTurnEnd.appear();
        chkFastConstruction.appear();
        chkExportTxt.appear();
    }


    private void initChecks() {
        if (chkSound != null) return;

        chkSound = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkSound.setParent(label);
        chkSound.alignTop(0.02);
        chkSound.setTitle(getString("sound"));
        chkSound.setListener(getChkSoundListener());

        chkMusic = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkMusic.setParent(label);
        chkMusic.alignUnderPreviousElement();
        chkMusic.setListener(getChkMusicListener());
        chkMusic.setTitle(getString("music"));

        chkAutosave = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkAutosave.setParent(label);
        chkAutosave.alignUnderPreviousElement();
        chkAutosave.setTitle(getString("autosave"));

        chkTurnEnd = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkTurnEnd.setParent(label);
        chkTurnEnd.alignUnderPreviousElement();
        chkTurnEnd.setTitle(getString("long_tap_to_end_turn"));

        chkFastConstruction = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkFastConstruction.setParent(label);
        chkFastConstruction.alignUnderPreviousElement();
        chkFastConstruction.setTitle(getString("fast_construction"));

        chkExportTxt = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkExportTxt.setParent(label);
        chkExportTxt.alignUnderPreviousElement();
        chkExportTxt.setTitle(getString("export_txt"));
    }


    private ICheckButtonListener getChkMusicListener() {
        return new ICheckButtonListener() {
            @Override
            public void onStateChanged(boolean checked) {
                applyValues();
                MusicManager.getInstance().onMusicStatusChanged();
            }
        };
    }


    private ICheckButtonListener getChkSoundListener() {
        return new ICheckButtonListener() {
            @Override
            public void onStateChanged(boolean checked) {
                if (loadingCurrently) return;
                applyValues();
                MusicManager.getInstance().onMusicStatusChanged();
            }
        };
    }


    private void createLabel() {
        labelHeight = 0.52;
        labelTopY = 0.7;
        label = buttonFactory.getButton(generateRectangle(0.04, labelTopY - labelHeight, 0.92, labelHeight), 192, " ");
        label.setTouchable(false);
        label.setAnimation(Animation.from_center);
    }


    private void createInfoButton() {
        if (YioGdxGame.platformType == PlatformType.ios) return;

        ButtonYio infoButton = buttonFactory.getButton(generateSquare(0.95 - 0.07 / YioGdxGame.screenRatio, 0.9, 0.07), 191, null);
        menuControllerYio.loadButtonOnce(infoButton, "menu/info_icon.png");
        infoButton.setShadow(true);
        infoButton.setAnimation(Animation.up);
        infoButton.setTouchOffset(0.05f * GraphicsYio.width);
        infoButton.setReaction(Reaction.rbAboutGame);
    }


    public void loadValues() {
        loadingCurrently = true;
        chkAutosave.setChecked(SettingsManager.autosave);
        chkSound.setChecked(SettingsManager.soundEnabled);
        chkFastConstruction.setChecked(SettingsManager.fastConstructionEnabled);
        chkTurnEnd.setChecked(SettingsManager.cautiosEndTurnEnabled);
        chkMusic.setChecked(SettingsManager.musicEnabled);
        chkExportTxt.setChecked(SettingsManager.exportTxt);
        loadingCurrently = false;
    }


    public void applyValues() {
        SettingsManager.autosave = chkAutosave.isChecked();
        SettingsManager.musicEnabled = chkMusic.isChecked();
        SettingsManager.cautiosEndTurnEnabled = chkTurnEnd.isChecked();
        SettingsManager.soundEnabled = chkSound.isChecked();
        SettingsManager.fastConstructionEnabled = chkFastConstruction.isChecked();
        SettingsManager.exportTxt = chkExportTxt.isChecked();
    }


    public boolean isInitialized() {
        return initialized;
    }
}
