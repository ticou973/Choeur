package dedicace.com.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import dedicace.com.AppExecutors;
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

    private ChoraleRepository(SongsDao songsDao, SourceSongDao sourceSongDao,
                              final ChoraleNetWorkDataSource choraleNetworkDataSource,
                              AppExecutors executors) {
        Log.d("coucou", "Repository: constructor");
        mSongDao = songsDao;
        mSourceDao=sourceSongDao;
        mChoraleNetworkDataSource = choraleNetworkDataSource;
        mExecutors = executors;

        /*LiveData<List<Song>> networkDataSongs = mChoraleNetworkDataSource.getCurrentSongs();
        networkDataSongs.observeForever(new Observer<List<Song>>() {
            @Override
            public void onChanged(@Nullable final List<Song> songs) {
                mExecutors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // Insert our new weather data into Sunshine's database

                        Log.d(SongsAdapter.TAG, "run: Songs dans la database");
                        mSongDao.bulkInsert(songs);

                        Log.d(SongsAdapter.TAG, "run :"+mSongDao.getLastSong().getSourceSongTitre());
                    }
                });
            }
        });*/

        LiveData<List<SourceSong>> networkDataSourceSongs = mChoraleNetworkDataSource.getSourceSongs();
        networkDataSourceSongs.observeForever(new Observer<List<SourceSong>>() {
            @Override
            public void onChanged(@Nullable final List<SourceSong> sourceSongs) {
                Log.d("coucou", "Repository: observers ");
                mExecutors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database avant");
                        mSourceDao.bulkInsert(sourceSongs);
                        Log.d(SongsAdapter.TAG, "run: sourceSongs dans la database après");
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

    public LiveData<List<Song>> getSongs() {
        //initializeData();
        Log.d(SongsAdapter.TAG, "getSongs: repository ");
        return  mSongDao.getAllSongs();
    }
}


