package yio.tro.antiyoy.gameplay.event;

import yio.tro.antiyoy.gameplay.data_storage.EncodeableYio;
import yio.tro.antiyoy.gameplay.messages.MessagesManager;

public class PreparedEvent implements EncodeableYio{

    EventManager eventManager;
    public String value,title;
    public int fraction;
    public int turns;
    public int type;
    public boolean isVisible,isLoop;
    int key;


    public PreparedEvent(EventManager eventManager) {
        this.eventManager = eventManager;
        value = "";
        title = "";
        isVisible = true;
        isLoop = false;
        turns = 0;
        type = 1;
        key = -1;
        fraction = 1;
    }



    public void setValue(String value) {this.value = value;}

    public void setTitle(String title) {this.title = title;}

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setVisible(boolean isVisiable) {
        this.isVisible = isVisible;
    }

    public void setLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }


    public int getKey() {
        return key;
    }

    public String getValue() {return value;}

    public String getTitle() {return title;}

    public int getTurns() {return turns;}

    public int getType() {return type;}

    public String getStringType(int type) {
        String stringType="";
        switch (type){
            case 1:
                stringType="Message";
                break;
            case 2:
                stringType="Goto";
                break;
            case 3:
                stringType="Delete Hex";
                break;
            case 4:
                stringType="Add Hex";
                break;
            case 5:
                stringType="Change Owner";
                break;
            case 6:
                stringType="Build";
                break;
            case 7:
                stringType="Destory";
                break;
            case 8:
                stringType="Spawn";
                break;
            case 9:
                stringType="Kill Units";
                break;
            default:
                stringType="Unknown";
        }
        return stringType;
    }


    public void setKey(int key) {
        this.key = key;
    }


    @Override
    public String encode() {
        return value+"/"+title+"/"+turns+"/"+type+"/"+(isVisible ? 1 : 0)+"/"+(isLoop ? 1 : 0);
    }


    @Override
    public void decode(String source) {
        value = source;
    }
}
