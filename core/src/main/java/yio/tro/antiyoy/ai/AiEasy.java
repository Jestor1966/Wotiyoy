package yio.tro.antiyoy.ai;

import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.gameplay.Unit;

import java.util.ArrayList;


public class AiEasy extends ArtificialIntelligence {

    public AiEasy(GameController gameController, int fraction) {
        super(gameController, fraction);
    }


    @Override
    public void makeMove() {
        moveUnits();

        spendMoneyAndMergeUnits();
        moveAfkUnits();
    }


    @Override
    void decideAboutUnit(Unit unit, ArrayList<Hex> moveZone, Province province) {
        // 海上单位交给海上远征逻辑：沿最短海路前往最近的非己方大陆
        if (unit.currentHex.isSea()) {
            moveAfkUnit(province, unit);
            return;
        }

        if (checkToCleanSomeTrees(unit, moveZone, province)) return;
        gameController.moveUnit(unit, moveZone.get(random.nextInt(moveZone.size())), province);
    }


    @Override
    void tryToBuildUnits(Province province) {
        for (int i = 1; i <= 9; i++) {
            if (!province.canAiAffordUnit(i)) break;
            while (canProvinceBuildUnit(province, i)) {
                int moneyBefore = province.money;
                if (!tryToBuiltUnitInsideProvince(province, i)) break;
                if (province.money == moneyBefore) break;
            }
        }

        // this is to kick start province
        if (canProvinceBuildUnit(province, 1) && howManyUnitsInProvince(province) <= 1)
            tryToAttackWithStrength(province, 1);
    }


    @Override
    void tryToBuildTowers(Province province) {
        return; // easy AI can't build towers
    }


    @Override
    protected boolean mergeConditions(Province province, Unit unit, Hex hex) {
        return super.mergeConditions(province, unit, hex) && unit.strength == 1 && hex.unit.strength == 1;
    }


    @Override
    void mergeUnits(Province province) {
        if (random.nextDouble() < 0.25) {
            super.mergeUnits(province);
            return;
        }
    }
}
