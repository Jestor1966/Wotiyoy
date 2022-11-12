package yio.tro.antiyoy.gameplay.editor;

import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Province;
import yio.tro.antiyoy.gameplay.data_storage.EncodeableYio;
import yio.tro.antiyoy.gameplay.name_generator.CityNameGenerator;
import yio.tro.antiyoy.gameplay.name_generator.LeaderNameGenerator;
import yio.tro.antiyoy.gameplay.name_generator.NameGenerator;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.stuff.object_pool.ReusableYio;

import java.util.ArrayList;

public class EditorProvinceData implements ReusableYio, EncodeableYio{

    public String name,leader;
    public int startingMoney;
    public int id;
    public float[] aiPara;
    public ArrayList<Hex> hexList;
    public PointYio geometricalCenter;


    public EditorProvinceData() {
        hexList = new ArrayList<>();
        geometricalCenter = new PointYio();
        aiPara=new float[3];
    }


    @Override
    public void reset() {
        name = "";
        leader = "";
        startingMoney = 10;
        hexList.clear();
        id = -1;
        aiPara[0]=1f;
        aiPara[1]=1f;
        aiPara[2]=1f;
    }


    void copySomeDataFrom(EditorProvinceData src) {
        name = src.name;
        leader = src.leader;
        startingMoney = src.startingMoney;
        id = src.id;
    }


    public void fillWithDefaultData() {
        startingMoney = 10;
        aiPara[0]=1f;
        aiPara[1]=1f;
        aiPara[2]=1f;
        generateRandomName();
        generateRandomLeader();
    }


    private void generateRandomName() {
        name = CityNameGenerator.getInstance().generateName(hexList.get(0));
    }

    private void generateRandomLeader() {
        leader = LeaderNameGenerator.getInstance().generateName(hexList.get(0));
    }


    int countIntersection(EditorProvinceData anotherProvince) {
        for (Hex hex : hexList) {
            hex.flag = false;
        }
        for (Hex hex : anotherProvince.hexList) {
            hex.flag = true;
        }
        int c = 0;
        for (Hex hex : hexList) {
            if (!hex.flag) continue;
            c++;
        }
        return c;
    }


    public int countFraction(int fraction) {
        int c = 0;
        for (Hex hex : hexList) {
            if (hex.fraction != fraction) continue;
            c++;
        }
        return c;
    }


    public int getMajorFraction() {
        int majorFraction = -1;
        int majorValue = 0;
        for (int fraction = 0; fraction < GameRules.MAX_FRACTIONS_QUANTITY; fraction++) {
            int value = countFraction(fraction);
            if (value == 0) continue;
            if (majorFraction == -1 || value > majorValue) {
                majorFraction = fraction;
                majorValue = value;
            }
        }
        return majorFraction;
    }


    public void applyDataToRealProvince(Province province) {
        province.setName(name);
        province.setLeader(leader);
        province.money = startingMoney;
        province.aiPara[0] = aiPara[0];
        province.aiPara[1] = aiPara[1];
        province.aiPara[2] = aiPara[2];
    }


    public boolean contains(Hex hex) {
        return hexList.contains(hex);
    }


    int getFraction() {
        return hexList.get(0).fraction;
    }


    public boolean isBigEnough() {
        return hexList.size() > 1;
    }


    public void updateGeometricalCenter() {
        geometricalCenter.reset();
        for (Hex hex : hexList) {
            geometricalCenter.x += hex.pos.x;
            geometricalCenter.y += hex.pos.y;
        }
        geometricalCenter.x /= hexList.size();
        geometricalCenter.y /= hexList.size();
    }


    public String getUniqueCode() {
        String s = super.toString();
        return s.substring(s.indexOf("@"));
    }

    public Province getProvince() {
        return getProvince();
    }


    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "[ProvinceData " +
                getUniqueCode() +
                ": " +
                name +
                " " +
                "f" + getFraction() +
                " " +
                hexList.size() +
                "]";
    }


    @Override
    public String encode() {
        Hex firstHex = hexList.get(0);
        return firstHex.index1 + "@" + firstHex.index2 + "@" + id + "@" + name + "@" + startingMoney + "@" +leader +"@" + aiPara[0] + "@" +aiPara[1] + "@" + aiPara[2] + "@";
    }


    public boolean containsInvalidSymbols() {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == ' ') continue;
            if (c == '.') continue;
            if (!Fonts.getAllCharacters().contains("" + c)) return true;
        }
        return false;
    }


    @Override
    public void decode(String source) {
        String[] split = source.split("@");
        id = Integer.parseInt(split[2]);
        if (split.length<9) {
            aiPara[0]=1f;
            aiPara[1]=1f;
            aiPara[2]=1f;
            if (split.length<6) {
                generateRandomLeader();
                if(split.length<4){
                    generateRandomName();
                }
                return;
            }
        }else{
            aiPara[0] = Float.parseFloat(split[6]);
            aiPara[1] = Float.parseFloat(split[7]);
            aiPara[2] = Float.parseFloat(split[8]);
        }
        name = split[3];
        startingMoney = Integer.parseInt(split[4]);
        leader = split[5];




        if (containsInvalidSymbols()) {
            generateRandomName();
        }
    }


}
