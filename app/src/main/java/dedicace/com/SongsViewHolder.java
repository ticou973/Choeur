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

class SongsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

        int position = getAdapterPosition();
    }


    public void setButtonActivable(boolean activable, Button... buttons){

        for (Button button: buttons) {

            if(activable){

                button.setAlpha(1.0f);
                button.setEnabled(true);

            }else{

                button.setAlpha(0.3f);
                button.setEnabled(false);
            }
        }
    }






    public void setColorButton(boolean focus, Button... buttons) {

        int red, green, blue;

        for (Button button: buttons) {

            if(focus) {
                red = 249;
                green = 191;
                blue = 45;
                button.setAlpha(1.0f);

            }else{
                red = 255;
                green = 241;
                blue = 99;
                button.setAlpha(0.5f);
            }

            button.setBackgroundColor(rgb(red,green,blue));
        }


    }

    public void setGreyButton(boolean focus, Button... buttons){

        int red, green, blue;

        for (Button button: buttons) {

            red = 224;
            green = 224;
            blue = 224;


            if(focus) { button.setAlpha(1.0f);

            }else{ button.setAlpha(0.5f); }



            button.setBackgroundColor(rgb(red,green,blue));

        }
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

    @Override
    public void onClick(View view) {



    }

    public void disableBtn(Button ... buttons) {

        for (Button button:buttons) {
            button.setAlpha(0.3f);
            button.setEnabled(false);

        }
    }
}
