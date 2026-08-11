package yio.tro.antiyoy.gameplay;

import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.PointYio;

import java.util.ArrayList;

public class ContinentalMapGenerator extends DefaultMapGenerator {


    public ContinentalMapGenerator(GameController gameController) {
        super(gameController);
    }


    @Override
    protected void createLand() {
        createContinentalLandOn(this);
    }


    static void createContinentalLandOn(MapGenerator host) {
        int outerGuard = 0;
        while (!host.isGood() && outerGuard++ < 20) {
            host.deactivateHexes();

            int islandsQuantity = host.numberOfIslandsByLevelSize();

            // 1. irregular sea covering most of the map (like a rounded island shape)
            PointYio seaCenter = getRandomPointInsideBounds(host);
            double seaRadius = getSeaRadius(host);
            createIrregularSea(host, seaCenter, seaRadius);

            // 2. several main continents (quantity depends on map size)
            int continentsQuantity = getContinentsQuantity(host);
            double continentRadius = getContinentRadius(host, islandsQuantity, continentsQuantity);
            for (int i = 0; i < continentsQuantity; i++) {
                PointYio center = getRandomPointInSea(host, seaCenter, seaRadius, 0.55);
                spawnRoundContinent(host, center, continentRadius);
            }

            // 3. scattered small islands, each bigger than 3 hexes
            int smallIslandsQuantity = getSmallIslandsQuantity(host);
            for (int i = 0; i < smallIslandsQuantity; i++) {
                PointYio center = getRandomPointInSea(host, seaCenter, seaRadius, 0.75);
                int islandSize = 4 + host.random.nextInt(4);
                spawnSmallIsland(host, center, islandSize);
            }

            // 4. sprinkle single-hex islets
            int singleIslandsQuantity = getSingleIslandsQuantity(host);
            for (int i = 0; i < singleIslandsQuantity; i++) {
                PointYio center = getRandomPointInSea(host, seaCenter, seaRadius, 0.85);
                Hex hex = host.gameController.fieldManager.getHexByPos(center.x, center.y);
                if (hex == null || hex.isNullHex()) continue;
                activateLandHex(host, hex);
            }

            host.centerLand();
            host.cutOffHexesOutsideOfBounds();
        }
    }


    private static double getSeaRadius(MapGenerator host) {
        double minBound = Math.min(host.boundWidth, host.boundHeight);
        return 0.42 * minBound / host.gameController.fieldManager.hexSize;
    }


    private static int getContinentsQuantity(MapGenerator host) {
        switch (host.gameController.levelSizeManager.levelSize) {
            default:
            case LevelSize.SMALL:
                return 1;
            case LevelSize.MEDIUM:
                return 2;
            case LevelSize.BIG:
                return 3;
            case LevelSize.HUGE:
                return 4;
            case LevelSize.GIANT:
                return 5;
            case LevelSize.WORLD:
                return 6;
        }
    }


    private static double getContinentRadius(MapGenerator host, int islandsQuantity, int continentsQuantity) {
        return 3.6 * Math.sqrt((double) islandsQuantity / continentsQuantity);
    }


    private static int getSmallIslandsQuantity(MapGenerator host) {
        switch (host.gameController.levelSizeManager.levelSize) {
            default:
            case LevelSize.SMALL:
                return 1;
            case LevelSize.MEDIUM:
                return 2;
            case LevelSize.BIG:
                return 4;
            case LevelSize.HUGE:
                return 6;
            case LevelSize.GIANT:
                return 8;
            case LevelSize.WORLD:
                return 10;
        }
    }


    private static int getSingleIslandsQuantity(MapGenerator host) {
        switch (host.gameController.levelSizeManager.levelSize) {
            default:
            case LevelSize.SMALL:
                return 2;
            case LevelSize.MEDIUM:
                return 3;
            case LevelSize.BIG:
                return 5;
            case LevelSize.HUGE:
                return 7;
            case LevelSize.GIANT:
                return 9;
            case LevelSize.WORLD:
                return 12;
        }
    }


    private static PointYio getRandomPointInSea(MapGenerator host, PointYio seaCenter, double seaRadius, double factor) {
        double angle = host.random.nextDouble() * 2 * Math.PI;
        double distance = seaRadius * factor * (0.3 + 0.7 * host.random.nextDouble()) * host.gameController.fieldManager.hexSize;
        double x = seaCenter.x + distance * Math.cos(angle);
        double y = seaCenter.y + distance * Math.sin(angle);
        x = Math.max(0.1 * host.boundWidth, Math.min(0.9 * host.boundWidth, x));
        y = Math.max(0.15 * host.boundHeight, Math.min(0.9 * host.boundHeight, y));
        return new PointYio(x, y);
    }


    private static void createIrregularSea(MapGenerator host, PointYio center, double radiusInHexes) {
        spawnSeaBlob(host, center, radiusInHexes);

        // a smaller offset blob makes the sea outline irregular
        double hexSize = host.gameController.fieldManager.hexSize;
        PointYio offset = new PointYio(
                center.x + radiusInHexes * 0.4 * hexSize * Math.cos(0.7),
                center.y + radiusInHexes * 0.4 * hexSize * Math.sin(0.7)
        );
        spawnSeaBlob(host, offset, radiusInHexes * 0.55);
    }


}
