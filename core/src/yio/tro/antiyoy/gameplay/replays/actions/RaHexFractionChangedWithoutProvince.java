package yio.tro.antiyoy.gameplay.replays.actions;

import yio.tro.antiyoy.gameplay.FieldManager;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;

import java.util.ArrayList;

public class RaHexFractionChangedWithoutProvince extends RepAction{

    // should be used only when fraction changed without normal reason (cheats, for example)
    Hex hex;
    int newFraction;


    public RaHexFractionChangedWithoutProvince(Hex hex, int newFraction) {
        this.hex = hex;
        this.newFraction = newFraction;
    }


    @Override
    public void initType() {
        type = HEX_CHANGED_FRACTION_WITHOUT_PROVINCE;
    }


    @Override
    public String saveInfo() {
        return convertHexToTwoTokens(hex) + newFraction;
    }


    @Override
    public void loadInfo(FieldManager fieldManager, String source) {
        ArrayList<String> strings = convertSourceStringToList(source);
        hex = getHexByTwoTokens(fieldManager, strings.get(0), strings.get(1));
        newFraction = Integer.valueOf(strings.get(2));
    }


    @Override
    public void perform(GameController gameController) {
        hex.fraction=newFraction;
        //hex.changingFraction = true;
        hex.animFactor.setValues(0, 0);
        hex.animFactor.appear(1, 1);

        //gameController.fieldManager.tryToDetectAdditionalProvinces();
    }


    @Override
    public String toString() {
        return "[Hex changed fraction]";
    }
}
