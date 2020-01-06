package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
    private String oldNomStr, oldIdSpectacleStr, newNomStr;
    private ArrayList<String> oldLieuxStr,oldTitreStr,olddatesLongStr,newLieuxStr,newTitreStr,newdatesLongStr;
    private ArrayList<String> oldTitresNameStr,newTitresNameStr;
    private TextView oldNom, oldLieux, oldDates, oldTitres, newLieux, newDates, newTitres;
    private EditText newNom;
    private Button modifTitres, modifConcerts, suppSpectacle, modifSpectacle;
    private Map<String,Object> spectacle;

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
        newLieux = findViewById(R.id.tv_modif_spectacle_new_lieux2);
        newDates = findViewById(R.id.tv_modif_spectacle_new_dates2);
        newTitres = findViewById(R.id.tv_modif_spectacle_new_titres2);
        newNom = findViewById(R.id.et_modif_spectacle_new_nom);
        modifTitres = findViewById(R.id.btn_modif_spectacle_modif_titres);
        modifConcerts = findViewById(R.id.btn_modif_spectacle_modif_concerts);
        suppSpectacle = findViewById(R.id.btn_supp_spectacle);
        modifSpectacle = findViewById(R.id.btn_modify_spectacle);

        db = FirebaseFirestore.getInstance();

        clearLists();

        getIntentBundle();

        completeOld();

        suppSpectacle.setOnClickListener(view -> {
            DialogFragment dialogFragment = new DialogSuppFragment();
            dialogFragment.show(getSupportFragmentManager(),TAG);
        });

        modifTitres.setOnClickListener(view -> {
            Intent startActivityList = new Intent(ModifySpectaclesDetails.this,GenList.class);
            if(newTitreStr!=null&&newTitreStr.size()!=0){
                startActivityList.putStringArrayListExtra("titres", newTitreStr);
                startActivityList.putStringArrayListExtra("oldTitreNames", newTitresNameStr);

            }else {
                startActivityList.putStringArrayListExtra("titres", oldTitreStr);
                startActivityList.putStringArrayListExtra("oldTitreNames", oldTitresNameStr);
            }
            startActivityList.putExtra("origine", "modifTitres");
            startActivityForResult(startActivityList,REQUEST_CODE);
        });

        modifConcerts.setOnClickListener(view -> {
            Intent startActivityList = new Intent(ModifySpectaclesDetails.this,GenList.class);
            if(newLieuxStr!=null&&newLieuxStr.size()!=0){
                startActivityList.putStringArrayListExtra("lieux", newLieuxStr);
                startActivityList.putStringArrayListExtra("datesLong", newdatesLongStr);

            }else {
                startActivityList.putStringArrayListExtra("lieux", oldLieuxStr);
                startActivityList.putStringArrayListExtra("datesLong", olddatesLongStr);
            }

            startActivityList.putExtra("origine","modifConcerts");
            startActivityForResult(startActivityList,REQUEST_CODEB);
        });

        modifSpectacle.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                newNomStr = newNom.getText().toString();
                if(!TextUtils.isEmpty(newNomStr)||(newLieuxStr!=null&&newLieuxStr.size()!=0)||(newTitreStr!=null&&newTitreStr.size()!=0)){
                    spectacle = new HashMap<>();
                    spectacle.put("maj",Timestamp.now());

                    if(!TextUtils.isEmpty(newNomStr)){
                        Log.d(TAG, "MSpD onClick: putNom");
                        spectacle.put("nom",newNomStr);
                    }

                    if(newLieuxStr!=null&&newLieuxStr.size()!=0){
                        spectacle.put("concerts_lieux",newLieuxStr);

                        ArrayList<Date> dates = new ArrayList<>();

                        for (String dateLongStr:newdatesLongStr){
                            Log.d(TAG, "MSpD onClick: newDates "+dateLongStr);
                            dates.add(new Date(Long.parseLong(dateLongStr)));
                        }
                        Log.d(TAG, "MSpD onClick: putLieux et dates ");
                        spectacle.put("concerts_dates",dates);
                    }

                    if(newTitreStr!=null&&newTitreStr.size()!=0){
                        Log.d(TAG, "MSpD onClick: puttitre");
                        spectacle.put("id_titres",newTitreStr);
                    }

                    insertSpectacleInDb();
                }

            }
        });
    }

    private void insertSpectacleInDb() {
        Log.d(TAG, "MSpD insertSpectacleInDb: ");
        db.collection("chorale").document(idChorale).collection("spectacles").document(oldIdSpectacleStr)
                .update(spectacle)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "MSpD onSuccess: OK");
                        modifyMajChorale();
                        Intent startMSp = new Intent(ModifySpectaclesDetails.this,ChooseChorale.class);
                        startMSp.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(startMSp);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "MSpD onSuccess: maj spectacle failed");
            }
        });
    }

    private void clearLists() {
        if(oldTitresNameStr!=null){
            oldTitresNameStr.clear();
        }
        if(oldTitreStr!=null){
            oldTitreStr.clear();
        }
        if(olddatesLongStr!=null){
            olddatesLongStr.clear();
        }
        if(oldLieuxStr!=null){
            oldLieuxStr.clear();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==REQUEST_CODE){
                if (data != null) {
                    newTitreStr=data.getStringArrayListExtra("listGenModif");
                    newTitresNameStr=data.getStringArrayListExtra("listGenCompModif");

                    StringBuilder sb = new StringBuilder(" ");
                    String newLine = System.getProperty("line.separator");
                    int i=0;
                    for(String titre:newTitresNameStr){
                        i++;
                        String listTitres = i+". "+titre+newLine;
                        sb.append(listTitres+newLine);
                    }
                    newTitres.setText(sb.toString());
                }

            }else if(requestCode==REQUEST_CODEB){
                if (data != null) {
                    newLieuxStr=data.getStringArrayListExtra("listGenCompModif");
                    newdatesLongStr=data.getStringArrayListExtra("listGenModif");

                    StringBuilder sb = new StringBuilder(" ");
                    String newLine = System.getProperty("line.separator");
                    int i=0;
                    for(String lieu:newLieuxStr){
                        i++;
                        String listLieu = i+". "+lieu+newLine;
                        sb.append(listLieu+newLine);
                    }
                    newLieux.setText(sb.toString());

                    StringBuilder sb1 = new StringBuilder(" ");
                    int j=0;
                    for(String date:newdatesLongStr){
                        long dateLong = Long.parseLong(date);
                        Date date1 = new Date(dateLong);
                        j++;
                        String listTitres = j+". "+date1.toString()+newLine;
                        sb1.append(listTitres+newLine);
                    }
                    newDates.setText(sb1.toString());

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
        for(String titre:oldTitresNameStr){
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
            String listLieux = j+". "+lieu+newLine;
            sb1.append(listLieux+newLine);
        }

        Log.d(TAG, "MspD completed Old: lieux"+ sb1.toString() );
        oldLieux.setText(sb1.toString());

        StringBuilder sb2 = new StringBuilder(" ");
        int k=0;
        for(String dateLongStr:olddatesLongStr){
            Log.d(TAG, "MSpD completeOld: "+dateLongStr);
            k++;
            long dateLong = Long.parseLong(dateLongStr);
            Date date = new Date(dateLong);

            Log.d(TAG, "MSpD completeOld: "+date);
            String listDates = k+". "+date.toString().substring(0,20)+newLine;
            sb2.append(listDates+newLine);
        }

        Log.d(TAG, "MspD completed Old: dates"+ sb2.subSequence(0,24).toString() );
        oldDates.setText(sb2.toString());
    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleSpectacle");
        oldNomStr=args.getString("oldNom");
        oldIdSpectacleStr = args.getString("idSpectacle");
        oldLieuxStr = args.getStringArrayList("oldLieux");
        olddatesLongStr =args.getStringArrayList("datesLong");
        idChorale = args.getString("idChorale");
        oldTitreStr = args.getStringArrayList("oldTitres");
        oldTitresNameStr=args.getStringArrayList("OldTitresNames");
        Log.d(TAG, "MSpD getIntentBundle: "+oldIdSpectacleStr+" "+oldNomStr+" "+oldTitreStr+" "+oldLieuxStr+" "+olddatesLongStr+" "+idChorale+" "+oldTitresNameStr);
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
                                    Log.d(TAG, "MSpD onComplete: spectacles in saison réussi ");
                                    Toast.makeText(ModifySpectaclesDetails.this, "Réussi saison", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ModifySpectaclesDetails.this, "Réussi spectacle", Toast.LENGTH_SHORT).show();

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
