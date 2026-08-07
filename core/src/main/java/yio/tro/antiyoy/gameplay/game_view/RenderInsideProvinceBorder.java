package yio.tro.antiyoy.gameplay.game_view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.GraphicsYio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RenderInsideProvinceBorder extends GameRender {

    private static final int GROUP_UPDATE_INTERVAL = 60;
    private static final int MIN_HEXES_TO_SHOW_NAME = 4;

    private TextureRegion provincePixel;
    private ArrayList<ArrayList<Province>> groups;
    private ArrayList<Province> cachedProvinceList;
    private HashMap<Province, HashSet<Hex>> provinceHexSets;
    private HashMap<Hex, Province> hexToProvince;
    private int updateTimer;
    private int cachedHash;


    public RenderInsideProvinceBorder(GameRendersList gameRendersList) {
        super(gameRendersList);
    }


    @Override
    public void loadTextures() {
        provincePixel = loadTextureRegion("province_pixel.png", false);
    }


    @Override
    public void render() {
        if (!GameRules.keepDifferentProvinceNames) return;

        rebuildHexToProvince();
        renderBorders();
    }


    public ArrayList<ArrayList<Province>> getGroups() {
        updateGroupCache();
        return groups;
    }


    // hex -> province mapping must always be up to date, because provinces
    // get replaced by new objects on split/merge; rebuilding it every frame is cheap
    private void rebuildHexToProvince() {
        if (hexToProvince == null) {
            hexToProvince = new HashMap<>();
        }
        hexToProvince.clear();
        for (Province province : gameController.fieldManager.provinces) {
            for (Hex hex : province.hexList) {
                hexToProvince.put(hex, province);
            }
        }
    }


    private void updateGroupCache() {
        ArrayList<Province> provinces = gameController.fieldManager.provinces;
        if (groups != null && cachedProvinceList == provinces && provinces.hashCode() == cachedHash && updateTimer > 0) {
            updateTimer--;
            return;
        }

        recomputeGroups(provinces);
        updateTimer = GROUP_UPDATE_INTERVAL;
        cachedProvinceList = provinces;
        cachedHash = provinces.hashCode();
    }


    private void recomputeGroups(ArrayList<Province> provinces) {
        buildMaps(provinces);
        groups = new ArrayList<>();
        ArrayList<Province> processed = new ArrayList<>();

        for (Province province : provinces) {
            if (province.hexList.size() < MIN_HEXES_TO_SHOW_NAME) continue;
            if (processed.contains(province)) continue;

            groups.add(getAdjacentSameFractionGroup(province, provinces, processed));
        }
    }


    private void buildMaps(ArrayList<Province> provinces) {
        provinceHexSets = new HashMap<>();
        for (Province province : provinces) {
            provinceHexSets.put(province, new HashSet<Hex>(province.hexList));
        }
    }


    private ArrayList<Province> getAdjacentSameFractionGroup(Province start, ArrayList<Province> provinces, ArrayList<Province> processed) {
        ArrayList<Province> group = new ArrayList<>();
        ArrayList<Province> queue = new ArrayList<>();
        group.add(start);
        queue.add(start);

        while (queue.size() > 0) {
            Province current = queue.remove(0);
            for (Province other : provinces) {
                if (other == current || group.contains(other)) continue;
                if (other.hexList.size() < MIN_HEXES_TO_SHOW_NAME) continue;
                if (other.getFraction() != current.getFraction()) continue;
                if (areProvincesAdjacent(current, other)) {
                    group.add(other);
                    queue.add(other);
                }
            }
        }

        processed.addAll(group);
        return group;
    }


    private boolean areProvincesAdjacent(Province a, Province b) {
        Province smaller = a.hexList.size() <= b.hexList.size() ? a : b;
        HashSet<Hex> otherSet = (smaller == a) ? provinceHexSets.get(b) : provinceHexSets.get(a);

        for (Hex hex : smaller.hexList) {
            for (int dir = 0; dir < 6; dir++) {
                Hex adj = hex.getAdjacentHex(dir);
                if (adj == null || adj.isNullHex()) continue;
                if (otherSet.contains(adj)) return true;
            }
        }
        return false;
    }


    private void renderBorders() {
        for (Province province : gameController.fieldManager.provinces) {
            for (Hex hex : province.hexList) {
                for (int dir = 0; dir < 6; dir++) {
                    Hex adj = hex.getAdjacentHex(dir);
                    if (adj == null || adj.isNullHex()) continue;
                    // draw each shared border only once, from the hex with smaller indexes
                    if (hex.index1 > adj.index1 || (hex.index1 == adj.index1 && hex.index2 > adj.index2)) continue;
                    Province adjProvince = hexToProvince.get(adj);
                    if (adjProvince == null || adjProvince == province) continue;
                    if (adjProvince.getFraction() != province.getFraction()) continue;

                    renderLineBetweenHexesWithOffset(
                            batchMovable,
                            provincePixel,
                            hex,
                            adj,
                            0.03 * GraphicsYio.width,
                            0,
                            dir,
                            1
                    );
                }
            }
        }
    }


    @Override
    public void disposeTextures() {
        if (provincePixel != null) {
            provincePixel.getTexture().dispose();
        }
    }
}
