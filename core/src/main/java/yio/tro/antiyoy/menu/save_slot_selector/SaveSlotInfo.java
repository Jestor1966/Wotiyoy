package yio.tro.antiyoy.menu.save_slot_selector;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SaveSlotInfo {


    public String name;
    public String description;
    public String key;
    public String size;
    public TextureRegion preview;


    public String getDescription() {
        if (description == null) {
            return "";
        }

        return description;
    }


    @Override
    public String toString() {
        return "[Slot: " +
                "key='" + key + "' " +
                "name='" + name + "' " +
                "description='" + description + "' " +
                "]";
    }
}
