package yio.tro.antiyoy.gameplay.event;

public class EventType {
    public static final int MESSAGE = 1;//Only message
    public static final int GOTO = 2;//Goto another event
    public static final int DELETEHEX = 3;//Delete designal hexes
    public static final int ADDHEX = 4;//Add designal hexes
    public static final int CHANGEOWNER = 5;//change owner of designal hexes
    public static final int BUILD = 6;//build building
    public static final int DESTORY = 7;//destory buliding
    public static final int SPAWN = 8;//spawn units
    public static final int KILL = 9;//kill units
    public static final int CONTROL = 10;//if control designal hexes
    public static final int LOSE = 11;//if lose designal hexes
    public static final int REACHMONEY = 12;//if reach designal money

    //public static final int ENDGAME = 13;
}
