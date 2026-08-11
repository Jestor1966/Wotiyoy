package yio.tro.antiyoy.ai.master;

import yio.tro.antiyoy.ai.AbstractAi;
import yio.tro.antiyoy.gameplay.Hex;

import java.util.ArrayList;

public abstract class PropagationCaster {

    AbstractAi abstractAi;
    ArrayList<Hex> propagationList;
    ArrayList<Hex> touchedHexes;


    public PropagationCaster(AbstractAi abstractAi) {
        this.abstractAi = abstractAi;
        propagationList = new ArrayList<>();
        touchedHexes = new ArrayList<>();
    }


    public abstract boolean isPropagationAllowed(Hex src, Hex dst);


    public abstract void onHexReached(Hex previousHex, Hex hex);


    public void perform(Hex start, int depth) {
        resetFlags();
        propagationList.clear();

        touchedHexes.add(start);
        propagationList.add(start);
        start.aiData.propCastValue = depth;

        applyMainCycle();
    }


    public void perform(ArrayList<Hex> launchList, int depth) {
        resetFlags();
        propagationList.clear();

        propagationList.addAll(launchList);
        for (Hex hex : launchList) {
            touchedHexes.add(hex);
            hex.aiData.propCastValue = depth;
        }

        applyMainCycle();
    }


    private void applyMainCycle() {
        int index = 0;
        while (index < propagationList.size()) {
            Hex hex = propagationList.get(index);
            index++;
            touchedHexes.add(hex);
            if (hex.aiData.propCastValue == 0) continue;

            for (int dir = 0; dir < 6; dir++) {
                Hex adjacentHex = hex.getAdjacentHex(dir);
                if (adjacentHex == null) continue;
                if (!adjacentHex.active) continue;
                if (adjacentHex.isNullHex()) continue;
                if (adjacentHex.aiData.propCastValue != -1) continue;
                if (!isPropagationAllowed(hex, adjacentHex)) continue;
                propagationList.add(adjacentHex);
                touchedHexes.add(adjacentHex);
                adjacentHex.aiData.propCastValue = hex.aiData.propCastValue - 1;
                onHexReached(hex, adjacentHex);
            }
        }
    }


    private void resetFlags() {
        for (Hex hex : touchedHexes) {
            hex.aiData.propCastValue = -1;
        }
        touchedHexes.clear();
    }
}
