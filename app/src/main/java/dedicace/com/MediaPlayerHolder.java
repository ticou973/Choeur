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

package dedicace.com;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

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
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;
    private int i=0;
    private int duration;

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
        Log.d(SongsAdapter.TAG, "initializeMediaPlayer: ");
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
    @Override
    public void prepareMediaPlayer(Context context, int songResId) {

        mContext=context;
        mResourceId =songResId;
        initializeMediaPlayer();

        mMediaPlayer=MediaPlayer.create(mContext,songResId);

        Log.d(SongsAdapter.TAG, "prepareMediaPlayer: ");

        initializeProgressCallback();
    }

    @Override
    public void record() {

    }

    @Override
    public String convertResourcesRawToString(int resId) {

        return null;
    }

    @Override
    public int convertResStringToResourcesRaw(String resStr) {

        int resId=0;
        switch (resStr){

            case "R.raw.des_hommes_pareils_tutti":
                resId=R.raw.des_hommes_pareils_tutti;
                break;
            case "R.raw.des_hommes_pareils_basse":
                resId=R.raw.des_hommes_pareils_basse;
                break;
            case "R.raw.des_hommes_pareils_tenor":
                resId=R.raw.des_hommes_pareils_tenor;
                break;
            case "R.raw.des_hommes_pareils_alto":
                resId=R.raw.des_hommes_pareils_alto;
                break;
            case "R.raw.des_hommes_pareils_soprano":
                resId=R.raw.des_hommes_pareils_soprano;
                break;
            case "R.raw.l_un_pour_l_autre_basse":
                resId=R.raw.l_un_pour_l_autre_basse;
                break;
            case "R.raw.l_un_pour_l_autre_tenor":
                resId=R.raw.l_un_pour_l_autre_tenor;
                break;
            case "R.raw.l_un_pour_l_autre_alto":
                resId=R.raw.l_un_pour_l_autre_alto;
                break;
            case "R.raw.l_un_pour_l_autre_soprano":
                resId=R.raw.l_un_pour_l_autre_soprano;
                break;
            case "R.raw.l_eau_tutti":
                resId=R.raw.l_eau_tutti;
                break;
            case "R.raw.le_tissu_basse":
                resId=R.raw.le_tissu_basse;
                break;
            case "R.raw.le_tissu_tenor":
                resId=R.raw.le_tissu_tenor;
                break;
            case "R.raw.le_tissu_alto":
                resId=R.raw.le_tissu_alto;
                break;
            case "R.raw.le_tissu_soprano":
                resId=R.raw.le_tissu_soprano;
                break;

        }
        return resId;
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
            mMediaPlayer.setScreenOnWhilePlaying(true);

            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);
            }
            startUpdatingCallbackWithPosition();
        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {

            mMediaPlayer.reset();
            prepareMediaPlayer(mContext,mResourceId);
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

    public int getDuration() {
        return duration;
    }
}
