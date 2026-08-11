package yio.tro.antiyoy.gameplay.replays.actions;

import yio.tro.antiyoy.gameplay.FieldManager;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;

import java.util.ArrayList;


public class RaCannonShot extends RepAction {

    Hex hex;


    public RaCannonShot(Hex hex) {
        this.hex = hex;
    }


    @Override
    public void initType() {
        type = CANNON_SHOT;
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
        gameController.fieldManager.cannonShot(hex);
    }


    @Override
    public String toString() {
        return "[Cannon shot: " + hex + "]";
    }
}
