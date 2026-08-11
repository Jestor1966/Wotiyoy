package yio.tro.antiyoy.gameplay;

public class MapGeneratorFactory {


    public static MapGenerator getGenerator(GameController gameController, MapType mapType) {
        if (mapType == null) {
            mapType = MapType.DEFAULT;
        }

        switch (mapType) {
            case CONTINENTAL:
                return new ContinentalMapGenerator(gameController);
            case ISLANDS:
                return new IslandsMapGenerator(gameController);
            default:
                return new DefaultMapGenerator(gameController);
        }
    }
}
