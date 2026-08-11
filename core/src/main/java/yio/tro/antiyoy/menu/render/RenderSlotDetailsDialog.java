package yio.tro.antiyoy.menu.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.menu.InterfaceElement;
import yio.tro.antiyoy.menu.SlotDetailsDialog;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.GraphicsYio;


public class RenderSlotDetailsDialog extends MenuRender {

    private TextureRegion backgroundTexture;
    private SlotDetailsDialog dialog;


    @Override
    public void loadTextures() {
        backgroundTexture = GraphicsYio.loadTextureRegion("pixels/gray_pixel.png", false);
    }


    @Override
    public void renderFirstLayer(InterfaceElement element) {

    }


    @Override
    public void renderSecondLayer(InterfaceElement element) {
        dialog = (SlotDetailsDialog) element;
        float alpha = dialog.getFactor().get();
        if (alpha <= 0.01) return;

        GraphicsYio.setBatchAlpha(batch, alpha);
        renderBackground();
        renderTitle();
        renderRows();
        GraphicsYio.setBatchAlpha(batch, 1);
    }


    @Override
    public void renderThirdLayer(InterfaceElement element) {

    }


    private void renderBackground() {
        GraphicsYio.drawByRectangle(batch, backgroundTexture, dialog.viewPosition);
    }


    private void renderTitle() {
        BitmapFont font = Fonts.gameFont;
        Color color = font.getColor();
        font.setColor(Color.BLACK);
        GraphicsYio.setFontAlpha(font, dialog.getFactor().get());

        float textWidth = GraphicsYio.getTextWidth(font, dialog.getTitle());
        font.draw(
                batch,
                dialog.getTitle(),
                (float) (dialog.viewPosition.x + dialog.viewPosition.width / 2 - textWidth / 2),
                dialog.getTitleBaselineY()
        );

        GraphicsYio.setFontAlpha(font, 1);
        font.setColor(color);
    }


    private void renderRows() {
        BitmapFont font = Fonts.smallerMenuFont;
        Color color = font.getColor();
        font.setColor(Color.BLACK);
        GraphicsYio.setFontAlpha(font, dialog.getFactor().get());

        float x = (float) dialog.viewPosition.x + 0.06f * GraphicsYio.width;
        for (int i = 0; i < 3; i++) {
            font.draw(batch, dialog.getRowLabel(i), x, dialog.getRowBaselineY(i));
        }

        // description label + wrapped lines
        font.draw(batch, dialog.getRowLabel(3), x, dialog.getRowBaselineY(3));
        float lineY = dialog.getDescFirstLineY();
        for (String line : dialog.getDescLines()) {
            font.draw(batch, line, x, lineY);
            lineY -= dialog.getDescLineStep();
        }

        GraphicsYio.setFontAlpha(font, 1);
        font.setColor(color);
    }
}
