package yio.tro.antiyoy.gameplay.game_view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.gameplay.CameraController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.stuff.RectangleYio;

public class RenderProvinceNames extends GameRender{


    private float hvSize;
    private float camSize;
    private TextureRegion greenPixel;
    private TextureRegion blackTriangle;
    private float f;
    private Color c;
    private Province province;
    public RectangleYio pos;
    RectangleYio trianglePos;
    PointYio textPosition;

    public RenderProvinceNames(GameRendersList gameRendersList) {
        super(gameRendersList);

        pos = new RectangleYio();
        trianglePos = new RectangleYio();
        textPosition = new PointYio();
    }


    @Override
    public void loadTextures() {
        greenPixel = loadTextureRegion("pixels/pixel_green.png", false);
        blackTriangle = loadTextureRegion("triangle.png", false);
    }


    @Override
    public void render() {
        if (!gameController.areCityNamesEnabled()) return;
        hvSize = gameView.hexViewSize;
        System.out.println("viewSize:"+hvSize);
        renderInNormalMode();
    }


    private void renderInNormalMode() {
        for (Province province : gameController.fieldManager.provinces) {
            renderSingleCityName(province);
        }
    }


    private void renderSingleCityName(Province province) {
        prepare(province);
        updateTextPosition();

        batchMovable.setColor(c.r, c.g, c.b, 0.3f + 0.7f * f);
        renderText();

        batchMovable.setColor(c.r, c.g, c.b, 1);
    }


    private void updateTextPosition() {
        textPosition.set(
                pos.x + 0.1f * hvSize,
                pos.y + 0.7f * hvSize
        );
    }


    private void renderText() {
        float bckpValue = Fonts.mapFont.getColor().r;
        Fonts.mapFont.setColor(0.9f, 0.9f, 0.9f, 1);
        Fonts.mapFont.draw(
                batchMovable,
                province.getName(),
                textPosition.x,
                textPosition.y
        );
        Fonts.mapFont.setColor(bckpValue, bckpValue, bckpValue, 1);
    }

    private void prepare(Province province) {
        this.province = province;
        c = batchMovable.getColor();
    }


    @Override
    public void disposeTextures() {
        greenPixel.getTexture().dispose();
        blackTriangle.getTexture().dispose();
    }
}
