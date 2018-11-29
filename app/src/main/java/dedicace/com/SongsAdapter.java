package dedicace.com;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {

    private List<Song> songs;


    public SongsAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);
        return new SongsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder songsViewHolder, int i) {

        songsViewHolder.setTitre(songs.get(i).getTitre());
        songsViewHolder.setGroupe(songs.get(i).getGroupe());

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
