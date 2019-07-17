package dedicace.com.data.database;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import dedicace.com.ui.SongsAdapter;

public class ListSongs {

    //todo vérifier si le fait d'avoir enlever final au 4 premiers posent un problème
    private  List<List<Song>> SongsOnPhones= new ArrayList<>();
    private  List<List<Song>> SongOnClouds=new ArrayList<>();
    private  List<List<RecordSource>> RecordSources=new ArrayList<>();
    private  List<Song> songToPlays=new ArrayList<>();
    private List<List<Song>> SongOnPhonesLive= new ArrayList<>();

    private List<List<Song>> SongOnPhonesBS= new ArrayList<>();
    private List<Song> songToPlaysLive=new ArrayList<>();
    private List<Song> songToPlaysBs=new ArrayList<>();
    private SongsDao mSongDao;
    private SourceSongDao mSourceSongDao;
    private List<SourceSong> sourceSongs;
    private List<Song> songs;
    private List<SourceSong> sourceSongsTemp;
    private List<Song> songsTemp;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final String LOG_TAG = "coucou";


    public ListSongs(SongsDao mSongDao, SourceSongDao mSourceSongDao, List<SourceSong> sourceSongs, List<Song> songs) {

        this.mSongDao = mSongDao;
        this.mSourceSongDao = mSourceSongDao;
        this.sourceSongs = sourceSongs;
        this.songs = songs;
        Log.d(LOG_TAG, "LS CR ListSongs:constructeur A "+songs.size()+"local songs"+this.songs.size());
    }

    public void getSongOnClouds() {
        //chercher les SongsOnCloud BS

            for (SourceSong sourceSong : sourceSongs) {
                List<Song> listSongbyTitre = new ArrayList<>();

                for (Song song : songs) {
                     if (song.getSourceSongTitre().equals(sourceSong.getTitre())&&song.getRecordSource()==RecordSource.BANDE_SON) {
                        listSongbyTitre.add(song);
                     }
                }
                if(listSongbyTitre.size()!=0) {
                    SongOnClouds.add(listSongbyTitre);
                }else{
                    SongOnClouds.add(null);
                }
            }

        Log.d(LOG_TAG, "LS ChoraleRepository LiveData: ListSongOnCloud "+ SongOnClouds.size()+ " songs "+songs.size()+" "+SongOnClouds+" "+SongOnClouds.get(0).size()+" "+Thread.currentThread().getName());
    }

    public void getSongOnPhoneBS(List<SourceSong> sourceSongsAfterSync) {
        //chercher les SongsOnPhoneBS after Sync
        Log.d(LOG_TAG, "LS getSongOnPhoneBS: ssAftersync size "+sourceSongsAfterSync.size());

        for (SourceSong sourceSong: sourceSongsAfterSync){
            List<Song> listBs = mSongDao.getSongsOnPhone(sourceSong.getTitre(),RecordSource.BANDE_SON);

            Log.d(LOG_TAG, "LS getSongOnPhoneBS: listBS "+ listBs.size());

            if(listBs!=null){
                for (int i = 0; i <listBs.size() ; i++) {
                    Log.d(LOG_TAG, "LS getSongOnPhoneBS: listBs songs "+listBs.get(i).getSourceSongTitre()+" "+listBs.get(i).getPupitre()+" "+listBs.get(i).getUrlCloudMp3());
                }
            }

            if(listBs!=null&&listBs.size()!=0){
                SongOnPhonesBS.add(listBs);

            }else{
                SongOnPhonesBS.add(null);
            }
        }

//        Log.d(LOG_TAG, "LS ChoraleRepository LiveData après sync SongOnPhonesBS: "+SongOnPhonesBS.size()+" "+SongOnPhonesBS.get(0).size()+" "+Thread.currentThread().getName());
        for (int i = 0; i <SongOnPhonesBS.size() ; i++) {
            Log.d(LOG_TAG, "LS run: song onPhone BS "+ SongOnPhonesBS.get(i));
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

        Log.d(LOG_TAG, "LS ChoraleRepository LiveData après sync Song to Plays BS: "+songToPlaysBs.size()+" "+songToPlaysBs+Thread.currentThread().getName());
    }

    public  List<List<RecordSource>> getRecordSources() {
        Log.d(LOG_TAG, "LS getRecordSources: list de list "+sourceSongs.size()+Thread.currentThread().getName());

                for (SourceSong sourceSong : sourceSongs) {
                    String titre = sourceSong.getTitre();
                    RecordSources.add(getRecordSources(titre));
                }
                Log.d(SongsAdapter.TAG, "LS run: RecordSources après B " + RecordSources.size()+" "+RecordSources);

        return RecordSources;
    }

    private List<RecordSource> getRecordSources(String titre) {


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

        Log.d(LOG_TAG, "LS getRecordSources titre : "+sources+ " "+ titre+" "+Thread.currentThread().getName());
        return sources;
    }

    public List<Song> getSongToPlays() {
        Log.d(LOG_TAG, "LS getSongToPlays: "+Thread.currentThread().getName());

        for (List<Song> songs:SongsOnPhones) {
                if(songs!=null){
                    songToPlays.add(songs.get(0));
                }else{
                    songToPlays.add(null);
                }
        }
        Log.d(LOG_TAG, "LS getSongToPlays: "+songToPlays.size());

//        Log.d(LOG_TAG, "CR getSongToPlays: après calcul "+" "+songToPlays.get(2).getSourceSongTitre()+" "+songToPlays.get(2).getPupitre()+Thread.currentThread().getName()+songToPlays);
        return songToPlays;
    }

    public  List<List<Song>> getSongsOnPhones() {
        Log.d(LOG_TAG, "LS getSongsOnPhones: (recordSourcesSize) "+RecordSources.size()+" "+Thread.currentThread().getName());

        /**
         * Ce sont les songs On phones une fois que la RecordSource est déterminée, ce ne sont pas toutes les chansons sur le téléphone.
         */

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


//       Log.d(LOG_TAG, "LS getSongsOnPhones: (SongOnPhones) "+SongsOnPhones.size()+" "+SongsOnPhones+" "+SongsOnPhones.get(0).size()+" "+Thread.currentThread().getName());

        return SongsOnPhones;
    }

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

        Log.d(LOG_TAG, "LS ListSongs getSongToPlaysA: après "+songToPlays);
        return  songToPlays;
    }

    public List<List<Song>> getSongsOnPhonesA() {

        Log.d(LOG_TAG, "LS ListSongs getSongsOnPhonesA: après "+SongsOnPhones);
        return SongsOnPhones;
    }

    public List<List<Song>> getSongsOnCloudsA() {

        Log.d(LOG_TAG, "LS ListSongs getSongsOnCloudsA : après "+SongOnClouds);
        return SongOnClouds;
    }

    public List<List<RecordSource>> getRecordSourcesA(){

        Log.d(LOG_TAG, "LS ListSongs getRecordSourcesA: après "+RecordSources);
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

    public List<SourceSong> getSourceSongs() {
        return sourceSongs;
    }
}
