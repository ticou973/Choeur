package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.Choriste;
import dedicace.com.ui.PlaySong.DialogMA;
import dedicace.com.ui.PlaySong.DialogMajSS;
import dedicace.com.utilities.AppExecutors;
import dedicace.com.utilities.InjectorUtils;

public class TrombiActivity extends AppCompatActivity implements TrombiAdapter.ListItemClickListener, DialogMajChoristes.DialogMajChoristesListener {

    private LiveData<List<Choriste>> choristes;
    //ViewModel
    private TrombiActivityViewModel mViewModel;
    private TrombiActivityViewModelFactory mfactory;
    private boolean installation;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "coucou";
    private AppExecutors mExecutors;
    private AppDataBase dataBase;
    private Thread currentThread;
    private String typeChoriste;
    private  RecyclerView recyclerView;
    private ProgressBar mLoadingIndicator;
    private DialogMA dialogWait;
    private int mPosition = RecyclerView.NO_POSITION;
    private TrombiAdapter trombiAdapter;
    private List<Choriste> choristes1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trombi);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //faire une liste de choristes pour que cela marche


        mLoadingIndicator = findViewById(R.id.pb_loading_indicator_choriste);
        recyclerView = findViewById(R.id.recyclerview_trombi);
        //Adapter
        trombiAdapter = new TrombiAdapter(this,this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(trombiAdapter);

        mfactory = InjectorUtils.provideChoristeViewModelFactory(this.getApplicationContext(),this);

        mViewModel = ViewModelProviders.of(this,mfactory).get(TrombiActivityViewModel.class);

        setUp();

        choristes = mViewModel.getChoristes();

        choristes.observe(this, choristes -> {

            choristes1=choristes;

            Log.d(TAG, "TA onChanged: Alerte, ça bouge dans le coin !" + choristes + " " + mViewModel.getChoristes() + " " + Thread.currentThread().getName());

            currentThread = mViewModel.getCurrentThread();
            typeChoriste = mViewModel.getTypeChoriste();

            //permet que la listsongs de CR se calcule
            if (typeChoriste == null) {
                Log.d(TAG, "TA onChanged: typeChoraiste null et alertbox");
                showLoading();
                AlertBox();
            } else {
                if (!typeChoriste.equals("modificationSS")) {
                    Log.d(TAG, "TA onChanged: typeChoriste non null et alertbox " + typeChoriste);
                    showLoading();
                }
            }

            if (choristes != null && choristes.size() != 0 && currentThread != null) {
                Log.d(TAG, "TA onChanged: currentThread " + currentThread);
                try {
                    currentThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "TA onChanged: interrupted exception");
                }
                Log.d("coucou", "TA onCreate: Thread fini " + typeChoriste);

            }


            if (choristes != null) {

                    Log.d(TAG, "TA onChanged: conditions toutes réunies" + typeChoriste);
                    switch (typeChoriste) {
                        case "oldChoriste":
                            Log.d(TAG, "TA onChanged: type OldChoriste");
                            if (dialogWait != null) {
                                dialogWait.dismiss();
                            }
                            if(choristes.size()!=0) {
                                Log.d(TAG, "TA visualisation: if old");
                                affichageRecyclerView(choristes);
                                trombiAdapter.swapChoristes(choristes);
                            }
                            break;
                        case "modificationChoriste":
                                //mettre un dialogue pour changer ou non
                                Log.d(TAG, "TA onChanged: modification avant dialogAlert ");
                                if (dialogWait != null) {
                                    dialogWait.dismiss();
                                }
                                DialogFragment dialog = new DialogMajSS();
                                dialog.show(getSupportFragmentManager(), "TAG");
                            break;
                        case "newChoriste":
                            Toast.makeText(TrombiActivity.this, "Veuillez patienter le temps de mettre en place toutes les chansons...", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "MA onChanged: newSS ");
                            if (dialogWait != null) {
                                dialogWait.dismiss();
                            }
                            affichageRecyclerView(choristes);
                            trombiAdapter.swapChoristes(choristes);
                            break;
                    }

            } else {
                Log.d(TAG, "TA onChanged: choristes null ");
            }

        });
    }

    private void affichageRecyclerView(List<Choriste> choristes) {

        if (mPosition == RecyclerView.NO_POSITION) {
            mPosition = 0;
        }

        Log.d(TAG, "TA affichageRecyclerView: " + mPosition);
        recyclerView.smoothScrollToPosition(mPosition);

        Log.d("coucou", "MA onCreate: observers - mposition " + mPosition);
        // Show the weather list or the loading screen based on whether the forecast data exists
        // and is loaded
        if (choristes != null && choristes.size() != 0) {
            Log.d(TAG, "TA onChanged: Ready to affiche  !");

            showSongsDataView();
            Log.d("coucou", "MA onCreate: showDataView");
        } else {
            showLoading();
            Log.d("coucou", "MA onCreate: showLoading");
        }
    }

    private void showSongsDataView() {
        Log.d(TAG, "TA showSongsDataView: ");
        // First, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // Finally, make sure the weather data is visible
        recyclerView.setVisibility(View.VISIBLE);
    }


    private void AlertBox() {
        Log.d(TAG, "TA AlertBox: ");
        dialogWait = new DialogMA();
        Bundle args = new Bundle();
        args.putString("origine", "waitChoristes");
        dialogWait.setArguments(args);
        dialogWait.show(getSupportFragmentManager(), "TAG");
    }

    private void showLoading() {
        // Then, hide the weather data
        recyclerView.setVisibility(View.INVISIBLE);
        // Finally, show the loading indicator
        mLoadingIndicator.setVisibility(View.VISIBLE);

    }

    private void setUp() {
        installation = sharedPreferences.getBoolean("installationTrombi", true);

        if (installation) {
            //getData();
            Log.d(TAG, "TA setUpSharedPreferences: installation ");
            deleteDbRoom();

        } else {
            Log.d(TAG, "TA setUpSharedPreferences: plus une installation ");
        }

    }

    private void deleteDbRoom() {
        mExecutors = AppExecutors.getInstance();
        mExecutors.diskIO().execute(() -> {
            dataBase.choristeDao().deleteAll();
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnClickItem() {

    }

    @Override
    public void onDialogPositiveClick() {
        Log.d(TAG, "TA onDialogPositiveClick dialog positif: " + choristes);
        trombiAdapter.swapChoristes(choristes1);
    }

    @Override
    public void onDialogNegativeClick() {
        Toast.makeText(this, "Les nouveaux choristes appraitront au prochain lancement de l'application", Toast.LENGTH_LONG).show();
    }
}
