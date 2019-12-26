package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dedicace.com.R;

public class ModifySpectaclesDetails extends AppCompatActivity implements DialogSuppFragment.DialogSuppListener{
    private String oldNomStr, oldIdSpectacleStr;
    private ArrayList<String> oldLieuxStr,oldTitreStr,olddatesLongStr;
    private TextView oldNom, oldLieux, oldDates, oldTitres, newLieux, newDates, newTitres;
    private EditText newNom;
    private Button modifTitres, modifConcerts, suppSpectacle, modifSpectacle;

    private FirebaseFirestore db;


    private static final String TAG ="coucou";
    private String idChorale;
    private ArrayList<String> listIdSpectacles = new ArrayList<>();
    private ArrayList<String> listIdSaisons = new ArrayList<>();
    private final static int REQUEST_CODE=100;
    private final static int REQUEST_CODEB=200;
    private final static int REQUEST_CODEC=300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_spectacles_details);
        Log.d("coucou", "MSpD onCreate: bien arrivé");


        oldNom = findViewById(R.id.tv_modif_spectacle_old_nom);
        oldLieux = findViewById(R.id.tv_modif_spectacle_old_lieux);
        oldDates = findViewById(R.id.tv_modif_spectacles_old_dates);
        oldTitres = findViewById(R.id.tv_modif_spectacle_old_titres);
        newLieux = findViewById(R.id.tv_modif_spectacle_new_lieux);
        newDates = findViewById(R.id.tv_modif_spectacle_new_dates);
        newTitres = findViewById(R.id.tv_modif_spectacle_new_titres);
        newNom = findViewById(R.id.et_modif_spectacle_new_nom);
        modifTitres = findViewById(R.id.btn_modif_spectacle_modif_titres);
        modifConcerts = findViewById(R.id.btn_modif_spectacle_modif_concerts);
        suppSpectacle = findViewById(R.id.btn_supp_spectacle);
        modifSpectacle = findViewById(R.id.btn_modify_spectacle);

        db = FirebaseFirestore.getInstance();

        getIntentBundle();

        completeOld();

        suppSpectacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new DialogSuppFragment();
                dialogFragment.show(getSupportFragmentManager(),TAG);
            }
        });

        modifTitres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startActivityList = new Intent(ModifySpectaclesDetails.this,GenList.class);
                startActivityList.putStringArrayListExtra("titres",oldTitreStr);
                startActivityForResult(startActivityList,REQUEST_CODE);
            }
        });

        modifConcerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startActivityList = new Intent(ModifySpectaclesDetails.this,GenList.class);
                startActivityList.putStringArrayListExtra("lieux",oldLieuxStr);
                startActivityList.putStringArrayListExtra("datesLong",olddatesLongStr);
                startActivityForResult(startActivityList,REQUEST_CODEB);
            }
        });

        modifSpectacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==REQUEST_CODE){
                if (data != null) {

                }

            }else if(requestCode==REQUEST_CODEB){
                if (data != null) {

                }
            }else if(requestCode==REQUEST_CODEC){
                if (data != null) {

                }
            }
        }
    }

    private void completeOld() {
        oldNom.setText(oldNomStr);

        StringBuilder sb = new StringBuilder(" ");
        String newLine = System.getProperty("line.separator");
        int i=0;
        for(String titre:oldTitreStr){
            i++;
            String listTitres = i+". "+titre+newLine;
            sb.append(listTitres+newLine);
        }

        Log.d(TAG, "MspD completed Old: titres"+ sb.toString() );
        oldTitres.setText(sb.toString());

        StringBuilder sb1 = new StringBuilder(" ");
        int j=0;
        for(String lieu:oldLieuxStr){
            j++;
            String listLieux = i+". "+lieu+newLine;
            sb1.append(listLieux+newLine);
        }

        Log.d(TAG, "MspD completed Old: lieux"+ sb.toString() );
        oldLieux.setText(sb1.toString());

        StringBuilder sb2 = new StringBuilder(" ");
        int k=0;
        for(String dateLongStr:oldLieuxStr){
            k++;
            long dateLong = Long.parseLong(dateLongStr);
            Date date = new Date(dateLong);

            String listDates = i+". "+date.toString()+newLine;
            sb2.append(listDates+newLine);
        }

        Log.d(TAG, "MspD completed Old: lieux"+ sb.toString() );
        oldDates.setText(sb2.toString());
    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleSpectacle");
        oldNomStr=args.getString("oldNom");
        oldIdSpectacleStr = args.getString("idSpectacle");
        oldLieuxStr = args.getStringArrayList("oldLieux");
        oldTitreStr = args.getStringArrayList("oldTitres");
        olddatesLongStr =args.getStringArrayList("datesLong");
        idChorale = args.getString("idChorale");
        Log.d(TAG, "MSpD getIntentBundle: "+oldIdSpectacleStr+" "+oldNomStr+" "+oldTitreStr+" "+oldLieuxStr+" "+olddatesLongStr);
    }

    @Override
    public void onDialogSuppPositiveClick() {
        SuppSpectaclesInSaisons();
        suppSpectacles();
        modifyMajChorale();
        Intent startModifySpectacleActivity = new Intent(ModifySpectaclesDetails.this,ChooseChorale.class);
        startModifySpectacleActivity.putExtra("origine","AdminHome");
        startModifySpectacleActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(startModifySpectacleActivity);
    }

    private void SuppSpectaclesInSaisons() {
        CollectionReference saisonRef = db.collection("chorale").document(idChorale).collection("saisons");

        saisonRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                    Map<String,Object> updates = new HashMap<>();
                    updates.put("spectacles", FieldValue.arrayRemove(oldIdSpectacleStr));
                    updates.put("maj", Timestamp.now());

                    DocumentReference saisonRefSpec = saisonRef.document(document.getId());

                    saisonRefSpec.update(updates)
                            .addOnCompleteListener(task1 -> {

                                if(task1.isSuccessful()) {
                                    Log.d(TAG, "MSpD onComplete: réussi ");
                                    Toast.makeText(ModifySpectaclesDetails.this, "Réussi ", Toast.LENGTH_SHORT).show();
                                }else{
                                    Log.d(TAG, "MSp onComplete: pb dans le onComplete");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "MSp onFailure: pb sur update");
                        }
                    });
                }

            }else{
                Log.d(TAG, "MSpD SuppSpectaclesInSaisons: help pb sur doc saisons ");
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "MSpD onFailure: pas de correspondance");
            }
        });
    }

    private void suppSpectacles() {
        CollectionReference spectacleRef = db.collection("chorale").document(idChorale).collection("spectacles");
        spectacleRef.document(oldIdSpectacleStr)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "MSpD OnSucess: suppSpectacles");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "MSpD OnFailure: suppSpectacles");
            }
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
                        Log.d(TAG, "MSpD onSuccess: maj chorale done");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MSSD onSuccess: maj chorale failed");
                    }
                });
    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer cette Source Song", Toast.LENGTH_SHORT).show();
        finish();
    }
}
