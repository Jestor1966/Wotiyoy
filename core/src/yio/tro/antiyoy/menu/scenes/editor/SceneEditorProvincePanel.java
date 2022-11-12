package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.KeyboardManager;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.MoveZoneManager;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.gameplay.editor.EditorProvinceData;
import yio.tro.antiyoy.gameplay.touch_mode.TmShowChosenHexes;
import yio.tro.antiyoy.gameplay.touch_mode.TouchMode;
import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.TextLabelElement;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.keyboard.AbstractKbReaction;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.menu.scenes.gameplay.AbstractModalScene;
import yio.tro.antiyoy.menu.scenes.gameplay.SceneAreaSelectionUI;
import yio.tro.antiyoy.menu.scenes.gameplay.SceneHexPurchaseDialog;
import yio.tro.antiyoy.menu.slider.SliderBehavior;
import yio.tro.antiyoy.menu.slider.SliderYio;
import yio.tro.antiyoy.stuff.GraphicsYio;

import java.util.ArrayList;

public class SceneEditorProvincePanel extends AbstractModalScene{


    public static final int NAME_LIMIT = 15;
    public static final int LEADER_LIMIT = 20;
    private Reaction rbHide;
    private ButtonYio label;
    EditorProvinceData editorProvinceData;
    private TextLabelElement textLabelElement;
    private TextLabelElement leaderLabelElement;
    private ButtonYio changeNameButton;
    private ButtonYio changeLeaderButton;
    private ButtonYio devideProvinceButton;

    private SliderYio moneySlider;
    private SliderYio developSlider;
    private SliderYio attackSlider;
    private SliderYio defenseSlider;
    int moneyValues[],developValues[],attackValues[],defenseValues[];
    SceneChosenLands sceneChosenLands=Scenes.sceneChosenLands;


    public SceneEditorProvincePanel(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);
        moneySlider = null;
        developSlider = null;
        attackSlider = null;
        defenseSlider = null;

        textLabelElement = null;
        leaderLabelElement = null;
        devideProvinceButton = null;
        initMoneyValues();
        initReactions();
    }


    private void initMoneyValues() {
        moneyValues = new int[]{0, 5, 10, 25, 50, 75, 100, 125, 150, 200, 250, 500, 1000, 2500, 5000, 10000, 100000};
        attackValues = new int[]{0,50, 75, 100, 125, 150, 200};
        defenseValues = new int[]{0,50, 75, 100, 125, 150, 200};
        developValues = new int[]{0,50, 75, 100, 125, 150, 200};
    }


    private void initReactions() {
        rbHide = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                hide();
            }
        };
    }


    @Override
    public void create() {
        createInvisibleCloseButton(rbHide);
        invisibleCloseElement.setPosition(generateRectangle(0, 0.7, 1, 0.3));
        createLabel();
        createTitle();
        createChangeNameButton();
        createChangeLeaderButton();
        //createDevideProvinceButton();
        createSlider();
    }


    private void createSlider() {
        initSlider();
        moneySlider.appear();
        attackSlider.appear();
        defenseSlider.appear();
        developSlider.appear();
    }


    private void initSlider() {
        if (moneySlider != null) return;
        moneySlider = new SliderYio(menuControllerYio, -1);
        moneySlider.setValues(0.5, 0, moneyValues.length - 1, Animation.down);
        moneySlider.setPosition(generateRectangle(0.1, 0, 0.8, 0));
        moneySlider.setParentElement(label, 0.1);
        moneySlider.setTitle("money");
        moneySlider.setBehavior(getMoneySliderBehavior());
        moneySlider.setVerticalTouchOffset(0.05f * GraphicsYio.height);
        menuControllerYio.addElementToScene(moneySlider);

        if (defenseSlider != null) return;
        defenseSlider = new SliderYio(menuControllerYio, -1);
        defenseSlider.setValues(0.5, 0, defenseValues.length - 1, Animation.down);
        defenseSlider.setPosition(generateRectangle(0.1, 0, 0.8, 0));
        defenseSlider.setParentElement(label, 0.2);
        defenseSlider.setTitle("ai_defense");
        defenseSlider.setBehavior(getDefenseSliderBehavior());
        defenseSlider.setVerticalTouchOffset(0.05f * GraphicsYio.height);
        menuControllerYio.addElementToScene(defenseSlider);

        if (attackSlider != null) return;
        attackSlider = new SliderYio(menuControllerYio, -1);
        attackSlider.setValues(0.5, 0, attackValues.length - 1, Animation.down);
        attackSlider.setPosition(generateRectangle(0.1, 0, 0.8, 0));
        attackSlider.setParentElement(label, 0.3);
        attackSlider.setTitle("ai_attack");
        attackSlider.setBehavior(getAttackSliderBehavior());
        attackSlider.setVerticalTouchOffset(0.05f * GraphicsYio.height);
        menuControllerYio.addElementToScene(attackSlider);

        if (developSlider != null) return;
        developSlider = new SliderYio(menuControllerYio, -1);
        developSlider.setValues(0.5, 0, developValues.length - 1, Animation.down);
        developSlider.setPosition(generateRectangle(0.1, 0, 0.8, 0));
        developSlider.setParentElement(label, 0.4);
        developSlider.setTitle("ai_develop");
        developSlider.setBehavior(getDevelopSliderBehavior());
        developSlider.setVerticalTouchOffset(0.05f * GraphicsYio.height);
        menuControllerYio.addElementToScene(developSlider);
    }


    private SliderBehavior getMoneySliderBehavior() {
        return new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return "$" + moneyValues[sliderYio.getValueIndex()];
            }


            @Override
            public void onValueChanged(SliderYio sliderYio) {
                editorProvinceData.startingMoney = moneyValues[sliderYio.getValueIndex()];
            }
        };
    }

    private SliderBehavior getAttackSliderBehavior() {
        return new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return attackValues[sliderYio.getValueIndex()]+"%";
            }


            @Override
            public void onValueChanged(SliderYio sliderYio) {
                editorProvinceData.aiPara[0] = (float) attackValues[sliderYio.getValueIndex()]/100;
            }
        };
    }

    private SliderBehavior getDevelopSliderBehavior() {
        return new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return developValues[sliderYio.getValueIndex()]+"%";
            }


            @Override
            public void onValueChanged(SliderYio sliderYio) {
                editorProvinceData.aiPara[1] = (float) developValues[sliderYio.getValueIndex()]/100;
            }
        };
    }

    private SliderBehavior getDefenseSliderBehavior() {
        return new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return defenseValues[sliderYio.getValueIndex()]+"%";
            }


            @Override
            public void onValueChanged(SliderYio sliderYio) {
                editorProvinceData.aiPara[2] = (float) defenseValues[sliderYio.getValueIndex()]/100;
            }
        };
    }


    private void createChangeNameButton() {
        changeNameButton = buttonFactory.getButton(generateRectangle(0.5, 0.63, 0.45, 0.05), 272, getString("change_city"));
        changeNameButton.setAnimation(Animation.down);
        changeNameButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onChangeNameButtonPressed();
            }
        });
    }

    private void createChangeLeaderButton() {
        changeLeaderButton = buttonFactory.getButton(generateRectangle(0.5, 0.53, 0.45, 0.05), 280, getString("change_leader"));
        changeLeaderButton.setAnimation(Animation.down);
        changeLeaderButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onChangeLeaderButtonPressed();
            }
        });
    }


    private void onChangeNameButtonPressed() {
        KeyboardManager.getInstance().apply(editorProvinceData.name, new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                System.out.println("input = " + input);
                if (input.length() > NAME_LIMIT) {
                    input = input.substring(0, NAME_LIMIT);
                }
                editorProvinceData.name = input;
                loadValues();
            }
        });
    }

    private void onChangeLeaderButtonPressed() {
        KeyboardManager.getInstance().apply(editorProvinceData.leader, new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                System.out.println("input = " + input);
                if (input.length() > LEADER_LIMIT) {
                    input = input.substring(0, LEADER_LIMIT);
                }
                editorProvinceData.leader = input;
                loadValues();
            }
        });
    }

    private void createDevideProvinceButton(){
        devideProvinceButton = buttonFactory.getButton(generateRectangle(0.15, 0.23, 0.7, 0.05), 290, getString("devide_province"));
        devideProvinceButton.setAnimation(Animation.down);
        devideProvinceButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onDevideProvinceButtonPressed();
            }
        });
    }

    private void onDevideProvinceButtonPressed(){

//
//
//        GameController gameController = getGameController();
//
//        hide();
//
//        TouchMode.tmShowChosenHexes.setParentScene(Scenes.sceneEditorProvincePanel);
//        gameController.setTouchMode(TouchMode.tmShowChosenHexes);
//        gameController.selectionManager.setAreaSelectionMode(true);
//        gameController.selectionManager.setAsFilterFraction(editorProvinceData.hexList.get(0).fraction);
//        MoveZoneManager moveZoneManager = gameController.fieldManager.moveZoneManager;
//        moveZoneManager.detectAndShowMoveZone(editorProvinceData.hexList.get(0), 0, 0);
//        moveZoneManager.clear();
//
//        sceneChosenLands.create();


        /*
        ArrayList<Hex> hexes=sceneChosenLands.getChosenLands();
        for(Hex hex:hexes){
            System.out.println(hex.fraction);
        }*/
    }

    public void getChosenLands(ArrayList<Hex> lands){
        sceneChosenLands.hide();
        getGameController().setTouchMode(TouchMode.tmEditor);
        getGameController().selectionManager.setAreaSelectionMode(false);
        for(Hex hex:lands){
            System.out.println(hex.fraction);
        }
    }

    private void createTitle() {
        initTextLabel();
        textLabelElement.appear();
        leaderLabelElement.appear();
    }


    private void initTextLabel() {
        if (textLabelElement != null) return;
        if (leaderLabelElement !=null) return;
        textLabelElement = new TextLabelElement(menuControllerYio);
        textLabelElement.setParent(label);
        textLabelElement.alignTitleTop(0.04);
        textLabelElement.alignTitleLeft(0.05);
        menuControllerYio.addElementToScene(textLabelElement);

        leaderLabelElement = new TextLabelElement(menuControllerYio);
        leaderLabelElement.setParent(label);
        leaderLabelElement.alignTitleTop(0.14);
        leaderLabelElement.alignTitleLeft(0.05);
        menuControllerYio.addElementToScene(leaderLabelElement);
    }


    private void createLabel() {
        label = buttonFactory.getButton(generateRectangle(0, 0, 1, 0.7), 271, null);
        menuControllerYio.loadButtonOnce(label, "gray_pixel.png");
        label.setAnimation(Animation.down);
        label.setTouchable(false);
    }


    public void setEditorProvinceData(EditorProvinceData editorProvinceData) {
        this.editorProvinceData = editorProvinceData;
        loadValues();
    }


    private int getSliderIndexByMoneyValue(int moneyValue) {
        for (int i = 0; i < moneyValues.length; i++) {
            if (moneyValue == moneyValues[i]) return i;
        }
        return 2;
    }


    private void loadValues() {
        textLabelElement.setTitle("" + editorProvinceData.name);
        leaderLabelElement.setTitle(""+editorProvinceData.leader);
        moneySlider.setValueIndex(getSliderIndexByMoneyValue(editorProvinceData.startingMoney));
    }


    @Override
    public void hide() {
        destroyByIndex(270, 290);
        if (textLabelElement != null) {
            textLabelElement.destroy();
        }if (leaderLabelElement != null) {
            leaderLabelElement.destroy();
        }
    }
}
