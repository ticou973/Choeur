package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import dedicace.com.R;
import dedicace.com.data.database.SourceSong;

public class ModifySourceSong extends AppCompatActivity implements SourceSongAdapter.OnItemListener{

    private RecyclerView recyclerSS;
    private SourceSongAdapter sourceSongAdapter;
    private List<SourceSong> listSourceSongs = new ArrayList<>();
    private List<String> listId = new ArrayList<>();
    private static final String TAG ="coucou";
    private FirebaseFirestore db;
    private String origine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_source_song);
        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        origine = intent.getStringExtra("origine");

        FloatingActionButton fab = findViewById(R.id.fab_SS);

        fab.setOnClickListener(view -> {
            Intent startCreateSSActivity = new Intent(ModifySourceSong.this,CreateSourceSong.class);
            startActivity(startCreateSSActivity);
        });
        recyclerSS = findViewById(R.id.recyclerview_cloud_SS);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ModifySourceSong.this);
        recyclerSS.setLayoutManager(layoutManager);
        recyclerSS.setHasFixedSize(true);

        db=FirebaseFirestore.getInstance();

        getListSourceSongs();
    }

    //todo plus tard voir comment ne prendre que les sources songs dd'une chorale donnée
    private void getListSourceSongs() {
        try {
            db.collection("sourceSongs")
                    .get()
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "MSp onComplete: sourceSongs " + Thread.currentThread().getName());
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, "NDS-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));

                                String titre, groupe, baseUrlOriginalSong, urlCloudBackground,idDocument;
                                Date maj;
                                int duration;
                                Timestamp majSS;

                                idDocument = document.getId();
                                listId.add(idDocument);

                                titre = (String) document.getData().get("titre");
                                groupe = (String) document.getData().get("groupe");
                                duration = ((Long) Objects.requireNonNull(document.getData().get("duration"))).intValue();
                                baseUrlOriginalSong = (String) document.getData().get("original_song");
                                majSS= (Timestamp) document.getData().get("maj");
                                maj = Objects.requireNonNull(majSS).toDate();
                                urlCloudBackground = (String) document.getData().get("background");

                                Log.d(TAG, "MSS-exec onComplete:A SourceSongs " + titre + " " + groupe + " " + duration + " " + baseUrlOriginalSong + " " + maj + " " + urlCloudBackground);
                                SourceSong sourceSong = new SourceSong(titre, groupe, duration, urlCloudBackground, baseUrlOriginalSong, maj);
                                listSourceSongs.add(sourceSong);
                            }
                                Log.d(TAG, "MSS onComplete: pas de new intent");
                                sourceSongAdapter = new SourceSongAdapter(listSourceSongs);
                                recyclerSS.setAdapter(sourceSongAdapter);


                            Log.d(TAG, "MSS fetchSourceSongs: après fetch");
                        } else {
                            Log.d(TAG, "MSS-exec Error getting documents.", task.getException());
                        }
                    });

        } catch (Exception e) {
            // Server probably invalid
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int i) {

        if (origine.equals("AdminHome")||origine.equals("ModifySSDetails")) {
            Log.d(TAG, "MSS onItemClick: A "+i);
        Intent startDetailsSSActivity = new Intent(ModifySourceSong.this, ModifySourceSongDetails.class);
        Bundle args = new Bundle();
        args.putString("idSS", listId.get(i));
        args.putString("oldTitre", listSourceSongs.get(i).getTitre());
        args.putString("oldGroupe", listSourceSongs.get(i).getGroupe());
        args.putInt("oldDuration", listSourceSongs.get(i).getDuration());
        startDetailsSSActivity.putExtra("bundleSS", args);
        startActivity(startDetailsSSActivity);

        }else if(origine.equals("CreateSong")){
            Log.d(TAG, "MSS onItemClick: B"+i);
            Intent result = new Intent();
            result.putExtra("titreselected",listSourceSongs.get(i).getTitre());
            setResult(RESULT_OK,result);
            finish();
        }else if(origine.equals("CreateSpectacle")||origine.equals("GenList")){
            Log.d(TAG, "MSS onItemClick: C"+i);
            Intent result = new Intent();
            result.putExtra("titreselected",listSourceSongs.get(i).getTitre());
            result.putExtra("idselected",listId.get(i));
            setResult(RESULT_OK,result);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

}
