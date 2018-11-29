package dedicace.com;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class SongsViewHolder extends RecyclerView.ViewHolder {

    private TextView titre, groupe;
    private ImageView imageSong, playSongs,stopSongs;



    public SongsViewHolder(@NonNull View itemView) {
        super(itemView);

        titre = itemView.findViewById(R.id.tv_titre);
        groupe = itemView.findViewById(R.id.tv_groupe);
        imageSong =itemView.findViewById(R.id.iv_songs);
        playSongs=itemView.findViewById(R.id.play_image);
        stopSongs=itemView.findViewById(R.id.stopSongs);


    }

    public void setTitre(String titre){

        this.titre.setText(titre);
    }

    public void setGroupe(String groupe){

        this.groupe.setText(groupe);
    }

    public void setImageSong(int imageInt){

        imageSong.setImageResource(imageInt);
    }

    public ImageView getPlaySongs() {
        return playSongs;
    }

    public ImageView getStopSongs() {
        return stopSongs;
    }
}
