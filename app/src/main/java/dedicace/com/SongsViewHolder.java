package dedicace.com;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import static android.graphics.Color.rgb;

class SongsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView titre, groupe;
    private ImageView imageSong, playSongs,stopSongs,recordSongs;
    private Button bsBtn, liveBtn, tuttiBtn, bassBtn, tenorBtn, altoBtn, sopranoBtn, mainBtn, secondBtn, thirdBtn, fourthBtn, fifthBtn;
    private SeekBar seekBar;
    private Pupitre pupitre=Pupitre.NA;
    private RecordSource source=RecordSource.NA;
    private final static String TAG = "coucou";
    private SourceSong sourceSong;
    private List<Song> choeurSongs;
    private boolean isFirstTime=true;
    private boolean mUserIsSeeking = false;

    private PlayerAdapter mPlayerAdapter;


    private SongsAdapter.ListemClickedListener mlistItemClickedListener;

    public SongsViewHolder(@NonNull View itemView, SongsAdapter.ListemClickedListener listener, SourceSong sourceSong) {
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

        mlistItemClickedListener=listener;
        this.sourceSong=sourceSong;

        Log.d(TAG, "SongsViewHolder: " + sourceSong.getTitre());

        verifyExistingSongs();


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

        int clickedPosition = getAdapterPosition();
        mlistItemClickedListener.OnClickedItem(clickedPosition);

        switch (view.getId()){

            case R.id.btn_bs :
                setColorButton(true,bsBtn);
                setColorButton(false,liveBtn);
                source=RecordSource.BANDE_SON;
                setActivableBtn(source);
                isFirstTime=true;
                if(mPlayerAdapter!=null) {
                    mPlayerAdapter.reset();
                }

                break;

            case R.id.btn_live:
                setColorButton(true,liveBtn);
                setColorButton(false,bsBtn);
                source=RecordSource.LIVE;
                setActivableBtn(source);
                isFirstTime=true;

                if(mPlayerAdapter!=null) {
                    mPlayerAdapter.reset();
                }

                break;

            case R.id.btn_tutti:
                setColorButton(true,tuttiBtn);
                setColorButton(false,bassBtn,tenorBtn,altoBtn,sopranoBtn);
                pupitre=Pupitre.TUTTI;
                isFirstTime=true;
                if(mPlayerAdapter!=null) {
                    mPlayerAdapter.reset();
                }

                break;

            case R.id.btn_bass:
                setColorButton(true,bassBtn);
                setColorButton(false,tuttiBtn,tenorBtn,altoBtn,sopranoBtn);
                //setActivableBtn(source);
                pupitre=Pupitre.BASS;
                isFirstTime=true;
                if(mPlayerAdapter!=null) {
                    mPlayerAdapter.reset();
                }
                break;

            case R.id.btn_tenor:
                setColorButton(true,tenorBtn);
                setColorButton(false,tuttiBtn,bassBtn,altoBtn,sopranoBtn);
                pupitre=Pupitre.TENOR;
                isFirstTime=true;
                if(mPlayerAdapter!=null) {
                    mPlayerAdapter.reset();
                }
                break;

            case R.id.btn_alto:
                setColorButton(true,altoBtn);
                setColorButton(false,tuttiBtn,bassBtn,tenorBtn,sopranoBtn);
                pupitre=Pupitre.ALTO;
                isFirstTime=true;
                if(mPlayerAdapter!=null) {
                    mPlayerAdapter.reset();
                }
                break;

            case R.id.btn_soprano:
                setColorButton(true,sopranoBtn);
                setColorButton(false,tuttiBtn,bassBtn,tenorBtn,altoBtn);
                pupitre=Pupitre.SOPRANO;
                isFirstTime=true;
                if(mPlayerAdapter!=null) {
                    mPlayerAdapter.reset();
                }
                break;

            case R.id.play_image:
                if(mPlayerAdapter==null ||!mPlayerAdapter.isPlaying()) {
                    setPlayListener();
                }
                break;

            case R.id.recordSongs:
                setRecordListener();

                break;

            case R.id.stopSongs:
                setStopListener();
                break;
        }


    }

    public void setActivableBtn(RecordSource mainSource){

        Log.d(TAG, "setActivableBtn: A "+mainSource);

        String titre = sourceSong.getTitre();
        RecordSource secondSource = RecordSource.NA;
        Button mainBtn=null,secondBtn=null;

        resetButton();

        if(mainSource==RecordSource.BANDE_SON){
            secondSource=RecordSource.LIVE;
            mainBtn = bsBtn;
            secondBtn = liveBtn;
            Log.d(TAG, "setActivableBtn: B1 "+secondSource);
        }else if(mainSource==RecordSource.LIVE){
            secondSource=RecordSource.BANDE_SON;
            mainBtn = liveBtn;
            secondBtn = bsBtn;

            Log.d(TAG, "setActivableBtn: B2 "+secondSource);
        }

        List<Song> songsMainSource = MainActivity.choeurDataBase.songsDao().getSongOrderedByPupitre(titre,mainSource);
        List<Song> songsSecondSource = MainActivity.choeurDataBase.songsDao().getSongOrderedByPupitre(titre,secondSource);

        if(songsMainSource.size()==0){
            setButtonActivable(false,mainBtn);
        }


        if(songsMainSource.size()!=0){
            Log.d(TAG, "setActivableBtn: C "+ songsMainSource.size());
            setButtonActivable(true,mainBtn);
            setColorButton(true,mainBtn);
            source=mainSource;

            Log.d(TAG, "setActivableBtn: D" + MainActivity.choeurDataBase.songsDao().getSongsByTitreSource(titre,mainSource));

            if(MainActivity.choeurDataBase.songsDao().getSongsByTitreSource(titre,secondSource).size()==0){
                setButtonActivable(false,secondBtn);

            }else{
                Log.d(TAG, "setActivableBtn: E");
                setColorButton(false,secondBtn);
            }

            for (Song mainSourceSong: songsMainSource) {
                Log.d(TAG, "setActivableBtn: F "+songsMainSource.size());

                Pupitre tempPupitre = mainSourceSong.getPupitre();

                Log.d(TAG, "setActivableBtn: F"+ pupitre.toString());

                if(tempPupitre==Pupitre.TUTTI){
                    setButtonActivable(true,tuttiBtn);
                    setColorButton(true,tuttiBtn);
                    pupitre=Pupitre.TUTTI;
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
                    setColorButton(true,bassBtn);
                    pupitre=Pupitre.BASS;
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
                    setColorButton(true,tenorBtn);
                    pupitre=Pupitre.TENOR;
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
                    setColorButton(true,altoBtn);
                    pupitre=Pupitre.ALTO;
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,mainSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(tempPupitre==Pupitre.SOPRANO){
                    Log.d(TAG, "setActivableBtn: G");
                    setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn);
                    setButtonActivable(true,sopranoBtn);
                    setColorButton(true,sopranoBtn);
                    pupitre=Pupitre.SOPRANO;
                    return;
                }

            }
        }else if(songsSecondSource.size()!=0){
            setButtonActivable(true,secondBtn);
            setColorButton(true,secondBtn);
            source=secondSource;

            for (Song secondSourceSong: songsSecondSource) {

                Pupitre pupitre = secondSourceSong.getPupitre();

                if(pupitre==Pupitre.TUTTI){
                    setButtonActivable(true,tuttiBtn);
                    setColorButton(true,tuttiBtn);
                    pupitre=Pupitre.TUTTI;
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
                }else if(pupitre==Pupitre.BASS){
                    setButtonActivable(false,tuttiBtn);
                    setButtonActivable(true,bassBtn);
                    setColorButton(true,bassBtn);
                    pupitre=Pupitre.BASS;
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
                }else if(pupitre==Pupitre.TENOR){
                    setButtonActivable(false,tuttiBtn,bassBtn);
                    setButtonActivable(true,tenorBtn);
                    setColorButton(true,tenorBtn);
                    pupitre=Pupitre.TENOR;
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.ALTO,secondSource)==null){
                        setButtonActivable(false,altoBtn);
                    }else{setColorButton(false,altoBtn);}
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,secondSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(pupitre==Pupitre.ALTO){
                    setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn);
                    setButtonActivable(true,altoBtn);
                    setColorButton(true,altoBtn);
                    pupitre=Pupitre.ALTO;
                    if(MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(titre,Pupitre.SOPRANO,secondSource)==null){
                        setButtonActivable(false,sopranoBtn);
                    }else{setColorButton(false,sopranoBtn);}
                    return;
                }else if(pupitre==Pupitre.SOPRANO){
                    setButtonActivable(false,tuttiBtn,bassBtn,tenorBtn,altoBtn);
                    setButtonActivable(true,sopranoBtn);
                    setColorButton(true,sopranoBtn);
                    pupitre=Pupitre.SOPRANO;
                    return;
                }
            }

        }

    }

    private void resetButton() {
        setButtonActivable(true,bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);
        setGreyButton(true, bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);
    }

    private void verifyExistingSongs() {

        choeurSongs = MainActivity.choeurDataBase.songsDao().getSongsBySourceSong(sourceSong.getTitre());

        if(choeurSongs.size()==0){
            Log.d(TAG, "verifyExistingSongs: A");
            setButtonActivable(false,bsBtn,liveBtn,tuttiBtn,bassBtn,tenorBtn,altoBtn,sopranoBtn);

        }else{

            Log.d(TAG, "verifyExistingSongs: B");
            setActivableBtn(RecordSource.BANDE_SON); }

    }


    //Méthodes pour les boutons de controle du mediaplayer et mediarecorder
    private void setPlayListener() {

                if(pupitre==Pupitre.NA||source==RecordSource.NA){

                    Log.d(TAG, "setPlayListener: Veuillez renseigner le pupitre et/ou la source (Live ou Bande Son) !");

                }else{

                    if(isFirstTime) {

                        //Gestion de la seekBar
                        initializeSeekbar();

                        //PlayBackController
                        initializePlaybackController();

                        //Fournit et parépare le Mediaplayer

                        Song songToPlay = MainActivity.choeurDataBase.songsDao().getSongsByTitrePupitreSource(sourceSong.getTitre(),pupitre,source);
                        int resIdToPlay = songToPlay.getSongResId();

                        mPlayerAdapter.prepareMediaPlayer((Context) mlistItemClickedListener, resIdToPlay);

                        isFirstTime=false;
                    }

                    if(!mPlayerAdapter.isPlaying()) {

                        mPlayerAdapter.play();

                    }else {
                        mPlayerAdapter.pause();
                    }
                }

    }

    private void setRecordListener() {

    }


    private void setStopListener() {

        mPlayerAdapter.reset();

    }


    private void initializeSeekbar() {
        seekBar.setOnSeekBarChangeListener(
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

    private void initializePlaybackController() {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder((Context) mlistItemClickedListener);

        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;

    }


    public class PlaybackListener extends PlaybackInfoListener {



        public PlaybackListener(){

        }

        @Override
        public void onDurationChanged(int duration) {
            seekBar.setMax(duration);
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

    }



}
