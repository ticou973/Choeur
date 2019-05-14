package dedicace.com.ui;

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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;

import static android.graphics.Color.rgb;

public class SongsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,MainActivity.OnPositiveClickListener,View.OnLongClickListener {

    //Element UI
    private TextView titre, groupe;
    private ImageView imageSong, playSongs,stopSongs,recordSongs;
    private Button bsBtn, liveBtn, tuttiBtn, bassBtn, tenorBtn, altoBtn, sopranoBtn, mainBtn, secondBtn, thirdBtn, fourthBtn, fifthBtn;
    private SeekBar seekBar;
    private TextView totalTime;
    private Chronometer chronometer;
    private AnimationDrawable animation;

    //Element des songs
    private Pupitre pupitre=Pupitre.NA;
    private RecordSource source=RecordSource.NA;
    private List<RecordSource> recordSources= new ArrayList<>();
    private SourceSong sourceSong = new SourceSong();
    private Song songToPlay;
    private List<Song> songOnPhoneRecorded= new ArrayList<>();
    private List<Song> songOnCloudRecorded= new ArrayList<>();
    private Song[] recordedCloudSongs, recordedLocalSongs;

    private ArrayList pupitreSourceButton = new ArrayList();

    //Mediaplayer
    private PlayerAdapter mPlayerAdapter;

    //Utils
    private final static String TAG = "coucou";
    private boolean isFirstTime=true;
    private SongsAdapter.ListemClickedListener mlistItemClickedListener;
    private boolean mUserIsSeeking = false;
    private boolean isRunning=false;
    private boolean isRecording = false;
    private String message;
    private long lastPause;
    private Context context;
    private boolean pause = false;
    private Song songToRecord;
    private Pupitre recordPupitre;
    private String pathSave;
    private Song songToDownload, songToDelete;


    //todo prévoir d'effacer les chansons que l'on a soit même enregistré (long click et menu)
    //todo voir pour suppprimer le listener
    //todo voir pour interdire 2 lectures parallèles

    public SongsViewHolder(@NonNull View itemView, SongsAdapter.ListemClickedListener listener, Context context) {
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

        this.context=context;
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

        tuttiBtn.setOnLongClickListener(this);
        bassBtn.setOnLongClickListener(this);
        tenorBtn.setOnLongClickListener(this);
        altoBtn.setOnLongClickListener(this);
        sopranoBtn.setOnLongClickListener(this);

        setButtonActivable(false, bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);

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

        //créé le player pour chaque viewHolder
        isFirstTime();
       // Log.d(TAG, "SVH SongsViewHolder: "+ mPlayerAdapter);

        animation = (AnimationDrawable) ContextCompat.getDrawable((Context) mlistItemClickedListener, R.drawable.ic_equalizer_white_36dp);
       //todo vérifier la place d'animation start à cet endroit
        animation.start();

        initPupitreSourceButton();

        totalTime.setText(DateFormat.format("m:ss",0));
    }

    /**Méthode d'initialisation du MediaPlayer et
     * de la Seekbar
     */

    //todo vérifier utilité doublon is First Time
    public void isFirstTime(){
        if(isFirstTime) {
            //PlayBackController
            initializePlaybackController();
            //Gestion de la seekBar
            initializeSeekbar();
            isFirstTime=false;
        }
    }

    private void initializePlaybackController() {
        //todo voir comment récupérer plutôt le context de l'application
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder((Context) mlistItemClickedListener);

        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
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

    /**Méthodes pour l'apparence des boutons de RecordSource
     * ou de pupitres
     */
    private void initPupitreSourceButton() {
        pupitreSourceButton.add(Pupitre.TUTTI);
        pupitreSourceButton.add(tuttiBtn);
        pupitreSourceButton.add(Pupitre.BASS);
        pupitreSourceButton.add(bassBtn);
        pupitreSourceButton.add(Pupitre.TENOR);
        pupitreSourceButton.add(tenorBtn);
        pupitreSourceButton.add(Pupitre.ALTO);
        pupitreSourceButton.add(altoBtn);
        pupitreSourceButton.add(Pupitre.SOPRANO);
        pupitreSourceButton.add(sopranoBtn);
        pupitreSourceButton.add(RecordSource.BANDE_SON);
        pupitreSourceButton.add(bsBtn);
        pupitreSourceButton.add(RecordSource.LIVE);
        pupitreSourceButton.add(liveBtn);
    }

    //todo 1.
    public void setSourceSong(SourceSong sourceSong) {
        this.sourceSong = sourceSong;
    }

    //todo 2.
    public void setRecordSource(List<RecordSource> recordSources) {
        this.recordSources = recordSources;

        if(recordSources.size()==2){
            source=RecordSource.BANDE_SON;
            setSourceActivable(RecordSource.LIVE);
        }else if(recordSources.size()==1){
            if(recordSources.get(0)==RecordSource.BANDE_SON){
                source=RecordSource.BANDE_SON;
            }else if(recordSources.get(0)==RecordSource.LIVE){
                source=RecordSource.LIVE;
            }else if(recordSources.get(0)==RecordSource.NA){
                source=RecordSource.NA;
            }
        }
        if(source!=RecordSource.NA) {
            setCurrentSourceActive(source);
        }
    }

    public void setSource(RecordSource recordSource){
        source=recordSource;
    }

    public void setCurrentSourceActive(RecordSource source) {
        if(source!=RecordSource.NA) {
            int recordSourceIndex = pupitreSourceButton.indexOf(source);
            setColorButton(true, (Button) pupitreSourceButton.get(recordSourceIndex + 1));
        }
    }

    public void setSourceActivable(RecordSource source) {
        int recordSourceIndex = pupitreSourceButton.indexOf(source);
        setColorButton(false,(Button) pupitreSourceButton.get(recordSourceIndex+1));
    }

    public void setSongToPlay(Song songToPlay) {
        if(pupitre != Pupitre.NA&&source !=RecordSource.NA) {
           setSongRecorded(songToPlay);
        }
        this.songToPlay=songToPlay;
        if(songToPlay!=null) {
            pupitre = songToPlay.getPupitre();
            setCurrentSongActive(pupitre);
        }
    }

    public void setButtonNonActivable(){
        setButtonActivable(false, bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);
    }

    public void setCurrentSongActive(Pupitre pupitre){
        int pupitreIndex=pupitreSourceButton.indexOf(pupitre);
        setColorButton(true,(Button) pupitreSourceButton.get(pupitreIndex+1));
    }

    public void setSongRecorded(Song...recordedLocalSongs){

        this.recordedLocalSongs=recordedLocalSongs;
        for (Song song:recordedLocalSongs) {
            if(song!=null) {
                Pupitre pupitrerecorded = song.getPupitre();

                if (pupitrerecorded != pupitre) {
                    setPupitresLoadedOnPhoneVisible(pupitrerecorded);
                }
            }
        }
    }

    public void setPupitresLoadedOnPhoneVisible(Pupitre... pupitres){
        for (Pupitre pupitre: pupitres) {
            int pupitreIndex=pupitreSourceButton.indexOf(pupitre);
            setColorButton(false,(Button) pupitreSourceButton.get(pupitreIndex+1));
        }
    }

    public void setSongCloudRecorded(Song... recordedCloudSongs){
        this.recordedCloudSongs=recordedCloudSongs;
        for (Song song:recordedCloudSongs) {
            Pupitre pupitrerecorded = song.getPupitre();

            if(pupitrerecorded!=pupitre){
                setPupitresLoadedOnCloudVisible(pupitrerecorded);            }
        }
    }


    public void setPupitresLoadedOnCloudVisible(Pupitre... pupitres){
        for (Pupitre pupitre: pupitres) {
            int pupitreIndex=pupitreSourceButton.indexOf(pupitre);
            setGreyButton(true,(Button) pupitreSourceButton.get(pupitreIndex+1));
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
            button.setEnabled(true);
        }
    }

    public void setGreyButton(boolean focus, Button... buttons){
        int red, green, blue;
        for (Button button: buttons) {
            red = 224;
            green = 224;
            blue = 224;

            if(focus) {
                button.setAlpha(0.9f);
                button.setEnabled(true);

            }else{ button.setAlpha(0.3f); }
            button.setBackgroundColor(rgb(red,green,blue));

        }
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

    //todo à voir si on met une variable pour gérer cela

    /** Gestion des clicks sur tous les boutons de l'interface
     * Gestion aussi du longClick pour les téléchargements des songs sur le cloud
     * et pas sur le téléphone.
     * 1.0f chanson active, 0.9f On Cloud grey, 0.5f chanson sur téléphone mais pas active 0.3f chanson inexistante
     * @param view
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //todo faire les 2 cas suivants dès que le système de fichiers est au point
            case R.id.btn_bs :
                handleClickSource(RecordSource.BANDE_SON,bsBtn);
                break;

            case R.id.btn_live:
                handleClickSource(RecordSource.LIVE,liveBtn);
                break;

            case R.id.btn_tutti:
                handleClickPupitre(Pupitre.TUTTI,tuttiBtn);
                break;

            case R.id.btn_bass:
                handleClickPupitre(Pupitre.BASS,bassBtn);
                break;

            case R.id.btn_tenor:
                handleClickPupitre(Pupitre.TENOR,tenorBtn);
                break;

            case R.id.btn_alto:
                Log.d(TAG, "SVH onClick alto : ");
                handleClickPupitre(Pupitre.ALTO,altoBtn);
                break;

            case R.id.btn_soprano:
                handleClickPupitre(Pupitre.SOPRANO,sopranoBtn);
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

    private void handleClickSource(RecordSource recordSource, Button button) {

        if(button.getAlpha()==1.0f||button.getAlpha()==0.5f) {
            Log.d(TAG, "handleClickSource: ");
            setSourceActivable(source);
            source=recordSource;
            setCurrentSourceActive(source);

            HandleListSongs(recordSource,button);

            isFirstTime = true;
            message= source.toString();
            if (mPlayerAdapter != null&&(mPlayerAdapter.isPlaying()||pause)) {
                setStopListener();
            }
            setResourceToMediaPlayer();

        }else{
            message = source.toString() +" pas encore chargée ! Pour charger, appuyer longuement sur le pupitre désiré ! ";
        }
    }

    private void HandleListSongs(RecordSource recordSource, Button button) {

        Log.d(TAG, "SVH HandleListSongs début");

        setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);

        Song[] songsPhone, songsCloud;

        songOnCloudRecorded = mlistItemClickedListener.OnListRecordedSongsOnCloud(sourceSong,recordSource);

        if(songOnCloudRecorded!=null) {
            songsCloud = songOnCloudRecorded.toArray(new Song[0]);
        }else{
            songsCloud = new Song[0];
        }

        setSongCloudRecorded(songsCloud);

        Log.d(TAG, "SVH HandleListSongs "+ songsCloud.length);

        songOnPhoneRecorded = mlistItemClickedListener.OnListRecordedSongsOnPhone(sourceSong,recordSource);
        if(songOnPhoneRecorded!=null) {
            songsPhone = songOnPhoneRecorded.toArray(new Song[0]);
        }else{
            songsPhone = new Song[0];
        }
        setSongRecorded(songsPhone);

        Log.d(TAG, "SA initDataSongs: songsPhone "+ songsPhone.length);

        if(songsPhone.length!=0) {
            songToPlay = mlistItemClickedListener.OnPlayFirstSong(sourceSong,recordSource);
            setSongToPlay(songToPlay);
        }

    }


    private void handleClickPupitre(Pupitre pupitre, Button button) {

        if(button.getAlpha()==1.0f||button.getAlpha()==0.5f) {
            Log.d(TAG, "SVH handleClickPupitre: ");
            setPupitresLoadedOnPhoneVisible(this.pupitre);
            this.pupitre = pupitre;
            setCurrentSongActive(pupitre);
            isFirstTime = true;
            message = pupitre.toString();
            if (mPlayerAdapter != null&&(mPlayerAdapter.isPlaying()||pause)) {
                Log.d(TAG, "SVH handleClickPupitre: setStopListener");
                setStopListener();
            }
            setResourceToMediaPlayer();
        }else{
            message = pupitre.toString() +" non chargé";
        }
    }

    //todo à gérer lorsque l'on téléchargera des songs
    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){

            case R.id.btn_tutti:

                handleLongClickPupitre(Pupitre.TUTTI,tuttiBtn);
                break;

            case R.id.btn_bass:
                handleLongClickPupitre(Pupitre.BASS,bassBtn);
                break;

            case R.id.btn_tenor:
                handleLongClickPupitre(Pupitre.TENOR,tenorBtn);
                break;

            case R.id.btn_alto:
                Log.d(TAG, "SVH onLongClick: Alto");
                handleLongClickPupitre(Pupitre.ALTO,altoBtn);
                break;

            case R.id.btn_soprano:
                handleLongClickPupitre(Pupitre.SOPRANO,sopranoBtn);
                break;
        }

        return true;
    }


    private void handleLongClickPupitre(Pupitre pupitre, Button button) {

        if(button.getAlpha()==0.9f){
            for(Song song : recordedCloudSongs){
                if(song.getPupitre()==pupitre){
                    songToDownload=song;
                    Log.d(TAG, "SVH handleLongClickPupitre:A download "+songToDownload);
                }
            }
            Log.d(TAG, "SVH handleLongClickPupitre: single song "+songToDownload);
            mlistItemClickedListener.OnLongClickItem(getAdapterPosition(),songToDownload);

        }else{
            Toast.makeText(context, "Song déjà chargée sur le téléphone", Toast.LENGTH_SHORT).show();
            for(Song song : recordedLocalSongs){
                if(song.getPupitre()==pupitre){
                    songToDelete=song;
                    Log.d(TAG, "SVH handleLongClickPupitre:B delete "+songToDelete);
                }
            }
            Log.d(TAG, "SVH handleLongClickPupitre: delete single song "+songToDelete);
            mlistItemClickedListener.OnLongClickDeleteItem(getAdapterPosition(),songToDelete);

        }
    }

    /**
     * Gestion du Mediaplayer et MediaRecorder
     */

    public void setResourceToMediaPlayer(){
        Log.d(TAG, "SVH setResourceToMediaPlayer: début "+songToPlay+" "+ pupitre+" "+isFirstTime);
        //Fournit et prépare le Mediaplayer
        if(isFirstTime) {
            //Gestion de la seekBar
            initializeSeekbar();
            //PlayBackController
            initializePlaybackController();
        }
            if (songToPlay != null) {
                if(songToPlay.getPupitre()!=pupitre||songToPlay.getRecordSource()!=source){
                    Log.d(TAG, "SVH setResourceToMediaPlayer: "+songToPlay.getSourceSongTitre()+songToPlay.getPupitre());
                    songToPlay = mlistItemClickedListener.OnPlaySong(sourceSong,pupitre,source);
                }

                String resStrToPlay = songToPlay.getSongPath();
                Log.d(TAG, "SVH setResourceToMediaPlayer: songPath "+resStrToPlay);
                try {
                    mPlayerAdapter.prepareMediaPlayer(context, resStrToPlay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isFirstTime = false;
            } else {
                setTotalTime(0);
            }

    }

    //Méthodes pour les boutons de controle du mediaplayer et mediarecorder
    private void setPlayListener() {
        //todo voir pour la vérification de la présence de la SD pour lecture du LIVE
        if(pupitre==Pupitre.NA||source==RecordSource.NA){
            Toast.makeText((Context) mlistItemClickedListener, "Il manque la chanson demandée !", Toast.LENGTH_SHORT).show();
        }else{
            Log.d(TAG, "SVH setPlayListener: avant firstTime "+isFirstTime );
                    if(isFirstTime) {
                        Log.d(TAG, "SVH setPlayListener: si firstTime" );
                        //Fournit et parépare le Mediaplayer
                        setResourceToMediaPlayer();
                    }
                    if(!mPlayerAdapter.isPlaying()&&!isRecording) {
                        Log.d(TAG, "SVH setPlayListener: avec animation playing" );
                        playSongs.setImageDrawable(animation);
                        pause=false;
                        mPlayerAdapter.play();
                        setChronometerStart();

                    }else if(mPlayerAdapter.isPlaying()&&!isRecording){
                        Log.d(TAG, "SVH setPlayListener: pause" );
                        playSongs.setImageResource(R.drawable.ic_pause_orange);
                        pause = true;
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
            mPlayerAdapter.stopRecord();
            recordSongs.setImageResource(R.drawable.ic_record_orange);
            Log.d(TAG, "SVH setRecordListener: StopRecord");
            isRecording=false;


            //RecordSource UI
            setCurrentSourceActive(RecordSource.LIVE);

            if(source==RecordSource.BANDE_SON){
                setSourceActivable(source);
            }

            //pupitre UI
            setCurrentSongActive(recordPupitre);
            if(source==RecordSource.LIVE){
              setPupitresLoadedOnPhoneVisible(pupitre);
            }

            //verifyExistingSongs();
        }
    }

    public void setRecord(){

        recordSongs.setImageDrawable(animation);

        isRecording=true;
        if (mlistItemClickedListener != null) {
            mlistItemClickedListener.OnRequestPermission();
        }


        recordPupitre = songToRecord.getPupitre();
        String recordSongName = songToRecord.getSourceSongTitre();
        String recordNamePupitre = recordSongName+"_"+recordPupitre.toString();

        Log.d(TAG, "SVH setRecord: c'est parti ! "+ recordPupitre+ " "+recordNamePupitre);

        Log.d(TAG, "SVH setRecord: "+mPlayerAdapter);

        pathSave = mPlayerAdapter.record(recordNamePupitre);

        if(pathSave.equals("")) {

            Log.d(TAG, "setRecord: pb de mémoire externe");

        }else{
            songToRecord.setSongPath(pathSave);
            Log.d(TAG, "SVH setRecord: " + pathSave);
            mlistItemClickedListener.OnSaveRecordSong(songToRecord);
        }
    }

    private void setStopListener() {
        if(mPlayerAdapter!=null) {
            Log.d(TAG, "SVH setStopListener: entrée");

            if(isRecording) {
                mPlayerAdapter.stopRecord();
                recordSongs.setImageResource(R.drawable.ic_record_orange);
                Log.d(TAG, "SVH setRecordListener: StopRecord");
                isRecording=false;
            }else{
                Log.d(TAG, "SVH setStopListener: not recording but playing");
                playSongs.setImageResource(R.drawable.ic_play_orange);
                try {
                    mPlayerAdapter.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setChronometerStop();
            }
        }
    }

    /**
     * Gestion du chronomètre
     */

    public void setChronometer(long time){
        //todo vérifier car cela me semble compliqué pour mettre à 0
        Log.d(TAG, "SVH setChronometer: "+time+" "+chronometer.getBase());
        long t = time - chronometer.getBase();
        chronometer.setText(DateFormat.format("m:ss", time));

        Log.d(TAG, "SVH setChronometer: "+DateFormat.format("m:ss", t));
    }

    private void setChronometerStart(){
        if(!isRunning){
            Log.d(TAG, "SVH setChronometerStart: démarrage");
            chronometer.setBase(SystemClock.elapsedRealtime()-lastPause);
            chronometer.start();
            isRunning=true;
        }
    }

    private void setChronometerPause(){
        if(isRunning){
            Log.d(TAG, "SVH setChronometerPause: ");
            chronometer.stop();
            lastPause=SystemClock.elapsedRealtime()-chronometer.getBase();
            isRunning=false;
        }
    }

    private void setChronometerStop(){
        Log.d(TAG, "SVH setChronometerStop: ");
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setText(DateFormat.format("m:ss", 0));
        lastPause =0;
        isRunning=false;
    }

    public void setTotalTime(int totalTimeMillis) {

        if(totalTimeMillis>=600000){
            this.totalTime.setText(DateFormat.format("mm:ss",totalTimeMillis));

        }else{
            Log.d(TAG, "SVH setTotalTime: "+DateFormat.format("m:ss",totalTimeMillis));
            this.totalTime.setText(DateFormat.format("m:ss",totalTimeMillis));
        }

    }


    @Override
    public void OnRecord(Song song) {
        songToRecord=song;
        setRecord();
    }

    @Override
    public void OndeleteSong() {

       // verifyExistingSongs(RecordSource.LIVE);
    }

    /**
     * Getter et setters
     */

    public void setTitre(String titre){ this.titre.setText(titre); }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }

    public void setGroupe(String groupe){ this.groupe.setText(groupe); }

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

    public RecordSource getSource() {
        return source;
    }

    public ImageView getPlaySongs() {
        return playSongs;
    }

    /** Interface pour communiquer avec la classe MediaPLayer
     */
    public class PlaybackListener extends PlaybackInfoListener {

        public PlaybackListener(){
        }

        @Override
        public void onDurationChanged(int duration) {
            Log.d(TAG, "SVH onDurationChanged: "+duration);
            setTotalTime(duration);
            seekBar.setMax(duration);

        }

        @Override
        public void onPositionChanged(int position) {
            Log.d(TAG, "SVH onPositionChanged: "+position);
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
            Log.d(TAG, "onTimeChanged: ");
            setStopListener();
            setChronometerStop();
        }
    }
}
