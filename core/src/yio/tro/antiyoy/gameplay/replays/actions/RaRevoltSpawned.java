package yio.tro.antiyoy.gameplay.replays.actions;

import yio.tro.antiyoy.gameplay.FieldManager;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Obj;

import java.util.ArrayList;

public class RaRevoltSpawned extends RepAction{

    Hex hex;


    public RaRevoltSpawned(Hex hex) {
        this.hex = hex;
    }


    @Override
    public void initType() {
        type = REVOLT_SPAWNED;
    }


    @Override
    public String saveInfo() {
        return convertHexToTwoTokens(hex);
    }


    @Override
    public void loadInfo(FieldManager fieldManager, String source) {
        ArrayList<String> strings = convertSourceStringToList(source);
        hex = getHexByTwoTokens(fieldManager, strings.get(0), strings.get(1));
    }


    @Override
    public void perform(GameController gameController) {
        gameController.fieldManager.addSolidObject(hex, Obj.REVOLT);
    }


    @Override
    public String toString() {
        return "[Pine spawned: " +
                hex +
                "]";
    }
}
