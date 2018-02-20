package com.colorchains.colorchainsgl;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by francisco.cnmarao on 17/04/2017.
 */

public class Media {
    private static MediaPlayer bgMusic;
    private static AudioManager am;
    private static AudioManager.OnAudioFocusChangeListener afChangeListener;
    public  static List<Uri> tracks = new ArrayList<>();
    public static int curTrack = -1;
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
        tracks.add(Uri.parse("android.resource://" + GameView.context.getPackageName() + "/" + R.raw.file1));
        tracks.add(Uri.parse("android.resource://" + GameView.context.getPackageName() + "/" + R.raw.file2));
        tracks.add(Uri.parse("android.resource://" + GameView.context.getPackageName() + "/" + R.raw.file3));
        tracks.add(Uri.parse("android.resource://" + GameView.context.getPackageName() + "/" + R.raw.file4));
        tracks.add(Uri.parse("android.resource://" + GameView.context.getPackageName() + "/" + R.raw.file5));
        tracks.add(Uri.parse("android.resource://" + GameView.context.getPackageName() + "/" + R.raw.file6));

        bgMusic = new MediaPlayer();
        bgMusic.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                //finish(); // finish current activity
                //mp.selectTrack(0);
                if(playingPuzzleBGM) {
                    if (curTrack + 1 >= tracks.size()) {
                        Collections.shuffle(tracks);
                        curTrack = -1;
                    }
                    curTrack++;
                    playCurrentTrack(mp);
                }
            }
        });
        //song will be started after completion of preparing...
        bgMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer player) {
                if(GameView.playBGM)
                    player.start();
            }

        });
        if(GameView.playBGM)
            setBackGroundMusic(R.raw.title);
    }

    public static void playCurrentTrack(MediaPlayer mp) {
        try {
            mp.reset();
            mp.setDataSource(GameView.context, tracks.get(curTrack));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.prepareAsync();
    }

    private static void release() {
        bgMusic.release();
        bgMusic = null;
    }

    public static void setBackGroundMusic(int bg){

        try {
            bgMusic.reset();
            bgMusic.setDataSource(GameView.context, Uri.parse("android.resource://" + GameView.context.getPackageName() + "/" + bg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        bgMusic.prepareAsync();
        //bgMusic = MediaPlayer.create(GameView.context, bg/*R.raw.title*/);
    }

    public static boolean playingPuzzleBGM = false;
    public static void playPuzzleBGM() throws Exception {
        if(!GameView.playBGM) {
            if(playingPuzzleBGM){
                stopPuzzleBGM();
            }
            return;
        }
        playingPuzzleBGM = true;
        curTrack = 0;
        Collections.shuffle(tracks);
        if(bgMusic == null) throw new Exception("Media Player not set!");
        playCurrentTrack(bgMusic);
    }

    public static void stopPuzzleBGM(){
        playingPuzzleBGM = false;
        bgMusic.pause();
        bgMusic.reset();
    }

    public static void pause()
    {
        bgMusic.pause();
    }

    public static void unpause()
    {
        if(bgMusic != null)
            bgMusic.start();
    }
}
