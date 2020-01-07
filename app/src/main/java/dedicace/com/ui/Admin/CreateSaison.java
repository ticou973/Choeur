package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedicace.com.R;

public class CreateSaison extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener{

    private Button addSpectacles, createSaisonInDb, selectChorale;
    private EditText nomSaison;
    private TextView nomChorale, listSpectacles;
    private String nomSaisonStr, idChorale, nomChoraleStr, nomSpectacle, idSpectacle;
    private List<String> nomsSpectacles = new ArrayList<>();
    private List<String> listIds = new ArrayList<>();
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;
    private static final int REQUEST_CODE_C = 300;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_saison);

        Log.d(TAG, "CSa onCreate: arrivée");

        addSpectacles = findViewById(R.id.btn_add_spectacles_saison);
        selectChorale = findViewById(R.id.btn_select_chorale_saison);
        createSaisonInDb = findViewById(R.id.btn_create_saison_db);
        nomSaison = findViewById(R.id.et_nom_saison);
        nomChorale=findViewById(R.id.et_select_chorale_saison);
        listSpectacles = findViewById(R.id.tv_list_noms_spectacle);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Log.d(TAG, "CSa onCreate: ");

        db = FirebaseFirestore.getInstance();

        createSaisonInDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nomChoraleStr = nomChorale.getText().toString();
                nomSaisonStr =nomSaison.getText().toString();

                Log.d(TAG, "CSa Click: create in DB "+ nomSaisonStr+" "+nomChoraleStr+ " "+idChorale+" "+nomsSpectacles+ " "+listIds);
                if(!TextUtils.isEmpty(nomSaisonStr)&&!TextUtils.isEmpty(nomChoraleStr)&&listIds!=null&&listIds.size()!=0){

                    Map<String,Object> saison = new HashMap<>();
                    saison.put("nom",nomSaisonStr);

                    saison.put("spectacles", listIds);
                    saison.put("maj", Timestamp.now());

                    db.collection("chorale").document(idChorale).collection("saisons")
                            .add(saison)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "CS DocumentSnapshot added with ID: " + documentReference.getId());
                                    newSaison();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "CSa Error adding document", e);
                                }
                            });
                }else {
                    Log.d(TAG, "CSa onClick: else create in Db ");
                    Toast.makeText(CreateSaison.this, "tout n'est pas rempli !", Toast.LENGTH_SHORT).show();
                }

            }
        });

        addSpectacles.setOnClickListener(view -> {
            Log.d(TAG, "CSa onClick: add Spectacles");
            Intent startModifySpActivity = new Intent(CreateSaison.this,ModifySpectacle.class);
            Bundle args = new Bundle();
            args.putString("idChorale",idChorale);
            args.putString("nomChorale",nomChoraleStr);
            args.putString("origine","CreateSaison");
            startModifySpActivity.putExtra("bundleChorale",args);
            startActivityForResult(startModifySpActivity,REQUEST_CODE_B);
        });

        selectChorale.setOnClickListener(view -> {
            Intent startModifySSActivity = new Intent(CreateSaison.this,ModifyChorale.class);
            startModifySSActivity.putExtra("origine","CreateSaison");
            startActivityForResult(startModifySSActivity,REQUEST_CODE_C);
        });


    }

    private void newSaison() {
        DialogFragment dialog = new DialogNewSSFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
    }

    private void modifyMajChorale() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj", Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "CSS onSuccess: maj chorale done");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "CSS onSuccess: maj chorale failed");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "CSa onActivityResult: "+ requestCode+ " "+resultCode+" "+data);

        if(resultCode== Activity.RESULT_OK) {

            if (requestCode == REQUEST_CODE_B) {

                Log.d(TAG, "CSa onActivityResult: request_codeB");
                if (data != null) {
                    idSpectacle=data.getStringExtra("idselected");
                    nomSpectacle=data.getStringExtra("nomselected");

                }
                Log.d(TAG, "CSa onActivityResult: request_codeB "+idSpectacle+" "+nomSpectacle);
                nomsSpectacles.add(nomSpectacle);
                listIds.add(idSpectacle);

                StringBuilder sb = new StringBuilder(" ");
                String newLine = System.getProperty("line.separator");

                int i=0;
                for(String nom:nomsSpectacles){
                    i++;
                    String listNoms = i+". "+nom+newLine;
                    sb.append(listNoms+newLine);
                }

                Log.d(TAG, "CSa onActivityResult: "+ sb.toString() );

                listSpectacles.setText(sb.toString());
            }else if(requestCode==REQUEST_CODE_C){

                if (data != null) {
                    idChorale = data.getStringExtra("idselected");
                    nomChoraleStr=data.getStringExtra("nomChorale");
                }
                nomChorale.setText(nomChoraleStr);
            }

        }else{

            Log.d(TAG, "CSa onActivityResult: petit problème au retour "+resultCode);
        }
    }

    @Override
    public void onDialogPositiveClick() {
        nomChorale.setText("");
        nomSaison.setText("");
        listSpectacles.setText("");
        nomsSpectacles.clear();
        listIds.clear();
    }

    @Override
    public void onDialogNegativeClick() {
        finish();
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
