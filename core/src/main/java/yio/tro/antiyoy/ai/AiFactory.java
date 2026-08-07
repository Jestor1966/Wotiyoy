package yio.tro.antiyoy.ai;

import yio.tro.antiyoy.ai.master.AiMaster;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.rules.GameRules;

import java.util.ArrayList;

public class AiFactory {

    private final GameController gameController;
    ArrayList<AbstractAi> aiList;


    public AiFactory(GameController gameController) {
        this.gameController = gameController;
    }


    public void createAiList(int difficulty) {
        aiList = gameController.getAiList();
        aiList.clear();

        for (int i = 0; i < GameRules.fractionsQuantity; i++) {
            addAiToList(difficulty, i);
        }
    }


    public void createCustomAiList(int difficulties[]) {
        if (GameRules.fractionsQuantity != difficulties.length) {
            System.out.println("AiFactory.createCustomAiList(): problem");
        }

        aiList = gameController.getAiList();
        aiList.clear();

        for (int i = 0; i < GameRules.fractionsQuantity; i++) {
            addAiToList(difficulties[i], i);
        }
    }


    private void addAiToList(int difficulty, int fraction) {
        switch (difficulty) {
            default:
            case Difficulty.EASY:
                aiList.add(getMasterAi(fraction));
                break;
            case Difficulty.NORMAL:
                aiList.add(getMasterAi(fraction));
                break;
            case Difficulty.HARD:
                aiList.add(getMasterAi(fraction));
                break;
            case Difficulty.EXPERT:
                aiList.add(getMasterAi(fraction));
                break;
            case Difficulty.BALANCER:
                aiList.add(getBalancerAi(fraction));
                break;
            case Difficulty.MASTER:
                aiList.add(getMasterAi(fraction));
                break;
        }
    }


    private AbstractAi getMasterAi(int fraction) {
        return new AiMaster(gameController, fraction);
    }


    private ArtificialIntelligence getBalancerAi(int fraction) {
        return new AiBalancerGenericRules(gameController, fraction);
    }


    private ArtificialIntelligence getExpertAi(int fraction) {
        return new AiBalancerGenericRules(gameController, fraction);
    }


    private ArtificialIntelligence getHardAi(int fraction) {
        return new AiBalancerGenericRules(gameController, fraction);
    }


    private ArtificialIntelligence getNormalAi(int fraction) {
        return new AiBalancerGenericRules(gameController, fraction);
    }


    private ArtificialIntelligence getEasyAi(int fraction) {
        return new AiBalancerGenericRules(gameController, fraction);
    }

}