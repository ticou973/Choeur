package dedicace.com.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    private List<Song> songsAfterSync = new ArrayList<>();
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
    private List<SourceSong> newSourceSongsList = new ArrayList<>();
    private List<Song> newSongsList = new ArrayList<>();
    private List<SourceSong> deletedSourceSongsList = new ArrayList<>();
    private List<Song> deletedSongsList = new ArrayList<>();
    private List<SourceSong> modifiedSourceSongsList = new ArrayList<>();
    private List<Song> modifiedSongsList = new ArrayList<>();


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

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Date majCloudDB;
    private Long majCloudDBLong;
    private Long majLocalDBLong;
    private boolean isAuto;
    private boolean isFromLocal;



    private ChoraleRepository(SongsDao songsDao, SourceSongDao sourceSongDao, final ChoraleNetWorkDataSource choraleNetworkDataSource, AppExecutors executors) {
        Log.d(LOG_TAG, "CR Repository: constructor");
        mSongDao = songsDao;
        mSourceDao=sourceSongDao;
        mChoraleNetworkDataSource = choraleNetworkDataSource;
        mExecutors = executors;

        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                oldSourcesSongs = mSourceDao.getAllSources();
                oldSongs=mSongDao.getAllSongs();

                if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0){
                    isFromLocal=true;

                    //todo voir comment retirer les arguments qui sont inutiles
                    getListSongs(oldSourcesSongs,oldSongs);
                    Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : données initiales "+oldSourcesSongs+" "+oldSongs);
                }else{
                    Log.d(LOG_TAG, "CR run: getOldSongs et SOurcesSongs : pas de données initiales ");
                }

            }
        });

        final LiveData<Long> majDBCloudLong= mChoraleNetworkDataSource.getMajDBCloudLong();
        majDBCloudLong.observeForever(majclouddblong -> {
            //todo vérifier l'utilité de l'égalité
            majCloudDBLong = majclouddblong;
            Log.d(LOG_TAG, "CR ChoraleRepository: majCloudLong "+majclouddblong);

            if(majLocalDBLong<majCloudDBLong){
                Log.d(LOG_TAG, "CR ChoraleRepository: ok on lance startFetchSongService");
                startFetchSongsService();
            }else{
                Log.d(LOG_TAG, "CR ChoraleRepository: Stop startFectch pas lancé !");
            }
        });


        final LiveData<List<SourceSong>> networkDataSourceSongs = mChoraleNetworkDataSource.getSourceSongs();
        Log.d(LOG_TAG, "CR ChoraleRepository: LiveData mChoraleNetworkdtasource SS "+mChoraleNetworkDataSource+" "+ networkDataSourceSongs);
        networkDataSourceSongs.observeForever(sourceSongs -> {
            //todo vérifier utilité de sourceSongs1
            sourceSongs1=sourceSongs;

            Log.d(LOG_TAG, "CR Repository: observers Alerte cela bouge ! "+sourceSongs1+Thread.currentThread().getName());
            songs = choraleNetworkDataSource.getSongs();
            Log.d(LOG_TAG, "CR ChoraleRepository LiveData: songs " +songs.size());

            isFromLocal=false;
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

        //todo voir la différence avec un autre thread
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                currentThread = Thread.currentThread();
                Log.d(LOG_TAG, "CR run: currentThread "+currentThread);

                if(!isFromLocal) {
                    synchronisationLocalDataBase(sourceSongs,songs);
                }

                //chercher les Sourcesongs sur Room
                sourceSongsAfterSync=mSourceDao.getAllSources();
                songsAfterSync=mSongDao.getAllSongs();

                listSongs= new ListSongs(mSongDao,mSourceDao,sourceSongsAfterSync,songsAfterSync);

                listSongs.getSongOnClouds();

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


    private void synchronisationLocalDataBase(List<SourceSong> sourceSongs, List<Song> songs) {
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

        newSourceSongsList(sourceSongs);
        newSongsList();
        modifiedSourcesSongsList(sourceSongs);
        modifiedSongsList();
        deletedSourceSongsList(sourceSongs);
        deletedSongsList();

        if(newSourceSongsList!=null&&newSourceSongsList.size()!=0){
            mSourceDao.bulkInsert(newSourceSongsList);
        }
        if(newSongsList!=null&newSongsList.size()!=0){
            mSongDao.bulkInsert(newSongsList);
        }
        if(deletedSourceSongsList!=null&&deletedSourceSongsList.size()!=0){
            mSourceDao.deleteSourceSongs(deletedSourceSongsList);
        }
        if(deletedSongsList!=null&&deletedSongsList.size()!=0){
            mSongDao.deleteSongs(deletedSongsList);
        }
        if(modifiedSourceSongsList!=null&&modifiedSourceSongsList.size()!=0){
            mSourceDao.upDateSourceSongs(modifiedSourceSongsList);
        }
        if(modifiedSongsList!=null&&modifiedSongsList.size()!=0){
            mSongDao.updateSongs(modifiedSongsList);
        }


        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database avant "+ Thread.currentThread().getName());
        //todo à voir si on change cette Méthode brute pas économique (?) on met à jour les données peu importe si elles existent ou pas.
      /*  mSourceDao.bulkInsert(sourceSongs);
        mSongDao.bulkInsert(songs);
        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database après A "+ sourceSongs.size()+" "+songs.size()+Thread.currentThread().getName());*/

        editor = sharedPreferences.edit();
        editor.putLong("majDB",majCloudDBLong);
        editor.apply();

    }

    //todo voir pour les mettre dans SongsUtilities
    private void deletedSongsList() {
        for (Song oldSong:oldSongs) {
            int i = 0;
            for (Song song: songs) {
                if(!oldSong.getSourceSongTitre().equals(song.getSourceSongTitre())){
                    i++;
                }
            }
            if(i==songs.size()){
                deletedSongsList.add(oldSong);
            }
        }

        Log.d(LOG_TAG, "CR deletedSongsList:  "+deletedSongsList);
    }

    private void deletedSourceSongsList(List<SourceSong> sources) {
        for (SourceSong oldSource:oldSourcesSongs) {
            int i = 0;
            for (SourceSong source: sources) {
                if(!oldSource.getTitre().equals(source.getTitre())){
                    i++;
                }
            }
            if(i==sources.size()){
                deletedSourceSongsList.add(oldSource);
            }
        }
        Log.d(LOG_TAG, "CR deletedSourceSongsList: "+ deletedSourceSongsList);
    }

    private void modifiedSongsList() {
        for (Song song:songs) {

            for (Song oldSong: oldSongs) {
                if(oldSong.getSourceSongTitre().equals(song.getSourceSongTitre())){
                    if(oldSong.getUpdatePhone().getTime()<song.getUpdatePhone().getTime()){
                        modifiedSongsList.add(song);
                        if(!oldSong.getUrlCloudMp3().equals(song.getUrlCloudMp3())){

                        }
                    }
                }
            }
        }

        Log.d(LOG_TAG, "CR modifiedSongsList: "+modifiedSongsList);
    }

    private void modifiedSourcesSongsList(List<SourceSong> sources) {
        for (SourceSong source:sources) {
            for (SourceSong oldSource: oldSourcesSongs) {
                if(oldSource.getTitre().equals(source.getTitre())){
                    if(oldSource.getUpdatePhone().getTime()<source.getUpdatePhone().getTime()){
                        modifiedSourceSongsList.add(source);
                        if(!oldSource.getUrlCloudBackground().equals(source.getUrlCloudBackground())){
                            //deleteOldBg();
                           // uploadNewBg();
                        }
                    }
                }
            }
        }

        Log.d(LOG_TAG, "CR modifiedSourcesSongsList: "+modifiedSourceSongsList);
    }

    private void uploadNewBg() {

    }

    private void deleteOldBg() {

    }

    private void newSongsList() {
        for (Song song:songs) {
            int i = 0;
            for (Song oldSong: oldSongs) {
                if(!oldSong.getSourceSongTitre().equals(song.getSourceSongTitre())){
                    i++;
                }
            }
            if(i==oldSongs.size()){
                newSongsList.add(song);
            }
        }
        Log.d(LOG_TAG, "CR newSongsList: "+newSongsList);
    }

    private void newSourceSongsList(List<SourceSong> sources) {
        for (SourceSong source:sources) {
            int i = 0;
            for (SourceSong oldSource: oldSourcesSongs) {
                if(!oldSource.getTitre().equals(source.getTitre())){
                   i++;
                }
            }
            if(i==oldSourcesSongs.size()){
                newSourceSongsList.add(source);
               // uploadNewBg();
            }
        }
        Log.d(LOG_TAG, "CR newSourceSongsList: "+ newSourceSongsList.size());
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

                //todo vérifier l'utilité du sourceBeforSync ?
                //chercher les Sourcesongs sur Room
               sourceSongsBeforeSync=mSourceDao.getAllSources();
                Log.d(LOG_TAG, "CR run initialize Data SourceSongsBeforeSync: "+sourceSongsBeforeSync.size()+ " "+Thread.currentThread().getName());

            }
        });

        if (isFetchNeeded()) {
            currentPupitreStr=getCurrentPupitreStr();
            Log.d(LOG_TAG, "CR : isFetchNeeded "+ currentPupitreStr);

            //todo remettre si chargement fonctionne pas
            //startFetchSongsService();

            String idChorale=sharedPreferences.getString("idChorale"," ");
            Log.d(LOG_TAG, "initializeData: idchorale "+idChorale);

            LoadMajCloudDB();

            Log.d(LOG_TAG, "CR : isFetchNeeded "+ Thread.currentThread().getName());
        }else{
            Log.d(LOG_TAG, "CR initializeData: inutile les données n'ont pas changées ");
        }
    }

    private void LoadMajCloudDB() {
        mChoraleNetworkDataSource.getMajDateCloudDataBase();
    }

    private void getMajDateLocalDataBase() {

        majLocalDBLong =sharedPreferences.getLong("majDB",0);
        Log.d(LOG_TAG, "CR : datelong : local "+new Date(majLocalDBLong)+" cloud "+majCloudDB+ " comparaison Long-local/Cloud "+ majLocalDBLong+" "+majCloudDBLong);

    }

    private void startFetchSongsService() {
        Log.d(LOG_TAG, "CR repo startService: début");
        mChoraleNetworkDataSource.startFetchSongsService();
        Log.d(LOG_TAG, "CR repo startService: fin ");
    }


    private boolean isFetchNeeded() {
        //todo à modifier éventuellement sur préférences veut - on regarder si on veut télécharger automatique ou non (à la demande) ou si la dernière date de maj à changer


        Context context = mChoraleNetworkDataSource.getContext();

        //todo à modifier dans le listener pour appliquer les changements
        sharedPreferences =PreferenceManager.getDefaultSharedPreferences(context);
        isAuto = sharedPreferences.getBoolean("maj_auto",true);

        getMajDateLocalDataBase();

        Log.d(LOG_TAG, "CR isFetchNeeded: condition "+isAuto);

        return isAuto;
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


