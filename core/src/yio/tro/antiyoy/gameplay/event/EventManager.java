package yio.tro.antiyoy.gameplay.event;

import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.data_storage.DecodeManager;
import yio.tro.antiyoy.gameplay.data_storage.EncodeableYio;
import yio.tro.antiyoy.gameplay.messages.PreparedMessage;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.stuff.RepeatYio;

import java.util.ArrayList;

public class EventManager implements EncodeableYio{

    GameController gameController;
    public ArrayList<PreparedEvent> event;
    RepeatYio<yio.tro.antiyoy.gameplay.event.EventManager> repeatApply;


    public EventManager(GameController gameController) {
        this.gameController = gameController;
        event = new ArrayList<>();
        initRepeats();
    }


    public void initRepeats() {
        repeatApply = new RepeatYio<EventManager>(this, 30) {
            @Override
            public void performAction() {
                parent.checkToApply();
            }
        };
    }


    public void defaultValues() {
        event.clear();
    }


    public void checkToApply() {
        int i=0;
        boolean remove=true;
        if (event.size() == 0) {
            //System.out.println("NULL EVENT:"+event);
            return;
        }

        //PreparedEvent preparedEvent = event.get(0);
        //event.remove(0);
        //System.out.println(event);
        if(GameRules.eventEnabled){
            for(PreparedEvent preparedEvent : event){
                if(gameController.matchStatistics.turnsMade==preparedEvent.turns){
                    if(preparedEvent.type==1){
                        Scenes.sceneDipMessage.showEvent(preparedEvent.getTitle(), preparedEvent.value, true);
                        remove = preparedEvent.isLoop;
                        break;
                    }else if(preparedEvent.type==2){

                    }else if(preparedEvent.type==3){

                    }
                }
                i++;
            }

            if(!remove){
                event.remove(i);
            }else{
                //event.get(event.size()).setTurns(event.get(i).turns+1);
            }
        }
    }


    public void addEvent(String value, String title, int turns, int type, boolean isVisiable, boolean isLoop) {
        PreparedEvent preparedEvent = new PreparedEvent(this);

        preparedEvent.setValue(value);
        preparedEvent.setTitle(title);
        preparedEvent.setTurns(turns);
        preparedEvent.setType(type);
        preparedEvent.setVisible(isVisiable);
        preparedEvent.setLoop(isLoop);

        preparedEvent.setKey(getKeyForNewEvent());
        event.add(preparedEvent);
    }


    private int getKeyForNewEvent() {
        int max = -1;
        for (PreparedEvent event : event) {
            if (max == -1 || event.key > max) {
                max = event.key;
            }
        }
        return max + 1;
    }


    public PreparedEvent getEvent(int key) {
        for (PreparedEvent event : event) {
            if (event.key != key) continue;
            return event;
        }
        return null;
    }


    public void modifyEvent(int key, String value, String title, int turns, int type, boolean isVisiable, boolean isLoop) {
        PreparedEvent preparedEvent = getEvent(key);
        if (event == null) return;
        preparedEvent.setValue(value);
        preparedEvent.setTitle(title);
        preparedEvent.setTurns(turns);
        preparedEvent.setType(type);
        preparedEvent.setVisible(isVisiable);
        preparedEvent.setLoop(isLoop);
    }


    public void removeEvent(int key) {
        PreparedEvent event = getEvent(key);
        if (event == null) return;
        removeEvent(key);
    }


    public void removeEvent(PreparedEvent preparedEvent) {
        event.remove(preparedEvent);
    }


    public void onEndCreation() {
        //addEvent("Veni Veci Vedi","Caesar",0,1,true,false);
    }


    public void move() {
        if (gameController.isInEditorMode()) return;
        repeatApply.move();
    }


    public void onLevelImported(String levelCode) {
        DecodeManager decodeManager = gameController.decodeManager;
        decodeManager.setSource(levelCode);
        String eventSection = decodeManager.getSection("event");
        if (eventSection == null) return;

        decode(eventSection);
    }


    @Override
    public String encode() {
        StringBuilder builder = new StringBuilder();
        for (PreparedEvent event : event) {
            builder.append(event.encode()).append("@");
        }
        return builder.toString();
    }


    @Override
    public void decode(String source) {
        int i=0;

        String value="";
        String title="";
        int turns=0;
        int type=0;
        boolean isVisiable=true;
        boolean isLoop=false;

        if (source.length() < 2) return;
        for (String eventSource : source.split("@")) {
            if (eventSource.length() == 0) continue;
            /*
            for (String everySource : eventSource.split("/",6)) {
                if (everySource.length() == 0) continue;
                System.out.println(everySource);

                switch (i){
                    case 0:value=everySource;
                    case 1:title=everySource;
                    case 2:turns=Integer.parseInt(everySource);
                    case 3:type=Integer.parseInt(everySource);
                    case 4:isVisiable=Integer.parseInt(everySource) != 0;
                    case 5:isLoop=Integer.parseInt(everySource) != 0;
                    default:System.out.println("Default");
                }

                i+=1;
            }*/

            value=eventSource.split("/",6)[0];
            title=eventSource.split("/",6)[1];
            turns=Integer.parseInt(eventSource.split("/",6)[2]);
            type=Integer.parseInt(eventSource.split("/",6)[3]);
            isVisiable=Integer.parseInt(eventSource.split("/",6)[4])!= 0;
            isLoop=Integer.parseInt(eventSource.split("/",6)[5])!= 0;

            i=0;
            addEvent(value,title,turns,type,isVisiable,isLoop);
        }
    }


    public void checkToApplyAdditionalData() {
        String source = gameController.initialParameters.preparedEventData;
        //gameController.initialParameters.showInConsole();
        if (source == null) return;
        if (source.length() < 5) return;

        onEndCreation();
        decode(source);
    }
}
