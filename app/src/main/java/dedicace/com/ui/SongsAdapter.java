package dedicace.com.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder>  {

    private List<SourceSong> sourceSongs;
    private Context context;

    public static final String TAG = "coucou";

    private ListemClickedListener mlistemClickedListener;

    private SourceSong sourceSong;
    private List<RecordSource> recordSources;
    private List<List<RecordSource>> RecordSources= new ArrayList<>();
    private RecordSource recordSource;
    private List<Song> songToPlays= new ArrayList<>();
    private Song songToPlay;
    private List<Song> songOnPhoneRecorded= new ArrayList<>();
    private List<Song> songOnCloudRecorded= new ArrayList<>();
    private List<List<Song>> songOnPhones= new ArrayList<>();
    private List<List<Song>> songOnClouds= new ArrayList<>();
    private List<List<Song>> SongOnPhonesBS = new ArrayList<>();
    private List<List<Song>> SongOnPhonesLive = new ArrayList<>();
    private String origine = "Default";

    //todo voir si il faut envoyer dans le constructeur les songs
    public SongsAdapter(Context context, ListemClickedListener handler ) {
        this.context = context;
        //todo voir si à enlever ?
        mlistemClickedListener=handler;
        Log.d(TAG, "SA SongsAdapter: ");
    }

    //todo à voir si bouger interface côté viewHolder
    public interface ListemClickedListener {
        void OnClickedItem(String titre, String message);
        void OnDialogRecord(int position, SongsViewHolder songsViewHolder);
        void OnRequestPermission();
        Song OnPlaySong(SourceSong sourceSong, Pupitre pupitre, RecordSource source);
        Song OnPlayFirstSong(int position, RecordSource recordSource);
        List<Song> OnListRecordedSongsOnCloud(int position,RecordSource recordSource);
        List<Song> OnListRecordedSongsOnPhone(int position,RecordSource recordSource);
        void OnSaveRecordSong(Song song);
        void OnLongClickItem(int position, Song song);
        void OnLongClickDeleteItem(int adapterPosition, Song songToDelete);
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);
        Log.d(TAG, "SA onCreateViewHolder: ");

        //todo voir si on ne peut pas déplacer le listener dans viewHolder
        return new SongsViewHolder(view,mlistemClickedListener,context);
    }


    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int position) {
        Log.d(TAG, "SA onBindViewHolder: "+position+" "+origine);
        //Gestion des datas de la SourceSong
        songsViewHolder.setOrigine(origine);
        init(songsViewHolder);
        initDataSourceSong(songsViewHolder, position);
        songsViewHolder.setResourceToMediaPlayer();
    }

    //Autres méthodes
    private void init(SongsViewHolder songsViewHolder){
        Log.d(TAG, "SA init: "+songsViewHolder.getAdapterPosition());
        songsViewHolder.getPlaySongs().setImageResource(R.drawable.ic_play_orange);
        songsViewHolder.setSongToPlay(null);
        songsViewHolder.setButtonNonActivable();
        songsViewHolder.setFirstTime(true);
        songsViewHolder.isFirstTime();
    }

    private void initDataSourceSong(SongsViewHolder songsViewHolder, int position) {
        Log.d(TAG, "SA initDataSourceSong: SA "+sourceSongs.get(position).getBackground()+" "+sourceSongs.get(position).getTitre());
        //initialisation de la sourceSong
        songsViewHolder.setTitre(sourceSongs.get(position).getTitre());
        songsViewHolder.setGroupe(sourceSongs.get(position).getGroupe());
        GlideApp.with(context)
                .load(sourceSongs.get(position).getBackground())
                .centerCrop() // scale to fill the ImageView and crop any extra
                .into(songsViewHolder.getImageSong());

        songsViewHolder.setChronometer(0);
        sourceSong = sourceSongs.get(position);

        Log.d(TAG, "SA initDataSourceSong: "+sourceSong.getTitre()+" "+position);

        songsViewHolder.setSourceSong(sourceSong);

        initRecordSongs(songsViewHolder,position);

        //initalisation des songs de la sourceSongs
        if(recordSource!=RecordSource.NA) {

            initDataSongs(songsViewHolder,position);
        }
        else{
            //Attention la regéneresence des recyclerview à 10 vues par conséquent on réutilise les instances précédentes.
            Log.d(TAG, "SA initDataSourceSong: null SongToPlays");

        }
    }


    private void initRecordSongs(SongsViewHolder songsViewHolder, int position) {
        Log.d(TAG, "SA initRecordSongs: "+position);
        recordSources=RecordSources.get(position);
        Log.d(TAG, "SA initRecordSongs: recordSources "+recordSources);

        songsViewHolder.setRecordSource(recordSources);
        recordSource = songsViewHolder.getSource();

        Log.d(TAG, "SA initRecordSongs: recordSource "+recordSource);
    }

    private void initDataSongs(SongsViewHolder songsViewHolder,int position) {
        Log.d(TAG, "SA initDataSongs: ");

        Song[] songsPhone, songsCloud;

        songOnCloudRecorded = songOnClouds.get(position);
        if(songOnCloudRecorded!=null) {
            Log.d(TAG, "SA initDataSongs: "+songOnCloudRecorded);
            songsCloud = songOnCloudRecorded.toArray(new Song[0]);

        }else{
            songsCloud = new Song[0];
        }
        Log.d(TAG, "SA initDataSongs: songsCloud "+ songsCloud.length+" "+ Arrays.toString(songsCloud));

            songsViewHolder.setValueCloudSongRecorded(songsCloud);
            songsViewHolder.setListSongCloudRecorded(songOnCloudRecorded);
            songsViewHolder.setSongCloudRecorded(songsCloud);


        if(recordSource==RecordSource.BANDE_SON){
            songOnPhoneRecorded = SongOnPhonesBS.get(position);
        }else if(recordSource==RecordSource.LIVE){
            songOnPhoneRecorded = SongOnPhonesLive.get(position);
        }

        //songOnPhoneRecorded = songOnPhones.get(position);
        if(songOnPhoneRecorded!=null) {
            Log.d(TAG, "SA initDataSongs: songOnPhone "+songOnPhoneRecorded);
            songsPhone = songOnPhoneRecorded.toArray(new Song[0]);
        }else{
            songsPhone = new Song[0];
        }

        Log.d(TAG, "SA initDataSongs: songsPhone "+ songsPhone.length+" "+ Arrays.toString(songsPhone));

        songsViewHolder.setValueSongLocalRecorded(songsPhone);
        songsViewHolder.setListSongLocalRecorded(songOnPhoneRecorded);
        songsViewHolder.setSongRecorded(songsPhone);


        if(songsPhone.length!=0) {
            if(origine.equals("Default")) {

                songToPlay = songToPlays.get(position);
                Log.d(TAG, "SA initDataSongs: songToPlay Default "+ songToPlay +" "+songToPlays);
            }
            if(songToPlay!=null) {
                Log.d(TAG, "SA initDataSongs: song to play" + songToPlay.getSourceSongTitre() + " " + songToPlay.getPupitre());
            }else{
                Log.d(TAG, "SA initDataSongs: song to pllay est null ");
            }

            songsViewHolder.setSongToPlay(songToPlay);
            origine="Default";
            songsViewHolder.setOrigine(origine);
        }
    }

    /**
     * lancement et maj des Songs
     */

    void swapSongs(final List<SourceSong> sources, List<List<RecordSource>> recordSources, List<Song> songToPlays, List<List<Song>> songOnClouds,List<List<Song>> SongOnPhonesBS,List<List<Song>> SongOnPhonesLive) {
        sourceSongs=sources;
        Log.d(TAG, "SA swapSongs: url :"+sourceSongs+" "+recordSources+" "+" "+songToPlays+" "+songOnPhones+" "+songOnClouds);
        logSources(sources,recordSources,songToPlays,songOnClouds,SongOnPhonesBS,SongOnPhonesLive);
        this.RecordSources=recordSources;
        this.songToPlays=songToPlays;
        this.songOnClouds=songOnClouds;
        this.SongOnPhonesBS=SongOnPhonesBS;
        this.SongOnPhonesLive=SongOnPhonesLive;
        origine="Default";
        notifyDataSetChanged();
    }

    void swapSingleSong(int position, Song songToPlay, List<List<Song>> songOnClouds,List<List<Song>> SongOnPhonesBS,List<List<Song>> SongOnPhonesLive,List<List<RecordSource>> recordSources) {
        Log.d(TAG, "SA swapSongs single: url :"+sourceSongs+" "+RecordSources+" "+position+" "+songToPlays+" "+songOnPhones+" "+songOnClouds+" "+SongOnPhonesBS+" "+SongOnPhonesLive);
        //logSources(sourceSongs,RecordSources,songToPlays,songOnPhones,songOnClouds);
        this.songToPlay=songToPlay;
        songToPlays.set(position,songToPlay);
        this.songOnClouds=songOnClouds;
        this.SongOnPhonesBS=SongOnPhonesBS;
        this.SongOnPhonesLive=SongOnPhonesLive;
        this.RecordSources=recordSources;
        origine="Single";
        notifyItemChanged(position);
    }

    void swapSingleDeleteSong(int position, List<Song> songToPlays, List<List<Song>> songOnClouds,List<List<Song>> SongOnPhonesBS,List<List<Song>> SongOnPhonesLive,List<List<RecordSource>> recordSources) {
        Log.d(TAG, "SA swapSongs single: url :"+sourceSongs+" "+RecordSources+" "+position+" "+songToPlays+" "+songOnPhones+" "+songOnClouds+ " "+SongOnPhonesBS+" "+SongOnPhonesLive);
        //logSources(sourceSongs,RecordSources,songToPlays,songOnPhones,songOnClouds);
        this.songToPlays=songToPlays;
        this.songOnClouds=songOnClouds;
        this.SongOnPhonesBS=SongOnPhonesBS;
        this.SongOnPhonesLive=SongOnPhonesLive;
        this.RecordSources=recordSources;
        origine="Default";
        notifyItemChanged(position);
    }

    void swaprecordedSongs(int position, List<List<RecordSource>> recordSources,Song songToPlay, List<List<Song>> songOnClouds,List<List<Song>> SongOnPhonesBS,List<List<Song>> SongOnPhonesLive) {
        Log.d(TAG, "SA swapRecordSongs: url :"+position+" "+sourceSongs+" "+recordSources+" "+" "+songToPlays+" "+songOnPhones+" "+songOnClouds);
        logSources(sourceSongs,recordSources,songToPlays,songOnClouds,SongOnPhonesBS,SongOnPhonesLive);
        this.RecordSources=recordSources;
        this.songToPlay=songToPlay;
        if(songToPlays.get(position)==null){
            Log.d(TAG, "SA swaprecordedSongs: songToplay swapRecorded ");
            songToPlays.set(position,songToPlay);
        }
        this.songOnClouds=songOnClouds;
        this.SongOnPhonesBS=SongOnPhonesBS;
        this.SongOnPhonesLive=SongOnPhonesLive;
        origine="Record";
        notifyItemChanged(position);
    }

    private static void logSources(List<SourceSong> sourceSongs, List<List<RecordSource>> RecordSources, List<Song> songToPlays, List<List<Song>> songOnClouds, List<List<Song>> SongOnPhonesBS, List<List<Song>> SongOnPhonesLive) {
            Log.d(TAG, "SA logSources: size " +sourceSongs.size()+" "+RecordSources.size()+" "+songToPlays.size()+" "+songOnClouds.size()+" "+SongOnPhonesBS.size()+" "+SongOnPhonesLive.size());
            Log.d(TAG, "SA logSources: :\n sourcesSongs " + sourceSongs + "\n RecordSources " + RecordSources + "\nsongToPlays " + songToPlays + "\nSongOnClouds " + songOnClouds+ "\nSongOnPhonesBS " + SongOnPhonesBS+ "\nSongOnPhonesLive " + SongOnPhonesLive);

        for (List<Song> songs : songOnClouds) {

            if(songs!=null) {
                Log.d(TAG, "SA logSources: size SongsOnClouds "+songs.size());
                for (Song song : songs) {
                    if(song!=null) {
                        Log.d(TAG, "SA logSources: Clouds " + song.getSourceSongTitre() + " " + song.getPupitre());
                    }else{
                        Log.d(TAG, "logSources: pb songclouds");
                    }
                }
            }else{
                Log.d(TAG, "logSources: pb songclouds song");
            }
        }
    }

    @Override
    public int getItemCount() {
        if(null==sourceSongs) {
            return 0;
        }
        return sourceSongs.size();
    }
}
