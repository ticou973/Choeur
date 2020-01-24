package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
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
import java.util.Objects;

import dedicace.com.R;

//todo voir tout le doublonnage de code par rapport o Create SourceSong, à factoriser à tous les niveaux
//todo voir comment delete sur le cloud les éléments dont on ne se sert plus pour éviter une accumulation dans le cloud
public class ModifySourceSongDetails extends AppCompatActivity implements DialogAlertTitle.DialogAlertTitleListener, DialogSuppFragment.DialogSuppListener {
    private TextView oldTitre, oldGroupe, oldDuration, oldBackground, newBackground, nomChorale;
    private EditText newTitre, newGroupe, newDuration;

    private String[] listImages;
    private String pathSelected;
    private String fileNameSelected;
    private File[] listFiles;

    private final static int REQUEST_CODE=100;
    private final static int REQUEST_CODEB=200;
    private static final String TAG ="coucou";
    private int imageSelected,oldDurationInt,newDurationInt;
    private String idChorale,idSS,oldTitreStr,oldGroupeStr,newTitreStr,newGroupeStr,newBackgroundStr, nomChoraleStr;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Uri downloadUrl;
    private Map<String,Object> sourceSong;
    private List<String> listIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_source_song_details);
        oldTitre= findViewById(R.id.tv_titre_old);
        oldGroupe= findViewById(R.id.tv_groupe_old);
        oldDuration= findViewById(R.id.tv_duration_old);
        oldBackground= findViewById(R.id.tv_background_old);
        newBackground= findViewById(R.id.tv_background_new);
        newTitre= findViewById(R.id.et_titre_new);
        newGroupe= findViewById(R.id.et_groupe_new);
        newDuration= findViewById(R.id.et_duration_new);
        Button suppSS = findViewById(R.id.btn_supp_source_song);
        Button modifySS = findViewById(R.id.btn_modify_source_song);
        Button selectBackground = findViewById(R.id.btn_select_new_background);
        Button selectChorale = findViewById(R.id.btn_select_chorale_modifySS);
        nomChorale = findViewById(R.id.tv_modifSS_chorale);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getIntentBundle();

        completeOld();

        suppSS.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(idChorale)) {
                DialogFragment dialogFragment = new DialogSuppFragment();
                dialogFragment.show(getSupportFragmentManager(),TAG);
            }else{
                Log.d(TAG, "MSSD onCreate: id chorale vide");
            }
        });

        modifySS.setOnClickListener(view -> {

            //obligatoire
            newTitreStr = newTitre.getText().toString();
            newGroupeStr = newGroupe.getText().toString();
            newBackgroundStr = newBackground.getText().toString();

            //facultatif
            if(newDuration.getText().toString().equals("")){
                newDurationInt=0;
            }else{
                newDurationInt = Integer.parseInt(newDuration.getText().toString());
            }

            if(!newTitreStr.equals("")||!newGroupeStr.equals("")||!newBackgroundStr.equals("Select. Backgr.")||newDurationInt!=0){
                Log.d(TAG, "MSSD onClick: conditions passées "+ newTitreStr+ " "+newGroupeStr+" "+newDurationInt+" "+newBackgroundStr);
                if(!TextUtils.isEmpty(idChorale)) {
                    insertBackgroundInCloudStorage();
                }else{
                    Log.d(TAG, "MSSD onCreate: id chorale vide");
                    Toast.makeText(this, "Il faut préciser la Chorale !", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(ModifySourceSongDetails.this, "Il manque des éléments pour insérer", Toast.LENGTH_SHORT).show();
            }
        });


        selectBackground.setOnClickListener(view -> {
            getLists();
            selectBackground();
        });

        selectChorale.setOnClickListener(view -> {
            Intent startModifyChorale = new Intent(ModifySourceSongDetails.this,ModifyChorale.class);
            startModifyChorale.putExtra("origine","ModifySSDetails");
            startActivityForResult(startModifyChorale,REQUEST_CODEB);
        });

    }


    private void insertBackgroundInCloudStorage() {
        sourceSong = new HashMap<>();
        sourceSong.put("maj",Timestamp.now());
        Log.d(TAG, "MSSD insertSSinDb: "+idSS);

        if(!newBackgroundStr.equals("Select. Backgr.")){
            Log.d(TAG, "MSSD insertSSinDb: if background");

            Uri fileSelected = Uri.fromFile(new File(pathSelected));
            StorageReference imageRef = mStorageRef.child("songs/photos_background/"+fileNameSelected);

            UploadTask uploadTask = imageRef.putFile(fileSelected);

            uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "CSS onSuccess: bravo c'est uploadé !"))
                    .addOnFailureListener(exception -> Log.d(TAG, "CSS onFailure: dommage c'est raté"));


            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult();
                    sourceSong.put("background", Objects.requireNonNull(downloadUrl).toString());
                    //insertCloud();
                    insertSSinDb();
                    Log.d(TAG, "CSS onComplete: "+downloadUrl);
                } else {
                    Log.d(TAG, "CSS onComplete: Il y a eu un pb");
                }
            });

        }else{
            //todo mettre insert plus bas et enlever les 2 du if else
            Log.d(TAG, "MSSD insertBackgroundInCloudStorage: else if background");
            insertSSinDb();
        }
    }

    private void insertSSinDb() {

       if(!newGroupeStr.equals("")){
            Log.d(TAG, "MSSD insertSSinDb: if groupe");
            sourceSong.put("groupe",newGroupeStr);
        }
        if(newDurationInt!=0){
            Log.d(TAG, "MSSD insertSSinDb: if duration ");
            sourceSong.put("duration",newDurationInt);
        }

        if(!newTitreStr.equals("")) {
            DialogFragment dialog = new DialogAlertTitle();
            dialog.show(getSupportFragmentManager(), TAG);
            Log.d(TAG, "MSSD insertSSinDb: if titre");
        }else{
            Log.d(TAG, "MSSD insertSSinDb: else titre");
            insertCloud();
        }
    }

    private void insertCloud() {
        Log.d(TAG, "MSSD insertCloud: "+sourceSong);
        db.collection("sourceSongs").document(idSS)
                .update(sourceSong)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "MSSD onSuccess: maj sourcesong done");
                    if(pathSelected!=null) {
                        File file = new File(pathSelected);
                        if (file.delete()) {
                            Log.d(TAG, "MSSD onSuccess: le fichier est supprimé du local");
                        } else {
                            Log.d(TAG, "MSSD onSuccess: problème de suppression en local du fichier");
                        }
                    }else{
                        Log.d(TAG, "MSSD onSuccess: il n'y a pas de fichier à supprimer car pas de modif de background");
                    }
                    modifyMajChorale();
                    Intent startMSS = new Intent(ModifySourceSongDetails.this,ModifySourceSong.class);
                    startMSS.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startMSS.putExtra("origine","AdminHome");
                    startActivity(startMSS);
                })
                .addOnFailureListener(e -> Log.d(TAG, "MSSD onSuccess: maj sourceSong failed"));
    }

    private void modifyMajChorale() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj",Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "MSSD onSuccess: maj chorale done"))
                .addOnFailureListener(e -> Log.d(TAG, "MSSD onSuccess: maj chorale failed"));
    }

    //todo voir pour le modifyMajSong si utile lorsque l'on change le nom


    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleSS");
        oldTitreStr=args.getString("oldTitre");
        oldGroupeStr=args.getString("oldGroupe");
        oldDurationInt=args.getInt("oldDuration");
        idSS=args.getString("idSS");
        Log.d(TAG, "MSSD getIntentBundle: "+oldTitreStr+" "+oldGroupeStr+" "+oldDuration+" "+idSS);
    }

    private void completeOld() {

        oldTitre.setText(oldTitreStr);
        oldGroupe.setText(oldGroupeStr);
        oldDuration.setText(String.valueOf(oldDurationInt));

        //todo plus tard voir comment visualiser ce background avant
        oldBackground.setText(" ");
    }

    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/Image_Background_Chorale");

        if(file.mkdirs()){
            Log.d(TAG, "MSSD insertBackgroundInCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "MSSD insertBackgroundInCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()){
            listFiles = file.listFiles();

            Log.d(TAG, "MSSD selectBackground: "+" "+listFiles.length);

            listImages = new String[listFiles.length];

            for (int i = 0; i < listFiles.length; i++) {
                listImages[i]=listFiles[i].getName();

                Log.d(TAG, "MSSD selectBackground: "+listFiles[i].getName()+listImages[i]);
            }
        }

        Log.d(TAG, "MSSD : selectBackground: "+ listImages.length);
    }

    private void selectBackground() {
        Log.d(TAG, "MSS selectBackground: ");

        Intent startChooseBackgroundActivity = new Intent(ModifySourceSongDetails.this,ChooseBackground.class);
        startChooseBackgroundActivity.putExtra("listimages",listImages);
        startActivityForResult(startChooseBackgroundActivity,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Log.d(TAG, "MSSD onActivityResult: ok cela marche");
            if(requestCode==REQUEST_CODE){

                if (data != null) {
                    imageSelected = data.getIntExtra("imageselected",-1);
                }

                if(imageSelected!=-1) {
                    String name = listImages[imageSelected];
                    newBackground.setText(name);
                    pathSelected=listFiles[imageSelected].getAbsolutePath();
                    fileNameSelected = name;
                    Log.d(TAG, "MSS onCreate: " + pathSelected);
                }
            }else if(requestCode==REQUEST_CODEB){

                if (data != null) {
                    idChorale = data.getStringExtra("idselected");
                    nomChoraleStr=data.getStringExtra("nomChorale");
                }

                nomChorale.setText(nomChoraleStr);
            }
        }else{
            Log.d(TAG, "MSSD onActivityResult: petit problème au retour ");
        }
    }

    @Override
    public void onDialogPositiveClick() {
        Log.d(TAG, "MSSD onDialogPositiveClick: "+newTitreStr);
        sourceSong.put("titre",newTitreStr);
        CollectionReference songsRef=db.collection("songs");
        Query query = songsRef.whereEqualTo("titre_song",oldTitreStr);

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                List<DocumentSnapshot> documents =  Objects.requireNonNull(task.getResult()).getDocuments();

                for(DocumentSnapshot document:documents){
                    listIds.add(document.getId());
                    Log.d(TAG, "MSSD onEvent: "+document.getId());
                }

                if(listIds!=null&&listIds.size()!=0) {
                     changeSongsName();
                }else{

                    Log.d(TAG, "MSSD : pas de changments de noms");
                    insertCloud();
                }


                Log.d(TAG, "MSSD onDialogPositiveClick: "+query);

            }else{
                Log.d(TAG, "MSSD onComplete: pb dans la query");
            }
        });

    }

    private void changeSongsName() {
        Map<String,Object> data = new HashMap<>();
        data.put("titre_song",newTitreStr);
        data.put("maj",Timestamp.now());
        Log.d(TAG, "MSSD changeSongsName: ");
            for (String name : listIds) {
                //todo faire un try catch là dessus
                db.collection("songs").document(name)
                        .update(data)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "MSSD onSuccess: Ok pour chgt de noms des songs ");
                            insertCloud();
                        })
                        .addOnFailureListener(e -> Log.d(TAG, "MSSD onFailure: raté chgt de noms des songs "));
            }
    }

    @Override
    public void onDialogNegativeClick() {
        Log.d(TAG, "onDialogNegativeClick: ");

        Toast.makeText(this, "Nous n'avons pas inséré le nouveau titre ", Toast.LENGTH_SHORT).show();

        //ifBackground();
        insertCloud();
    }

    @Override
    public void onDialogSuppPositiveClick() {
        modifyMajChorale();
        getSuppSongs();
        suppSourceSong();
        Intent startModifySSActivity = new Intent(ModifySourceSongDetails.this,ModifySourceSong.class);
        startModifySSActivity.putExtra("origine","AdminHome");
        startModifySSActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(startModifySSActivity);
    }

    private void getSuppSongs() {

        CollectionReference songsRef=db.collection("songs");
        Query query = songsRef.whereEqualTo("titre_song",oldTitreStr);

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                List<DocumentSnapshot> documents =  Objects.requireNonNull(task.getResult()).getDocuments();

                for(DocumentSnapshot document:documents){
                    listIds.add(document.getId());
                    Log.d(TAG, "MSSD onEvent: "+document.getId());
                }
                deleteSongs();

                Log.d(TAG, "MSSD onDialogPositiveClick: "+query);

            }else{
                Log.d(TAG, "MSSD onComplete: pb dans la query");
            }
        });
    }

    private void deleteSongs() {
        Log.d(TAG, "MSSD DeleteSongs: ");

        for(String name:listIds){
            db.collection("songs").document(name)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "MSSD onSuccess: Ok pour deletesongs "))
                    .addOnFailureListener(e -> Log.d(TAG, "MSSD onFailure: raté delte songs "));
        }
    }

    private void suppSourceSong() {

        db.collection("sourceSongs").document(idSS)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "MSSD onSuccess: réussi SS supprimé"))
                .addOnFailureListener(e -> Log.d(TAG, "MSSD onFailure: SS pas supprimé"));
    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer cette Source Song", Toast.LENGTH_SHORT).show();
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
