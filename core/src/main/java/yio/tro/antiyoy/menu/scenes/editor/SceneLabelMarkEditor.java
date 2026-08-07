package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.KeyboardManager;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.HexLabel;
import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.keyboard.AbstractKbReaction;
import yio.tro.antiyoy.menu.scenes.gameplay.AbstractModalScene;
import yio.tro.antiyoy.menu.slider.SliderBehavior;
import yio.tro.antiyoy.menu.slider.SliderYio;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.LanguagesManager;


/**
 * Modal dialog for editing the label attached to a hex. The layout follows
 * the same full-width centered dialog style used by SceneEvent.
 */
public class SceneLabelMarkEditor extends AbstractModalScene {

    private static final int INVISIBLE_ID = 460;
    private static final int BASE_PANEL_ID = 461;
    private static final int TITLE_ID = 462;
    private static final int TEXT_ID = 463;
    private static final int COLOR_ID = 464;
    private static final int SIZE_ID = 465;
    private static final int OUTLINE_ID = 466;
    private static final int VISIBLE_ID = 467;
    private static final int CLOSE_ID = 468;
    private static final int DELETE_ID = 469;

    private Reaction rbHide;
    private Hex editedHex;
    private ButtonYio textButton;
    private ButtonYio colorButton;
    private SliderYio colorSlider;
    private ButtonYio sizeButton;
    private SliderYio sizeSlider;
    private ButtonYio outlineButton;
    private SliderYio transparentSlider;
    private ButtonYio visibleButton;


    public SceneLabelMarkEditor(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);
        initReactions();
    }


    public void setEditedHex(Hex editedHex) {
        this.editedHex = editedHex;
    }


    @Override
    public void create() {
        if (editedHex == null) return;

        createInvisibleButton();
        createBasePanel();
        createTitle();
        createTextButton();
        createColorButton();
        createColorSlider();
        createSizeButton();
        createSizeSlider();
        createOutlineButton();
        createTransparentSlider();
        createVisibleButton();
        createCloseButton();
        createDeleteButton();
        loadValues();
    }


    private void createInvisibleButton() {
        // full-screen invisible button blocks touches outside the dialog.
        // it is created first, so dialog buttons (checked later) win the touch.
        ButtonYio invisibleButton = buttonFactory.getButton(generateRectangle(0, 0, 1, 1), INVISIBLE_ID, null);
        invisibleButton.setRenderable(false);
        invisibleButton.setReaction(rbHide);
    }


    private void createBasePanel() {
        ButtonYio basePanel = buttonFactory.getButton(generateRectangle(0, 0.15, 1, 0.6), BASE_PANEL_ID, null);
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


    private void createTitle() {
        ButtonYio titleButton = buttonFactory.getButton(generateRectangle(0.08, 0.685, 0.84, 0.05), TITLE_ID, getString("label_editor"));
        titleButton.setTouchable(false);
        titleButton.setShadow(false);
    }


    private void createTextButton() {
        textButton = buttonFactory.getButton(generateRectangle(0.08, 0.615, 0.84, 0.06), TEXT_ID, null);
        textButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onTextButtonClicked();
            }
        });
        textButton.setShadow(false);
    }


    private void createColorButton() {
        colorButton = buttonFactory.getButton(generateRectangle(0.05, 0.54, 0.30, 0.06), COLOR_ID, null);
        colorButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onColorButtonClicked();
            }
        });
        colorButton.setShadow(false);
    }


    private void createColorSlider() {
        if (colorSlider != null) {
            colorSlider.appear();
            return;
        }
        colorSlider = new SliderYio(menuControllerYio, -1);
        colorSlider.setValues(0, 0, HexLabel.COLOR_NAME_KEYS.length - 1, Animation.down);
        colorSlider.setPosition(generateRectangle(0.38, 0.57, 0.57, 0.01));
        colorSlider.title = "";
        colorSlider.setBehavior(new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return HexLabel.getColorName(sliderYio.getValueIndex());
            }

            @Override
            public void onValueChanged(SliderYio sliderYio) {
                if (editedHex == null) return;
                editedHex.label.labelcolor = sliderYio.getValueIndex();
                updateColorButton();
            }
        });
        menuControllerYio.addElementToScene(colorSlider);
        colorSlider.appear();
        colorSlider.setVerticalTouchOffset(0.03f * GraphicsYio.height);
    }


    private void createSizeButton() {
        sizeButton = buttonFactory.getButton(generateRectangle(0.05, 0.465, 0.30, 0.06), SIZE_ID, null);
        sizeButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onSizeButtonClicked();
            }
        });
        sizeButton.setShadow(false);
    }


    private void createSizeSlider() {
        if (sizeSlider != null) {
            sizeSlider.appear();
            return;
        }
        sizeSlider = new SliderYio(menuControllerYio, -1);
        sizeSlider.setValues(0.5, 0, HexLabel.MAX_LABEL_SIZE - HexLabel.MIN_LABEL_SIZE, Animation.down);
        sizeSlider.setPosition(generateRectangle(0.38, 0.495, 0.57, 0.01));
        sizeSlider.title = "";
        sizeSlider.setBehavior(new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return (HexLabel.MIN_LABEL_SIZE + sliderYio.getValueIndex()) + "";
            }

            @Override
            public void onValueChanged(SliderYio sliderYio) {
                if (editedHex == null) return;
                editedHex.label.labelSize = HexLabel.MIN_LABEL_SIZE + sliderYio.getValueIndex();
                updateSizeButton();
            }
        });
        menuControllerYio.addElementToScene(sizeSlider);
        sizeSlider.appear();
        sizeSlider.setVerticalTouchOffset(0.03f * GraphicsYio.height);
    }


    private void createOutlineButton() {
        outlineButton = buttonFactory.getButton(generateRectangle(0.08, 0.40, 0.84, 0.06), OUTLINE_ID, null);
        outlineButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onOutlineButtonClicked();
            }
        });
        outlineButton.setShadow(false);
    }


    private void createTransparentSlider() {
        if (transparentSlider != null) {
            transparentSlider.appear();
            return;
        }
        transparentSlider = new SliderYio(menuControllerYio, -1);
        transparentSlider.setValues(1, 0, 100, Animation.down);
        transparentSlider.setPosition(generateRectangle(0.13, 0.35, 0.74, 0.06));
        transparentSlider.title = getString("label_transparent");
        transparentSlider.setBehavior(new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return sliderYio.getValueIndex() + "%";
            }

            @Override
            public void onValueChanged(SliderYio sliderYio) {
                if (editedHex == null) return;
                editedHex.label.transparent = sliderYio.getValueIndex() / 100f;
            }
        });
        menuControllerYio.addElementToScene(transparentSlider);
        transparentSlider.appear();
        transparentSlider.setVerticalTouchOffset(0.03f * GraphicsYio.height);
    }


    private void createVisibleButton() {
        visibleButton = buttonFactory.getButton(generateRectangle(0.08, 0.24, 0.84, 0.06), VISIBLE_ID, null);
        visibleButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onVisibleButtonClicked();
            }
        });
        visibleButton.setShadow(false);
    }


    private void createCloseButton() {
        ButtonYio closeButton = buttonFactory.getButton(generateRectangle(0.56, 0.16, 0.30, 0.06), CLOSE_ID, getString("close"));
        closeButton.setReaction(rbHide);
        closeButton.setShadow(false);
    }


    private void createDeleteButton() {
        ButtonYio deleteButton = buttonFactory.getButton(generateRectangle(0.14, 0.16, 0.30, 0.06), DELETE_ID, getString("delete_label"));
        deleteButton.setReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onDeleteButtonClicked();
            }
        });
        deleteButton.setShadow(false);
    }


    private void loadValues() {
        if (editedHex == null) return;
        HexLabel label = editedHex.label;

        textButton.setTextLine(LanguagesManager.getInstance().getString("label_text", label.labeltext));
        menuControllerYio.buttonRenderer.renderButton(textButton);

        updateColorButton();
        colorSlider.setValueIndex(label.labelcolor);
        colorSlider.updateValueString();

        updateSizeButton();
        sizeSlider.setValueIndex(label.labelSize - HexLabel.MIN_LABEL_SIZE);
        sizeSlider.updateValueString();

        outlineButton.setTextLine(LanguagesManager.getInstance().getString("label_outline", getString(label.isOutline ? "yes" : "no")));
        menuControllerYio.buttonRenderer.renderButton(outlineButton);

        transparentSlider.setValueIndex((int) (label.transparent * 100));
        transparentSlider.updateValueString();

        visibleButton.setTextLine(LanguagesManager.getInstance().getString("label_visible", getString(label.isVisible ? "yes" : "no")));
        menuControllerYio.buttonRenderer.renderButton(visibleButton);
    }


    private void updateColorButton() {
        if (editedHex == null || colorButton == null) return;
        colorButton.setTextLine(LanguagesManager.getInstance().getString("label_color", HexLabel.getColorName(editedHex.label.labelcolor)));
        menuControllerYio.buttonRenderer.renderButton(colorButton);
    }


    private void updateSizeButton() {
        if (editedHex == null || sizeButton == null) return;
        sizeButton.setTextLine(LanguagesManager.getInstance().getString("label_size", editedHex.label.labelSize));
        menuControllerYio.buttonRenderer.renderButton(sizeButton);
    }


    private void onTextButtonClicked() {
        if (editedHex == null) return;

        KeyboardManager.getInstance().apply(editedHex.label.labeltext, new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                if (editedHex == null) return;
                if (input.length() == 0) {
                    editedHex.label.labeltext = "Label";
                } else {
                    editedHex.label.labeltext = input;
                }
                loadValues();
            }
        });
    }


    private void onColorButtonClicked() {
        if (editedHex == null) return;
        editedHex.label.labelcolor = (editedHex.label.labelcolor + 1) % HexLabel.COLOR_NAME_KEYS.length;
        loadValues();
    }


    private void onSizeButtonClicked() {
        if (editedHex == null) return;

        KeyboardManager.getInstance().apply(editedHex.label.labelSize + "", new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                if (editedHex == null) return;
                String cleaned = input.replaceAll("[^\\d]", "");
                if (cleaned.length() == 0) {
                    editedHex.label.labelSize = HexLabel.DEFAULT_LABEL_SIZE;
                } else {
                    int value = Integer.parseInt(cleaned);
                    if (value < HexLabel.MIN_LABEL_SIZE) value = HexLabel.MIN_LABEL_SIZE;
                    if (value > HexLabel.MAX_LABEL_SIZE) value = HexLabel.MAX_LABEL_SIZE;
                    editedHex.label.labelSize = value;
                }
                loadValues();
            }
        });
    }


    private void onOutlineButtonClicked() {
        if (editedHex == null) return;
        editedHex.label.isOutline = !editedHex.label.isOutline;
        loadValues();
    }


    private void onVisibleButtonClicked() {
        if (editedHex == null) return;
        editedHex.label.isVisible = !editedHex.label.isVisible;
        loadValues();
    }


    private void onDeleteButtonClicked() {
        if (editedHex == null) return;
        editedHex.label.delete();
        hide();
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
    public void hide() {
        destroyByIndex(460, 469);
        if (colorSlider != null) {
            colorSlider.destroy();
        }
        if (sizeSlider != null) {
            sizeSlider.destroy();
        }
        if (transparentSlider != null) {
            transparentSlider.destroy();
        }
    }
}
