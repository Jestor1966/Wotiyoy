package yio.tro.antiyoy.gameplay;

import yio.tro.antiyoy.gameplay.diplomacy.DiplomacyManager;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomaticEntity;
import yio.tro.antiyoy.gameplay.name_generator.CityNameGenerator;
import yio.tro.antiyoy.gameplay.name_generator.LeaderNameGenerator;
import yio.tro.antiyoy.gameplay.rules.GameRules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NamingManager implements SavableYio{

    GameController gameController;
    public HashMap<Hex, String> renamedHexes,renamedLeaderHexes;
    StringBuilder stringBuilder;


    public NamingManager(GameController gameController) {
        this.gameController = gameController;
        renamedHexes = new HashMap<>();
        renamedLeaderHexes = new HashMap<>();
        stringBuilder = new StringBuilder();
    }


    public void defaultValues() {
        renamedHexes.clear();
        renamedLeaderHexes.clear();
    }


    public void onEndCreation() {

    }


    public void move() {

    }


    public String getProvinceName(Province province) {
        Hex capital = province.getCapital();

        if (renamedHexes.containsKey(capital)) {
            return renamedHexes.get(capital);
        }

        return CityNameGenerator.getInstance().generateName(capital);
    }

    public String getProvinceLeader(Province province) {
        Hex capital = province.getCapital();

        if (renamedLeaderHexes.containsKey(capital)) {
            return renamedLeaderHexes.get(capital);
        }

        return LeaderNameGenerator.getInstance().generateName(capital);
    }


    public boolean isThereAtLeastOneRename() {
        if (renamedHexes.entrySet().size() > 0) return true;
        if (renamedLeaderHexes.entrySet().size() > 0) return true;

        return false;
    }


    public void setHexName(Hex hex, String name) {
        renamedHexes.put(hex, name);


        if (GameRules.diplomacyEnabled) {
            updateRelatedDiplomaticEntity(hex);
        }
    }

    public void setLeaderName(Hex hex, String leader) {
        renamedLeaderHexes.put(hex, leader);
        /*
        Set<Hex> b=renamedLeaderHexes.keySet();
        for(Hex a:b){
            System.out.println(a.toString()+","+renamedLeaderHexes.get(a));
        }*/

        if (GameRules.diplomacyEnabled) {
            updateRelatedDiplomaticEntity(hex);
        }
    }


    public boolean isNameUsed(String string) {
        for (String token : renamedHexes.values()) {
            if (token.equals(string)) return true;
        }
        return false;
    }


    private void updateRelatedDiplomaticEntity(Hex hex) {
        FieldManager fieldManager = gameController.fieldManager;
        DiplomacyManager diplomacyManager = fieldManager.diplomacyManager;
        DiplomaticEntity entity = diplomacyManager.getEntity(hex.fraction);
        if (entity == null) return;
        if (!entity.alive) return;

        entity.updateCapitalName();
        entity.updateLeaderName();
    }


    public void checkForCapitalRelocate(int previousObject, Hex previousHex, Province province) {
        if (previousObject != Obj.TOWN) return;
        if (!renamedHexes.containsKey(previousHex)) return;
        String name = renamedHexes.get(previousHex);
        String leader = renamedLeaderHexes.get(previousHex);
        renamedHexes.remove(previousHex);
        renamedLeaderHexes.remove(previousHex);
        setHexName(province.getCapital(), name);
        setLeaderName(province.getCapital(),leader);
    }


    @Override
    public String saveToString() {
        if (!isThereAtLeastOneRename()) return "-";

        stringBuilder.setLength(0);

        for (Map.Entry<Hex, String> entry : renamedHexes.entrySet()) {
            for (Map.Entry<Hex,String> entry2: renamedLeaderHexes.entrySet()){
                Hex hex = entry.getKey();
                String name = entry.getValue();
                String leader = entry2.getValue();
                stringBuilder.append(hex.index1).append("/").append(hex.index2).append("/").append(name).append("/").append(leader).append(",");
            }
        }

        return stringBuilder.toString();
    }


    @Override
    public void loadFromString(String src) {
        if (src.length() == 0) return;
        if (src.equals("-")) return;

        String[] renamedHexesSplit = src.split(",");
        for (Province province : gameController.fieldManager.provinces) {
            province.name = "";
            province.leader= "";
        }
        for (DiplomaticEntity entity : gameController.fieldManager.diplomacyManager.entities) {
            entity.capitalName = null;
            entity.leaderName = null;
        }
        renamedHexes.clear();
        System.out.println(src);
        for (String token : renamedHexesSplit) {
            String[] split = token.split("/");
            if (split.length < 4) continue;
            if (split[0].length() == 0) continue;
            if (split[1].length() == 0) continue;

            int index1 = Integer.valueOf(split[0]);
            int index2 = Integer.valueOf(split[1]);
            String name = split[2];
            String leader = split[3];
            /*
            if (split.length > 3) {
                StringBuilder builder = new StringBuilder();
                for (int i = 2; i < split.length; i++) {
                    builder.append(split[i]).append(" ");
                }
                int length = builder.length();
                builder.delete(length - 1, length);
                name = builder.toString();
            }*/
            Hex hex = gameController.fieldManager.getHex(index1, index2);

            //System.out.println("-------");
            //System.out.println(leader);
            setHexName(hex, name);
            setLeaderName(hex, leader);
            //System.out.println(leader);
        }

        forceProvincesToUpdateNames();
        updateDiplomaticNames();
    }


    private void updateDiplomaticNames() {
        for (DiplomaticEntity entity : gameController.fieldManager.diplomacyManager.entities) {
            entity.updateCapitalName();
            entity.updateLeaderName();
        }
    }


    private void forceProvincesToUpdateNames() {
        for (Province province : gameController.fieldManager.provinces) {
            province.updateName();
            province.updateLeader();
        }
    }
}
