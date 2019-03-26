package dedicace.com.data.database;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import dedicace.com.ui.SongsAdapter;

public class ListSongs {

    private final List<List<Song>> SongsOnPhones= new ArrayList<>();
    private final List<List<Song>> SongOnClouds=new ArrayList<>();
    private final List<List<RecordSource>> RecordSources=new ArrayList<>();
    private final List<Song> songToPlays=new ArrayList<>();
    private List<List<Song>> SongOnPhonesLive= new ArrayList<>();

    private List<List<Song>> SongOnPhonesBS= new ArrayList<>();
    private List<Song> songToPlaysLive=new ArrayList<>();
    private List<Song> songToPlaysBs=new ArrayList<>();
    private List<RecordSource> recordToPlays=new ArrayList<>();
    private SongsDao mSongDao;
    private SourceSongDao mSourceSongDao;
    private List<SourceSong> sourceSongs;
    private List<Song> songs;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final String LOG_TAG = "coucou";


    public ListSongs(SongsDao mSongDao, SourceSongDao mSourceSongDao, List<SourceSong> sourceSongs, List<Song> songs) {
        this.mSongDao = mSongDao;
        this.mSourceSongDao = mSourceSongDao;
        this.sourceSongs = sourceSongs;
        this.songs = songs;
        Log.d(LOG_TAG, "LS CR ListSongs:constructeur A ");
    }

    public ListSongs(SongsDao mSongDao, SourceSongDao mSourceSongDao) {
        this.mSongDao=mSongDao;
        this.mSourceSongDao=mSourceSongDao;
        Log.d(LOG_TAG, "LS CR ListSongs:constructeur B ");
    }

    public void getSongOnClouds() {
        //chercher les SongsOnCloud BS

            for (SourceSong sourceSong : sourceSongs) {
                List<Song> listSongbyTitre = new ArrayList<>();

                for (Song song : songs) {
                     if (song.getSourceSongTitre().equals(sourceSong.getTitre())) {
                        listSongbyTitre.add(song);
                     }
                }
                if(listSongbyTitre.size()!=0) {
                    SongOnClouds.add(listSongbyTitre);
                }else{
                    SongOnClouds.add(null);
                }
            }

        Log.d(LOG_TAG, "CR ChoraleRepository LiveData: ListSongOnCloud "+ SongOnClouds.size()+ " songs "+songs.size()+" "+SongOnClouds+Thread.currentThread().getName());
    }

    public void getSongOnPhoneBS(List<SourceSong> sourceSongsAfterSync) {
        //chercher les SongsOnPhoneBS after Sync

        for (SourceSong sourceSong: sourceSongsAfterSync){
            List<Song> listBs = mSongDao.getSongsOnPhone(sourceSong.getTitre(),RecordSource.BANDE_SON);

            Log.d(LOG_TAG, "CR getSongOnPhoneBS: listBS "+ listBs.size());

            if(listBs!=null){
                for (int i = 0; i <listBs.size() ; i++) {
                    Log.d(LOG_TAG, "CR getSongOnPhoneBS: listBs songs "+listBs.get(i).getSourceSongTitre()+" "+listBs.get(i).getPupitre());
                }

            }

            if(listBs!=null&&listBs.size()!=0){
                SongOnPhonesBS.add(listBs);

            }else{
                SongOnPhonesBS.add(null);
            }
        }

        Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync SongOnPhonesBS: "+SongOnPhonesBS.size()+" "+Thread.currentThread().getName());
        for (int i = 0; i <SongOnPhonesBS.size() ; i++) {
            Log.d(LOG_TAG, "CR run: "+ SongOnPhonesBS.get(i));
        }
    }

    public void getSongToPlaysBs() {
        //chercher la liste des songs To play pour le BS
        for (List<Song> songs:SongOnPhonesBS){

            if(songs==null || songs.size()==0){
                songToPlaysBs.add(null);
            }else{
                songToPlaysBs.add(songs.get(0));
            }
        }

        Log.d(LOG_TAG, "CR ChoraleRepository LiveData après sync Song to Plays BS: "+songToPlaysBs.size()+" "+songToPlaysBs+Thread.currentThread().getName());
    }

    public  List<List<RecordSource>> getRecordSources() {
        Log.d(LOG_TAG, "CR getRecordSources: list de list "+sourceSongs.size()+Thread.currentThread().getName());

                for (SourceSong sourceSong : sourceSongs) {
                    String titre = sourceSong.getTitre();
                    RecordSources.add(getRecordSources(titre));
                }
                Log.d(SongsAdapter.TAG, "CR run: RecordSources après B " + RecordSources.size()+" "+RecordSources);

        return RecordSources;
    }

    public  List<RecordSource> getRecordSources(String titre) {


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

        Log.d(LOG_TAG, "CR getRecordSources titre : "+sources+ " "+ titre+" "+Thread.currentThread().getName());
        return sources;
    }

    public List<Song> getSongToPlays() {
        Log.d(LOG_TAG, "CR getSongToPlays: "+Thread.currentThread().getName());

          /*  for (int i = 0; i < RecordSources.size(); i++) {

                //todo voir si inutilité d'avoir une list pour recordToPlays puisque local
                if (RecordSources.get(i).get(0) == RecordSource.NA) {
                    recordToPlays.add(RecordSource.NA);
                } else if (RecordSources.get(i).get(0) == RecordSource.LIVE) {
                    recordToPlays.add(RecordSource.LIVE);
                } else {
                    recordToPlays.add(RecordSource.BANDE_SON);
                }

                Log.d(LOG_TAG, "LS CR getSongToPlays: recordToplays "+recordToPlays.get(i));


               List<Song> listSongs = mSongDao.getSongOrderedByPupitre(sourceSongs.get(i).getTitre(), recordToPlays.get(i));

                Log.d(LOG_TAG, "LS CR getSongToPlays: listSongs "+listSongs);

                if(i==2){
                    for (int j = 0; j <listSongs.size() ; j++) {
                        Log.d(LOG_TAG, "LS CR getSongToPlays: item 2 "+listSongs.get(j).getSourceSongTitre()+" "+listSongs.get(j).getPupitre());
                    }
                }

                if (listSongs != null && listSongs.size() != 0&&SongsOnPhones.get(i)!=null) {
                    songToPlays.add(listSongs.get(0));
                } else {
                    songToPlays.add(null);
                }

//            Log.d(LOG_TAG, "getSongToPlays: " + songToPlays.get(i).getSourceSongTitre() + " " +songToPlays.get(i).getPupitre() );


            }*/

        for (List<Song> songs:SongsOnPhones) {
                if(songs!=null){
                    songToPlays.add(songs.get(0));
                }else{
                    songToPlays.add(null);
                }
        }

//        Log.d(LOG_TAG, "CR getSongToPlays: après calcul "+" "+songToPlays.get(2).getSourceSongTitre()+" "+songToPlays.get(2).getPupitre()+Thread.currentThread().getName()+songToPlays);
        return songToPlays;
    }

    public  List<List<Song>> getSongsOnPhones() {
        Log.d(LOG_TAG, "CR getSongsOnPhones: (recordSourcesSize) "+RecordSources.size()+" "+Thread.currentThread().getName());


        /**
         * ce sont les songs On phones une fois que la RecordSource est déterminée, ce ne sont pas toutes les chansons sur le téléphone.
         */

        // synchronized (SongsOnPhones) {

           for (int i = 0; i < RecordSources.size(); i++) {

               if (RecordSources.get(i).get(0) == RecordSource.NA) {
                   SongsOnPhones.add(null);
               } else if (RecordSources.get(i).get(0) == RecordSource.LIVE) {
                   if (SongOnPhonesLive.size() != 0) {
                       SongsOnPhones.add(SongOnPhonesLive.get(i));
                   } else {
                       SongsOnPhones.add(null);
                   }
               } else {
                   if (SongOnPhonesBS.size() != 0) {
                       SongsOnPhones.add(SongOnPhonesBS.get(i));
                   } else {
                       SongsOnPhones.add(null);
                   }
               }
           }
      //     SongsOnPhones.notifyAll();
      // }

        Log.d(LOG_TAG, "CR getSongsOnPhones: (SongOnPhones) "+SongsOnPhones.size()+" "+SongsOnPhones+Thread.currentThread().getName());

        return SongsOnPhones;
    }

    /*public  List<List<Song>> getSongsOnClouds(){

        Log.d(LOG_TAG, "CR getSongsOnClouds: "+Thread.currentThread().getName());

        return SongOnClouds;
    }*/

    public void getSongToPlayLive() {
        //chercher la liste des songs To play pour le LIVE
        for (List<Song> songs:SongOnPhonesLive){

            if(songs==null || songs.size()==0){
                songToPlaysLive.add(null);
            }else{
                songToPlaysLive.add(songs.get(0));
            }
        }
        Log.d(LOG_TAG, "LS CR run initialize Data songToPlaysLive: "+songToPlaysLive.size()+" "+songToPlaysLive);
    }

    public void getSongOnPhoneLive(List<SourceSong> sourceSongsBeforeSync) {
        //chercher les SongsOnPhoneLive before Sync

        for (SourceSong sourceSong: sourceSongsBeforeSync){
            List<Song> listLive = mSongDao.getSongsOnPhone(sourceSong.getTitre(),RecordSource.LIVE);

            if(listLive!=null&&listLive.size()!=0){
                SongOnPhonesLive.add(listLive);

            }else{
                SongOnPhonesLive.add(null);
            }
        }
        Log.d(LOG_TAG, "LS CR run initialize Data SongOnPhonesLive : "+SongOnPhonesLive.size()+" "+SongOnPhonesLive);
    }

    public List<Song> getSongToPlaysA() {

        Log.d(LOG_TAG, "MA ListSongs getSongToPlaysA: après "+songToPlays);
        return  songToPlays;
    }

    public List<List<Song>> getSongsOnPhonesA(Thread currentThread) {

        /*if(currentThread.isAlive()) {
            synchronized (SongsOnPhones) {
                try {
                    Log.d(LOG_TAG, "MA ListSongs getSongOnPhonesA: thread alive ");
                    SongsOnPhones.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Log.d(LOG_TAG, "MA ListSongs getSongOnPhonesA: thread not alive ");
        }*/
        Log.d(LOG_TAG, "MA ListSongs getSongsOnPhonesA: après "+SongsOnPhones);
        return SongsOnPhones;
    }

    public List<List<Song>> getSongsOnCloudsA() {

        Log.d(LOG_TAG, "MA ListSongs getSongsOnCloudsA : après "+SongOnClouds);
        return SongOnClouds;
    }

    public List<List<RecordSource>> getRecordSourcesA(){

        Log.d(LOG_TAG, "MA ListSongs getRecordSourcesA: après "+RecordSources);
        return RecordSources;
    }

    public void setSourceSongs(List<SourceSong> sourceSongs) {
        this.sourceSongs = sourceSongs;
        Log.d(LOG_TAG, "LS CR setSourceSongs: "+sourceSongs);
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        Log.d(LOG_TAG, "LS CR setSongs: "+songs);
    }

    public List<List<Song>> getSongOnPhoneBSA() {
        Log.d(LOG_TAG, "LS CR getSongOnPhoneBSA: "+SongOnPhonesBS);
        return SongOnPhonesBS;
    }

    public List<List<Song>> getSongOnPhoneLiveA() {
        Log.d(LOG_TAG, "LS CR getSongOnPhoneLiveA: "+SongOnPhonesLive);
        return SongOnPhonesLive;
    }
}
