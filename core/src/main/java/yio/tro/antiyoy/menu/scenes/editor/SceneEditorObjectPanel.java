package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.YioGdxGame;
import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.behaviors.editor.EditorReactions;
import yio.tro.antiyoy.stuff.RectangleYio;

import java.util.ArrayList;

public class SceneEditorObjectPanel extends AbstractEditorPanel{


    private ButtonYio basePanel;
    private ButtonYio cancelButton;
    private EditorObjectPanelScroll scrollController;
    private ArrayList<ButtonYio> objectButtons;


    public SceneEditorObjectPanel(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        objectButtons = new ArrayList<>();
    }


    @Override
    public void create() {
        basePanel = buttonFactory.getButton(generateRectangle(0, SceneEditorOverlay.PANEL_HEIGHT, 1, SceneEditorOverlay.PANEL_HEIGHT), 160, null);
        menuControllerYio.loadButtonOnce(basePanel, "gray_pixel.png");
        basePanel.setTouchable(false);

        cancelButton = buttonFactory.getButton(generateSquare(0, SceneEditorOverlay.PANEL_HEIGHT, SceneEditorOverlay.PANEL_HEIGHT), 162, null);
        menuControllerYio.loadButtonOnce(cancelButton, "cancel_icon.png");
        cancelButton.setReaction(EditorReactions.rbInputModeSetObject);

        objectButtons.clear();
        ButtonYio objectButton;
        for (int i = 0; i < 13; i++) {
            RectangleYio position = generateSquare(
                    (0.07 + 0.07 * i) / YioGdxGame.screenRatio,
                    SceneEditorOverlay.PANEL_HEIGHT,
                    SceneEditorOverlay.PANEL_HEIGHT
            );
            objectButton = buttonFactory.getButton(position, 163 + i, null);
            objectButton.setPosition(position);
            objectButton.setReaction(EditorReactions.rbInputModeSetObject);
            objectButtons.add(objectButton);

            switch (i) {
                case 0:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/pine_low.png");
                    break;
                case 1:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/palm_low.png");
                    break;
                case 2:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/house_low.png");
                    break;
                case 3:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/tower_low.png");
                    break;
                case 4:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/man0_low.png");
                    break;
                case 5:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/man1_low.png");
                    break;
                //case 6:
                    //menuControllerYio.loadButtonOnce(objectButton, "field_elements/man2_low.png");
                    //break;
                case 7:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/mountain.png");
                    break;
                case 8:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/hill.png");
                    break;
                case 9:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/fort.png");
                    break;
                case 10:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/city.png");
                    break;
                case 11:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/revolt.png");
                    break;
                case 12:
                    menuControllerYio.loadButtonOnce(objectButton, "field_elements/label_mark.png");
                    break;
            }
        }

        for (int i = 160; i <= 175; i++) {
            ButtonYio buttonYio = menuControllerYio.getButtonById(i);
            if (buttonYio == null) continue;

            buttonYio.appearFactor.appear(MenuControllerYio.SPAWN_ANIM, MenuControllerYio.SPAWN_SPEED);
            buttonYio.enableRectangularMask();
            buttonYio.setAnimation(Animation.down);
        }

        forceElementToTop(cancelButton);
        initScrollController();
    }


    private void initScrollController() {
        if (scrollController == null) {
            scrollController = new EditorObjectPanelScroll(menuControllerYio, -1);
            scrollController.setPanelPosition(generateRectangle(0, SceneEditorOverlay.PANEL_HEIGHT, 1, SceneEditorOverlay.PANEL_HEIGHT));
            menuControllerYio.addElementToScene(scrollController);
        }

        scrollController.setup(objectButtons);
        scrollController.setActive(true);
    }


    @Override
    public void hide() {
        if (scrollController != null) {
            scrollController.setActive(false);
        }

        for (int i = 160; i <= 175; i++) {
            ButtonYio buttonYio = menuControllerYio.getButtonById(i);
            if (buttonYio == null) continue;
            buttonYio.destroy();
        }
    }


    @Override
    public boolean isCurrentlyOpened() {
        return basePanel != null && basePanel.appearFactor.get() == 1;
    }
}
