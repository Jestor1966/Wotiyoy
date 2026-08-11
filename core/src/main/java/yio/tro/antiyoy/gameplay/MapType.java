package yio.tro.antiyoy.gameplay;

public enum MapType {

    DEFAULT(0, "default"),
    CONTINENTAL(1, "continental"),
    ISLANDS(2, "islands");


    public final int id;
    public final String key;


    MapType(int id, String key) {
        this.id = id;
        this.key = key;
    }


    public static MapType byId(int id) {
        for (MapType mapType : values()) {
            if (mapType.id == id) return mapType;
        }
        return DEFAULT;
    }
}
