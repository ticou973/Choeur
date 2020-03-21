package dedicace.com.ui.PlaySong;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dedicace.com.R;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.ListSongs;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;
import dedicace.com.data.database.Spectacle;
import dedicace.com.ui.Admin.AdminHome;
import dedicace.com.ui.Trombinoscope.TrombiActivity;
import dedicace.com.utilities.AppExecutors;
import dedicace.com.utilities.InjectorUtils;
import dedicace.com.utilities.SongsUtilities;

//todo revoir dans tout le logiciel les new Object lorsqu'ils sont déjà définis
public class MainActivity extends AppCompatActivity implements SongsAdapter.ListemClickedListener, DialogRecordFragment.DialogRecordFragmentListener, SharedPreferences.OnSharedPreferenceChangeListener, DialogMajSS.DialogMajSSListener, DialogMA.DialogMAListener, ChoraleNetWorkDataSource.OnNDSListener, DialogSpectacleFragment.DialogSpectacleFragmentListener {

    //UI
    private RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private ProgressBar mLoadingIndicator;

    //Adapter
    private SongsAdapter songsAdapter;

    //Songs
    private Toast mToast;
    private static final String TAG = "coucou";
    private final int REQUEST_PERMISSION_CODE = 1000;
    private int positionToRecord, positionToDownload, positionToDelete;
    private ListSongs listSongs;
    private Thread currentThread;

    private List<SourceSong> sourceSongList = new ArrayList<>();
    private Song firstSongPlayed;
    private List<List<RecordSource>> recordSources;
    private List<Song> songToPlays;
    private List<List<Song>> songOnPhones;
    private List<List<Song>> songOnClouds;
    private List<List<Song>> SongOnPhonesLive = new ArrayList<>();
    private List<List<Song>> SongOnPhonesBS = new ArrayList<>();
    private Song songToPlay;
    private ArrayList<String> namesSpectacles = new ArrayList<>();
    private String spectacle = "Tous";
    private boolean isMediaPlaying = false;
    private boolean isMediaPausing = true;

    //ViewModel
    private MainActivityViewModel mViewModel;

    //Firebase
    public static String current_user_id;
    private String mCurrentAuthRole;
    private String typeSS;


    //Utils
    private OnPositiveClickListener mPositiveClickListener;
    private SharedPreferences sharedPreferences;
    private List<Pupitre> pupitresToDownloadDelete;
    private DialogMA dialogWait;
    private Thread threadSpectacles;
    private AppDataBase dataBase;
    private SharedPreferences.Editor editor;
    boolean deletedSpectacles;




    //todo vérifier si extras dans des intents avec HasExtras
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("coucou", "MA onCreate: " + Thread.currentThread().getName());
        Log.d(TAG, "MA onCreate: connection internet "+haveInternetConnection());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mCurrentAuthRole = sharedPreferences.getString("role", "Choriste");
        current_user_id =sharedPreferences.getString("userId", "");
        boolean installationAuth = sharedPreferences.getBoolean("installationAuth", true);
        dataBase = AppDataBase.getInstance(getApplicationContext());

        if(sharedPreferences.getBoolean("spectacleDeleted",false)) {
            editor = sharedPreferences.edit();
            editor.putBoolean("spectacleDeleted",false);
            editor.apply();
        }

        Log.d(TAG, "MA onCreate:  deletedspectacle "+deletedSpectacles);

        if (!TextUtils.isEmpty(current_user_id)) {
            Log.d(TAG, "MA onCreate: current user Id " + current_user_id);
        }

        //UI
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        recyclerView = findViewById(R.id.recyclerview_media_item);
        songsAdapter = new SongsAdapter(this, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(songsAdapter);

        if(!installationAuth){
            if(haveInternetConnection()) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                if (mAuth.getCurrentUser() != null) {
                    Log.d("coucou", "MA onCreate: current user non null");
                    mAuth.signOut();
                    mAuth = FirebaseAuth.getInstance();
                }

                Log.d("coucou", "MA onCreate: installationAuth " + mAuth + " " + installationAuth);
                String loginEmail = sharedPreferences.getString("loginEmail", "");
                String loginPass = sharedPreferences.getString("loginMdp", "");

                Log.d("coucou", "MA onCreate: login et mdp connus " + loginEmail + " " + loginPass);

                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)) {
                    mAuth.signInWithEmailAndPassword(Objects.requireNonNull(loginEmail), Objects.requireNonNull(loginPass)).addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "MA onComplete: login automatique");
                            visualisation();

                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Pb de connexion", Toast.LENGTH_SHORT).show();
                    Log.d("coucou", "LA onCreate: pb de reconnexion");
                }
            }else{
                Log.d(TAG, "MA onCreate: pas de connexion internet mais on y va quand même avec sa connection");
                visualisation();
            }

        }else{
            Log.d(TAG, "MA onCreate: visualisation");
            editor = sharedPreferences.edit();
            editor.putBoolean("installationAuth", false);
            editor.apply();
            visualisation();
        }

    }

    private void visualisation() {
        if (!TextUtils.isEmpty(current_user_id)) {
            Log.d(TAG, "" + "MA onCreate: avant Onrequest permission " + current_user_id);
            OnRequestPermission();
            MainActivityViewModelFactory mfactory = InjectorUtils.provideViewModelFactory(this.getApplicationContext(), this);
            Log.d("coucou", "MA onCreate: fin de la factory");
            mViewModel = ViewModelProviders.of(this, mfactory).get(MainActivityViewModel.class);
            Log.d("coucou", "MA onCreate: fin du viewModel");
            setUpSharedPreferences();

            getCurrentSpectacles();

            LiveData<List<SourceSong>> sourceSongs1 = mViewModel.getChoeurSourceSongs();
            Log.d(TAG, "MA onCreate: getChoeurSourcesongs " + sourceSongs1);
            sourceSongs1.observe(this, sourceSongs -> {
                Log.d(TAG, "MA onChanged: Alerte, ça bouge dans le coin !" + sourceSongs + " " + mViewModel.getChoeurSourceSongs() + " " + Thread.currentThread().getName());

                currentThread = mViewModel.getCurrentThread();
                typeSS = mViewModel.getTypeSS();

                //permet que la listsongs de CR se calcule
                if (typeSS == null) {
                    Log.d(TAG, "MA onChanged: typeSS null et alertbox");
                    showLoading();
                    AlertBox();
                } else {
                    if (!typeSS.equals("modificationSS")) {
                        Log.d(TAG, "MA onChanged: typeSS non null et alertbox " + typeSS);
                        showLoading();
                    }
                }

                if (sourceSongs != null && sourceSongs.size() != 0 && currentThread != null) {
                    Log.d(TAG, "MA onChanged: currentThread " + currentThread);
                    try {
                        currentThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(TAG, "MA onChanged: interrupted exception");
                    }
                    Log.d("coucou", "MA onCreate: Thread fini " + typeSS);
                }

                getListSongs();

                if (listSongs != null) {
                    if (recordSources.size() == sourceSongList.size() && songToPlays.size() == sourceSongList.size() && songOnPhones.size() == sourceSongList.size() && songOnClouds.size() == sourceSongList.size()) {
                        Log.d(TAG, "MA onChanged: conditions toutes réunies" + typeSS);
                        switch (typeSS) {
                            case "oldSS":
                                Log.d(TAG, "MA onChanged: type OldSS");
                                if (dialogWait != null) {
                                    dialogWait.dismiss();
                                }
                                if(sourceSongList.size()!=0) {
                                    Log.d(TAG, "MA visualisation: if old");
                                    affichageRecyclerView(sourceSongList);
                                    songsAdapter.swapSongs(sourceSongList, recordSources, songToPlays, songOnClouds, SongOnPhonesBS, SongOnPhonesLive);
                                }else{
                                    Log.d(TAG, "MA oldSS:else ");
                                    editor = sharedPreferences.edit();
                                    editor.putString("currentSpectacle", "Tous");
                                    editor.apply();
                                    invalidateOptionsMenu();
                                }
                                break;
                            case "modificationSS":
                                boolean deleted = mViewModel.getDeleted();
                                if (deleted) {
                                    Log.d(TAG, "MA visualisation: modificationSS" );
                                    //todo voir pour différé la suppression
                                  //  Log.d(TAG, "MA deleted: " + sourceSongList + sourceSongList.get(0).getUrlCloudBackground());
                                    Toast.makeText(MainActivity.this, "Des éléments ont été spprimés de votre liste de chansons.", Toast.LENGTH_SHORT).show();
                                    if (dialogWait != null) {
                                        dialogWait.dismiss();
                                    }
                                    songsAdapter.swapSongs(sourceSongList, recordSources, songToPlays, songOnClouds, SongOnPhonesBS, SongOnPhonesLive);

                                } else {
                                    //mettre un dialogue pour changer ou non
                                    Log.d(TAG, "MA onChanged: modification avant dialogAlert ");
                                    if (dialogWait != null) {
                                        dialogWait.dismiss();
                                    }
                                    DialogFragment dialog = new DialogMajSS();
                                    dialog.show(getSupportFragmentManager(), "TAG");
                                }
                                break;
                            case "newSS":
                                Toast.makeText(MainActivity.this, "Veuillez patienter le temps de mettre en place toutes les chansons...", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "MA onChanged: newSS ");
                                if (dialogWait != null) {
                                    dialogWait.dismiss();
                                }
                                affichageRecyclerView(sourceSongList);
                                songsAdapter.swapSongs(sourceSongList, recordSources, songToPlays, songOnClouds, SongOnPhonesBS, SongOnPhonesLive);
                                break;
                            case "newSongOnPhone":
                                Log.d(TAG, "MA onChanged: lancement du SA pour le single");
                                affichageRecyclerView(sourceSongList);
                                Log.d(TAG, "MA onChanged: position " + positionToDownload);
                                for(List<Song> songList : songOnClouds){
                                    if(songList!=null) {
                                        for (Song song : songList) {
                                            Log.d(TAG, "MA initDataSongs: listOnClouds " + song.getSourceSongTitre() + " " + song.getPupitre());
                                        }
                                    }
                                }
                                songsAdapter.swapSingleSong(positionToDownload, songToPlay, songOnClouds, SongOnPhonesBS, SongOnPhonesLive, recordSources);
                                break;
                            case "newSongsOnPhone":
                                Log.d(TAG, "MA onChanged: lancement du SA pour le multiple");
                                if (dialogWait != null) {
                                    dialogWait.dismiss();
                                }
                                affichageRecyclerView(sourceSongList);
                                songsAdapter.swapSongs(sourceSongList, recordSources, songToPlays, songOnClouds, SongOnPhonesBS, SongOnPhonesLive);
                                break;
                            case "newRecord":
                                Log.d(TAG, "MA onChanged: lancement du SA pour le recordSong " + positionToRecord);
                                affichageRecyclerView(sourceSongList);
                                songsAdapter.swaprecordedSongs(positionToRecord, recordSources, songToPlay, songOnClouds, SongOnPhonesBS, SongOnPhonesLive);

                                break;
                            case "deleteSingleSongOnPhone":
                                Log.d(TAG, "MA onChanged: lancement du SA pour le single (delete) ");
                                affichageRecyclerView(sourceSongList);
                                for(List<Song> songList : songOnClouds){
                                    if(songList!=null) {
                                        for (Song song : songList) {
                                            Log.d(TAG, "MA initDataSongs: listOnClouds " + song.getSourceSongTitre() + " " + song.getPupitre());
                                        }
                                    }
                                }
                                Log.d(TAG, "MA onChanged: position (delete) " + positionToDelete);
                                songsAdapter.swapSingleDeleteSong(positionToDelete, songToPlays, songOnClouds, SongOnPhonesBS, SongOnPhonesLive, recordSources);
                                break;
                            case "deleteMultipleSongOnPhone":
                                Log.d(TAG, "MA onChanged: lancement du SA pour le multiple delete");
                                affichageRecyclerView(sourceSongList);
                                songsAdapter.swapSongs(sourceSongList, recordSources, songToPlays, songOnClouds, SongOnPhonesBS, SongOnPhonesLive);
                                break;
                        }
                    } else {
                        Log.d(TAG, "MA onChanged: conditions pas réunies");
                    }
                } else {
                    Log.d(TAG, "MA onChanged: listsongs null ");
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("coucou", "MA onStart: A  " + current_user_id);

        //todo à voir si utile du coup ?
        if (TextUtils.isEmpty(current_user_id)) {
            sendToLogin();

            Log.d("coucou", "MA onSTart:B Start " + current_user_id);
        } else {

            Log.d(TAG, "MA onStart: currentuser non null");

            Log.d("coucou", "MA onStart C: " + current_user_id);
        }
    }

    private void AlertBox() {
        Log.d(TAG, "MA AlertBox: ");
        dialogWait = new DialogMA();
        Bundle args = new Bundle();
        args.putString("origine", "waitSongs");
        dialogWait.setArguments(args);
        dialogWait.show(getSupportFragmentManager(), "TAG");
    }

    @Override
    public void onDialogPositiveClick() {
        Log.d(TAG, "MA onDialogPositiveClick dialog positif: " + sourceSongList);
        songsAdapter.swapSongs(sourceSongList, recordSources, songToPlays, songOnClouds, SongOnPhonesBS, SongOnPhonesLive);
    }

    @Override
    public void onDialogNegativeClick() {

        Toast.makeText(this, "Les nouvelles chansons appraitront au prochain lancement de l'application", Toast.LENGTH_LONG).show();
    }

    private void setUpSharedPreferences() {
        boolean installation = sharedPreferences.getBoolean("installation", true);

        if (installation) {

            getData();
            Log.d(TAG, "MA setUpSharedPreferences: installation " + current_user_id);
            deleteDbRoom();

        } else {
            Log.d(TAG, "MA setUpSharedPreferences: plus une installation ");
        }

        Log.d(TAG, "MA setUpSharedPreferences: idchorale " + sharedPreferences.getString("idchorale", ""));

    }

    private void getData() {
        mViewModel.getData(current_user_id);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //todo faire pour gérer entre autres si on veut charger les songs qui viennent d être changées

        Log.d(TAG, "MA onSharedPreferenceChanged: key " + key);
        Set<String> pupitreAuto = new HashSet<>();

        if (key.equals(getString(R.string.pref_pupitre_key))) {
            Log.d(TAG, "MA onSharedPreferenceChanged: changement pupitres auto " + sharedPreferences.getStringSet(key, pupitreAuto));
            //todo voir si on propose de charger les pupitres non chargées encore

        }

        if (key.equals("spectacleDeleted")){
            Log.d(TAG, "MA onSharedPreferenceChanged: key deletedspectacles");
            boolean deletedSpectacles = sharedPreferences.getBoolean("spectacleDeleted",false);
            if(deletedSpectacles){
                Log.d(TAG, "MA onSharedPreferenceChanged: spectacle deleted ");
                Toast.makeText(this, "Spectacle supprimé, veuillez patienter", Toast.LENGTH_LONG).show();
                editor = sharedPreferences.edit();
                editor.putString("currentSpectacle", "Tous");
                editor.putBoolean("spectacleDeleted",false);
                editor.apply();
                invalidateOptionsMenu();
            }else{
                Log.d(TAG, "MA onSharedPreferenceChanged: nothing happened spectacle deleted");
            }
        }
        if (key.equals("maj_auto")) {
            Log.d(TAG, "MA onSharedPreferenceChanged: changement maj_auto " + sharedPreferences.getBoolean(key, true));
        }

        if (key.equals("initializeData")) {
            Log.d(TAG, "MA onSharedPreferenceChanged: initialize ");
            mViewModel.getSourceSongs();
        }

        if (key.equals("currentSaison")) {
            /*getCurrentSpectacles();

            if(threadSpectacles!=null){
                try {
                    threadSpectacles.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "MA onSharedPreferenceChanged: current saison");*/
            // mViewModel.getSourceSongs();
        }

        if (key.equals("currentSpectacles")) {
            Log.d(TAG, "MA onSharedPreferenceChanged: currentSpectacles");
            getCurrentSpectacles();

            if (threadSpectacles != null) {
                try {
                    threadSpectacles.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "MA onSharedPreferenceChanged: current spectacles ");

        }

        if (key.equals("currentSpectacle")) {
            Log.d(TAG, "MA onSharedPreferenceChanged: currentSpectacle");
            mViewModel.getSourceSongs();
        }

        if (key.equals("role")) {
            Log.d(TAG, "MA onSharedPreferenceChanged: role ");
            mCurrentAuthRole = sharedPreferences.getString("role", "Choriste");
        }
    }

    private void getListSongs() {
        Log.d(TAG, "MA getListSongs: début");

        listSongs = mViewModel.getListSongs();

        Log.d(TAG, "MA getListSongs: début après aller chercher listSongs " + listSongs);

        if (listSongs != null) {
            sourceSongList = listSongs.getSourceSongs();
            Log.d(TAG, "MA getListSongs: début liste "+sourceSongList);
            songOnPhones = listSongs.getSongsOnPhonesA();
            Log.d(TAG, "MA getSongElements songOnphones: " + songOnPhones);
            Log.d(TAG, "MA getListSongs: avant recordResources");
            recordSources = listSongs.getRecordSourcesA();
            Log.d(TAG, "MA getSongElements recordsources: " + recordSources);

            songToPlays = listSongs.getSongToPlaysA();
            Log.d(TAG, "MA getListSongs: juste après" + songToPlays);

            songOnClouds = listSongs.getSongsOnCloudsA();
            Log.d(TAG, "MA getSongElements songOnclouds: " + songOnClouds);


            SongOnPhonesBS = listSongs.getSongOnPhoneBSA();
            Log.d(TAG, "MA getSongElements getSongOnPhoneBSA: " + SongOnPhonesBS);

            SongOnPhonesLive = listSongs.getSongOnPhoneLiveA();
            Log.d(TAG, "MA getSongElements getSongOnPhoneLiveA: " + SongOnPhonesLive);

        } else {
            Log.d(TAG, "MA getListSongs: est null " + listSongs);
        }
    }


    private void affichageRecyclerView(List<SourceSong> sourceSongs) {

        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
        }

        if (typeSS.equals("newSongOnPhone")) {
            mPosition = positionToDownload;
        } else if (typeSS.equals("newRecord")) {
            mPosition = positionToRecord;
        }

        Log.d(TAG, "MA affichageRecyclerView: " + mPosition);
        recyclerView.smoothScrollToPosition(mPosition);

        Log.d("coucou", "MA onCreate: observers - mposition " + mPosition);
        // Show the weather list or the loading screen based on whether the forecast data exists
        // and is loaded
        if (sourceSongs != null && sourceSongs.size() != 0 && recordSources != null && recordSources.size() != 0) {
            Log.d(TAG, "MA onChanged: Ready to affiche  !");

            showSongsDataView();
            Log.d("coucou", "MA onCreate: showDataView");
        } else {
            showLoading();
            Log.d("coucou", "MA onCreate: showLoading");
        }
    }


    //todo mettre des conditions pour rester logger entre 2 utilisations (à conserver ?) dans OnDestroy ? On Stop ?
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    private void sendToLogin() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }


    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showSongsDataView() {
        Log.d(TAG, "MA showSongsDataView: ");

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
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);

        if (!TextUtils.isEmpty(current_user_id) && mCurrentAuthRole.equals("Super Admin")) {
            Log.d(TAG, "MA onCreateOptionsMenu: true");

            menu.getItem(5).setVisible(true);
        } else {
            Log.d(TAG, "MA onCreateOptionsMenu: false");
            menu.getItem(5).setVisible(false);
        }
        Log.d(TAG, "MA onCreateOptionsMenu: ");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "MA onOptionsItemSelected: ");
        switch (item.getItemId()) {

            case R.id.trombi:
                launchTrombiActivity();
                break;


            case R.id.parametres:
                launchSettingsActivity();
                break;

            case R.id.spectacles:
                launchChoiceSpectacle();
                break;

            case R.id.load_pupitre:
                //todo faire les calculs pour ne prposer que les pupitres non complets
                loadSongsPupitre();
                Log.d(TAG, "MA onOptionsItemSelected: " + sourceSongList);
                break;

            case R.id.delete_pupitre:
                //todo faire les calculs pour ne prposer que les pupitres qui ont des chansons à supprimer
                deleteSongsPupitre();
                break;

            case R.id.admin:
                Toast.makeText(this, "Vous êtes " + mCurrentAuthRole, Toast.LENGTH_SHORT).show();
                Intent startAdminHomeActivity = new Intent(this, AdminHome.class);
                startActivity(startAdminHomeActivity);
                break;

            case R.id.contact:
                String[] adresses={"thierryc973@gmail.com"};
                Intent contactMail = new Intent(Intent.ACTION_SENDTO);
                contactMail.setData(Uri.parse("mailto:")); // only email apps should handle this
                contactMail.putExtra(Intent.EXTRA_EMAIL, adresses);
                contactMail.putExtra(Intent.EXTRA_SUBJECT, "Remarques sur l'application Korale");
                if (contactMail.resolveActivity(getPackageManager()) != null) {
                    startActivity(contactMail);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (threadSpectacles != null) {
            try {
                threadSpectacles.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String spectacle = sharedPreferences.getString("currentSpectacle", "Tous");

        MenuItem spectacleItem = menu.getItem(6);
        spectacleItem.setTitle(spectacle);

        Log.d(TAG, "MA onPrepareOptionsMenu: "+spectacle+" "+spectacleItem.getTitle());

        return super.onPrepareOptionsMenu(menu);
    }

    private void launchTrombiActivity() {
        Intent startTrombiActivity = new Intent(this, TrombiActivity.class);
        startActivity(startTrombiActivity);
    }

    private void getCurrentSpectacles() {
        Set<String> currentSpectacles = sharedPreferences.getStringSet("currentSpectacles", null);
        Log.d(TAG, "MA getCurrentSpectacles: " + currentSpectacles);
        if (namesSpectacles != null) {
            namesSpectacles = new ArrayList<>();
        }

        threadSpectacles = new Thread(() -> {
            if (currentSpectacles != null) {
                for (String idSpectacle : currentSpectacles) {
                    Spectacle spectacle = dataBase.spectacleDao().getSpectacleById(idSpectacle);
                    Log.d(TAG, "MA run: getCurrentSpectacles " + idSpectacle+" "+spectacle);
                    if (spectacle != null) {
                        String spectacleName = spectacle.getSpectacleName();
                        Log.d(TAG, "MA run: nom du spectacle " + spectacleName);
                        namesSpectacles.add(spectacleName);
                    } else {
                        Log.d(TAG, "MA run: else getCurrent Spectacles null "+idSpectacle);
                    }
                }
            }
        });
        threadSpectacles.start();
    }

    private void launchChoiceSpectacle() {
        Log.d(TAG, "MA launchChoiceSpectacle: noms spectacles " + namesSpectacles);
        DialogFragment dialog = new DialogSpectacleFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("spectaclesName", namesSpectacles);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "TAG");
        Log.d(TAG, "MA OnDialogSpectacle fragment: ");
    }

    private void deleteSongsPupitre() {
        Log.d(TAG, "MA deleteSongsPupitre: ");
        DialogFragment dialog = new DialogMA();
        Bundle args = new Bundle();
        args.putString("origine", "deletePupitres");
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "TAG");
    }

    private void loadSongsPupitre() {
        Log.d(TAG, "MA loadSongsPupitre: ");
        DialogFragment dialog = new DialogMA();
        Bundle args = new Bundle();
        args.putString("origine", "downloadPupitres");
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "TAG");

    }

    private void launchSettingsActivity() {
        Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(startSettingsActivity);
    }


    /**
     * Méthode passée dans le listener dans l'adapter
     */


    @Override
    public void OnClickedItem(String titre, String message) {

        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message + "-" + titre, Toast.LENGTH_SHORT);
        mToast.show();
    }


    @Override
    public Song OnPlaySong(SourceSong sourceSong, Pupitre pupitre, RecordSource source) {
        Log.d(TAG, "MA OnPlaySong: au secours " + sourceSong + " " + sourceSong.getTitre() + " " + pupitre + " " + source + " " + sourceSongList);

        Song songToPlay = null;

        for (SourceSong sourceSong1 : sourceSongList) {
            Log.d(TAG, "MA OnPlaySong: " + sourceSong1.getTitre());
        }

        int indexSourceSong = sourceSongList.indexOf(sourceSong);
        Log.d(TAG, "MA OnPlaySong: index " + indexSourceSong);

        if (source == RecordSource.BANDE_SON) {
            Log.d(TAG, "MA OnPlaySong: BS");
            for (Song song : SongOnPhonesBS.get(indexSourceSong)) {
                Log.d(TAG, "MA OnPlaySong: " + song.getPupitre());
                if (song.getPupitre() == pupitre && song.getRecordSource() == source) {
                    songToPlay = song;
                    Log.d(TAG, "MA OnPlaySong: " + songToPlay + " " + songToPlay.getPupitre());
                }
            }
        } else if (source == RecordSource.LIVE) {
            for (Song song : SongOnPhonesLive.get(indexSourceSong)) {
                if (song.getPupitre() == pupitre && song.getRecordSource() == source) {
                    songToPlay = song;
                }
            }
        }
        return songToPlay;
    }

    @Override
    public Song OnPlayFirstSong(final int position, final RecordSource recordSource) {

        if (recordSource == RecordSource.BANDE_SON) {
            firstSongPlayed = SongOnPhonesBS.get(position).get(0);
        } else if (recordSource == RecordSource.LIVE) {
            firstSongPlayed = SongOnPhonesLive.get(position).get(0);
        }
        Log.d(TAG, "MA OnPlayFirstSong: " + firstSongPlayed);
        return firstSongPlayed;
    }

    @Override
    public List<Song> OnListRecordedSongsOnCloud(int position, RecordSource recordSource) {

        List<Song> cloudSongs = new ArrayList<>();

        if (recordSource == RecordSource.BANDE_SON) {

            cloudSongs = songOnClouds.get(position);
        }
        return cloudSongs;
    }

    @Override
    public List<Song> OnListRecordedSongsOnPhone(int position, RecordSource recordSource) {

        List<Song> phoneSongs = new ArrayList<>();

        Log.d(TAG, "MA OnListRecordedSongsOnPhone: " + position + " " + recordSource);

        if (recordSource == RecordSource.BANDE_SON) {
            phoneSongs = SongOnPhonesBS.get(position);
            Log.d(TAG, "MA OnListRecordedSongsOnPhone: phoneSongs " + phoneSongs);

        } else if (recordSource == RecordSource.LIVE) {

            phoneSongs = SongOnPhonesLive.get(position);
        }
        Log.d(TAG, "MA OnListRecordedSongsOnPhone: " + phoneSongs);
        return phoneSongs;
    }

    @Override
    public void OnSaveRecordSong(Song song) {
        mViewModel.setRecordSongInAppDb(song);
    }

    /**
     * gestion du long click pour téléchargement single
     */

    @Override
    public void OnLongClickItem(int position, Song song) {
        songToPlay = song;
        Log.d(TAG, "MA OnLongClickItem: " + position);
        DialogMA dialog = new DialogMA();
        Bundle args = new Bundle();
        args.putString("origine", "downloadSingle");
        args.putInt("position", position);
        dialog.setArguments(args);
        dialog.setSong(song);
        dialog.show(getSupportFragmentManager(), "TAG");
    }

    @Override
    public void OnLongClickDeleteItem(int position, Song song) {


        Log.d(TAG, "MA OnLongClickItem B: " + position);
        DialogMA dialog = new DialogMA();
        Bundle args = new Bundle();
        args.putString("origine", "deleteSingle");
        args.putInt("position", position);
        dialog.setArguments(args);
        dialog.setSong(song);
        dialog.show(getSupportFragmentManager(), "TAG");
    }

    @Override
    public void setPlaying(boolean playing) {
        isMediaPlaying=playing;

    }

    @Override
    public boolean isPlaying() {
        return isMediaPlaying;
    }

    @Override
    public void setPausing(boolean pausing) {
        isMediaPausing=pausing;
    }

    @Override
    public boolean isPausing() {
        return isMediaPausing;
    }


    @Override
    public void onDialogMAPositiveClick(int position, Song song) {
        positionToDownload = position;
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mViewModel.downloadSingleSong(song);
        Toast.makeText(this, "Votre chanson est en train de se charger sur le téléphone", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "MA onDialogMAPositiveClick: chargement sur tel du single (position) " + position);
    }

    @Override
    public void onDialogMANegativeClick() {
        Toast.makeText(this, "Vous pourrez la charger ultérieurement...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogMADeletePositiveClick(int position, Song song) {
        positionToDelete = position;
        if (song != null) {
            mViewModel.deleteSingleSong(song);
        } else {
            Log.d(TAG, "MA onDialogMADeletePositiveClick: chanson null");
        }
        Toast.makeText(this, "Votre chanson est supprimé sur le téléphone", Toast.LENGTH_LONG).show();
        Log.d(TAG, "MA onDialogMAPositiveClick: suppression sur tel du single (position) " + position);
    }

    @Override
    public void onDialogMADeleteNegativeClick() {
        Toast.makeText(this, "Vous pourrez la supprimer ultérieurement...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDialogMADownloadPupitresPositiveClick(List<Integer> selectedItems) {
        getListPupitresToDownloadDelete(selectedItems);
        Log.d(TAG, "onDialogMADownloadPupitresPositiveClick: selectedItems " + selectedItems + " " + pupitresToDownloadDelete + " " + songOnClouds.size() + " " + songOnClouds);
        //Toast.makeText(this, "Vos chansons sont en train de se charger sur le téléphone", Toast.LENGTH_SHORT).show();
        AlertBox();

        mLoadingIndicator.setVisibility(View.VISIBLE);
        List<Song> songsToDownload = new ArrayList<>();

        for (Pupitre pupitre : pupitresToDownloadDelete) {
            for (List<Song> songs : songOnClouds) {
                Log.d(TAG, "MA onDialogMADownloadPupitresPositiveClick: songs " + songs);
                if (songs != null) {
                    for (Song song : songs) {
                        Log.d(TAG, "MA onDialogMADownloadPupitresPositiveClick: song " + song);
                        if (song.getPupitre() == pupitre) {
                            if (song.getUpdatePhoneMp3() == null) {
                                songsToDownload.add(song);
                            } else {
                                Log.d(TAG, "NDS downloadMp3PupitresSongs: déjà présent dans le phone ");
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "onDialogMADownloadPupitresPositiveClick: songsNull");
                }
            }
        }
        mLoadingIndicator.setVisibility(View.VISIBLE);
        Log.d(TAG, "MA onDialogMADownloadPupitresPositiveClick: " + songsToDownload);
        mViewModel.downloadPupitresSongs(songsToDownload);

    }

    private void getListPupitresToDownloadDelete(List<Integer> selectedItems) {
        pupitresToDownloadDelete = new ArrayList<>();

        for (int itemId : selectedItems) {
            String pupitreToLoad = null;
            switch (itemId) {
                case 0:
                    pupitreToLoad = "TUTTI";
                    break;
                case 1:
                    pupitreToLoad = "BASSE";
                    break;
                case 2:
                    pupitreToLoad = "TENOR";
                    break;
                case 3:
                    pupitreToLoad = "ALTO";
                    break;
                case 4:
                    pupitreToLoad = "SOPRANO";
                    break;
            }
            if (pupitreToLoad != null) {
                pupitresToDownloadDelete.add(SongsUtilities.converttoPupitre(pupitreToLoad));
            }
        }
    }

    @Override
    public void onDialogMADownloadPupitresNegativeClick() {
        Toast.makeText(this, "Vous pourrez les charger ultérieurement...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDialogMADeletePupitresPositiveClick(List<Integer> selectedItems) {
        getListPupitresToDownloadDelete(selectedItems);
        Log.d(TAG, "onDialogMADeletePupitresPositiveClick: selectedItems " + selectedItems + " " + pupitresToDownloadDelete);
        Toast.makeText(this, "Vos chansons sont en train d'être supprimer de votre téléphone", Toast.LENGTH_SHORT).show();

        List<Song> songsToDelete = new ArrayList<>();

        for (Pupitre pupitre : pupitresToDownloadDelete) {
            for (List<Song> songs : songOnPhones) {
                if (songs != null) {
                    for (Song song : songs) {
                        if (song.getPupitre() == pupitre) {
                            songsToDelete.add(song);
                        }
                    }
                }
            }
        }
        Log.d(TAG, "MA onDialogMADownloadPupitresPositiveClick: " + songsToDelete);
        mViewModel.deletePupitresSongs(songsToDelete);
    }

    @Override
    public void onDialogMADeletePupitresNegativeClick() {
        Toast.makeText(this, "Vous pourrez les supprimer ultérieurement...", Toast.LENGTH_LONG).show();
    }


    @Override
    public void OnDialogRecord(int position, SongsViewHolder songsViewHolder) {
        this.positionToRecord = position;
        mPositiveClickListener = songsViewHolder;
        DialogFragment dialog = new DialogRecordFragment();
        dialog.show(getSupportFragmentManager(), "TAG");
        Log.d(TAG, "MA OnDialogRecord: ");
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, final Pupitre pupitre) {
        Log.d(TAG, "MA onDialogPositiveClick: enregistrer " + pupitre + " " + positionToRecord);
        Song song = new Song(sourceSongList.get(positionToRecord).getTitre(), RecordSource.LIVE, pupitre, new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        songToPlay = song;
        mPositiveClickListener.OnRecord(song);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        Log.d(TAG, "MA onDialogPositiveClick: annuler");
    }

    @Override
    public void onDialogSpectaclePositiveClick(String spectacle) {

        Log.d(TAG, "MA onDialogSpectaclePositiveClick: " + spectacle);
        this.spectacle = spectacle;

        editor = sharedPreferences.edit();
        editor.putString("currentSpectacle", spectacle);
        editor.apply();

        invalidateOptionsMenu();
    }

    @Override
    public void onDialogSpectacleNegativeClick() {
        Log.d(TAG, "MA onDialogPositiveSpectacleClick: annuler");
    }

    /**
     * Permission pour le stockage externe
     * Permission utilisée suivant les versions
     */
    @Override
    public void OnRequestPermission() {
        Log.d(TAG, "MA OnRequestPermission: entrée");
        if (!checkPermissionFromDevice()) {
            Log.d(TAG, "MA OnRequestPermission: request permission");
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION_CODE);
        Log.d(TAG, "MA requestPermission: ");
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        Log.d(TAG, "MA checkPermissionFromDevice: " + (write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED));
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void OnProgressLoading(int progress) {
        mLoadingIndicator.setProgress(progress);
    }

    @Override
    public void OnProgressSongs(int nbSong, int nbSongTotal) {
        if(dialogWait!=null){
        dialogWait.dismiss();
        }
        dialogWait = new DialogMA();
        Bundle args = new Bundle();
        args.putString("origine", "progress");
        args.putInt("nombreSongs",nbSong);
        args.putInt("nombreTotalSongs",nbSongTotal);
        dialogWait.setArguments(args);
        dialogWait.show(getSupportFragmentManager(), "TAG");

        Log.d(TAG, "MA OnProgressSongs: il y a "+nbSong+" sur "+nbSongTotal+ "chansons chargées sur votre téléphone");
        //Toast.makeText(this, nbSong+ "/"+nbSongTotal+" chansons téléchargées", Toast.LENGTH_SHORT).show();
       // Log.d(TAG, "MA OnProgressSongs: il y a "+nbSong+" sur "+nbSongTotal+ "chansons chargées sur votre téléphone");

        //dialogWait.putProgress(nbSong,nbSongTotal);


    }

    //todo à renommer
    public interface OnPositiveClickListener {
        void OnRecord(Song song);

        void OndeleteSong();
    }

    private void deleteDbRoom() {
        AppExecutors mExecutors = AppExecutors.getInstance();
        mExecutors.diskIO().execute(() -> {
            dataBase.songsDao().deleteAll();
            dataBase.sourceSongDao().deleteAll();
        });
    }

    private boolean haveInternetConnection(){
        // Fonction haveInternetConnection : return true si connecté, return false dans le cas contraire
        NetworkInfo network = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (network==null || !network.isConnected())
        {
            Log.d(TAG, "MA haveInternetConnection: network pas connecté à internet ");

            // Le périphérique n'est pas connecté à Internet
            return false;
        }else{
            boolean wifi = network.getType() == ConnectivityManager.TYPE_WIFI;
            boolean res = network.getType() == ConnectivityManager.TYPE_MOBILE;


            Log.d(TAG, "MA haveInternetConnection: wifi " + wifi + " réseau 4G " + res);
            return true;

        }
        // Le périphérique est connecté à Internet

    }
}


