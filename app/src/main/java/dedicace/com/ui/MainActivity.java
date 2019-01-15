package dedicace.com.ui;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dedicace.com.AppExecutors;
import dedicace.com.R;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;
import dedicace.com.utilities.InjectorUtils;

public class MainActivity extends AppCompatActivity implements SongsAdapter.ListemClickedListener,DialogRecordFragment.DialogRecordFragmentListener {

    //UI
    private  RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private ProgressBar mLoadingIndicator;

    //Adapter
    private SongsAdapter songsAdapter;
    private RecyclerView.LayoutManager layoutManager;

    //Songs
    private List<Song> songs = new ArrayList<>();
    private List<Song> recordedSongs = new ArrayList<>();
    private Toast mToast;
    private static final String TAG = "coucou";
    private final int REQUEST_PERMISSION_CODE = 1000;
    private int position;

    //todo à retirer seuelement pour les tests
    LiveData<List<SourceSong>> sourceSongs;
    List<SourceSong> sourceSongList = new ArrayList<>();
    Song song3, song4, song6, song5,song2, firstSongPlayed;
    SourceSong sourceSong1, sourceSong2,sourceSong7;
    List<List<RecordSource>> recordSources= new ArrayList<>();
    List<Song> songToPlays= new ArrayList<>();
    private List<List<Song>> songOnPhones= new ArrayList<>();
    private List<List<Song>> songOnClouds= new ArrayList<>();

    //ViewModel
    private MainActivityViewModel mViewModel;
    private  MainActivityViewModelFactory mfactory;

    //Firebase
    private FirebaseAuth mAuth;
    public static String current_user_id;

    //Utils
    private OnPositiveClickListener mPositiveClickListener;
    public static AppDataBase choeurDataBase;
    private AppExecutors mExecutors;

    //todo vérifier si extras dans des intents avec HasExtras
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("coucou", "MA onCreate: ");


        //Firebase
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {
            //UI
            mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
            recyclerView = findViewById(R.id.recyclerview_media_item);
            songsAdapter = new SongsAdapter(this, this);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(songsAdapter);

            //Songs
            initData();

            mExecutors = AppExecutors.getInstance();

            mfactory = InjectorUtils.provideViewModelFactory(this.getApplicationContext());
            Log.d("coucou", "onCreate: fin de la factoy");
            mViewModel = ViewModelProviders.of(this, mfactory).get(MainActivityViewModel.class);
            Log.d("coucou", "onCreate: fin du viewModel");
            mViewModel.getChoeurSourceSongs().observe(this, new Observer<List<SourceSong>>() {
                @Override
                public void onChanged(@Nullable List<SourceSong> sourceSongs) {
                    Log.d("coucou", "MainActivity: observers");

                    //getElementsToplaysSongs();

                    //todo gérer le cas où l'on a que des chansons live sur Phone mais tout de même des chansons onCloud. pour l'instant btn disabled. voir télécharger via le menu
                    Log.d(TAG, "onChanged: RecordSources " + recordSources.size());
                    Log.d(TAG, "onChanged: songToplays " + songToPlays.size());
                    Log.d(TAG, "onChanged: songOnPhones " + songOnPhones.size());
                    Log.d(TAG, "onChanged: songOnClouds " + songOnClouds.size());

                    for (List<RecordSource> sources : recordSources) {
                        for (RecordSource source : sources) {
                            Log.d(TAG, "onChanged: A " + source + " " + sources.size());
                        }
                    }

                    for (Song song : songToPlays) {
                        if (song != null) {
                            Log.d(TAG, "onChanged: B " + song + "  " + song.getSourceSongTitre() + " " + song.getRecordSource() + " " + song.getPupitre());
                        } else {
                            Log.d(TAG, "onChanged: B pas de chanson " + song);
                        }
                    }

                    for (List<Song> songs : songOnPhones) {

                        for (Song song : songs) {
                            if (song != null) {
                                Log.d(TAG, "onChanged: C " + song.getSourceSongTitre() + " " + song.getRecordSource() + " " + song.getPupitre());
                            } else {
                                Log.d(TAG, "onChanged: C pas de chanson sur le Phone");
                            }
                        }
                    }

                    for (List<Song> songs : songOnClouds) {
                        for (Song song : songs) {
                            if (song != null) {
                                Log.d(TAG, "onChanged: D " + song.getSourceSongTitre() + " " + song.getRecordSource() + " " + song.getPupitre());
                            } else {
                                Log.d(TAG, "onChanged: D pas de chanson non enregistrée");
                            }
                        }
                    }

                    songsAdapter.swapSongs(sourceSongs, recordSources, songToPlays, songOnPhones, songOnClouds);

                    if (sourceSongs != null) {
                        Log.d("coucou", "onCreate: observers " + sourceSongs.size());
                    }
                    if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                    recyclerView.smoothScrollToPosition(mPosition);

                    Log.d("coucou", "onCreate: observers - mposition " + mPosition);
                    // Show the weather list or the loading screen based on whether the forecast data exists
                    // and is loaded
                    if (songs != null && songs.size() != 0) {
                        showSongsDataView();
                        Log.d("coucou", "onCreate: showDataView");
                    } else {
                        showLoading();
                        Log.d("coucou", "onCreate: showLoading");
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("coucou", "onCreate: A Start "+ current_user_id);


        if(currentUser == null){
            sendToLogin();

            Log.d("coucou", "onCreate:B Start "+ current_user_id);
        } else {
            //todo à compléter

            Log.d(TAG, "onStart: currentusernonnull");

            current_user_id = mAuth.getCurrentUser().getUid();
            Log.d("coucou", "onStart C: "+ current_user_id);

        }
    }

    //todo mettre des conditions pour rester logger entre 2 utilisations (à conserver ?)
    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth!=null) {
            mAuth.signOut();
            mAuth = null;
        }
    }

    private void sendToLogin() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void getElementsToplaysSongs() {

        List<Object> listElements = new ArrayList();
        listElements=mViewModel.getListElements();

        recordSources= (List<List<RecordSource>>) listElements.get(0);
        songOnPhones= (List<List<Song>>) listElements.get(1);
        songToPlays = (List<Song>) listElements.get(2);
        songOnClouds= (List<List<Song>>) listElements.get(3);
        /*recordSources =mViewModel.getRecordSources();
        songOnPhones=mViewModel.getSongOnPhones();
        songToPlays =mViewModel.getSongToPlays();
        songOnClouds=mViewModel.getSongOnClouds();*/

    }

    private void initData() {
        Log.d(TAG, "initData: MA songs");
        String titreSourceSong1 = "Des hommes pareils";
        String titreSourceSong2 = "L'un pour l'autre";
        String titreSourceSong7 = "North Star";

        Date date = new Date(System.currentTimeMillis());

        song2 = new  Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.BASS,"des_hommes_pareils_basse",null);
        song3 = new Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.TENOR,"des_hommes_pareils_tenor",date);
        song4 = new Song(titreSourceSong1,RecordSource.LIVE,Pupitre.ALTO,"des_hommes_pareils_alto",date);
        song5 = new  Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.SOPRANO,"des_hommes_pareils_soprano",date);
        song6 = new Song(titreSourceSong2,RecordSource.LIVE,Pupitre.BASS,"l_un_pour_l_autre_basse",null);

        songs.add(song3);
        songs.add(song4);
        songs.add(song2);
        songs.add(song5);
        songs.add(song6);

        List<RecordSource>  recordSources1 = new ArrayList<>();
        List<RecordSource>  recordSources2 = new ArrayList<>();
        List<RecordSource>  recordSources3 = new ArrayList<>();
        recordSources1.add(RecordSource.BANDE_SON);
        recordSources1.add(RecordSource.LIVE);
        recordSources2.add(RecordSource.LIVE);
        recordSources3.add(RecordSource.NA);
        recordSources.add(recordSources1);
        recordSources.add(recordSources2);
        recordSources.add(recordSources3);

        List<Song> songs1 = new ArrayList<>();
        List<Song> songs2 = new ArrayList<>();
        List<Song> songs3 = new ArrayList<>();
        songs1.add(song3);
        songs1.add(song5);
        songs2.add(null);
        songs3.add(null);
        songOnPhones.add(songs1);
        songOnPhones.add(songs2);
        songOnPhones.add(songs3);

        List<Song> songs4 = new ArrayList<>();
        List<Song> songs5 = new ArrayList<>();
        List<Song> songs6 = new ArrayList<>();
        songs4.add(song2);
        songs5.add(song6);
        songs6.add(null);
        songOnClouds.add(songs4);
        songOnClouds.add(songs5);
        songOnClouds.add(songs6);

        songToPlays.add(song3);
        songToPlays.add(null);
        songToPlays.add(null);


    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showSongsDataView() {
        // First, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // Finally, make sure the weather data is visible
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the loading indicator visible and hide the weather View and error
     * message.
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        // Then, hide the weather data
        recyclerView.setVisibility(View.INVISIBLE);
        // Finally, show the loading indicator
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Gestion du menu avec Options
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.reset_db:

                deleteAllRecordedSongs();
                break;

            case R.id.reset_song:

                deleteLastRecordedSong();
                break;

            case R.id.log_out:
                logOut();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteLastRecordedSong() {


        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Song song = choeurDataBase.songsDao().getLastSong();

                if(song.getRecordSource()==RecordSource.LIVE){

                    //todo supprimer le fichier physique aussi

                    choeurDataBase.songsDao().deleteSong(song);

                    mPositiveClickListener.OndeleteSong();

                    String path = song.getSongPath();

                    deleteMusicFiles(path);
                }
            }
        });

    }

    private void deleteAllRecordedSongs(){

        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {

                recordedSongs =MainActivity.choeurDataBase.songsDao().getSongsBySource(RecordSource.LIVE);

                if(recordedSongs.size()>0){

                    for (Song recordedSong:recordedSongs) {

                        //todo supprimer le fichier physique aussi
                        choeurDataBase.songsDao().deleteSong(recordedSong);

                        //todo voir pour que les couleurs fonctionnenent

                        songsAdapter.notifyDataSetChanged();

                        //mPositiveClickListener.OndeleteSong();
                    }
                }
            }
        });
    }

    public void deleteMusicFiles(final String path){
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {

                File file = new File(path);
                boolean deletefile = file.delete();
                Log.d(TAG, "deleteMusicFiles: "+deletefile);
            }
        });
    }


    /**
     * Méthode passée dans le listener dans l'adapter
     *
     */


    @Override
    public void OnClickedItem(String titre, String message) {

        if(mToast!=null){
            mToast.cancel();
        }
        mToast=Toast.makeText(this, message +"-"+titre, Toast.LENGTH_SHORT);
        mToast.show();
    }


    @Override
    public Song OnPlaySong(SourceSong sourceSong, Pupitre pupitre, RecordSource source) {
        return song3;
    }

    @Override
    public Song OnPlayFirstSong(final String titre, final RecordSource recordSource) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
               firstSongPlayed= mViewModel.getFirstSong(titre,recordSource);
            }
        });

        return firstSongPlayed;
    }

    @Override
    public List<Song> OnListRecordedSongsOnPhone() {

        List<Song> phoneSongs = new ArrayList<>();
        phoneSongs.add(song2);


        return phoneSongs;
    }

    @Override
    public List<Song> OnListRecordedSongsOnCloud() {

        List<Song> cloudSongs = new ArrayList<>();
        cloudSongs.add(song4);

        return cloudSongs;
    }



    @Override
    public void OnDialogRecord(int position, SongsViewHolder songsViewHolder) {
        this.position=position;
        mPositiveClickListener=songsViewHolder;
        DialogFragment dialog = new DialogRecordFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
        Log.d(TAG, "MA OnDialogRecord: ");
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, final Pupitre pupitre) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "MA onDialogPositiveClick: enregistrer " + pupitre+ " "+ position);
               // Song recordSong = new Song(songs.get(position),RecordSource.LIVE,pupitre,"NA");
              //  choeurDataBase.songsDao().insertSong(recordSong);
                // songsAdapter.notifyItemChanged(position);
                mPositiveClickListener.OnRecord(pupitre);
            }
        });
    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        Log.d(TAG, "MA onDialogPositiveClick: annuler");
    }


    /** Permission pour le stockage externe
     * Permission utilisée suivant les versions
     */
    @Override
    public void OnRequestPermission() {
        if(!checkPermissionFromDevice()){
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);

        return write_external_storage_result==PackageManager.PERMISSION_GRANTED &&record_audio_result==PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }


    //todo à renommer
    public interface OnPositiveClickListener {
        void OnRecord(Pupitre pupitre);
        void OndeleteSong();
    }



}
