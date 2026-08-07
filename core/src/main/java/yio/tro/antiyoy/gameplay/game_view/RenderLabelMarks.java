package yio.tro.antiyoy.gameplay.game_view;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.HexLabel;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.GraphicsYio;


/**
 * Draws hex labels. When a label is visible it is rendered both in the level
 * editor and during normal gameplay.
 */
public class RenderLabelMarks extends GameRender {

    private static final float OUTLINE_OFFSET = 2f;

    private GlyphLayout glyphLayout;
    private TextureRegion redDotTexture;


    public RenderLabelMarks(GameRendersList gameRendersList) {
        super(gameRendersList);
        glyphLayout = new GlyphLayout();
    }


    @Override
    public void loadTextures() {
        redDotTexture = loadTextureRegion("pixels/red_dot.png", true);
    }


    @Override
    public void render() {
        for (Hex hex : gameController.fieldManager.activeHexes) {
            if (!hex.active) continue;
            if (hex.label == null || !hex.label.isVisible) continue;
            if (hex.label.labeltext == null || hex.label.labeltext.length() == 0) continue;
            if (!isPosInViewFrame(hex.getPos(), hexViewSize)) continue;

            renderLabel(hex);
        }

        Fonts.gameFont.setColor(1, 1, 1, 1);
        Fonts.gameFont.getData().setScale(1);
    }


    private void renderLabel(Hex hex) {
        HexLabel label = hex.label;
        String text = label.labeltext;

        float zoom = gameController.cameraController.viewZoomLevel;
        float nativeFontSize = 0.8f * Fonts.FONT_SIZE;
        if (nativeFontSize <= 0) nativeFontSize = 1;
        float scale = label.labelSize / nativeFontSize;
        scale /= (float) Math.sqrt(Math.max(0.1f, zoom));
        if (scale > 25f) scale = 25f;
        if (scale < 0.4f) scale = 0.4f;

        Fonts.gameFont.getData().setScale(scale);
        glyphLayout.setText(Fonts.gameFont, text);

        float x = hex.pos.x - glyphLayout.width / 2;
        float y = hex.pos.y + 0.5f * Fonts.gameFont.getCapHeight();
        float alpha = 1f - label.transparent;
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;

        if (label.isOutline) {
            Fonts.gameFont.setColor(0, 0, 0, alpha);
            Fonts.gameFont.draw(batchMovable, text, x - OUTLINE_OFFSET, y);
            Fonts.gameFont.draw(batchMovable, text, x + OUTLINE_OFFSET, y);
            Fonts.gameFont.draw(batchMovable, text, x, y - OUTLINE_OFFSET);
            Fonts.gameFont.draw(batchMovable, text, x, y + OUTLINE_OFFSET);
        }

        float[] rgb = label.getColorRgb();
        Fonts.gameFont.setColor(rgb[0], rgb[1], rgb[2], alpha);
        Fonts.gameFont.draw(batchMovable, text, x, y);

        if (GameRules.inEditorMode) {
            renderOriginDot(hex, zoom, alpha);
        }
    }


    private void renderOriginDot(Hex hex, float zoom, float alpha) {
        float dotRadius = 0.12f * hexViewSize;
        dotRadius /= (float) Math.sqrt(Math.max(0.1f, zoom));
        if (dotRadius < 3f) dotRadius = 3f;
        if (dotRadius > 12f) dotRadius = 12f;

        batchMovable.setColor(1, 0, 0, alpha);
        GraphicsYio.drawFromCenter(batchMovable, redDotTexture, hex.pos.x, hex.pos.y, dotRadius);
        batchMovable.setColor(1, 1, 1, 1);
    }


    @Override
    public void disposeTextures() {
        if (redDotTexture != null) {
            redDotTexture.getTexture().dispose();
        }
    }
}
