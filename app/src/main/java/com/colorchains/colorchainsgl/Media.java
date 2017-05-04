package com.colorchains.colorchainsgl;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * Created by francisco.cnmarao on 17/04/2017.
 */

public class Media {
    private static MediaPlayer bgMusic;
    private static AudioManager am;
    private static AudioManager.OnAudioFocusChangeListener afChangeListener;
    public static void init() {
        am = (AudioManager) GameView.context.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        afChangeListener =
                new AudioManager.OnAudioFocusChangeListener() {
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            // Pause playback because your Audio Focus was
                            // temporarily stolen, but will be back soon.
                            // i.e. for a phone call
                            Media.pause();
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            // Stop playback, because you lost the Audio Focus.
                            // i.e. the user started some other playback app
                            // Remember to unregister your controls/buttons here.
                            // And release the kra — Audio Focus!
                            // You’re done.
                            Media.pause();
                            Media.release();
                            am.abandonAudioFocus(afChangeListener);
                        } else if (focusChange ==
                                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            // Lower the volume, because something else is also
                            // playing audio over you.
                            // i.e. for notifications or navigation directions
                            // Depending on your audio playback, you may prefer to
                            // pause playback here instead. You do you.
                            Media.pause();
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            // Resume playback, because you hold the Audio Focus
                            // again!
                            // i.e. the phone call ended or the nav directions
                            // are finished
                            // If you implement ducking and lower the volume, be
                            // sure to return it to normal here, as well.
                            Media.unpause();
                        }
                    }
                };
    }

    private static void release() {
        bgMusic.release();
        bgMusic = null;
    }

    public static void setBackGroundMusic(int bg){
        bgMusic = MediaPlayer.create(GameView.context, bg/*R.raw.title*/);
    }

    public static void playBGM() throws Exception {
        if(bgMusic == null) throw new Exception("Media Player not set!");
        bgMusic.setLooping(true);
        bgMusic.start();
    }

    public static void pause()
    {
        bgMusic.pause();
    }

    public static void unpause()
    {
        bgMusic.start();
    }
}
