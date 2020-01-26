package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dedicace.com.R;

public class ModifySaisonDetails extends AppCompatActivity implements DialogSuppFragment.DialogSuppListener{
    private String oldNomStr, newNomStr, oldIdSaisonStr, currentSaison;
    private ArrayList<String> oldSpectaclesStr, newSpectaclesStr,oldNamesSpectaclesStr, newNamesSpectaclesStr;
    private TextView oldNom, oldNameSpectacle, newNameSpectacle;
    private EditText newNom;
    private Map<String,Object> saison;

    private FirebaseFirestore db;
    private static final String TAG ="coucou";
    private String idChorale, nomChoraleStr;
    private final static int REQUEST_CODE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_saison_details);

        Log.d("coucou", "MSaD onCreate: bien arrivé");

        oldNom = findViewById(R.id.tv_modif_saison_old_nom);
        oldNameSpectacle = findViewById(R.id.tv_modif_saison_old_noms_spectacles);
        newNameSpectacle = findViewById(R.id.tv_modif_saison_new_nom_spectacles);
        newNom = findViewById(R.id.et_modif_saison_new_nom);
        Button modifSpectacles = findViewById(R.id.btn_modif_saison_modif_nom_spectacle);
        Button suppSaison = findViewById(R.id.btn_supp_saison);
        Button modifSaison = findViewById(R.id.btn_modify_saison);

        db = FirebaseFirestore.getInstance();

        clearLists();

        getIntentBundle();

        completeOld();

        verifyCurrentSaison();


        suppSaison.setOnClickListener(view -> {
            DialogFragment dialogFragment = new DialogSuppFragment();
            dialogFragment.show(getSupportFragmentManager(),TAG);
        });

        modifSpectacles.setOnClickListener(view -> {
            if(oldNamesSpectaclesStr!=null&&oldNamesSpectaclesStr.size()!=0) {
                Intent startActivityList = new Intent(ModifySaisonDetails.this, GenList.class);
                if (newSpectaclesStr != null && newSpectaclesStr.size() != 0) {
                    startActivityList.putStringArrayListExtra("nomsSpectacles", newSpectaclesStr);
                    startActivityList.putStringArrayListExtra("oldSpectaclesNames", newNamesSpectaclesStr);

                } else {
                    startActivityList.putStringArrayListExtra("nomsSpectacles", oldSpectaclesStr);
                    startActivityList.putStringArrayListExtra("oldSpectaclesNames", oldNamesSpectaclesStr);
                }
                Bundle args = new Bundle();
                args.putString("idChorale", idChorale);
                args.putString("nomChorale", nomChoraleStr);
                startActivityList.putExtra("bundleChorale", args);
                startActivityList.putExtra("origine", "modifNomsSaisons");
                startActivityForResult(startActivityList, REQUEST_CODE);
            }else{
                Log.d(TAG, "MSD onCreate: pas le droit");
                Toast.makeText(this, "Pas le droit car spectacles vide", Toast.LENGTH_SHORT).show();
            }
        });

        modifSaison.setOnClickListener(view -> {
            newNomStr = newNom.getText().toString();

            if(!TextUtils.isEmpty(newNomStr)||(newSpectaclesStr!=null&&newSpectaclesStr.size()!=0)){
                saison = new HashMap<>();
                saison.put("maj",Timestamp.now());

                if(!TextUtils.isEmpty(newNomStr)){
                    Log.d(TAG, "MSaD onClick: putNom");
                    saison.put("nom",newNomStr);
                }

                if(newSpectaclesStr!=null&&newSpectaclesStr.size()!=0){
                    Log.d(TAG, "MSaD onClick: puttitre");
                    saison.put("spectacles",newSpectaclesStr);
                }

                insertSaisonInDb();
            }

        });

    }

    private void insertSaisonInDb() {
        Log.d(TAG, "MSaD insertSaisonInDb: ");
        db.collection("chorale").document(idChorale).collection("saisons").document(oldIdSaisonStr)
                .update(saison)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "MSpD onSuccess: OK");
                    modifyMajChorale();
                    Intent startMSp = new Intent(ModifySaisonDetails.this, ChooseChorale.class);
                    startMSp.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(startMSp);

                }).addOnFailureListener(e -> Log.d(TAG, "MSpD onSuccess: maj spectacle failed"));

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK){
            if(requestCode==REQUEST_CODE){
                if (data != null) {
                    newSpectaclesStr=data.getStringArrayListExtra("listGenModif");
                    newNamesSpectaclesStr=data.getStringArrayListExtra("listGenCompModif");

                    StringBuilder sb = new StringBuilder(" ");
                    String newLine = System.getProperty("line.separator");
                    int i=0;
                    for(String titre:newNamesSpectaclesStr){
                        i++;
                        String listTitres = i+". "+titre+newLine;
                        sb.append(listTitres).append(newLine);
                    }
                    newNameSpectacle.setText(sb.toString());
                }

            }
        }
    }


    private void completeOld() {

        oldNom.setText(oldNomStr);

        StringBuilder sb = new StringBuilder(" ");
        String newLine = System.getProperty("line.separator");

        if(oldNamesSpectaclesStr!=null&&oldNamesSpectaclesStr.size()!=0) {
            int i = 0;
            for (String titre : oldNamesSpectaclesStr) {
                i++;
                String listTitres = i + ". " + titre + newLine;
                sb.append(listTitres).append(newLine);
            }

            Log.d(TAG, "MSaD completed Old: noms spectacles" + sb.toString());
            oldNameSpectacle.setText(sb.toString());
        }else{
            oldNameSpectacle.setText("");
        }

    }

    private void getIntentBundle() {

        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleSpectacle");

        oldNomStr=args.getString("oldNom");
        oldIdSaisonStr = args.getString("idSaison");
        idChorale = args.getString("idChorale");
        oldSpectaclesStr = args.getStringArrayList("oldSpectacles");
        oldNamesSpectaclesStr = args.getStringArrayList("OldSpectaclesNames");
        nomChoraleStr =args.getString("nomChorale");

        Log.d(TAG, "MSaD getIntentBundle: "+oldNomStr+" "+oldIdSaisonStr+" "+idChorale+" "+oldSpectaclesStr+" "+oldNamesSpectaclesStr);

    }

    private void clearLists() {

        if(oldNamesSpectaclesStr!=null){

            oldNamesSpectaclesStr.clear();
        }

        if(oldSpectaclesStr!=null){
            oldSpectaclesStr.clear();
        }

    }



    private void suppSaisons() {
        CollectionReference saisonRef = db.collection("chorale").document(idChorale).collection("saisons");
        saisonRef.document(oldIdSaisonStr)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "MSpD OnSucess: suppSpectacles");
                    Toast.makeText(ModifySaisonDetails.this, "Réussi saison deleted", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> Log.d(TAG, "MSpD OnFailure: suppSaisons"));

    }

    private void modifyMajChorale() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj", Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "MSD onSuccess: maj chorale done");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MSD onSuccess: maj chorale failed");
                    }
                });
    }

    private void verifyCurrentSaison() {
        Log.d(TAG, "MSD verifyCurrentSaison: début");
        db.collection("chorale").document(idChorale)
                .get()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "MSD verifyCurrentSaison: avant success ");
                    if(task.isSuccessful()){
                        Log.d(TAG, "MSD verifyCurrentSaison: successfull ");
                        if(task.getResult().exists()){
                            currentSaison=(String) task.getResult().get("current_saison");
                            Log.d(TAG, "MSD verifyCurrentSaison: "+currentSaison);
                        }
                    }
                }).addOnFailureListener(e -> {
            Log.d(TAG, "MSD verifyCurrentSaison: pb verify chorale");
                });

    }

    @Override
    public void onDialogSuppPositiveClick() {
        if(!currentSaison.equals(oldIdSaisonStr)) {
            suppSaisons();
            modifyMajChorale();
            Intent startModifySaisonActivity = new Intent(ModifySaisonDetails.this, ChooseChorale.class);
            startModifySaisonActivity.putExtra("origine", "AdminHomeModifSaison");
            startModifySaisonActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(startModifySaisonActivity);
        }else{
            Log.d(TAG, "MSD onDialogSuppPositiveClick: pas le droit de supprimer saison courrante");
            Toast.makeText(this, "Attention on ne peut pas supprimer une saison courante !", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer cette Saison", Toast.LENGTH_SHORT).show();
        finish();
    }
}
