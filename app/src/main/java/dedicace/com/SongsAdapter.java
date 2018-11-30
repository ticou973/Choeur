package dedicace.com;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {

    private List<Song> songs;
    private Context context;

    private Pupitre currentPupitre = Pupitre.NA;
    private RecordSource currentSource = RecordSource.NA;


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
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int position) {

        //Gestion des datas de la SourceSong
        songsViewHolder.setTitre(songs.get(position).getSourceSong().getTitre());
        songsViewHolder.setGroupe(songs.get(position).getSourceSong().getGroupe());
        songsViewHolder.setImageSong(songs.get(position).getSourceSong().getBgSong());

        //Gestion des boutons de RecordSource
        setListener2Button(songsViewHolder,position, songsViewHolder.getLiveBtn(),songsViewHolder.getBsBtn());
        setListener2Button(songsViewHolder,position,songsViewHolder.getBsBtn(),songsViewHolder.getLiveBtn());

        //Gestion des boutons de Pupitre
        setListener4Button(songsViewHolder,position,songsViewHolder.getTuttiBtn(),songsViewHolder.getBassBtn(),songsViewHolder.getTenorBtn(),songsViewHolder.getAltoBtn(),songsViewHolder.getSopranoBtn());
        setListener4Button(songsViewHolder,position,songsViewHolder.getBassBtn(), songsViewHolder.getTuttiBtn(),songsViewHolder.getTenorBtn(),songsViewHolder.getAltoBtn(),songsViewHolder.getSopranoBtn());
        setListener4Button(songsViewHolder,position,songsViewHolder.getTenorBtn(),songsViewHolder.getTuttiBtn(),songsViewHolder.getBassBtn(),songsViewHolder.getAltoBtn(),songsViewHolder.getSopranoBtn());
        setListener4Button(songsViewHolder,position,songsViewHolder.getAltoBtn(), songsViewHolder.getTuttiBtn(),songsViewHolder.getBassBtn(),songsViewHolder.getTenorBtn(),songsViewHolder.getSopranoBtn());
        setListener4Button(songsViewHolder,position,songsViewHolder.getSopranoBtn(),songsViewHolder.getTuttiBtn(),songsViewHolder.getBassBtn(),songsViewHolder.getTenorBtn(),songsViewHolder.getAltoBtn());

        //gestion de Play
        songsViewHolder.getPlaySongs().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(currentPupitre==Pupitre.NA||currentSource==RecordSource.NA){

                    Toast.makeText(context, "Veuillez renseigner le pupitre et/ou la source (Live ou Bande Son) !", Toast.LENGTH_LONG).show();
                }else{

                    mediaPlayer = MediaPlayer.create(context, songs.get(position).getSongResId());
                    mediaPlayer.start();

                }


            }
        });

        songsViewHolder.getStopSongs().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
            }
        });

    }

    private void setListener4Button(final SongsViewHolder songsViewHolder, int position, final Button mainBtn, final Button secondBtn, final Button thirdBtn, final Button fourthBtn, final Button fifthBtn) {

        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                songsViewHolder.setColorButton(mainBtn,true);
                songsViewHolder.setColorButton(secondBtn,false);
                songsViewHolder.setColorButton(thirdBtn,false);
                songsViewHolder.setColorButton(fourthBtn,false);
                songsViewHolder.setColorButton(fifthBtn,false);

                if(mainBtn==songsViewHolder.getTuttiBtn()){ currentPupitre = Pupitre.TUTTI;

                }else if (mainBtn==songsViewHolder.getBassBtn()){ currentPupitre = Pupitre.BASS;

                }else if (mainBtn==songsViewHolder.getTenorBtn()){ currentPupitre = Pupitre.TENOR;

                }else if (mainBtn==songsViewHolder.getAltoBtn()){ currentPupitre = Pupitre.ALTO;

                }else if (mainBtn==songsViewHolder.getSopranoBtn()){ currentPupitre = Pupitre.SOPRANO;

                }else{ currentPupitre=Pupitre.NA;
                }
            }
        });


    }

    private void setListener2Button(final SongsViewHolder songsViewHolder, int position, final Button mainBtn, final Button secondBtn) {

        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                songsViewHolder.setColorButton(mainBtn,true);
                songsViewHolder.setColorButton(secondBtn,false);

                if(mainBtn==songsViewHolder.getBsBtn()){ currentSource = RecordSource.BANDE_SON;

                }else if(mainBtn==songsViewHolder.getLiveBtn()){ currentSource = RecordSource.LIVE;

                }else{ currentSource = RecordSource.NA;
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
