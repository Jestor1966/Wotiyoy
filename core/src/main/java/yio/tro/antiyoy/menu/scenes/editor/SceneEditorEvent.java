package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.KeyboardManager;
import yio.tro.antiyoy.gameplay.event.EventManager;
import yio.tro.antiyoy.gameplay.event.PreparedEvent;
import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.customizable_list.*;
import yio.tro.antiyoy.menu.keyboard.AbstractKbReaction;
import yio.tro.antiyoy.menu.scenes.gameplay.AbstractModalScene;

public class SceneEditorEvent extends AbstractModalScene {

    private Reaction rbHide;
    private ButtonYio basePanel;
    private double panelHeight;
    public CustomizableListYio customizableListYio;
    private SliReaction sliRelationClick;


    public SceneEditorEvent(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);
        panelHeight = 0.6;
        initReactions();
    }


    @Override
    public void create() {
        getEventManager().checkToApplyAdditionalData();
        createInvisibleCloseButton(rbHide);
        createBasePanel();
        createList();
        loadValues();
    }


    private void loadValues() {
        customizableListYio.clearItems();

        TitleListItem titleListItem = new TitleListItem();
        titleListItem.setTitle(getString("event"));

        customizableListYio.addItem(titleListItem);

        EventManager EventManager = getEventManager();
        for (PreparedEvent event : EventManager.event) {
            ScrollListItem scrollListItem = new ScrollListItem();
            scrollListItem.setTitle(event.title + " [Turn:"+event.getStringType(event.turns)+"]");
            scrollListItem.setKey("" + event.getKey());
            //System.out.println("KEY: "+event.getKey()+"\n");
            scrollListItem.setClickReaction(sliRelationClick);
            customizableListYio.addItem(scrollListItem);
        }

        ScrollListItem addItem = new ScrollListItem();
        addItem.setTitle(getString("new_event"));
        addItem.setCentered(true);
        addItem.setClickReaction(getAddEventReaction());
        customizableListYio.addItem(addItem);
    }

    private EventManager getEventManager() {
        return getGameController().eventManager;
    }


    private SliReaction getAddEventReaction() {
        return new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onAddEventItemClicked();
            }
        };
    }


    private void onAddEventItemClicked() {
        hide();
        int type=(int)(Math.random()*10);

        //ScrollListItem addItem = new ScrollListItem();

        getEventManager().addEvent("This is a New Event,you can write in here","New Event",0,1,true,false);
        //System.out.println(type+"\n");
        create();

        /*
        KeyboardManager.getInstance().apply(new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                if (input.length() > 0) {
                    getEventManager().addEvent(input);
                }
                create();
            }
        });*/



        //createEventBasePanel();

        //addItem.setTitle("Title: "+event.title);
        //customizableListYio.addItem(addItem);
    }


    private void onEventItemClicked(final ScrollListItem scrollListItem) {
        hide();
        Integer key = Integer.valueOf(scrollListItem.key);
        SceneEditorEventEditor sceneEditorEventEditor = new SceneEditorEventEditor(menuControllerYio);
        sceneEditorEventEditor.key=key;
        sceneEditorEventEditor.create();
        /*
        KeyboardManager.getInstance().apply(scrollListItem.title.string, new AbstractKbReaction() {
            @Override
            public void onInputFromKeyboardReceived(String input) {
                Integer key = Integer.valueOf(scrollListItem.key);
                if (input.length() == 0) {
                    getEventManager().removeEvent(key);
                } else {
                    getEventManager().modifyEvent(key, input);
                }
                create();
            }
        });*/


        //createEventBasePanel();

        //ScrollListItem addItem = new ScrollListItem();
        //addItem.setTitle("Title:");
    }


    private void createList() {
        initCustomList();
        customizableListYio.appear();
    }


    private void initCustomList() {
        if (customizableListYio != null) return;
        customizableListYio = new CustomizableListYio(menuControllerYio);
        customizableListYio.setAnimation(Animation.down);
        customizableListYio.setEmbeddedMode(true);
        customizableListYio.setPosition(generateRectangle(0.02, SceneEditorOverlay.PANEL_HEIGHT, 0.96, panelHeight - 0.02));

        menuControllerYio.addElementToScene(customizableListYio);
    }




    private void createBasePanel() {
        basePanel = buttonFactory.getButton(generateRectangle(0, SceneEditorOverlay.PANEL_HEIGHT, 1, panelHeight), 421, null);
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


    private void initReactions() {
        rbHide = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                hide();
            }
        };
        sliRelationClick = new SliReaction() {
            @Override
            public void apply(AbstractCustomListItem item) {
                onEventItemClicked((ScrollListItem) item);
            }
        };
    }


    @Override
    public void hide() {
        destroyByIndex(420, 429);
        if (customizableListYio != null) {
            customizableListYio.destroy();
        }

    }
}
