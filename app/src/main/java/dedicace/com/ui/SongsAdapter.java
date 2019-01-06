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
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {

    private List<SourceSong> sourceSongs;
    private List<Song> songsEssai;
    private List<Song> mSongs;
    private Context context;
    private Pupitre recordPupitre=Pupitre.NA;

    public static final String TAG = "coucou";

    public ListemClickedListener mlistemClickedListener;

    private SourceSong sourceSong;
    private Song songToPlay;
    private List<Song> songOnPhoneRecorded= new ArrayList<>();
    private List<Song> songOnCloudRecorded;
    private List<Song> songNotRecorded;


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
        Song OnPlaySong();
        List<Song> OnListRecordedSongsOnPhone();
        List<Song> OnListRecordedSongsOnCloud();
        List<Song> OnListNotRecordedSong();
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

        //initalisation des songs de la sourceSongs
        initDataSongs(songsViewHolder,sourceSong);
    }

    private void initDataSongs(SongsViewHolder songsViewHolder,SourceSong sourceSong) {
        Log.d(TAG, "initDataSongs: SA");
        songToPlay=mlistemClickedListener.OnPlaySong();
        Log.d(TAG, "initDataSongs: plyaSong choisi");

        songOnPhoneRecorded = mlistemClickedListener.OnListRecordedSongsOnPhone();
        Song[] songsPhone = songOnPhoneRecorded.toArray(new Song[0]);

        songOnCloudRecorded = mlistemClickedListener.OnListRecordedSongsOnCloud();
        Song[] songsCloud = songOnCloudRecorded.toArray(new Song[0]);

        songNotRecorded = mlistemClickedListener.OnListNotRecordedSong();
        Song[] songsNotRecorded = songNotRecorded.toArray(new Song[0]);


        /*Song songToPlay = songsEssai.get(0);
        Song songOnPhoneRecorded = songsEssai.get(1);
        Song songOnCloudRecorded = songsEssai.get(2);
        Song songNotRecorded = songsEssai.get(3);*/

       /* for (SourceSong source: sourceSongs) {

            Log.d(TAG, "run MA: "+source.getSourceSongId());

        }
        //récupération de la liste des song par sourcesong
        for (Song song: songsEssai){

            Log.d(TAG, "initDataSongs:"+song.getSourceSongId());
            Log.d(TAG, "initDataSongs:"+sourceSong.getSourceSongId());

            if(song.getSourceSongId()==sourceSongs.indexOf(sourceSong)){

                Log.d(TAG, "initDataSongs: songsbysource");
                songsBysourceSong.add(song);
            }
        }

        Log.d(TAG, "initDataSongs: "+songsBysourceSong.size());

        if(songsBysourceSong==null){
            songToPlay = null;
        }else{
           songToPlay = songsBysourceSong.get(0);
        }*/

        songsViewHolder.setSongToPlay(songToPlay);
        songsViewHolder.setSongRecorded(songsPhone);
        songsViewHolder.setSongCloudRecorded(songsCloud);
        songsViewHolder.setSongNotRecorded(songsNotRecorded);
    }


    public void swapSongs(final List<SourceSong> sources) {
        Log.d("coucou", "swapSongs: SongAdapter");

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
