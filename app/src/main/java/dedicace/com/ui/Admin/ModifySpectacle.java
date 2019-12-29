package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import dedicace.com.R;
import dedicace.com.data.database.Spectacle;

public class ModifySpectacle extends AppCompatActivity implements SpectacleAdapter.OnItemListener{
    private RecyclerView recyclerSpectacles;
    private FloatingActionButton fab;
    private String origine;
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;
    private FirebaseFirestore db;
    private List<Spectacle> listSpectacles = new ArrayList<>();
    private SpectacleAdapter spectacleAdapter;

    private List<String> listId = new ArrayList<>();

    private boolean newIntent = false;
    private String idChorale;
    private String nomChoraleStr;
    private ArrayList<String> oldTitresNameStr = new ArrayList<>();
    private Intent startDetailsSpectaclesActivity;
    private Bundle args= new Bundle();
    private int entier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_spectacle);
        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getIntentBundle();

        fab = findViewById(R.id.fab_Spectacle);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateSpectacleActivity = new Intent(ModifySpectacle.this,CreateSpectacle.class);
                startActivity(startCreateSpectacleActivity);
            }
        });


        recyclerSpectacles = findViewById(R.id.recyclerview_cloud_spectacle);
        layoutManager = new LinearLayoutManager(ModifySpectacle.this);
        recyclerSpectacles.setLayoutManager(layoutManager);
        recyclerSpectacles.setHasFixedSize(true);

        db=FirebaseFirestore.getInstance();
        getListSpectacles();
    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleChorale");
        idChorale=args.getString("idChorale");
        nomChoraleStr=args.getString("nomChorale");
        origine=args.getString("origine");
        //origine = intent.getStringExtra("origine");
        Log.d(TAG, "MSp getIntentBundle: "+idChorale+" "+nomChoraleStr+" "+origine);
    }

    private void getListSpectacles() {
        try {
            db.collection("chorale").document(idChorale).collection("spectacles")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, "MSp onComplete: spectacles " + Thread.currentThread().getName());
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "MSp-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));

                                    String nom,idDocument;
                                    Date maj;
                                    Timestamp majSS;
                                    ArrayList<String>  idTitres, lieuxConcerts;
                                    ArrayList<Timestamp> timeStampsConcerts;
                                    ArrayList<Date> datesConcerts = new ArrayList<>();

                                    idDocument = document.getId();
                                    listId.add(idDocument);

                                    nom = (String) document.getData().get("nom");
                                    majSS= (Timestamp) document.getData().get("maj");
                                    maj = majSS.toDate();
                                    idTitres =(ArrayList<String>) document.getData().get("id_titres");
                                    lieuxConcerts =(ArrayList<String>) document.getData().get("concerts_lieux");
                                    timeStampsConcerts=(ArrayList<Timestamp>) document.getData().get("concerts_dates");

                                    for(Timestamp timestamp:timeStampsConcerts){
                                        datesConcerts.add(timestamp.toDate());
                                    }

                                    Log.d(TAG, "MSp-exec onComplete:A Spectacles " + nom + " " + idTitres+" "+lieuxConcerts+" "+maj);
                                    Spectacle spectacle = new Spectacle(idDocument,nom,idTitres,lieuxConcerts,datesConcerts,maj);
                                    listSpectacles.add(spectacle);
                                }

                                //todo voir l'intêtert du new Intent non mis en place à voir aussi sur le modify chorale
                                if(newIntent){
                                    Log.d(TAG, "MSp onComplete: new intent");
                                    spectacleAdapter.notifyDataSetChanged();

                                }else{
                                    Log.d(TAG, "MSp onComplete: pas de new intent");
                                    spectacleAdapter = new SpectacleAdapter(listSpectacles);
                                    recyclerSpectacles.setAdapter(spectacleAdapter);
                                }

                                Log.d(TAG, "MSp fetchSourceSongs: après fetch");
                            } else {
                                Log.d(TAG, "MSS-exec Error getting documents.", task.getException());
                            }
                        }
                    });

        } catch (Exception e) {
            // Server probably invalid
            e.printStackTrace();
        }
    }

    private void getTitresSong(ArrayList<String> listTitres) {
        entier=0;
        for(String idTitre:listTitres){

            db.collection("sourceSongs").document(idTitre)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            entier++;
                            Log.d(TAG, "MSp onComplete: réussi getTitresSong "+listTitres.lastIndexOf(idTitre)+"/"+listTitres.size());

                            if(Objects.requireNonNull(task.getResult()).exists()){
                                String name = (String) task.getResult().get("titre");
                                oldTitresNameStr.add(name);
                                Log.d(TAG, "MSp getTitresSong: réussi "+name);

                                if(entier ==listTitres.size()){
                                    Log.d(TAG, "MSp getTitresSong: if"+oldTitresNameStr);
                                    args.putStringArrayList("OldTitresNames",oldTitresNameStr);
                                    startDetailsSpectaclesActivity.putExtra("bundleSpectacle", args);
                                    startActivity(startDetailsSpectaclesActivity);
                                }
                            }

                        }else{
                            Log.d(TAG, "MSp onComplete: failure getTitresSong ");
                        }

                    }).addOnFailureListener(e -> Log.d(TAG, "MSpD onFailure: getTitresSongs"));
        }
    }

    @Override
    public void onItemClick(int i) {
        if (origine.equals("ChooseChorale")) {
            Log.d(TAG, "MSp onItemClick: A "+i);
            startDetailsSpectaclesActivity = new Intent(ModifySpectacle.this, ModifySpectaclesDetails.class);
            args.putString("idSpectacle", listId.get(i));
            args.putString("oldNom", listSpectacles.get(i).getSpectacleName());
            args.putStringArrayList("oldLieux", listSpectacles.get(i).getSpectacleLieux());
            args.putString("idChorale",idChorale);
            ArrayList<String> datesLongStr = new ArrayList<String>();
            for(Date date :listSpectacles.get(i).getSpectacleDates()){
               datesLongStr.add(String.valueOf(date.getTime()));
            }
            args.putStringArrayList("datesLong",datesLongStr);

            args.putStringArrayList("oldTitres",listSpectacles.get(i).getIdTitresSongs());

            getTitresSong(listSpectacles.get(i).getIdTitresSongs());
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
