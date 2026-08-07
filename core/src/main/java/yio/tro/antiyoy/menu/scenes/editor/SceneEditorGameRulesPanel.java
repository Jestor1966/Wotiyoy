package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.menu.*;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.LanguagesManager;

public class SceneEditorGameRulesPanel extends AbstractEditorPanel{

    private ButtonYio basePanel;
    private double bottom;
    private double pHeight;
    private CheckButtonYio chkFog;
    private CheckButtonYio chkDiplomacy;
    private CheckButtonYio chkLockRelations;
    private CheckButtonYio chkEnableEvent;
    private CheckButtonYio chkEnableRebel;
    private CheckButtonYio chkEnableCaptainCity;
    private CheckButtonYio chkKeepDifferentNames;
    private ButtonYio nextPageButton;
    private ButtonYio previousPageButton;
    private ButtonYio pageLabel;
    private int currentPage;

    EditGoalElement editGoalElement;


    public SceneEditorGameRulesPanel(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        initMetrics();
        chkFog = null;
        editGoalElement = null;
    }


    private void initMetrics() {
        bottom = SceneEditorOverlay.PANEL_HEIGHT;
        pHeight = 0.72;
    }


    @Override
    public void create() {
        createBasePanel();

        createNextPageButton();
        createPreviousPageButton();
        createPageLabel();
        createCheckButtons();
        createEditGoalElement();

        loadValues();
    }


    private void createEditGoalElement() {
        initEditGoalElement();
        editGoalElement.appear();
    }


    private void initEditGoalElement() {
        if (editGoalElement != null) return;

        editGoalElement = new EditGoalElement(menuControllerYio);
        editGoalElement.setPosition(generateRectangle(0.08, 0.1, 0.84, 0.1));
        editGoalElement.setAnimation(Animation.down);
        menuControllerYio.addElementToScene(editGoalElement);
    }


    private void createCheckButtons() {
        initCheckButtons();
        currentPage = 0;
        applyPage();
    }


    private void createNextPageButton() {
        nextPageButton = buttonFactory.getButton(generateSquare(0.85, 0.22, 0.07), 940, "»");
        nextPageButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                currentPage = 1;
                applyPage();
            }
        });
        nextPageButton.setCustomFont(Fonts.biggerButtonFont, (int) (1.5f * Fonts.FONT_SIZE + 10));
        menuControllerYio.buttonRenderer.renderButton(nextPageButton);
        nextPageButton.setShadow(false);
        nextPageButton.setVisualHook(basePanel);
    }


    private void createPreviousPageButton() {
        previousPageButton = buttonFactory.getButton(generateSquare(0.08, 0.22, 0.07), 941, "«");
        previousPageButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                currentPage = 0;
                applyPage();
            }
        });
        previousPageButton.setCustomFont(Fonts.biggerButtonFont, (int) (1.5f * Fonts.FONT_SIZE + 10));
        menuControllerYio.buttonRenderer.renderButton(previousPageButton);
        previousPageButton.setShadow(false);
        previousPageButton.setVisualHook(basePanel);
    }


    private void createPageLabel() {
        pageLabel = buttonFactory.getButton(generateRectangle(0.3, 0.22, 0.4, 0.07), 942, null);
        pageLabel.setTouchable(false);
        pageLabel.setShadow(false);
        pageLabel.setVisualHook(basePanel);
    }


    private void updatePageLabel() {
        if (pageLabel == null) return;
        pageLabel.setTextLine(LanguagesManager.getInstance().getString("page", currentPage + 1));
        menuControllerYio.buttonRenderer.renderButton(pageLabel);
    }


    private void applyPage() {
        if (currentPage == 0) {
            chkFog.appear();
            chkDiplomacy.appear();
            chkLockRelations.appear();
            chkEnableEvent.appear();
            chkEnableCaptainCity.appear();
            chkEnableRebel.appear();
            chkKeepDifferentNames.destroy();
            nextPageButton.appear();
            nextPageButton.setTouchable(true);
            previousPageButton.destroy();
        } else {
            chkFog.destroy();
            chkDiplomacy.destroy();
            chkLockRelations.destroy();
            chkEnableEvent.destroy();
            chkEnableCaptainCity.destroy();
            chkEnableRebel.destroy();
            chkKeepDifferentNames.appear();
            previousPageButton.appear();
            previousPageButton.setTouchable(true);
            nextPageButton.destroy();
        }
        updatePageLabel();
    }


    private void initCheckButtons() {
        if (chkFog != null) return;

        chkFog = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkFog.setParent(basePanel);
        chkFog.alignTop(0.02);
        chkFog.setTitle(getString("fog_of_war"));
        chkFog.centerHorizontal(0.05);



        chkEnableCaptainCity = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkEnableCaptainCity.setParent(basePanel);
        chkEnableCaptainCity.alignUnderPreviousElement();
        chkEnableCaptainCity.setTitle(getString("captain_city"));
        chkEnableCaptainCity.centerHorizontal(0.05);

        chkEnableEvent = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkEnableEvent.setParent(basePanel);
        chkEnableEvent.alignUnderPreviousElement();
        chkEnableEvent.setTitle(getString("enable_event"));
        chkEnableEvent.centerHorizontal(0.05);

        chkDiplomacy = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkDiplomacy.setParent(basePanel);
        chkDiplomacy.alignUnderPreviousElement();
        chkDiplomacy.setTitle(getString("diplomacy"));
        chkDiplomacy.centerHorizontal(0.05);

        chkEnableRebel = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkEnableRebel.setParent(basePanel);
        chkEnableRebel.alignUnderPreviousElement();
        chkEnableRebel.setTitle(getString("enable_rebel"));
        chkEnableRebel.centerHorizontal(0.05);

        chkLockRelations = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkLockRelations.setParent(basePanel);
        chkLockRelations.alignUnderPreviousElement();
        chkLockRelations.setTitle(getString("lock_relations"));
        chkLockRelations.centerHorizontal(0.05);

        chkKeepDifferentNames = CheckButtonYio.getFreshCheckButton(menuControllerYio);
        chkKeepDifferentNames.setParent(basePanel);
        chkKeepDifferentNames.alignTop(0.02);
        chkKeepDifferentNames.setTitle(getString("different_province_names_no_merge"));
        chkKeepDifferentNames.centerHorizontal(0.05);
    }


    private void saveValues() {
        if (!saveAllowed) return;
        GameRules.editorFog = chkFog.isChecked();
        GameRules.editorDiplomacy = chkDiplomacy.isChecked();
        GameRules.diplomaticRelationsLocked = chkLockRelations.isChecked();
        GameRules.eventEnabled = chkEnableEvent.isChecked();
        GameRules.enableRebel = chkEnableRebel.isChecked();
        GameRules.captainAsCityRules = chkEnableCaptainCity.isChecked();
        GameRules.keepDifferentProvinceNames = chkKeepDifferentNames.isChecked();
        if (editGoalElement != null) {
            editGoalElement.saveValues();
        }
    }


    private void loadValues() {
        chkFog.setChecked(GameRules.editorFog);
        chkDiplomacy.setChecked(GameRules.editorDiplomacy);
        chkLockRelations.setChecked(GameRules.diplomaticRelationsLocked);
        chkEnableEvent.setChecked(GameRules.eventEnabled);
        chkEnableRebel.setChecked(GameRules.enableRebel);
        chkEnableCaptainCity.setChecked(GameRules.captainAsCityRules);
        chkKeepDifferentNames.setChecked(GameRules.keepDifferentProvinceNames);
        editGoalElement.loadValues();
    }


    private void createBasePanel() {
        basePanel = buttonFactory.getButton(generateRectangle(0, bottom, 1, pHeight), 920, null);
        if (basePanel.notRendered()) {
            basePanel.cleatText();
            basePanel.addEmptyLines(1);
            basePanel.loadCustomBackground("gray_pixel.png");
            basePanel.setIgnorePauseResume(true);
            menuControllerYio.buttonRenderer.renderButton(basePanel);
        }
        basePanel.setTouchable(false);
        basePanel.setAnimation(Animation.fixed_down);
        basePanel.enableRectangularMask();
        basePanel.setShadow(true);
    }


    @Override
    public void hide() {
        if (editGoalElement != null && editGoalElement.getFactor().get() > 0.1) {
            saveValues();
        }

        destroyByIndex(920, 929);

        chkFog.destroy();
        chkDiplomacy.destroy();
        chkLockRelations.destroy();
        chkEnableEvent.destroy();
        chkEnableCaptainCity.destroy();
        chkEnableRebel.destroy();
        if (nextPageButton != null) {
            nextPageButton.destroy();
        }
        if (previousPageButton != null) {
            previousPageButton.destroy();
        }
        if (pageLabel != null) {
            pageLabel.destroy();
        }

        if (editGoalElement != null) {
            editGoalElement.destroy();
        }
    }


    @Override
    public boolean isCurrentlyOpened() {
        return basePanel.appearFactor.get() == 1;
    }
}
