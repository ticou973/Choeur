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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedicace.com.R;

public class CreateSpectacle extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener{

    private Button addTitres, addConcerts, createSpectacleInDb, selectChorale;
    private EditText nomSpectacle, nomChorale;
    private TextView listTitres;
    private String nomSpectacleStr, idChorale, titreSong, idSong, nomChoraleStr;
    private ArrayList<String> lieux = new ArrayList();
    private ArrayList<Date> dates = new ArrayList<>();
    private List<String> titres = new ArrayList<>();
    private List<String> listIds = new ArrayList<>();
    private List<String> datesStr = new ArrayList<>();
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;
    private static final int REQUEST_CODE_C = 300;
    private static final int REQUEST_CODE_D = 400;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_spectacle);

        addTitres=findViewById(R.id.btn_add_titres);
        addConcerts=findViewById(R.id.btn_add_concerts);
        createSpectacleInDb=findViewById(R.id.btn_create_spectacle_db);
        nomSpectacle=findViewById(R.id.et_nom_spectacle);
        selectChorale=findViewById(R.id.btn_select_chorale_spectacle);
        nomChorale=findViewById(R.id.et_select_chorale_spectacle);
        listTitres=findViewById(R.id.tv_list_titres_spectacle);


        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Log.d(TAG, "CS onCreate: ");

        db = FirebaseFirestore.getInstance();

        createSpectacleInDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nomSpectacleStr = nomSpectacle.getText().toString();
                nomChoraleStr = nomChorale.getText().toString();

                Log.d(TAG, "CS Click: create in DB "+ nomSpectacleStr+" "+nomChoraleStr+ " "+idChorale+" "+titres+ " "+listIds+" "+ lieux+" "+dates);
                if(!TextUtils.isEmpty(nomSpectacleStr)&&!TextUtils.isEmpty(nomChoraleStr)&&listIds!=null&&listIds.size()!=0){

                    Map<String,Object> spectacle = new HashMap<>();
                    spectacle.put("nom",nomSpectacleStr);
                    if(lieux==null||lieux.size()==0) {
                        lieux.add("");
                    }
                    if(dates==null||dates.size()==0) {
                        dates.add(null);
                    }

                    spectacle.put("concerts_lieux", lieux);
                    spectacle.put("concerts_dates",dates);
                    spectacle.put("id_titres",listIds);
                    spectacle.put("maj", Timestamp.now());

                    db.collection("chorale").document(idChorale).collection("spectacles")
                            .add(spectacle)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "CS DocumentSnapshot added with ID: " + documentReference.getId());
                                    newSpectacle();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "CS Error adding document", e);
                                }
                            });
                }else {
                    Log.d(TAG, "CS onClick: else create in Db ");
                }
            }
        });

        addTitres.setOnClickListener(view -> {
            Log.d(TAG, "CS onClick: ");
            Intent startModifySSActivity = new Intent(CreateSpectacle.this,ModifySourceSong.class);
            startModifySSActivity.putExtra("origine","CreateSpectacle");
            startActivityForResult(startModifySSActivity,REQUEST_CODE_B);
        });

        addConcerts.setOnClickListener(view -> {
            Intent startAddConcert= new Intent(CreateSpectacle.this,AddConcert.class);
            startAddConcert.putExtra("origine","CreateSpectacle");
            startActivityForResult(startAddConcert,REQUEST_CODE_D);
        });

        selectChorale.setOnClickListener(view -> {
            Intent startModifySSActivity = new Intent(CreateSpectacle.this,ModifyChorale.class);
            startModifySSActivity.putExtra("origine","CreateSpectacle");
            startActivityForResult(startModifySSActivity,REQUEST_CODE_C);
        });

    }


    private void modifyMajChorale() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj",Timestamp.now());

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

    private void newSpectacle() {
        DialogFragment dialog = new DialogNewSSFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "CS onActivityResult: "+ requestCode+ " "+resultCode+" "+data);

        if(resultCode== Activity.RESULT_OK) {

            if (requestCode == REQUEST_CODE_B) {

                Log.d(TAG, "CSP onActivityResult: request_codeB");
                if (data != null) {
                    titreSong = data.getStringExtra("titreselected");
                    idSong = data.getStringExtra("idselected");

                }
                Log.d(TAG, "CS onActivityResult: request_codeB " + titreSong+" "+idSong);
                titres.add(titreSong);
                listIds.add(idSong);

                StringBuilder sb = new StringBuilder(" ");
                String newLine = System.getProperty("line.separator");

                int i=0;
                for(String titre:titres){
                    i++;

                    String listTitres = i+". "+titre+newLine;
                    sb.append(listTitres+newLine);

                }

                Log.d(TAG, "CSP onActivityResult: "+ sb.toString() );

                listTitres.setText(sb.toString());
            }else if(requestCode==REQUEST_CODE_C){

                if (data != null) {
                    idChorale = data.getStringExtra("idselected");
                    nomChoraleStr=data.getStringExtra("nomChorale");
                }

                nomChorale.setText(nomChoraleStr);
            }else if(requestCode==REQUEST_CODE_D){

                if(data!=null){

                    lieux = data.getStringArrayListExtra("lieuxconcerts");
                    datesStr = data.getStringArrayListExtra("datesconcerts");

                    for (String dateStr: datesStr){

                        dates.add(new Date(Long.parseLong(dateStr)));
                    }

                    Log.d(TAG, "CS onActivityResult: "+lieux+" "+ dates);
                }
            }

        }else{

            Log.d(TAG, "CSP onActivityResult: petit problème au retour "+resultCode);
        }
    }

    @Override
    public void onDialogPositiveClick() {
        nomChorale.setText("");
        nomSpectacle.setText("");
        listTitres.setText("");
        titres.clear();
        listIds.clear();
        lieux.clear();
        dates.clear();
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
