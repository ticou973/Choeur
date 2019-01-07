/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dedicace.com.ui;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Exposes the functionality of the {@link MediaPlayer} and implements the {@link PlayerAdapter}
 * so that {@link MainActivity} can control music playback.
 */
public final class MediaPlayerHolder implements PlayerAdapter {

    public static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000;

    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;
    private int mResourceId;
    private String mResourceStr;
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;
    private int i=0;
    private int duration;
    private String pathSave="";
    private final static String BASEURI = "storage/emulated/0/Android/data/dedicace.com/files/";
    private final static String BASEURI2 = "storage/emulated/0/DedicaceAdmin/";


    //todo voir quand mettre les release du mediaplayer et mediarecorder(voir doc de ref)

    public MediaPlayerHolder(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the {@link MainActivity} the {@link MediaPlayer} is
     * released. Then in the onStart() of the {@link MainActivity} a new {@link MediaPlayer}
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    private void initializeMediaPlayer() {
        Log.d(SongsAdapter.TAG, "MPH initializeMediaPlayer: ");
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopUpdatingCallbackWithPosition(true);
                    Log.d(SongsAdapter.TAG, "onCompletion: ");

                    //todo voir si on peut supprimer le playbackinfo

                    if (mPlaybackInfoListener != null) {
                        mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED);
                        mPlaybackInfoListener.onPlaybackCompleted();
                    }
                }
            });
        }
    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        mPlaybackInfoListener = listener;
    }

    // Implements PlaybackControl.
    //todo voir comment installer dès le départ le fichier pour l'accueil avec getFilesDir.
    @Override
    public void prepareMediaPlayer(Context context, String resStrToPlay) throws IOException {
        mContext=context;
        mResourceStr =resStrToPlay;
        //mResourceId = convertResStringToResourcesRaw(mResourceStr);
        initializeMediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //todo modifier avec le bon uribase
        String path4 = BASEURI + mResourceStr + ".mp3";

        mMediaPlayer =MediaPlayer.create(mContext,Uri.parse(path4));
        //setupMediaRecorder();

        Log.d(SongsAdapter.TAG, "prepareMediaPlayerB: "+resStrToPlay);

        //todo penser à créer les dossiers pour les songs
        initializeProgressCallback();
    }

    @Override
    public String record(String songNamePupitre) {

        if(isExternalStorageWritable()){
        pathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                UUID.randomUUID() + ".3gp";
        setupMediaRecorder();

        Log.d(SongsAdapter.TAG, "MPH record: "+ pathSave);

        try {
            Log.d(SongsAdapter.TAG, "MPH record: ");
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(SongsAdapter.TAG, "MPH record: ");
        }
        else{
            Toast.makeText(mContext, "Vous n'avez pas de mémoire externe disponible", Toast.LENGTH_SHORT).show();
        }
        return pathSave;
    }

    @Override
    public void stopRecord() {
        Log.d(SongsAdapter.TAG, "MPH stopRecord: ");
        if(mMediaRecorder!=null) {
            mMediaRecorder.stop();
        }else{
            Log.d(SongsAdapter.TAG, "MPH stopRecord: Mediaplayer null ");
        }
    }


    @Override
    public void release() {
        if (mMediaPlayer != null) {

            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }


    @Override
    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            //todo vérifier l'utilité de la méthode suivante
            mMediaPlayer.setScreenOnWhilePlaying(true);

            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);
            }
            startUpdatingCallbackWithPosition();
        }
    }

    @Override
    public void reset() throws IOException {
        if (mMediaPlayer != null) {

            mMediaPlayer.reset();
            prepareMediaPlayer(mContext,mResourceStr);
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.RESET);
            }
            stopUpdatingCallbackWithPosition(true);
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED);
            }

        }
    }

    @Override
    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    /**
     * Syncs the mMediaPlayer position with mPlaybackProgressCallback via recurring task.
     */
    private void startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
                    updateProgressCallbackTask();

                }
            };
        }
        mExecutor.scheduleAtFixedRate(
                mSeekbarPositionUpdateTask,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private void stopUpdatingCallbackWithPosition(boolean resetUIPlaybackPosition) {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
            mSeekbarPositionUpdateTask = null;
            Log.d(SongsAdapter.TAG, "stopUpdatingCallbackWithPosition: ");
            if (resetUIPlaybackPosition && mPlaybackInfoListener != null) {
                Log.d(SongsAdapter.TAG, "stopUpdatingCallbackWithPosition: ");
                mPlaybackInfoListener.onPositionChanged(0);

            }
        }
        i=0;
    }

    private void updateProgressCallbackTask() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(currentPosition);
            }
        }
    }

    @Override
    public void initializeProgressCallback() {
        duration = mMediaPlayer.getDuration();
        Log.d(SongsAdapter.TAG, "initializeProgressCallback: "+ duration);
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(0);
        }
    }

    @Override
    public int getDuration() {

        return duration;
    }

    private void setupMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        Log.d(SongsAdapter.TAG, "MPH setupMediaRecorder: "+mMediaRecorder);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(pathSave);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //todo voir quand release le mediaplayer
}
