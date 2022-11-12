package yio.tro.antiyoy;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import yio.tro.antiyoy.factor_yio.FactorYio;
import yio.tro.antiyoy.gameplay.*;
import yio.tro.antiyoy.gameplay.campaign.CampaignLevelFactory;
import yio.tro.antiyoy.gameplay.campaign.CampaignProgressManager;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomacyInfoCondensed;
import yio.tro.antiyoy.gameplay.game_view.GameView;
import yio.tro.antiyoy.gameplay.loading.LoadingManager;
import yio.tro.antiyoy.gameplay.loading.LoadingParameters;
import yio.tro.antiyoy.gameplay.name_generator.CityNameGenerator;
import yio.tro.antiyoy.gameplay.name_generator.CustomCityNamesManager;
import yio.tro.antiyoy.gameplay.name_generator.LeaderNameGenerator;
import yio.tro.antiyoy.gameplay.replays.ReplaySaveSystem;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.gameplay.skins.SkinManager;
import yio.tro.antiyoy.gameplay.user_levels.UserLevelFactory;
import yio.tro.antiyoy.gameplay.user_levels.UserLevelsManager;
import yio.tro.antiyoy.menu.*;
import yio.tro.antiyoy.menu.diplomatic_exchange.ArgumentViewFactory;
import yio.tro.antiyoy.menu.save_slot_selector.SaveSystem;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.stuff.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public class YioGdxGame extends ApplicationAdapter implements InputProcessor{
    final SplatController splatController;
    private final OnKeyReactions onKeyReactions;
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public int w, h;
    public MenuControllerYio menuControllerYio;
    public MenuViewYio menuViewYio;
    private static GlyphLayout glyphLayout = new GlyphLayout();
    public static PlatformType platformType;
    TextureRegion mainBackground, infoBackground, settingsBackground, pauseBackground;
    TextureRegion currentBackground;
    TextureRegion lastBackground;
    public static float screenRatio;
    public GameController gameController;
    public GameView gameView;
    public boolean gamePaused, readyToUnPause, startedExitProcess;
    private long timeToUnPause;
    private int frameSkipCount;
    private FrameBuffer frameBuffer;
    private FactorYio transitionFactor;
    public static final Random random = new Random();
    private boolean alreadyShownErrorMessageOnce;
    private int fps, currentFrameCount;
    long timeToUpdateFpsInfo;
    private int currentBackgroundIndex;
    public int currentBubbleIndex, selectedLevelIndex, splashCount;
    public float defaultBubbleRadius, pressX, pressY, animX, animY, animRadius;
    double bubbleGravity;
    boolean loadedResources;
    boolean ignoreDrag;
    public boolean simpleTransitionAnimation, useMenuMasks;
    TextureRegion splash;
    ArrayList<Integer> backButtonIds;
    static boolean screenVerySmall;
    public int balanceIndicator[];
    public CampaignLevelFactory campaignLevelFactory;
    public SaveSystem saveSystem;
    FactorYio blackoutFactor;
    public SkinManager skinManager;
    public Stage stage; // for keyboard input
    public InputMultiplexer inputMultiplexer;
    FileHandle createDatas;
    


    public YioGdxGame() {
        splatController = new SplatController(this);
        onKeyReactions = new OnKeyReactions(this);
    }

    public boolean scrolled(float a,float b){
        return true;
    }

    @Override
    public void create() {
        loadedResources = false;
        splashCount = 0;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        splash = GraphicsYio.loadTextureRegion("splash.png", true);
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        pressX = 0.5f * w;
        pressY = 0.5f * h;
        frameSkipCount = 50; // >= 2
        screenRatio = (float) w / (float) h;
        frameBuffer = FrameBufferYio.getInstance(Pixmap.Format.RGB565, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        balanceIndicator = new int[GameRules.fractionsQuantity];
        backButtonIds = new ArrayList<Integer>();
        useMenuMasks = true;
        stage = new Stage();


    }

    private void initFiles(){
        createDatas=Gdx.files.external("AntiyoyDatas/temp.txt");
        createDatas.writeString("temp",false);
    }


    private void generalInitialization() {
        long time1 = System.currentTimeMillis();

        loadedResources = true;
        startedExitProcess = false;
        screenVerySmall = Gdx.graphics.getDensity() < 1.2;
        initializeSingletons();
        loadSomeTextures();
        SoundManagerYio.loadAllSounds();
        MusicManager.getInstance().load();
        transitionFactor = new FactorYio();
        splatController.splatTransparencyFactor = new FactorYio();
        splatController.initSplats();

        SettingsManager.getInstance().setYioGdxGame(this);
        SettingsManager.getInstance().loadAllSettings();
        Fonts.initFonts();
        CityNameGenerator.getInstance().load();
        LeaderNameGenerator.getInstance().load();
        DiplomacyInfoCondensed.onGeneralInitialization();
        gamePaused = true;
        alreadyShownErrorMessageOnce = false;
        fps = 0;
        timeToUpdateFpsInfo = System.currentTimeMillis() + 1000;

        loadProgress();
        SingleMessages.load();
        skinManager = new SkinManager(this);
        menuControllerYio = new MenuControllerYio(this);
        menuViewYio = new MenuViewYio(this);
        gameController = new GameController(this); // must be called after menu controller is created. because of languages manager and other stuff
        saveSystem = new SaveSystem(gameController); // must be called after game controller is created
        gameView = new GameView(this);
        gameView.appearFactor.destroy(1, 1);
        campaignLevelFactory = new CampaignLevelFactory(gameController);
        currentBackgroundIndex = -1;
        currentBackground = gameView.texturesManager.blackPixel; // call this after game view is created
        beginBackgroundChange(0, true, false);
        defaultBubbleRadius = 0.02f * w;
        bubbleGravity = 0.00025 * w;
        splatController.revealSplats();
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        initBlackoutFactor();

        Scenes.sceneMainMenu.checkToCreateResumeButton();
        ReplaySaveSystem.getInstance().setGameController(gameController);

        YioGdxGame.say("full loading time: " + (System.currentTimeMillis() - time1));
        checkForSingleMessageOnStart();
        checkToShowIosCheckMyGames();
    }


    private void checkToShowIosCheckMyGames() {
        if (platformType != PlatformType.ios) return;
        if (OneTimeInfo.getInstance().iosCheckMyGames) return;
        int numberOfCompletedLevels = CampaignProgressManager.getInstance().getNumberOfCompletedLevels();
        if (numberOfCompletedLevels < 5 && !areSkirmishOptionsModified()) return;

        OneTimeInfo.getInstance().iosCheckMyGames = true;
        OneTimeInfo.getInstance().save();
        Scenes.sceneIosCheckMyGames.create();
    }


    private void initBlackoutFactor() {
        blackoutFactor = new FactorYio();
        blackoutFactor.setValues(0, 0);
        blackoutFactor.appear(3, 0.7);
    }


    private void checkForSingleMessageOnStart() {
        if (OneTimeInfo.getInstance().newGameRelease) return;
        int numberOfCompletedLevels = CampaignProgressManager.getInstance().getNumberOfCompletedLevels();
        if (numberOfCompletedLevels < 5 && !areSkirmishOptionsModified()) return;
        if (platformType == PlatformType.ios) return;

        OneTimeInfo.getInstance().newGameRelease = true;
        OneTimeInfo.getInstance().save();
        Scenes.sceneAttraction.create();
    }


    private boolean areSkirmishOptionsModified() {
        Preferences prefs = Gdx.app.getPreferences("skirmish");

        if (prefs.getInteger("difficulty", 1) != 1) return true;
        if (prefs.getInteger("map_size", 1) != 1) return true;
        if (prefs.getInteger("color_number", 2) != 2) return true;
        if (prefs.getInteger("player_number", 1) != 1) return true;

        return false;
    }


    private void initializeSingletons() {
        CampaignProgressManager.initialize();
        CityNameGenerator.initialize();
        LanguagesManager.initialize();
        LoadingManager.initialize();
        LoadingParameters.initialize();
        MusicManager.initialize();
        OneTimeInfo.initialize();
        RefuseStatistics.initialize();
        SettingsManager.initialize();
        UserLevelFactory.initialize();
        UserLevelsManager.initialize();
        GlobalStatistics.initialize();
        ReplaySaveSystem.initialize();
        KeyboardManager.initialize();
        CustomCityNamesManager.initialize();
        ArgumentViewFactory.initialize();
        StoreLinksYio.initialize();
    }


    private void loadSomeTextures() {
        mainBackground = GraphicsYio.loadTextureRegion("main_menu_background.png", true);
        infoBackground = GraphicsYio.loadTextureRegion("info_background.png", true);
        settingsBackground = GraphicsYio.loadTextureRegion("settings_background.png", true);
        pauseBackground = GraphicsYio.loadTextureRegion("pause_background.png", true);
        splatController.loadTextures();
    }


    private void loadProgress() {
        Preferences preferences = Gdx.app.getPreferences("main");
        selectedLevelIndex = preferences.getInteger("progress", 1); // 1 - default value;
        if (selectedLevelIndex > CampaignProgressManager.getIndexOfLastLevel()) { // completed campaign
            selectedLevelIndex = CampaignProgressManager.getIndexOfLastLevel();
        }
    }


    public void setGamePaused(boolean gamePaused) {
        if (this.gamePaused == gamePaused) return;

        if (gamePaused) { // actions when paused
            this.gamePaused = true;
            gameController.selectionManager.deselectAll();
            splatController.revealSplats();
            readyToUnPause = false;
            menuControllerYio.forceDyingButtonsToEnd();
            DebugFlags.closerLookMode = false;
            return;
        }

        // actions when unpaused
        unPauseAfterSomeTime();
        beginBackgroundChange(4, true, true);
        splatController.hideSplats();
        Fonts.gameFont.setColor(Color.WHITE);
    }


    public void beginBackgroundChange(int index, boolean updateAnimPos, boolean simpleTransition) {
        if (currentBackgroundIndex == index && index == 4) return;
        this.simpleTransitionAnimation = simpleTransition;
        currentBackgroundIndex = index;
        lastBackground = currentBackground;
        if (updateAnimPos) {
            animX = pressX;
            animY = pressY;
            float r1, r2, r3, r4;
            r1 = (float) Yio.distance(animX, animY, 0, 0);
            r2 = (float) Yio.distance(animX, animY, w, 0);
            r3 = (float) Yio.distance(animX, animY, 0, h);
            r4 = (float) Yio.distance(animX, animY, w, h);
            animRadius = r1;
            if (r2 > animRadius) animRadius = r2;
            if (r3 > animRadius) animRadius = r3;
            if (r4 > animRadius) animRadius = r4;
        }
        switch (index) {
            case 0:
                currentBackground = mainBackground;
                break;
            case 1:
                currentBackground = infoBackground;
                break;
            case 2:
                currentBackground = settingsBackground;
                break;
            case 3:
                currentBackground = pauseBackground;
                break;
            case 4:
                currentBackground = gameView.texturesManager.blackPixel;
                break;
        }
        transitionFactor.setValues(0.02, 0.01);
        transitionFactor.appear(0, 0.8);
    }


    private void checkToUseMenuMasks() {
        if (!useMenuMasks) { // check to switch on masks
            if (gameView.appearFactor.get() < 1) {
                useMenuMasks = true;
                return;
            }
            ButtonYio buttonYio = menuControllerYio.getButtonById(30);
            if (buttonYio != null && buttonYio.isCurrentlyTouched()) {
                useMenuMasks = true;
                return;
            }
        } else { // check to switch off masks
            if (gameView.appearFactor.get() == 1 && gameView.appearFactor.getDy() == 0) {
                useMenuMasks = false;
                return;
            }
        }
    }


    private void move() {
        if (!loadedResources) return;

        transitionFactor.move();
        moveSelMoneyFactor();
        blackoutFactor.move();
        checkToUnPause();

        moveInGameStuff();
        gameView.moveFactors();

        menuControllerYio.move();
        if (!loadedResources) return; // if exit button was pressed

        checkToUseMenuMasks();

        splatController.moveSplats();
        stage.act();
    }


    private void moveInGameStuff() {
        if (gamePaused) return;

        gameView.updateCurrentZoomQuality();
        gameController.move();

        if (gameView.appearFactor.get() < 0.95) {
            say("game not paused but game view is not visible");
        }
    }


    private void moveSelMoneyFactor() {
        gameController.selectionManager.getSelMoneyFactor().move();
    }


    private void checkToUnPause() {
        if (!readyToUnPause) return;
        if (System.currentTimeMillis() <= timeToUnPause) return;
        if (!gameView.coversAllScreen()) return;

        gamePaused = false;
        readyToUnPause = false;
        gameController.resetCurrentTouchCount();
        frameSkipCount = 10; // >= 2
    }


    private void drawBackground(TextureRegion textureRegion) {
        batch.begin();
        batch.draw(textureRegion, 0, 0, w, h);
        batch.end();
    }


    private void renderMenuLayersWhenNothingIsMoving() { // when transitionFactor.get() == 1
        Color c = batch.getColor();
        batch.setColor(c.r, c.g, c.b, 1);
        batch.begin();
        batch.draw(currentBackground, 0, 0, w, h);
        splatController.renderSplats(c);
        batch.end();
    }


    private void renderMenuLayersWhenUsualAnimation() {
        Color c = batch.getColor();
        batch.setColor(c.r, c.g, c.b, 1);
        drawBackground(lastBackground);

        if (simpleTransitionAnimation) {
            float f = (0 + transitionFactor.get());
            batch.setColor(c.r, c.g, c.b, f);
            drawBackground(currentBackground);
        } else {
            float f = (0 + transitionFactor.get());
            LegacyMasking.begin();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.circle(animX, animY, f * animRadius, 32);
            shapeRenderer.end();
            LegacyMasking.continueAfterBatchBegin();
            drawBackground(currentBackground);
            LegacyMasking.end();
        }

        batch.begin();
        splatController.renderSplats(c);
        batch.end();

        menuViewYio.render(false, true);
    }


    private void renderMenuWhenGameViewNotVisible() {
        if (gameView.coversAllScreen()) return;

        if (transitionFactor.get() == 1) {
            renderMenuLayersWhenNothingIsMoving();
            return;
        }

        renderMenuLayersWhenUsualAnimation();
    }


    private void renderInternals() {
        currentFrameCount++;
        checkToUpdateFpsInfo();
        renderMenuWhenGameViewNotVisible();

        gameView.render();
        renderMenuView();
        checkToRenderBlackout();
        checkToRenderFps();
    }


    private void checkToRenderBlackout() {
        if (blackoutFactor.get() == 1) return;

        GraphicsYio.setBatchAlpha(batch, 1 - blackoutFactor.get());
        batch.begin();
        batch.draw(gameView.texturesManager.blackPixel, 0, 0, GraphicsYio.width, GraphicsYio.height);
        batch.end();
        GraphicsYio.setBatchAlpha(batch, 1);
    }


    private void checkToRenderFps() {
        if (!DebugFlags.showFpsInfo) return;

        batch.begin();
        Fonts.microFont.draw(batch, "" + Math.min(fps, 60), 0.02f * GraphicsYio.width, GraphicsYio.height - 0.02f * GraphicsYio.width);
        batch.end();
    }


    private void renderMenuView() {
        if (gamePaused) {
            menuViewYio.render(true, false);
        } else {
            menuViewYio.render(true, true);
        }
    }


    private void checkToUpdateFpsInfo() {
        if (!DebugFlags.showFpsInfo) return;
        if (System.currentTimeMillis() <= timeToUpdateFpsInfo) return;

        timeToUpdateFpsInfo = System.currentTimeMillis() + 1000;
        fps = currentFrameCount;
        currentFrameCount = 0;
    }


    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!loadedResources) {
            renderWhenNotLoadedResources();
            return;
        }

        try {
            move();
        } catch (Exception exception) {
            onCatchedExceptionInMove(exception);
        }

        if (gamePaused) {
            renderInternals();
        } else {
            if (Gdx.graphics.getDeltaTime() < 0.025 || frameSkipCount >= 2) {
                frameSkipCount = 0;
                frameBuffer.begin();
                renderInternals();
                frameBuffer.end();
            } else {
                frameSkipCount++;
            }
            batch.begin();
            batch.draw(frameBuffer.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
            batch.end();
        }

        stage.draw();
    }


    private void onCatchedExceptionInMove(Exception exception) {
        if (alreadyShownErrorMessageOnce) return;

        exception.printStackTrace();
        alreadyShownErrorMessageOnce = true;
        gameView.destroy();
        setGamePaused(true);
        Scenes.sceneExceptionReport.setException(exception);
        Scenes.sceneExceptionReport.create();
    }


    private void renderWhenNotLoadedResources() {
        batch.begin();
        batch.draw(splash, 0, 0, w, h);
        batch.end();
        if(splashCount==52){
            generalInitialization();
        }
        if (splashCount == 50) {
            splash = GraphicsYio.loadTextureRegion("splash_2.png", true);
        }

        splashCount++;
    }


    private void unPauseAfterSomeTime() {
        readyToUnPause = true;
        timeToUnPause = System.currentTimeMillis() + 450; // время анимации - около 420мс
    }


    public void setAnimToPlayButtonSpecial() {
        ButtonYio buttonYio = menuControllerYio.getButtonById(3);
        if (buttonYio != null) {
            animX = buttonYio.cx;
            animY = buttonYio.cy;
        } else {
            animX = w/2;
            animY = h/2;
        }
        transitionFactor.setValues(0.15, 0);
    }


    public void setAnimToResumeButtonSpecial() {
        animX = w;
        animY = h;
        animRadius = (float) Yio.distance(0, 0, w, h);
    }


    public void setAnimToStartButtonSpecial() {
        animX = 0.5f * w;
        animY = 0.65f * h;
        animRadius = animY;
    }


    public static boolean isScreenVerySmall() {
        return screenVerySmall;
    }


    public static boolean isScreenVeryWide() {
        float ratio = (float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        return ratio < 1.51;
    }


    public void forceBackgroundChange() {
        transitionFactor.setValues(1, 0);
        simpleTransitionAnimation = true;
    }


    public static void say(String text) {
        System.out.println(text);
    }


    public void restartGame() {
        if (GameRules.campaignMode) {
            int currentLevelIndex = CampaignProgressManager.getInstance().currentLevelIndex;
            campaignLevelFactory.createCampaignLevel(currentLevelIndex);
            return;
        }

        gameController.restartGame();
    }


    void increaseLevelSelection() {
//        menuControllerYio.scrollerYio.increaseSelection();
        setSelectedLevelIndex(selectedLevelIndex + 1);
    }


    public static ArrayList<String> decodeStringToArrayList(String string, String delimiters) {
        ArrayList<String> res = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(string, delimiters);
        while (tokenizer.hasMoreTokens()) {
            res.add(tokenizer.nextToken());
        }
        return res;
    }


    public int getSelectedLevelIndex() {
        return selectedLevelIndex;
    }


    public void setSelectedLevelIndex(int selectedLevelIndex) {
        if (selectedLevelIndex >= 0 && selectedLevelIndex <= CampaignProgressManager.getIndexOfLastLevel()) {
            this.selectedLevelIndex = selectedLevelIndex;
        }
    }


    public boolean pressButtonIfVisible(int id) {
        ButtonYio button = menuControllerYio.getButtonById(id);
        if (button == null) return false;
        if (!button.isVisible()) return false;
        if (button.appearFactor.get() != 1) return false;
        button.press();
        return true;
    }


    public void registerBackButtonId(int id) {
        for (Integer integer : backButtonIds) {
            if (integer == id) return;
        }
        backButtonIds.add(id);
    }


    public void onEndCreation() {
        gameView.updateCacheLevelTextures();
        menuControllerYio.removeButtonById(38); // build object button
        gameView.appear();
        setGamePaused(false);
    }


    @Override
    public boolean keyDown(int keycode) {
        return onKeyReactions.onKeyPressed(keycode);
    }


    @Override
    public boolean keyUp(int keycode) {
        return false;
    }


    @Override
    public boolean keyTyped(char character) {
        return false;
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        ignoreDrag = true;
        pressX = screenX;
        pressY = h - screenY;
        try {
            if (!gameView.isInMotion() && transitionFactor.get() > 0.99 && menuControllerYio.touchDown(screenX, Gdx.graphics.getHeight() - screenY, pointer, button)) {
                return false;
            } else {
                ignoreDrag = false;
            }
            if (!gamePaused) {
                gameController.touchDown(screenX, Gdx.graphics.getHeight() - screenY, pointer, button);
            }
        } catch (Exception exception) {
            if (!alreadyShownErrorMessageOnce) {
                exception.printStackTrace();
                alreadyShownErrorMessageOnce = true;
                Scenes.sceneExceptionReport.setException(exception);
                Scenes.sceneExceptionReport.create();
            }
        }
        return false;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        try {
            menuControllerYio.touchUp(screenX, Gdx.graphics.getHeight() - screenY, pointer, button);
            // System.currentTimeMillis() > lastTimeButtonPressed + 300
            if (!gamePaused && gameView.coversAllScreen()) {
                gameController.touchUp(screenX, Gdx.graphics.getHeight() - screenY, pointer, button);
            }
        } catch (Exception exception) {
            if (!alreadyShownErrorMessageOnce) {
                exception.printStackTrace();
                alreadyShownErrorMessageOnce = true;
                Scenes.sceneExceptionReport.setException(exception);
                Scenes.sceneExceptionReport.create();
            }
        }
        return false;
    }


    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        menuControllerYio.touchDragged(screenX, Gdx.graphics.getHeight() - screenY, pointer);
        if (!ignoreDrag && !gamePaused && gameView.coversAllScreen()) {
            gameController.touchDragged(screenX, Gdx.graphics.getHeight() - screenY, pointer);
        }
        return false;
    }


    public int gamesPlayed() {
        int s = 0;
        for (int i = 0; i < balanceIndicator.length; i++) {
            s += balanceIndicator[i];
        }
        return s;
    }


    private String getBalanceIndicatorAsString(int array[]) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("[");
        for (int i = 0; i < GameRules.fractionsQuantity; i++) {
            stringBuffer.append(" ").append(array[i]);
        }
        stringBuffer.append(" ]");
        return stringBuffer.toString();
    }


    public String getBalanceIndicatorString() {
        double D = 0;
        int max = balanceIndicator[0], min = balanceIndicator[0];
        for (int i = 0; i < GameRules.fractionsQuantity; i++) {
            if (balanceIndicator[i] > max) max = balanceIndicator[i];
            if (balanceIndicator[i] < min) min = balanceIndicator[i];
        }
        if (max > 0) {
            D = 1d - (double) min / (double) max;
        }
        String dStr = Double.toString(D);
        if (dStr.length() > 4) dStr = dStr.substring(0, 4);

        return getBalanceIndicatorAsString(balanceIndicator) + " = " + dStr;
    }


    static public float getTextWidth(BitmapFont font, String text) {
        glyphLayout.setText(font, text);
        return glyphLayout.width;
    }


    static float maxElement(ArrayList<Float> list) {
        if (list.size() == 0) return 0;
        float max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) > max) max = list.get(i);
        }
        return max;
    }


    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }



    public boolean scrolled(int amount) {
        if (menuControllerYio.onMouseWheelScrolled(amount)) return true; // UI can catch mouse scroll

        if (gameView.appearFactor.get() > 0.1) {
            gameController.scrolled(amount);
        }

        return true;
    }


    @Override
    public void pause() {
        super.pause();

        if (startedExitProcess) return;
        if (platformType == PlatformType.pc) return;

        if (menuControllerYio != null) {
            menuControllerYio.onAppPause();
        }

        if (gameView != null) {
            gameView.onAppPause();
        }

        if (gameController != null) {
            gameController.onAppPause();
        }
    }


    @Override
    public void resume() {
        super.resume();

        if (startedExitProcess) return;
        if (platformType == PlatformType.pc) return;

        if (menuControllerYio != null) {
            menuControllerYio.onAppResume();
        }

        if (gameView != null) {
            gameView.onAppResume();
        }
    }


    public void close() {
        // this is not working properly, so it's disabled for now
        if (true) return;
        loadedResources = false;
        gameController.close();
        menuControllerYio.close();

        gameController = null;
        menuControllerYio = null;
        menuViewYio = null;
        gameView = null;
    }
}
