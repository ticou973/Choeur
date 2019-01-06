package dedicace.com.ui;

import android.content.Context;

import java.io.IOException;

/**
 * Allows {@link MainActivity} to control media playback of {@link MediaPlayerHolder}.
 */
public interface PlayerAdapter {

    void release();

    boolean isPlaying();

    void play();

    void reset() throws IOException;

    void pause();

    void initializeProgressCallback();

    void seekTo(int position);

    void prepareMediaPlayer(Context context, String resStrToPlay) throws IOException;

    String record(String songNamePupitre);

    void stopRecord();

    int getDuration();
}
