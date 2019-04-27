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
import dedicace.com.data.database.Pupitre;
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
    private final Object LOCK1 = new Object();
    private static final String LOG_TAG ="coucou" ;
    private static ChoraleRepository sInstance;
    private final SongsDao mSongDao;
    private final SourceSongDao mSourceDao;
    private final ChoraleNetWorkDataSource mChoraleNetworkDataSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;
    private Thread currentThread,t2,t1;



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
    private List<SourceSong> bgSourcesToDelete = new ArrayList<>();
    private List<SourceSong> bgSourcesToDownLoad = new ArrayList<>();

    private List<Song>  mp3SongsToDelete = new ArrayList<>();
    private List<Song>  mp3SongsToDownload = new ArrayList<>();
    private List<Song> tempSongs = new ArrayList<>();
    private List<SourceSong> tempSourceSongs = new ArrayList<>();



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
            }
        });

        final LiveData<Long> majDBCloudLong= mChoraleNetworkDataSource.getMajDBCloudLong();
        majDBCloudLong.observeForever(majclouddblong -> {
            //todo vérifier l'utilité de l'égalité
            majCloudDBLong = majclouddblong;
            Log.d(LOG_TAG, "CR ChoraleRepository: majCloudLong "+ majclouddblong);

            if(majLocalDBLong<majCloudDBLong){
                Log.d(LOG_TAG, "CR ChoraleRepository: ok on lance startFetchSongService");
                startFetchSongsService();
            }else{
                //pour le cas aucune modif
                //todo à vérifier que cela marche
                if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0){
                    isFromLocal=true;
                    //todo voir comment retirer les arguments qui sont inutiles
                    getListSongs(oldSourcesSongs,oldSongs);
                    Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : données initiales "+oldSourcesSongs+" "+oldSongs);
                }else{
                    Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : pas de données initiales ");
                }
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
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                currentThread = Thread.currentThread();
                Log.d(LOG_TAG, "CR run: currentThread "+currentThread+" "+isFromLocal);

                if(!isFromLocal) {
                    Log.d(LOG_TAG, "CR run: if from local avant synchronisation db");
                    synchronisationLocalDataBase(sourceSongs,songs);
                    //chercher les Sourcesongs sur Room
                    sourceSongsAfterSync=mSourceDao.getAllSources();
                    songsAfterSync=mSongDao.getAllSongs();
                }else{
                    //todo trouver une méthode un peu moins artificielle ? cf modèle architecture.
                    Log.d(LOG_TAG, "CR run: else isFrom Local pas de synchronisation");
                    //mis pour que alerte se déclenche
                    mSourceDao.updateSourceSong(oldSourcesSongs.get(0));
                    sourceSongsAfterSync=oldSourcesSongs;
                    songsAfterSync=oldSongs;
                }

                listSongs= new ListSongs(mSongDao,mSourceDao,sourceSongsAfterSync,songsAfterSync);
                listSongs.getSongOnClouds();

                Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync sourceSongs : "+sourceSongs.size()+ " "+sourceSongsAfterSync.size()+" "+Thread.currentThread().getName());

                for (SourceSong source:sourceSongsAfterSync) {
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

        Log.d(LOG_TAG, "CR getListSongs début du current Thread pour la listSongs: ");
        t1.start();

    }


    private void synchronisationLocalDataBase(List<SourceSong> sourceSongs, List<Song> songs) {

        getLists(sourceSongs,songs);

        if(deletedSongsList!=null&&deletedSongsList.size()!=0){
            mChoraleNetworkDataSource.deleteSongsMp3OnPhone(deletedSongsList);
            mSongDao.deleteSongs(deletedSongsList);
        }


        if(deletedSourceSongsList!=null&&deletedSourceSongsList.size()!=0){
            mChoraleNetworkDataSource.deleteBgOnPhone(deletedSourceSongsList);
            mSourceDao.deleteSourceSongs(deletedSourceSongsList);
        }

        //todo non utilisé pour l'instant gérer au niveau de la db avec suppression et création après

        if(modifiedSongsList!=null&&modifiedSongsList.size()!=0){
            if(mp3SongsToDelete!=null&&mp3SongsToDelete.size()!=0) {
                mChoraleNetworkDataSource.deleteSongsMp3OnPhone(mp3SongsToDelete);
            }
            Log.d(LOG_TAG, "NDS synchronisationLocalDataBase: "+Thread.currentThread().getName());
            mChoraleNetworkDataSource.downloadMp3(modifiedSongsList);

            for (Song song:modifiedSongsList) {
                Log.d(LOG_TAG, "CR synchronisationLocalDataBase: milieu source "+song.getSourceSongTitre()+" "+song.getUpdatePhone());
                String titre = song.getSourceSongTitre();
                Pupitre pupitre = song.getPupitre();
                RecordSource recordSource = song.getRecordSource();
                Song tempSong = mSongDao.getSongsByTitrePupitreSource(titre,pupitre,recordSource);
                tempSong.setUpdatePhoneMp3(song.getUpdatePhoneMp3());
                tempSong.setUpdatePhone(song.getUpdatePhone());
                tempSong.setSongPath(song.getSongPath());
                tempSong.setPupitre(song.getPupitre());
                tempSong.setRecordSource(song.getRecordSource());
                tempSong.setSourceSongTitre(song.getSourceSongTitre());
                tempSong.setUrlCloudMp3(song.getUrlCloudMp3());
                tempSongs.add(tempSong);
               }

            int tempInt = mSongDao.updatesSongs(tempSongs);

            Log.d(LOG_TAG, "CR synchronisationLocalDataBase: nb d'update Songs "+tempInt);
        }


        if(modifiedSourceSongsList!=null&&modifiedSourceSongsList.size()!=0){

            if(bgSourcesToDelete!=null&&bgSourcesToDelete.size()!=0) {
                mChoraleNetworkDataSource.deleteBgOnPhone(bgSourcesToDelete);
            }
            if(bgSourcesToDownLoad!=null&&bgSourcesToDownLoad.size()!=0) {
                mChoraleNetworkDataSource.downloadBgImage(bgSourcesToDelete);
            }

            for (SourceSong source:modifiedSourceSongsList) {
                Log.d(LOG_TAG, "CR synchronisationLocalDataBase: milieu source "+source.getTitre()+" "+source.getUpdatePhone()+" "+source.getDuration());
                String titre = source.getTitre();
                SourceSong tempSource = mSourceDao.getSourceSongByTitre(titre);
                tempSource.setTitre(source.getTitre());
                tempSource.setUpdatePhone(source.getUpdatePhone());
                tempSource.setBackground(source.getBackground());
                tempSource.setBaseUrlOriginalSong(source.getBaseUrlOriginalSong());
                tempSource.setBgSong(source.getBgSong());
                tempSource.setDuration(source.getDuration());
                tempSource.setGroupe(source.getGroupe());
                tempSource.setUrlCloudBackground(source.getUrlCloudBackground());

                tempSourceSongs.add(tempSource);
                 }
            int tempInt = mSourceDao.upDateSourceSongs(tempSourceSongs);

            Log.d(LOG_TAG, "CR synchronisationLocalDataBase: nb d'update SS"+tempInt);
        }


        if(newSourceSongsList!=null&&newSourceSongsList.size()!=0){
            mChoraleNetworkDataSource.downloadBgImage(newSourceSongsList);
            mSourceDao.bulkInsert(newSourceSongsList);
        }


        if(newSongsList!=null&newSongsList.size()!=0){
            Log.d(LOG_TAG, "NDS1 synchronisationLocalDataBase: "+Thread.currentThread().getName());
            mChoraleNetworkDataSource.downloadMp3(newSongsList);
            mSongDao.bulkInsert(newSongsList);
        }


        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database après "+ Thread.currentThread().getName());
        //todo à voir si on change cette Méthode brute pas économique (?) on met à jour les données peu importe si elles existent ou pas.
      /*  mSourceDao.bulkInsert(sourceSongs);
        mSongDao.bulkInsert(songs);
        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database après A "+ sourceSongs.size()+" "+songs.size()+Thread.currentThread().getName());*/

      //todo à retirer dès que test passé
      List<SourceSong> ssafter = mSourceDao.getAllSources();
      List<Song> songafter =mSongDao.getAllSongs();

        Log.d(LOG_TAG, "CR synchronisationLocalDataBase: bilan des courses : Sources Songs "+ssafter.size()+" songs dans la db "+songafter.size());

        editor = sharedPreferences.edit();
        editor.putLong("majDB",majCloudDBLong);
        editor.apply();

    }

    private void getLists(List<SourceSong> sourceSongs, List<Song> songs){
        deletedSongsList(songs);
        deletedSourceSongsList(sourceSongs);
        modifiedSongsList(songs);
        modifiedSourcesSongsList(sourceSongs);
        newSourceSongsList(sourceSongs);
        newSongsList(songs);
    }

    //todo voir pour les mettre dans SongsUtilities
    private void deletedSongsList(List<Song> songs) {
        for (Song oldSong:oldSongs) {
            int i = 0;
            for (Song song: songs) {
                if((!oldSong.getSourceSongTitre().equals(song.getSourceSongTitre()))||(oldSong.getSourceSongTitre().equals(song.getSourceSongTitre())&&oldSong.getPupitre()!=song.getPupitre()&&!oldSong.getUrlCloudMp3().equals(song.getUrlCloudMp3()))){
                    i++;
                }
            }
            if(i==songs.size()){
                deletedSongsList.add(oldSong);
            }
        }

        Log.d(LOG_TAG, "CR deletedSongsList:  "+deletedSongsList.size());
        for (Song song:deletedSongsList) {
            Log.d(LOG_TAG, "CR deletedSongsList: "+song.getSourceSongTitre());
        }
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

        Log.d(LOG_TAG, "CR deletedSourceSongsList: "+ deletedSourceSongsList.size());
    }


    private void modifiedSongsList(List<Song> songs) {
        Log.d(LOG_TAG, "CR modifiedSongsList: entrée méthode");
        for (Song song:songs) {
            Log.d(LOG_TAG, "CR modifiedSongsList: entrée songs "+ song.getSourceSongTitre());
            for (Song oldSong: oldSongs) {
                Log.d(LOG_TAG, "CR modifiedSongsList: "+oldSong.getSourceSongTitre()+" "+song.getSourceSongTitre());
                if(oldSong.getSourceSongTitre().equals(song.getSourceSongTitre())&&oldSong.getPupitre()==song.getPupitre()){
                    Log.d(LOG_TAG, "CR modifiedSongsList: "+oldSong.getUpdatePhone()+" "+song.getUpdatePhone());
                    if(oldSong.getUpdatePhone().getTime()<song.getUpdatePhone().getTime()){
                        Log.d(LOG_TAG, "CR modifiedSongsList: ajout de modified SS "+song.getSourceSongTitre());
                        modifiedSongsList.add(song);
                        if(!oldSong.getUrlCloudMp3().equals(song.getUrlCloudMp3())){
                            mp3SongsToDelete.add(oldSong);
                        }
                    }
                }
            }
        }
        Log.d(LOG_TAG, "CR modifiedSongsList: "+modifiedSongsList.size());
    }

    private void modifiedSourcesSongsList(List<SourceSong> sources) {
        for (SourceSong source:sources) {
            for (SourceSong oldSource: oldSourcesSongs) {
                if(oldSource.getTitre().equals(source.getTitre())){
                    Log.d(LOG_TAG, "CR modifiedSourcesSongsList: "+oldSource.getUpdatePhone()+" "+source.getUpdatePhone());

                    if(oldSource.getUpdatePhone().getTime()<source.getUpdatePhone().getTime()){
                        Log.d(LOG_TAG, "CR modifiedSourcesSongsList: ajout de modified SS "+source.getTitre());
                        modifiedSourceSongsList.add(source);
                        if(!oldSource.getUrlCloudBackground().equals(source.getUrlCloudBackground())){
                            bgSourcesToDelete.add(oldSource);
                            bgSourcesToDownLoad.add(source);
                        }
                    }
                }
            }
        }
        //todo voir si utile delete peut être que cela écrase !

        Log.d(LOG_TAG, "CR modifiedSourcesSongsList: "+modifiedSourceSongsList.size());
    }


    private void newSongsList(List<Song> songs) {
        for (Song song:songs) {
            int i = 0;
            for (Song oldSong: oldSongs) {
                if((!oldSong.getSourceSongTitre().equals(song.getSourceSongTitre()))||(oldSong.getSourceSongTitre().equals(song.getSourceSongTitre())&&oldSong.getPupitre()!=song.getPupitre())){
                    i++;
                }
            }
            if(i==oldSongs.size()){
                newSongsList.add(song);
            }
        }


        Log.d(LOG_TAG, "CR newSongsList: "+newSongsList.size());
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
            }
        }

        Log.d(LOG_TAG, "CR newSourceSongsList: "+ newSourceSongsList.size());
    }


    public synchronized void initializeData() {
        Log.d(LOG_TAG, "CR initializeData: repository isfetchneeded "+mInitialized);

        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;
        mInitialized = true;

        if (isFetchNeeded()) {
            currentPupitreStr=getCurrentPupitreStr();
            Log.d(LOG_TAG, "CR : isFetchNeeded "+ currentPupitreStr);

            //todo remettre si chargement fonctionne pas
            //startFetchSongsService();

            String idChorale=sharedPreferences.getString("idchorale"," ");
            Log.d(LOG_TAG, "CR initializeData: idchorale "+idChorale);


            //lance la recherche d'un emise à jour et condition le lancement de startFetchData

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


        Log.d(LOG_TAG, "CR getSourceSongs après t2.join: ");

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


