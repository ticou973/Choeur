package dedicace.com;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {

    private List<Song> songs;
    private Context context;
    private MediaPlayer mediaPlayer;


    public SongsAdapter(List<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);

        return new SongsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int i) {

        songsViewHolder.getPlaySongs().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer = MediaPlayer.create(context, songs.get(i).getSongResId());
                mediaPlayer.start();
            }
        });

        songsViewHolder.getStopSongs().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
            }
        });

        songsViewHolder.setTitre(songs.get(i).getTitre());
        songsViewHolder.setGroupe(songs.get(i).getGroupe());
        songsViewHolder.setImageSong(songs.get(i).getBgSong());


    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
