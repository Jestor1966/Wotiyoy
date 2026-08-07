package yio.tro.antiyoy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class MusicManager {


    private static MusicManager instance = null;

    public Music music;
    private static FileHandle fileHandle;


    public static void initialize() {
        fileHandle=Gdx.files.external("AntiyoyDatas/Music/temp.txt");
        fileHandle.writeString("temp",false);
        instance = null;
    }


    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }


    public void onMusicStatusChanged() {
        if (SettingsManager.musicEnabled) {
            if (music.isPlaying()) return;
            play();
        } else {
            if (!music.isPlaying()) return;
            stop();
        }
    }


    public void play() {
        if (music == null) return;
        music.play();
        music.setLooping(true);
    }


    public void stop() {
        if (music == null) return;
        music.stop();
    }


    public void load() {
        if (YioGdxGame.platformType == PlatformType.ios) {
            music = Gdx.audio.newMusic(Gdx.files.internal("sound/music/music.mp3"));
            return;
        }
        if(Gdx.files.external("AntiyoyDatas/Music/fullMusic.ogg").exists()){
            music = Gdx.audio.newMusic(Gdx.files.external("AntiyoyDatas/Music/fullMusic.ogg"));
        } else{
            music = Gdx.audio.newMusic(Gdx.files.internal("sound/music/music.ogg"));
        }
    }

}
