package dedicace.com.ui.Admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Song;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> listSongs = new ArrayList<>();
    private OnItemListener mListener;
    private static final String TAG="coucou";

    public SongAdapter(List<Song> listSongs) {
        this.listSongs = listSongs;
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_song_cloud, viewGroup, false);

        SongViewHolder songViewHolder = new SongViewHolder(view);
        return songViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder songViewHolder, int i) {
        songViewHolder.titre.setText(listSongs.get(i).getSourceSongTitre());
        songViewHolder.pupitre.setText(listSongs.get(i).getPupitre().toString());
    }

    @Override
    public int getItemCount() {
        return listSongs.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        Context context = recyclerView.getContext();

        if (context instanceof OnItemListener) {
            mListener = (OnItemListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        mListener = null;
    }

    private void selectSong(int i) {
        if(mListener!=null){
            mListener.onItemClick(i);
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        TextView titre,pupitre;
        CardView cv;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titre = itemView.findViewById(R.id.songName);
            pupitre =itemView.findViewById(R.id.songPupitre);
            cv=itemView.findViewById(R.id.cv_list_song);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "SA onClick: "+getAdapterPosition());
                    int i = getAdapterPosition();
                    selectSong(i);
                }
            });
        }
    }
}
