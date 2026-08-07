package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.gameplay.FieldManager;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomacyManager;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomaticEntity;
import yio.tro.antiyoy.menu.*;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.menu.scenes.gameplay.AbstractModalScene;
import yio.tro.antiyoy.stuff.GraphicsYio;

import java.util.ArrayList;
public class SceneChosenLands extends AbstractModalScene {
    private Reaction rbCancel;
    private Reaction rbApply;
    private ButtonYio cancelButton;
    private ButtonYio applyButton;
    private TextViewElement textViewTitle;
    ArrayList<Hex> moveZone;

    public SceneChosenLands(MenuControllerYio menuControllerYio) {
            super(menuControllerYio);

            textViewTitle = null;

            initReactions();
        }


        private void initReactions() {
            rbCancel = new Reaction() {
                @Override
                public void perform(ButtonYio buttonYio) {
                    onCancelButtonPressed();
                }
            };

            rbApply = new Reaction() {
                @Override
                public void perform(ButtonYio buttonYio) {
                    onApplyButtonPressed();
                }
            };
        }


        private void onCancelButtonPressed() {
            FieldManager fieldManager = getGameController().fieldManager;
        }


        private void onApplyButtonPressed() {
            GameController gameController = getGameController();
            FieldManager fieldManager = gameController.fieldManager;
            moveZone = fieldManager.moveZoneManager.moveZone;
            Scenes.sceneEditorProvincePanel.getChosenLands(moveZone);
            Scenes.sceneEditorProvincePanel.create();
        }

        public ArrayList<Hex> getChosenLands(){
            return moveZone;
        }


        @Override
        public void create() {
            createButtons();
            createTextView();
        }


        private void createTextView() {
            initTextView();

            textViewTitle.appear();
        }


        private void initTextView() {
            if (textViewTitle != null) return;

            textViewTitle = new TextViewElement();
            textViewTitle.setPosition(generateRectangle(0.45, 0.95, 0.1, 0.02));
            textViewTitle.setTextValue(getString("province"));
            textViewTitle.setAnimation(Animation.up);
            menuControllerYio.addElementToScene(textViewTitle);
        }


        private void createButtons() {
            double bw = 0.12;

            cancelButton = buttonFactory.getButton(generateRectangle(0.5 - bw - bw / 2, 0.86, bw, GraphicsYio.convertToHeight(bw)), 870, null);
            menuControllerYio.loadButtonOnce(cancelButton, "game/cancel.png");
            cancelButton.setAnimation(Animation.up);
            cancelButton.setTouchOffset(0.01f * GraphicsYio.width);
            cancelButton.setReaction(rbCancel);

            applyButton = buttonFactory.getButton(generateRectangle(0.5 + bw - bw / 2, 0.86, bw, GraphicsYio.convertToHeight(bw)), 871, null);
            menuControllerYio.loadButtonOnce(applyButton, "game/ok_icon.png");
            applyButton.setAnimation(Animation.up);
            applyButton.setTouchOffset(0.01f * GraphicsYio.width);
            applyButton.setReaction(rbApply);
        }


        @Override
        public void hide() {
            destroyByIndex(870, 879);
            if (textViewTitle != null) {
                textViewTitle.destroy();
            }
        }
    }
