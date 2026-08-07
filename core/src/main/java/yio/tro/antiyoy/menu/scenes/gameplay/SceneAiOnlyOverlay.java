package yio.tro.antiyoy.menu.scenes.gameplay;

import yio.tro.antiyoy.SoundManagerYio;
import yio.tro.antiyoy.YioGdxGame;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.income_view.MoneyViewElement;
import yio.tro.antiyoy.menu.income_view.MveBehavior;
import yio.tro.antiyoy.menu.scenes.AbstractScene;
import yio.tro.antiyoy.menu.speed_panel.SpeedPanel;
import yio.tro.antiyoy.stuff.Fonts;

public class SceneAiOnlyOverlay extends AbstractScene {


    public SpeedPanel speedPanel;
    public ButtonYio inGameMenuButton;
    public ButtonYio coinButton;
    public MoneyViewElement turnMadeButton;


    public SceneAiOnlyOverlay(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        speedPanel = null;
        coinButton = null;
        turnMadeButton = null;
    }


    @Override
    public void create() {
        menuControllerYio.beginMenuCreation();

        createInGameMenuButton();

        createSpeedPanel();
        speedPanel.appear();
        createCoinButton();
        createTurnMadeButton();
        turnMadeButton.appear();

        menuControllerYio.endMenuCreation();
    }


    private void createInGameMenuButton() {
        inGameMenuButton = buttonFactory.getButton(generateSquare(1 - 0.07 / YioGdxGame.screenRatio, 0.93, 0.07), 530, null);
        menuControllerYio.loadButtonOnce(inGameMenuButton, "menu_icon.png");
        inGameMenuButton.setReaction(Reaction.rbPauseMenu);
        inGameMenuButton.setAnimation(Animation.up);
        inGameMenuButton.enableRectangularMask();
    }


    private void createSpeedPanel() {
        if (speedPanel != null) return;

        speedPanel = new SpeedPanel(menuControllerYio, -1);
        menuControllerYio.addElementToScene(speedPanel);
    }


    private void createCoinButton() {
        // important: there is another coin button on SceneFastConstruction
        coinButton = menuControllerYio.getButtonById(531);
        if (coinButton == null) { // init
            coinButton = buttonFactory.getButton(generateSquare(0, 0.93, 0.07), 531, null);
            coinButton.setAnimation(Animation.up);
            coinButton.setPressSound(SoundManagerYio.soundCoin);
            coinButton.enableRectangularMask();
        }
        loadCoinButtonTexture();
        coinButton.appearFactor.appear(3, 2);
        coinButton.setTouchable(true);
        coinButton.setReaction(Reaction.rbShowIncomeGraph);
    }

    private void createTurnMadeButton() {
        // important: there is another coin button on SceneFastConstruction
        turnMadeButton = new MoneyViewElement(menuControllerYio);
        turnMadeButton.setPosition(generateRectangle(0.4, 0.935, 0.2, 0.06));
        turnMadeButton.setFont(Fonts.smallerMenuFont);
        turnMadeButton.setCentered(true);
        turnMadeButton.setAnimation(Animation.up);
        turnMadeButton.setPlusNeeded(false);
        turnMadeButton.setTurn(true);
        turnMadeButton.setBehavior(getTurnMadeBehavior());
        menuControllerYio.addElementToScene(turnMadeButton);
    }

    private MveBehavior getTurnMadeBehavior() {
        return new MveBehavior() {
            @Override
            public int getTitleValue() {
                GameController gameController = menuControllerYio.yioGdxGame.gameController;
                int turnsMade = gameController.matchStatistics.turnsMade;
                return turnsMade;
            }
        };
    }


    public void onSkinChanged() {
        if (coinButton != null) {
            coinButton.resetTexture();
        }
    }


    void loadCoinButtonTexture() {
        menuControllerYio.loadButtonOnce(coinButton, menuControllerYio.yioGdxGame.skinManager.getCoinTexturePath());
    }
}
