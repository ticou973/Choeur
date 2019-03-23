package dedicace.com.data;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dedicace.com.AppExecutors;
import dedicace.com.data.database.ListSongs;
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
    private Thread currentThread;

    //Songs
    private List<SourceSong> sourceSongsBeforeSync = new ArrayList<>();
    private List<SourceSong> sourceSongsAfterSync = new ArrayList<>();
    private List<SourceSong> oldSourcesSongs = new ArrayList<>();
    private  List<Song> songs;
    private List<Song> oldSongs = new ArrayList<>();
    private Song firstSongPlayed;
    private List<List<RecordSource>> RecordSources=new ArrayList<>();
    private List<RecordSource> recordToPlays=new ArrayList<>();
    private List<Song> songToPlaysLive=new ArrayList<>();
    private List<Song> songToPlaysBs=new ArrayList<>();
    private List<Song> songToPlays=new ArrayList<>();
    private List<List<Song>> SongOnPhonesLive= new ArrayList<>();

    private List<List<Song>> SongOnPhonesBS= new ArrayList<>();
    private List<List<Song>> SongsOnPhones= new ArrayList<>();

    private List<List<Song>> SongOnClouds=new ArrayList<>();
    private List<Song> listSongsOnPhone= new ArrayList<>();
    private List<Song> listOrderByPupitre=new ArrayList<>();
    private List<Object> listElements = new ArrayList<>();
    private List<SourceSong> sourceSongs1;
    private String currentPupitreStr;

    private ListSongs listSongs;

    private String mCurrentAuthRole;


    private ChoraleRepository(SongsDao songsDao, SourceSongDao sourceSongDao, final ChoraleNetWorkDataSource choraleNetworkDataSource, AppExecutors executors) {
        Log.d(LOG_TAG, "CR Repository: constructor");
        mSongDao = songsDao;
        mSourceDao=sourceSongDao;
        mChoraleNetworkDataSource = choraleNetworkDataSource;
        mExecutors = executors;



        final LiveData<List<SourceSong>> networkDataSourceSongs = mChoraleNetworkDataSource.getSourceSongs();
        Log.d(LOG_TAG, "CR ChoraleRepository: LiveData mChoraleNetworkdtasource SS "+mChoraleNetworkDataSource+" "+ networkDataSourceSongs);
        networkDataSourceSongs.observeForever(sourceSongs -> {
            //todo vérifier utilité de sourceSongs1
            sourceSongs1=sourceSongs;

            Log.d(LOG_TAG, "CR Repository: observers Alerte cela bouge ! "+sourceSongs1+Thread.currentThread().getName());
            songs = choraleNetworkDataSource.getSongs();
            Log.d(LOG_TAG, "CR ChoraleRepository LiveData: songs " +songs.size());

            getListSongs(sourceSongs,songs);

        });
    }

    public synchronized static ChoraleRepository getInstance(SongsDao songsDao, SourceSongDao sourceSongDao,ChoraleNetWorkDataSource choraleNetworkDataSource, AppExecutors executors) {

        Log.d(LOG_TAG, "CR getInstance: repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleRepository(songsDao, sourceSongDao,choraleNetworkDataSource,
                        executors);

                Log.d(LOG_TAG, "CR getInstance: new repository");
            }
        }
        return sInstance;
    }

    private void getListSongs(List<SourceSong> sourceSongs, List<Song> songs) {
        listSongs= new ListSongs(mSongDao,mSourceDao,sourceSongs,songs);

        listSongs.getSongOnClouds();

        //todo voir la différence avec un autre thread
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                currentThread = Thread.currentThread();
                Log.d(LOG_TAG, "CR run: currentThread "+currentThread);

                synchronisationLocalDataBase(sourceSongs);

                //chercher les Sourcesongs sur Room
                sourceSongsAfterSync=mSourceDao.getAllSources();
                Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync sourceSongs : "+sourceSongs.size()+ " "+sourceSongsAfterSync.size()+" "+Thread.currentThread().getName());

                for (SourceSong source:sourceSongsAfterSync
                        ) {
                    Log.d(LOG_TAG, "CR run: sourcesSONG dans la data : "+source.getTitre());
                }

                listSongs.getSongOnPhoneBS(sourceSongsAfterSync);

                // getSongOnPhoneBS();

                //todo vérifier l'utilité de celui-là
                listSongs.getSongToPlaysBs();
                //getSongToPlaysBs();

                listSongs.getSongOnPhoneLive(sourceSongsAfterSync);

                listSongs.getSongToPlayLive();

                listSongs.getRecordSources();

                //getRecordSources();

                listSongs.getSongsOnPhones();
                listSongs.getSongToPlays();

                //listSongs.getSongOnClouds();

                Log.d(LOG_TAG, "CR ChoraleRepository LiveData après tout : "+Thread.currentThread().getName());
            }
        });

        t1.start();

    }

    private void getSongToPlaysBs() {
        //chercher la liste des songs To play pour le BS
        for (List<Song> songs:SongOnPhonesBS){

            if(songs==null || songs.size()==0){
                songToPlaysBs.add(null);
            }else{
                songToPlaysBs.add(songs.get(0));
            }
        }

        Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync Song to Plays BS: "+songToPlaysBs.size()+Thread.currentThread().getName());
    }


    private void synchronisationLocalDataBase(List<SourceSong> sourceSongs) {
        //todo à retirer dès que cela marche (test)
        for (SourceSong source:sourceSongs) {
            List<Song> listBs = mSongDao.getSongsOnPhone(source.getTitre(),RecordSource.BANDE_SON);
            //Log.d(LOG_TAG, "CR synchronisationLocalDataBase: test Database Room "+ listBs.size());
            if(listBs!=null){
                for (int i = 0; i <listBs.size() ; i++) {
                    Log.d(LOG_TAG, "CR getSongOnPhoneBS: listBs songs dans synchronisation "+listBs.get(i).getSourceSongTitre()+" "+listBs.get(i).getPupitre());
                }
            }
        }

        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database avant "+ Thread.currentThread().getName());
        //todo à voir si on change cette Méthode brute pas économique (?) on met à jour les données peu importe si elles existent ou pas.
        mSourceDao.bulkInsert(sourceSongs);
        mSongDao.bulkInsert(songs);
        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database après A "+ sourceSongs.size()+" "+songs.size()+Thread.currentThread().getName());
    }

    private void startFetchSongsService() {
        Log.d(LOG_TAG, "CR repo startService: début");
        mChoraleNetworkDataSource.startFetchSongsService();
        Log.d(LOG_TAG, "CR repo startService: fin ");
    }

    public synchronized void initializeData() {
        Log.d(LOG_TAG, "CR initializeData: repository isfetchneeded");

        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;
        mInitialized = true;

        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {

                //chercher les Sourcesongs sur Room
               sourceSongsBeforeSync=mSourceDao.getAllSources();
                Log.d(LOG_TAG, "CR run initialize Data SourceSongsBeforeSync: "+sourceSongsBeforeSync.size()+ " "+Thread.currentThread().getName());

                //todo à voir lorsque les données ne sont pas vides au départ.
                // getSongOnPhoneLive();

                //getSongToPlayLive();

            }
        });

        if (isFetchNeeded()) {
            currentPupitreStr=getCurrentPupitreStr();
            Log.d(LOG_TAG, "CR : isFetchNeeded "+ currentPupitreStr);
            startFetchSongsService();

            Log.d(LOG_TAG, "CR : isFetchNeeded "+ Thread.currentThread().getName());
        }
    }


    private boolean isFetchNeeded() {
        //todo à modifier éventuellement sur préférences veut - on regarder si on veut télécharger automatique ou non (à la demande) ou si la dernière date de maj à changer
        Date majCloudDB, majLocalDB;
        majCloudDB = mChoraleNetworkDataSource.getMajDateCloudDataBase();
        Log.d(LOG_TAG, "CR isFetchNeeded: condition "+majCloudDB);

        return true;
    }

    public LiveData<List<SourceSong>> getSourceSongs() {
        Log.d(LOG_TAG, "CR getSourceSongs: avant initialized data "+ Thread.currentThread().getName());
        initializeData();
        Log.d(SongsAdapter.TAG, "CR getSourceSongs: repository après iniatialize data");

        return mSourceDao.getAllSourceSongs();
    }

    public List<Song> getSongs() {
        Log.d(SongsAdapter.TAG, "CR getSongs: repository ");
        return  mSongDao.getAllSongs();
    }


    public String getCurrentPupitreStr() {
        Log.d(LOG_TAG, "CR getCurrentPupitreStr: ");

        currentPupitreStr=mChoraleNetworkDataSource.getCurrentPupitreStr();
        return currentPupitreStr;
    }

    public String getCurrentAuthRole(){
        Log.d(LOG_TAG, "getCurrentAuthRole: ");

        mCurrentAuthRole=mChoraleNetworkDataSource.getmCurrentAuthRole();
        return mCurrentAuthRole;

    }

    public List<Object> getElements() {
        return listElements;

    }


    public List<Song> getSongToPlaysA() {
        return  songToPlays;
    }

    public List<List<Song>> getSongsOnPhonesA() {
        return SongsOnPhones;
    }

    public List<List<Song>> getSongsOnCloudsA() {
        return SongOnClouds;
    }

    public List<List<RecordSource>> getRecordSourcesA(){

        return RecordSources;
    }

    public Thread getCurrentThread() {

        return currentThread;
    }

    public ListSongs getListSongs() {

        return listSongs;
    }

    public void setRecordSongInAppDb(Song song) {

        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mSongDao.insertSong(song);
            }
        });
    }
}


