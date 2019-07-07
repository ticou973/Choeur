package dedicace.com.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedicace.com.AppExecutors;
import dedicace.com.R;
import dedicace.com.data.database.ListSongs;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Saison;
import dedicace.com.data.database.SaisonDao;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SongsDao;
import dedicace.com.data.database.SourceSong;
import dedicace.com.data.database.SourceSongDao;
import dedicace.com.data.database.Spectacle;
import dedicace.com.data.database.SpectacleDao;
import dedicace.com.data.networkdatabase.ChoraleNetWorkDataSource;
import dedicace.com.ui.SongsAdapter;

public class ChoraleRepository {
    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static final String LOG_TAG ="coucou" ;
    private static ChoraleRepository sInstance;
    private final SongsDao mSongDao;
    private final SourceSongDao mSourceDao;
    private final SaisonDao mSaisonDao;
    private final  SpectacleDao mSpectacleDao;
    private final ChoraleNetWorkDataSource mChoraleNetworkDataSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;
    private Thread currentThread,t2,t1,t3,t4,t5,t6, threadSaisons;
    private Context context;

    //Songs
    private List<SourceSong> sourceSongsAfterSync = new ArrayList<>();
    private List<Song> songsAfterSync = new ArrayList<>();
    private List<SourceSong> oldSourcesSongs = new ArrayList<>();
    private  List<Song> songs;
    private List<Song> oldSongs = new ArrayList<>();
    private List<SourceSong> newSourceSongsList = new ArrayList<>();
    private List<Song> newSongsList = new ArrayList<>();
    private List<SourceSong> deletedSourceSongsList = new ArrayList<>();
    private List<Song> deletedSongsList = new ArrayList<>();
    private List<Song> deletedMp3SongsList = new ArrayList<>();
    private List<SourceSong> deletedBgSongsList = new ArrayList<>();
    private List<SourceSong> modifiedSourceSongsList = new ArrayList<>();
    private List<Song> modifiedSongsList = new ArrayList<>();
    private List<SourceSong> bgSourcesToDelete = new ArrayList<>();
    private List<SourceSong> bgSourcesToDownLoad = new ArrayList<>();

    private List<Spectacle> spectacles = new ArrayList<>();
    private List<Saison> saisons = new ArrayList<>();

    private List<Song>  mp3SongsToDelete = new ArrayList<>();
    private List<Song>  mp3SongsToDownload = new ArrayList<>();
    private List<Song> tempSongs = new ArrayList<>();
    private List<SourceSong> tempSourceSongs = new ArrayList<>();
    private List<Song> totalMp3Todowload = new ArrayList<>();
    private List<SourceSong> totalBgTodowload = new ArrayList<SourceSong>();
    private List<Song> totalMp3ToDelete = new ArrayList<>();
    private List<SourceSong> totalBgToDelete = new ArrayList<SourceSong>();
    private Map<String,String> titres = new HashMap<>();
    private Map<String,String> titress = new HashMap<>();
    private Song songToDownload;
    private List<Song> songsToDownload, songsToDelete;
    private List<SourceSong> sourceSongs1;
    private String currentPupitreStr;


    private ListSongs listSongs;

    private String typeSS;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Date majCloudDB;
    private Long majCloudDBLong;
    private Long majLocalDBLong;
    private boolean isAuto;
    private boolean isFromLocal;
    private Song songToDelete;


    private ChoraleRepository(SongsDao songsDao, SourceSongDao sourceSongDao, SaisonDao saisonDao, SpectacleDao spectacleDao, final ChoraleNetWorkDataSource choraleNetworkDataSource, AppExecutors executors) {
        Log.d(LOG_TAG, "CR Repository: constructor");
        mSongDao = songsDao;
        mSourceDao=sourceSongDao;
        mSaisonDao=saisonDao;
        mSpectacleDao=spectacleDao;
        mChoraleNetworkDataSource = choraleNetworkDataSource;
        mExecutors = executors;

        //todo à modifier éventuellement sur préférences veut - on regarder si on veut télécharger automatique ou non (à la demande) ou si la dernière date de maj à changer
        context = mChoraleNetworkDataSource.getContext();
        //todo à modifier dans le listener pour appliquer les changements
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        t5 = new Thread(() -> {
            t5 = Thread.currentThread();
            oldSourcesSongs = mSourceDao.getAllSources();
            oldSongs=mSongDao.getAllSongs();
            Log.d(LOG_TAG, "CR run:  old SS et song "+oldSourcesSongs.size()+" songs "+oldSongs.size());
        });
        t5.start();

        final LiveData<List<Saison>> majSaisonCloud = mChoraleNetworkDataSource.getSaisonsCloud();
        Log.d(LOG_TAG, "CR ChoraleRepository: getSaisonsCloud "+majSaisonCloud);

        majSaisonCloud.observeForever(saisons -> {
            this.saisons=saisons;
            spectacles = mChoraleNetworkDataSource.getSpectacles();
            majRoomDb();

            String currentSaisonId = null;

            for(Saison saison:saisons){

                if(saison.isCurrentSaison()){
                    currentSaisonId=saison.getIdsaisonCloud();
                }
            }

            editor = sharedPreferences.edit();
            editor.putString("currentSaison",currentSaisonId);
            editor.apply();
        });


        final LiveData<Long> majDBCloudLong= mChoraleNetworkDataSource.getMajDBCloudLong();
        Log.d(LOG_TAG, "CR ChoraleRepository: getmajDBCCloud "+majDBCloudLong);

        majDBCloudLong.observeForever(majclouddblong -> {
            //todo vérifier l'utilité de l'égalité
            majCloudDBLong = majclouddblong;
            Log.d(LOG_TAG, "CR AlerteMaj ChoraleRepository: majCloudLong "+ majclouddblong);

            if(majLocalDBLong<majCloudDBLong){
                if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0){
                    typeSS="oldSS";
                    Log.d(LOG_TAG, "CR ChoraleRepository modification : ok on lance startFetchSongService");
                    isFromLocal=true;
                    //todo voir comment retirer les arguments qui sont inutiles
                    DoSynchronization(oldSourcesSongs,oldSongs);
                    Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs 2 modifications: données initiales "+oldSourcesSongs+" "+oldSongs);
                }else{
                    typeSS="newSS";
                    Log.d(LOG_TAG, "CR ChoraleRepository new SS : ok on lance startFetchSongService");
                }
                Log.d(LOG_TAG, "CR ChoraleRepository: ok on lance startFetchSongService "+t2);

                if(typeSS.equals("oldSS")) {
                    try {
                        t2.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "CR join: interrupted exception");
                    }
                }
                startFetchSongsService();
            }else{
                //pour le cas aucune modif
                if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0){
                    isFromLocal=true;
                    typeSS="oldSS";
                    //todo voir comment retirer les arguments qui sont inutiles
                    DoSynchronization(oldSourcesSongs,oldSongs);
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
            if(sourceSongs1!=null&&sourceSongs1.size()!=0){
                Log.d(LOG_TAG, "NDS ChoraleRepository: "+sourceSongs1.size()+sourceSongs.size());
            }
            sourceSongs1=sourceSongs;

            Log.d(LOG_TAG, "CR Repository: observers Alerte cela bouge ! "+" "+sourceSongs1.size()+" "+sourceSongs1+Thread.currentThread().getName());
            songs = choraleNetworkDataSource.getSongs();
            Log.d(LOG_TAG, "CR ChoraleRepository LiveData: songs " +songs.size());

            isFromLocal=false;
            DoSynchronization(sourceSongs,songs);

        });

        final LiveData<String> downloads =mChoraleNetworkDataSource.getDownloads();
        downloads.observeForever(message -> {
            if(message.equals("Done")){
                Log.d(LOG_TAG, "CR ChoraleRepository: observer Done pour downloads");
                DoWorkInRoomAndLists();
            }else if(message.equals("SingleDownload")){
                Log.d(LOG_TAG, "CR ChoraleRepository: single download ");
                DoWorkInRoomBis(songToDownload);
            }else if(message.equals("MultipleDownloads")){
                Log.d(LOG_TAG, "CR ChoraleRepository: multiple download ");
                DoWorkInRoomBis(songsToDownload);
            }else if(message.equals("deleteSingle")) {
                Log.d(LOG_TAG, "CR ChoraleRepository: single delete ");
                DoWorkInRoomBis(songToDelete);
            }else if(message.equals("deleteMultiple")) {
                Log.d(LOG_TAG, "CR ChoraleRepository: multiple delete ");
                DoWorkInRoomBis(songsToDelete);
            }else {
                Log.d(LOG_TAG, "CR ChoraleRepository: il faut encore attendre... ");
            }


        });
    }

    private void majRoomDb() {
        Log.d(LOG_TAG, "CR majRoomDb: ");
        threadSaisons = new Thread(() -> {
            mSaisonDao.bulkInsert(saisons);
            mSpectacleDao.bulkInsert(spectacles);


            //todo supprimer dès que test passé
            List<Saison> tempSaisons = mSaisonDao.getAllSaisons();
            List<Spectacle> tempSpectacles = mSpectacleDao.getAllSpectacles();

            for(Saison saison : tempSaisons) {
                Log.d(LOG_TAG, "CR majRoomDb: saisons"+ saison.getIdsaisonCloud()+" "+ saison.getSaisonName()+ " "+ saison.getIdSpectacles()+" "+saison.getUpdatePhone()+ " "+ saison.isCurrentSaison());
            }

            for(Spectacle spectacle : tempSpectacles){
                Log.d(LOG_TAG, "CR majRoomDb: spectacles "+ spectacle.getIdSpectacleCloud()+" "+spectacle.getSpectacleName()+" "+ spectacle.getIdTitresSongs()+ " "+spectacle.getSpectacleLieux()+ " "+ spectacle.getSpectacleDates()+ " "+ spectacle.getUpdatePhone());
            }

        });
        threadSaisons.start();
    }

    //todo voir les factorisations possibles
    private void DoWorkInRoomBis(List<Song> songsToDownload) {
        Log.d(LOG_TAG, "CR DoWorkInRoomBis: multiple ");
        t4 = new Thread(() -> {
            currentThread = Thread.currentThread();
            syncMultipleSongDb(songsToDownload);
            getListSongsA();
        });
        t4.start();
    }



    private void DoWorkInRoomBis(Song song) {
        Log.d(LOG_TAG, "CR DoWorkInRoomBis: single");
        t3 = new Thread(() -> {
            currentThread = Thread.currentThread();
            syncSingleSongDb(song);
            getListSongsA();
        });
        t3.start();
    }

    private void DoWorkInRoomAndLists() {

        t1 = new Thread(() -> {
            currentThread = Thread.currentThread();
            DoWorkInRoom();
            getListSongsA();
        });

        t1.start();
    }

    public synchronized static ChoraleRepository getInstance(SongsDao songsDao, SourceSongDao sourceSongDao, SaisonDao saisonDao, SpectacleDao spectacleDao, ChoraleNetWorkDataSource choraleNetworkDataSource, AppExecutors executors) {

        Log.d(LOG_TAG, "CR getInstance: repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleRepository(songsDao, sourceSongDao, saisonDao,spectacleDao,choraleNetworkDataSource,
                        executors);

                Log.d(LOG_TAG, "CR getInstance: new repository");
            }
        }
        return sInstance;
    }

    private void DoSynchronization(List<SourceSong> sourceSongs, List<Song> songs) {
        t2 = new Thread(() -> {

            currentThread = Thread.currentThread();
            Log.d(LOG_TAG, "CR run: currentThread "+currentThread+" "+isFromLocal);
            if(!isFromLocal) {
                if(typeSS.equals("oldSS")){
                    typeSS="modificationSS";
                }
                Log.d(LOG_TAG, "CR run: if from local avant synchronisation db "+typeSS);
                synchronisationLocalDataBase(sourceSongs,songs);

            }else{
                //todo trouver une méthode un peu moins artificielle ? cf modèle architecture.
                Log.d(LOG_TAG, "CR run: else isFrom Local pas de synchronisation");
                //mis pour que alerte se déclenche
                mSourceDao.updateSourceSong(oldSourcesSongs.get(0));
                sourceSongsAfterSync=oldSourcesSongs;
                songsAfterSync=oldSongs;
                getListSongsA();
            }
            Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync sourceSongs : "+sourceSongs.size()+ " "+sourceSongsAfterSync.size()+" "+Thread.currentThread().getName());

            for (SourceSong source:sourceSongsAfterSync) {
                Log.d(LOG_TAG, "CR run: sourcesSONG dans la data : "+source.getTitre());
            }
        });

        Log.d(LOG_TAG, "CR DoSynchronization: juste avant T2 start");
        t2.start();
    }

    private void getListSongsA() {
        listSongs= new ListSongs(mSongDao,mSourceDao,sourceSongsAfterSync,songsAfterSync);
        listSongs.getSongOnClouds();
        listSongs.getSongOnPhoneBS(sourceSongsAfterSync);

        // getSongOnPhoneBS();

        //todo vérifier l'utilité de celui-là
        listSongs.getSongToPlaysBs();

        listSongs.getSongOnPhoneLive(sourceSongsAfterSync);

        listSongs.getSongToPlayLive();

        listSongs.getRecordSources();

        listSongs.getSongsOnPhones();
        listSongs.getSongToPlays();

        Log.d(LOG_TAG, "CR ChoraleRepository LiveData après tout : "+Thread.currentThread().getName()+" "+currentThread);
    }

    public void downloadSingleSong(Song song){
        typeSS="newSongOnPhone";
        songToDownload=song;
        mChoraleNetworkDataSource.downloadSingleMp3(song);
    }

    public void downloadPupitresSongs(List<Song> songsToDownload) {
        typeSS="newSongsOnPhone";
        this.songsToDownload=songsToDownload;
        mChoraleNetworkDataSource.downloadMp3PupitresSongs(songsToDownload);
    }

    public void deleteSingleSong(Song song){
        typeSS="deleteSingleSongOnPhone";
        songToDelete=song;
        mChoraleNetworkDataSource.deleteSingleSong(song);
    }

    public void deletePupitresSongs(List<Song> songsToDelete) {
        typeSS="deleteMultipleSongOnPhone";
        this.songsToDelete=songsToDelete;
        mChoraleNetworkDataSource.deleteMultipleSong(songsToDelete);
    }

    //todo faire les factorisations
    //todo voir comment supprimer les songs dans la db qaund nécessaire pour éviter des chansons que l'on ne reprendra plus
    private void syncMultipleSongDb(List<Song> songsToDownload) {

        List<Song> tempSongs = new ArrayList<>();
        List<Song> tempSongsdelete = new ArrayList<>();
        List<SourceSong> tempSources = new ArrayList<>();
        Log.d(LOG_TAG, "CR DoWorkInRoom: sync multiple Song");

        for (Song song:songsToDownload) {
            Log.d(LOG_TAG, "CR synchronisationLocalDataBase: multiple "+song.getSourceSongTitre()+" "+song.getUpdatePhone());
            String titre = song.getSourceSongTitre();
            Pupitre pupitre = song.getPupitre();
            RecordSource recordSource = song.getRecordSource();
            Song tempSong = mSongDao.getSongsByTitrePupitreSource(titre,pupitre,recordSource);
            SourceSong tempSource = mSourceDao.getSourceSongByTitre(titre);
            tempSource.setUpdateBgPhone(new Date(System.currentTimeMillis()));
            tempSources.add(tempSource);
            if(tempSong!=null) {
                Log.d(LOG_TAG, "CR syncMultipleSongDb : "+tempSong);
                tempSong.setUpdatePhone(new Date(System.currentTimeMillis()));
                tempSong.setSongPath(song.getSongPath());
                tempSong.setUpdatePhoneMp3(song.getUpdatePhoneMp3());
                if(tempSong.getSongPath()!=null) {
                    tempSongs.add(tempSong);
                }else{
                    if(tempSong.getRecordSource()==RecordSource.LIVE) {
                        tempSongsdelete.add(tempSong);
                    }else{
                        tempSongs.add(tempSong);
                    }
                }
            }else{
                Log.d(LOG_TAG, "CR syncMultipleSongDb: pb sur TempSong");
            }
        }

        if(tempSongs!=null&&tempSongs.size()!=0) {
            int tempInt = mSongDao.updatesSongs(tempSongs);
            Log.d(LOG_TAG, "CR syncMultipleSongDb: nb d'update Songs "+tempInt);

        }else{
            Log.d(LOG_TAG, "CR syncMultipleSongDb: pb sync single");
        }

        if(tempSongsdelete!=null&&tempSongsdelete.size()!=0) {
            int tempInt2 = mSongDao.deleteSongs(tempSongsdelete);
            Log.d(LOG_TAG, "CR syncMultipleSongDb: nb d'update Songs B "+tempInt2);

        }else{
            Log.d(LOG_TAG, "CR syncMultipleSongDb: pb sync single delete");
        }


        if(tempSources!=null) {
            int nbUpdate=mSourceDao.upDateSourceSongs(tempSources);
            Log.d(LOG_TAG, "CR syncMultipleSongDb(: update single source "+nbUpdate);
        }else{
            Log.d(LOG_TAG, "CR syncMultipleSongDb: pb sync single");
        }
    }

    private void syncSingleSongDb(Song song) {
        String titre = song.getSourceSongTitre();
        Pupitre pupitre = song.getPupitre();
        RecordSource recordSource = song.getRecordSource();
        Song tempSong = mSongDao.getSongsByTitrePupitreSource(titre,pupitre,recordSource);
        SourceSong tempSource = mSourceDao.getSourceSongByTitre(titre);
        tempSource.setUpdateBgPhone(new Date(System.currentTimeMillis()));
        if(tempSong!=null) {
            Log.d(LOG_TAG, "CR syncSingleSongDb "+tempSong);
            tempSong.setUpdatePhone(new Date(System.currentTimeMillis()));
            tempSong.setSongPath(song.getSongPath());
            tempSong.setUpdatePhoneMp3(song.getUpdatePhoneMp3());
        }else{
            Log.d(LOG_TAG, "CR syncSingleSongDb: pb sur TempSong single");
        }

        if(tempSong!=null) {
            if(tempSong.getSongPath()!=null) {
                int tempInt = mSongDao.updateSong(tempSong);
                Log.d(LOG_TAG, "CR syncSingleSongDb(: update single "+tempInt+" "+tempSong.getPupitre()+" "+tempSong.getSongPath());
            }else{
                if(tempSong.getRecordSource()==RecordSource.LIVE) {
                    int tempInt2 = mSongDao.deleteSong(tempSong);
                    Log.d(LOG_TAG, "CR syncSingleSongDb(: update sing le B " + tempInt2 + " " + tempSong.getPupitre() + " " + tempSong.getSongPath());
                }else{
                    int tempInt = mSongDao.updateSong(tempSong);
                    Log.d(LOG_TAG, "CR syncSingleSongDb(: update single C"+tempInt+" "+tempSong.getPupitre()+" "+tempSong.getSongPath());
                }
            }

        }else{
            Log.d(LOG_TAG, "CR syncSingleSongDb: pb sync single");
        }

        if(tempSource!=null) {
            mSourceDao.updateSourceSong(tempSource);
            Log.d(LOG_TAG, "CR syncSingleSongDb(: update single source "+" "+tempSong.getPupitre()+" "+tempSong.getSongPath());
        }else{
            Log.d(LOG_TAG, "CR syncSingleSongDb: pb sync single");
        }

        //chercher les Sourcesongs sur Room
        sourceSongsAfterSync=mSourceDao.getAllSources();
        songsAfterSync=mSongDao.getAllSongs();
    }


    private void synchronisationLocalDataBase(List<SourceSong> sourceSongs, List<Song> songs) {
        editor = sharedPreferences.edit();
        editor.putLong("majDB",majCloudDBLong);
        editor.apply();

        getModificationLists(sourceSongs,songs);
        DoWorkInLocalStorage();
        DoWorkDownloadCloud();

      //todo à retirer dès que test passé
      List<SourceSong> ssafter = mSourceDao.getAllSources();
      List<Song> songafter =mSongDao.getAllSongs();
      Log.d(LOG_TAG, "CR synchronisationLocalDataBase: bilan des courses : Sources Songs "+ssafter.size()+" songs dans la db "+songafter.size());

    }

    private void DoWorkDownloadCloud() {
        //Download Image
        if(bgSourcesToDownLoad!=null&&bgSourcesToDownLoad.size()!=0) {
            Log.d(LOG_TAG, "CR DoWorkDownloadCloud:  bgsourcetoDownload "+bgSourcesToDownLoad);
            totalBgTodowload.addAll(bgSourcesToDownLoad);
        }
        if(newSourceSongsList!=null&&newSourceSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkDownloadCloud:  newSS "+newSourceSongsList);
            totalBgTodowload.addAll(newSourceSongsList);
        }
        //download Mp3
        if(mp3SongsToDownload!=null&&mp3SongsToDownload.size()!=0) {
            Log.d(LOG_TAG, "CR DoWorkDownloadCloud:  mp3toDownload "+mp3SongsToDownload);
            totalMp3Todowload.addAll(mp3SongsToDownload);
        }

        if(newSongsList!=null&newSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkDownloadCloud:  new Songs "+newSongsList);
            totalMp3Todowload.addAll(newSongsList);
        }

        if(totalBgTodowload!=null&&totalBgTodowload.size()!=0){
            if(totalMp3Todowload!=null&&totalMp3Todowload.size()!=0){
                Log.d(LOG_TAG, "CR DoWorkDownloadCloud : 2 à downloader"+totalBgTodowload+" "+totalMp3Todowload);
                mChoraleNetworkDataSource.downloadBgImage(totalBgTodowload,false);
                mChoraleNetworkDataSource.downloadMp3(totalMp3Todowload);
            }else{
                Log.d(LOG_TAG, "CR DoWorkDownloadCloud :  que bg download"+ totalBgTodowload);
                mChoraleNetworkDataSource.downloadBgImage(totalBgTodowload,true);
            }
        }else {
            if(totalMp3Todowload!=null&&totalMp3Todowload.size()!=0){
                Log.d(LOG_TAG, "CR DoWorkDownloadCloud: que Mp3 à download "+totalMp3Todowload);
                mChoraleNetworkDataSource.downloadMp3(totalMp3Todowload);
            }else{
                Log.d(LOG_TAG, "CR DoWorkDownloadCloud: aucun download "+totalBgTodowload+" "+totalMp3Todowload);
                DoWorkInRoomAndLists();
            }
        }
    }

    private void DoWorkInLocalStorage() {
        //delete bg images
        if(deletedSourceSongsList!=null&&deletedSourceSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInLocalStorage: deletedSourceSongs "+deletedSourceSongsList);
            totalBgToDelete.addAll(deletedSourceSongsList);
        }

        if(bgSourcesToDelete!=null&&bgSourcesToDelete.size()!=0) {
            Log.d(LOG_TAG, "CR DoWorkInLocalStorage: bgSourcesTo delete "+bgSourcesToDelete);
            totalBgToDelete.addAll(bgSourcesToDelete);
        }
        Log.d(LOG_TAG, "CR DoWorkInLocalStorage: "+deletedSourceSongsList+" "+bgSourcesToDelete);

        //delete songs
        if(deletedSongsList!=null&&deletedSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInLocalStorage: deletedSongs "+deletedSongsList);
            totalMp3ToDelete.addAll(deletedSongsList);
        }
        if(mp3SongsToDelete!=null&&mp3SongsToDelete.size()!=0) {
            Log.d(LOG_TAG, "CR DoWorkInLocalStorage: mp3SongsTodelete "+mp3SongsToDelete);
           totalMp3ToDelete.addAll(mp3SongsToDelete);
        }
        Log.d(LOG_TAG, "CR DoWorkInLocalStorage: "+deletedSongsList+" "+mp3SongsToDelete);

        if(totalBgToDelete!=null&&totalBgToDelete.size()!=0){
            if(totalMp3ToDelete!=null&&totalMp3ToDelete.size()!=0){
                Log.d(LOG_TAG, "CR DoWorkInLocalStorage: Les 2 à delete "+bgSourcesToDelete);
                if(deletedBgSongsList!=null&&deletedBgSongsList.size()!=0) {
                    mChoraleNetworkDataSource.deleteBgOnPhone(deletedBgSongsList);
                }
                if(deletedMp3SongsList!=null&&deletedMp3SongsList.size()!=0) {
                    mChoraleNetworkDataSource.deleteSongsMp3OnPhone(deletedMp3SongsList);
                }
            }else{
                Log.d(LOG_TAG, "CR DoWorkInLocalStorage: que Bgtodelete "+bgSourcesToDelete);
                if(deletedBgSongsList!=null&&deletedBgSongsList.size()!=0) {
                    mChoraleNetworkDataSource.deleteBgOnPhone(deletedBgSongsList);
                }
            }
        }else {
            if(totalMp3ToDelete!=null&&totalMp3ToDelete.size()!=0){
                Log.d(LOG_TAG, "CR DoWorkInLocalStorage: que mp3 to delete "+bgSourcesToDelete);
                if(deletedMp3SongsList!=null&&deletedMp3SongsList.size()!=0) {
                    mChoraleNetworkDataSource.deleteSongsMp3OnPhone(deletedMp3SongsList);
                }
            }else{
                Log.d(LOG_TAG, "CR DoWorkInLocalStorage: Aucun à delete "+bgSourcesToDelete);
            }
        }
    }


    private void DoWorkInRoom() {
        if(deletedSongsList!=null&&deletedSongsList.size()!=0){
            int temp = mSongDao.deleteSongs(deletedSongsList);
            Log.d(LOG_TAG, "CR DoWorkInRoom: deletesongs "+temp);
        }
        if(deletedSourceSongsList!=null&&deletedSourceSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: delete sourcesongs");
            mSourceDao.deleteSourceSongs(deletedSourceSongsList);
        }

        if(modifiedSourceSongsList!=null&&modifiedSourceSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: modify sourcesongs");
            for (SourceSong source:modifiedSourceSongsList) {
                Log.d(LOG_TAG, "CR synchronisationLocalDataBase: milieu sourceSong "+source.getTitre()+" "+source.getUpdatePhone()+" "+source.getDuration()+" "+source.getUrlCloudBackground());
                String tempTitre = source.getTitre();
                String titre = titres.get(tempTitre);
                Log.d(LOG_TAG, "CR DoWorkInRoom : titres : "+tempTitre+" "+titre);
                SourceSong tempSource = mSourceDao.getSourceSongByTitre(titre);
                Log.d(LOG_TAG, "CR DoWorkInRoom: tempSource "+tempSource);
                if(tempSource!=null) {
                    tempSource.setTitre(tempTitre);
                    tempSource.setUpdatePhone(source.getUpdatePhone());
                    //todo pb ici avec le bg ?
                    if(source.getBackground()!=null) {
                        tempSource.setBackground(source.getBackground());
                    }
                    tempSource.setBaseUrlOriginalSong(source.getBaseUrlOriginalSong());
                    tempSource.setDuration(source.getDuration());
                    tempSource.setGroupe(source.getGroupe());
                    tempSource.setUrlCloudBackground(source.getUrlCloudBackground());

                    tempSourceSongs.add(tempSource);
                }else{
                    Log.d(LOG_TAG, "CR DoWorkInRoom: pb sur TempSource");
                }
            }
            if(tempSourceSongs!=null) {
                int tempInt = mSourceDao.upDateSourceSongs(tempSourceSongs);
                Log.d(LOG_TAG, "CR synchronisationLocalDataBase: nb d'update SS " + tempInt);
            }
        }

        //todo à finir la partie modifiedSong du côté admin
        if(modifiedSongsList!=null&&modifiedSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: modifysongs");

            for (Song song:modifiedSongsList) {
                Log.d(LOG_TAG, "CR synchronisationLocalDataBase: milieu song "+song.getSourceSongTitre()+" "+song.getUpdatePhone());
                String tempTitre = song.getSourceSongTitre();
                String titre = titress.get(tempTitre);
                Log.d(LOG_TAG, "CR DoWorkInRoom : titres songs : "+tempTitre+" "+titre);
                Pupitre pupitre = song.getPupitre();
                RecordSource recordSource = song.getRecordSource();
                Song tempSong = mSongDao.getSongsByTitrePupitreSource(titre,pupitre,recordSource);
                //todo voir utilité pupitre et recordSource et de titre
                if(tempSong!=null) {
                    Log.d(LOG_TAG, "CR DoWorkInRoom: "+tempSong);
                    tempSong.setSourceSongTitre(tempTitre);
                    tempSong.setUpdatePhone(song.getUpdatePhone());
                    tempSong.setPupitre(song.getPupitre());
                    tempSong.setRecordSource(song.getRecordSource());
                    tempSong.setUrlCloudMp3(song.getUrlCloudMp3());
                    tempSongs.add(tempSong);
                }else{
                    Log.d(LOG_TAG, "CR DoWorkInRoom: pb sur TempSong");
                }
            }

            if(tempSongs!=null) {
                int tempInt = mSongDao.updatesSongs(tempSongs);
                Log.d(LOG_TAG, "CR synchronisationLocalDataBase: nb d'update Songs "+tempInt);
            }
        }

        if(newSourceSongsList!=null&&newSourceSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: create sourcesongs "+newSourceSongsList);
            mSourceDao.bulkInsert(newSourceSongsList);
        }


        if(newSongsList!=null&newSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: create songs "+newSongsList);
            mSongDao.bulkInsert(newSongsList);
        }

        //chercher les Sourcesongs sur Room
        sourceSongsAfterSync=mSourceDao.getAllSources();
        songsAfterSync=mSongDao.getAllSongs();
    }

    private void getModificationLists(List<SourceSong> sourceSongs, List<Song> songs){
        Log.d(LOG_TAG, "CR getModificationLists: "+" "+songs.size()+" "+sourceSongs.size()+" "+songs+" "+sourceSongs);
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
            if (oldSong.getRecordSource() == RecordSource.BANDE_SON) {
                Log.d(LOG_TAG, "CR deletedSongsList: old " + oldSong.getSourceSongTitre());
                int i = 0;
                for (Song song : songs) {
                    Log.d(LOG_TAG, "CR deletedSongsList: cloud " + song.getSourceSongTitre());
                    if (oldSong == null) {
                        Log.d(LOG_TAG, "CR deletedSongsList:  old null ");
                    } else {
                        if (oldSong.getSongIdCloud() != null) {
                            Log.d(LOG_TAG, "CR deletedSongsList: songId pour old " + oldSong.getSongIdCloud());
                        } else {
                            Log.d(LOG_TAG, "CR deletedSongsList: songId old null " + oldSong.getSourceSongTitre() + " " + oldSong.getPupitre());
                        }
                    }
                    if (song == null) {
                        Log.d(LOG_TAG, "CR deletedSongsList: cloud song null ");
                    } else {
                        if (song.getSongIdCloud() != null) {
                            Log.d(LOG_TAG, "CR deletedSongsList: songId pour  cloud " + song.getSongIdCloud());
                        } else {
                            Log.d(LOG_TAG, "CR deletedSongsList: songId cloud null ");
                        }
                    }
                    if ((!oldSong.getSongIdCloud().equals(song.getSongIdCloud()))) {
                        i++;
                        Log.d(LOG_TAG, "CR deletedSongsList: increment " + i);
                    } else {
                        Log.d(LOG_TAG, "CR deletedSongsList: pas incrément ");
                    }
                }
                if (i == songs.size()) {
                    Log.d(LOG_TAG, "CR deletedSongsList: ajout deletesong " + oldSong.getSourceSongTitre());
                    deletedSongsList.add(oldSong);
                }
                Log.d(LOG_TAG, "CR deletedSongsList: fin d'une bloucle old ");
            }
        }

        Log.d(LOG_TAG, "CR deletedSongsList:  "+deletedSongsList.size());
        for (Song song:deletedSongsList) {
            Log.d(LOG_TAG, "CR deletedSongsList: "+song.getSourceSongTitre()+" "+song.getUpdatePhoneMp3());
            if(song.getUpdatePhoneMp3()!=null){
                deletedMp3SongsList.add(song);
            }
        }
        Log.d(LOG_TAG, "CR deletedSongsList: "+deletedMp3SongsList);
    }

    private void deletedSourceSongsList(List<SourceSong> sources) {
        for (SourceSong oldSource:oldSourcesSongs) {
            int i = 0;
            for (SourceSong source: sources) {
                if(!oldSource.getIdSourceSongCloud().equals(source.getIdSourceSongCloud())){
                    i++;
                }
            }
            if(i==sources.size()){
                deletedSourceSongsList.add(oldSource);
            }
        }
        Log.d(LOG_TAG, "CR deletedSourceSongsList: "+ deletedSourceSongsList.size());
        for(SourceSong source:deletedSourceSongsList){
            Log.d(LOG_TAG, "CR deletedSourceSongsList: "+source.getUpdateBgPhone());
            if(source.getUpdateBgPhone()!=null){
              deletedBgSongsList.add(source)  ;
            }
        }
    }


    private void modifiedSongsList(List<Song> songs) {
        Log.d(LOG_TAG, "CR modifiedSongsList: modified Songs List");
        for (Song song:songs) {
            for (Song oldSong: oldSongs) {
                if (oldSong.getRecordSource() == RecordSource.BANDE_SON) {
                    if (oldSong.getSongIdCloud().equals(song.getSongIdCloud())) {
                        Log.d(LOG_TAG, "CR modifiedSongsList: " + oldSong.getUpdatePhone() + " " + song.getUpdatePhone());
                        if (oldSong.getUpdatePhone().getTime() < song.getUpdatePhone().getTime()) {
                            Log.d(LOG_TAG, "CR modifiedSongsList: ajout de modified SS " + song.getSourceSongTitre());
                            modifiedSongsList.add(song);
                            if (!oldSong.getUrlCloudMp3().equals(song.getUrlCloudMp3())) {
                                mp3SongsToDelete.add(oldSong);
                                mp3SongsToDownload.add(song);
                            }
                        }
                    }
                    titress.put(song.getSourceSongTitre(), oldSong.getSourceSongTitre());
                }
            }
        }
        Log.d(LOG_TAG, "CR modifiedSongsList: "+modifiedSongsList.size());
        for (Song song:mp3SongsToDelete) {
            Log.d(LOG_TAG, "CR deletedSongsList: "+song.getSourceSongTitre()+" "+song.getUpdatePhoneMp3());
            if(song.getUpdatePhoneMp3()!=null){
                deletedMp3SongsList.add(song);
            }
        }
    }

    private void modifiedSourcesSongsList(List<SourceSong> sources) {
        for (SourceSong source:sources) {
            for (SourceSong oldSource: oldSourcesSongs) {
                if(oldSource.getIdSourceSongCloud().equals(source.getIdSourceSongCloud())){
                    Log.d(LOG_TAG, "CR modifiedSourcesSongsList: "+oldSource.getUpdatePhone()+" "+source.getUpdatePhone());

                    if(oldSource.getUpdatePhone().getTime()<source.getUpdatePhone().getTime()){
                        Log.d(LOG_TAG, "CR modifiedSourcesSongsList: "+oldSource.getBackground()+" "+source.getBackground()+" "+oldSource.getTitre()+" "+source.getTitre()+" "+oldSource.getGroupe()+" "+source.getGroupe()+" "+oldSource.getBaseUrlOriginalSong()+ " "+ source.getBaseUrlOriginalSong()+" "+oldSource.getDuration()+" "+source.getDuration());
                        Log.d(LOG_TAG, "CR modifiedSourcesSongsList: ajout de modified SS "+source.getTitre());
                        modifiedSourceSongsList.add(source);
                        if(!oldSource.getUrlCloudBackground().equals(source.getUrlCloudBackground())){
                            bgSourcesToDelete.add(oldSource);
                            bgSourcesToDownLoad.add(source);
                            Log.d(LOG_TAG, "CR modifiedSourcesSongsList:  delete download ");
                        }
                    }
                    titres.put(source.getTitre(),oldSource.getTitre());
                }
            }
        }

        Log.d(LOG_TAG, "CR modifiedSourcesSongsList: "+modifiedSourceSongsList.size());
        for(SourceSong source:bgSourcesToDelete){
            Log.d(LOG_TAG, "CR deletedSourceSongsList: "+source.getUpdateBgPhone());
            if(source.getUpdateBgPhone()!=null){
                deletedBgSongsList.add(source);
            }
        }
    }


    private void newSongsList(List<Song> songs) {
        for (Song song:songs) {
            int i = 0;
            for (Song oldSong: oldSongs) {
                if(oldSong.getRecordSource()==RecordSource.BANDE_SON) {
                    if (!oldSong.getSongIdCloud().equals(song.getSongIdCloud())) {
                        i++;
                    }
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
                if(!oldSource.getIdSourceSongCloud().equals(source.getIdSourceSongCloud())){
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
        boolean initialisation = sharedPreferences.getBoolean("initializeData",false);
        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if(initialisation){
            mInitialized=false;
        }
        if (mInitialized) return;
        mInitialized = true;

        if (isFetchNeeded()) {
            currentPupitreStr=getCurrentPupitreStr();
            Log.d(LOG_TAG, "CR : isFetchNeeded "+ currentPupitreStr);

            String idChorale=sharedPreferences.getString("idchorale","");
            Log.d(LOG_TAG, "CR initializeData: idchorale "+idChorale);

            if(!TextUtils.isEmpty(idChorale)&&initialisation) {
                getMajDateLocalDataBase();
                //lance la recherche d'une mise à jour et condition le lancement de startFetchData
                LoadMajCloudDB();
            }else{
                Log.d(LOG_TAG, "CR initializeData: pb d'initialisation du sharedpreferences");
            }

            Log.d(LOG_TAG, "CR : isFetchNeeded "+ Thread.currentThread().getName());
        }else{
            Log.d(LOG_TAG, "CR initializeData: inutile les données n'ont pas changées ");
            try {
                t5.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "CR join: interrupted exception");
            }
            if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0){
                isFromLocal=true;
                typeSS="oldSS";
                //todo voir comment retirer les arguments qui sont inutiles
                DoSynchronization(oldSourcesSongs,oldSongs);
                Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : données initiales "+oldSourcesSongs+" "+oldSongs);
            }else{
                Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : pas de données initiales ");
                //todo ajouter une alerte éventuelle si nécessaire ?
            }
            Log.d(LOG_TAG, "CR ChoraleRepository: Stop startFectch pas lancé !");
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

        isAuto = sharedPreferences.getBoolean(context.getString(R.string.maj_auto),true);
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
        currentPupitreStr=mChoraleNetworkDataSource.getCurrentPupitreStr();
        Log.d(LOG_TAG, "CR getCurrentPupitreStr: "+currentPupitreStr);
        return currentPupitreStr;
    }

    public Thread getCurrentThread() {

        return currentThread;
    }

    public ListSongs getListSongs() {

        return listSongs;
    }

    public void setRecordSongInAppDb(Song song) {
        typeSS="newRecord";
        Log.d(LOG_TAG, "CR setRecordSongInAppDb: ");
        t6 = new Thread(new Runnable() {
            @Override
            public void run() {
                currentThread = Thread.currentThread();
                syncrecordedSongDb(song);
                getListSongsA();
            }
        });
        t6.start();


    }

    private void syncrecordedSongDb(Song song) {
        String titre = song.getSourceSongTitre();
        SourceSong tempSource = mSourceDao.getSourceSongByTitre(titre);
        tempSource.setUpdateBgPhone(new Date(System.currentTimeMillis()));
        if(song!=null) {
            Log.d(LOG_TAG, "CR syncrecordedSongDb "+song);
            mSongDao.insertSong(song);
        }else{
            Log.d(LOG_TAG, "CR syncRecordedSongDb: pb sur TempSong single");
        }

        if(tempSource!=null) {
            mSourceDao.updateSourceSong(tempSource);
            Log.d(LOG_TAG, "CR syncRecordedSongDb(: update single source "+" "+song.getPupitre()+" "+song.getSongPath()+" "+song.getRecordSource());
        }else{
            Log.d(LOG_TAG, "CR syncRecordedSongDb: pb sync single");
        }

        //chercher les Sourcesongs sur Room
        sourceSongsAfterSync=mSourceDao.getAllSources();
        songsAfterSync=mSongDao.getAllSongs();

    }

    public String getTypeSS() {
        return typeSS;
    }

    public boolean getDeleted() {
       return mChoraleNetworkDataSource.isDeleted();
    }

    public void getData(String current_user_id) {
        mChoraleNetworkDataSource.getData(current_user_id);
    }
}


