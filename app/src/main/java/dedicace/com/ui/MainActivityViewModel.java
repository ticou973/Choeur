package dedicace.com.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.data.ChoraleRepository;
import dedicace.com.data.database.ListSongs;
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
    private String currentPupitreStr;
    private List<Song> songs;
    private ListSongs listSongs;

    private String mCurrentAuthRole;
    private String typeSS;

    public MainActivityViewModel(ChoraleRepository repository) {
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: avant "+choeurSourceSongs);
        mRepository = repository;
        choeurSourceSongs = mRepository.getSourceSongs();
        Log.d(SongsAdapter.TAG, "MainActivityViewModel: fin "+ choeurSourceSongs.getValue());
    }


    public LiveData<List<SourceSong>> getChoeurSourceSongs() {

        Log.d(SongsAdapter.TAG, "MAVM getChoeurSourceSongs: ");

        if(choeurSourceSongs!=null){
            Log.d(SongsAdapter.TAG, "MAVM getChoeurSourceSongs: non null "+choeurSourceSongs.getValue()+" "+choeurSourceSongs);
        }
        return choeurSourceSongs;
    }

    public Song getFirstSong(String titre, RecordSource recordSource) {

        //firstSong = mRepository.getFirstSongPlayed(titre,recordSource);
        return firstSong;
    }

    public List<List<RecordSource>> getRecordSources() {
        recordSources = mRepository.getRecordSourcesA();
        Log.d("coucou", "MAVM getRecordSources: " + recordSources);
        return recordSources;
    }

    public List<Song> getSongToPlays() {
        songToPlays=mRepository.getSongToPlaysA();
        Log.d("coucou", "MAVM getSongToPlays: " + songToPlays);
        return songToPlays;
    }

    public List<List<Song>> getSongOnPhones() {
        songOnPhones=mRepository.getSongsOnPhonesA();
        Log.d("coucou", "MAVM getSongOnPhones: " + songOnPhones);
        return songOnPhones;
    }

    public List<List<Song>> getSongOnClouds() {
        songOnClouds=mRepository.getSongsOnCloudsA();
        Log.d("coucou", "MAVM getSongsOnCloud: " + songOnClouds);
        return songOnClouds;
    }

    //todo à retirer ?
    public List<Object> getListElements() {
        listElements=mRepository.getElements();
        return listElements;
    }

    public String getCurrentPupitreStr() {
        currentPupitreStr=mRepository.getCurrentPupitreStr();
        return currentPupitreStr;
    }

    public List<Song> getAllSongs() {

        songs = mRepository.getSongs();

        return songs;
    }

    public Thread getCurrentThread() {
        Thread currentThread = mRepository.getCurrentThread();
        return currentThread;
    }

    public ListSongs getListSongs() {

        listSongs=mRepository.getListSongs();

        return listSongs;
    }

    public void setRecordSongInAppDb(Song song) {
        mRepository.setRecordSongInAppDb(song);
    }


    public String getCurrentAuthRole(){
        Log.d("coucou", "getCurrentAuthRole: ");

        mCurrentAuthRole=mRepository.getCurrentAuthRole();
        return mCurrentAuthRole;

    }

    public String getTypeSS() {
        typeSS = mRepository.getTypeSS();
        return typeSS;
    }

    public boolean getDeleted() {
        return mRepository.getDeleted();
    }

    public void downloadSingleSong(Song song) {
        mRepository.downloadSingleSong(song);
    }
}


