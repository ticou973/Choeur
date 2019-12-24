package dedicace.com.data.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private SpectacleDao mSpectacleDao;
    private List<SourceSong> sourceSongs;
    private List<Song> songs;
    private List<SourceSong> sourceSongsTemp;
    private List<Song> songsTemp;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;


    private static final String LOG_TAG = "coucou";


    public ListSongs(SongsDao mSongDao, SourceSongDao mSourceSongDao, SpectacleDao mSpectacleDao, List<SourceSong> sourceSongs, List<Song> songs, Context context) {

        this.mSongDao = mSongDao;
        this.mSourceSongDao = mSourceSongDao;
        this.sourceSongsTemp = sourceSongs;
        this.songsTemp = songs;
        this.context=context;
        this.mSpectacleDao=mSpectacleDao;

        for(Song song : songsTemp){
            Log.d(LOG_TAG, "LS constructor list songs "+song.getSourceSongTitre()+" "+song.getPupitre());
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        this.sourceSongs=getSSCurrentSpectacle(sourceSongsTemp);
        this.songs=getSongCurrentSpectacle(this.sourceSongs,songsTemp);
        Log.d(LOG_TAG, "LS CR ListSongs:constructeur A "+songs.size()+" local songs "+this.songs.size());
    }

    private List<Song> getSongCurrentSpectacle(List<SourceSong> sourceSongs, List<Song> oldSongs) {
        ArrayList<Song> currentSongs = new ArrayList<>();
        for(SourceSong sourceSong:sourceSongs){
            String titre = sourceSong.getTitre();
            List<Song> songs = mSongDao.getSongsByTitre(titre);
            currentSongs.addAll(songs);
        }

        return currentSongs;
    }

    private List<SourceSong> getSSCurrentSpectacle(List<SourceSong> sourceSongsTemp) {
        List<SourceSong> currentSS = new ArrayList<>();

        String currentSpectacleStr = sharedPreferences.getString("currentSpectacle","Tous");

        if(!TextUtils.isEmpty(currentSpectacleStr)){
            if(currentSpectacleStr.equals("Tous")){

                currentSS=getAllSourcesSongs();
                Log.d(LOG_TAG, "LS getSSCurrentSpectacle: Tous");

                Collections.sort(currentSS,new comparatorSS());
            }else{
                ArrayList<String> currentSSId;
                Spectacle currentSpectacle = mSpectacleDao.getSpectacleByName(currentSpectacleStr);
                if(currentSpectacle!=null) {
                    Log.d(LOG_TAG, "LS getSSCurrentSpectacle: " + currentSpectacle.getSpectacleName());
                    currentSSId = currentSpectacle.getIdTitresSongs();
                    for(String idSource: currentSSId){
                        SourceSong sourceSong = mSourceSongDao.getSourceSongByIdCloud(idSource);
                        currentSS.add(sourceSong);
                    }
                }else{
                    Log.d(LOG_TAG, "LS getSSCurrentSpectacle: le spectacle n'existe pas ");
                }
            }
        }else{
            Log.d(LOG_TAG, "LS getSSCurrentSpectacle: pas de current spectacle ");
        }


        return currentSS;
    }

    private List<SourceSong> getAllSourcesSongs() {
        List<Spectacle> allSpectacles = mSpectacleDao.getAllSpectacles();
        for(Spectacle spectacle:allSpectacles){
            Log.d(LOG_TAG, "LS getAllSourcesSongs: listSpectacles room "+spectacle.getSpectacleName()+" "+spectacle.getIdSpectacleCloud());
        }

        Set<String> currentSpectaclesSet = sharedPreferences.getStringSet("currentSpectacles",null);
        Log.d("coucou", "LS getAllSourcesSongs: currentSpectaclesSet "+currentSpectaclesSet);
        ArrayList<String> currentSSId = null;
        Set<SourceSong> currentsourceSongs = new HashSet<>();

        if(currentSpectaclesSet!=null) {
            for (String idSpectacle : currentSpectaclesSet) {

                Spectacle currentSpectacle = mSpectacleDao.getSpectacleById(idSpectacle);
                Log.d(LOG_TAG, "LS getAllSourcesSongs: début 1er fort "+idSpectacle+" "+currentSpectacle);
                if(currentSpectacle!=null) {
                    currentSSId = currentSpectacle.getIdTitresSongs();
                    Log.d(LOG_TAG, "LS getAllSourcesSongs: currenspectacleId "+currentSpectacle.getSpectacleName()+" currentSSid"+currentSSId);
                }else{
                    Log.d(LOG_TAG, "LS getAllSourcesSongs: cas null pour currentSpectacle");
                }
                
                for(String idSource: currentSSId){
                    Log.d(LOG_TAG, "LS getAllSourcesSongs: début boucle "+ idSource);
                    if(currentsourceSongs.size()!=0){
                        Log.d(LOG_TAG, "LS getAllSourcesSongs: if "+" "+currentsourceSongs.size());

                        int i=0;
                        for (SourceSong sourceSong : currentsourceSongs){
                            Log.d(LOG_TAG, "LS getAllSourcesSongs: début boucle B "+" "+sourceSong.getTitre()+" "+sourceSong);
                            if(!sourceSong.getIdSourceSongCloud().equals(idSource)){
                                i++;
                                Log.d(LOG_TAG, "LS getAllSourcesSongs: début boucle dans if "+i+" "+sourceSong.getTitre()+" "+sourceSong);
                            }
                        }
                        if(i==currentsourceSongs.size()){
                            SourceSong sourceSong = mSourceSongDao.getSourceSongByIdCloud(idSource);
                            currentsourceSongs.add(sourceSong);
                            Log.d(LOG_TAG, "LS getAllSourcesSongs: if currentSongsSize"+sourceSong.getTitre()+ " "+currentsourceSongs.size());
                        }else{
                            Log.d(LOG_TAG, "LS getAllSourcesSongs: doublon différents spectacles "+ idSource);
                        }
                    }else{
                        SourceSong sourceSong = mSourceSongDao.getSourceSongByIdCloud(idSource);
                        currentsourceSongs.add(sourceSong);
                        Log.d(LOG_TAG, "LS getAllSourcesSongs: else "+ sourceSong.getTitre()+" "+currentsourceSongs.size());
                    }
                }
            }
        }

        Log.d(LOG_TAG, "LS getAllSourcesSongs: fin "+currentsourceSongs.size());
        return new ArrayList<>(currentsourceSongs);
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

            if(SongOnClouds!=null) {
                Log.d(LOG_TAG, "LS getSongOnClouds: "+SongOnClouds.size());
            }

        for(List<Song> songList : SongOnClouds){
            if(songList!=null) {
                for (Song song : songList) {
                    Log.d(LOG_TAG, "LS getSongsOncloud: listOnClouds " + song.getSourceSongTitre() + " " + song.getPupitre());
                }
            }
        }
//        Log.d(LOG_TAG, "LS ChoraleRepository LiveData: ListSongOnCloud "+ SongOnClouds.size()+ " songs "+songs.size()+" "+SongOnClouds+" "+SongOnClouds.get(0).size()+" "+Thread.currentThread().getName());
    }

    public void getSongOnPhoneBS() {
        //chercher les SongsOnPhoneBS after Sync
        Log.d(LOG_TAG, "LS getSongOnPhoneBS: ssAftersync size "+sourceSongs.size());

        for (SourceSong sourceSong: sourceSongs){
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

    public void getSongOnPhoneLive() {
        //chercher les SongsOnPhoneLive before Sync

        for (SourceSong sourceSong: sourceSongs){
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

    public class comparatorSS implements Comparator<SourceSong> {
        public comparatorSS() {
        }

        @Override
        public int compare(SourceSong sourceSong, SourceSong sourceSong1) {
            return sourceSong.getIdSourceSongCloud().compareTo(sourceSong1.getIdSourceSongCloud());
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
