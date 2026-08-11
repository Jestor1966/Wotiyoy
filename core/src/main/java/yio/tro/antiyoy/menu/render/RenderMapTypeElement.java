package yio.tro.antiyoy.menu.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.menu.InterfaceElement;
import yio.tro.antiyoy.menu.map_type_picking.MapTypeElement;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.RectangleYio;
import yio.tro.antiyoy.stuff.RenderableTextYio;

public class RenderMapTypeElement extends MenuRender {

    private MapTypeElement mapTypeElement;
    private float alpha;
    private RectangleYio viewPosition;
    private TextureRegion grayPixel;


    @Override
    public void loadTextures() {
        grayPixel = GraphicsYio.loadTextureRegion("pixels/gray_pixel.png", false);
    }


    @Override
    public void renderFirstLayer(InterfaceElement element) {

    }


    @Override
    public void renderSecondLayer(InterfaceElement element) {
        mapTypeElement = (MapTypeElement) element;
        alpha = mapTypeElement.getAlpha();
        viewPosition = mapTypeElement.viewPosition;

        GraphicsYio.setBatchAlpha(batch, 0.15 * alpha);
        renderBackground();
        GraphicsYio.setBatchAlpha(batch, alpha);
        renderValue();
        renderTitle();
        renderSelection();
        GraphicsYio.setBatchAlpha(batch, 1);
    }


    private void renderBackground() {
        GraphicsYio.drawByRectangle(batch, grayPixel, viewPosition);
    }


    private void renderValue() {
        renderText(mapTypeElement.valueText);
    }


    private void renderTitle() {
        renderText(mapTypeElement.title);
    }


    private void renderText(RenderableTextYio text) {
        BitmapFont font = text.font;
        Color color = font.getColor();
        font.setColor(Color.BLACK);
        GraphicsYio.renderTextOptimized(batch, getBlackPixel(), text, alpha);
        font.setColor(color);
    }


    private void renderSelection() {
        if (!mapTypeElement.selectionEngineYio.isSelected()) return;
        GraphicsYio.setBatchAlpha(batch, mapTypeElement.selectionEngineYio.getAlpha() * alpha);
        GraphicsYio.drawByRectangle(batch, getBlackPixel(), mapTypeElement.viewPosition);
    }


    @Override
    public void renderThirdLayer(InterfaceElement element) {

    }
}
