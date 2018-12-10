package dedicace.com;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import static android.graphics.Color.rgb;

public class SongsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,MainActivity.OnPositiveClickListener {

    private TextView titre, groupe;
    private ImageView imageSong, playSongs,stopSongs,recordSongs;
    private Button bsBtn, liveBtn, tuttiBtn, bassBtn, tenorBtn, altoBtn, sopranoBtn, mainBtn, secondBtn, thirdBtn, fourthBtn, fifthBtn;
    private SeekBar seekBar;
    private Pupitre pupitre=Pupitre.NA,recordPupitre;
    private RecordSource source=RecordSource.NA;
    private final static String TAG = "coucou";
    private SourceSong sourceSong;
    private List<Song> choeurSongs;
    private boolean isFirstTime=true;
    private boolean mUserIsSeeking = false;
    private String message;
    private TextView totalTime;
    private Chronometer chronometer;
    private long lastPause;
    private boolean isRunning=false;
    private boolean isRecording = false;
    private AnimationDrawable animation;

    private String pathSave="";


    private PlayerAdapter mPlayerAdapter;
    private SongsAdapter.ListemClickedListener mlistItemClickedListener;

    //todo prévoir d'effacer les chansons que l'on a soit même enregistré (long click et menu)
    //todo

    public SongsViewHolder(@NonNull View itemView, SongsAdapter.ListemClickedListener listener) {
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
        totalTime=itemView.findViewById(R.id.total_time);
        chronometer=itemView.findViewById(R.id.chronometre);

        mlistItemClickedListener=listener;

        bsBtn.setOnClickListener(this);
        liveBtn.setOnClickListener(this);
        tuttiBtn.setOnClickListener(this);
        bassBtn.setOnClickListener(this);
        tenorBtn.setOnClickListener(this);
        altoBtn.setOnClickListener(this);
        sopranoBtn.setOnClickListener(this);
        playSongs.setOnClickListener(this);
        recordSongs.setOnClickListener(this);
        stopSongs.setOnClickListener(this);

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer cArg) {

                long t = SystemClock.elapsedRealtime() - cArg.getBase();
                if(t>=600000) {
                    cArg.setText(DateFormat.format("mm:ss", t));
                }else{
                    cArg.setText(DateFormat.format("m:ss", t));
                }

                if(t>=mPlayerAdapter.getDuration()){
                    setStopListener();
                }
            }
        });

        isFirstTime();
        Log.d(TAG, "SongsViewHolder: "+mPlayerAdapter);

        animation = (AnimationDrawable) ContextCompat.getDrawable((Context) mlistItemClickedListener, R.drawable.ic_equalizer_white_36dp);
        animation.start();
    }


    public void setButtonActivable(boolean activable, Button... buttons){

        for (Button button: buttons) {
            if(activable){
                button.setAlpha(1.0f);
                button.setEnabled(true);

            }else{
                button.setAlpha(0.3f);
                button.setEnabled(false);
                setGreyButton(false,button);
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

            }else{ button.setAlpha(0.3f); }



            button.setBackgroundColor(rgb(red,green,blue));

        }
    }

    public void setTitre(String titre){

        this.titre.setText(titre);
    }

    public void setGroupe(String groupe){

        this.groupe.setText(groupe);
    }

    public ImageView getStopSongs() {
        return stopSongs;
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

    public void setTotalTime(int totalTimeMillis) {
        SimpleDateFormat simpleDateFormat;

        if(totalTimeMillis>=600000){

            simpleDateFormat = new SimpleDateFormat("mm:ss");

        }else{
            simpleDateFormat = new SimpleDateFormat("m:ss");
        }
        String totalTimeSong = simpleDateFormat.format(totalTimeMillis);
        this.totalTime.setText(totalTimeSong);
    }

    public void setSourceSong(SourceSong sourceSong) {
        this.sourceSong = sourceSong;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btn_bs :
                source=RecordSource.BANDE_SON;
                pupitre=Pupitre.NA;
                setActivableBtn(source);
                setColorButton(true,bsBtn);
                isFirstTime=true;
                message="Bande Son";
                //setResourceToMediaPlayer();
                if(mPlayerAdapter!=null) {
                    setStopListener();
                }

                break;

            case R.id.btn_live:
                source=RecordSource.LIVE;
                pupitre=Pupitre.NA;
                setActivableBtn(source);
                setColorButton(true,liveBtn);
                isFirstTime=true;
                message="Live";
                //setResourceToMediaPlayer();

                if(mPlayerAdapter!=null) {
                    setStopListener();
                }

                break;

            case R.id.btn_tutti:
                pupitre=Pupitre.TUTTI;
                setActivableBtn(source);
                setColorButton(true,tuttiBtn);
                isFirstTime=true;
                message="Tutti";
                if(mPlayerAdapter!=null) {
                    setStopListener();
                }
                //setResourceToMediaPlayer();
                break;

            case R.id.btn_bass:
                pupitre=Pupitre.BASS;
                setActivableBtn(source);
                setColorButton(true,bassBtn);
                isFirstTime=true;
                message="Basse";

                //setResourceToMediaPlayer();
                if(mPlayerAdapter!=null) {
                    setStopListener();
                }
                break;

            case R.id.btn_tenor:
                pupitre=Pupitre.TENOR;
                setActivableBtn(source);
                setColorButton(true,tenorBtn);
                isFirstTime=true;
                message="Tenor";

                //setResourceToMediaPlayer();
                if(mPlayerAdapter!=null) {
                    setStopListener();
                }
                break;

            case R.id.btn_alto:
                pupitre=Pupitre.ALTO;
                setActivableBtn(source);
                setColorButton(true,altoBtn);
                isFirstTime=true;
                message="Alto";

                //setResourceToMediaPlayer();
                if(mPlayerAdapter!=null) {
                    setStopListener();
                }
                break;

            case R.id.btn_soprano:
                pupitre=Pupitre.SOPRANO;
                setActivableBtn(source);
                setColorButton(true,sopranoBtn);
                isFirstTime=true;
                message="Soprano";

               // setResourceToMediaPlayer();
                if(mPlayerAdapter!=null) {
                    setStopListener();
                }
                break;

            case R.id.play_image:
                if(!isRecording) {
                    message = "Lecture";
                    setPlayListener();
                }
                break;

            case R.id.recordSongs:
                if(!mPlayerAdapter.isPlaying()) {
                    message = "Enregistrement";
                }
                setRecordListener();

                break;

            case R.id.stopSongs:
                message="Arrêt";
                setStopListener();
                break;
        }
        String titreText=sourceSong.getTitre();
        mlistItemClickedListener.OnClickedItem(titreText,message);
    }

    public void setActivableBtn(RecordSource mainSource){
        String titre = sourceSong.getTitre();
        RecordSource secondSource = RecordSource.NA;
        Button mainBtn=null,secondBtn=null;

        resetButton();

        if(mainSource==RecordSource.BANDE_SON){
            secondSource=RecordSource.LIVE;
            mainBtn = bsBtn;
            secondBtn = liveBtn;

        }else if(mainSource==RecordSource.LIVE){
            secondSource=RecordSource.BANDE_SON;
            mainBtn = liveBtn;
            secondBtn = bsBtn;


        }

        List<Song> songsMainSource = MainActivity.choeurDataBase.songsDao().getSongOrderedByPupitre(titre,mainSource);
        List<Song> songsSecondSource = MainActivity.choeurDataBase.songsDao().getSongOrderedByPupitre(titre,secondSource);

        if(songsMainSource.size()==0){
            setButtonActivable(false,mainBtn);
        }


        if(songsMainSource.size()!=0){

            setButtonActivable(true,mainBtn);
            setColorButton(true,mainBtn);
            source=mainSource;

            if(MainActivity.choeurDataBase.songsDao().getSongsByTitreSource(titre,secondSource).size()==0){
                setButtonActivable(false,secondBtn);

            }else{

                setColorButton(false,secondBtn);
            }

            for (Song mainSourceSong: songsMainSource) {

                Pupitre tempPupitre = mainSourceSong.getPupitre();

                if(tempPupitre==Pupitre.TUTTI){
                    setButtonActivable(true,tuttiBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.TUTTI){
                        setColorButton(true,tuttiBtn);
                        pupitre=Pupitre.TUTTI;
                    }else setColorButton(false,tuttiBtn);

                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.BASS,mainSource)==null){
                        setButtonActivable(false,bassBtn);
                    }else{setColorButton(false,bassBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,mainSource)==null){
                        setButtonActivable(false,tenorBtn);
                    }else{setColorButton(false,tenorBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,mainSource)==null){
                        setButtonActivable(false,altoBtn);
                    }else{setColorButton(false,altoBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,mainSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.BASS){
                    setButtonActivable(false,tuttiBtn);
                    setButtonActivable(true,bassBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.BASS){
                        setColorButton(true,bassBtn);
                        pupitre=Pupitre.BASS;
                    }else setColorButton(false,bassBtn);

                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,mainSource)==null){
                        setButtonActivable(false,tenorBtn);
                    }else{setColorButton(false,tenorBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,mainSource)==null){
                        setButtonActivable(false,altoBtn);
                    }else{setColorButton(false,altoBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,mainSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.TENOR){
                    setButtonActivable(false,tuttiBtn,bassBtn);
                    setButtonActivable(true,tenorBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.TENOR){
                        setColorButton(true,tenorBtn);
                        pupitre=Pupitre.TENOR;
                    }else setColorButton(false,tenorBtn);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,mainSource)==null){
                        setButtonActivable(false,altoBtn);
                    }else{setColorButton(false,altoBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,mainSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.ALTO){

                    setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn);
                    setButtonActivable(true,altoBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.ALTO){
                        setColorButton(true,altoBtn);
                        pupitre=Pupitre.ALTO;
                    }else setColorButton(false,altoBtn);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,mainSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.SOPRANO){

                    setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn);
                    setButtonActivable(true,sopranoBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.SOPRANO){
                        setColorButton(true,sopranoBtn);
                        pupitre=Pupitre.SOPRANO;
                    }else setColorButton(false,sopranoBtn);
                    return;
                }

            }
        }else if(songsSecondSource.size()!=0){
            setButtonActivable(true,secondBtn);
            setColorButton(true,secondBtn);
            source=secondSource;

            for (Song secondSourceSong: songsSecondSource) {

                Pupitre tempPupitre = secondSourceSong.getPupitre();

                if(tempPupitre==Pupitre.TUTTI){
                    setButtonActivable(true,tuttiBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.TUTTI){
                        setColorButton(true,tuttiBtn);
                        pupitre=Pupitre.TUTTI;
                    }else setColorButton(false,tuttiBtn);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.BASS,secondSource)==null){
                        setButtonActivable(false,bassBtn);
                    }else{setColorButton(false,bassBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,secondSource)==null){
                        setButtonActivable(false,tenorBtn);
                    }else{setColorButton(false,tenorBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,secondSource)==null){
                        setButtonActivable(false,altoBtn);
                    }else{setColorButton(false,altoBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,secondSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}

                    return;
                }else if(tempPupitre==Pupitre.BASS){
                    setButtonActivable(false,tuttiBtn);
                    setButtonActivable(true,bassBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.BASS){
                        setColorButton(true,bassBtn);
                        pupitre=Pupitre.BASS;
                    }else setColorButton(false,bassBtn);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.TENOR,secondSource)==null){
                        setButtonActivable(false,tenorBtn);
                    }else{setColorButton(false,tenorBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,secondSource)==null){
                        setButtonActivable(false,altoBtn);
                    }else{setColorButton(false,altoBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,secondSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.TENOR){
                    setButtonActivable(false,tuttiBtn,bassBtn);
                    setButtonActivable(true,tenorBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.TENOR){
                        setColorButton(true,tenorBtn);
                        pupitre=Pupitre.TENOR;
                    }else setColorButton(false,tenorBtn);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,secondSource)==null){
                        setButtonActivable(false,altoBtn);
                    }else{setColorButton(false,altoBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,secondSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.ALTO){
                    setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn);
                    setButtonActivable(true,altoBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.ALTO){
                        setColorButton(true,altoBtn);
                        pupitre=Pupitre.ALTO;
                    }else setColorButton(false,altoBtn);
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,secondSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.SOPRANO){
                    setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn);
                    setButtonActivable(true,sopranoBtn);
                    if(pupitre==Pupitre.NA||pupitre==Pupitre.SOPRANO){
                        setColorButton(true,sopranoBtn);
                        pupitre=Pupitre.SOPRANO;
                    }else setColorButton(false,sopranoBtn);
                    return;
                }
            }

        }

    }

    private void resetButton() {
        setButtonActivable(true,bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);
        setGreyButton(true, bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);
    }

    public void verifyExistingSongs(RecordSource source) {

        choeurSongs = MainActivity.choeurDataBase.songsDao().getSongsBySourceSong(sourceSong.getTitre());

        if(choeurSongs.size()==0){

            setButtonActivable(false,bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);
            seekBar.setEnabled(false);
        }else{
            Log.d(TAG, "SVH verifyExistingSongs: ");
            setActivableBtn(source); }
    }

    //todo vérifier utilité doublon is First Time


    public void isFirstTime(){
        if(isFirstTime) {
            //PlayBackController
            initializePlaybackController();

            //Gestion de la seekBar
            initializeSeekbar();
        }
    }

    private void setTempDuration(){

        Song songToPlay = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(sourceSong.getTitre(), pupitre, source);
        if (songToPlay != null) {
            Log.d(TAG, "SVH setResourceToMediaPlayer: " + pupitre + source);
            String resStrToPlay = songToPlay.getSongPath();
            mPlayerAdapter.prepareMediaPlayer((Context) mlistItemClickedListener, resStrToPlay);
        } else {
            setTotalTime(0);
        }

    }

    public void setResourceToMediaPlayer(){
        //Fournit et prépare le Mediaplayer
        if(isFirstTime) {
            Song songToPlay = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(sourceSong.getTitre(), pupitre, source);
            if (songToPlay != null) {
                Log.d(TAG, "SVH setResourceToMediaPlayer: " + pupitre + source);
                String resStrToPlay = songToPlay.getSongPath();
                mPlayerAdapter.prepareMediaPlayer((Context) mlistItemClickedListener, resStrToPlay);
                isFirstTime = false;
            } else {
                setTotalTime(0);
            }
        }
    }

    //Méthodes pour les boutons de controle du mediaplayer et mediarecorder
    private void setPlayListener() {
        if(pupitre==Pupitre.NA||source==RecordSource.NA){


        }else{

                    //todo voir si à enlever car au dessus
                    if(isFirstTime) {
                        //Gestion de la seekBar
                        initializeSeekbar();
                        Log.d(TAG, "SVH setPlayListener: isFirst Time");

                        Log.d(TAG, "setPlayListener: A");
                        //PlayBackController
                        initializePlaybackController();

                        //Fournit et parépare le Mediaplayer
                        Song songToPlay = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(sourceSong.getTitre(),pupitre,source);
                        String resStrToPlay = songToPlay.getSongPath();

                        mPlayerAdapter.prepareMediaPlayer((Context) mlistItemClickedListener, resStrToPlay);
                        isFirstTime=false;
                    }

                    if(!mPlayerAdapter.isPlaying()&&!isRecording) {

                        playSongs.setImageDrawable(animation);
                        mPlayerAdapter.play();
                        setChronometerStart();

                    }else if(mPlayerAdapter.isPlaying()&&!isRecording){
                        playSongs.setImageResource(R.drawable.ic_pause_orange);
                        mPlayerAdapter.pause();
                        setChronometerPause();
                    }
                }
    }

    private void setRecordListener() {
        if(!isRecording&&!mPlayerAdapter.isPlaying()) {
            if (mlistItemClickedListener != null) {
                mlistItemClickedListener.OnDialogRecord(getAdapterPosition(),this);
                Log.d(TAG, "SVH setRecordListener: ");
            }

        }else if(isRecording&&!mPlayerAdapter.isPlaying()){
            /*mPlayerAdapter.stopRecord();
            recordSongs.setImageResource(R.drawable.ic_record_orange);
            Log.d(TAG, "SVH setRecordListener: StopRecord");
            isRecording=false;
            verifyExistingSongs();*/
        }
    }

    public void setRecord(){

        recordSongs.setImageDrawable(animation);

        isRecording=true;
        if (mlistItemClickedListener != null) {
            mlistItemClickedListener.OnRequestPermission();
        }

        Song songToRecord =MainActivity.choeurDataBase.songsDao().getLastSong();
        recordPupitre = songToRecord.getPupitre();
        String recordSongName = songToRecord.getSourceSong().getTitre();
        String recordNamePupitre = recordSongName+"_"+recordPupitre.toString();

        Log.d(TAG, "SVH setRecord: c'est parti ! "+ recordPupitre);

        Log.d(TAG, "SVH setRecord: "+mPlayerAdapter);

        pathSave = mPlayerAdapter.record(recordNamePupitre);

        songToRecord.setSongPath(pathSave);
        Log.d(TAG, "SVH setRecord: "+pathSave);
        MainActivity.choeurDataBase.songsDao().updateSong(songToRecord);

        String path = MainActivity.choeurDataBase.songsDao().getLastSong().getSongPath();

        Log.d(TAG, "SVH setRecord: "+path);
    }


    private void setStopListener() {
        if(mPlayerAdapter!=null) {

            if(isRecording) {
                mPlayerAdapter.stopRecord();
                recordSongs.setImageResource(R.drawable.ic_record_orange);
                Log.d(TAG, "SVH setRecordListener: StopRecord");
                isRecording=false;
                verifyExistingSongs(RecordSource.LIVE);
            }else{
                playSongs.setImageResource(R.drawable.ic_play_orange);
                mPlayerAdapter.reset();
                setChronometerStop();


            }
        }
    }


    private void initializeSeekbar() {
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                        setChronometerPause();
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                            chronometer.setText(DateFormat.format("m:ss", progress));
                            lastPause=progress;

                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(userSelectedPosition);
                        if(mPlayerAdapter.isPlaying()){
                            setChronometerStart();
                        }


                    }
                });
    }

    private void initializePlaybackController() {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder((Context) mlistItemClickedListener);

        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
    }

    private void setChronometerStart(){
        if(!isRunning){
            chronometer.setBase(SystemClock.elapsedRealtime()-lastPause);
            chronometer.start();
            isRunning=true;
        }
    }

    private void setChronometerPause(){
        if(isRunning){
            chronometer.stop();
            lastPause=SystemClock.elapsedRealtime()-chronometer.getBase();
            isRunning=false;
        }
    }

    private void setChronometerStop(){
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setText(DateFormat.format("m:ss", 0));
        lastPause =0;
        isRunning=false;
    }

    public void setChronometer(long time){
        long t = time - chronometer.getBase();
        chronometer.setText(DateFormat.format("m:ss", t));

    }

    @Override
    public void OnRecord(Pupitre pupitre) {
       setRecord();
    }

    public class PlaybackListener extends PlaybackInfoListener {

        public PlaybackListener(){
        }

        @Override
        public void onDurationChanged(int duration) {
            seekBar.setMax(duration);
            setTotalTime(duration);
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                seekBar.setProgress(position);
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

        @Override
        void onTimeChanged() {
            setStopListener();
            setChronometerStop();
        }

    }



}
