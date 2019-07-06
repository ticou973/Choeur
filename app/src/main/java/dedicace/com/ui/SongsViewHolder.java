package dedicace.com.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
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

//todo changer toute la logique des listes et importer les listes BS et Live pour chaque liste
public class SongsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, MainActivity.OnPositiveClickListener,View.OnLongClickListener {

    //Element UI
    private TextView titre, groupe;
    private ImageView imageSong, playSongs,stopSongs,recordSongs;
    private Button bsBtn, liveBtn, tuttiBtn, bassBtn, tenorBtn, altoBtn, sopranoBtn;
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
    private List<Song> songOnPhoneRecorded;
    private List<Song> songOnCloudRecorded;
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
    private boolean pauseRecord = false;
    private Song songToRecord;
    private Pupitre recordPupitre;
    private String pathSave;
    private Song songToDownload, songToDelete;
    private String origine;


    //todo prévoir d'effacer les chansons que l'on a soit même enregistré (long click et menu)
    //todo voir pour suppprimer le listener
    //todo voir pour interdire 2 lectures parallèles

    SongsViewHolder(@NonNull View itemView, SongsAdapter.ListemClickedListener listener, Context context) {
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

        //todo vérifier que pas doublons avec le SA
        setButtonActivable(false, bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);

        chronometer.setOnChronometerTickListener(cArg -> {
            long t = SystemClock.elapsedRealtime() - cArg.getBase();
            if(t>=600000) {
                cArg.setText(DateFormat.format("mm:ss", t));
            }else{
                cArg.setText(DateFormat.format("m:ss", t));
            }

            if(t>=mPlayerAdapter.getDuration()){
                setStopListener();
            }
        });

        //créé le player pour chaque viewHolder
        isFirstTime();
       // Log.d(TAG, "SVH SongsViewHolder: "+ mPlayerAdapter);

        animation = (AnimationDrawable) ContextCompat.getDrawable((Context) mlistItemClickedListener, R.drawable.ic_equalizer_white_36dp);
       //todo vérifier la place d'animation start à cet endroit
        if (animation != null) {
            animation.start();
        }

        initPupitreSourceButton();

        totalTime.setText(DateFormat.format("m:ss",0));
    }

    /**Méthode d'initialisation du MediaPlayer et
     * de la Seekbar
     */

    //todo vérifier utilité doublon is First Time
    void isFirstTime(){
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
            if(!origine.equals("Record")) {
                Log.d(TAG, "SVH setRecordSource: cas non Record");
                source = RecordSource.BANDE_SON;
                setSourceActivable(RecordSource.LIVE);
            }else{
                Log.d(TAG, "SVH setRecordSource: cas Record");
                source = RecordSource.LIVE;
                setSourceActivable(RecordSource.BANDE_SON);
            }
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

    private void setCurrentSourceActive(RecordSource source) {
        if(source!=RecordSource.NA) {
            int recordSourceIndex = pupitreSourceButton.indexOf(source);
            setColorButton(true, (Button) pupitreSourceButton.get(recordSourceIndex + 1));
        }
    }

    private void setSourceActivable(RecordSource source) {
        int recordSourceIndex = pupitreSourceButton.indexOf(source);
        setColorButton(false,(Button) pupitreSourceButton.get(recordSourceIndex+1));
    }

    void setSongToPlay(Song songToPlay) {
        if(pupitre != Pupitre.NA&&source !=RecordSource.NA) {
           setSongRecorded(songToPlay);
        }
        this.songToPlay=songToPlay;
        if(songToPlay!=null) {
            pupitre = songToPlay.getPupitre();
            setCurrentSongActive(pupitre);
        }
    }

    void setButtonNonActivable(){
        setButtonActivable(false, bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);
    }

    private void setCurrentSongActive(Pupitre pupitre){
        int pupitreIndex=pupitreSourceButton.indexOf(pupitre);
        setColorButton(true,(Button) pupitreSourceButton.get(pupitreIndex+1));
    }

    void setSongRecorded(Song... recordedLocalSongs){
        for (Song song:recordedLocalSongs) {
            if(song!=null) {
                Pupitre pupitrerecorded = song.getPupitre();

                //todo voir le pb idem pour cloud recorded
                //if (pupitrerecorded != pupitre) {
                   // Log.d(TAG, "SVH setSongRecorded: "+song+" "+recordedLocalSongs);
                    setPupitresLoadedOnPhoneVisible(pupitrerecorded);
                //}
            }
        }
    }

    void setValueCloudSongRecorded(Song... recordedCloudSongs){

        this.recordedCloudSongs=recordedCloudSongs;

    }
    void setValueSongLocalRecorded(Song... recordedLocalSongs){
        this.recordedLocalSongs=recordedLocalSongs;
    }

    private void setPupitresLoadedOnPhoneVisible(Pupitre... pupitres){
        for (Pupitre pupitre: pupitres) {
            int pupitreIndex=pupitreSourceButton.indexOf(pupitre);
            Log.d(TAG, "SVH setPupitresLoadedOnPhoneVisible: "+pupitre+" "+pupitreIndex);
            setColorButton(false,(Button) pupitreSourceButton.get(pupitreIndex+1));
        }
    }

    void setSongCloudRecorded(Song... recordedCloudSongs){

        for (Song song:recordedCloudSongs) {
            Pupitre pupitrerecorded = song.getPupitre();

            //todo voir le cas dans le changement de source ? cf. note plus bas de todo
           // if(pupitrerecorded!=pupitre){
               // Log.d(TAG, "SVH setSongCloudRecorded: "+song+" "+recordedCloudSongs);
            if(!origine.equals("Record")) {
                Log.d(TAG, "SVH setSongCloudRecorded: cas non record");
                setPupitresLoadedOnCloudVisible(pupitrerecorded);
            }else{
                Log.d(TAG, "SVH setSongCloudRecorded: cas Record");
            }
           // }
        }
    }


    private void setPupitresLoadedOnCloudVisible(Pupitre... pupitres){
        for (Pupitre pupitre: pupitres) {
            int pupitreIndex=pupitreSourceButton.indexOf(pupitre);
            //Log.d(TAG, "SVH setPupitresLoadedOnCloudVisible : gris Cloud "+ pupitre);
            setGreyButton(true,(Button) pupitreSourceButton.get(pupitreIndex+1));
        }
    }


    private void setColorButton(boolean focus, Button... buttons) {
        int red, green, blue;
        for (Button button: buttons) {
            if(focus) {
               // Log.d(TAG, "SVH setColorButton: true");
                red = 249;
                green = 191;
                blue = 45;
                button.setAlpha(1.0f);

            }else{
               // Log.d(TAG, "SVH setColorButton: false");
                red = 255;
                green = 241;
                blue = 99;
                button.setAlpha(0.5f);
            }
            button.setBackgroundColor(rgb(red,green,blue));
            button.setEnabled(true);
        }
    }

    private void setGreyButton(boolean focus, Button... buttons){
        int red, green, blue;
        for (Button button: buttons) {
            red = 224;
            green = 224;
            blue = 224;

            if(focus) {
               // Log.d(TAG, "SVH setGreyButton: true");
                button.setAlpha(0.9f);
                button.setEnabled(true);

            }else{
               // Log.d(TAG, "SVH setGreyButton: false");
                button.setAlpha(0.3f); }
            button.setBackgroundColor(rgb(red,green,blue));

        }
    }

    private void setButtonActivable(boolean activable, Button... buttons){
        for (Button button: buttons) {
            if(activable){
               // Log.d(TAG, "SVH setButtonActivable: activable "+ button);
                button.setAlpha(1.0f);
                button.setEnabled(true);

            }else{
              //  Log.d(TAG, "SVH setButtonActivable: non activable "+ button);
                button.setAlpha(0.3f);
                button.setEnabled(false);
                setGreyButton(false,button);
            }
        }
    }

    /** Gestion des clicks sur tous les boutons de l'interface
     * Gestion aussi du longClick pour les téléchargements des songs sur le cloud
     * et pas sur le téléphone.
     * 1.0f chanson active, 0.9f On Cloud grey, 0.5f chanson sur téléphone mais pas active 0.3f chanson inexistante
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //todo faire les 2 cas suivants dès que le système de fichiers est au point voir notamment les recorded dans le handle
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

    public void handleClickSource(RecordSource recordSource, Button button) {

        if(button.getAlpha()==1.0f||button.getAlpha()==0.5f) {
            Log.d(TAG, "handleClickSource: ");
            setSourceActivable(source);
            source=recordSource;
            setCurrentSourceActive(source);

            HandleListSongs(recordSource);

            isFirstTime = true;
            message= source.toString();
            //todo ajouter les cas de record pause et record dans le if
            if (mPlayerAdapter != null&&(mPlayerAdapter.isPlaying()||pause)) {
                setStopListener();
            }
            setResourceToMediaPlayer();

        }else{
            message = source.toString() +" pas encore chargée ! Pour charger, appuyer longuement sur le pupitre désiré ! ";
        }
    }

    private void HandleListSongs(RecordSource recordSource) {

        Log.d(TAG, "SVH HandleListSongs début "+recordSource);

        setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);

        Song[] songsPhone,songsCloud;


        if(recordSource==RecordSource.BANDE_SON) {
            songOnCloudRecorded = mlistItemClickedListener.OnListRecordedSongsOnCloud(getAdapterPosition(),recordSource);

            Log.d(TAG, "SVH HandleListSongs: cas recordSource "+songOnCloudRecorded);
            if(songOnCloudRecorded!=null) {
                Log.d(TAG, "SVH HandleListSongs: SOCR non null"+ songOnCloudRecorded.size());
                songsCloud = songOnCloudRecorded.toArray(new Song[0]);
            }else{
                Log.d(TAG, "SVH HandleListSongs: cas songsOncloudrecorded null");
                songsCloud = new Song[0];
            }

            origine="Default";
            //todo voir ce cas avec le cas différent de pupitre cf. la méthode

            setSongCloudRecorded(songsCloud);

            Log.d(TAG, "SVH HandleListSongs "+ songsCloud.length);

            //setSongCloudRecorded(recordedCloudSongs);
        }

        //todo voir comment calculer le songLocalRecorded ici
          songOnPhoneRecorded = mlistItemClickedListener.OnListRecordedSongsOnPhone(getAdapterPosition(),recordSource);
        if(songOnPhoneRecorded!=null) {
            Log.d(TAG, "SVH HandleListSongs: phones "+songOnPhoneRecorded.size());
            songsPhone = songOnPhoneRecorded.toArray(new Song[0]);
        }else{
            songsPhone = new Song[0];
        }
        setSongRecorded(songsPhone);

        Log.d(TAG, "SA initDataSongs: songsPhone "+ songsPhone.length);

        //todo voir pour utiliser le calcul songToPlay
        if(songsPhone.length!=0) {
            songToPlay = mlistItemClickedListener.OnPlayFirstSong(getAdapterPosition(),recordSource);
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
            //todo idem que pour les sources gérer les cas de record et pause
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
        if(source==RecordSource.BANDE_SON) {
            if (button.getAlpha() == 0.9f) {
                for (Song song : recordedCloudSongs) {
                    if (song.getPupitre() == pupitre && song.getSourceSongTitre().equals(sourceSong.getTitre()) && song.getRecordSource() == source) {
                        songToDownload = song;
                        Log.d(TAG, "SVH handleLongClickPupitre:A download " + songToDownload);
                    }
                }
                Log.d(TAG, "SVH handleLongClickPupitre: single song A" + songToDownload);
                mlistItemClickedListener.OnLongClickItem(getAdapterPosition(), songToDownload);

            } else {
                Toast.makeText(context, "Song déjà chargée sur le téléphone", Toast.LENGTH_SHORT).show();
                for (Song song : recordedLocalSongs) {
                    if (song.getPupitre() == pupitre && song.getSourceSongTitre().equals(sourceSong.getTitre()) && song.getRecordSource() == source) {
                        songToDelete = song;
                        Log.d(TAG, "SVH handleLongClickPupitre:B delete " + songToDelete);
                    }
                }
                Log.d(TAG, "SVH handleLongClickPupitre: delete single song B" + songToDelete);
                mlistItemClickedListener.OnLongClickDeleteItem(getAdapterPosition(), songToDelete);
            }
        }else if(source==RecordSource.LIVE){
            if (button.getAlpha() == 1.0f||button.getAlpha()==0.5f) {
                songOnPhoneRecorded = mlistItemClickedListener.OnListRecordedSongsOnPhone(getAdapterPosition(),source);
                if(songOnPhoneRecorded!=null) {
                    Log.d(TAG, "SVH HandleListSongs: phones B"+songOnPhoneRecorded.size());
                    recordedLocalSongs = songOnPhoneRecorded.toArray(new Song[0]);
                }else{
                    recordedLocalSongs = new Song[0];
                }

                Log.d(TAG, "SVH handleLongClickPupitre: B "+recordedLocalSongs.length);
                for (Song song : recordedLocalSongs) {
                    Log.d(TAG, "SVH handleLongClickPupitre: "+song.getSourceSongTitre()+" "+song.getPupitre()+" "+song.getRecordSource());
                    if (song.getPupitre() == pupitre && song.getSourceSongTitre().equals(sourceSong.getTitre()) && song.getRecordSource() == source) {
                        songToDelete = song;
                        Log.d(TAG, "SVH handleLongClickPupitre:C delete " + songToDelete);
                    }
                }
                Log.d(TAG, "SVH handleLongClickPupitre: delete single song " + songToDelete);
                mlistItemClickedListener.OnLongClickDeleteItem(getAdapterPosition(), songToDelete);

            } else {

            }

        }
    }

    /**
     * Gestion du Mediaplayer et MediaRecorder
     */

    void setResourceToMediaPlayer(){
        Log.d(TAG, "SVH setResourceToMediaPlayer: début "+songToPlay+" "+ pupitre+" "+isFirstTime);
        //Fournit et prépare le Mediaplayer
        if(isFirstTime) {
            //Gestion de la seekBar
            initializeSeekbar();
            //PlayBackController
            initializePlaybackController();
        }
        //todo revoir pour simplifier les cas null de songToPlay
            if (songToPlay != null) {
                if(songToPlay.getPupitre()!=pupitre||songToPlay.getRecordSource()!=source){
                    Log.d(TAG, "SVH setResourceToMediaPlayer: "+songToPlay.getSourceSongTitre()+songToPlay.getPupitre());
                    calculSongToPlay(pupitre,source);
                }

                if(songToPlay!=null) {
                    String resStrToPlay = songToPlay.getSongPath();
                    Log.d(TAG, "SVH setResourceToMediaPlayer: songPath " + resStrToPlay);
                    try {
                        mPlayerAdapter.prepareMediaPlayer(context, resStrToPlay);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isFirstTime = false;
                }else {
                    setTotalTime(0);
                }
            } else {
                setTotalTime(0);
            }
    }

    private void calculSongToPlay(Pupitre pupitre,RecordSource source) {
        if (songOnPhoneRecorded!=null) {
            for (Song song : songOnPhoneRecorded) {
                if (song.getPupitre() == pupitre && song.getRecordSource() == source) {
                    songToPlay = song;
                    Log.d(TAG, "SVH OnPlaySong: " + songToPlay + " " + songToPlay.getPupitre());
                }
            }
        }else{
            songToPlay=null;
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
                        Log.d(TAG, "SVH setPlayListener: avec animation playing "+songToPlay);
                        if(songToPlay!=null) {
                            playSongs.setImageDrawable(animation);
                            pause = false;
                            mPlayerAdapter.play();
                            setChronometerStart();
                        }else{
                            Log.d(TAG, "SVH setPlayListener: rien à jouer");
                            Toast.makeText(context, "Pas de chanson à jouer !", Toast.LENGTH_LONG).show();
                        }

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if(!pauseRecord) {
                    Log.d(TAG, "SVH setRecordListener: pause");
                    mPlayerAdapter.pauseRecord();
                    recordSongs.setImageResource(R.drawable.ic_pause_orange);
                    pauseRecord = true;
                }else{
                    Log.d(TAG, "SVH setRecordListener: restart");
                    mPlayerAdapter.restartRecord();
                    recordSongs.setImageDrawable(animation);
                    pauseRecord=false;
                }
            }else{
                stopRecord();
            }
        }
    }

    @Override
    public void OnRecord(Song song) {
        songToRecord=song;
        setRecord();
    }

    private void setRecord(){

        recordSongs.setImageDrawable(animation);
        isRecording=true;

        recordPupitre = songToRecord.getPupitre();
        String recordSongName = songToRecord.getSourceSongTitre();

        //todo ajouter suivant le nombre d'enregistrement
        String recordNamePupitre = recordSongName+"_"+recordPupitre.toString();

        Log.d(TAG, "SVH setRecord: c'est parti ! "+ recordPupitre+ " "+recordNamePupitre);

        Log.d(TAG, "SVH setRecord: "+mPlayerAdapter);

        pathSave = mPlayerAdapter.record(recordNamePupitre);
    }

    private void setStopListener() {
        if(mPlayerAdapter!=null) {
            Log.d(TAG, "SVH setStopListener: entrée");

            if(isRecording) {
                stopRecord();
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

    private void stopRecord(){
        mPlayerAdapter.stopRecord();
        recordSongs.setImageResource(R.drawable.ic_record_orange);
        Log.d(TAG, "SVH setRecordListener: StopRecord");
        isRecording=false;

        if(pathSave.equals("")) {
            Log.d(TAG, "stopRecord: pb de mémoire externe");
        }else{
            songToRecord.setSongPath(pathSave);
            Log.d(TAG, "SVH setRecord: " + pathSave);
            mlistItemClickedListener.OnSaveRecordSong(songToRecord);
        }


        /*
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
        */


    }

    /**
     * Gestion du chronomètre
     */

    void setChronometer(long time){
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

    private void setTotalTime(int totalTimeMillis) {

        if(totalTimeMillis>=600000){
            this.totalTime.setText(DateFormat.format("mm:ss",totalTimeMillis));

        }else{
            Log.d(TAG, "SVH setTotalTime: "+DateFormat.format("m:ss",totalTimeMillis));
            this.totalTime.setText(DateFormat.format("m:ss",totalTimeMillis));
        }

    }

    ImageView getImageSong() {
        return imageSong;
    }

    @Override
    public void OndeleteSong() {

    }



    /**
     * Getter et setters
     */
    public void setOrigine(String origine) {
        this.origine = origine;
    }

    public void setTitre(String titre){ this.titre.setText(titre); }

    void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }

    public void setGroupe(String groupe){ this.groupe.setText(groupe); }


    public RecordSource getSource() {
        return source;
    }

    ImageView getPlaySongs() {
        return playSongs;
    }

    public Button getLiveBtn() {
        return liveBtn;
    }

    void setListSongLocalRecorded(List<Song> songOnPhoneRecorded) {
        this.songOnPhoneRecorded=songOnPhoneRecorded;
    }

    void setListSongCloudRecorded(List<Song> songOnCloudRecorded) {
        this.songOnCloudRecorded=songOnCloudRecorded;
    }

    /** Interface pour communiquer avec la classe MediaPLayer
     */
    public class PlaybackListener extends PlaybackInfoListener {

        PlaybackListener(){
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
