package yio.tro.antiyoy.gameplay.name_generator;

import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.gameplay.FieldManager;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.stuff.LanguagesManager;

import java.util.*;

public class LeaderNameGenerator {


    private static LeaderNameGenerator instance = null;
    NameGenerator nameGenerator;
    ArrayList<String> masks;
    HashMap<String, String> groups;


    public LeaderNameGenerator() {
        nameGenerator = new NameGenerator();

        masks = new ArrayList<>();
        groups = new HashMap<>();
    }


    public static void initialize() {
        instance = null;
    }


    public static LeaderNameGenerator getInstance() {
        if (instance == null) {
            instance = new LeaderNameGenerator();
        }

        return instance;
    }


    public void load() {
        loadMasks();
        loadGroups();
    }


    private void loadGroups() {
        String groupsSrcString = LanguagesManager.getInstance().getString("leader_name_gen_groups");

        StringTokenizer tokenizer = new StringTokenizer(groupsSrcString, "#");
        groups.clear();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int indexOfSeparator = token.indexOf(':');
            String key = token.substring(0, indexOfSeparator);
            String value = " "+token.substring(indexOfSeparator + 1);

            groups.put(key, value);
        }
    }


    private void loadMasks() {
        String masksSrcString = LanguagesManager.getInstance().getString("leader_name_gen_masks");

        StringTokenizer tokenizer = new StringTokenizer(masksSrcString, " ");

        masks.clear();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            masks.add(token);
        }
    }


    public String generateName(Hex capitalHex) {

        Random random = new Random(capitalHex.index1 + 53 * capitalHex.index2);

        nameGenerator.setMasks(masks);
        nameGenerator.setGroups(groups);
        nameGenerator.setCapitalize(true);

        return nameGenerator.generateName(random);
    }
}
