package dedicace.com;

import android.content.Context;

/**
 * Allows {@link MainActivity} to control media playback of {@link MediaPlayerHolder}.
 */
public interface PlayerAdapter {

    void release();

    boolean isPlaying();

    void play(Context context, int songResId);

    void reset();

    void pause();

    void initializeProgressCallback();

    void seekTo(int position);

    void prepareMediaPlayer();
}
