package yio.tro.antiyoy.ai;

import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.MoveZoneDetection;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.gameplay.Unit;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.Yio;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class AbstractAi {

    public GameController gameController;
    protected int fraction;
    protected ArrayList<Hex> seaPropagationList;
    private int seaDistanceFieldTurn = -1;
    private int seaTargetCheckedTurn = -1;
    private boolean hasOverseasTargetValue;
    protected Hex seaTargetLand; // 当前海军进攻目标大陆的代表格
    private int seaTargetRound = -1;
    private boolean weakEnemyOnContinentValue;
    private int weakEnemyCheckRound = -1;
    private final ArrayList<Hex> cachedPortWaters = new ArrayList<>();
    private final ArrayList<Hex> cachedCoastalLand = new ArrayList<>();
    private int approachCacheRound = -1;
    private final HashSet<Hex> ownContinentCache = new HashSet<>();


    public AbstractAi(GameController gameController, int fraction) {
        this.gameController = gameController;
        this.fraction = fraction;
        seaPropagationList = new ArrayList<>();
    }


    public abstract void perform();


    public int getFraction() {
        return fraction;
    }


    /**
     * 海上单位的远征逻辑：计算当前移动范围内最接近非己方大陆的海格。
     * 这是独立于 MassMarchManager / findBestWayToHex 的全新海上路径规划，
     * 只经过海格（不能进入敌方港口水域），并沿 BFS 最短海路朝目标大陆前进。
     * 调用前需要先执行 updateSeaDistanceField()。
     * 没有可抵达的目标时返回 null。
     */
    protected Hex getSeaStepTowardsEnemyLand(Unit unit) {
        ArrayList<Hex> moveZone = gameController.detectMoveZone(unit.currentHex, unit.strength, GameRules.UNIT_MOVE_LIMIT);
        return getSeaStepTowardsEnemyLand(unit, moveZone);
    }


    /**
     * 使用已经算好的移动范围选择海上最优目标，避免重复 BFS。
     */
    protected Hex getSeaStepTowardsEnemyLand(Unit unit, ArrayList<Hex> moveZone) {
        if (!unit.currentHex.isSea()) return null;
        ensureSeaDistanceField();
        updateSeaTarget(unit);
        if (seaTargetLand == null) return null; // 没有可进攻的海外目标

        Hex bestSea = null;
        int bestSeaDistance = Integer.MAX_VALUE;
        Hex bestLand = null;
        int bestLandScore = -1;
        double bestSeaTargetDist = Double.MAX_VALUE;
        double bestLandTargetDist = Double.MAX_VALUE;

        for (Hex hex : moveZone) {
            if (hex.isSea()) {
                if (hex.genPotential < 0) continue; // 该海域无法抵达任何海外陆地
                double targetDist = hex.pos.fastDistanceTo(seaTargetLand.pos);
                if (hex.genPotential < bestSeaDistance
                        || (hex.genPotential == bestSeaDistance && targetDist < bestSeaTargetDist)) {
                    bestSeaDistance = hex.genPotential;
                    bestSeaTargetDist = targetDist;
                    bestSea = hex;
                }
            } else {
                if (hex.genPotential == -2) continue; // 己方大陆的陆地（含其上的残余）不登录
                if (hex.fraction == fraction) continue; // 己方陆地不登录
                if (hex.isNeutral() && hex.containsMountain()) continue;
                // 登录目标：选离目标大陆最近的可登录陆格（优先敌方势力）
                double targetDist = hex.pos.fastDistanceTo(seaTargetLand.pos);
                int score = hex.isNeutral() ? 1 : 2;
                if (bestLand == null || score > bestLandScore || (score == bestLandScore && targetDist < bestLandTargetDist)) {
                    bestLandScore = score;
                    bestLandTargetDist = targetDist;
                    bestLand = hex;
                }
            }
        }

        // 能登录就先登录（朝目标大陆上岸），否则继续沿海路朝目标前进
        if (bestLand != null) return bestLand;
        return bestSea;
    }


    /**
     * 选择/更新海军进攻目标：离参考单位最近的非己方大陆（不含己方大陆及其残余）。
     * 目标被自己控制或失效后，下一回合自动重新选择。
     */
    private void updateSeaTarget(Unit referenceUnit) {
        int currentRound = gameController.matchStatistics.turnsMade;
        if (seaTargetRound == currentRound && seaTargetLand != null
                && seaTargetLand.active && seaTargetLand.fraction != fraction) {
            return; // 目标仍然有效
        }
        seaTargetRound = currentRound;
        seaTargetLand = null;

        double bestDist = Double.MAX_VALUE;
        for (Hex hex : gameController.fieldManager.activeHexes) {
            if (hex.isSea()) continue;
            if (hex.genPotential != 0) continue; // 只有非己方大陆的陆格 genPotential 为 0
            if (hex.fraction == GameRules.NEUTRAL_FRACTION && hex.containsMountain()) continue;
            double dist = hex.pos.fastDistanceTo(referenceUnit.currentHex.pos);
            if (dist < bestDist) {
                bestDist = dist;
                seaTargetLand = hex;
            }
        }
    }


    /**
     * 是否存在值得出海的海外目标（不在己方大陆上的陆地）。
     * 没有海外目标时，AI 不应无脑下海。
     */
    protected boolean hasOverseasTarget() {
        int currentRound = gameController.matchStatistics.turnsMade;
        if (seaTargetCheckedTurn == currentRound) return hasOverseasTargetValue;
        seaTargetCheckedTurn = currentRound;
        hasOverseasTargetValue = false;
        // 轻量判断：地图上存在非己方势力的陆地格（精确的海路距离场只在海上单位移动时计算）
        for (Hex hex : gameController.fieldManager.activeHexes) {
            if (hex.isSea()) continue;
            if (hex.fraction == fraction) continue;
            if (hex.fraction == GameRules.NEUTRAL_FRACTION && hex.containsMountain()) continue; // 中立山地不可登陆
            hasOverseasTargetValue = true;
            break;
        }
        return hasOverseasTargetValue;
    }


    /**
     * 本大陆是否存在接壤且收入不敌自己一半的弱势力（每回合判断一次）。
     * 存在时应优先陆地消灭，而不是下海远征。
     */
    protected boolean hasWeakEnemyOnContinent(Province province) {
        int currentRound = gameController.matchStatistics.turnsMade;
        if (weakEnemyCheckRound == currentRound) return weakEnemyOnContinentValue;
        weakEnemyCheckRound = currentRound;
        weakEnemyOnContinentValue = gameController.fieldManager.hasWeakEnemyOnContinent(province);
        return weakEnemyOnContinentValue;
    }


    /**
     * 无法下海时，从移动范围内选一个最靠近己方港口水域的格子，
     * 让单位主动走向港口，为下海做准备。没有己方港口水域时返回 null。
     */
    protected Hex findHexToApproachPort(Unit unit, ArrayList<Hex> moveZone) {
        ensureApproachCache();

        Hex targetWater = null;
        double bestWaterDist = Double.MAX_VALUE;
        for (Hex hex : cachedPortWaters) {
            double dist = hex.pos.fastDistanceTo(unit.currentHex.pos);
            if (dist < bestWaterDist) {
                bestWaterDist = dist;
                targetWater = hex;
            }
        }
        if (targetWater == null) {
            // 还没有港口：以最近的己方沿海格为目标，让单位先走向海岸（之后才能建港/下海）
            double bestLandDist = Double.MAX_VALUE;
            for (Hex hex : cachedCoastalLand) {
                double dist = hex.pos.fastDistanceTo(unit.currentHex.pos);
                if (dist < bestLandDist) {
                    bestLandDist = dist;
                    targetWater = hex;
                }
            }
            if (targetWater == null) return null;
        }

        Hex best = null;
        double bestDist = Double.MAX_VALUE;
        for (Hex hex : moveZone) {
            if (hex.isSea()) continue;
            if (hex.containsBuilding()) continue; // 农场/塔等不能作为落脚点
            if (!hex.sameFraction(fraction)) continue;
            double dist = hex.pos.fastDistanceTo(targetWater.pos);
            if (dist < bestDist) {
                bestDist = dist;
                best = hex;
            }
        }
        return best;
    }


    /**
     * 每回合缓存一次己方港口水域和沿海陆格，避免每个单位都遍历全图。
     */
    private void ensureApproachCache() {
        int currentRound = gameController.matchStatistics.turnsMade;
        if (approachCacheRound == currentRound) return;
        approachCacheRound = currentRound;

        cachedPortWaters.clear();
        cachedCoastalLand.clear();
        for (Hex hex : gameController.fieldManager.activeHexes) {
            if (hex.isSea()) {
                if (hex.inPortRange && hex.portOwnerFraction == fraction) {
                    cachedPortWaters.add(hex);
                }
            } else {
                if (hex.fraction == fraction && gameController.fieldManager.hasSeaNeighbor(hex)) {
                    cachedCoastalLand.add(hex);
                }
            }
        }
    }


    /**
     * 只在确实有海上单位需要移动时计算距离场（同一回合只算一次）。
     */
    private void ensureSeaDistanceField() {
        if (seaDistanceFieldTurn == gameController.matchStatistics.turnsMade) return;
        updateSeaDistanceField();
    }


    /**
     * 计算海上距离场：
     * 从所有非己方陆地格的海岸出发，只经过海格做反向 BFS
     * （不能进入敌方港口水域），把每个海格到最近非己方陆地的
     * 海上距离存入 genPotential，不可达的海格为 -1。
     * genPotential 仅在游戏开局生成地图时使用，游戏进行中空闲，可以复用。
     */
    private void updateSeaDistanceField() {
        seaDistanceFieldTurn = gameController.matchStatistics.turnsMade;
        ArrayList<Hex> activeHexes = gameController.fieldManager.activeHexes;

        // 重置：陆格默认 0（非己方大陆），海格默认 -1（不可达）
        for (Hex hex : activeHexes) {
            if (hex.isSea()) {
                hex.genPotential = -1;
            } else {
                hex.genPotential = 0;
            }
        }

        // 阶段 1：标记己方大陆连通块（与己方陆地格陆地相连的所有地块，含其上的敌方残余）
        ownContinentCache.clear();
        for (Hex hex : activeHexes) {
            if (hex.isSea()) continue;
            if (hex.fraction != fraction) continue;
            if (ownContinentCache.contains(hex)) continue;

            seaPropagationList.clear();
            seaPropagationList.add(hex);
            ownContinentCache.add(hex);
            hex.genPotential = -2; // 起点也标记为己方大陆
            int ownIndex = 0;
            while (ownIndex < seaPropagationList.size()) {
                Hex h = seaPropagationList.get(ownIndex);
                ownIndex++;
                for (int dir = 0; dir < 6; dir++) {
                    Hex adj = h.getAdjacentHex(dir);
                    if (adj == null || adj.isNullHex()) continue;
                    if (!adj.active || adj.isSea()) continue;
                    if (ownContinentCache.contains(adj)) continue;
                    ownContinentCache.add(adj);
                    adj.genPotential = -2; // 己方大陆的陆地标记
                    seaPropagationList.add(adj);
                }
            }
        }

        // 阶段 2：海上距离场，目标 = 不在己方大陆上的陆地（海外）
        seaPropagationList.clear();

        // 起点：海外陆地格的海岸海格，距离为 1
        for (Hex hex : activeHexes) {
            if (hex.isSea()) continue;
            if (hex.genPotential != 0) continue; // 排除己方大陆（-2）和已标记格
            for (int dir = 0; dir < 6; dir++) {
                Hex adj = hex.getAdjacentHex(dir);
                if (adj == null || adj.isNullHex()) continue;
                if (!adj.active || !adj.isSea()) continue;
                if (adj.genPotential >= 0) continue; // 已访问
                adj.genPotential = 1;
                seaPropagationList.add(adj);
            }
        }

        // 反向 BFS 扩散到整个海面
        int index = 0;
        while (index < seaPropagationList.size()) {
            Hex hex = seaPropagationList.get(index);
            index++;
            for (int dir = 0; dir < 6; dir++) {
                Hex adj = hex.getAdjacentHex(dir);
                if (adj == null || adj.isNullHex()) continue;
                if (!adj.active || !adj.isSea()) continue;
                if (adj.genPotential >= 0) continue; // 已访问
                // 敌方港口的水域不可进入
                if (adj.inPortRange && adj.portOwnerFraction != fraction) continue;
                adj.genPotential = hex.genPotential + 1;
                seaPropagationList.add(adj);
            }
        }
    }
}
