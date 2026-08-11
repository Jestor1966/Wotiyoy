package yio.tro.antiyoy.gameplay;

import yio.tro.antiyoy.stuff.PointYio;

public class IslandsMapGenerator extends DefaultMapGenerator {


    public IslandsMapGenerator(GameController gameController) {
        super(gameController);
    }


    @Override
    protected void createLand() {
        createIslandsLandOn(this);
    }


    static void createIslandsLandOn(MapGenerator host) {
        int outerGuard = 0;
        while (!host.isGood() && outerGuard++ < 20) {
            host.deactivateHexes();

            int islandsQuantity = host.numberOfIslandsByLevelSize();

            // 1. one solid round main island exactly in the center of the map
            PointYio mainCenter = new PointYio(host.boundWidth / 2, host.boundHeight / 2);
            double minBoundHex = Math.min(host.boundWidth, host.boundHeight) / host.gameController.fieldManager.hexSize;
            double maxBoundHex = Math.max(host.boundWidth, host.boundHeight) / host.gameController.fieldManager.hexSize;
            double mainRadius = 0.18 * minBoundHex;
            spawnSolidIsland(host, mainCenter, mainRadius);

            // 2. open sea covering the whole map, connecting all islands
            double seaRadius = 0.5 * maxBoundHex;
            spawnSeaBlob(host, mainCenter, seaRadius);

            // 3. small islands scattered across the sea (each bigger than 3 hexes)
            int smallIslandsQuantity = getSmallIslandsQuantity(host);
            for (int i = 0; i < smallIslandsQuantity; i++) {
                PointYio center = getRandomPointInRing(host, mainCenter, mainRadius + 4.0, 0.45 * minBoundHex);
                int islandSize = 4 + host.random.nextInt(4);
                spawnSmallIsland(host, center, islandSize);
            }

            // 4. sprinkle single-hex islets across the sea
            int singleIslandsQuantity = getSingleIslandsQuantity(host);
            for (int i = 0; i < singleIslandsQuantity; i++) {
                PointYio center = getRandomPointInRing(host, mainCenter, mainRadius + 3.0, 0.5 * minBoundHex);
                Hex hex = host.gameController.fieldManager.getHexByPos(center.x, center.y);
                if (hex == null || hex.isNullHex()) continue;
                activateLandHex(host, hex);
            }

            host.centerLand();
            host.cutOffHexesOutsideOfBounds();
        }
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


    private static PointYio getRandomPointInRing(MapGenerator host, PointYio center, double innerRadius, double outerRadius) {
        double angle = host.random.nextDouble() * 2 * Math.PI;
        double distance = (innerRadius + host.random.nextDouble() * (outerRadius - innerRadius))
                * host.gameController.fieldManager.hexSize;
        double x = center.x + distance * Math.cos(angle);
        double y = center.y + distance * Math.sin(angle);
        x = Math.max(0.1 * host.boundWidth, Math.min(0.9 * host.boundWidth, x));
        y = Math.max(0.15 * host.boundHeight, Math.min(0.9 * host.boundHeight, y));
        return new PointYio(x, y);
    }
}
