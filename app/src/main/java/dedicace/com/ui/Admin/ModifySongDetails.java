package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedicace.com.R;
import dedicace.com.data.database.Song;

//todo voir tout le doublonnage de code par rapport o Create SourceSong, à factoriser à tous les niveaux
//todo voir pour plus tard les modifications de songs lorsque cela marchera au nieveau de la maj côté utilisateur
public class ModifySongDetails extends AppCompatActivity implements DialogSuppFragment.DialogSuppListener {
    private TextView oldTitre, oldPupitre, oldSource, oldMp3, newMp3,nomChorale;
    private EditText newPupitre, newSource;

    private String[] listMp3s;
    private static List<String> listFilesMp3 = new ArrayList<>();
    private List<String> listPath = new ArrayList<>();
    private String pathSelected;
    private String fileNameSelected;
    private File[] listFiles;

    private final static int REQUEST_CODE=100;
    private static final String TAG ="coucou";
    private int mp3Selected;
    private String idChorale,idSong,oldTitreStr,oldPupitreStr,oldSourceStr,newPupitreStr,newMp3Str,newSourceStr, nomChoraleStr;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Uri downloadUrl;
    private Uri fileSelected;
    private Map<String,Object> Song;
    private List<Song> songs = new ArrayList<>();
    private List<String> listIds = new ArrayList<>();
    private String idSourceSong;
    private Map<String,Object> song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_song_details);

        oldTitre= findViewById(R.id.tv_titre_song_old_modify);
        oldPupitre= findViewById(R.id.tv_pupitre_old);
        oldSource= findViewById(R.id.tv_source_old);
        oldMp3= findViewById(R.id.tv_mp3_old);
        newMp3= findViewById(R.id.tv_mp3_new_modify);
        newPupitre= findViewById(R.id.et_pupitre_new);
        newSource= findViewById(R.id.et_source_new);
        Button suppSong = findViewById(R.id.btn_supp_song);
        Button modifySong = findViewById(R.id.btn_modify_song);
        Button selectMp3 = findViewById(R.id.btn_select_new_mp3);
        Button selectChorale = findViewById(R.id.btn_select_chorale_modify_song);
        nomChorale = findViewById(R.id.tv_select_chorale_modify_song);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();


        Log.d(TAG, "MSD onCreate: idChorale "+ idChorale );

        getIntentBundle();

        completeOld();

        getLists();

        selectChorale.setOnClickListener(view -> {
            Intent startModifyChorale = new Intent(ModifySongDetails.this,ModifyChorale.class);
            startModifyChorale.putExtra("origine","ModifySongDetails");
            startActivityForResult(startModifyChorale,REQUEST_CODE);
        });

        suppSong.setOnClickListener(view -> {
            DialogFragment dialogFragment = new DialogSuppFragment();
            dialogFragment.show(getSupportFragmentManager(),TAG);
        });

        //todo à remettre lorsque modify sera possible ou pas?
        modifySong.setOnClickListener(view -> {

            //todo prévoir le if avec la chorale cf suppression

            Toast.makeText(ModifySongDetails.this, "Option A venir...", Toast.LENGTH_SHORT).show();
            //obligatoire

           /* newPupitreStr = newPupitre.getText().toString();
            newMp3Str = newMp3.getText().toString();
            newSourceStr = newSource.getText().toString();

            if(!newPupitreStr.equals("")||!newSourceStr.equals("")||!newMp3Str.equals(getString(R.string.select_mp3))){
                Log.d(TAG, "MSD onClick: conditions passées "+ " "+newPupitreStr+" "+newSourceStr+" "+newMp3Str);
                insertSonginDb();

            }else{
                Toast.makeText(ModifySongDetails.this, "Il manque des éléments pour insérer", Toast.LENGTH_SHORT).show();
            }*/
        });


        selectMp3.setOnClickListener(view -> selectMp3());

    }

    private void selectMp3() {
        //todo faire lorsque qu'on activera le modify ou pas...

    }

    private void insertSonginDb() {
        song = new HashMap<>();
        song.put("maj",Timestamp.now());
        Log.d(TAG, "MSD insertSonginDb: "+idSong);


        if(!newPupitreStr.equals("")){
            Log.d(TAG, "MSD insertSonginDb: if pupitre");
            song.put("pupitre",newPupitreStr);
        }
        if(!newSourceStr.equals("")){
            Log.d(TAG, "MSD insertSonginDb: if source ");
            song.put("recordSource",newSourceStr);
        }

        if(!newMp3Str.equals(getString(R.string.select_mp3))){
            Log.d(TAG, "MSD insertSonginDb: if mp3");

            insertMp3InCloudStorage();
        }else{
            Log.d(TAG, "MSSD insertSSinDb: else background");
            insertCloud();
        }

    }

    private void insertMp3InCloudStorage() {
        fileSelected = Uri.fromFile(new File(pathSelected));
        StorageReference mp3Ref = mStorageRef.child("songs/fichier_mp3/"+fileNameSelected);

        UploadTask uploadTask = mp3Ref.putFile(fileSelected);

        uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "MSD onSuccess: bravo c'est uploadé"))
                .addOnFailureListener(exception -> {
                    Log.d(TAG, "MSD onFailure: dommage c'est raté");
                });


        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return mp3Ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                downloadUrl = task.getResult();
                song.put("songPath",downloadUrl.toString());
                insertCloud();
                Log.d(TAG, "CSS onComplete: "+downloadUrl);
            } else {
                // Handle failures
                // ...
                Log.d(TAG, "CSS onComplete: Il y a eu un pb");
            }
        });


    }

    private void insertCloud() {
        Log.d(TAG, "MSD insertCloud: "+song);
        db.collection("songs").document(idSong)
                .update(song)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "MSD onSuccess: maj song done");
                    File file = new File(pathSelected);
                    if(file.delete()){
                        Log.d(TAG, "MSD onSuccess: le fichier est supprimé du local");
                    }else{
                        Log.d(TAG, "MSD onSuccess: problème de suppression en local du fichier");
                    }
                    modifyMajChorale();
                    modifyMajSourceSong();
                    finish();
                })
                .addOnFailureListener(e -> Log.d(TAG, "MSSD onSuccess: maj chorale failed"));

    }

    private void getLists() {

        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/fichier_mp3_Chorale");

        if(file.mkdirs()){
            Log.d(TAG, "MSD insertMp3InCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "MSD insertMp3InCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()){
            listFiles = file.listFiles();

            for (File image:listFiles) {
                Log.d(TAG, "MSD selectMp3: "+image.getName());
                listFilesMp3.add(image.getName());
                listPath.add(image.getAbsolutePath());
            }

            Log.d(TAG, "MSD selectMp3: "+listFilesMp3.size()+" "+listFiles.length);

            listMp3s = new String[listFiles.length];

            for (int i = 0; i < listFiles.length; i++) {
                listMp3s[i]=listFiles[i].getName();

                Log.d(TAG, "MSD selectMp3: "+listFiles[i].getName());
            }
        }

        Log.d(TAG, "MSD : selectMp3: "+ listMp3s.length);

    }

    private void completeOld() {
        oldTitre.setText(oldTitreStr);
        oldPupitre.setText(oldPupitreStr);
        oldSource.setText(oldSourceStr);
        oldMp3.setText(" ");
    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleSong");
        oldTitreStr=args.getString("oldTitre");
        oldPupitreStr=args.getString("oldPupitre");
        oldSourceStr=args.getString("oldSource");
        idSong=args.getString("idSong");
    }

    @Override
    public void onDialogSuppPositiveClick() {

        if(!TextUtils.isEmpty(idChorale)) {
            modifyMajChorale();
            modifyMajSourceSong();
            suppSong();
            Intent startModifySongActivity = new Intent(ModifySongDetails.this,ModifySong.class);
            startModifySongActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(startModifySongActivity);

        }else{
            Log.d(TAG, "MSD onCreate: id chorale vide");
            Toast.makeText(this, "Il faut préciser la Chorale !", Toast.LENGTH_SHORT).show();
        }

    }

    private void suppSong() {
        db.collection("songs").document(idSong)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "MSD onSuccess: réussi Song supprimée"))
                .addOnFailureListener(e -> Log.d(TAG, "MSD onFailure: Song pas supprimé"));

//todo faire la suppression dan sl ecloud storage de la song idem pour le background, difficulté, on ne connait pas le nom et storage ne le propose pas


    }

    private void modifyMajSourceSong() {
        CollectionReference sourceSongRef=db.collection("sourceSongs");
        Query query = sourceSongRef.whereEqualTo("titre",oldTitreStr);

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                List<DocumentSnapshot> documents =  task.getResult().getDocuments();

                if(documents.size()==1){
                    idSourceSong=documents.get(0).getId();
                    Log.d(TAG, "MSD onComplete: il y a 1 SS correspondante "+idSourceSong);

                    Map<String,Object> data = new HashMap<>();
                    data.put("maj",Timestamp.now());
                    db.collection("sourceSongs").document(idSourceSong)
                            .update(data)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "MSD onSuccess: maj SS done"))
                            .addOnFailureListener(e -> Log.d(TAG, "CS onFailure: maj SS failed"));
                }else{
                    Log.d(TAG, "MSD onComplete: il n'y a pas de SS correspondante");
                }
                Log.d(TAG, "MSD onDialogPositiveClick: "+query);

            }else{
                Log.d(TAG, "MSD onComplete: pb dans la query");
            }
        });

    }

    private void modifyMajChorale() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj",Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "MSD onSuccess: maj chorale done"))
                .addOnFailureListener(e -> Log.d(TAG, "MSD onSuccess: maj chorale failed"));

    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer cette Song", Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Log.d(TAG, "MSD onActivityResult: ok cela marche");
            if(requestCode==REQUEST_CODE){
                if (data != null) {
                    idChorale = data.getStringExtra("idselected");
                    nomChoraleStr=data.getStringExtra("nomChorale");
                }
                nomChorale.setText(nomChoraleStr);
            }else{

                Log.d(TAG, "MSDonActivityResult: pb result existe pas");

            }
        }else{
            Log.d(TAG, "MSD onActivityResult: petit problème au retour ");
        }
    }
}
