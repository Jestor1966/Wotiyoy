package yio.tro.antiyoy.gameplay;

import yio.tro.antiyoy.stuff.LanguagesManager;

import java.util.ArrayList;


/**
 * Editor-only label attached to a hex. The label does not occupy the building
 * slot (objectInside) of the hex; it is a separate annotation that can be
 * placed, edited and hidden from the level editor.
 */
public class HexLabel {

    public static final int DEFAULT_LABEL_SIZE = 24;
    public static final int MIN_LABEL_SIZE = 8;
    public static final int MAX_LABEL_SIZE = 300;

    public static final int COLOR_WHITE = 0;
    public static final int COLOR_BLACK = 1;
    public static final int COLOR_RED = 2;
    public static final int COLOR_YELLOW = 3;
    public static final int COLOR_BLUE = 4;
    public static final int COLOR_GREEN = 5;
    public static final int COLOR_CYAN = 6;

    public static final String[] COLOR_NAME_KEYS = {
            "label_color_white", "label_color_black", "label_color_red",
            "label_color_yellow", "label_color_blue", "label_color_green", "label_color_cyan"
    };

    public static final float[][] COLOR_RGB = {
            {1f, 1f, 1f}, // white
            {0f, 0f, 0f}, // black
            {1f, 0f, 0f}, // red
            {1f, 1f, 0f}, // yellow
            {0f, 0f, 1f}, // blue
            {0f, 1f, 0f}, // green
            {0f, 1f, 1f}  // cyan
    };

    public String labeltext;
    public int labelcolor;
    public boolean isOutline;
    public float transparent; // transparency amount: 0 = opaque, 1 = fully transparent
    public boolean isVisible;
    public int labelSize;


    public HexLabel() {
        reset();
    }


    public void reset() {
        labeltext = "Label";
        labelcolor = COLOR_WHITE;
        isOutline = false;
        transparent = 0f;
        isVisible = false;
        labelSize = DEFAULT_LABEL_SIZE;
    }


    public void setBy(HexLabel other) {
        labeltext = other.labeltext;
        labelcolor = other.labelcolor;
        isOutline = other.isOutline;
        transparent = other.transparent;
        isVisible = other.isVisible;
        labelSize = other.labelSize;
    }


    public void delete() {
        reset();
    }


    public float[] getColorRgb() {
        int index = labelcolor;
        if (index < 0 || index >= COLOR_RGB.length) index = COLOR_WHITE;
        return COLOR_RGB[index];
    }


    public static String getColorName(int index) {
        if (index < 0 || index >= COLOR_NAME_KEYS.length) index = COLOR_WHITE;
        return LanguagesManager.getInstance().getString(COLOR_NAME_KEYS[index]);
    }


    public boolean isDefault() {
        return !isVisible &&
                !isOutline &&
                labelcolor == COLOR_WHITE &&
                transparent == 0f &&
                labelSize == DEFAULT_LABEL_SIZE &&
                labeltext.equals("Label");
    }


    public String encode() {
        return labeltext + "/" +
                labelcolor + "/" +
                (isOutline ? 1 : 0) + "/" +
                transparent + "/" +
                (isVisible ? 1 : 0) + "/" +
                labelSize;
    }


    public void decode(String source) {
        String[] split = source.split("/", 6);
        if (split.length < 5) return;

        labeltext = split[0];
        try {
            labelcolor = Integer.valueOf(split[1]);
        } catch (NumberFormatException e) {
            labelcolor = COLOR_WHITE;
        }
        isOutline = !split[2].equals("0");
        try {
            transparent = Float.valueOf(split[3]);
        } catch (NumberFormatException e) {
            transparent = 0f;
        }
        if (transparent < 0) transparent = 0;
        if (transparent > 1) transparent = 1;
        isVisible = !split[4].equals("0");
        if (split.length > 5) {
            try {
                labelSize = Integer.valueOf(split[5]);
            } catch (NumberFormatException e) {
                labelSize = DEFAULT_LABEL_SIZE;
            }
            if (labelSize < MIN_LABEL_SIZE) labelSize = MIN_LABEL_SIZE;
            if (labelSize > MAX_LABEL_SIZE) labelSize = MAX_LABEL_SIZE;
        } else {
            labelSize = DEFAULT_LABEL_SIZE;
        }
    }


    public static String encodeLabels(ArrayList<Hex> activeHexes) {
        StringBuilder builder = new StringBuilder();
        for (Hex hex : activeHexes) {
            if (hex.label == null || hex.label.isDefault()) continue;
            builder.append(hex.index1).append("/")
                    .append(hex.index2).append("/")
                    .append(hex.label.encode())
                    .append("@");
        }
        return builder.toString();
    }


    public static void decodeLabels(String source, FieldManager fieldManager) {
        if (source == null || source.length() < 2) return;

        for (String token : source.split("@")) {
            if (token.length() == 0) continue;
            String[] split = token.split("/", 5);
            if (split.length < 5) continue;

            int index1, index2;
            try {
                index1 = Integer.valueOf(split[0]);
                index2 = Integer.valueOf(split[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            Hex hex = fieldManager.getHex(index1, index2);
            if (hex == null || hex.label == null) continue;

            String labelSource = split[2] + "/" + split[3] + "/" + split[4];
            hex.label.decode(labelSource);
        }
    }
}
