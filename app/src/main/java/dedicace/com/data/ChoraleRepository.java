package dedicace.com.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.AppExecutors;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SongsDao;
import dedicace.com.data.database.SourceSong;
import dedicace.com.data.database.SourceSongDao;
import dedicace.com.data.networkdatabase.ChoraleNetWorkDataSource;
import dedicace.com.ui.SongsAdapter;

public class ChoraleRepository {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static final String LOG_TAG ="coucou" ;
    private static ChoraleRepository sInstance;
    private final SongsDao mSongDao;
    private final SourceSongDao mSourceDao;
    private final ChoraleNetWorkDataSource mChoraleNetworkDataSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;

    private LiveData<List<SourceSong>> sourceSongs;
    private  List<Song> songs;
    private Song firstSongPlayed;
    private List<List<RecordSource>> RecordSources=new ArrayList<>();
    private List<RecordSource> recordToPlays=new ArrayList<>();
    private List<Song> songToPlays=new ArrayList<>();
    private List<List<Song>> SongOnPhones= new ArrayList<>();
    private List<List<Song>> SongOnClouds=new ArrayList<>();

    private ChoraleRepository(SongsDao songsDao, SourceSongDao sourceSongDao,
                              final ChoraleNetWorkDataSource choraleNetworkDataSource,
                              AppExecutors executors) {
        Log.d("coucou", "Repository: constructor");
        mSongDao = songsDao;
        mSourceDao=sourceSongDao;
        mChoraleNetworkDataSource = choraleNetworkDataSource;
        mExecutors = executors;

        final LiveData<List<SourceSong>> networkDataSourceSongs = mChoraleNetworkDataSource.getSourceSongs();
        networkDataSourceSongs.observeForever(new Observer<List<SourceSong>>() {
            @Override
            public void onChanged(@Nullable final List<SourceSong> sourceSongs) {
                Log.d("coucou", "Repository: observers ");
                songs = choraleNetworkDataSource.getSongs();
                mExecutors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database avant");
                        mSourceDao.bulkInsert(sourceSongs);
                        mSongDao.bulkInsert(songs);
                        Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après A"+sourceSongs.size()+" "+songs.size());

                        if(sourceSongs!=null) {

                            for (SourceSong sourceSong : sourceSongs) {
                                String titre = sourceSong.getTitre();
                                RecordSources.add(getRecordSources(titre));
                            }

                            Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après B "+RecordSources.size());

                            for (List<RecordSource> recordSources: RecordSources) {
                                recordToPlays.add(getRecordSource(recordSources));
                            }

                            Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après C "+recordToPlays.size());

                            for (SourceSong sourceSong:sourceSongs) {
                                String titre = sourceSong.getTitre();
                                int indexSourceSong = sourceSongs.indexOf(sourceSong);

                                RecordSource recordToPlay = recordToPlays.get(indexSourceSong);
                                Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après D "+recordToPlay);
                                songToPlays.add(getFirstSongPlayed(titre,recordToPlay));
                                Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après E "+songToPlays.size());

                                SongOnPhones.add(getSongsOnPhone(titre,recordToPlay));

                                Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après F "+SongOnPhones.size());
                                SongOnClouds.add(getSongsOnCloud(titre,recordToPlay));

                                Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après G "+SongOnClouds.size());
                            }
                        }
                    }
                });
            }
        });
    }



    public synchronized static ChoraleRepository getInstance(
            SongsDao songsDao, SourceSongDao sourceSongDao,ChoraleNetWorkDataSource choraleNetworkDataSource,
            AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the repository");
        Log.d("coucou", "getInstance: repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleRepository(songsDao, sourceSongDao,choraleNetworkDataSource,
                        executors);
                Log.d(LOG_TAG, "Made new repository");
                Log.d("coucou", "getInstance: new repository");

            }
        }
        return sInstance;
    }

    private void startFetchSongsService() {
        Log.d("coucou", "repo startService: début");
        mChoraleNetworkDataSource.startFetchSongsService();
        Log.d("coucou", "repo startService: fin ");
    }

    public synchronized void initializeData() {
        Log.d("coucou", "initializeData: repository isfetchneeded");

        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;
        mInitialized = true;

        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (isFetchNeeded()) {
                    startFetchSongsService();
                }
            }
        });
    }

    private boolean isFetchNeeded() {
        Log.d("coucou", "isFetchNeeded: ");
        return true;
    }

    public LiveData<List<SourceSong>> getSourceSongs() {
        Log.d("coucou", "getSourceSongs: avant initialized data");
        initializeData();
        Log.d(SongsAdapter.TAG, "getSourceSongs: repository ");
        sourceSongs = mSourceDao.getAllSourceSongs();
        return sourceSongs;
    }

    public List<Song> getSongs() {
        Log.d(SongsAdapter.TAG, "getSongs: repository ");
        return  mSongDao.getAllSongs();
    }



    private RecordSource getRecordSource(List<RecordSource> recordSources) {
        RecordSource source =RecordSource.NA;

        if(recordSources.size()==2){
            source=RecordSource.BANDE_SON;
        }else if(recordSources.size()==1){
            if(recordSources.get(0)==RecordSource.BANDE_SON){
                source=RecordSource.BANDE_SON;
            }else if(recordSources.get(0)==RecordSource.LIVE){
                source=RecordSource.LIVE;
            }
        }else if(recordSources.size()==0){
            source = RecordSource.NA;
        }

        return source;
    }

    public List<RecordSource> getRecordSources(String titre) {

        List<RecordSource> sources= new ArrayList<>();

        List<Song> listBS;
        List<Song> listLIVE;

        listBS = mSongDao.getSongsBySourceTitre(RecordSource.BANDE_SON,titre);
        listLIVE=mSongDao.getSongsBySourceTitre(RecordSource.LIVE,titre);

        if(listBS.size()!=0&&listLIVE.size()!=0){
            sources.add(RecordSource.BANDE_SON);
            sources.add(RecordSource.LIVE);
        }else if(listBS.size()==0&&listLIVE.size()!=0){
            sources.add(RecordSource.LIVE);
        }else if(listBS.size()!=0&&listLIVE.size()==0){
            sources.add(RecordSource.BANDE_SON);
        }else{
            sources.add(RecordSource.NA);
        }
        return sources;
    }

    public List<List<RecordSource>> getRecordSources() {
        return RecordSources;
    }

    public Song getFirstSongPlayed(String titre, RecordSource recordSource) {

        List<Song> listOrderByPupitre;

        listOrderByPupitre= mSongDao.getSongOrderedByPupitre(titre,recordSource);

        if(recordSource!=RecordSource.NA) {
            firstSongPlayed = listOrderByPupitre.get(0);
        }else{
            firstSongPlayed = null;
        }

        return firstSongPlayed;
    }

    public List<Song> getSongToPlays() {
        return songToPlays;
    }

    public List<List<Song>> getSongsOnPhones(){

       return SongOnPhones;
    }

    public List<Song> getSongsOnPhone(String titre, RecordSource source){

        List<Song> listSongsOnPhone;

        listSongsOnPhone=mSongDao.getSongsOnPhone(titre,source);

        return listSongsOnPhone;

    }


    public List<List<Song>> getSongsOnClouds(){

        return SongOnClouds;
    }

    public List<Song> getSongsOnCloud(String titre, RecordSource source){

        List<Song> listSongsOnCloud;

        listSongsOnCloud=mSongDao.getSongsOnCloud(titre,source);

        return listSongsOnCloud;

    }


}


