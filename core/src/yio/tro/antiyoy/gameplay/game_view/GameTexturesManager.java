package yio.tro.antiyoy.gameplay.game_view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.Storage3xTexture;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Unit;
import yio.tro.antiyoy.gameplay.skins.SkinManager;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.stuff.AtlasLoader;
import yio.tro.antiyoy.stuff.GraphicsYio;

public class GameTexturesManager {

    GameView gameView;
    TextureRegion backgroundRegion;
    public TextureRegion blackCircleTexture, exclamationMarkTexture, forefingerTexture;
    TextureRegion animationTextureRegion, blackBorderTexture;
    TextureRegion hexGreen, hexRed, hexBlue, hexYellow, hexCyan ,
            hexAlgae, hexLavender, hexOrchid, hexPurple, hexRose, hexWhiskey,
            hexWhite, hexDarkblue, hexWhiteblue,
            hexLightYellow,hexPrussiaBlue,hexBrown, hexSea,
            hexBlueGreen,hexDeepBlue,hexDeepRed,hexGold,hexTea,
            hexPaper,hexOld,hexBamboo,hexByzantine,hexPink,hexMing,hexGrayGreen,
            hexColor1, hexColor2, hexColor3, hexColor4, hexColor5, hexColor6;

    TextureRegion hexGreenSea, hexRedSea, hexBlueSea, hexYellowSea, hexCyanSea ,
            hexAlgaeSea, hexLavenderSea, hexOrchidSea, hexPurpleSea, hexRoseSea, hexWhiskeySea,
            hexWhiteSea, hexDarkblueSea, hexWhiteblueSea,
            hexLightYellowSea,hexPrussiaBlueSea,hexBrownSea, hexSeaSea,
            hexBlueGreenSea,hexDeepBlueSea,hexDeepRedSea,hexGoldSea,hexTeaSea,
            hexPaperSea,hexOldSea,hexBambooSea,hexByzantineSea,hexPinkSea,hexMingSea,hexGrayGreenSea,
            hexColor1Sea, hexColor2Sea, hexColor3Sea,hexColor4Sea, hexColor5Sea, hexColor6Sea;

    public TextureRegion blackPixel, grayPixel, selectionPixel, shadowHexTexture, gradientShadow, transCircle1, transCircle2, selUnitShadow;
    TextureRegion sideShadow, responseAnimHexTexture, selectionBorder, defenseIcon;
    public Storage3xTexture manTextures[], palmTexture[], houseTexture,towerTexture, graveTexture, pineTexture[];
    public Storage3xTexture manSeaTextures[],graveSeaTexture;
    public Storage3xTexture castleTexture, strongTowerTexture, mountainTexture[], hillTexture, fortTexture, cityTexture[], revoltTexture, farmTexture[];
    public AtlasLoader atlasLoader;


    public GameTexturesManager(GameView gameView) {
        this.gameView = gameView;
    }


    public void loadTextures() {
        loadSkin();
        loadSkinDependentTextures();
        blackCircleTexture = GraphicsYio.loadTextureRegion("black_circle.png", true);
        gradientShadow = GraphicsYio.loadTextureRegion("gradient_shadow.png", false);
        blackPixel = GraphicsYio.loadTextureRegion("black_pixel.png", false);
        transCircle1 = GraphicsYio.loadTextureRegion("transition_circle_1.png", false);
        transCircle2 = GraphicsYio.loadTextureRegion("transition_circle_2.png", false);
        selUnitShadow = GraphicsYio.loadTextureRegion("sel_shadow.png", true);
        sideShadow = GraphicsYio.loadTextureRegion("money_shadow.png", true);
        responseAnimHexTexture = GraphicsYio.loadTextureRegion("response_anim_hex.png", false);
        forefingerTexture = GraphicsYio.loadTextureRegion("forefinger.png", true);
        blackBorderTexture = GraphicsYio.loadTextureRegion("pixels/black_border.png", true);
        grayPixel = GraphicsYio.loadTextureRegion("pixels/gray_pixel.png", false);
    }


    private void loadSkinDependentTextures() {
        shadowHexTexture = GraphicsYio.loadTextureRegion(getSkinManager().getShadowHexPath(), true);
        exclamationMarkTexture = GraphicsYio.loadTextureRegion(getSkinManager().getExclamationMarkPath(), true);
        defenseIcon = GraphicsYio.loadTextureRegion(getSkinManager().getDefenseIconPath(), true);
        loadFieldTextures();
    }


    private void loadHexTextures() {
        SkinManager skinManager = getSkinManager();
        hexGreen = skinManager.loadHexTexture("green");
        hexRed = skinManager.loadHexTexture("red");
        hexBlue = skinManager.loadHexTexture("blue");
        hexCyan = skinManager.loadHexTexture("cyan");
        hexYellow = skinManager.loadHexTexture("yellow");
        hexAlgae = skinManager.loadHexTexture("algae");
        hexLavender = skinManager.loadHexTexture("lavender");
        hexOrchid = skinManager.loadHexTexture("orchid");
        hexPurple= skinManager.loadHexTexture("purple");
        hexRose = skinManager.loadHexTexture("rose");
        hexWhiskey = skinManager.loadHexTexture("whiskey");
        hexWhite = skinManager.loadHexTexture("white");
        hexDarkblue = skinManager.loadHexTexture("darkblue");
        hexWhiteblue = skinManager.loadHexTexture("whiteblue");
        hexSea = skinManager.loadHexTexture("sea");
        hexPrussiaBlue = skinManager.loadHexTexture("prussia_blue");
        hexLightYellow = skinManager.loadHexTexture("light_yellow");
        hexBrown = skinManager.loadHexTexture("brown");
        hexBlueGreen = skinManager.loadHexTexture("bluegreen");
        hexDeepBlue = skinManager.loadHexTexture("deepblue");
        hexDeepRed = skinManager.loadHexTexture("deepred");
        hexGold = skinManager.loadHexTexture("gold");
        hexTea = skinManager.loadHexTexture("tea");
        hexPaper = skinManager.loadHexTexture("paper");
        hexOld = skinManager.loadHexTexture("old");
        hexBamboo = skinManager.loadHexTexture("bamboo");
        hexByzantine = skinManager.loadHexTexture("byzantine");
        hexPink = skinManager.loadHexTexture("pink");
        hexMing = skinManager.loadHexTexture("ming");
        hexGrayGreen = skinManager.loadHexTexture("gray_green");

        hexColor1 = skinManager.loadHexTexture("color1");
        hexColor2 = skinManager.loadHexTexture("color2");
        hexColor3 = skinManager.loadHexTexture("color3");
        hexColor4 = skinManager.loadHexTexture("color4");
        hexColor5 = skinManager.loadHexTexture("color5");
        hexColor6 = skinManager.loadHexTexture("color6");

        loadSeaHexTextures();
    }

    private void loadSeaHexTextures() {
        SkinManager skinManager = getSkinManager();
        hexGreenSea = skinManager.loadSeaHexTexture("green");
        hexRedSea = skinManager.loadSeaHexTexture("red");
        hexBlueSea = skinManager.loadSeaHexTexture("blue");
        hexCyanSea = skinManager.loadSeaHexTexture("cyan");
        hexYellowSea = skinManager.loadSeaHexTexture("yellow");
        hexAlgaeSea = skinManager.loadSeaHexTexture("algae");
        hexLavenderSea = skinManager.loadSeaHexTexture("lavender");
        hexOrchidSea = skinManager.loadSeaHexTexture("orchid");
        hexPurpleSea= skinManager.loadSeaHexTexture("purple");
        hexRoseSea = skinManager.loadSeaHexTexture("rose");
        hexWhiskeySea = skinManager.loadSeaHexTexture("whiskey");
        hexWhiteSea = skinManager.loadSeaHexTexture("white");
        hexDarkblueSea = skinManager.loadSeaHexTexture("darkblue");
        hexWhiteblueSea = skinManager.loadSeaHexTexture("whiteblue");
        hexSeaSea = skinManager.loadSeaHexTexture("sea");
        hexPrussiaBlueSea = skinManager.loadSeaHexTexture("prussia_blue");
        hexLightYellowSea = skinManager.loadSeaHexTexture("light_yellow");
        hexBrownSea = skinManager.loadSeaHexTexture("brown");
        hexBlueGreenSea = skinManager.loadSeaHexTexture("bluegreen");
        hexDeepBlueSea = skinManager.loadSeaHexTexture("deepblue");
        hexDeepRedSea = skinManager.loadSeaHexTexture("deepred");
        hexGoldSea = skinManager.loadSeaHexTexture("gold");
        hexTeaSea = skinManager.loadSeaHexTexture("tea");
        hexPaperSea = skinManager.loadSeaHexTexture("paper");
        hexOldSea = skinManager.loadSeaHexTexture("old");
        hexBambooSea = skinManager.loadSeaHexTexture("bamboo");
        hexByzantineSea = skinManager.loadSeaHexTexture("byzantine");
        hexPinkSea = skinManager.loadSeaHexTexture("pink");
        hexMingSea = skinManager.loadSeaHexTexture("ming");
        hexGrayGreenSea = skinManager.loadSeaHexTexture("gray_green");

        hexColor1Sea = skinManager.loadSeaHexTexture("color1");
        hexColor2Sea = skinManager.loadSeaHexTexture("color2");
        hexColor3Sea = skinManager.loadSeaHexTexture("color3");
        hexColor4Sea = skinManager.loadSeaHexTexture("color4");
        hexColor5Sea = skinManager.loadSeaHexTexture("color5");
        hexColor6Sea = skinManager.loadSeaHexTexture("color6");

    }


    private void loadFieldTextures() {
        atlasLoader = getSkinManager().createAtlasLoader();
        selectionPixel = atlasLoader.getTexture("selection_pixel_lowest.png");
        manTextures = new Storage3xTexture[9];
        for (int i = 0; i < 9; i++) {
            manTextures[i] = new Storage3xTexture(atlasLoader, "man" + i + ".png");
        }

        manSeaTextures = new Storage3xTexture[9];
        for (int i = 0; i < 9; i++) {
            manSeaTextures[i] = new Storage3xTexture(atlasLoader, "man" + i + "_sea.png");
        }

        graveTexture = new Storage3xTexture(atlasLoader, "grave.png");
        graveSeaTexture = new Storage3xTexture(atlasLoader, "grave_sea.png");
        houseTexture = new Storage3xTexture(atlasLoader, "house.png");
        palmTexture = new Storage3xTexture[3];
        pineTexture = new Storage3xTexture[3];
        cityTexture = new Storage3xTexture[3];
        mountainTexture = new Storage3xTexture[2];
        palmTexture[0] = new Storage3xTexture(atlasLoader, "palm.png");
        palmTexture[1] = new Storage3xTexture(atlasLoader, "palm2.png");
        palmTexture[2] = new Storage3xTexture(atlasLoader, "palm3.png");
        pineTexture[0] = new Storage3xTexture(atlasLoader, "pine.png");
        pineTexture[1] = new Storage3xTexture(atlasLoader, "pine2.png");
        pineTexture[2] = new Storage3xTexture(atlasLoader, "pine3.png");
        towerTexture = new Storage3xTexture(atlasLoader, "tower.png");
        castleTexture = new Storage3xTexture(atlasLoader, "castle.png");
        farmTexture = new Storage3xTexture[6];
        farmTexture[0] = new Storage3xTexture(atlasLoader, "farm1.png");
        farmTexture[1] = new Storage3xTexture(atlasLoader, "farm2.png");
        farmTexture[2] = new Storage3xTexture(atlasLoader, "farm3.png");
        farmTexture[3] = new Storage3xTexture(atlasLoader, "farm4.png");
        farmTexture[4] = new Storage3xTexture(atlasLoader, "farm5.png");
        farmTexture[5] = new Storage3xTexture(atlasLoader, "farm6.png");
        strongTowerTexture = new Storage3xTexture(atlasLoader, "strong_tower.png");
        mountainTexture[0] = new Storage3xTexture(atlasLoader, "mountain.png");
        mountainTexture[1] = new Storage3xTexture(atlasLoader, "mountain2.png");
        hillTexture = new Storage3xTexture(atlasLoader, "hill.png");
        fortTexture = new Storage3xTexture(atlasLoader, "fort.png");
        cityTexture[0] = new Storage3xTexture(atlasLoader, "city.png");
        cityTexture[1] = new Storage3xTexture(atlasLoader, "city2.png");
        cityTexture[2] = new Storage3xTexture(atlasLoader, "city3.png");
        revoltTexture = new Storage3xTexture(atlasLoader, "revolt.png");
    }


    public void loadBackgroundTexture() {
        if (SettingsManager.waterTextureEnabled || getSkinManager().currentSkinRequiresWater()) {
            backgroundRegion = GraphicsYio.loadTextureRegion(getSkinManager().getWaterTexturePath(), true);
        } else {
            backgroundRegion = GraphicsYio.loadTextureRegion(getSkinManager().getGameBackgroundTexturePath(), true);
        }
    }


    public void loadSkin() {
        loadBackgroundTexture();
        loadHexTextures();
        reloadTextures();
        gameView.rList.loadTextures();
        selectionBorder = GraphicsYio.loadTextureRegion(getSkinManager().getSelectionBorderTexturePath(), false);
    }


    private void reloadTextures() {
        loadSkinDependentTextures();

        resetButtonTexture(38); // tower (build)
        resetButtonTexture(39); // unit (build)
    }


    public void onMoreSettingsChanged() {
        loadBackgroundTexture();
        loadSkin();
    }


    private void resetButtonTexture(int id) {
        ButtonYio button = gameView.yioGdxGame.menuControllerYio.getButtonById(id);
        if (button != null) {
            button.resetTexture();
        }
    }


    TextureRegion getTransitionTexture() {
        if (animationTextureRegion == null) {
            return backgroundRegion;
        }

        return animationTextureRegion;
    }


    public TextureRegion getHexTexture(int color) {
        switch (color) {
            default:
            case 0:
                return hexGreen;
            case 1:
                return hexRed;
            case 2:
                return hexBlue;
            case 3:
                return hexCyan;
            case 4:
                return hexYellow;
            case 5:
                return hexColor1;
            case 6:
                return hexColor2;
            case 7:
                return hexColor3;
            case 8:
                return hexColor4;
            case 9:
                return hexColor5;
            case 10:
                return hexColor6;
            case 11:
                return hexAlgae;
            case 12:
                return hexLavender;
            case 13:
                return hexOrchid;
            case 14:
                return hexPurple;
            case 15:
                return hexRose;
            case 16:
                return hexWhiskey;
            case 17:
                return hexWhite;
            case 18:
                return hexDarkblue;
            case 19:
                return hexWhiteblue;
            case 20:
                return hexSea;
            case 21:
                return hexLightYellow;
            case 22:
                return hexPrussiaBlue;
            case 23:
                return hexBrown;
            case 24:
                return hexBlueGreen;
            case 25:
                return hexDeepBlue;
            case 26:
                return hexDeepRed;
            case 27:
                return hexGold;
            case 28:
                return hexTea;
            case 29:
                return hexPaper;
            case 30:
                return hexOld;
            case 31:
                return hexBamboo;
            case 32:
                return hexByzantine;
            case 33:
                return hexPink;
            case 34:
                return hexMing;
            case 35:
                return hexGrayGreen;
        }
    }

    public TextureRegion getSeaHexTexture(int color) {
        switch (color) {
            default:
            case 0:
                return hexGreenSea;
            case 1:
                return hexRedSea;
            case 2:
                return hexBlueSea;
            case 3:
                return hexCyanSea;
            case 4:
                return hexYellowSea;
            case 5:
                return hexColor1Sea;
            case 6:
                return hexColor2Sea;
            case 7:
                return hexColor3Sea;
            case 8:
                return hexColor4Sea;
            case 9:
                return hexColor5Sea;
            case 10:
                return hexColor6Sea;
            case 11:
                return hexAlgaeSea;
            case 12:
                return hexLavenderSea;
            case 13:
                return hexOrchidSea;
            case 14:
                return hexPurpleSea;
            case 15:
                return hexRoseSea;
            case 16:
                return hexWhiskeySea;
            case 17:
                return hexWhiteSea;
            case 18:
                return hexDarkblueSea;
            case 19:
                return hexWhiteblueSea;
            case 20:
                return hexSeaSea;
            case 21:
                return hexLightYellowSea;
            case 22:
                return hexPrussiaBlueSea;
            case 23:
                return hexBrownSea;
            case 24:
                return hexBlueGreenSea;
            case 25:
                return hexDeepBlueSea;
            case 26:
                return hexDeepRedSea;
            case 27:
                return hexGoldSea;
            case 28:
                return hexTeaSea;
            case 29:
                return hexPaperSea;
            case 30:
                return hexOldSea;
            case 31:
                return hexBambooSea;
            case 32:
                return hexByzantineSea;
            case 33:
                return hexPinkSea;
            case 34:
                return hexMingSea;
            case 35:
                return hexGrayGreenSea;
        }
    }


    public TextureRegion getHexTextureByFraction(int fraction) {
        return getHexTexture(getGameController().getColorByFraction(fraction));
    }

    public TextureRegion getSeaHexTextureByFraction(int fraction) {
        return getSeaHexTexture(getGameController().getColorByFraction(fraction));
    }

    private SkinManager getSkinManager() {
        return gameView.getSkinManager();
    }


    TextureRegion getUnitTexture(Unit unit) {
        if (!getGameController().isPlayerTurn() && unit.moveFactor.get() < 1 && unit.moveFactor.get() > 0.1) {
            if(unit.currentHex.sea){
                return manSeaTextures[unit.strength - 1].getLowest();
            }else{
                return manTextures[unit.strength - 1].getLowest();
            }
        }
        if(unit.currentHex.sea){
            return manSeaTextures[unit.strength - 1].getTexture(gameView.currentZoomQuality);
        }else{
            return manTextures[unit.strength - 1].getTexture(gameView.currentZoomQuality);
        }

    }


    private GameController getGameController() {
        return gameView.yioGdxGame.gameController;
    }


    void disposeTextures() {
        backgroundRegion.getTexture().dispose();

        blackCircleTexture.getTexture().dispose();
        shadowHexTexture.getTexture().dispose();
        gradientShadow.getTexture().dispose();
        blackPixel.getTexture().dispose();
        transCircle1.getTexture().dispose();
        transCircle2.getTexture().dispose();
        selUnitShadow.getTexture().dispose();
        sideShadow.getTexture().dispose();
        responseAnimHexTexture.getTexture().dispose();
        selectionBorder.getTexture().dispose();
        forefingerTexture.getTexture().dispose();
        defenseIcon.getTexture().dispose();
        blackBorderTexture.getTexture().dispose();

        atlasLoader.disposeAtlasRegion();
        gameView.rList.disposeTextures();
    }
}
