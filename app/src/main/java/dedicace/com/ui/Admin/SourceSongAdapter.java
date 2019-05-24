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

import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.SourceSong;

class SourceSongAdapter extends RecyclerView.Adapter<SourceSongAdapter.SourceSongViewHolder> {

    private List<SourceSong> listSourceSongs;
    private OnItemListener mListener;
    private static final String TAG="coucou";

    public SourceSongAdapter(List<SourceSong> listSourceSongs) {
        this.listSourceSongs = listSourceSongs;
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public SourceSongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_ss_cloud, viewGroup, false);
        SourceSongViewHolder sourceSongViewHolder = new SourceSongViewHolder(view);
        return sourceSongViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SourceSongViewHolder sourceSongViewHolder, int i) {
        sourceSongViewHolder.titre.setText(listSourceSongs.get(i).getTitre());
    }

    @Override
    public int getItemCount() {
        return listSourceSongs.size();
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

    private void selectSourceSong(int i) {

        if(mListener!=null){
            mListener.onItemClick(i);
        }
    }

    public class SourceSongViewHolder extends RecyclerView.ViewHolder {

        TextView titre;
        CardView cv;

        public SourceSongViewHolder(@NonNull View itemView) {
            super(itemView);
            titre = itemView.findViewById(R.id.sourceSongName);
            cv=itemView.findViewById(R.id.cv_list_source_song);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "SSA onClick: "+getAdapterPosition());
                    int i = getAdapterPosition();
                    selectSourceSong(i);
                }
            });
        }
    }
}
