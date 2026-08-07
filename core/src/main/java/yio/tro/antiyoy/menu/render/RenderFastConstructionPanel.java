package yio.tro.antiyoy.menu.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.Storage3xTexture;
import yio.tro.antiyoy.gameplay.skins.SkinManager;
import yio.tro.antiyoy.menu.InterfaceElement;
import yio.tro.antiyoy.menu.fast_construction.FastConstructionPanel;
import yio.tro.antiyoy.menu.fast_construction.FcpItem;
import yio.tro.antiyoy.stuff.AtlasLoader;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.RectangleYio;

public class RenderFastConstructionPanel extends MenuRender{


    private TextureRegion backgroundTexture;
    private TextureRegion selectionPixel;
    private FastConstructionPanel panel;
    private float factor;
    private RectangleYio pos;
    private TextureRegion man0;
    private TextureRegion man1;
    private TextureRegion man2;
    private TextureRegion man3;
    private TextureRegion man4;
    private TextureRegion man5;
    private TextureRegion man6;
    private TextureRegion man7;
    private TextureRegion man8;
    private TextureRegion tower;
    private TextureRegion strongTower;
    private TextureRegion house;
    private TextureRegion sideShadow;
    private float smDelta;
    private TextureRegion endTurnIcon;
    private TextureRegion undoIcon;
    private TextureRegion diplomacyIcon;
    private TextureRegion mailIconTexture;
    AtlasLoader atlasLoader;


    @Override
    public void loadTextures() {
        backgroundTexture = GraphicsYio.loadTextureRegion("pixels/gray_pixel.png", false);
        selectionPixel = GraphicsYio.loadTextureRegion("pixels/black_pixel.png", false);
        sideShadow = GraphicsYio.loadTextureRegion("money_shadow.png", true);
        endTurnIcon = GraphicsYio.loadTextureRegion("end_turn.png", true);
        undoIcon = GraphicsYio.loadTextureRegion("undo.png", true);
        diplomacyIcon = GraphicsYio.loadTextureRegion("diplomacy/flag.png", true);
        mailIconTexture = GraphicsYio.loadTextureRegion("diplomacy/mail_icon.png", true);

        loadSkinDependentTextures();
    }


    private void loadSkinDependentTextures() {
        atlasLoader = getSkinManager().createAtlasLoader();
        man0 = loadFromFieldElements("man0");
        man1 = loadFromFieldElements("man1");
        man2 = loadFromFieldElements("man2");
        man3 = loadFromFieldElements("man3");
        man4 = loadFromFieldElements("man4");
        man5 = loadFromFieldElements("man5");
        man6 = loadFromFieldElements("man6");
        man7 = loadFromFieldElements("man7");
        man8 = loadFromFieldElements("man8");
        tower = loadFromFieldElements("tower");
        strongTower = loadFromFieldElements("strong_tower");
        house = loadFromFieldElements("house");
    }


    public void onSkinChanged() {
        loadSkinDependentTextures();
    }


    private TextureRegion loadFromFieldElements(String name) {
        return (new Storage3xTexture(atlasLoader, name + ".png")).getNormal();
    }


    private SkinManager getSkinManager() {
        return menuViewYio.yioGdxGame.skinManager;
    }


    @Override
    public void renderFirstLayer(InterfaceElement element) {

    }


    @Override
    public void renderSecondLayer(InterfaceElement element) {
        panel = (FastConstructionPanel) element;
        factor = panel.getFactor().get();
        pos = panel.viewPosition;

        GraphicsYio.setBatchAlpha(batch, factor);

        renderShadow();
        renderBackground();
        renderItems();

        GraphicsYio.setBatchAlpha(batch, 1);
    }


    private void renderShadow() {
        smDelta = 0.1f * h * (1 - factor);
        batch.draw(sideShadow, 0, -smDelta + 0.03f * h, w, 0.1f * h);
    }


    private void renderItems() {
        for (FcpItem item : panel.items) {
            if (!item.isVisible()) continue;

            GraphicsYio.drawFromCenter(
                    batch,
                    getItemTexture(item),
                    item.position.x,
                    item.position.y,
                    item.radius
            );

            if (item.isSelected()) {
                GraphicsYio.setBatchAlpha(batch, 0.5f * item.selectionFactor.get());

                GraphicsYio.drawFromCenter(
                        batch,
                        selectionPixel,
                        item.position.x,
                        item.position.y,
                        item.radius
                );

                GraphicsYio.setBatchAlpha(batch, factor);
            }
        }
    }


    private void renderBackground() {
        GraphicsYio.drawByRectangle(
                batch,
                backgroundTexture,
                pos
        );
    }


    private TextureRegion getItemTexture(FcpItem item) {
        switch (item.actionType) {
            default:
                return getSkinDependentItemTexture(item);
            case undo:
                return undoIcon;
            case end_turn:
                return endTurnIcon;
            case diplomacy:
                return diplomacyIcon;
            case log:
                return mailIconTexture;
        }
    }


    private TextureRegion getSkinDependentItemTexture(FcpItem item) {
        switch (item.actionType) {
            default: return null;
            case unit_1: return man0;
            case unit_2: return man1;
            case unit_3: return man2;
            case unit_4: return man3;
            case unit_5: return man4;
            case unit_6: return man5;
            case unit_7: return man6;
            case unit_8: return man7;
            case unit_9: return man8;
            case farm: return house;
            case tower: return tower;
            case strong_tower: return strongTower;
        }
    }


    @Override
    public void renderThirdLayer(InterfaceElement element) {

    }
}
