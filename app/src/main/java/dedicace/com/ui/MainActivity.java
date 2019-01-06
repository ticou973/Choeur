package dedicace.com.ui;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.util.ArrayList;
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

    private OnPositiveClickListener mPositiveClickListener;
    private SongsViewHolder songsViewHolder;

    public static AppDataBase choeurDataBase;
    private AppExecutors mExecutors;

    private MainActivityViewModel mViewModel;
    private  MainActivityViewModelFactory mfactory;


    //todo à retirer seuelement pour les tests
    LiveData<List<SourceSong>> sourceSongs;
    List<SourceSong> sourceSongList = new ArrayList<>();
    List<Song> songsEssai = new ArrayList<>();
    Song song3, song4, song6, song5,song2;
    SourceSong sourceSong1, sourceSong2,sourceSong7;


    //todo vérifier si extras dans des intents avec HasExtras
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("coucou", "MA onCreate: ");

        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        recyclerView = findViewById(R.id.recyclerview_media_item);
        songsAdapter =new SongsAdapter(this, this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(songsAdapter);

        initData();

        mExecutors =AppExecutors.getInstance();

        mfactory = InjectorUtils.provideViewModelFactory(this.getApplicationContext());
        Log.d("coucou", "onCreate: fin de la factoy");
        mViewModel = ViewModelProviders.of(this,mfactory).get(MainActivityViewModel.class);
        Log.d("coucou", "onCreate: fin du viewModel");
        mViewModel.getChoeurSourceSongs().observe(this, new Observer<List<SourceSong>>() {
            @Override
            public void onChanged(@Nullable List<SourceSong> sourceSongs) {
                Log.d("coucou", "MainActivity: observers");
                songsAdapter.swapSongs(sourceSongs);

                if(sourceSongs!=null){
                    Log.d("coucou", "onCreate: observers " + sourceSongs.size());
                }
                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);

                Log.d("coucou", "onCreate: observers - mposition "+ mPosition);
                // Show the weather list or the loading screen based on whether the forecast data exists
                // and is loaded
                if (songs != null && songs.size() != 0) {
                    showSongsDataView();
                    Log.d("coucou", "onCreate: showDataView");
                }
                else {
                    showLoading();
                    Log.d("coucou", "onCreate: showLoading");
                }

            }
        });
        /*mViewModel.getChoeurSongs().observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(@Nullable final List<Song> songs) {

                songsAdapter.swapSongs(songs, sourceSongList);

                //sourceSongs= mViewModel.getChoeurSourceSongs();

                mExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(sourceSongs.getValue()!=null) {
                            songsAdapter.swapSongs(songs, sourceSongs.getValue());
                            Log.d(TAG, "onChanged:B  "+sourceSongs.getValue().get(0).getTitre());
                        }
                    }
                });

                if(sourceSongs!=null) {
                    songsAdapter.swapSongs(songs, sourceSongs.getValue());
                    Log.d(TAG, "onChanged: B"+sourceSongs.toString());
                }

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);

                // Show the weather list or the loading screen based on whether the forecast data exists
                // and is loaded
                if (songs != null && songs.size() != 0) showSongsDataView();
                else showLoading();
            }
        });*/
    }

    private void initData() {
        Log.d(TAG, "initData: MA songs");
        String titreSourceSong1 = "Des hommes pareils";
        String titreSourceSong2 = "L'un pour l'autre";
        String titreSourceSong7 = "North Star";

        song2 = new  Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.BASS,"des_hommes_pareils_basse");
        song3 = new Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.TENOR,"des_hommes_pareils_tenor");
        song4 = new Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.ALTO,"des_hommes_pareils_alto");
        song5 = new  Song(titreSourceSong1,RecordSource.BANDE_SON,Pupitre.SOPRANO,"des_hommes_pareils_soprano");
        song6 = new Song(titreSourceSong2,RecordSource.BANDE_SON,Pupitre.BASS,"l_un_pour_l_autre_basse");

        songs.add(song3);
        songs.add(song4);
        songs.add(song2);
        songs.add(song5);
        songs.add(song6);
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
    public Song OnPlaySong() {
       // todo à faire avec le viewModel
        Song songToPlay = song3;

        return songToPlay;
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
    public List<Song> OnListNotRecordedSong() {

        List<Song> notRecordedSongs = new ArrayList<>();
        notRecordedSongs.add(song5);

        return notRecordedSongs;
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

    public File getPublicMusicStorageDir(String titre) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), titre);
        if (!file.mkdirs()) {
            Log.e(TAG, "Répertoire non créé");
        }
        return file;
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





}
