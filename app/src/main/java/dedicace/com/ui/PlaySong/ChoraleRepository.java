package dedicace.com.ui.PlaySong;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private boolean mInitialized = false;
    private Thread currentThread,t2,t1,t3,t4,t5,t6,t7, threadSaisons;
    private static Context context;

    //Songs
    private List<SourceSong> sourceSongsAfterSync = new ArrayList<>();
    private List<Song> songsAfterSync = new ArrayList<>();
    private List<SourceSong> oldSourcesSongs = new ArrayList<>();
    private List<Song> songs;
    private List<Song> oldSongs = new ArrayList<>();
    private List<SourceSong> newSourceSongsList = new ArrayList<>();
    private List<Song> newSongsList = new ArrayList<>();
    private List<SourceSong> deletedSourceSongsList = new ArrayList<>();
    private List<Song> deletedSongsList = new ArrayList<>();
    private List<Song> deletedMp3SongsList = new ArrayList<>();
    private List<SourceSong> deletedBgSongsList = new ArrayList<>();
    private List<SourceSong> modifiedSourceSongsList = new ArrayList<>();
    private List<Song> modifiedSongsList = new ArrayList<>();
    private List<Song> modifiedSongsTitresList = new ArrayList<>();
    private List<SourceSong> bgSourcesToDelete = new ArrayList<>();
    private List<SourceSong> bgSourcesToDownLoad = new ArrayList<>();
    private List<String> listIdSongs = new ArrayList<>();
    private List<String> listOldIdSongs = new ArrayList<>();
    private List<String> listOldIdSourceSongs = new ArrayList<>();
    private List<String> listIdSourceSongs = new ArrayList<>();
    private List<String> listOldIdSaisons = new ArrayList<>();
    private List<String> listIdSaisons = new ArrayList<>();
    private List<String> listOldIdSpectacles = new ArrayList<>();

    private List<Spectacle> spectacles = new ArrayList<>();
    private List<String> listIdSpectacles = new ArrayList<>();
    private static List<Saison> saisons = new ArrayList<>();
    private List<Song>  mp3SongsToDelete = new ArrayList<>();
    private List<Song>  mp3SongsToDownload = new ArrayList<>();
    private List<Song> tempSongs = new ArrayList<>();
    private List<Saison> tempSaisons = new ArrayList<>();
    private List<Spectacle> tempSpectacles = new ArrayList<>();
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
    private List<Saison> oldSaisons = new ArrayList<>();
    private List<Spectacle> oldSpectacles = new ArrayList<>();
    private List<Saison> deletedSaisonsList = new ArrayList<>();
    private List<Spectacle> deletedSpectaclesList = new ArrayList<>();
    private List<Saison> modifiedSaisonsList = new ArrayList<>();
    private List<Spectacle> modifiedSpectaclesList = new ArrayList<>();
    private List<Saison> newSaisonsList = new ArrayList<>();
    private List<Spectacle> newSpectaclesList = new ArrayList<>();
    private boolean modifEnCours;

    private ListSongs listSongs;

    private String typeSS;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Long majCloudDBLong;
    private Long majLocalDBLong;
    private boolean isFromLocal;
    private Song songToDelete;


    private ChoraleRepository(SongsDao songsDao, SourceSongDao sourceSongDao, SaisonDao saisonDao, SpectacleDao spectacleDao, final ChoraleNetWorkDataSource choraleNetworkDataSource) {
        Log.d(LOG_TAG, "CR Repository: constructor");

        mSongDao = songsDao;
        mSourceDao=sourceSongDao;
        mSaisonDao=saisonDao;
        mSpectacleDao=spectacleDao;
        mChoraleNetworkDataSource = choraleNetworkDataSource;

        context = mChoraleNetworkDataSource.getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        modifEnCours=false;

        t5 = new Thread(() -> {
            t5 = Thread.currentThread();
            oldSourcesSongs = mSourceDao.getAllSources();
            oldSongs=mSongDao.getAllSongs();
            oldSaisons=mSaisonDao.getAllSaisons();
            oldSpectacles=mSpectacleDao.getAllSpectacles();
            Log.d(LOG_TAG, "CR run:  old SS et song "+oldSourcesSongs.size()+" songs "+oldSongs.size()+" saisons "+oldSaisons.size()+" spectacles "+oldSpectacles.size());
           for(SourceSong sourceSong : oldSourcesSongs){
               Log.d(LOG_TAG, "CR ChoraleRepository: old SS "+sourceSong.getTitre()+" "+ sourceSong.getIdSourceSongCloud()+" "+sourceSong.getUpdatePhone());
           }
           for(Song song:oldSongs){
               //patch pour des Idcloud ayant disparu ou nul
               if(song.getSongIdCloud()==null){
                   Log.d(LOG_TAG, "CR ChoraleRepository: idCloud null "+ song.getSourceSongTitre() + " " + song.getPupitre() + " " + song.getSongIdCloud() + " " + song.getUpdatePhone() + " " + song.getSongPath());

               }
               //patch pour récupérer les chansons qui n'ont plus leur fichier mp3
               String path = song.getSongPath();
               Log.d(LOG_TAG, "CR ChoraleRepository: old Songs début " + song.getSourceSongTitre() + " " + song.getPupitre() + " " + song.getSongIdCloud() + " " + song.getUpdatePhone() + " " + song.getSongPath());


               if(!TextUtils.isEmpty(path)) {
                   File tempFile = new File(path);
                   String name = tempFile.getName();
                   Log.d(LOG_TAG, "CR patch fichier disparu " + name);

                   if (tempFile.exists()) {
                       Log.d(LOG_TAG, "CR ChoraleRepository: old Songs A " + song.getSourceSongTitre() + " " + song.getPupitre() + " " + song.getSongIdCloud() + " " + song.getUpdatePhone() + " " + song.getSongPath());

                   } else {
                       totalMp3Todowload.add(song);
                       Log.d(LOG_TAG, "CR ChoraleRepository: old Songs B : n'existe pas " + song.getSourceSongTitre() + " " + song.getPupitre() + " " + song.getSongIdCloud() + " " + song.getUpdatePhone() + " " + song.getSongPath());

                   }
               }else{
                   Log.d(LOG_TAG, "CR ChoraleRepository: pas de path");
               }
           }

           for(Saison saison:oldSaisons){
               Log.d(LOG_TAG, "CR ChoraleRepository: oldSaison "+saison.getSaisonName()+ " "+saison.getIdsaisonCloud()+ " "+saison.getIdSpectacles()+" "+ saison.getUpdatePhone());
           }

           for(Spectacle spectacle: oldSpectacles){
                Log.d(LOG_TAG, "CR ChoraleRepository: oldSpectacles "+spectacle.getSpectacleName()+" "+spectacle.getIdSpectacleCloud()+" "+spectacle.getIdTitresSongs());
            }

        });
        t5.start();


        final LiveData<List<Saison>> majSaisonCloud = mChoraleNetworkDataSource.getSaisonsCloud();
        Log.d(LOG_TAG, "CR ChoraleRepository: getSaisonsCloud "+majSaisonCloud);

        majSaisonCloud.observeForever(saisons -> {
            ChoraleRepository.saisons =saisons;
            spectacles = mChoraleNetworkDataSource.getSpectacles();
            listIdSpectacles =mChoraleNetworkDataSource.getListIdSpectacles();

            Log.d(LOG_TAG, "CR ChoraleRepository: modifencours "+modifEnCours+" "+listIdSpectacles+" "+spectacles);

            //cas où il y a une modif éventuelle sur saisons et spectacles.
            if(modifEnCours){
                modifEnCours=false;
                editor=sharedPreferences.edit();
                editor.putBoolean("modifEnCours",false);
                editor.apply();
                Log.d(LOG_TAG, "CR ChoraleRepository: modif en cours majSaisonCloud ");
                startFetchSongsService();

            }else {
                Log.d(LOG_TAG, "CR ChoraleRepository: pas de modification en cours ");
                majRoomDb();
            }
        });

        LiveData<Long> majDBCloudLong = mChoraleNetworkDataSource.getMajDBCloudLong();
        Log.d(LOG_TAG, "CR ChoraleRepository: getmajDBCCloud "+ majDBCloudLong);

        majDBCloudLong.observeForever(majclouddblong -> {
            majCloudDBLong = majclouddblong;
            Log.d(LOG_TAG, "CR Alerte Maj ChoraleRepository: majCloudLong "+ majclouddblong);

            if(majLocalDBLong<majCloudDBLong){
                if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0){
                    typeSS="oldSS";
                    Log.d(LOG_TAG, "CR ChoraleRepository modification : ok on lance startFetchSongService");
                    isFromLocal=true;
                    DoSynchronization(oldSourcesSongs,oldSongs);
                    Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs 2 modifications: données initiales "+oldSourcesSongs+" "+oldSongs);
                    try {
                        t2.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "CR join: interrupted exception");
                    }
                    String userId = sharedPreferences.getString("userId","");
                    Log.d(LOG_TAG, "CR ChoraleRepository: userID "+ userId);

                    if(!TextUtils.isEmpty(userId)) {
                        Log.d(LOG_TAG, "CR ChoraleRepository: getData");
                        modifEnCours=true;
                        mChoraleNetworkDataSource.getData(userId);
                    }else{
                        Log.d(LOG_TAG, "CR ChoraleRepository: pb de userId");
                    }

                }else{
                    typeSS="newSS";
                    Log.d(LOG_TAG, "CR ChoraleRepository new SS : ok on lance startFetchSongService");
                    startFetchSongsService();
                }
                Log.d(LOG_TAG, "CR ChoraleRepository: ok on lance startFetchSongService "+t2);


            }else{
                //pour le cas aucune modif
                if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0){
                    //chemin A aucune modification et données initiales
                    isFromLocal=true;
                    typeSS="oldSS";
                    //todo voir comment retirer les arguments qui sont inutiles
                    DoSynchronization(oldSourcesSongs,oldSongs);
                    Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : données initiales "+oldSourcesSongs+" "+oldSongs);
                }else{
                    //cas de réinitialisation de spectacles lors de la première installation
                    Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : pas de données initiales else majCloud");
                    t7 = new Thread(() -> {
                        t7 = Thread.currentThread();
                        oldSourcesSongs = mSourceDao.getAllSources();
                        oldSongs=mSongDao.getAllSongs();
                    });
                    t7.start();

                    try {
                        t7.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(oldSourcesSongs!=null&&oldSourcesSongs.size()!=0) {
                        Log.d(LOG_TAG, "CR run:  old SS et song " + oldSourcesSongs.size() + " songs " + oldSongs.size());
                        isFromLocal = true;
                        typeSS = "oldSS";
                        //todo voir comment retirer les arguments qui sont inutiles
                        DoSynchronization(oldSourcesSongs, oldSongs);
                        Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : données initiales B" + oldSourcesSongs + " " + oldSongs);
                    }
                }
                Log.d(LOG_TAG, "CR ChoraleRepository: Stop startFetcch pas lancé !");
            }
        });


        final LiveData<List<SourceSong>> networkDataSourceSongs = mChoraleNetworkDataSource.getSourceSongs();
        Log.d(LOG_TAG, "CR ChoraleRepository: LiveData mChoraleNetworkdtasource SS "+mChoraleNetworkDataSource+" "+ networkDataSourceSongs);
        networkDataSourceSongs.observeForever(sourceSongs -> {

            sourceSongs1=sourceSongs;
            //todo vérifier utilité de sourceSongs1
            if(sourceSongs1!=null&&sourceSongs1.size()!=0){
                Log.d(LOG_TAG, "NDS ChoraleRepository: "+sourceSongs1.size()+sourceSongs.size());
            }

            Log.d(LOG_TAG, "CR Repository: observers Alerte cela bouge ! "+" "+sourceSongs1.size()+" "+sourceSongs1+Thread.currentThread().getName());
            songs = choraleNetworkDataSource.getSongs();
            for(Song song : songs){
                Log.d(LOG_TAG, "CR fetchSongs: list songs "+song.getSourceSongTitre()+" "+song.getPupitre()+" "+song.getSongIdCloud()+" ");
            }
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
        //todo retirer le thread dès que l'on enlèvera le test
        threadSaisons = new Thread(() -> {
            mSaisonDao.bulkInsert(saisons);
            mSpectacleDao.bulkInsert(spectacles);

            editor = sharedPreferences.edit();

            Set<String> setIdSpectacles = new HashSet<>(listIdSpectacles);
            Log.d(LOG_TAG, "CR getData: setIdSpectacles B "+ setIdSpectacles);
            editor.putStringSet("currentSpectacles", setIdSpectacles);
            editor.apply();


            //todo supprimer dès que test passé
            List<Saison> tempSaisons = mSaisonDao.getAllSaisons();
            List<Spectacle> tempSpectacles = mSpectacleDao.getAllSpectacles();
            Log.d(LOG_TAG, "CR majRoomDb: nb saisons "+tempSaisons.size());
            Log.d(LOG_TAG, "CR majRoomDb: nb spectacles "+tempSpectacles.size());


            for(Saison saison : tempSaisons) {
                Log.d(LOG_TAG, "CR majRoomDb: saisons"+ saison.getIdsaisonCloud()+" "+ saison.getSaisonName()+ " "+ saison.getIdSpectacles()+" "+saison.getUpdatePhone());

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

            Log.d(LOG_TAG, "CR getData: setIdSpectacles avant B  "+ listIdSpectacles);
            editor=sharedPreferences.edit();
            Set<String> setIdSpectacles = new HashSet<>(listIdSpectacles);

            Log.d(LOG_TAG, "CR doworkInRoomandlists: setIdSpectacles A "+ setIdSpectacles);
            editor.putStringSet("currentSpectacles", setIdSpectacles);
            editor.apply();

            getListSongsA();
        });

        t1.start();
    }

    public synchronized static ChoraleRepository getInstance(SongsDao songsDao, SourceSongDao sourceSongDao, SaisonDao saisonDao, SpectacleDao spectacleDao, ChoraleNetWorkDataSource choraleNetworkDataSource) {

        Log.d(LOG_TAG, "CR getInstance: repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleRepository(songsDao, sourceSongDao, saisonDao,spectacleDao, choraleNetworkDataSource);

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
                Log.d(LOG_TAG, "CR run: if from local avant synchronisation db "+typeSS+" "+spectacles.size());
                synchronisationLocalDataBase(sourceSongs,songs);

            }else{
                //chemin A : pas de modification
                //todo trouver une méthode un peu moins artificielle ? cf modèle architecture.
                Log.d(LOG_TAG, "CR run: else isFrom Local pas de synchronisation");
                //mis pour que alerte se déclenche
                mSourceDao.updateSourceSong(oldSourcesSongs.get(0));
                sourceSongsAfterSync=oldSourcesSongs;
                songsAfterSync=oldSongs;
                getListSongsA();
            }
            Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync sourceSongs : "+sourceSongs.size()+ " "+sourceSongsAfterSync.size()+" "+Thread.currentThread().getName());

        });

        Log.d(LOG_TAG, "CR DoSynchronization: juste avant T2 start");
        t2.start();
    }


    private void getListSongsA() {

        listSongs= new ListSongs(mSongDao,mSourceDao,mSpectacleDao,sourceSongsAfterSync,songsAfterSync,context);
        listSongs.getSongOnClouds();
        listSongs.getSongOnPhoneBS();

        //todo vérifier l'utilité de celui-là
        listSongs.getSongToPlaysBs();

        listSongs.getSongOnPhoneLive();

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

        Log.d(LOG_TAG, "CR synchronisationLocalDataBase: "+spectacles.size());

        if(!typeSS.equals("newSS")) {
            getModificationListsSaisonSpectacle();
        }
        getModificationLists(sourceSongs,songs);
        DoWorkInLocalStorage();
        DoWorkDownloadCloud();

        if(t1!=null) {

            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //todo à retirer dès que test passé
      List<SourceSong> ssafter = mSourceDao.getAllSources();
      List<Song> songafter =mSongDao.getAllSongs();
      Log.d(LOG_TAG, "CR synchronisationLocalDataBase: bilan des courses : Sources Songs "+ssafter.size()+" songs dans la db "+songafter.size());

    }

    private void getModificationListsSaisonSpectacle() {
        Log.d(LOG_TAG, "CR getModificationListsSaisonSpectacle: "+saisons+" "+ spectacles.size()+" "+spectacles);
        deletedSaisonsList();
        deletedSpectaclesList();
        modifiedSaisonsList();
        modifiedSpectaclesList();
        newSaisonsList();
        newSpectaclesList();
    }

    private void deletedSaisonsList() {
        List<String> listIdSaisons = new ArrayList<>();
        List<String> listOldIdSaisons = new ArrayList<>();
        List<Saison> listRestOldSaison = new ArrayList<>();

        //Patch gestion des doublons sur Room (patch pour erreur ancienne)
        List<Saison> oldTest = new ArrayList<>(oldSaisons);

        for(Saison saison : oldTest){
            for(Saison saison1 :oldTest){
                if(saison.getIdsaisonCloud().equals(saison1.getIdsaisonCloud())&&saison.getSaisonId()!=saison1.getSaisonId()){
                    if(!listRestOldSaison.contains(saison1)) {
                        listRestOldSaison.add(saison);
                        break;
                    }
                }
            }
        }

        for(Saison saison:listRestOldSaison){
            oldTest.remove(saison);
        }

        Log.d(LOG_TAG, "CR deletedSaisonsList: "+oldTest.size()+" "+oldTest.get(0).getSaisonId());
        deletedSaisonsList.addAll(listRestOldSaison);


        //gestion des saisons à supprimer (Normal)

        for(Saison saison:saisons){
            Log.d(LOG_TAG, "CR deletedSaisonsList: new "+ saison.getSaisonName()+" "+saison.getIdsaisonCloud());
            listIdSaisons.add(saison.getIdsaisonCloud());
        }

        for(Saison saison:oldSaisons){
            Log.d(LOG_TAG, "CR deletedSaisonsList: idlocal "+saison.getSaisonId()+ saison.getUpdatePhone());
            Log.d(LOG_TAG, "CR deletedSaisonsList: old "+ saison.getSaisonName()+" "+saison.getIdsaisonCloud()+" "+saison.getSaisonId());
            listOldIdSaisons.add(saison.getIdsaisonCloud());
        }


        Log.d(LOG_TAG, "CR deletedSaisonsList: A "+listIdSaisons.size()+" "+listOldIdSaisons.size()+" "+saisons.size()+" "+oldSaisons.size()+" "+listOldIdSaisons);
        for(String oldStrSaison :listOldIdSaisons){
            Log.d(LOG_TAG, "CR deletedSaisonsList: coucou");
            Log.d(LOG_TAG, "CR deletedSaisonsList: oldSaisons "+ listOldIdSaisons.size()+" "+oldStrSaison);

            if(!listIdSaisons.contains(oldStrSaison)){
                Log.d(LOG_TAG, "CR deletedSaisonsList: coucou2");
                Log.d(LOG_TAG, "CR deletedSaisonsList: ajout dans la list de deleteSaison ");
                for(Saison saison:oldSaisons){
                    Log.d(LOG_TAG, "CR deletedSaisonsList: coucou3");
                    Log.d(LOG_TAG, "CR deletedSaisonsList: "+saison.getSaisonName());
                    if(saison.getIdsaisonCloud().equals(oldStrSaison)){
                        Log.d(LOG_TAG, "CR deletedSaisonsList: coucou4");
                        Log.d(LOG_TAG, "CR deletedSaisonsList: addlist deleted saisosn");
                        deletedSaisonsList.add(saison);
                    }
                }
            }
        }

        //gestion des doublons

        Log.d(LOG_TAG, "CR deletedSaisonsList: deleted saisons "+deletedSaisonsList.size());
    }


    private void deletedSpectaclesList() {
        Log.d(LOG_TAG, "CR deletedSpectaclesList: list "+ spectacles.size()+" "+oldSpectacles.size());
        List<String> listIdSpectacles = new ArrayList<>();
        List<String> listOldIdSpectacles = new ArrayList<>();
        List<Spectacle> listRestOldSpectacle = new ArrayList<>();



        //patch doublon
        List<Spectacle> oldTest = new ArrayList<>(oldSpectacles);

        for(Spectacle spectacle : oldTest){
            //Log.d(LOG_TAG, "CR deletedSpectaclesList: oldtest 1 "+spectacle.getSpectacleId()+ " "+spectacle.getIdSpectacleCloud());
            for(Spectacle spectacle1 :oldTest){
              //  Log.d(LOG_TAG, "CR deletedSpectaclesList: oldtest 2 "+spectacle1.getSpectacleId()+ " "+spectacle1.getIdSpectacleCloud());

                if(spectacle.getIdSpectacleCloud().equals(spectacle1.getIdSpectacleCloud())&&spectacle.getSpectacleId()!=spectacle1.getSpectacleId()){
                  //  Log.d(LOG_TAG, "CR deletedSpectaclesList: oldtest 3 "+spectacle.getSpectacleId()+ " "+spectacle.getIdSpectacleCloud());

                    if(!listRestOldSpectacle.contains(spectacle1)) {
                    //    Log.d(LOG_TAG, "CR deletedSpectaclessList: oldtest 4 "+spectacle.getSpectacleId()+ " "+spectacle.getIdSpectacleCloud());
                        listRestOldSpectacle.add(spectacle);
                        break;
                    }
                }
            }
        }
       // Log.d(LOG_TAG, "CR deletedSpectaclesList: oldtest B"+listRestOldSpectacle.size()+ " "+listRestOldSpectacle+" "+listRestOldSpectacle.get(0).getSpectacleId());


        for(Spectacle spectacle:listRestOldSpectacle){
            oldTest.remove(spectacle);
        }

        Log.d(LOG_TAG, "CR deletedSpectaclesList: "+oldTest.size()+" "+oldTest.get(0).getSpectacleId());

        for(Spectacle spectacle :oldTest){
            Log.d(LOG_TAG, "CR deletedSpectaclesList: liste restante "+spectacle.getSpectacleId()+" "+spectacle.getSpectacleName());
        }

        deletedSpectaclesList.addAll(listRestOldSpectacle);

        //gestion deleted saisons normal

        for(Spectacle spectacle:spectacles){
            Log.d(LOG_TAG, "CR deletedSpectaclesList: new "+spectacle.getSpectacleName());
            listIdSpectacles.add(spectacle.getIdSpectacleCloud());
        }

        for(Spectacle spectacle:oldSpectacles){
            Log.d(LOG_TAG, "CR deletedSpectaclesList: idlocal "+spectacle.getSpectacleId()+ spectacle.getUpdatePhone());
            Log.d(LOG_TAG, "CR deletedSpectaclesList: old "+spectacle.getSpectacleName());
            listOldIdSpectacles.add(spectacle.getIdSpectacleCloud());
        }

        Log.d(LOG_TAG, "CR deletedSpectaclesList: new "+listIdSpectacles.size()+" old "+listOldIdSpectacles.size());
        for(String oldSpectacleStr :listOldIdSpectacles){
            Log.d(LOG_TAG, "CR deletedSpectaclesList: oldSpectacles "+listOldIdSpectacles.size()+" "+oldSpectacleStr);
            if(!listIdSpectacles.contains(oldSpectacleStr)){
                for(Spectacle spectacle:oldSpectacles){
                    if(spectacle.getIdSpectacleCloud().equals(oldSpectacleStr)){
                        deletedSpectaclesList.add(spectacle);
                    }
                }
            }
        }
        Log.d(LOG_TAG, "CR deletedSpectaclesList: deleted spectacles "+deletedSpectaclesList.size());
    }

    private void modifiedSaisonsList() {
        for(Saison saison:saisons){
            for(Saison oldSaison:oldSaisons){
                if(oldSaison.getIdsaisonCloud().equals(saison.getIdsaisonCloud())){
                    if(oldSaison.getUpdatePhone().getTime()<saison.getUpdatePhone().getTime()){
                        modifiedSaisonsList.add(saison);
                    }
                }
            }
        }

        Log.d(LOG_TAG, "CR modifiedSaisonsList: "+modifiedSaisonsList.size());
        for(Saison saison:modifiedSaisonsList){
            Log.d(LOG_TAG, "CR modifiedSaisonsList: "+saison.getSaisonName()+" "+saison.getIdSpectacles());
        }
    }


    private void modifiedSpectaclesList() {
        for(Spectacle spectacle:spectacles){
            for(Spectacle oldSpectacle:oldSpectacles){
                if(oldSpectacle.getIdSpectacleCloud().equals(spectacle.getIdSpectacleCloud())){
                    if(oldSpectacle.getUpdatePhone().getTime()<spectacle.getUpdatePhone().getTime()){
                        modifiedSpectaclesList.add(spectacle);
                    }
                }
            }
        }

        Log.d(LOG_TAG, "CR modifiedSpectaclesList: "+modifiedSpectaclesList.size());

        for(Spectacle spectacle:modifiedSpectaclesList){
            Log.d(LOG_TAG, "CR modifiedSpectaclesList: "+spectacle.getSpectacleName()+" "+spectacle.getIdTitresSongs()+" "+spectacle.getSpectacleLieux()+ " "+ spectacle.getSpectacleDates());
        }
    }


    private void newSaisonsList() {
        List<String> listIdSaisons = new ArrayList<>();
        List<String> listOldIdSaisons = new ArrayList<>();
        for(Saison saison:saisons){
            listIdSaisons.add(saison.getIdsaisonCloud());
        }

        for(Saison saison:oldSaisons){
            listOldIdSaisons.add(saison.getIdsaisonCloud());
        }

        for(String strSaison :listIdSaisons){
            Log.d(LOG_TAG, "CR newSaisonsList: Saisons size des new saisons "+ listIdSaisons.size());

            if(!listOldIdSaisons.contains(strSaison)){
                Log.d(LOG_TAG, "CR newSaisonsList: ajout dans la list de newSaison ");
                for(Saison saison:saisons){
                    if(saison.getIdsaisonCloud().equals(strSaison)){
                        newSaisonsList.add(saison);
                    }
                }
            }
        }
        Log.d(LOG_TAG, "CR newSaisonsList: new saisons "+newSaisonsList.size());
    }


    private void newSpectaclesList() {
        Log.d(LOG_TAG, "CR newSpectaclesList: list "+ spectacles.size()+" "+ oldSpectacles.size());
        List<String> listIdSpectacles = new ArrayList<>();
        List<String> listOldIdSpectacles = new ArrayList<>();
        for(Spectacle spectacle:spectacles){
            Log.d(LOG_TAG, "CR newSpectaclesList: new "+spectacle.getSpectacleName());
            listIdSpectacles.add(spectacle.getIdSpectacleCloud());
        }

        for(Spectacle spectacle:oldSpectacles){
            Log.d(LOG_TAG, "CR newSpectaclesList: Old "+spectacle.getSpectacleName());
            listOldIdSpectacles.add(spectacle.getIdSpectacleCloud());
        }

        for(String spectacleStr :listIdSpectacles){
            Log.d(LOG_TAG, "CR newSpectaclesList: spectacles "+listIdSpectacles.size());
            if(!listOldIdSpectacles.contains(spectacleStr)){
                for(Spectacle spectacle:spectacles){
                    Log.d(LOG_TAG, "CR newSpectaclesList: C "+spectacle.getSpectacleName());
                    if(spectacle.getIdSpectacleCloud().equals(spectacleStr)){
                        Log.d(LOG_TAG, "CR newSpectaclesList: D "+spectacle.getSpectacleName());
                        newSpectaclesList.add(spectacle);
                    }
                }
            }
        }
        Log.d(LOG_TAG, "CR newSpectaclesList: new spectacles "+newSpectaclesList.size()+" "+spectacles.size()+" "+oldSpectacles.size());
    }


    private void getModificationLists(List<SourceSong> sourceSongs, List<Song> songs){
        Log.d(LOG_TAG, "CR getModificationLists: "+" "+songs.size()+" "+sourceSongs.size()+" "+songs+" "+sourceSongs);

        getListIdSongs(songs);
        getListIdSourceSongs(sourceSongs);
        deletedSongsList(songs);
        deletedSourceSongsList(sourceSongs);
        modifiedSongsList(songs);
        modifiedSourcesSongsList(sourceSongs);
        newSourceSongsList(sourceSongs);
        newSongsList(songs);
    }

    private void getListIdSourceSongs(List<SourceSong> sourceSongs) {
        for(SourceSong sourceSong:oldSourcesSongs){
            listOldIdSourceSongs.add(sourceSong.getIdSourceSongCloud());
          //  Log.d(LOG_TAG, "CR getListIdSourceSongs: old "+sourceSong.getTitre()+" "+sourceSong.getIdSourceSongCloud());
        }

        Log.d(LOG_TAG, "CR getListIdSourceSongs: size old "+listOldIdSourceSongs.size());

        for(SourceSong sourceSong:sourceSongs){
            listIdSourceSongs.add(sourceSong.getIdSourceSongCloud());
           // Log.d(LOG_TAG, "CR getListIdSourceSongs: new "+sourceSong.getTitre()+" "+sourceSong.getIdSourceSongCloud());
        }

        Log.d(LOG_TAG, "CR getListIdSourceSongs: size new "+listIdSourceSongs.size());

    }

    private void getListIdSongs(List<Song> songs) {

        for(Song song:songs){
            listIdSongs.add(song.getSongIdCloud());
           // Log.d(LOG_TAG, "CR newSongsList: new "+song.getSourceSongTitre()+" "+song.getPupitre()+" "+song.getSongIdCloud());
        }
        Log.d(LOG_TAG, "CR newSongsList: rés new "+listIdSongs.size()+" "+listIdSongs);

        for(Song song:oldSongs){
            listOldIdSongs.add(song.getSongIdCloud());
            //Log.d(LOG_TAG, "CR newSongsList: old "+song.getSourceSongTitre()+" "+song.getPupitre()+" "+song.getSongIdCloud());
        }

        Log.d(LOG_TAG, "CR newSongsList: res old "+listOldIdSongs.size()+listOldIdSourceSongs);


    }

    //todo voir pour les mettre dans SongsUtilities
    private void deletedSongsList(List<Song> songs) {
        //patch de doublons old songs
        List<Song> listRestOldSong = new ArrayList<>();
        List<Song> oldTest = new ArrayList<>(oldSongs);

        for(Song song : oldTest){
            for(Song song1 :oldTest){
                Log.d(LOG_TAG, "CR deletedSongsList: song old "+ song.getSourceSongTitre()+" "+song.getSongIdCloud()+" "+song.getSongId());
                if(song.getSongIdCloud()!=null) {
                    if (song.getSongIdCloud().equals(song1.getSongIdCloud()) && song.getSongId() != song1.getSongId()) {
                        Log.d(LOG_TAG, "CR deletedSongsList: song old 1 " + song.getSourceSongTitre() + " " + song.getSongIdCloud() + " " + song.getSongId());
                        if (!listRestOldSong.contains(song1)) {
                            Log.d(LOG_TAG, "CR deletedSongsList: song old 2 " + song.getSourceSongTitre() + " " + song.getSongIdCloud() + " " + song.getSongId());
                            listRestOldSong.add(song);
                            break;
                        }
                    }
                }
            }
        }

        for(Song song:listRestOldSong){
            oldTest.remove(song);
        }

//        Log.d(LOG_TAG, "CR deletedSongsList: "+oldTest.size()+" "+oldTest.get(0).getSongId());

        for(Song song :oldTest){
           // Log.d(LOG_TAG, "CR deletedSongsList: liste restante "+song.getSongId()+" "+song.getSourceSongTitre()+" "+song.getPupitre());
        }

        deletedSongsList.addAll(listRestOldSong);

        //patch problèmes sur les songs qui n'ont pas le bon Id
        for(Song song:oldSongs){
            for(Song song1:songs){
                if(song.getSongIdCloud()!=null) {
                    if (song.getSourceSongTitre().equals(song1.getSourceSongTitre()) && !song.getSongIdCloud().equals(song1.getSongIdCloud()) && song.getPupitre() == song1.getPupitre()) {
                        deletedSongsList.add(song);
                        Log.d(LOG_TAG, "CR deletedSongsList: patch id et titre diff activé " + song.getSourceSongTitre() + " " + song.getSongIdCloud() + " " + song.getPupitre());
                    }
                }
            }
        }
        Log.d(LOG_TAG, "CR deletedSongsList: intermédiaire 1 "+deletedSongsList.size());


        //gestion normal des deleted songs
        List<String> tempIdSongs = new ArrayList<>(listOldIdSongs);

        tempIdSongs.removeAll(listIdSongs);

        for(String idStr:tempIdSongs){
            int indexidStr = listOldIdSongs.indexOf(idStr);
            deletedSongsList.add(oldSongs.get(indexidStr));
        }

        Log.d(LOG_TAG, "CR deletedSongsList: 1er bilan song "+deletedSongsList.size());
        for (Song song:deletedSongsList) {
            Log.d(LOG_TAG, "CR deletedSongsList: intermédiaire "+song.getSourceSongTitre()+" "+song.getUpdatePhoneMp3());
            if(song.getUpdatePhoneMp3()!=null){
                deletedMp3SongsList.add(song);
            }
        }
        Log.d(LOG_TAG, "CR deletedSongsList : finalMP3 "+deletedMp3SongsList);
    }

    private void deletedSourceSongsList(List<SourceSong> sources) {

        List<String> tempIdSourceSongs = new ArrayList<>(listOldIdSourceSongs);

        tempIdSourceSongs.removeAll(listIdSourceSongs);

        for(String idStr:tempIdSourceSongs){
            int indexidStr = listOldIdSourceSongs.indexOf(idStr);
            deletedSourceSongsList.add(oldSourcesSongs.get(indexidStr));
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
                    if(oldSong.getSongIdCloud()!=null) {
                        if (oldSong.getSongIdCloud().equals(song.getSongIdCloud())) {
                            if (oldSong.getUpdatePhone().getTime() < song.getUpdatePhone().getTime()) {
                                modifiedSongsList.add(song);
                                if (!oldSong.getUrlCloudMp3().equals(song.getUrlCloudMp3())) {
                                    mp3SongsToDelete.add(oldSong);
                                    mp3SongsToDownload.add(song);
                                }
                                if (!oldSong.getSourceSongTitre().equals(song.getSourceSongTitre())) {
                                    modifiedSongsTitresList.add(oldSong);
                                }
                                titress.put(song.getSourceSongTitre(), oldSong.getSourceSongTitre());
                            }
                        }
                    }
                }
            }
        }
        Log.d(LOG_TAG, "CR modifiedSongsList: "+modifiedSongsList.size()+" "+modifiedSongsTitresList.size());

        for (Song song : modifiedSongsList){
            Log.d(LOG_TAG, "CR modifiedSongsList: liste des modifiés "+song.getSourceSongTitre()+" "+song.getPupitre()+" "+song.getUpdatePhone());
        }


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

                    if(oldSource.getUpdatePhone().getTime()<source.getUpdatePhone().getTime()){
                        modifiedSourceSongsList.add(source);
                        if(!oldSource.getUrlCloudBackground().equals(source.getUrlCloudBackground())){
                            bgSourcesToDelete.add(oldSource);
                            bgSourcesToDownLoad.add(source);
                            Log.d(LOG_TAG, "CR modifiedSourcesSongsList:  delete download ");
                        }
                        titres.put(source.getTitre(),oldSource.getTitre());
                    }
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

        //patch problèmes sur les songs qui n'ont pas le bon Id
       for(Song song:oldSongs){
           if(!modifiedSongsTitresList.contains(song)) {
               for (Song song1 : songs) {
                   if(song.getSongIdCloud()!=null) {
                       if (!song.getSourceSongTitre().equals(song1.getSourceSongTitre()) && song.getSongIdCloud().equals(song1.getSongIdCloud())) {
                           newSongsList.add(song1);
                           Log.d(LOG_TAG, "CR newSongsList: patch id et titre diff activé " + song1.getSourceSongTitre() + " " + song1.getSongIdCloud() + " " + song1.getPupitre());
                       }
                   }
               }
           }else{
               Log.d(LOG_TAG, "CR newSongsList:  titres modifiés ne rentrent pas dans ce cas ");
           }
        }
        Log.d(LOG_TAG, "CR newSongsList: intermédiaire 1 "+newSongsList.size());


        //gestion normal

        Log.d(LOG_TAG, "CR newSongsList: décompte "+oldSongs.size()+" "+ songs.size());

        List<String> tempIdSongs = new ArrayList<>(listIdSongs);

        tempIdSongs.removeAll(listOldIdSongs);

        Log.d(LOG_TAG, "CR newSongsList: tempIDsong "+tempIdSongs.size()+" "+tempIdSongs);

        for(String idStr:tempIdSongs){
            int indexidStr = listIdSongs.indexOf(idStr);
            newSongsList.add(songs.get(indexidStr));
        }

        Log.d(LOG_TAG, "CR newSongsList:  size "+newSongsList.size());

    }

    private void newSourceSongsList(List<SourceSong> sources) {

        List<String> tempIdSourceSongs = new ArrayList<>(listIdSourceSongs);

        tempIdSourceSongs.removeAll(listOldIdSourceSongs);

        Log.d(LOG_TAG, "CR newSourceSongsList: tempIDSS "+tempIdSourceSongs.size());

        for(String idStr:tempIdSourceSongs){
            int indexidStr = listIdSourceSongs.indexOf(idStr);
            newSourceSongsList.add(sources.get(indexidStr));
        }
        Log.d(LOG_TAG, "CR newSourceSongsList: "+ newSourceSongsList.size());
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

        if(newSongsList!=null& Objects.requireNonNull(newSongsList).size()!=0){
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
        Log.d(LOG_TAG, "CR DoWorkInLocalStorage: delete SS et bg"+deletedSourceSongsList+" "+bgSourcesToDelete);

        //delete songs
        if(deletedSongsList!=null&&deletedSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInLocalStorage: deletedSongs "+deletedSongsList);
            totalMp3ToDelete.addAll(deletedSongsList);
        }
        if(mp3SongsToDelete!=null&&mp3SongsToDelete.size()!=0) {
            Log.d(LOG_TAG, "CR DoWorkInLocalStorage: mp3SongsTodelete "+mp3SongsToDelete);
           totalMp3ToDelete.addAll(mp3SongsToDelete);
        }
        Log.d(LOG_TAG, "CR DoWorkInLocalStorage: delete songs et mp3"+deletedSongsList+" "+mp3SongsToDelete);

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
        Log.d(LOG_TAG, "CR DoWorkInRoom: entrée ");
        if(deletedSongsList!=null&&deletedSongsList.size()!=0){
            int temp = mSongDao.deleteSongs(deletedSongsList);
            Log.d(LOG_TAG, "CR DoWorkInRoom: deletesongs "+temp);
        }
        if(deletedSourceSongsList!=null&&deletedSourceSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: delete sourcesongs");
            mSourceDao.deleteSourceSongs(deletedSourceSongsList);
        }

        if(deletedSaisonsList!=null&&deletedSaisonsList.size()!=0){
            int temp = mSaisonDao.deleteSaisons(deletedSaisonsList);
            Log.d(LOG_TAG, "CR DoWorkInRoom: delete saisons "+ temp);
        }

        if(deletedSpectaclesList!=null&&deletedSpectaclesList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: avant "+sharedPreferences.getBoolean("spectacleDeleted",false));
            int temp = mSpectacleDao.deleteSpectacles(deletedSpectaclesList);
            Log.d(LOG_TAG, "CR DoWorkInRoom: delete spectacles "+ temp);
            String currentSpectacle = sharedPreferences.getString("currentSpectacle","Tous");
            for(Spectacle spectacle:deletedSpectaclesList){
                Log.d(LOG_TAG, "CR DoWorkInRoom: boucle deleted "+ spectacle.getSpectacleName());
                if(Objects.equals(currentSpectacle, spectacle.getSpectacleName())){
                    Log.d(LOG_TAG, "CR DoWorkInRoom:spectacle deleted "+currentSpectacle);
                    editor =sharedPreferences.edit();
                    editor.putBoolean("spectacleDeleted", true);
                    editor.apply();
                    Log.d(LOG_TAG, "CR DoWorkInRoom: après "+sharedPreferences.getBoolean("spectacleDeleted",false));
                }
            }
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
                //relance la bête pour s'afficher
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
                    Log.d(LOG_TAG, "CR DoWorkInRoom: temp song "+tempSong+" "+tempSong.getUpdatePhone()+" "+song.getUpdatePhone());
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

            Log.d(LOG_TAG, "CR DoWorkInRoom: tempsongs "+tempSongs.size());

            if(tempSongs!=null) {
               // int tempInt = mSongDao.updatesSongs(tempSongs);
               // Log.d(LOG_TAG, "CR synchronisationLocalDataBase: nb d'update Songs "+tempInt);


                for(Song song : tempSongs){
                    int tempInt = mSongDao.updateSong(song);
                    Log.d(LOG_TAG, "DoWorkInRoom: "+tempInt);
                }
            }


            List<Song> newSongsList1 = new ArrayList<>();

            newSongsList1 = mSongDao.getAllSongs();

            for(Song song:newSongsList1){
                Log.d(LOG_TAG, "CR ChoraleRepository: new Songs "+song.getSourceSongTitre()+" "+song.getPupitre()+" "+song.getSongIdCloud()+" "+song.getUpdatePhone());
            }
//            Log.d(LOG_TAG, "CR DoWorkInRoom: résultat "+ mSongDao.getSongsByTitrePupitreSource("Another Brick in the Wall",Pupitre.ALTO,RecordSource.BANDE_SON).getUpdatePhone());
        }

        if(modifiedSaisonsList!=null&&modifiedSaisonsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: modified Saisons");

            for(Saison saison:modifiedSaisonsList){
                Saison tempSaison = mSaisonDao.getSaisonById(saison.getIdsaisonCloud());
                if(tempSaison!=null){
                    Log.d(LOG_TAG, "CR DoWorkInRoom: temp saison "+tempSaison.getSaisonName());
                    tempSaison.setSaisonName(saison.getSaisonName());
                    tempSaison.setUpdatePhone(saison.getUpdatePhone());
                    tempSaison.setIdSpectacles(saison.getIdSpectacles());
                    tempSaisons.add(tempSaison);

                }else{
                    Log.d(LOG_TAG, "CR DoWorkInRoom: pb sur tempsaisons");
                }

            }

            if(tempSaisons!=null){
                int tempInt = mSaisonDao.upDateSaisons(tempSaisons);
                Log.d(LOG_TAG, "CR DoWorkInRoom: nb update saisons "+tempInt);
            }
        }

        if(modifiedSpectaclesList!=null&&modifiedSpectaclesList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: modified Spectacles");

            for(Spectacle spectacle: modifiedSpectaclesList){
                Spectacle tempSpectacle = mSpectacleDao.getSpectacleById(spectacle.getIdSpectacleCloud());
                if(tempSpectacle!=null){
                    Log.d(LOG_TAG, "CR DoWorkInRoom: tempspectacle "+tempSpectacle);
                    tempSpectacle.setUpdatePhone(spectacle.getUpdatePhone());
                    tempSpectacle.setSpectacleName(spectacle.getSpectacleName());
                    tempSpectacle.setIdTitresSongs(spectacle.getIdTitresSongs());
                    tempSpectacle.setSpectacleLieux(spectacle.getSpectacleLieux());
                    tempSpectacle.setSpectacleDates(spectacle.getSpectacleDates());
                    tempSpectacles.add(tempSpectacle);
                }

                if(tempSpectacles!=null){
                    int tempInt = mSpectacleDao.upDateSpectacles(tempSpectacles);
                    Log.d(LOG_TAG, "CR DoWorkInRoom: nb updates spectacles "+ tempInt);
                }
            }
        }

        if(newSourceSongsList!=null&&newSourceSongsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: create sourcesongs "+newSourceSongsList);
            mSourceDao.bulkInsert(newSourceSongsList);
        }


        if(newSongsList!=null&newSongsList.size()!=0){
            mSongDao.bulkInsert(newSongsList);
            Log.d(LOG_TAG, "CR DoWorkInRoom: create songs "+newSongsList);
        }

        if(newSaisonsList!=null&&newSaisonsList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: new Saisons "+ newSaisonsList);
            mSaisonDao.bulkInsert(newSaisonsList);
        }

        if(newSpectaclesList!=null&&newSpectaclesList.size()!=0){
            Log.d(LOG_TAG, "CR DoWorkInRoom: new Spectacles "+newSpectaclesList);
            mSpectacleDao.bulkInsert(newSpectaclesList);
        }

        //chercher les Sourcesongs sur Room
        sourceSongsAfterSync=mSourceDao.getAllSources();
        songsAfterSync=mSongDao.getAllSongs();
    }


    private void LoadMajCloudDB() {
        mChoraleNetworkDataSource.getMajDateCloudDataBase();
    }

    private void getMajDateLocalDataBase() {

        majLocalDBLong =sharedPreferences.getLong("majDB",0);
        Log.d(LOG_TAG, "CR : datelong : local "+new Date(majLocalDBLong)+ " comparaison Long-local/Cloud "+ majLocalDBLong+" "+majCloudDBLong);

    }

    private void startFetchSongsService() {
        Log.d(LOG_TAG, "CR repo startService: début");
        mChoraleNetworkDataSource.startFetchSongsService();
        Log.d(LOG_TAG, "CR repo startService: fin ");
    }


    public LiveData<List<SourceSong>> getSourceSongs() {
        Log.d(LOG_TAG, "CR getSourceSongs: avant initialized data "+ Thread.currentThread().getName());

        initializeData();


        Log.d(SongsAdapter.TAG, "CR getSourceSongs: repository après iniatialize data");
        return mSourceDao.getAllSourceSongs();
    }

    private synchronized void initializeData() {
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

            Log.d(LOG_TAG, "CR : isFetchNeeded 2 "+ Thread.currentThread().getName());
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
                DoSynchronization(oldSourcesSongs,oldSongs);
                Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : données initiales "+oldSourcesSongs+" "+oldSongs);
            }else{
                Log.d(LOG_TAG, "CR run: getOldSongs et SourcesSongs : pas de données initiales ");
            }
            Log.d(LOG_TAG, "CR ChoraleRepository: Stop startFectch pas lancé !");
        }
    }

    private boolean isFetchNeeded() {
        boolean isAuto = sharedPreferences.getBoolean(context.getString(R.string.maj_auto), true);
        Log.d(LOG_TAG, "CR isFetchNeeded: condition "+ isAuto);
        return isAuto;
    }

    public List<Song> getSongs() {
        Log.d(SongsAdapter.TAG, "CR getSongs: repository ");
        return  mSongDao.getAllSongs();
    }


    private String getCurrentPupitreStr() {
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

    public Thread getThreadSaisons() {
        return threadSaisons;
    }

    public class ComparatorSaison implements Comparator<Saison> {

        public ComparatorSaison() {
        }

        @Override
        public int compare(Saison saison, Saison saison1) {
            String saisonStr = String.valueOf(saison.getSaisonId());
            String saisonStr1 = String.valueOf(saison1.getSaisonId());
            return saisonStr1.compareTo(saisonStr);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return false;
        }
    }

}


