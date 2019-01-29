package dedicace.com.data;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.ArrayList;
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

            Log.d(LOG_TAG, "CR Repository: observers Alerte cela bouge !"+sourceSongs1+Thread.currentThread().getName());
            songs = choraleNetworkDataSource.getSongs();
            Log.d(LOG_TAG, "CR ChoraleRepository LiveData: songsOnCloud " +songs.size());


            listSongs= new ListSongs(mSongDao,mSourceDao,sourceSongs,songs);

            listSongs.getSongOnClouds();

           // getSongOnClouds(sourceSongs);

            mExecutors.diskIO().execute(new Runnable() {
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
                        Log.d(LOG_TAG, "CR run: sourcesSONG dans la data "+source.getTitre());
                    }

                    listSongs.getSongOnPhoneBS(sourceSongsAfterSync);

                   // getSongOnPhoneBS();

                    listSongs.getSongToPlaysBs();
                    //getSongToPlaysBs();

                    listSongs.getRecordSources();

                    //getRecordSources();

                    listSongs.getSongToPlays();
                    listSongs.getSongsOnPhones();
                    //listSongs.getSongOnClouds();

                    Log.d(LOG_TAG, "CR ChoraleRepository LiveData après tout : "+Thread.currentThread().getName());
                }
            });

        });
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

    private void getSongOnPhoneBS() {
        //chercher les SongsOnPhoneBS after Sync
        for (SourceSong sourceSong: sourceSongsAfterSync){
            List<Song> listBs = mSongDao.getSongsOnPhone(sourceSong.getTitre(),RecordSource.BANDE_SON);

            Log.d(LOG_TAG, "CR run: "+ listBs.size());

            if(listBs!=null&&listBs.size()!=0){
                SongOnPhonesBS.add(listBs);
                Log.d(LOG_TAG, "CR run A: "+ SongOnPhonesBS);

            }else{
                SongOnPhonesBS.add(null);
                Log.d(LOG_TAG, "CR run B: "+ SongOnPhonesBS);
            }
        }

        Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync SongOnPhonesBS: "+SongOnPhonesBS.size()+Thread.currentThread().getName());
        for (int i = 0; i <SongOnPhonesBS.size() ; i++) {
            Log.d(LOG_TAG, "CR run: "+ SongOnPhonesBS.get(i));
        }
    }

    private void getSongOnClouds(List<SourceSong> sourceSongs) {
        //chercher les SongsOnCloud BS
        for(SourceSong sourceSong:sourceSongs){
            List<Song> listSongbyTitre = new ArrayList<>();

            for(Song song : songs){

                if(song.getSourceSongTitre().equals(sourceSong.getTitre())){
                    listSongbyTitre.add(song);
                }
            }

            SongOnClouds.add(listSongbyTitre);
        }

        Log.d(LOG_TAG, "CR ChoraleRepository LiveData: ListSongOnCloud "+ SongOnClouds.size()+ " songs "+songs.size()+Thread.currentThread().getName());
    }

    private void synchronisationLocalDataBase(List<SourceSong> sourceSongs) {
        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database avant "+Thread.currentThread().getName());
        //todo à voir si on change cette Méthode brute pas économique (?) on met à jour les données peu importe si elles existent ou pas.
        mSourceDao.bulkInsert(sourceSongs);
        mSongDao.bulkInsert(songs);
        Log.d(SongsAdapter.TAG, "CR run-exec: sourceSongs dans la database après A "+ sourceSongs.size()+" "+songs.size()+Thread.currentThread().getName());
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

                getSongOnPhoneLive();

                getSongToPlayLive();

            }
        });

        if (isFetchNeeded()) {
            currentPupitreStr=getCurrentPupitreStr();
            Log.d(LOG_TAG, "CR run: isFetchNeeded "+ currentPupitreStr);
            startFetchSongsService();

            Log.d(LOG_TAG, "CR run: isFetchNeeded "+ Thread.currentThread().getName());
        }
    }

    private void getSongToPlayLive() {
        //chercher la liste des songs To play pour le LIVE
        for (List<Song> songs:SongOnPhonesLive){

            if(songs==null || songs.size()==0){
                songToPlaysLive.add(null);
            }else{
                songToPlaysLive.add(songs.get(0));
            }
        }
        Log.d(LOG_TAG, "CR run initialize Data songToPlaysLive: "+songToPlaysLive.size());
    }

    private void getSongOnPhoneLive() {
        //chercher les SongsOnPhoneLive before Sync
        for (SourceSong sourceSong: sourceSongsBeforeSync){
            SongOnPhonesLive.add(mSongDao.getSongOrderedByPupitre(sourceSong.getTitre(),RecordSource.LIVE));
        }
        Log.d(LOG_TAG, "CR run initialize Data SongOnPhonesLive : "+SongOnPhonesLive.size());
    }

    private boolean isFetchNeeded() {
        //todo à modifier éventuellement sur préférences veut - on regarder si on veut télécharger automatique ou non (à la demande)
        Log.d(LOG_TAG, "CR isFetchNeeded: condition ");
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

    private RecordSource getRecordSource(List<RecordSource> recordSources) {
        RecordSource source =RecordSource.NA;

        if(recordSources.size()==2){
            source=RecordSource.BANDE_SON;
        }else if(recordSources.size()==1){
            if(recordSources.get(0)==RecordSource.BANDE_SON){
                source=RecordSource.BANDE_SON;
            }else if(recordSources.get(0)==RecordSource.LIVE){
                source=RecordSource.LIVE;
            }else if(recordSources.get(0)==RecordSource.NA){
                source=RecordSource.NA;
            }
        }

        return source;
    }

    public List<RecordSource> getRecordSources(String titre) {

        Log.d(LOG_TAG, "CR getRecordSources: avec titre "+Thread.currentThread().getName());

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

        Log.d(LOG_TAG, "CR getRecordSources titre : "+sources+ " "+ titre+Thread.currentThread().getName());
        return sources;
    }

    public List<List<RecordSource>> getRecordSources() {
        Log.d(LOG_TAG, "CR getRecordSources: list de list "+sourceSongs1.size()+Thread.currentThread().getName());

        if(sourceSongs1!=null){
            for (SourceSong sourceSong : sourceSongs1) {
                String titre = sourceSong.getTitre();
                RecordSources.add(getRecordSources(titre));
            }
            Log.d(SongsAdapter.TAG, "CR run: sourceSongs dans la database après B "+RecordSources.size());
            getSongToPlays();
            getSongsOnPhones();
            getSongsOnClouds();

            /*for (SourceSong sourceSong : sourceSongs1) {
               sourceSong.setUpdatePhone(new Date(System.currentTimeMillis()));
               mSourceDao.updateSourceSong(sourceSong);
                Log.d(LOG_TAG, "CR getRecordSources: update SS "+ sourceSong.getTitre());
            }*/
        }

        return RecordSources;
    }


    public Song getFirstSongPlayed(String titre, RecordSource recordSource) {

        listOrderByPupitre = mSongDao.getSongOrderedByPupitre(titre, recordSource);

        if (recordSource != RecordSource.NA) {
            firstSongPlayed = listOrderByPupitre.get(0);
        } else {
            firstSongPlayed = null;
        }

        if (firstSongPlayed != null) {
            Log.d(LOG_TAG, "CR getFirstSongPlayed: " + firstSongPlayed.getSourceSongTitre());
        }else{
            Log.d(LOG_TAG, "CR getFirstSongPlayed: pas de chanson");
        }

        return firstSongPlayed;
    }

    public List<Song> getSongToPlaysLive() {
        return songToPlaysLive;
    }

    public List<Song> getSongToPlays() {
        Log.d(LOG_TAG, "CR getSongToPlays: "+Thread.currentThread().getName());

        for (int i = 0; i < RecordSources.size(); i++) {

            //todo voir si inutilité d'avoir une list pour recordToPlays puisque local
            if(RecordSources.get(i).get(0)==RecordSource.NA){
                recordToPlays.add(RecordSource.NA);
            }else if(RecordSources.get(i).get(0)==RecordSource.LIVE){
                recordToPlays.add(RecordSource.LIVE);
            }else{
                recordToPlays.add(RecordSource.BANDE_SON);
            }

            List<Song> listSongs = mSongDao.getSongOrderedByPupitre(sourceSongs1.get(i).getTitre(),recordToPlays.get(i));

            if(listSongs!=null&&listSongs.size()!=0) {
                songToPlays.add(listSongs.get(0));
            }else{
                songToPlays.add(null);
            }

//            Log.d(LOG_TAG, "getSongToPlays: " + songToPlays.get(i).getSourceSongTitre() + " " +songToPlays.get(i).getPupitre() );
        }
        Log.d(LOG_TAG, "CR getSongToPlays: après calcul "+songToPlays+Thread.currentThread().getName());
        return songToPlays;
    }

    public List<Song> getSongsOnPhone(String titre, RecordSource source){

        listSongsOnPhone=mSongDao.getSongsOnPhoneA(titre,source);

        return listSongsOnPhone;

    }

    public List<List<Song>> getSongsOnPhones() {
        Log.d(LOG_TAG, "CR getSongsOnPhones: (recordSourcesSize) "+RecordSources.size()+Thread.currentThread().getName());

        for (int i = 0; i < RecordSources.size(); i++) {
            Log.d(LOG_TAG, "CR getSongsOnPhones: RecordSpources geti get0 "+RecordSources.size()+" "+RecordSources.get(i).get(0)+Thread.currentThread().getName());

            if(RecordSources.get(i).get(0)==RecordSource.NA){
                SongsOnPhones.add(null);
            }else if(RecordSources.get(i).get(0)==RecordSource.LIVE){
                if(SongOnPhonesLive.size()!=0) {
                    SongsOnPhones.add(SongOnPhonesLive.get(i));
                }else{
                    SongsOnPhones.add(null);
                }
            }else{
                if(SongOnPhonesBS.size()!=0) {
                    SongsOnPhones.add(SongOnPhonesBS.get(i));
                }else{
                    SongsOnPhones.add(null);
                }
            }

        }

        Log.d(LOG_TAG, "CR getSongsOnPhones: (SongOnPhones) "+SongsOnPhones.size()+Thread.currentThread().getName());

        return SongsOnPhones;
    }

    public List<List<Song>> getSongsOnClouds(){

        Log.d(LOG_TAG, "CR getSongsOnClouds: "+Thread.currentThread().getName());

        return SongOnClouds;
    }

    public List<Song> getSongsOnCloud(String titre, RecordSource source){

        List<Song> listSongsOnCloud;

        listSongsOnCloud=mSongDao.getSongsOnCloud(titre,source);

        return listSongsOnCloud;

    }

    public String getCurrentPupitreStr() {

        currentPupitreStr=mChoraleNetworkDataSource.getCurrentPupitreStr();
        return currentPupitreStr;
    }

    public List<Object> getElements() {
        return listElements;

    }

    public List<List<Song>> getSongOnPhonesBS() {
        return SongOnPhonesBS;
    }

    public List<List<Song>> getSongsOnPhonesLive(){

        return SongOnPhonesLive;
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
}


