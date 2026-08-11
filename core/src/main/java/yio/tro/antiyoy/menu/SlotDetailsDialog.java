package yio.tro.antiyoy.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import yio.tro.antiyoy.KeyboardManager;
import yio.tro.antiyoy.factor_yio.FactorYio;
import yio.tro.antiyoy.gameplay.ClickDetector;
import yio.tro.antiyoy.gameplay.editor.EditorSaveSystem;
import yio.tro.antiyoy.menu.keyboard.AbstractKbReaction;
import yio.tro.antiyoy.menu.render.MenuRender;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.LanguagesManager;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.stuff.RectangleYio;

import java.util.ArrayList;


/**
 * Modal dialog that shows and edits the stored information of an editor map
 * slot. Rows are plain left-aligned text near the top of the panel; clicking
 * a row opens the keyboard. Clicking outside the panel closes the dialog.
 * The description row wraps automatically and gets plenty of space.
 */
public class SlotDetailsDialog extends InterfaceElement {

    private static final int ROW_NAME = 0;
    private static final int ROW_AUTHOR = 1;
    private static final int ROW_TIME = 2;
    private static final int ROW_DESC = 3;
    private static final int ROWS_QUANTITY = 4;

    public RectangleYio position, viewPosition;
    public FactorYio appearFactor;
    private final MenuControllerYio menuControllerYio;
    private final PointYio currentTouch;
    private final ClickDetector clickDetector;
    private final String[] rowLabels;
    private final String[] rowValues;
    private final ArrayList<String> descLines;
    private final RectangleYio[] touchRects;
    private String slotKey;
    private int touchedRow;
    private boolean factorMoved;


    public SlotDetailsDialog(MenuControllerYio menuControllerYio) {
        super(-1);
        this.menuControllerYio = menuControllerYio;

        position = new RectangleYio();
        viewPosition = new RectangleYio();
        appearFactor = new FactorYio();
        currentTouch = new PointYio();
        clickDetector = new ClickDetector();
        rowLabels = new String[ROWS_QUANTITY];
        rowValues = new String[ROWS_QUANTITY];
        descLines = new ArrayList<>();
        touchRects = new RectangleYio[ROWS_QUANTITY];
        for (int i = 0; i < ROWS_QUANTITY; i++) {
            touchRects[i] = new RectangleYio();
        }
        slotKey = null;
        touchedRow = -1;
    }


    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
        updateTexts();
    }


    private void updateTexts() {
        rowLabels[ROW_NAME] = LanguagesManager.getInstance().getString("map_name") + ": " + getSlotValue(":name", getDefaultSlotName());
        rowLabels[ROW_AUTHOR] = LanguagesManager.getInstance().getString("author") + ": " + getSlotValue(":author", "Anonymous");
        rowLabels[ROW_TIME] = LanguagesManager.getInstance().getString("create_time") + ": " + getSlotValue(":create_time", "1970-01-01");
        rowLabels[ROW_DESC] = LanguagesManager.getInstance().getString("description") + ":";

        rowValues[ROW_NAME] = getSlotValue(":name", getDefaultSlotName());
        rowValues[ROW_AUTHOR] = getSlotValue(":author", "Anonymous");
        rowValues[ROW_TIME] = getSlotValue(":create_time", "1970-01-01");
        rowValues[ROW_DESC] = getSlotValue(":description", "");

        wrapDescription(rowValues[ROW_DESC], descLines);
    }


    private void wrapDescription(String source, ArrayList<String> target) {
        target.clear();
        if (source == null || source.length() == 0) {
            target.add("-");
            return;
        }

        BitmapFont font = Fonts.smallerMenuFont;
        float maxWidth = 0.88f * GraphicsYio.width;
        int maxLines = 6;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            builder.append(c);
            if (c == '\n' || GraphicsYio.getTextWidth(font, builder.toString()) >= maxWidth) {
                target.add(builder.toString().trim());
                builder.delete(0, builder.length());
            }
        }
        if (builder.length() > 0) {
            target.add(builder.toString().trim());
        }

        while (target.size() > maxLines) {
            String last = target.remove(target.size() - 1);
            if (target.size() == maxLines) {
                String previous = target.get(target.size() - 1);
                previous = previous.substring(0, Math.max(1, previous.length() - 3)) + "...";
                target.set(target.size() - 1, previous);
            }
        }
    }


    private float getRowDeltaY(int row) {
        switch (row) {
            case ROW_NAME:
                return (float) position.height - 0.10f * GraphicsYio.height;
            case ROW_AUTHOR:
                return (float) position.height - 0.16f * GraphicsYio.height;
            case ROW_TIME:
                return (float) position.height - 0.22f * GraphicsYio.height;
            default:
                return (float) position.height - 0.28f * GraphicsYio.height;
        }
    }


    private void updateTouchRects() {
        float x = (float) viewPosition.x + 0.06f * GraphicsYio.width;
        float width = 0.88f * GraphicsYio.width;
        float height = 0.055f * GraphicsYio.height;

        for (int i = ROW_NAME; i <= ROW_TIME; i++) {
            touchRects[i].set(x, (float) viewPosition.y + getRowDeltaY(i) - 0.045f * GraphicsYio.height, width, height);
        }

        float descTop = (float) viewPosition.y + getRowDeltaY(ROW_DESC) - 0.035f * GraphicsYio.height;
        float descHeight = 0.03f * GraphicsYio.height + descLines.size() * 0.026f * GraphicsYio.height;
        touchRects[ROW_DESC].set(x, descTop - descHeight, width, descHeight);
    }


    @Override
    public void move() {
        factorMoved = false;
        if (appearFactor.hasToMove()) {
            appearFactor.move();
            factorMoved = true;
        }
        if (factorMoved) {
            viewPosition.setBy(position);
            viewPosition.y -= (1 - appearFactor.get()) * (position.y + position.height + 0.1f * GraphicsYio.width);
        }
        updateTouchRects();
    }


    @Override
    public FactorYio getFactor() {
        return appearFactor;
    }


    @Override
    public void destroy() {
        appearFactor.destroy(2, 2);
        onDestroy();
    }


    private void onDestroy() {
        Scenes.sceneEditorLoad.reloadValues();
    }


    @Override
    public void appear() {
        appearFactor.setValues(0.01, 0);
        appearFactor.appear(MenuControllerYio.SPAWN_ANIM, MenuControllerYio.SPAWN_SPEED);
    }


    @Override
    public boolean isVisible() {
        return appearFactor.get() > 0;
    }


    @Override
    public boolean checkToPerformAction() {
        return false;
    }


    @Override
    public boolean isTouchable() {
        return true;
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        currentTouch.set(screenX, screenY);

        if (!viewPosition.isPointInside(currentTouch, 0)) {
            destroy();
            return true;
        }

        touchedRow = -1;
        for (int i = 0; i < ROWS_QUANTITY; i++) {
            if (isTouchInsideRectangle(currentTouch.x, currentTouch.y, touchRects[i], 0.02f * GraphicsYio.width)) {
                touchedRow = i;
                break;
            }
        }

        clickDetector.onTouchDown(currentTouch);
        return true;
    }


    @Override
    public boolean touchDrag(int screenX, int screenY, int pointer) {
        currentTouch.set(screenX, screenY);
        clickDetector.onTouchDrag(currentTouch);
        return true;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        currentTouch.set(screenX, screenY);
        clickDetector.onTouchUp(currentTouch);

        if (clickDetector.isClicked() && touchedRow >= 0) {
            performEdit(touchedRow);
        }

        touchedRow = -1;
        return true;
    }


    private void performEdit(int row) {
        switch (row) {
            case ROW_NAME:
                editValue(":name", getDefaultSlotName(), "Anonymous", false);
                break;
            case ROW_AUTHOR:
                editValue(":author", "Anonymous", "Anonymous", false);
                break;
            case ROW_TIME:
                editValue(":create_time", "1970-01-01", "1970-01-01", false);
                break;
            case ROW_DESC:
                editValue(":description", "", "", true);
                break;
        }
    }


    private void editValue(final String suffix, final String defaultValue, String fallback, final boolean allowEmpty) {
        if (slotKey == null) return;

        KeyboardManager.getInstance().apply(getSlotValue(suffix, defaultValue), new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                if (slotKey == null) return;
                if (!allowEmpty && input.length() == 0) input = fallback;
                getPreferences().putString(slotKey + suffix, input);
                getPreferences().flush();
                updateTexts();
            }
        });
    }


    private String getDefaultSlotName() {
        if (slotKey == null || slotKey.length() < 5) return "";
        return LanguagesManager.getInstance().getString("slot") + " " + slotKey.substring(4);
    }


    private String getSlotValue(String suffix, String defaultValue) {
        if (slotKey == null) return defaultValue;
        String value = getPreferences().getString(slotKey + suffix, defaultValue);
        if (value.length() == 0) return defaultValue;
        return value;
    }


    private Preferences getPreferences() {
        return Gdx.app.getPreferences(EditorSaveSystem.EDITOR_PREFS);
    }


    public float getTitleBaselineY() {
        return (float) (viewPosition.y + position.height - 0.04f * GraphicsYio.height);
    }


    public String getTitle() {
        return LanguagesManager.getInstance().getString("details");
    }


    public String getRowLabel(int row) {
        return rowLabels[row];
    }


    public float getRowBaselineY(int row) {
        return (float) viewPosition.y + getRowDeltaY(row);
    }


    public ArrayList<String> getDescLines() {
        return descLines;
    }


    public float getDescFirstLineY() {
        return (float) (viewPosition.y + position.height - 0.33f * GraphicsYio.height);
    }


    public float getDescLineStep() {
        return 0.026f * GraphicsYio.height;
    }


    @Override
    public void setTouchable(boolean touchable) {

    }


    @Override
    public void setPosition(RectangleYio position) {
        this.position.setBy(position);
        viewPosition.setBy(position);
    }


    @Override
    public MenuRender getRenderSystem() {
        return MenuRender.renderSlotDetailsDialog;
    }
}
