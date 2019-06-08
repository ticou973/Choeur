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
        Log.d(TAG, "SA onBindViewHolder: "+position);
        //Gestion des datas de la SourceSong
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
        //dans le sens à recordToPlay
        recordSource=songsViewHolder.getSource();
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


        songOnPhoneRecorded = songOnPhones.get(position);
        if(songOnPhoneRecorded!=null) {
            Log.d(TAG, "SA initDataSongs: "+songOnPhoneRecorded);
            songsPhone = songOnPhoneRecorded.toArray(new Song[0]);
        }else{
            songsPhone = new Song[0];
        }

        Log.d(TAG, "SA initDataSongs: songsPhone "+ songsPhone.length+" "+ Arrays.toString(songsPhone));

        songsViewHolder.setValueSongLocalRecorded(songsPhone);
        songsViewHolder.setListSongLocalRecorded(songOnPhoneRecorded);
        songsViewHolder.setSongRecorded(songsPhone);


        if(songsPhone.length!=0) {
            songToPlay = songToPlays.get(position);
            Log.d(TAG, "SA initDataSongs: "+songToPlay.getSourceSongTitre()+" "+songToPlay.getPupitre());

            songsViewHolder.setSongToPlay(songToPlay);
        }
    }

    /**
     * lancement et maj des Songs
     */

    void swapSongs(final List<SourceSong> sources, List<List<RecordSource>> recordSources, List<Song> songToPlays, List<List<Song>> songOnPhones, List<List<Song>> songOnClouds) {
        sourceSongs=sources;
        Log.d(TAG, "SA swapSongs: url :"+sourceSongs+" "+recordSources+" "+" "+songToPlays+" "+songOnPhones+" "+songOnClouds);
        logSources(sources,recordSources,songToPlays,songOnPhones,songOnClouds);
        this.RecordSources=recordSources;
        this.songToPlays=songToPlays;
        this.songOnPhones=songOnPhones;
        this.songOnClouds=songOnClouds;
        notifyDataSetChanged();
    }

    void swapSingleSong(int position, List<Song> songToPlays, List<List<Song>> songOnPhones, List<List<Song>> songOnClouds) {
        Log.d(TAG, "SA swapSongs single: url :"+sourceSongs+" "+RecordSources+" "+position+" "+songToPlays+" "+songOnPhones+" "+songOnClouds);
        //logSources(sourceSongs,RecordSources,songToPlays,songOnPhones,songOnClouds);
        this.songToPlays=songToPlays;
        this.songOnPhones=songOnPhones;
        this.songOnClouds=songOnClouds;
        notifyItemChanged(position);
    }

    void swaprecordedSongs(int position, List<List<RecordSource>> recordSources, List<Song> songToPlays, List<List<Song>> songOnPhones, List<List<Song>> songOnClouds) {
        Log.d(TAG, "SA swapRecordSongs: url :"+position+" "+sourceSongs+" "+recordSources+" "+" "+songToPlays+" "+songOnPhones+" "+songOnClouds);
        logSources(sourceSongs,recordSources,songToPlays,songOnPhones,songOnClouds);
        this.RecordSources=recordSources;
        this.songToPlays=songToPlays;
        this.songOnPhones=songOnPhones;
        this.songOnClouds=songOnClouds;
        notifyItemChanged(position);
    }

    public static void logSources(List<SourceSong> sourceSongs, List<List<RecordSource>> RecordSources, List<Song> songToPlays, List<List<Song>> songOnPhones, List<List<Song>> songOnClouds) {
            Log.d(TAG, "SA logSources: size " +sourceSongs.size()+" "+RecordSources.size()+" "+songToPlays.size()+" "+songOnPhones.size()+" "+songOnClouds.size());
            Log.d(TAG, "SA logSources: :\n sourcesSongs " + sourceSongs + "\n RecordSources " + RecordSources + "\nsongToPlays " + songToPlays + "\nsongOnPhones " + songOnPhones + "\nSongOnClouds " + songOnClouds);
            for (List<Song> songs : songOnPhones) {
                Log.d(TAG, "SA logSources: size SongsOnPhones "+songs.size());
                if(songs!=null) {
                    for (Song song : songs) {
                        if(song!=null) {
                            Log.d(TAG, "SA logSources: Phones " + song.getSourceSongTitre() + " " + song.getPupitre()+" "+song.getRecordSource());
                        }
                    }
                }
            }
        for (List<Song> songs : songOnClouds) {
            Log.d(TAG, "SA logSources: size SongsOnClouds "+songs.size());
            if(songs!=null) {
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
