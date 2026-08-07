package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.factor_yio.FactorYio;
import yio.tro.antiyoy.gameplay.ClickDetector;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.InterfaceElement;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.render.MenuRender;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.stuff.RectangleYio;
import yio.tro.antiyoy.stuff.scroll_engine.ScrollEngineYio;

import java.util.ArrayList;

public class EditorObjectPanelScroll extends InterfaceElement {

    MenuControllerYio menuControllerYio;
    public RectangleYio panelPosition;
    FactorYio appearFactor;
    PointYio currentTouch, lastTouch;
    ClickDetector clickDetector;
    ScrollEngineYio scrollEngineYio;
    ArrayList<ButtonYio> scrollButtons;
    float[] baseX;
    float scrollOffset;
    float itemSpacing, itemWidth;
    float scrollAreaStartX, scrollAreaEndX;
    boolean touched, active;


    public EditorObjectPanelScroll(MenuControllerYio menuControllerYio, int id) {
        super(id);
        this.menuControllerYio = menuControllerYio;

        panelPosition = new RectangleYio();
        appearFactor = new FactorYio();
        currentTouch = new PointYio();
        lastTouch = new PointYio();
        clickDetector = new ClickDetector();
        scrollEngineYio = new ScrollEngineYio();
        scrollEngineYio.setFriction(0.05);
        scrollEngineYio.setSoftLimitOffset(0.02f * GraphicsYio.width);
        scrollButtons = new ArrayList<>();
        baseX = new float[0];
        scrollOffset = 0;
        itemSpacing = 0;
        itemWidth = 0;
        scrollAreaStartX = 0;
        scrollAreaEndX = GraphicsYio.width;
        touched = false;
        active = false;
        appearFactor.setValues(1, 0);
    }


    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            touched = false;
        }
    }


    public void setPanelPosition(RectangleYio panelPosition) {
        this.panelPosition.setBy(panelPosition);
    }


    public void setup(ArrayList<ButtonYio> buttons) {
        scrollButtons.clear();
        scrollButtons.addAll(buttons);

        if (buttons.size() > 0) {
            ButtonYio firstButton = buttons.get(0);
            scrollAreaStartX = (float) firstButton.position.x;
            itemWidth = (float) firstButton.position.width;
            if (buttons.size() > 1) {
                ButtonYio secondButton = buttons.get(1);
                itemSpacing = (float) (secondButton.position.x - firstButton.position.x);
            }
        } else {
            scrollAreaStartX = 0;
            itemWidth = 0;
            itemSpacing = 0;
        }

        scrollAreaEndX = GraphicsYio.width - 0.02f * GraphicsYio.width;

        baseX = new float[buttons.size()];
        for (int i = 0; i < buttons.size(); i++) {
            baseX[i] = (float) buttons.get(i).position.x;
        }

        scrollOffset = 0;
        updateScrollEngineLimits();
        scrollEngineYio.resetToBottom();
    }


    private void updateScrollEngineLimits() {
        float viewportWidth = Math.max(0, scrollAreaEndX - scrollAreaStartX);

        float contentWidth = 0;
        if (scrollButtons.size() > 0) {
            contentWidth = (scrollButtons.size() - 1) * itemSpacing + itemWidth;
        }

        scrollEngineYio.setSlider(0, viewportWidth);
        scrollEngineYio.setLimits(0, contentWidth);
    }


    @Override
    public void move() {
        scrollEngineYio.move();
        scrollOffset = (float) scrollEngineYio.getSlider().a;

        if (!active) return;

        for (int i = 0; i < scrollButtons.size(); i++) {
            scrollButtons.get(i).position.x = baseX[i] - scrollOffset;
        }
    }


    @Override
    public FactorYio getFactor() {
        return appearFactor;
    }


    @Override
    public void destroy() {

    }


    @Override
    public void appear() {

    }


    @Override
    public boolean isVisible() {
        return true;
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
        if (!active) return false;
        if (!isTouchInsidePanelArea(screenX, screenY)) return false;

        lastTouch.set(screenX, screenY);
        currentTouch.set(screenX, screenY);
        touched = true;

        clickDetector.onTouchDown(currentTouch);
        scrollEngineYio.onTouchDown();
        return true;
    }


    private boolean isTouchInsidePanelArea(int screenX, int screenY) {
        float offset = 0.01f * GraphicsYio.width;
        if (screenX < panelPosition.x - offset) return false;
        if (screenX > panelPosition.x + panelPosition.width + offset) return false;
        if (screenY < panelPosition.y) return false; // don't steal touches from the main editor bar below
        if (screenY > panelPosition.y + panelPosition.height + offset) return false;
        return true;
    }


    @Override
    public boolean touchDrag(int screenX, int screenY, int pointer) {
        if (!touched) return false;

        lastTouch.setBy(currentTouch);
        currentTouch.set(screenX, screenY);

        scrollEngineYio.setSpeed(lastTouch.x - currentTouch.x);
        clickDetector.onTouchDrag(currentTouch);
        return true;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!touched) return false;

        lastTouch.setBy(currentTouch);
        currentTouch.set(screenX, screenY);

        scrollEngineYio.onTouchUp();
        clickDetector.onTouchUp(currentTouch);
        touched = false;

        if (clickDetector.isClicked()) {
            ButtonYio buttonYio = getButtonUnderTouch(currentTouch);
            if (buttonYio != null && buttonYio.isTouchable()) {
                buttonYio.press(screenX, screenY);
            }
        }

        return true;
    }


    private ButtonYio getButtonUnderTouch(PointYio touchPoint) {
        float touchOffset = 0.02f * GraphicsYio.width;
        for (int id = 160; id <= 175; id++) {
            ButtonYio buttonYio = menuControllerYio.getButtonById(id);
            if (buttonYio == null) continue;
            if (!buttonYio.isVisible()) continue;
            if (!buttonYio.isTouchable()) continue;
            if (isTouchInsideRectangle(
                    touchPoint.x,
                    touchPoint.y,
                    (float) buttonYio.position.x,
                    (float) buttonYio.position.y,
                    (float) buttonYio.position.width,
                    (float) buttonYio.position.height,
                    touchOffset
            )) return buttonYio;
        }
        return null;
    }


    @Override
    public boolean onMouseWheelScrolled(int amount) {
        if (!active) return false;

        if (amount == 1) {
            scrollEngineYio.giveImpulse(0.02f * GraphicsYio.width);
        } else if (amount == -1) {
            scrollEngineYio.giveImpulse(-0.02f * GraphicsYio.width);
        }

        return true;
    }


    @Override
    public void setTouchable(boolean touchable) {

    }


    @Override
    public void setPosition(RectangleYio position) {
        panelPosition.setBy(position);
    }


    @Override
    public MenuRender getRenderSystem() {
        return MenuRender.renderNothing;
    }
}
