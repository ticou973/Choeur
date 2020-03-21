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
import dedicace.com.data.database.Choriste;
import dedicace.com.data.database.Pupitre;
import dedicace.com.utilities.SongsUtilities;

public class ModifyChoriste extends AppCompatActivity implements ChoristeModifAdapter.clickedListener{
    private RecyclerView recyclerChoriste;
    private ChoristeModifAdapter choristeModifAdapter;
    private List<Choriste> listChoristes = new ArrayList<>();
    private List<String> listId = new ArrayList<>();
    private static final String TAG ="coucou";
    private FirebaseFirestore db;
    private String origine, idChorale, currentSaison, nomChoraleStr;
    private List<String> idSpectacles= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_choriste);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getIntentBundle();

        FloatingActionButton fab = findViewById(R.id.fab_choriste_modify);

        fab.setOnClickListener(view -> {
            Intent startCreateSSActivity = new Intent(ModifyChoriste.this,CreateChoristeMode.class);
            startActivity(startCreateSSActivity);
        });

        recyclerChoriste = findViewById(R.id.recyclerview_cloud_choriste);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ModifyChoriste.this);
        recyclerChoriste.setLayoutManager(layoutManager);
        recyclerChoriste.setHasFixedSize(true);

        db=FirebaseFirestore.getInstance();

        getListChoristes();

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

    private void getListChoristes() {
        try{
            db.collection("chorale").document(idChorale).collection("choristes")
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : Objects.requireNonNull((task.getResult()))){
                                Log.d(TAG, "Mch-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));

                                String idDocument = document.getId();
                                List<String> adresse = (List<String>) document.getData().get("adresse");
                                String adresseConcat = adresse.get(0)+ " "+adresse.get(1)+" "+adresse.get(2);
                                String email = (String) document.getData().get("email");
                                Timestamp majTs = (Timestamp) document.getData().get("maj");
                                Date maj = Objects.requireNonNull(majTs).toDate();
                                String nomChoriste = (String) document.getData().get("nom_choriste");
                                String prenomChoriste = (String) document.getData().get("prenom_choriste");
                                String pupitre = (String) document.getData().get("pupitre");
                                Pupitre pupitre1 = SongsUtilities.converttoPupitre(pupitre);
                                String roleAdmin = (String) document.getData().get("role_admin");
                                String roleChoeur = (String) document.getData().get("role_choeur");
                                String telFixe = (String) document.getData().get("tel_fixe");
                                String telPort = (String) document.getData().get("tel_port");
                                String urlPhoto = (String) document.getData().get("url_photo");

                                Log.d(TAG, "MCh getListChoristes: "+idDocument+" "+adresse+" "+email+" "+ maj+" "+ nomChoriste+" "+prenomChoriste+" "+pupitre+" "+roleAdmin+" "+roleChoeur+" "+telFixe+" "+telPort+ " "+urlPhoto);
                                Choriste choriste = new Choriste(idDocument,idChorale,nomChoriste,prenomChoriste,pupitre1,adresseConcat,telFixe,telPort,email,roleChoeur,roleAdmin,urlPhoto,maj);

                                listChoristes.add(choriste);
                            }
                            Log.d(TAG, "MCh onComplete: pas de new intent");
                            choristeModifAdapter = new ChoristeModifAdapter(this,listChoristes);
                            recyclerChoriste.setAdapter(choristeModifAdapter);

                            Log.d(TAG, "MSS fetchSourceSongs: après fetch");

                        }else{
                            Log.d(TAG, "MCh-exec Error getting documents.", task.getException());
                        }

                    });

        }catch (Exception e){
            // Server probably invalid
            e.printStackTrace();
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

    @Override
    public void onItemClick(int i) {
        if (origine.equals("AdminHome")||origine.equals("ChoristeModifyDetails")) {
            Log.d(TAG, "MCh onItemClick: A "+i);
            Intent startDetailsChoristesActivity = new Intent(ModifyChoriste.this, ChoristeModifyDetails.class);
            Bundle args = new Bundle();
            args.putString("nom_choriste",listChoristes.get(i).getNom());
            args.putString("prenom_choriste",listChoristes.get(i).getPrenom());
            args.putString("pupitre",listChoristes.get(i).getPupitre().toString());
            args.putString("role_choeur",listChoristes.get(i).getRoleChoeur());
            args.putString("role_admin",listChoristes.get(i).getRoleAdmin());
            args.putString("email",listChoristes.get(i).getEmail());
            args.putString("adresse",listChoristes.get(i).getAdresse());
            args.putString("tel_fixe",listChoristes.get(i).getFixTel());
            args.putString("tel_port",listChoristes.get(i).getPortTel());
            args.putString("url_photo",listChoristes.get(i).getUrlLocalPhoto());
            args.putString("idChoriste",listChoristes.get(i).getIdChoristeCloud());
            args.putString("idChorale",idChorale);
            startDetailsChoristesActivity.putExtra("bundleChoriste", args);

            startActivity(startDetailsChoristesActivity);
        }
    }
}
