package yio.tro.antiyoy.menu.scenes.editor;

import com.badlogic.gdx.Gdx;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.menu.*;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.KeyboardManager;
import yio.tro.antiyoy.gameplay.event.EventManager;
import yio.tro.antiyoy.gameplay.event.PreparedEvent;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.color_picking.ColorHolderElement;
import yio.tro.antiyoy.menu.customizable_list.*;
import yio.tro.antiyoy.menu.diplomatic_dialogs.AbstractDiplomaticDialog;
import yio.tro.antiyoy.menu.diplomatic_dialogs.DipMessageDialog;
import yio.tro.antiyoy.menu.keyboard.AbstractKbReaction;
import yio.tro.antiyoy.menu.scenes.gameplay.AbstractModalScene;
import yio.tro.antiyoy.menu.scenes.editor.SceneEditorEvent;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.LanguagesManager;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SceneEditorEventEditor extends AbstractModalScene{

    private Reaction rbHide;
    public int key;
    private ButtonYio basePanel;
    private double panelHeight;
    private CustomizableListYio eventList;
    private SliReaction sliTitleItem,sliValueItem,sliTurnsItem,sliTypeItem,sliVisible,sliLoop,sliDeleteItem;
    private ScrollListItem scrollListItem,eventListItem;
    private SceneEditorEvent sceneEditorEvent;
    private EventManager eventManager;
    private PreparedEvent preparedEvent;

    private TitleListItem titleListItem;
    private ScrollListItem titleItem;
    private ScrollListItem valueItem;
    private ScrollListItem turnsItem;
    private ScrollListItem typeItem;
    private ScrollListItem visibleItem;
    private ScrollListItem loopItem;
    private ScrollListItem deleteItem;
    private ColorHolderElement colorHolderElement;
    private ButtonYio deleteButton;
    private Reaction rbDeleteEvent;


    public SceneEditorEventEditor(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);
        panelHeight = 0.5;
        sceneEditorEvent=new SceneEditorEvent(menuControllerYio);
        preparedEvent = new PreparedEvent(getEventManager());
        scrollListItem = new ScrollListItem();
        titleListItem = new TitleListItem();
        titleItem = new ScrollListItem();
        valueItem = new ScrollListItem();
        turnsItem = new ScrollListItem();
        typeItem = new ScrollListItem();
        visibleItem = new ScrollListItem();
        loopItem = new ScrollListItem();
        deleteItem = new ScrollListItem();
        initReactions();
    }

    @Override
    public void create(){
        createInvisibleCloseButton(rbHide);
        createBasePanel();
        createEventList();
        //createColorHolder();
        //createDeleteButton();

        loadValuesEvent();
    }

    private void createColorHolder() {
        initColorHolder();
        colorHolderElement.appear();
    }

    private void initColorHolder() {
        if (colorHolderElement != null) return;
        colorHolderElement = new ColorHolderElement(menuControllerYio);
        colorHolderElement.setTitle(LanguagesManager.getInstance().getString("color") + ":");
        colorHolderElement.setValueIndex(preparedEvent.fraction);
        colorHolderElement.setAnimation(Animation.down);
        colorHolderElement.setPosition(generateRectangle(0.1, panelHeight-0.04, 0.8, 0.08));
        colorHolderElement.setChangeReaction(new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onChosenColorChanged();
            }
        });
        menuControllerYio.addElementToScene(colorHolderElement);
    }

    private void createEventList() {
        initEventList();
        eventList.appear();
    }

    private void loadValuesEvent() {
        eventList.clearItems();

        preparedEvent=getEventManager().getEvent(key);
        String dEncode="";

        deleteItem.setHeight(0.05f * GraphicsYio.height);
        titleItem.setHeight(0.05f * GraphicsYio.height);
        turnsItem.setHeight(0.05f * GraphicsYio.height);
        typeItem.setHeight(0.05f * GraphicsYio.height);
        visibleItem.setHeight(0.05f * GraphicsYio.height);
        loopItem.setHeight(0.05f * GraphicsYio.height);

        titleListItem.setTitle(getString("event_editor"));
        deleteItem.setTitle("- DELETE -");
        deleteItem.setCentered(true);
        deleteItem.setHighlightEnabled(true);
        titleItem.setTitle("Title: "+preparedEvent.title);
        turnsItem.setTitle("Turn: "+preparedEvent.turns);
        //typeItem.setTitle("Type: "+preparedEvent.getStringType(preparedEvent.type));
        visibleItem.setTitle("Visible:"+preparedEvent.isVisible);
        loopItem.setTitle("Loop: "+preparedEvent.isLoop);

        int m=(int)(Gdx.graphics.getHeight()/(0.75 * (int) (0.041 * Gdx.graphics.getHeight())));

        float n=0.05f;
        for(int i=m;;i=i+m){
            int j=i-m;
            if(i<=preparedEvent.value.length()) {
                String str = preparedEvent.value.substring(j,i);
                dEncode=dEncode+str+"\n";
                n+=0.05f;
            }else{
                String str = preparedEvent.value.substring(j,preparedEvent.value.length());
                dEncode=dEncode+str+"\n";
                n+=0.05f;
                break;
            }
        }

        valueItem.setHeight(n * GraphicsYio.height);

        valueItem.setTitle("Description: \n"+dEncode);

        deleteItem.setClickReaction(sliDeleteItem);
        titleItem.setClickReaction(sliTitleItem);
        turnsItem.setClickReaction(sliTurnsItem);
        //typeItem.setClickReaction(sliTypeItem);
        visibleItem.setClickReaction(sliVisible);
        //loopItem.setClickReaction(sliLoop);
        valueItem.setClickReaction(sliValueItem);

        eventList.addItem(titleListItem);
        eventList.addItem(deleteItem);
        eventList.addItem(visibleItem);
        eventList.addItem(loopItem);
        eventList.addItem(titleItem);
        eventList.addItem(turnsItem);
        //eventList.addItem(typeItem);
        eventList.addItem(valueItem);
    }

    private void createBasePanel() {
        basePanel = buttonFactory.getButton(generateRectangle(0, SceneEditorOverlay.PANEL_HEIGHT, 1, panelHeight), 421,null );
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

    private void initEventList() {
        if (eventList != null) return;
        eventList = new CustomizableListYio(menuControllerYio);
        eventList.setAnimation(Animation.down);
        eventList.setEmbeddedMode(true);
        eventList.setPosition(generateRectangle(0.02, SceneEditorOverlay.PANEL_HEIGHT+0.1, 0.96, panelHeight - 0.06));
        menuControllerYio.addElementToScene(eventList);
    }

    private void onTitleItemClicked(PreparedEvent event){
        KeyboardManager.getInstance().apply(event.getTitle(), new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                //Integer key = Integer.valueOf(scrollListItem.key);
                if (input.length() == 0) {
                    preparedEvent.setTitle("-");
                } else {
                    preparedEvent.setTitle(input);
                }
                loadValuesEvent();
            }
        });
    }

    private void onTurnsItemClicked(PreparedEvent event){
        KeyboardManager.getInstance().apply(event.getTurns()+"", new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                //Integer key = Integer.valueOf(scrollListItem.key);
                if (input.length() == 0) {
                    preparedEvent.setTurns(0);
                } else {
                    String inputTurns = input.replaceAll("[^\\d]", "");
                    preparedEvent.setTurns(Integer.parseInt(inputTurns));
                }
                loadValuesEvent();
            }
        });
    }

    private void onValueItemClicked(PreparedEvent event){
        KeyboardManager.getInstance().apply(event.getValue(), new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                if (input.length() == 0) {
                    preparedEvent.setValue("");
                } else {
                    preparedEvent.setValue(input);
                }
                loadValuesEvent();
            }
        });
    }

    private void onVisibleItemClicked(PreparedEvent event){
        if(event.isVisible){
            event.isVisible=false;
        }else{
            event.isVisible=true;
        }
        //create();
        loadValuesEvent();
    }

    private void onLoopItemClicked(PreparedEvent event){
        if(event.isLoop){
            event.isLoop=false;
        }else{
            event.isLoop=true;
        }
        //create();
        loadValuesEvent();
    }

    public void onDeleteEvent(PreparedEvent preparedEvent){
        hide();
        getEventManager().removeEvent(preparedEvent);
        sceneEditorEvent.create();
    }

    private void initReactions() {
        rbHide = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                System.out.println(key);
                getEventManager().modifyEvent(key, preparedEvent.value, preparedEvent.title, preparedEvent.turns, preparedEvent.type, preparedEvent.isVisible, preparedEvent.isLoop);
                hide();
            }
        };

        sliTitleItem = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onTitleItemClicked(preparedEvent);
            }
        };

        sliValueItem = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onValueItemClicked(preparedEvent);
            }
        };
        sliTurnsItem = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                //onEventItemClicked((ScrollListItem) item);
            }
        };
        sliTypeItem= new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                //onEventItemClicked((ScrollListItem) item);
            }
        };
        sliTurnsItem = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onTurnsItemClicked(preparedEvent);
            }
        };
        sliVisible = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onVisibleItemClicked(preparedEvent);
            }
        };
        sliLoop = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onLoopItemClicked(preparedEvent);
            }
        };
        sliDeleteItem = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onDeleteEvent(preparedEvent);
            }
        };
    }

    void onChosenColorChanged() {
        preparedEvent.fraction=colorHolderElement.getValueIndex();
    }

    private EventManager getEventManager() {
        return getGameController().eventManager;
    }

    @Override
    public void hide() {
        destroyByIndex(420, 429);
        //deleteButton.destroy();
        if (eventList != null) {
            eventList.destroy();
        }
        if (colorHolderElement != null) {
            colorHolderElement.destroy();
        }
    }
}
