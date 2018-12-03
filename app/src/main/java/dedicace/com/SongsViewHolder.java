package dedicace.com;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import static android.graphics.Color.rgb;

class SongsViewHolder extends RecyclerView.ViewHolder {

    private TextView titre, groupe;
    private ImageView imageSong, playSongs,stopSongs,recordSongs;
    private Button bsBtn, liveBtn, tuttiBtn, bassBtn, tenorBtn, altoBtn, sopranoBtn;
    private SeekBar seekBar;



    public SongsViewHolder(@NonNull View itemView) {
        super(itemView);

        titre = itemView.findViewById(R.id.tv_titre);
        groupe = itemView.findViewById(R.id.tv_groupe);
        imageSong =itemView.findViewById(R.id.iv_songs);
        playSongs=itemView.findViewById(R.id.play_image);
        stopSongs=itemView.findViewById(R.id.stopSongs);
        recordSongs =itemView.findViewById(R.id.recordSongs);
        bsBtn=itemView.findViewById(R.id.btn_bs);
        liveBtn=itemView.findViewById(R.id.btn_live);
        tuttiBtn=itemView.findViewById(R.id.btn_tutti);
        bassBtn=itemView.findViewById(R.id.btn_bass);
        tenorBtn=itemView.findViewById(R.id.btn_tenor);
        altoBtn=itemView.findViewById(R.id.btn_alto);
        sopranoBtn=itemView.findViewById(R.id.btn_soprano);
        seekBar=itemView.findViewById(R.id.seekBar);

        Log.d(SongsAdapter.TAG, "SongsViewHolder: ");
    }






    public void setColorButton (Button button, boolean focus) {

        int red, green, blue;

        if(focus) {
            red = 255;
            green = 235;
            blue = 59;
            button.setAlpha(1.0f);

        }else{
            red = 128;
            green = 226;
            blue = 126;
            button.setAlpha(0.3f);
        }

        button.setBackgroundColor(rgb(red,green,blue));
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

    public Button getBsBtn() {
        return bsBtn;
    }

    public Button getLiveBtn() {
        return liveBtn;
    }

    public Button getTuttiBtn() {
        return tuttiBtn;
    }

    public Button getBassBtn() {
        return bassBtn;
    }

    public Button getTenorBtn() {
        return tenorBtn;
    }

    public Button getAltoBtn() {
        return altoBtn;
    }

    public Button getSopranoBtn() {
        return sopranoBtn;
    }

    public ImageView getImageSong() {
        return imageSong;
    }

    public ImageView getRecordSongs() {
        return recordSongs;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }
}
