package dedicace.com.data.networkdatabase;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.AppExecutors;
import dedicace.com.R;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;
import dedicace.com.ui.SongsAdapter;

public class ChoraleNetWorkDataSource {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "coucou" ;
    private static ChoraleNetWorkDataSource sInstance;
    private final Context mContext;

    // LiveData storing the latest downloaded weather forecasts
    private AppExecutors mExecutors;
    // LiveData storing the latest downloaded weather forecasts
    //private final MutableLiveData<List<Song>> mDownloadedSongs;
    private final MutableLiveData<List<SourceSong>> mDownloaderSourceSongs;

    public static AppDataBase choeurDataBase;
    List<SourceSong> sourceSongs = new ArrayList<>();
    List<Song> songs = new ArrayList<>();
    Song song3, song4, song6, song5,song2;
    private SourceSong sourceSong1,sourceSong2,sourceSong3,sourceSong4,sourceSong5,sourceSong6, sourceSong7, sourceSong8;
    private Pupitre recordPupitre=Pupitre.NA;

    private ChoraleNetWorkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        //mDownloadedSongs = new MutableLiveData<>();
        mDownloaderSourceSongs = new MutableLiveData<>();
        Log.d("coucou", "NetworkDataSource: constructor ");


    }

    /**
     * Get the singleton for this class
     */
    public static ChoraleNetWorkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        Log.d("coucou", "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleNetWorkDataSource(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
                Log.d("coucou", "Made new network data source");
            }
        }
        return sInstance;
    }

   /* public LiveData<List<Song>> getCurrentSongs() {

        Log.d(SongsAdapter.TAG, "getCurrentSongs: ");
        return mDownloadedSongs;
    }*/

    public LiveData<List<SourceSong>> getSourceSongs() {
        Log.d("coucou", "network getSourceSongs: ");
        return mDownloaderSourceSongs;
    }


    public void startFetchSongsService() {
        Intent intentToFetch = new Intent(mContext, ChoraleSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "Service created");
        Log.d(LOG_TAG, "Service created");

    }

    public void fetchSongs() {
        Log.d("coucou", "network fetch started: ");
        initData();
        Log.d(SongsAdapter.TAG, "fetchSongs: " + sourceSongs.get(0).getTitre());
        mDownloaderSourceSongs.postValue(sourceSongs);
        Log.d("coucou", "fetchSongs: après post");
        //mDownloadedSongs.postValue(songs);
    }

    private void initData() {
        Log.d("coucou", "initData: ");
        choeurDataBase = AppDataBase.getInstance(mContext);
        //todo voir pour les migrations et fallBackTomigration
        mExecutors=AppExecutors.getInstance();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                choeurDataBase.songsDao().deleteAll();
                choeurDataBase.sourceSongDao().deleteAll();
            }
        });

        sourceSong1 = new SourceSong("Des hommes pareils","Francis Cabrel",321,R.drawable.hand,"");
        sourceSong2 = new SourceSong("L'un pour l'autre","Maurane",266,R.drawable.yinyang,"");
        sourceSong7 = new SourceSong("North Star","Philip Glas",160,R.drawable.etoile,"");

        sourceSongs.add(sourceSong1);
        sourceSongs.add(sourceSong2);
        sourceSongs.add(sourceSong7);


        String titreSourceSong1 = sourceSong1.getTitre();
        String titreSourceSong2 = sourceSong2.getTitre();
        String titreSourceSong7 = sourceSong7.getTitre();

        song2 = new  Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.BASS,"des_hommes_pareils_basse");
        song3 = new Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.TENOR,"des_hommes_pareils_tenor");
        song4 = new Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.ALTO,"des_hommes_pareils_alto");
        song5 = new  Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.SOPRANO,"des_hommes_pareils_soprano");
        song6 = new Song(titreSourceSong2,RecordSource.BANDE_SON,Pupitre.BASS,"l_un_pour_l_autre_basse");

        songs.add(song3);
        songs.add(song4);
        songs.add(song2);
        songs.add(song5);
        songs.add(song6);
        // choeurDataBase.songsDao().bulkInsert(songsEssai);

       /* mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                choeurDataBase.songsDao().deleteAll();
                choeurDataBase.sourceSongDao().deleteAll();

                sourceSong1 = new SourceSong("Des hommes pareils","Francis Cabrel",321,R.drawable.hand,"");
                sourceSong2 = new SourceSong("L'un pour l'autre","Maurane",266,R.drawable.yinyang,"");
                sourceSong7 = new SourceSong("North Star","Philip Glas",160,R.drawable.etoile,"");

                sourceSongs.add(sourceSong1);
                sourceSongs.add(sourceSong2);
                sourceSongs.add(sourceSong7);
                choeurDataBase.sourceSongDao().bulkInsert(sourceSongs);

                List<SourceSong> sourceEssai = choeurDataBase.sourceSongDao().getAllSourceSongs();
                for (SourceSong source: sourceEssai) {

                    Log.d(TAG, "run MA: " + source.getTitre()+" "+source.getSourceSongId());

                }
                int intSourceSong1 = choeurDataBase.sourceSongDao().getSourceSongByTitre("Des hommes pareils").getSourceSongId();
                int intSourceSong7 = choeurDataBase.sourceSongDao().getSourceSongByTitre("North Star").getSourceSongId();
                int intSourceSong2 = choeurDataBase.sourceSongDao().getSourceSongByTitre("L'un pour l'autre").getSourceSongId();

                Log.d(TAG, "run: MA1 "+ intSourceSong1);
                Log.d(TAG, "run: MA1 " + intSourceSong2);
                Log.d(TAG, "run: MA1 " + intSourceSong7);


                song3 = new Song(intSourceSong1,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.des_hommes_pareils_tenor");
                song4 = new Song(intSourceSong1,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.des_hommes_pareils_alto");
                song6 = new Song(intSourceSong2,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.l_un_pour_l_autre_basse");

                songsEssai.add(song3);
                songsEssai.add(song4);
                songsEssai.add(song6);
                choeurDataBase.songsDao().bulkInsert(songsEssai);

                List<Song> songsA = choeurDataBase.songsDao().getAllSongs();
                Log.d(TAG, "run: MA2 " + songsA.get(0).getSourceSongId());
                Log.d(TAG, "run: MA2 " + songsA.get(1).getSourceSongId());
                Log.d(TAG, "run: MA2 " + songsA.get(2).getSourceSongId());

            }
        });*/

        /*

        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                sourceSong1 = new SourceSong("Des hommes pareils","Francis Cabrel",321,R.drawable.hand,"");
                sourceSong2 = new SourceSong("L'un pour l'autre","Maurane",266,R.drawable.yinyang,"");
                sourceSong3 = new SourceSong("L'eau","Jeanne Cherhal",143,R.drawable.water,"");
                sourceSong4 = new SourceSong("Le tissu","Jeanne Cherhal",236,R.drawable.femme_tissu,"");
                sourceSong5 = new SourceSong("Papaoutai","Stromae",232,R.drawable.papa,"");
                sourceSong6 = new SourceSong("Recitation 11","Georges Aperghis",243,R.drawable.pyramide_texte,"");
                sourceSong7 = new SourceSong("North Star","Philip Glas",160,R.drawable.etoile,"");
                sourceSong8 = new SourceSong("Tout va bien","Inconnu",175,R.drawable.hommes_pareils,"");

                Song song1 = new Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.TUTTI,"R.raw.des_hommes_pareils_tutti");
                Song song2 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.des_hommes_pareils_basse");
                Song song3 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.des_hommes_pareils_tenor");
                Song song4 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.des_hommes_pareils_alto");
                Song song5 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.SOPRANO,"R.raw.des_hommes_pareils_soprano");
                Song song6 = new Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.l_un_pour_l_autre_basse");
                Song song7 = new  Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.l_un_pour_l_autre_tenor");
                Song song8 = new  Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.l_un_pour_l_autre_alto");
                Song song9 = new  Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.SOPRANO,"R.raw.l_un_pour_l_autre_soprano");
                Song song10 = new  Song(sourceSong3,RecordSource.BANDE_SON,Pupitre.TUTTI,"R.raw.l_eau_tutti");
                Song song11 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.le_tissu_basse");
                Song song12 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.le_tissu_tenor");
                Song song13 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.le_tissu_alto");
                Song song14 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.SOPRANO,"R.raw.le_tissu_soprano");
                Song song15 = new  Song(sourceSong8,RecordSource.BANDE_SON,Pupitre.SOPRANO,"R.raw.tout_va_bien_soprano");
                Song song16 = new  Song(sourceSong8,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.tout_va_bien_alto");
                Song song17 = new  Song(sourceSong8,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.tout_va_bien_tenor");
                Song song18 = new  Song(sourceSong8,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.tout_va_bien_basse");

                songs.add(sourceSong1);
                songs.add(sourceSong2);
                songs.add(sourceSong3);
                songs.add(sourceSong4);
                songs.add(sourceSong8);
                songs.add(sourceSong5);
                songs.add(sourceSong6);
                songs.add(sourceSong7);
                choeurDataBase.songsDao().insertSongs(song1,song2,song3,song4,song5,song6,song7,song8,song9,song10,song11,song12,song13,song14,song15,song16,song17,song18);
            }
        });*/

    }



}
