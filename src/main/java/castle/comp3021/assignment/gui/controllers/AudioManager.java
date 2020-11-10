package castle.comp3021.assignment.gui.controllers;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles audio related events.
 */
public class AudioManager {

    private static final AudioManager INSTANCE = new AudioManager();
    /**
     * Set of all currently playing sounds.
     *
     * <p>
     * Use this to keep a reference to the sound until it finishes playing.
     * </p>
     */
    private final Set<MediaPlayer> soundPool = new HashSet<>();
    private boolean enabled = true;

    /**
     * Enumeration of known sound resources.
     */
    public enum SoundRes {
        WIN, PLACE, CLICK,LOSE;
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private AudioManager() {
    }

    public static AudioManager getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Plays the sound. If disabled, simply return.
     *
     * <p>
     * Hint:
     * <ul>
     * <li>Use {@link MediaPlayer#play()} and {@link MediaPlayer#dispose()}.</li>
     * <li>When creating a new MediaPlayer object, add it into {@link AudioManager#soundPool} before playing it.</li>
     * <li>Set a callback for when the sound has completed playing. This is to remove it from the soundpool, and
     * dispose the player using a daemon thread.</li>
     * </ul>
     *
     * @param name the name of the sound file to be played, excluding .mp3
     */
    private void playFile(final String name) {
        if (enabled == false) return;
        Media music = new Media(ResourceLoader.getResource("/assets/audio/"+name+".mp3"));
        MediaPlayer mediaPlayer =  new MediaPlayer(music);
        mediaPlayer.setOnEndOfMedia(()->{
            mediaPlayer.dispose();
            soundPool.remove(mediaPlayer);
        });
        soundPool.add(mediaPlayer);
        mediaPlayer.play();
    }

    /**
     * Plays a sound.
     *
     * @param name Enumeration of the sound, given by {@link SoundRes}.
     */
    public void playSound(final SoundRes name) {
        String fileName = "";
        switch (name){
            case PLACE:
                fileName = "place";
                break;
            case LOSE:
                fileName = "lose";
                break;
            case WIN:
                fileName = "win";
                break;
            case CLICK:
                fileName = "click";
                break;
            default:
                break;
        }
        playFile(fileName.toString());
    }
}