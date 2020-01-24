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

import dedicace.com.R;

public class ModifyChorale extends AppCompatActivity implements ChoraleAdapter.OnItemListener{

    private RecyclerView recyclerChorale;
    private FloatingActionButton fab;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseFirestore db;
    private static final String TAG ="coucou";
    private List<String> listId = new ArrayList<>();
    private ChoraleAdapter choraleAdapter;
    private String origine;
    private boolean newIntent = false;
    private List<String> listChorales = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_chorale);
        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        origine = intent.getStringExtra("origine");

        //todo voir l'utilité de new Intent ?

       /* if(origine.equals("ModifyChoraleDetails")){
            newIntent=true;
        }*/

        fab = findViewById(R.id.fab_chorale);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateChoraleActivity = new Intent(ModifyChorale.this,CreateChorale.class);
                startActivity(startCreateChoraleActivity);
            }
        });
        recyclerChorale = findViewById(R.id.recyclerview_cloud_chorale);
        layoutManager = new LinearLayoutManager(ModifyChorale.this);
        recyclerChorale.setLayoutManager(layoutManager);
        recyclerChorale.setHasFixedSize(true);

        db= FirebaseFirestore.getInstance();

        getListChorales();
    }

    private void getListChorales() {
        try{
          db.collection("chorale")
                  .get()
                  .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                      @Override
                      public void onComplete(@NonNull Task<QuerySnapshot> task) {
                          if(task.isSuccessful()){
                              Log.d(TAG, "MC onComplete: ");
                              for (QueryDocumentSnapshot document : task.getResult()) {
                                  Log.d(TAG, "MC-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));

                                  String nom, urlCloudLogo,idDocument;
                                  Date maj;
                                  Timestamp majChorale;

                                  idDocument = document.getId();
                                  listId.add(idDocument);

                                  nom = (String) document.getData().get("nom");
                                  urlCloudLogo = (String) document.getData().get("logo");
                                  majChorale= (Timestamp) document.getData().get("maj");
                                  maj = majChorale.toDate();

                                  Log.d(TAG, "MC-exec onComplete:A SourceSongs " + nom + " " + urlCloudLogo + " " + urlCloudLogo + " " + maj);
                                  listChorales.add(nom);
                              }
                              if(newIntent){
                                  Log.d(TAG, "MC onComplete: new intent");
                                  choraleAdapter.notifyDataSetChanged();

                              }else{
                                  Log.d(TAG, "MC onComplete: pas de new intent");
                                  choraleAdapter = new ChoraleAdapter(listChorales);
                                  recyclerChorale.setAdapter(choraleAdapter);
                              }

                              Log.d(TAG, "MC fetchSourceSongs: après fetch");

                          }else {
                              Log.d(TAG, "MC Error getting documents.", task.getException());
                          }
                      }
                  });

        }catch (Exception e) {
            // Server probably invalid
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int i) {

        if (origine.equals("AdminHome")||origine.equals("ModifyChoraleDetails")) {
            Log.d(TAG, "MC onItemClick: "+i+" "+origine);
            Intent startDetailsChoraleActivity = new Intent(ModifyChorale.this, ModifyChoraleDetails.class);
            Bundle args = new Bundle();
            args.putString("idChorale", listId.get(i));
            args.putString("oldName", listChorales.get(i));
            startDetailsChoraleActivity.putExtra("bundleChorale", args);
            startActivity(startDetailsChoraleActivity);

        }else if(origine.equals("CreateUser")||origine.equals("CreateSpectacle")||origine.equals("ChooseChorale")||origine.equals("CreateSaison")||origine.equals("ModifySSDetails")||origine.equals("CreateSong")){
            Log.d(TAG, "MC onItemClick: 1"+i+ " "+origine);
            Intent result = new Intent();
            result.putExtra("idselected",listId.get(i));
            result.putExtra("nomChorale",listChorales.get(i));
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
