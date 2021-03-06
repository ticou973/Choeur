package dedicace.com.ui.PlaySong;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.List;

import dedicace.com.data.database.ListSongs;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;

public class MainActivityViewModel extends ViewModel {

    private ChoraleRepository mRepository;
    private LiveData<List<SourceSong>> choeurSourceSongs;
    private ListSongs listSongs;
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


    public Thread getCurrentThread() {
        return mRepository.getCurrentThread();
    }

    public ListSongs getListSongs() {

        listSongs=mRepository.getListSongs();

        return listSongs;
    }

    public void setRecordSongInAppDb(Song song) {
        mRepository.setRecordSongInAppDb(song);
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

    public void deleteSingleSong(Song song) {
        mRepository.deleteSingleSong(song);
    }

    public void downloadPupitresSongs(List<Song> songsToDownload) {
        mRepository.downloadPupitresSongs(songsToDownload);
    }

    public void deletePupitresSongs(List<Song> songsToDelete) {
        mRepository.deletePupitresSongs(songsToDelete);
    }

    public void getData(String current_user_id) {
        mRepository.getData(current_user_id);
    }

    public void getSourceSongs() {
        choeurSourceSongs=mRepository.getSourceSongs();
    }

    public Thread getThreadSaisons() {
        return mRepository.getThreadSaisons();
    }

}


