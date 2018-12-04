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
    private List<Song> choeurSongs;
    private Context context;
    private boolean mUserIsSeeking = false;

    private List<Pupitre> pupitres = new ArrayList<>();
    private List<RecordSource> sources = new ArrayList<>();
    private  List<Boolean> isFirstTime = new ArrayList<>();
    private List<Boolean> isBs = new ArrayList<>();
    private List<Boolean> isLive = new ArrayList<>();
    private List<Boolean> isTutti = new ArrayList<>();
    private List<Boolean> isBass = new ArrayList<>();
    private List<Boolean> isTenor = new ArrayList<>();
    private List<Boolean> isAlto = new ArrayList<>();
    private List<Boolean> isSoprano = new ArrayList<>();
    private boolean dispo[][] = new boolean[2][6];
    private int i,j;

    private Button bsBtn;
    private Button liveBtn;
    private Button tuttiBtn;
    private Button bassBtn;
    private Button tenorBtn;
    private Button altoBtn;
    private Button sopranoBtn;


    private PlayerAdapter mPlayerAdapter;

    public static final String TAG = "coucou";


    public SongsAdapter(List<SourceSong> songs, Context context) {
        this.songs = songs;
        this.context = context;

        initData();
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_songs, viewGroup, false);

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

        VerifyExistingSongs(songsViewHolder,position);

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


    private void VerifyExistingSongs(SongsViewHolder songsViewHolder, int position) {

        bsBtn = songsViewHolder.getBsBtn();
        liveBtn = songsViewHolder.getLiveBtn();
        tuttiBtn = songsViewHolder.getTuttiBtn();
        bassBtn = songsViewHolder.getBassBtn();
        tenorBtn = songsViewHolder.getTenorBtn();
        altoBtn = songsViewHolder.getAltoBtn();
        sopranoBtn = songsViewHolder.getSopranoBtn();

        /*List<Button> buttons = new ArrayList<>();

        buttons.add(bsBtn);
        buttons.add(liveBtn);
        buttons.add(tuttiBtn);
        buttons.add(bassBtn);
        buttons.add(tenorBtn);
        buttons.add(altoBtn);
        buttons.add(sopranoBtn);*/

        choeurSongs = MainActivity.choeurDataBase.songsDao().getSongsBySourceSong(songs.get(position).getTitre());

        if(choeurSongs.size()>0) {

            Song currentSong = choeurSongs.get(0);

            Log.d(TAG, "VerifyExistingSongs: " + currentSong.getSourceSong().getTitre());

        }

        if(choeurSongs.size()==0){

            songsViewHolder.setButtonActivable(false,bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);

        }else{

            setActivableBtn(songsViewHolder,choeurSongs,position);

            //setButtonActivables(songsViewHolder,choeurSongs);

            //getSongsPossible(choeurSongs,position);

        }

        /*if(songBTU!=null){
            sources.set(position,RecordSource.BANDE_SON);
            pupitres.set(position,Pupitre.TUTTI);
            songsViewHolder.setColorButton(true,bsBtn,tuttiBtn);
        }*/

    }

    private void getSongsPossible(List<Song> choeurSongs, int position) {

        String titre = songs.get(position).getTitre();

        Song songBTU = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TUTTI,RecordSource.BANDE_SON);
        Song songBBA = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.BASS,RecordSource.BANDE_SON);
        Song songBTE = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,RecordSource.BANDE_SON);
        Song songBAL = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.BANDE_SON);
        Song songBSO = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.BANDE_SON);
        Song songLTU = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TUTTI,RecordSource.LIVE);
        Song songLBA = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.BASS,RecordSource.LIVE);
        Song songLTE = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,RecordSource.LIVE);
        Song songLAL = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.LIVE);
        Song songLSO = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.LIVE);

        List<Song> possibles = new ArrayList<>();

        possibles.add(songBTU);
        possibles.add(songBBA);
        possibles.add(songBTE);
        possibles.add(songBAL);
        possibles.add(songBSO);
        possibles.add(songLTU);
        possibles.add(songLBA);
        possibles.add(songLTE);
        possibles.add(songLAL);
        possibles.add(songLSO);


        List<Song> activables = new ArrayList<>();
        for (Song possible: possibles) if(possible!=null) activables.add(possible);

        List<Song> nonActivables =new ArrayList<>();
        for (Song possible: possibles) if(possible==null) nonActivables.add(possible);


    }


    private void setButtonActivables(SongsViewHolder songsViewHolder, List<Song> choeurSongs) {

        List<RecordSource> localSources = new ArrayList<>();
        localSources.add(RecordSource.BANDE_SON);
        localSources.add(RecordSource.LIVE);

        List<Pupitre> localPupitres = new ArrayList<>();
        localPupitres.add(Pupitre.TUTTI);
        localPupitres.add(Pupitre.BASS);
        localPupitres.add(Pupitre.TENOR);
        localPupitres.add(Pupitre.ALTO);
        localPupitres.add(Pupitre.SOPRANO);


        for (Song song: choeurSongs) {
            Pupitre pupitre = song.getPupitre();
            RecordSource source = song.getRecordSource();

            for (RecordSource recordSource: localSources) {

                if(source==recordSource){

                    if(source==RecordSource.BANDE_SON) { songsViewHolder.setButtonActivable(true,bsBtn);
                    }else{ songsViewHolder.setButtonActivable(false,bsBtn); }

                    if(source==RecordSource.LIVE) { songsViewHolder.setButtonActivable(true,liveBtn); }
                    else{ songsViewHolder.setButtonActivable(false,liveBtn); }


                    for (Pupitre pupitre1 : localPupitres) {

                        if(pupitre==pupitre1){

                            if(pupitre==Pupitre.TUTTI){ songsViewHolder.setButtonActivable(true,tuttiBtn); }
                            else{ songsViewHolder.setButtonActivable(false,tuttiBtn); }

                            if(pupitre==Pupitre.BASS){ songsViewHolder.setButtonActivable(true,bassBtn);
                            }else{ songsViewHolder.setButtonActivable(false,bassBtn); }

                            if(pupitre==Pupitre.TENOR){ songsViewHolder.setButtonActivable(true,tenorBtn);
                            }else{ songsViewHolder.setButtonActivable(false,tenorBtn); }

                            if(pupitre==Pupitre.ALTO){ songsViewHolder.setButtonActivable(true,altoBtn);
                            }else{ songsViewHolder.setButtonActivable(false,altoBtn); }

                            if(pupitre==Pupitre.SOPRANO){ songsViewHolder.setButtonActivable(true,sopranoBtn);
                            }else{ songsViewHolder.setButtonActivable(false,sopranoBtn); }
                        }
                    }
                }
            }

        }
    }


    public void setActivableBtn(SongsViewHolder songsViewHolder, List<Song> choeurSongs, int position){

        String titre = songs.get(position).getTitre();
        Log.d(TAG, "setActivableBtn: "+titre);
        RecordSource recordSourceBS= RecordSource.BANDE_SON;
        RecordSource recordSourceLive = RecordSource.LIVE;

        List<Song> songsBS = MainActivity.choeurDataBase.songsDao().getSongOrderedByPupitre(titre,recordSourceBS);
        List<Song> songsLive = MainActivity.choeurDataBase.songsDao().getSongOrderedByPupitre(titre,recordSourceLive);


        for (Song song:songsBS) {
            Log.d(TAG, "setActivableBtn: BS "+song.getPupitre().toString()+song.getRecordSource().toString());        }

        for (Song song:songsLive) {
            Log.d(TAG, "setActivableBtn: Live "+song.getPupitre().toString()+song.getRecordSource().toString());
        }

        if(songsBS.size()==0){
            songsViewHolder.setButtonActivable(false,bsBtn);
        }

        if(songsLive.size()==0){
            songsViewHolder.setButtonActivable(false,liveBtn);
        }


        if(songsBS.size()!=0){
            songsViewHolder.setButtonActivable(true,bsBtn);
            songsViewHolder.setColorButton(true,bsBtn);
            sources.set(position,RecordSource.BANDE_SON);

            if(MainActivity.choeurDataBase.songsDao().getSongsByTitreSource(titre,RecordSource.LIVE)==null){
                songsViewHolder.setButtonActivable(false,liveBtn);
            }

            for (Song songBS: songsBS) {

                Pupitre pupitre = songBS.getPupitre();

                if(pupitre==Pupitre.TUTTI){
                    songsViewHolder.setButtonActivable(true,tuttiBtn);
                    songsViewHolder.setColorButton(true,tuttiBtn);
                    pupitres.set(position,Pupitre.TUTTI);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.BASS,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,bassBtn);
                    }
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,tenorBtn);
                    }
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,altoBtn);
                    }
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,sopranoBtn);
                    }
                    return;
                }else if(pupitre==Pupitre.BASS){
                    songsViewHolder.setButtonActivable(false,tuttiBtn);
                    songsViewHolder.setButtonActivable(true,bassBtn);
                    songsViewHolder.setColorButton(true,bassBtn);
                    pupitres.set(position,Pupitre.BASS);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,tenorBtn);
                    }
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,altoBtn);
                    }
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,sopranoBtn);
                    }
                    return;
                }else if(pupitre==Pupitre.TENOR){
                    songsViewHolder.setButtonActivable(false,tuttiBtn,bassBtn);
                    songsViewHolder.setButtonActivable(true,tenorBtn);
                    songsViewHolder.setColorButton(true,tenorBtn);
                    pupitres.set(position,Pupitre.TENOR);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,altoBtn);
                    }
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,sopranoBtn);
                    }
                    return;
                }else if(pupitre==Pupitre.ALTO){
                    songsViewHolder.setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn);
                    songsViewHolder.setButtonActivable(true,altoBtn);
                    songsViewHolder.setColorButton(true,altoBtn);
                    pupitres.set(position,Pupitre.ALTO);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.BANDE_SON)==null){
                        songsViewHolder.setButtonActivable(false,sopranoBtn);
                    }
                    return;
                }else if(pupitre==Pupitre.SOPRANO){
                    songsViewHolder.setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn);
                    songsViewHolder.setButtonActivable(true,sopranoBtn);
                    songsViewHolder.setColorButton(true,sopranoBtn);
                    pupitres.set(position,Pupitre.SOPRANO);
                    return;
                }

             }
        }else if(songsLive.size()!=0){
                songsViewHolder.setButtonActivable(true,liveBtn);
                songsViewHolder.setColorButton(true,liveBtn);
                sources.set(position,RecordSource.LIVE);

                for (Song songLive: songsLive) {

                    Pupitre pupitre = songLive.getPupitre();

                    if(pupitre==Pupitre.TUTTI){
                        songsViewHolder.setButtonActivable(true,tuttiBtn);
                        songsViewHolder.setColorButton(true,tuttiBtn);
                        pupitres.set(position,Pupitre.TUTTI);
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.BASS,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,bassBtn);
                        }
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,tenorBtn);
                        }
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,altoBtn);
                        }
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,sopranoBtn);
                        }

                        return;
                    }else if(pupitre==Pupitre.BASS){
                        songsViewHolder.setButtonActivable(false,tuttiBtn);
                        songsViewHolder.setButtonActivable(true,bassBtn);
                        songsViewHolder.setColorButton(true,bassBtn);
                        pupitres.set(position,Pupitre.BASS);
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,tenorBtn);
                        }
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,altoBtn);
                        }
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,sopranoBtn);
                        }
                        return;
                    }else if(pupitre==Pupitre.TENOR){
                        songsViewHolder.setButtonActivable(false,tuttiBtn,bassBtn);
                        songsViewHolder.setButtonActivable(true,tenorBtn);
                        songsViewHolder.setColorButton(true,tenorBtn);
                        pupitres.set(position,Pupitre.TENOR);
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,altoBtn);
                        }
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,sopranoBtn);
                        }
                        return;
                    }else if(pupitre==Pupitre.ALTO){
                        songsViewHolder.setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn);
                        songsViewHolder.setButtonActivable(true,altoBtn);
                        songsViewHolder.setColorButton(true,altoBtn);
                        pupitres.set(position,Pupitre.ALTO);
                        if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,RecordSource.LIVE)==null){
                            songsViewHolder.setButtonActivable(false,sopranoBtn);
                        }
                        return;
                    }else if(pupitre==Pupitre.SOPRANO){
                        songsViewHolder.setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn);
                        songsViewHolder.setButtonActivable(true,sopranoBtn);
                        songsViewHolder.setColorButton(true,sopranoBtn);
                        pupitres.set(position,Pupitre.SOPRANO);
                        return;
                    }
                }

            }


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
            isBs.add(false);
            isAlto.add(false);
            isBass.add(false);
            isSoprano.add(false);
            isTenor.add(false);
            isLive.add(false);
            isTutti.add(false);
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

                Log.d(TAG, "onClick: "+ position);

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

                songsViewHolder.setColorButton(true,mainBtn);
                songsViewHolder.setColorButton(false,secondBtn,thirdBtn,fourthBtn,fifthBtn);

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

                songsViewHolder.setColorButton(true,mainBtn);
                songsViewHolder.setColorButton(false,secondBtn);

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
