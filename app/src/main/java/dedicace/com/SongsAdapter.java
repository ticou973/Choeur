package dedicace.com;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsViewHolder> {

    private List<SourceSong> songs;
    private Context context;
    private boolean mUserIsSeeking = false;

    private List<Pupitre> pupitres = new ArrayList<>();
    private List<RecordSource> sources = new ArrayList<>();
    private  List<Boolean> isFirstTime = new ArrayList<>();

    private PlayerAdapter mPlayerAdapter;

    public static final String TAG = "coucou";


    public SongsAdapter(List<SourceSong> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);

        initData();

        SongsViewHolder songsViewHolder = new SongsViewHolder(view);

        return songsViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, final int position) {

        Log.d(TAG, "onBindViewHolder: " + position);

        //Gestion des datas de la SourceSong
        songsViewHolder.setTitre(songs.get(position).getTitre());
        songsViewHolder.setGroupe(songs.get(position).getGroupe());
        songsViewHolder.setImageSong(songs.get(position).getBgSong());

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
        setPlayListener(songsViewHolder, position);

        //gestion de Stop
        setStopListener(songsViewHolder,position);

        //gestion de record
        setRecordListener(songsViewHolder,position);

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        mPlayerAdapter.release();
        Log.d(TAG, "onStop: release MediaPlayer");
    }




    //Autres méthodes

    private void initData() {

        for (SourceSong song: songs) {
            pupitres.add(Pupitre.NA);
            sources.add(RecordSource.NA);
            isFirstTime.add(true);
        }

    }

    //Méthodes pour les boutons de controle du mediaplayer et mediarecorder
    private void setRecordListener(SongsViewHolder songsViewHolder, int position) {

        songsViewHolder.getRecordSongs().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
    }


    private void setStopListener(SongsViewHolder songsViewHolder, int position) {

        songsViewHolder.getStopSongs().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPlayerAdapter.reset();
            }
        });



    }

    private void setPlayListener(final SongsViewHolder songsViewHolder, final int position) {

        songsViewHolder.getPlaySongs().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: "+position);

                if(pupitres.get(position)==Pupitre.NA||sources.get(position)==RecordSource.NA){

                    Toast.makeText(context, "Veuillez renseigner le pupitre et/ou la source (Live ou Bande Son) !", Toast.LENGTH_LONG).show();
                }else{

                    if(isFirstTime.get(position)) {

                        //Gestion de la seekBar
                        initializeSeekbar(songsViewHolder);

                        //PlayBackController
                        initializePlaybackController(songsViewHolder);

                        //Fournit et parépare le Mediaplayer
                        mPlayerAdapter.prepareMediaPlayer(context, R.raw.menuett_krieger);

                        isFirstTime.set(position,false);
                    }

                    if(!mPlayerAdapter.isPlaying()) {

                        mPlayerAdapter.play();

                    }else {
                        mPlayerAdapter.pause();
                    }
                }
            }
        });

    }

    private void setListener4Button(final SongsViewHolder songsViewHolder, final int position, final Button mainBtn, final Button secondBtn, final Button thirdBtn, final Button fourthBtn, final Button fifthBtn) {

        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                songsViewHolder.setColorButton(mainBtn,true);
                songsViewHolder.setColorButton(secondBtn,false);
                songsViewHolder.setColorButton(thirdBtn,false);
                songsViewHolder.setColorButton(fourthBtn,false);
                songsViewHolder.setColorButton(fifthBtn,false);

                if(mainBtn==songsViewHolder.getTuttiBtn()){ pupitres.set(position,Pupitre.TUTTI);

                }else if (mainBtn==songsViewHolder.getBassBtn()){ pupitres.set(position,Pupitre.BASS);

                }else if (mainBtn==songsViewHolder.getTenorBtn()){ pupitres.set(position,Pupitre.TENOR);

                }else if (mainBtn==songsViewHolder.getAltoBtn()){ pupitres.set(position,Pupitre.ALTO);

                }else if (mainBtn==songsViewHolder.getSopranoBtn()){ pupitres.set(position,Pupitre.SOPRANO);

                }else{ pupitres.set(position,Pupitre.NA);
                }
            }
        });


    }

    private void setListener2Button(final SongsViewHolder songsViewHolder, final int position, final Button mainBtn, final Button secondBtn) {

        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                songsViewHolder.setColorButton(mainBtn,true);
                songsViewHolder.setColorButton(secondBtn,false);

                if(mainBtn==songsViewHolder.getBsBtn()){ sources.set(position,RecordSource.BANDE_SON);

                }else if(mainBtn==songsViewHolder.getLiveBtn()){ sources.set(position,RecordSource.LIVE);

                }else{ sources.set(position,RecordSource.NA);
                }

            }
        });

    }

    private void initializeSeekbar(SongsViewHolder songsViewHolder) {
        songsViewHolder.getSeekBar().setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }

    private void initializePlaybackController(SongsViewHolder songsViewHolder) {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(context);

        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener(songsViewHolder));
        mPlayerAdapter = mMediaPlayerHolder;

    }



    public class PlaybackListener extends PlaybackInfoListener {

        SongsViewHolder songsViewHolder;

        public PlaybackListener(SongsViewHolder songsViewHolder){
            this.songsViewHolder=songsViewHolder;
        }

        @Override
        public void onDurationChanged(int duration) {
            songsViewHolder.getSeekBar().setMax(duration);
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                songsViewHolder.getSeekBar().setProgress(position);
            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            Log.d(TAG, String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted() {
        }

    }



}
