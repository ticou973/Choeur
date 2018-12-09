package dedicace.com;

import android.content.Context;

/**
 * Allows {@link MainActivity} to control media playback of {@link MediaPlayerHolder}.
 */
public interface PlayerAdapter {

    void release();

    boolean isPlaying();

    void play();

    void reset();

    void pause();

    void initializeProgressCallback();

    void seekTo(int position);

    void prepareMediaPlayer(Context context, int songResId);

    void record();

    String convertResourcesRawToString(int resId);

    int convertResStringToResourcesRaw(String resStr);

    int getDuration();
}
