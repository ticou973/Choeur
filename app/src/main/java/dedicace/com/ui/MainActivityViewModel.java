package dedicace.com.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.data.ChoraleRepository;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;

public class MainActivityViewModel extends ViewModel {

    private ChoraleRepository mRepository;
    private LiveData<List<SourceSong>> choeurSourceSongs;
    private Song firstSong;
    private List<List<RecordSource>> recordSources=new ArrayList<>();
    private List<Song> songToPlays=new ArrayList<>();
    private List<List<Song>> songOnPhones= new ArrayList<>();
    private List<List<Song>> songOnClouds= new ArrayList<>();
    private List<Object> listElements = new ArrayList<>();

    public MainActivityViewModel(ChoraleRepository repository) {
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: avant");
        mRepository = repository;
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: ");
        choeurSourceSongs = mRepository.getSourceSongs();
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: ");
    }


    public LiveData<List<SourceSong>> getChoeurSourceSongs() {

        Log.d(SongsAdapter.TAG, "getChoeurSourceSongs: ");

        if(choeurSourceSongs!=null){
            Log.d(SongsAdapter.TAG, "getChoeurSourceSongs: non null");
        }
        return choeurSourceSongs;
    }

    public Song getFirstSong(String titre, RecordSource recordSource) {

        //firstSong = mRepository.getFirstSongPlayed(titre,recordSource);
        return firstSong;
    }

    public List<List<RecordSource>> getRecordSources() {
        recordSources = mRepository.getRecordSources();
        return recordSources;
    }

    public List<Song> getSongToPlays() {
        songToPlays=mRepository.getSongToPlays();
        return songToPlays;
    }

    public List<List<Song>> getSongOnPhones() {
        songOnPhones=mRepository.getSongsOnPhones();
        return songOnPhones;
    }

    public List<List<Song>> getSongOnClouds() {
        songOnClouds=mRepository.getSongsOnClouds();
        return songOnClouds;
    }

    public List<Object> getListElements() {
        listElements=mRepository.getListElements();
        return listElements;
    }
}
