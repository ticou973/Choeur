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
import java.util.List;

import dedicace.com.AppExecutors;
import dedicace.com.R;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.ListSongs;
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
    private ListSongs listSongs;


    private Thread currentThread;


    //todo à retirer seuelement pour les tests
    private LiveData<List<SourceSong>> sourceSongs;
    private List<SourceSong> sourceSongList = new ArrayList<>();
    Song song3, song4, song6, song5,song2, firstSongPlayed;
    SourceSong sourceSong1, sourceSong2,sourceSong7;
    private List<List<RecordSource>> recordSources;
    private List<Song> songToPlays;
    private List<List<Song>> songOnPhones;
    private List<List<Song>> songOnClouds;
    private List<List<Song>> SongOnPhonesLive= new ArrayList<>();
    private List<List<Song>> SongOnPhonesBS= new ArrayList<>();
    private Pupitre currentPupitre;
    private String currentPupitreStr;

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
        Log.d("coucou", "MA onCreate: "+Thread.currentThread().getName());

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

            //initData();

            mExecutors = AppExecutors.getInstance();

            mfactory = InjectorUtils.provideViewModelFactory(this.getApplicationContext());
            Log.d("coucou", "onCreate: fin de la factory");
            mViewModel = ViewModelProviders.of(this, mfactory).get(MainActivityViewModel.class);
            Log.d("coucou", "onCreate: fin du viewModel");

            //todo faire une condition au niveau du repository suivant que l'on connait ou non le currentpupitre (1ère fois ou non)
            //currentPupitre = SongsUtilities.converttoPupitre(getCurrentPupitreStr());
            Log.d(TAG, "onCreate: "+ currentPupitre);

            sourceSongs =mViewModel.getChoeurSourceSongs();

            Log.d(TAG, "MA onCreate: getChoeurSourcesongs " + sourceSongs);

            sourceSongs.observe(this, new Observer<List<SourceSong>>() {
                @Override
                public void onChanged(@Nullable List<SourceSong> sourceSongs) {

                    sourceSongList=sourceSongs;

                    Log.d(TAG, "MA onChanged: Alerte, ça bouge dans le coin !" + sourceSongs + " " + mViewModel.getChoeurSourceSongs() + " " + Thread.currentThread().getName());

                    currentThread = mViewModel.getCurrentThread();

                    if (sourceSongs != null && sourceSongs.size() != 0) {

                        Log.d(TAG, "MA onChanged: currentThread "+currentThread);
                        try {
                            currentThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.d(TAG, "MA onChanged: interrupted exception");
                        }

                        Log.d("coucou", "MA onCreate: Thread fini");
                    }

                    showLoading();
                    getListSongs();

                    // partieAux(sourceSongs);


                    if (listSongs != null) {
                        if (recordSources.size() == sourceSongs.size() && songToPlays.size() == sourceSongs.size() && songOnPhones.size() == sourceSongs.size() && songOnClouds.size() == sourceSongs.size()) {
                            Log.d(TAG, "MA onChanged: conditions toutes réunies");

                            affichageRecyclerView(sourceSongs);
                            songsAdapter.swapSongs(sourceSongs, recordSources, songToPlays, songOnPhones, songOnClouds);

                        } else {
                            Log.d(TAG, "MA onChanged: conditions pas réunies");
                        }
                    }else{
                        Log.d(TAG, "MA onChanged: listsongs null ");
                    }
                }
            });
        }
    }

    private void getListSongs() {
        Log.d(TAG, "MA getListSongs: début");

        listSongs=mViewModel.getListSongs();

        Log.d(TAG, "MA getListSongs: début après aller chercher listSongs "+listSongs);

        if(listSongs!=null) {
            Log.d(TAG, "MA getListSongs: début liste ");
            songOnPhones = listSongs.getSongsOnPhonesA(currentThread);
            Log.d(TAG, "MA getSongElements songOnphones: " + songOnPhones);
            Log.d(TAG, "MA getListSongs: avant recordResources");
            recordSources = listSongs.getRecordSourcesA();
            Log.d(TAG, "MA getSongElements recordsources: " + recordSources);

            songToPlays = listSongs.getSongToPlaysA();
            Log.d(TAG, "MA getListSongs: juste après"+songToPlays);
            if (songToPlays.size() > 1) {
      //          Log.d(TAG, "MA getSongElements songToplays: " + songToPlays + " " + songToPlays.get(0).getSourceSongTitre() + " " + songToPlays.get(0).getPupitre() + " " + songToPlays.get(1).getSourceSongTitre() + " " + songToPlays.get(1).getPupitre());
            }
            songOnClouds = listSongs.getSongsOnCloudsA();
            Log.d(TAG, "MA getSongElements songOnclouds: " + songOnClouds);


            SongOnPhonesBS=listSongs.getSongOnPhoneBSA();
            Log.d(TAG, "MA getSongElements getSongOnPhoneBSA: " + SongOnPhonesBS);

            SongOnPhonesLive = listSongs.getSongOnPhoneLiveA();
            Log.d(TAG, "MA getSongElements getSongOnPhoneLiveA: " + SongOnPhonesLive);

        }else{
            Log.d(TAG, "getListSongs: est null "+listSongs);
        }
    }


    private void affichageRecyclerView(List<SourceSong> sourceSongs) {

        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        recyclerView.smoothScrollToPosition(mPosition);

        Log.d("coucou", "MA onCreate: observers - mposition " + mPosition);
        // Show the weather list or the loading screen based on whether the forecast data exists
        // and is loaded
        if (sourceSongs != null && sourceSongs.size() != 0&&recordSources!=null&&recordSources.size()!=0) {
            Log.d(TAG, "MA onChanged: Ready to affiche  !");

            showSongsDataView();
            Log.d("coucou", "MA onCreate: showDataView");
        } else {
            showLoading();
            Log.d("coucou", "MA onCreate: showLoading");
        }
    }

    private void partieAux(List<SourceSong> sourceSongs) {

        Log.d("coucou", "MainActivity: observers");

        if (sourceSongs != null) {
            Log.d("coucou", "MA onCreate: observers A " + sourceSongs.size()+" ");
        }


        //todo gérer le cas où l'on a que des chansons live sur Phone mais tout de même des chansons onCloud. pour l'instant btn disabled. voir télécharger via le menu
        Log.d(TAG, "MA onChanged: RecordSources " + recordSources.size());
        Log.d(TAG, "MA onChanged: songToplays " + songToPlays.size());
        Log.d(TAG, " MA onChanged: songOnPhones " + songOnPhones.size());
        Log.d(TAG, "MA onChanged: songOnClouds " + songOnClouds.size());

        for (List<RecordSource> sources : recordSources) {
            for (RecordSource source : sources) {
                Log.d(TAG, "MA onChanged: A " + source + " " + sources.size());
            }
        }

        for (Song song : songToPlays) {
            if (song != null) {
                Log.d(TAG, "MA onChanged: B " + song + "  " + song.getSourceSongTitre() + " " + song.getRecordSource() + " " + song.getPupitre());
            } else {
                Log.d(TAG, "MA onChanged: B pas de chanson " + song);
            }
        }

        for (List<Song> songs : songOnPhones) {

            for (Song song : songs) {
                if (song != null) {
                    Log.d(TAG, "MA onChanged: C " + song.getSourceSongTitre() + " " + song.getRecordSource() + " " + song.getPupitre());
                } else {
                    Log.d(TAG, "MA onChanged: C pas de chanson sur le Phone");
                }
            }
        }

        for (List<Song> songs : songOnClouds) {
            for (Song song : songs) {
                if (song != null) {
                    Log.d(TAG, "MA onChanged: D " + song.getSourceSongTitre() + " " + song.getRecordSource() + " " + song.getPupitre());
                } else {
                    Log.d(TAG, "MA onChanged: D pas de chanson non enregistrée");
                }
            }
        }

        if (sourceSongs != null) {
            Log.d("coucou", "MA onCreate: observers B" + sourceSongs.size()+" ");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("coucou", "MA onStart: A  "+ current_user_id);


        if(currentUser == null){
            sendToLogin();

            Log.d("coucou", "MA onCreate:B Start "+ current_user_id);
        } else {
            //todo à compléter

            Log.d(TAG, "MA onStart: currentuser non null");

            current_user_id = mAuth.getCurrentUser().getUid();
            Log.d("coucou", "MA onStart C: "+ current_user_id);

        }
    }

    private String getCurrentPupitreStr() {

        return mViewModel.getCurrentPupitreStr();
    };

    //todo mettre des conditions pour rester logger entre 2 utilisations (à conserver ?) dans OnDestroy ?
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAuth!=null) {
            Log.d(TAG, "onStop: MA logout");
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
        Log.d(TAG, "MA OnPlaySong: au secours");

        Song songToPlay = null;

        int indexSourceSong = sourceSongList.indexOf(sourceSong);

        if(source ==RecordSource.BANDE_SON) {
            for (Song song : SongOnPhonesBS.get(indexSourceSong)) {
                if (song.getPupitre() == pupitre && song.getRecordSource() == source) {
                    songToPlay = song;
                }
            }
        }else if(source ==RecordSource.LIVE) {
            for (Song song : SongOnPhonesLive.get(indexSourceSong)) {
                if (song.getPupitre() == pupitre && song.getRecordSource() == source) {
                    songToPlay = song;
                }
            }
        }

        Log.d(TAG, "MA OnPlaySong: "+ songToPlay.getRecordSource()+" "+songToPlay.getPupitre()+" "+songToPlay.getSourceSongTitre());

        return songToPlay;
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
