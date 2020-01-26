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
import dedicace.com.data.database.Saison;

public class ModifySaison extends AppCompatActivity implements SaisonAdapter.OnItemListener{

    private String idChorale,nomChoraleStr,origine;
    private static final String TAG ="coucou";
    private List<String> listId = new ArrayList<>();
    private List<Saison> listSaisons = new ArrayList<>();
    private ArrayList<String> oldNameStr = new ArrayList<>();
    private RecyclerView recyclerSaison;
    private SaisonAdapter saisonAdapter;
    private FirebaseFirestore db;
    private Intent startDetailsSaisonsActivity;
    private Bundle args= new Bundle();
    private int entier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_saison);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getIntentBundle();

        FloatingActionButton fab = findViewById(R.id.fab_Saison);

        fab.setOnClickListener(view -> {
            Intent startCreateSaisonActivity = new Intent(ModifySaison.this,CreateSaison.class);
            startActivity(startCreateSaisonActivity);
        });

        recyclerSaison = findViewById(R.id.recyclerview_cloud_saison);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ModifySaison.this);
        recyclerSaison.setLayoutManager(layoutManager);
        recyclerSaison.setHasFixedSize(true);

        db= FirebaseFirestore.getInstance();

        getListSaisons();

    }

    private void getListSaisons() {
        try {
            db.collection("chorale").document(idChorale).collection("saisons")
                    .get()
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "MSp getListSaisons: on Complete saison");
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())){
                                Log.d(TAG, "Msp getListSaisons: "+document.getId()+" "+document.getData().get("maj"));
                                String idDocument, nom;
                                Date maj;
                                Timestamp majSaison;
                                ArrayList<String> idSpectacles;

                                idDocument = document.getId();
                                listId.add(idDocument);

                                nom = (String) document.getData().get("nom");
                                majSaison = (Timestamp) document.getData().get("maj");
                                maj = Objects.requireNonNull(majSaison).toDate();
                                idSpectacles =(ArrayList<String>) document.getData().get("spectacles");

                                Log.d(TAG, "MSa-exec onComplete:A Spectacles " + nom + " " + idSpectacles+" "+maj);
                                Saison saison =new Saison(idDocument,nom,idSpectacles,maj);
                                listSaisons.add(saison);
                            }

                            Log.d(TAG, "MSp onComplete: pas de new intent ");

                            if(listSaisons!=null&&listSaisons.size()!=0) {
                                for(Saison saison:listSaisons) {
                                    Log.d(TAG, "MSp getListSaisons: "+saison.getSaisonName());
                                }
                            }else{
                                Log.d(TAG, "MSp getListSaisons: liste vide de saisons liste");
                            }

                            saisonAdapter = new SaisonAdapter(listSaisons);
                            recyclerSaison.setAdapter(saisonAdapter);

                        }else{
                            Log.d(TAG, "MSp Error getting documents.", task.getException());
                        }

                    }).addOnFailureListener(e -> Log.d(TAG, "MSa onFailure: pb getLisrSaisons"));


        }catch (Exception e) {
            // Server probably invalid
            e.printStackTrace();
        }
    }

    private void getNomsSpectacle(ArrayList<String> idSpectacles) {
        Log.d(TAG, "MS getNomsSpectacle: arrivée"+idSpectacles);


        if(idSpectacles!=null&&idSpectacles.size()!=0) {
            entier=0;
            //todo voir une méthode plus esthétique ?
            for (int i = 0; i < idSpectacles.size(); i++) {
                oldNameStr.add("A supprimer");
            }

            for (String idSpectacle : idSpectacles) {
                Log.d(TAG, "MS getNomsSpectacle: " + idSpectacle);
                db.collection("chorale").document(idChorale).collection("spectacles").document(idSpectacle)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                entier++;
                                Log.d(TAG, "MSa onComplete: réussi getNomSaison " + idSpectacles.lastIndexOf(idSpectacle) + "/" + idSpectacles.size());

                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                    String name = (String) task.getResult().get("nom");
                                    oldNameStr.add(idSpectacles.indexOf(idSpectacle), name);
                                    oldNameStr.remove("A supprimer");
                                    Log.d(TAG, "MSa getNomSaison: réussi " + name);

                                    if (entier == idSpectacles.size()) {
                                        Log.d(TAG, "MSa getNomSaison: if" + oldNameStr);
                                        args.putStringArrayList("OldSpectaclesNames", oldNameStr);
                                        startDetailsSaisonsActivity.putExtra("bundleSpectacle", args);
                                        startActivity(startDetailsSaisonsActivity);
                                    }
                                } else {
                                    Log.d(TAG, "Msa getNomsSpectacle:  pb de task result ");
                                }

                            } else {
                                Log.d(TAG, "MSp onComplete: failure getTitresSong ");
                            }

                        }).addOnFailureListener(e -> Log.d(TAG, "MSpD onFailure: getTitresSongs"));
            }
        }else{
            args.putStringArrayList("OldSpectaclesNames", null);
            startDetailsSaisonsActivity.putExtra("bundleSpectacle", args);
            startActivity(startDetailsSaisonsActivity);
        }
    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleChorale");
        idChorale=args.getString("idChorale");
        nomChoraleStr=args.getString("nomChorale");
        origine=args.getString("origine");

        Log.d(TAG, "MSa getIntentBundle: "+idChorale+" "+nomChoraleStr+" "+origine);
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
    public void onItemClick(int i) {
        if (origine.equals("ChooseChorale")) {
            Log.d(TAG, "MSa onItemClick: A "+i);
            startDetailsSaisonsActivity = new Intent(ModifySaison.this, ModifySaisonDetails.class);
            args.putString("idSaison", listId.get(i));
            args.putString("oldNom", listSaisons.get(i).getSaisonName());
            args.putString("idChorale",idChorale);
            args.putString("nomChorale",nomChoraleStr);
            args.putStringArrayList("oldSpectacles",listSaisons.get(i).getIdSpectacles());
            getNomsSpectacle(listSaisons.get(i).getIdSpectacles());
        }
    }
}
