package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.LevelSize;
import yio.tro.antiyoy.gameplay.data_storage.ImportManager;
import yio.tro.antiyoy.gameplay.editor.EditorSaveSystem;
import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.behaviors.RbStartSkirmishGame;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.scenes.AbstractScene;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.menu.slider.SliderBehavior;
import yio.tro.antiyoy.menu.slider.SliderYio;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.LanguagesManager;
import yio.tro.antiyoy.stuff.RectangleYio;

import java.net.*;
import java.io.*;

public class SceneEditorLobby extends AbstractScene{


    private Reaction rbCreate;
    private ButtonYio label;
    private SliderYio mapSizeSlider;
    private ButtonYio downloadButton;
    private ButtonYio newLevelButton;
    private ButtonYio loadButton;
    private ButtonYio importButton;
    private Reaction rbLoad;
    private Reaction rbImport;
    private Reaction rbDownload;


    public SceneEditorLobby(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);
        initReactions();
    }


    private void initReactions() {
        rbCreate = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onCreateButtonPressed();
            }
        };
        rbLoad = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onLoadButtonPressed();
            }
        };
        rbImport = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onImportButtonPressed();
            }
        };
        rbDownload = new Reaction() {
            @Override
            public void perform(ButtonYio buttonYio) {
                onDownloadButtonPressed();
            }
        };
    }


    @Override
    public void create() {
        menuControllerYio.beginMenuCreation();
        menuControllerYio.getYioGdxGame().beginBackgroundChange(2, true, true);
        menuControllerYio.spawnBackButton(130, Reaction.rbChooseGameModeMenu);
        createInternals();
        menuControllerYio.endMenuCreation();
    }


    private void createInternals() {
        createLabel();
        createSlider();
        createNewLevelButton();
        createLoadButton();
        createImportButton();
        createDownloadButton();
    }

    private void createDownloadButton() {
        downloadButton = buttonFactory.getButton(generateRectangle(0.1, 0.1, 0.8, SceneEditorOverlay.PANEL_HEIGHT), 135, getString("download"));
        downloadButton.setAnimation(Animation.fixed_down);
        downloadButton.setReaction(rbDownload);
    }


    private void createImportButton() {
        importButton = buttonFactory.getButton(generateRectangle(0.1, 0.2, 0.8, SceneEditorOverlay.PANEL_HEIGHT), 133, getString("import"));
        importButton.setAnimation(Animation.fixed_down);
        importButton.setReaction(rbImport);
    }


    private void createLoadButton() {
        loadButton = buttonFactory.getButton(generateRectangle(0.1, 0.3, 0.8, SceneEditorOverlay.PANEL_HEIGHT), 132, getString("choose_game_mode_load"));
        loadButton.setAnimation(Animation.fixed_down);
        loadButton.setReaction(rbLoad);
    }

    private void createNewLevelButton() {
        newLevelButton = buttonFactory.getButton(generateRectangle(0.5, 0.5, 0.4, 0.05), 134, LanguagesManager.getInstance().getString("create"));
        newLevelButton.setShadow(false);
        newLevelButton.setAnimation(Animation.fixed_up);
        newLevelButton.setReaction(rbCreate);
    }


    private void createSlider() {
        initSlider();
        mapSizeSlider.appear();
    }


    private void initSlider() {
        if (mapSizeSlider != null) return;

        double sWidth = 0.6;
        RectangleYio pos = generateRectangle((1 - sWidth) / 2, 0, sWidth, 0);

        mapSizeSlider = new SliderYio(menuControllerYio, -1);
        mapSizeSlider.setValues(0.5, 1, 6, Animation.none);
        mapSizeSlider.setPosition(pos);
        mapSizeSlider.setParentElement(label, 0.1);
        mapSizeSlider.setTitle("map_size");
        mapSizeSlider.setValueIndex(2);
        mapSizeSlider.setVerticalTouchOffset(0.05f * GraphicsYio.height);
        mapSizeSlider.setBehavior(getMapSizeSliderBehavior());

        menuControllerYio.addElementToScene(mapSizeSlider);
    }


    private SliderBehavior getMapSizeSliderBehavior() {
        return new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return LevelSize.convertToString(sliderYio.getValueIndex());
            }
        };
    }


    private void createLabel() {
        label = buttonFactory.getButton(generateRectangle(0.1, 0.5, 0.8, 0.22), 131, " ");
        label.setTouchable(false);
        label.setAnimation(Animation.fixed_up);
    }


    private void onCreateButtonPressed() {
        int levelSize = RbStartSkirmishGame.getLevelSizeBySliderPos(mapSizeSlider);
        GameController gameController = getGameController();
        EditorSaveSystem editorSaveSystem = gameController.editorSaveSystem;
        editorSaveSystem.createNewLevel(levelSize);
    }


    private void onLoadButtonPressed() {
        Scenes.sceneEditorLoad.create();
    }


    private void onImportButtonPressed() {
        ImportManager importManager = getGameController().importManager;
        importManager.launchGameFromClipboard();
    }

    private void onDownloadButtonPressed() {
        Scenes.sceneNotification.show("Please Wait Update!");
        /*
        try {
            Socket socket=new Socket("127.0.0.1",4700);
            BufferedReader sin=new BufferedReader(new InputStreamReader(System.in));
            PrintWriter os=new PrintWriter(socket.getOutputStream());
            BufferedReader is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readline;
            readline=sin.readLine(); //从系统标准输入读入一字符串
            while(!readline.equals("bye")){
                os.println(readline);
                os.flush();
                System.out.println("Client:"+readline);
                System.out.println("Server:"+is.readLine());
                readline=sin.readLine(); //从系统标准输入读入一字符串
            }
            os.close(); //关闭Socket输出流
            is.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Error"+e);
        }
*/
    }
}
