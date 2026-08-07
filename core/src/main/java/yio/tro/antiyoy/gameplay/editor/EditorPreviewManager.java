package yio.tro.antiyoy.gameplay.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Base64Coder;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.FieldManager;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Obj;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.PointYio;

import java.util.ArrayList;

public class EditorPreviewManager {

    // every map hex occupies exactly one pixel of the preview image
    public static String encodePreview(GameController gameController) {
        ArrayList<Hex> activeHexes = gameController.fieldManager.activeHexes;
        if (activeHexes.size() == 0) return null;

        FieldManager fieldManager = gameController.fieldManager;
        float stepX = fieldManager.hexStep2 * fieldManager.sin60;
        float stepY = fieldManager.hexStep1;

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (Hex hex : activeHexes) {
            PointYio pos = hex.getPos();
            float x = pos.x;
            float y = pos.y;
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        int widthX = (int) ((maxX - minX) / stepX) + 1;
        int heightY = (int) ((maxY - minY) / stepY) + 1;

        boolean landscape = GameRules.landscape;
        int width = landscape ? heightY : widthX;
        int height = landscape ? widthX : heightY;

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (Hex hex : activeHexes) {
            PointYio pos = hex.getPos();
            int px = Math.round((pos.x - minX) / stepX);
            int py = Math.round((pos.y - minY) / stepY);

            int x, y;
            if (landscape) {
                x = py;
                y = px;
            } else {
                x = px;
                y = height - 1 - py;
            }
            if (x < 0) x = 0;
            if (x >= width) x = width - 1;
            if (y < 0) y = 0;
            if (y >= height) y = height - 1;
            pixmap.drawPixel(x, y, getPreviewColor(hex));
        }

        FileHandle tempFile = Gdx.files.local("tmp/preview.png");
        tempFile.parent().mkdirs();
        PixmapIO.writePNG(tempFile, pixmap);
        byte[] bytes = tempFile.readBytes();
        tempFile.delete();
        pixmap.dispose();

        return String.valueOf(Base64Coder.encode(bytes));
    }


    public static TextureRegion decodePreview(String base64) {
        if (base64 == null || base64.length() == 0) return null;

        try {
            byte[] bytes = Base64Coder.decode(base64);
            Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            pixmap.dispose();
            return new TextureRegion(texture);
        } catch (Exception e) {
            return null;
        }
    }


    // placeholder color mapping: replace this method when the real mapping table is provided
    private static int getPreviewColor(Hex hex) {
        if (hex.sea) {
            return Color.rgba8888(0.25f, 0.5f, 0.85f, 1f); // sea
        }

        if (hex.objectInside == Obj.MOUNTAIN) {
            return Color.rgba8888(0.4f, 0.35f, 0.3f, 1f); // mountain
        }
        if (hex.objectInside == Obj.HILL) {
            return Color.rgba8888(0.55f, 0.5f, 0.4f, 1f); // hill
        }

        switch (hex.fraction) {
            default:
                return Color.rgba8888(0.5f, 0.5f, 0.5f, 1f); // neutral grey
            case 0:
                return Color.rgba8888(0.25f, 0.65f, 0.3f, 1f);
            case 1:
                return Color.rgba8888(0.85f, 0.3f, 0.3f, 1f);
            case 2:
                return Color.rgba8888(0.3f, 0.45f, 0.85f, 1f);
            case 3:
                return Color.rgba8888(0.3f, 0.8f, 0.8f, 1f);
            case 4:
                return Color.rgba8888(0.85f, 0.8f, 0.3f, 1f);
            case 5:
                return Color.rgba8888(0.8f, 0.55f, 0.25f, 1f);
            case 6:
                return Color.rgba8888(0.65f, 0.4f, 0.8f, 1f);
        }
    }
}
