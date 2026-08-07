package yio.tro.antiyoy.gameplay.game_view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.Fonts;

import java.util.ArrayList;

public class RenderProvinceNames extends GameRender{


    private static final float MIN_ZOOM_TO_SHOW_NAMES = 1.3f;
    private static final int MIN_HEXES_TO_SHOW_NAME = 4;
    // independent, flexible font size adjustment for province names
    public static float provinceNameFontScale = 1.8f;

    private float hvSize;
    private Color c;
    private Province province;
    private GlyphLayout glyphLayout;
    private String name;
    private int charCount;
    private float[] charX, charY, charAngle;
    private float tRange;
    private float baseAngleRad, cosA, sinA, nx, ny;
    private Matrix4 tempTransform;
    private Matrix4 tempOldTransform;

    public RenderProvinceNames(GameRendersList gameRendersList) {
        super(gameRendersList);

        glyphLayout = new GlyphLayout();
        tempTransform = new Matrix4();
        tempOldTransform = new Matrix4();
    }


    @Override
    public void loadTextures() {

    }


    @Override
    public void render() {
        if (!SettingsManager.provinceNamesEnabled) return;
        if (gameController.cameraController.viewZoomLevel < MIN_ZOOM_TO_SHOW_NAMES) return;
        hvSize = gameView.hexViewSize;
        renderInNormalMode();
    }


    private void renderInNormalMode() {
        ArrayList<Province> provinces = gameController.fieldManager.provinces;

        if (!GameRules.keepDifferentProvinceNames) {
            for (Province province : provinces) {
                if (province.hexList.size() < MIN_HEXES_TO_SHOW_NAME) continue;
                renderSingleCityName(province, province.hexList, 1f, 1f);
            }
            return;
        }

        ArrayList<ArrayList<Province>> groups = gameView.rList.renderInsideProvinceBorder.getGroups();
        for (ArrayList<Province> group : groups) {
            if (group.size() >= 2) {
                Province biggest = group.get(0);
                for (Province p : group) {
                    if (p.hexList.size() > biggest.hexList.size()) biggest = p;
                }

                ArrayList<Hex> combinedHexes = new ArrayList<>();
                for (Province p : group) {
                    combinedHexes.addAll(p.hexList);
                }

                for (Province p : group) {
                    if (p == biggest) {
                        // biggest province: its name's long axis considers the whole group
                        renderSingleCityName(p, combinedHexes, 1f, 1f);
                    } else {
                        // smaller provinces: smaller font, 50% transparency
                        renderSingleCityName(p, p.hexList, 0.5f, 0.6f);
                    }
                }
            } else {
                renderSingleCityName(group.get(0), group.get(0).hexList, 1f, 1f);
            }
        }
    }


    private void renderSingleCityName(Province province, ArrayList<Hex> hexes, float alpha, float scaleFactor) {
        prepare(province);
        updateTextPosition(hexes, scaleFactor);

        batchMovable.setColor(c.r, c.g, c.b, alpha);
        renderText();
        batchMovable.setColor(c.r, c.g, c.b, 1);
    }


    private void updateTextPosition(ArrayList<Hex> hexes, float scaleFactor) {
        float centerX = 0, centerY = 0;
        for (Hex hex : hexes) {
            centerX += hex.pos.x;
            centerY += hex.pos.y;
        }
        centerX /= hexes.size();
        centerY /= hexes.size();

        // main axis: direction towards the hex farthest from the center
        Hex farHex = hexes.get(0);
        float maxDistance = 0;
        for (Hex hex : hexes) {
            float dx = hex.pos.x - centerX;
            float dy = hex.pos.y - centerY;
            float d = dx * dx + dy * dy;
            if (d > maxDistance) {
                maxDistance = d;
                farHex = hex;
            }
        }

        baseAngleRad = (float) Math.atan2(farHex.pos.y - centerY, farHex.pos.x - centerX);
        float angleDeg = (float) Math.toDegrees(baseAngleRad);
        while (angleDeg > 90) angleDeg -= 180;
        while (angleDeg < -90) angleDeg += 180;
        if (angleDeg > 30) angleDeg = 30;
        if (angleDeg < -30) angleDeg = -30;
        baseAngleRad = (float) Math.toRadians(angleDeg);
        cosA = (float) Math.cos(baseAngleRad);
        sinA = (float) Math.sin(baseAngleRad);
        nx = -sinA;
        ny = cosA;

        float minT = Float.MAX_VALUE, maxT = -Float.MAX_VALUE;
        for (Hex hex : hexes) {
            float t = (hex.pos.x - centerX) * cosA + (hex.pos.y - centerY) * sinA;
            if (t < minT) minT = t;
            if (t > maxT) maxT = t;
        }
        tRange = maxT - minT;
        if (tRange <= 0) tRange = 1;

        name = province.getName();
        charCount = name.length();

        Fonts.mapFont.getData().setScale(getFontScale() * scaleFactor);
        glyphLayout.setText(Fonts.mapFont, name);
        if (charX == null || charX.length != charCount) {
            charX = new float[charCount];
            charY = new float[charCount];
            charAngle = new float[charCount];
        }

        float textWidth = glyphLayout.width;
        float arcAmp = 0;
        float charStep;
        if (textWidth > tRange) {
            // name is too long for the province axis: compress it and bend it slightly
            float overflow = textWidth - 0.85f * tRange;
            arcAmp = Math.min(0.06f * tRange, 0.5f * overflow);
            charStep = (0.9f * tRange) / charCount;
        } else {
            charStep = textWidth / charCount;
            if (charStep <= 0) charStep = 1;
        }

        float cursorT = -charStep * (charCount - 1) / 2f;
        for (int i = 0; i < charCount; i++) {
            float frac = charCount > 1 ? i / (charCount - 1f) : 0.5f;
            float t = cursorT + i * charStep;
            float v = arcAmp * (float) Math.sin(Math.PI * frac);
            charX[i] = centerX + t * cosA + v * nx;
            charY[i] = centerY + t * sinA + v * ny;

            angleDeg = (float) Math.toDegrees(baseAngleRad);
            if (arcAmp > 0) {
                float eps = 0.05f;
                float f0 = Math.max(0, frac - eps);
                float f1 = Math.min(1, frac + eps);
                float v0 = arcAmp * (float) Math.sin(Math.PI * f0);
                float v1 = arcAmp * (float) Math.sin(Math.PI * f1);
                float slope = (v1 - v0) / ((f1 - f0) * tRange);
                angleDeg += (float) Math.toDegrees(Math.atan(slope));
            }
            charAngle[i] = angleDeg;
        }
    }


    private float getFontScale() {
        float zoom = gameController.cameraController.viewZoomLevel;

        // measure the name at unit scale, then fit it to 90% of the province's long axis
        Fonts.mapFont.getData().setScale(1);
        glyphLayout.setText(Fonts.mapFont, name);
        float widthAtUnitScale = glyphLayout.width;
        if (widthAtUnitScale <= 0) widthAtUnitScale = 1;

        float scale = (0.9f * tRange) / widthAtUnitScale;
        scale *= provinceNameFontScale;
        scale /= (float) Math.sqrt(zoom);

        if (scale > 5f) scale = 5f;
        if (scale < 0.7f) scale = 0.7f;
        return scale;
    }


    private void renderText() {
        float bckpValue = Fonts.mapFont.getColor().r;

        float outline = 2f;
        Fonts.mapFont.setColor(0, 0, 0, 1);
        for (int i = 0; i < charCount; i++) {
            drawCharacter(i, -outline, 0);
            drawCharacter(i, outline, 0);
            drawCharacter(i, 0, -outline);
            drawCharacter(i, 0, outline);
        }

        Fonts.mapFont.setColor(0.9f, 0.9f, 0.9f, 1);
        for (int i = 0; i < charCount; i++) {
            drawCharacter(i, 0, 0);
        }

        Fonts.mapFont.setColor(bckpValue, bckpValue, bckpValue, 1);
        Fonts.mapFont.getData().setScale(1);
    }


    private void drawCharacter(int index, float offsetX, float offsetY) {
        float x = charX[index] + offsetX;
        float y = charY[index] + offsetY;

        if (charAngle[index] == 0) {
            Fonts.mapFont.draw(batchMovable, String.valueOf(name.charAt(index)), x, y);
            return;
        }

        tempOldTransform.set(batchMovable.getTransformMatrix());
        tempTransform.idt();
        tempTransform.translate(x, y, 0);
        tempTransform.rotate(0, 0, 1, charAngle[index]);
        tempTransform.translate(-x, -y, 0);
        batchMovable.setTransformMatrix(tempTransform);
        Fonts.mapFont.draw(batchMovable, String.valueOf(name.charAt(index)), x, y);
        batchMovable.setTransformMatrix(tempOldTransform);
    }

    private void prepare(Province province) {
        this.province = province;
        c = batchMovable.getColor();
    }


    @Override
    public void disposeTextures() {

    }


}
