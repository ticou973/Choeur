package dedicace.com.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
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

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {

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
        Song OnPlayFirstSong(String titre, RecordSource recordSource);
        List<Song> OnListRecordedSongsOnPhone();
        List<Song> OnListRecordedSongsOnCloud();

    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);
        Log.d(TAG, "SA onCreateViewHolder: ");

        //todo voir si on ne peut pa déplacer le listener dans viewHolder
        SongsViewHolder songsViewHolder = new SongsViewHolder(view,mlistemClickedListener,context);
        return songsViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int position) {

        Log.d(TAG, "SA onBindViewHolderA: ");
        //Gestion des datas de la SourceSong
        initDataSourceSong(songsViewHolder, position);
        //songsViewHolder.verifyExistingSongs(RecordSource.BANDE_SON);
        songsViewHolder.setResourceToMediaPlayer();
    }

    @Override
    public int getItemCount() {
        if(null==sourceSongs) {
            Log.d("coucou", "getItemCount: 0");
            return 0;
        }else{
            Log.d("coucou", "getItemCount: "+sourceSongs.size());
        }
        return sourceSongs.size();
    }


    //Autres méthodes
    private void initDataSourceSong(SongsViewHolder songsViewHolder, int position) {
        Log.d(TAG, "initDataSourceSong: SA");
        //initialisation de la sourceSong
        songsViewHolder.setTitre(sourceSongs.get(position).getTitre());
        songsViewHolder.setGroupe(sourceSongs.get(position).getGroupe());
        GlideApp.with(context)
                .load(sourceSongs.get(position).getBgSong())
                .centerCrop() // scale to fill the ImageView and crop any extra
                .into(songsViewHolder.getImageSong());
        //todo voir si mettre 0 ou SystemClock.elapsedRealtime()
        songsViewHolder.setChronometer(0);
        sourceSong = sourceSongs.get(position);

        Log.d(TAG, "initDataSourceSong: "+sourceSong.getTitre()+" "+position);
        songsViewHolder.setSourceSong(sourceSong);

        initRecordSongs(songsViewHolder,sourceSong,position);

        //initalisation des songs de la sourceSongs
        if(recordSource!=RecordSource.NA) {
            initDataSongs(songsViewHolder, sourceSong,position);
        }
    }

    private void initRecordSongs(SongsViewHolder songsViewHolder,SourceSong sourceSong, int position) {
        recordSources=RecordSources.get(position);
        songsViewHolder.setRecordSource(recordSources);
        recordSource=songsViewHolder.getSource();
    }

    private void initDataSongs(SongsViewHolder songsViewHolder,SourceSong sourceSong,int position) {
        Log.d(TAG, "initDataSongs: SA");
        songToPlay = songToPlays.get(position);
        songOnPhoneRecorded = songOnPhones.get(position);
        Song[] songsPhone = songOnPhoneRecorded.toArray(new Song[0]);
        songOnCloudRecorded = songOnClouds.get(position);
        Song[] songsCloud = songOnCloudRecorded.toArray(new Song[0]);

        songsViewHolder.setSongRecorded(songsPhone);
        songsViewHolder.setSongToPlay(songToPlay);
        songsViewHolder.setSongCloudRecorded(songsCloud);
    }


    public void swapSongs(final List<SourceSong> sources, List<List<RecordSource>> recordSources, List<Song> songToPlays, List<List<Song>> songOnPhones, List<List<Song>> songOnClouds) {
        Log.d("coucou", "swapSongs: SongAdapter");
        this.RecordSources=recordSources;
        this.songToPlays=songToPlays;
        this.songOnPhones=songOnPhones;
        this.songOnClouds=songOnClouds;

        if(sourceSongs==null){
            Log.d("coucou", "swapForecast: cas null ");
            sourceSongs=sources;
            notifyDataSetChanged();
        }else{
            Log.d("coucou", "swapForecast: cas non null ");

            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return sourceSongs.size();
                }

                @Override
                public int getNewListSize() {
                    return sources.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return sourceSongs.get(oldItemPosition).getSourceSongId() ==
                            sources.get(newItemPosition).getSourceSongId();
                }

                //todo à véridier les conditions nécessaires puis revoir les init datas
                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    SourceSong newSong = sources.get(newItemPosition);
                    SourceSong oldSong = sourceSongs.get(oldItemPosition);
                    return newSong.getSourceSongId() == oldSong.getSourceSongId()
                            && newSong.getTitre().equals(oldSong.getTitre())
                            && newSong.getGroupe().equals(oldSong.getGroupe());

                }
            });
            sourceSongs = sources;
            result.dispatchUpdatesTo(this);
            Log.d(TAG, "swapSongs: fin dispatch SA");
        }
    }
}
