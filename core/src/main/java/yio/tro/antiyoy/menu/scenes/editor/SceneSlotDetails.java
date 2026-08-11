package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.SlotDetailsDialog;
import yio.tro.antiyoy.menu.scenes.gameplay.AbstractModalScene;
import yio.tro.antiyoy.stuff.GraphicsYio;


public class SceneSlotDetails extends AbstractModalScene {

    public SlotDetailsDialog dialog;
    private String slotKey;


    public SceneSlotDetails(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);
        dialog = null;
    }


    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
    }


    @Override
    public void create() {
        if (slotKey == null) return;

        if (dialog == null) {
            dialog = new SlotDetailsDialog(menuControllerYio);
            dialog.setPosition(generateRectangle(0, 0.12, 1, GraphicsYio.convertToHeight(0.85)));
            menuControllerYio.addElementToScene(dialog);
        }

        dialog.setSlotKey(slotKey);
        dialog.appear();
        forceElementToTop(dialog);
    }


    @Override
    public void hide() {
        if (dialog != null) {
            dialog.destroy();
        }
    }
}
