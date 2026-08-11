package yio.tro.antiyoy.gameplay;

import java.util.Random;

public class DefaultMapGenerator extends MapGenerator {


    public DefaultMapGenerator(GameController gameController) {
        super(gameController);
    }


    @Override
    public void generateMap(Random random, Hex field[][]) {
        setValues(random, field);

        beginGeneration();

        createLand();
        removeSingleHoles();
        addTrees();
        balanceMap();

        endGeneration();
    }
}
