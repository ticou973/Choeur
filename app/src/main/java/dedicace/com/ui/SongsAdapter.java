package dedicace.com.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
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

    public ListemClickedListener mlistemClickedListener;

    private SourceSong sourceSong;
    private List<RecordSource> recordSources;
    private List<List<RecordSource>> RecordSources= new ArrayList<>();
    private RecordSource recordSource;
    private List<Song> songToPlays= new ArrayList<>();
    private String titre;
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
        Song OnPlayFirstSong(SourceSong sourceSong, RecordSource recordSource);
        List<Song> OnListRecordedSongsOnPhone(SourceSong sourceSong,RecordSource recordSource);
        List<Song> OnListRecordedSongsOnCloud(SourceSong sourceSong,RecordSource recordSource);
        void OnSaveRecordSong(Song song);


    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);
        Log.d(TAG, "SA onCreateViewHolder: ");

        //todo voir si on ne peut pas déplacer le listener dans viewHolder
        SongsViewHolder songsViewHolder = new SongsViewHolder(view,mlistemClickedListener,context);
        return songsViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int position) {

        Log.d(TAG, "SA onBindViewHolder: ");
        //Gestion des datas de la SourceSong
        init(songsViewHolder);
        initDataSourceSong(songsViewHolder, position);
        songsViewHolder.setResourceToMediaPlayer();
    }

    //Autres méthodes
    private void initDataSourceSong(SongsViewHolder songsViewHolder, int position) {
        Log.d(TAG, "SA initDataSourceSong: SA");
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

    private void init(SongsViewHolder songsViewHolder){
        songsViewHolder.getPlaySongs().setImageResource(R.drawable.ic_play_orange);
        songsViewHolder.setSongToPlay(null);
        songsViewHolder.setButtonNonActivable();
        songsViewHolder.setFirstTime(true);
        songsViewHolder.isFirstTime();
    }

    private void initRecordSongs(SongsViewHolder songsViewHolder, int position) {
        Log.d(TAG, "SA initRecordSongs: "+position);
        recordSources=RecordSources.get(position);
        Log.d(TAG, "SA initRecordSongs: recordSources "+recordSources);

        songsViewHolder.setRecordSource(recordSources);
        //dans le sens à recordToPlay
        recordSource=songsViewHolder.getSource();
        Log.d(TAG, "initRecordSongs: recordSource "+recordSource);
    }

    private void initDataSongs(SongsViewHolder songsViewHolder,int position) {
        Log.d(TAG, "SA initDataSongs: ");

        Song[] songsPhone, songsCloud;

        songOnCloudRecorded = songOnClouds.get(position);
        if(songOnCloudRecorded!=null) {
            songsCloud = songOnCloudRecorded.toArray(new Song[0]);
        }else{
            songsCloud = new Song[0];
        }

        songsViewHolder.setSongCloudRecorded(songsCloud);
        Log.d(TAG, "SA initDataSongs: songsCloud "+ songsCloud.length);

        songOnPhoneRecorded = songOnPhones.get(position);
        if(songOnPhoneRecorded!=null) {
            songsPhone = songOnPhoneRecorded.toArray(new Song[0]);
        }else{
            songsPhone = new Song[0];
        }
        songsViewHolder.setSongRecorded(songsPhone);

        Log.d(TAG, "SA initDataSongs: songsPhone "+ songsPhone.length);

        if(songsPhone.length!=0) {
            songToPlay = songToPlays.get(position);

            songsViewHolder.setSongToPlay(songToPlay);

        }
    }


    /**
     *
     * @param sources
     * @param recordSources
     * @param songToPlays
     * @param songOnPhones
     * @param songOnClouds
     *
     * lancement et maj des Songs
     */

    public void swapSongs(final List<SourceSong> sources, List<List<RecordSource>> recordSources, List<Song> songToPlays, List<List<Song>> songOnPhones, List<List<Song>> songOnClouds) {
        Log.d("coucou", "swapSongs: SongAdapter \n"+sources+"\n"+recordSources+"\n"+ songToPlays+"\n"+ songOnPhones+"\n"+ songOnClouds);
        if(songToPlays.get(0)!=null){
            Log.d(TAG, "SA swapSongs: songToplays 1 "+songToPlays.get(0).getSourceSongTitre()+" "+songToPlays.get(0).getPupitre());
        }
        if(songToPlays.get(1)!=null){
            Log.d(TAG, "SA swapSongs: songToplays 2 "+songToPlays.get(1).getSourceSongTitre()+" "+songToPlays.get(1).getPupitre());
        }
        if(songOnPhones.get(1)!=null){
            Log.d(TAG, "SA swapSongs: songOnPhones 1 "+songOnPhones.get(1).get(0).getSourceSongTitre()+" "+songOnPhones.get(1).get(0).getPupitre());
        }
        if(songToPlays.get(1)!=null){
//            Log.d(TAG, "SA swapSongs: songOnPhones 2 "+songOnPhones.get(1).get(1).getSourceSongTitre()+" "+songOnPhones.get(1).get(1).getPupitre());
        }

        if(songOnClouds.get(0)!=null){
            Log.d(TAG, "SA swapSongs: songOnClouds 1 "+songOnClouds.get(0).get(0).getSourceSongTitre()+" "+songOnClouds.get(0).get(0).getPupitre());
        }
        if(songOnClouds.get(1)!=null){
            Log.d(TAG, "SA swapSongs: songOnClouds 2 "+songOnClouds.get(1).get(0).getSourceSongTitre()+" "+songOnClouds.get(1).get(0).getPupitre());
        }
        if(songOnClouds.get(1)!=null){
//            Log.d(TAG, "SA swapSongs: songOnClouds 3 "+songOnClouds.get(1).get(1).getSourceSongTitre()+" "+songOnClouds.get(1).get(1).getPupitre());
        }
        sourceSongs=sources;
        this.RecordSources=recordSources;
        this.songToPlays=songToPlays;
        this.songOnPhones=songOnPhones;
        this.songOnClouds=songOnClouds;
        notifyDataSetChanged();

    //todo voir si utile le diffResult cf evernote

    }

    @Override
    public int getItemCount() {
        if(null==sourceSongs) {
            // Log.d("coucou", "getItemCount: 0");
            return 0;
        }else{
            // Log.d("coucou", "getItemCount: "+sourceSongs.size());
        }
        return sourceSongs.size();
    }
}
