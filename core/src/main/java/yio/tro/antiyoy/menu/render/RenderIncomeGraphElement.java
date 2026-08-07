package yio.tro.antiyoy.menu.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.gameplay.ColorsManager;
import yio.tro.antiyoy.gameplay.skins.SkinManager;
import yio.tro.antiyoy.menu.InterfaceElement;
import yio.tro.antiyoy.menu.income_graph.IgeItem;
import yio.tro.antiyoy.menu.income_graph.IncomeGraphElement;
import yio.tro.antiyoy.stuff.GraphicsYio;

public class RenderIncomeGraphElement extends MenuRender{


    private TextureRegion backgroundTexture;
    private IncomeGraphElement incomeGraphElement;
    private float alpha;
    private TextureRegion borderTexture;
    private TextureRegion pixelGreen;
    private TextureRegion pixelRed;
    private TextureRegion pixelBlue;
    private TextureRegion pixelCyan;
    private TextureRegion pixelYellow;
    private TextureRegion pixelAlgae;
    private TextureRegion pixelLavender;
    private TextureRegion pixelOrchid;
    private TextureRegion pixelPurple;
    private TextureRegion pixelRose;
    private TextureRegion pixelWhiskey;
    private TextureRegion pixelWhite;
    private TextureRegion pixelDarkblue;
    private TextureRegion pixelWhiteblue;
    private TextureRegion pixelSea;
    private TextureRegion pixelLightYellow;
    private TextureRegion pixelPrussiaBlue;
    private TextureRegion pixelBrown;
    private TextureRegion pixelBlueGreen;
    private TextureRegion pixelDeepBlue;
    private TextureRegion pixelDeepRed;
    private TextureRegion pixelGold;
    private TextureRegion pixelTea;
    private TextureRegion pixelGrayGreen;
    private TextureRegion pixelMing;
    private TextureRegion pixelByzantine;
    private TextureRegion pixelPink;
    private TextureRegion pixelBamboo;
    private TextureRegion pixelOld;
    private TextureRegion pixelPaper;

    private TextureRegion pixelColor1;
    private TextureRegion pixelColor2;
    private TextureRegion pixelColor3;
    private TextureRegion pixelColor4;
    private TextureRegion pixelColor5;
    private TextureRegion pixelColor6;
    private TextureRegion grayPixel;


    @Override
    public void loadTextures() {
        backgroundTexture = GraphicsYio.loadTextureRegion("diplomacy/background.png", false);
        borderTexture = GraphicsYio.loadTextureRegion("menu/separator.png", true);
        grayPixel = GraphicsYio.loadTextureRegion("pixels/gray_pixel.png", false);
        loadSkinDependentTextures();
    }


    private void loadSkinDependentTextures() {
        pixelGreen = loadColorPixel("green");
        pixelRed = loadColorPixel("red");
        pixelBlue = loadColorPixel("blue");
        pixelCyan = loadColorPixel("cyan");
        pixelYellow = loadColorPixel("yellow");
        pixelAlgae = loadColorPixel("color12");
        pixelLavender=loadColorPixel("color13");
        pixelOrchid=loadColorPixel("color14");
        pixelPurple=loadColorPixel("color15");
        pixelRose=loadColorPixel("color16");
        pixelWhiskey=loadColorPixel("color17");
        pixelWhite=loadColorPixel("color18");
        pixelDarkblue=loadColorPixel("color19");
        pixelWhiteblue=loadColorPixel("color20");
        pixelSea=loadColorPixel("color21");
        pixelLightYellow=loadColorPixel("color22");
        pixelPrussiaBlue=loadColorPixel("color23");
        pixelBrown=loadColorPixel("color24");
        pixelBlueGreen=loadColorPixel("color25");
        pixelDeepBlue=loadColorPixel("color26");
        pixelDeepRed=loadColorPixel("color27");
        pixelGold=loadColorPixel("color28");
        pixelTea=loadColorPixel("color29");
        pixelPaper=loadColorPixel("color30");
        pixelOld=loadColorPixel("color31");
        pixelBamboo=loadColorPixel("color32");
        pixelByzantine=loadColorPixel("color33");
        pixelPink=loadColorPixel("color34");
        pixelMing=loadColorPixel("color35");
        pixelGrayGreen=loadColorPixel("color36");




        pixelColor1 = loadColorPixel("color1");
        pixelColor2 = loadColorPixel("color2");
        pixelColor3 = loadColorPixel("color3");
        pixelColor4 = loadColorPixel("color4");
        pixelColor5 = loadColorPixel("color5");
        pixelColor6 = loadColorPixel("color6");

    }


    private TextureRegion loadColorPixel(String name) {
        SkinManager skinManager = menuViewYio.yioGdxGame.skinManager;
        return GraphicsYio.loadTextureRegion(skinManager.getColorPixelsFolderPath() + "/" + name + ".png", false);
    }


    @Override
    public void renderFirstLayer(InterfaceElement element) {

    }


    @Override
    public void renderSecondLayer(InterfaceElement element) {
        incomeGraphElement = (IncomeGraphElement) element;
        alpha = incomeGraphElement.getFactor().get();

        MenuRender.renderShadow.renderShadow(incomeGraphElement.viewPosition, alpha);
        GraphicsYio.setBatchAlpha(batch, alpha);
        renderInternals();
        GraphicsYio.setBatchAlpha(batch, 1);
    }


    private void renderInternals() {
        BitmapFont font = incomeGraphElement.getFont();
        font.setColor(Color.BLACK);

        renderBackground();
        renderTitle();
        renderSeparator();
        renderBorders();
        renderItems();

        font.setColor(Color.WHITE);
    }


    private void renderBorders() {
        for (IgeItem item : incomeGraphElement.items) {
            if (item.borderFactor.get() == 0) continue;
            if (item.text.string.equals("0")) continue;
            GraphicsYio.setBatchAlpha(batch, alpha * item.borderFactor.get());
            GraphicsYio.renderBorder(batch, borderTexture, item.borderPosition);
        }
        GraphicsYio.setBatchAlpha(batch, alpha);
    }


    private void renderItems() {
        for (IgeItem item : incomeGraphElement.items) {
            renderSingleItem(item);
        }
    }


    private void renderSingleItem(IgeItem item) {
        GraphicsYio.drawByRectangle(
                batch,
                getTextureForItem(item),
                item.viewPosition
        );
        GraphicsYio.renderTextOptimized(batch, getBlackPixel(), item.text, alpha);
    }


    public TextureRegion getTextureForItem(IgeItem item) {
        if (!item.scouted) return grayPixel;
        ColorsManager colorsManager = getGameView().gameController.colorsManager;
        int colorByFraction = colorsManager.getColorByFraction(item.fraction);
        return getPixelByColor(colorByFraction);
    }


    public TextureRegion getPixelByColor(int color) {
        switch (color) {
            default:
            case 0:
                return pixelGreen;
            case 1:
                return pixelRed;
            case 2:
                return pixelBlue;
            case 3:
                return pixelCyan;
            case 4:
                return pixelYellow;
            case 5:
                return pixelColor1;
            case 6:
                return pixelColor2;
            case 7:
                return pixelColor3;
            case 8:
                return pixelColor4;
            case 9:
                return pixelColor5;
            case 10:
                return pixelColor6;
            case 11:
                return pixelAlgae;
            case 12:
                return pixelLavender;
            case 13:
                return pixelOrchid;
            case 14:
                return pixelPurple;
            case 15:
                return pixelRose;
            case 16:
                return pixelWhiskey;
            case 17:
                return pixelWhite;
            case 18:
                return pixelDarkblue;
            case 19:
                return pixelWhiteblue;
            case 20:
                return pixelSea;
            case 21:
                return pixelLightYellow;
            case 22:
                return pixelPrussiaBlue;
            case 23:
                return pixelBrown;
            case 24:
                return pixelBlueGreen;
            case 25:
                return pixelDeepBlue;
            case 26:
                return pixelDeepRed;
            case 27:
                return pixelGold;
            case 28:
                return pixelTea;
            case 29:
                return pixelPaper;
            case 30:
                return pixelOld;
            case 31:
                return pixelBamboo;
            case 32:
                return pixelByzantine;
            case 33:
                return pixelPink;
            case 34:
                return pixelMing;
            case 35:
                return pixelGrayGreen;
        }
    }


    private void renderSeparator() {
        GraphicsYio.drawByRectangle(batch, getBlackPixel(), incomeGraphElement.separatorPosition);
    }


    private void renderInnerAreaBorder() {
        GraphicsYio.renderBorder(batch, getBlackPixel(), incomeGraphElement.columnsArea);
    }


    private void renderTitle() {
        BitmapFont titleFont = incomeGraphElement.title.font;
        Color previousColor = titleFont.getColor();
        titleFont.setColor(Color.BLACK);
        GraphicsYio.renderTextOptimized(batch, getBlackPixel(), incomeGraphElement.title, alpha);
        titleFont.setColor(previousColor);
    }


    public void onSkinChanged() {
        loadSkinDependentTextures();
    }


    private void renderBackground() {
        GraphicsYio.drawByRectangle(batch, backgroundTexture, incomeGraphElement.viewPosition);
    }


    @Override
    public void renderThirdLayer(InterfaceElement element) {

    }
}
