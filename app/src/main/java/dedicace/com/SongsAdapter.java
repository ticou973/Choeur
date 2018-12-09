package dedicace.com;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {

    private List<SourceSong> songs;
    private Context context;
    private Pupitre recordPupitre=Pupitre.NA;

    public static final String TAG = "coucou";

    public ListemClickedListener mlistemClickedListener;


    public SongsAdapter(List<SourceSong> songs, Context context) {
        this.songs = songs;
        this.context = context;
        mlistemClickedListener=(ListemClickedListener) context;

        Log.d(TAG, "SongsAdapter: ");

    }

    public interface ListemClickedListener {
        void OnClickedItem(String titre, String message);
        void OnDialogRecord(int position);
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);
        Log.d(TAG, "onCreateViewHolder: "+position+" ");

        SongsViewHolder songsViewHolder = new SongsViewHolder(view,mlistemClickedListener);
        return songsViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int position) {

        //Gestion des datas de la SourceSong
        initDataSourceSong(songsViewHolder, position);
        VerifyAndLaunchRecord(songsViewHolder);

        songsViewHolder.verifyExistingSongs();
        songsViewHolder.isFirstTime();
    }

    private void VerifyAndLaunchRecord(SongsViewHolder songsViewHolder) {
        Song song = MainActivity.choeurDataBase.songsDao().getLastSong();
        Pupitre pupitre = song.getPupitre();
        RecordSource recordSource = song.getRecordSource();
        String songPath = song.getSongPath();

        if(recordSource==RecordSource.LIVE&&songPath.equals("NA")){
            songsViewHolder.setRecord(pupitre);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    //Autres méthodes
    private void initDataSourceSong(SongsViewHolder songsViewHolder, int position) {
        songsViewHolder.setTitre(songs.get(position).getTitre());
        songsViewHolder.setGroupe(songs.get(position).getGroupe());
        GlideApp.with(context)
                .load(songs.get(position).getBgSong())
                .centerCrop() // scale to fill the ImageView and crop any extra
                .into(songsViewHolder.getImageSong());
        songsViewHolder.setChronometer(SystemClock.elapsedRealtime());
        SourceSong song = songs.get(position);

        Log.d(TAG, "initDataSourceSong: "+song.getTitre());
        songsViewHolder.setSourceSong(song);
    }

}
