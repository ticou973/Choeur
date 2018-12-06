package dedicace.com;

import android.content.Context;
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

    private int i=0;


    public static final String TAG = "coucou";

    public ListemClickedListener mlistemClickedListener;


    public SongsAdapter(List<SourceSong> songs, Context context) {
        this.songs = songs;
        this.context = context;
        mlistemClickedListener=(ListemClickedListener) context;
    }

    public interface ListemClickedListener {
        void OnClickedItem(int clickedItem);
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);

        SourceSong song = songs.get(i);

        i++;

        Log.d(TAG, "onCreateViewHolder: "+position+" "+ song.getTitre());

        SongsViewHolder songsViewHolder = new SongsViewHolder(view,mlistemClickedListener, song);

        return songsViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int position) {

        //Gestion des datas de la SourceSong
        initDataSourceSong(songsViewHolder, position);

    }

    private void initDataSourceSong(SongsViewHolder songsViewHolder, int position) {
        songsViewHolder.setTitre(songs.get(position).getTitre());
        songsViewHolder.setGroupe(songs.get(position).getGroupe());
        GlideApp.with(context)
                .load(songs.get(position).getBgSong())
                .centerCrop() // scale to fill the ImageView and crop any extra
                .into(songsViewHolder.getImageSong());
    }


    @Override
    public int getItemCount() {
        return songs.size();
    }


    //Autres méthodes

}
