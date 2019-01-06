package dedicace.com.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.List;

import dedicace.com.data.ChoraleRepository;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;

public class MainActivityViewModel extends ViewModel {

    private ChoraleRepository mRepository;
    private LiveData<List<Song>> choeurSongs;
    private LiveData<List<SourceSong>> choeurSourceSongs;

    public MainActivityViewModel(ChoraleRepository repository) {
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: avant");
        mRepository = repository;
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: ");
        choeurSourceSongs = mRepository.getSourceSongs();
        //choeurSongs = mRepository.getSongs();
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: ");
    }

    public LiveData<List<Song>> getChoeurSongs() {
        return choeurSongs;
    }

    public LiveData<List<SourceSong>> getChoeurSourceSongs() {

        Log.d(SongsAdapter.TAG, "getChoeurSourceSongs: ");

        if(choeurSourceSongs!=null){
            Log.d(SongsAdapter.TAG, "getChoeurSourceSongs: non null");
        }

        return choeurSourceSongs;
    }
}
