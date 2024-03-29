package yio.tro.antiyoy.menu.scenes.gameplay;

import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.diplomatic_dialogs.DipMessageDialog;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.stuff.GraphicsYio;

public class SceneEvent extends AbstractModalScene {

    public DipMessageDialog dialog;


    public SceneEvent(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        dialog = null;
    }


    @Override
    public void create() {
        if (dialog == null) {
            initDialog();
        }

        dialog.appear();
    }


    private void initDialog() {
        dialog = new DipMessageDialog(menuControllerYio);

        dialog.setPosition(generateRectangle(0, 0.15, 1, GraphicsYio.convertToHeight(0.6)));

        menuControllerYio.addElementToScene(dialog);
    }


    public void showEvent(String titleKey, String eventKey) {
        create();
        dialog.setEvent(getString(titleKey), eventKey,true);
        forceElementToTop(dialog);
    }


    public boolean isCurrentlyVisible() {
        if (dialog == null) return false;
        return dialog.isVisible();
    }


    @Override
    public void hide() {
        dialog.destroy();
    }
}
