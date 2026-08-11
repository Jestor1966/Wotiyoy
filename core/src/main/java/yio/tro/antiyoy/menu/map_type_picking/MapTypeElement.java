package yio.tro.antiyoy.menu.map_type_picking;

import yio.tro.antiyoy.SoundManagerYio;
import yio.tro.antiyoy.menu.AbstractRectangularUiElement;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.render.MenuRender;
import yio.tro.antiyoy.menu.scrollable_list.ListBehaviorYio;
import yio.tro.antiyoy.menu.scrollable_list.ListItemYio;
import yio.tro.antiyoy.menu.scrollable_list.ScrollableListYio;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.LanguagesManager;
import yio.tro.antiyoy.stuff.RenderableTextYio;
import yio.tro.antiyoy.stuff.SelectionEngineYio;

public class MapTypeElement extends AbstractRectangularUiElement {

    public RenderableTextYio title;
    public RenderableTextYio valueText;
    public SelectionEngineYio selectionEngineYio;
    int valueIndex;
    ScrollableListYio dropdown;
    boolean dropdownShown;


    public MapTypeElement(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        title = new RenderableTextYio();
        title.setFont(Fonts.smallerMenuFont);
        valueText = new RenderableTextYio();
        valueText.setFont(Fonts.smallerMenuFont);
        selectionEngineYio = new SelectionEngineYio();
        valueIndex = 0;
        dropdownShown = false;
    }


    public void setTitle(String key) {
        title.setString(LanguagesManager.getInstance().getString(key));
        title.updateMetrics();
    }


    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
        updateValueText();
    }


    private void updateValueText() {
        String key;
        switch (valueIndex) {
            default:
                key = "map_type_default";
                break;
            case 1:
                key = "map_type_continental";
                break;
            case 2:
                key = "map_type_islands";
                break;
        }
        valueText.setString(LanguagesManager.getInstance().getString(key));
        valueText.updateMetrics();
    }


    public int getValueIndex() {
        return valueIndex;
    }


    @Override
    protected void onMove() {
        updateTitlePosition();
        updateValueTextPosition();
        selectionEngineYio.move();
    }


    private void updateValueTextPosition() {
        valueText.centerVertical(viewPosition);
        valueText.position.x = (float) (viewPosition.x + viewPosition.width - viewPosition.height / 3 - valueText.width);
        valueText.updateBounds();
    }


    private void updateTitlePosition() {
        title.position.x = (float) (viewPosition.x + viewPosition.height / 3);
        title.centerVertical(viewPosition);
        title.updateBounds();
    }


    @Override
    protected void onDestroy() {

    }


    @Override
    protected void onAppear() {
        updateValueText();

        if (dropdownShown && dropdown != null) {
            dropdown.destroy();
            dropdownShown = false;
        }
    }


    @Override
    protected void onTouchDown() {
        selectionEngineYio.select();
    }


    @Override
    protected void onTouchDrag() {

    }


    @Override
    protected void onTouchUp() {

    }


    @Override
    protected void onClick() {
        SoundManagerYio.playSound(SoundManagerYio.soundPressButton);
        toggleDropdown();
    }


    private void toggleDropdown() {
        initDropdown();

        if (dropdownShown) {
            dropdown.destroy();
            dropdownShown = false;
        } else {
            dropdown.appear();
            dropdownShown = true;
        }
    }


    private void initDropdown() {
        if (dropdown != null) return;

        dropdown = new ScrollableListYio(menuControllerYio);
        dropdown.setPosition(menuControllerYio.generateRectangle(0.1, 0.18, 0.8, 0.45));
        dropdown.setTitle(LanguagesManager.getInstance().getString("map_type"));
        dropdown.clearItems();
        dropdown.addItem("map_type_default", LanguagesManager.getInstance().getString("map_type_default"), "");
        dropdown.addItem("map_type_continental", LanguagesManager.getInstance().getString("map_type_continental"), "");
        dropdown.addItem("map_type_islands", LanguagesManager.getInstance().getString("map_type_islands"), "");
        dropdown.setListBehavior(new ListBehaviorYio() {
            @Override
            public void applyItem(ListItemYio item) {
                onDropdownItemSelected(item);
            }


            @Override
            public void onItemRenamed(ListItemYio item) {

            }


            @Override
            public void onItemDeleteRequested(ListItemYio item) {

            }
        });

        menuControllerYio.addElementToScene(dropdown);
    }


    private void onDropdownItemSelected(ListItemYio item) {
        if (item.key.equals("map_type_islands")) {
            setValueIndex(2);
        } else if (item.key.equals("map_type_continental")) {
            setValueIndex(1);
        } else {
            setValueIndex(0);
        }

        SoundManagerYio.playSound(SoundManagerYio.soundPressButton);
        dropdown.destroy();
        dropdownShown = false;
    }


    @Override
    public boolean checkToPerformAction() {
        return false;
    }


    @Override
    public MenuRender getRenderSystem() {
        return MenuRender.renderMapTypeElement;
    }
}
