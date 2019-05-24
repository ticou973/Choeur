package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
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
//todo voir comment delete sur le cloud les éléments dont on ne se sert plus pour éviter une accumulation dans le cloud
//todo revoir l'id de la chorale lorsqu'il y aura plusieures chorales, es sources songs changeront idem pour les songs
public class ModifySourceSongDetails extends AppCompatActivity implements DialogAlertTitle.DialogAlertTitleListener, DialogSuppFragment.DialogSuppListener {
    private TextView oldTitre, oldGroupe, oldDuration, oldBackground, newBackground;
    private EditText newTitre, newGroupe, newDuration;
    private Button suppSS, modifySS, selectBackground;

    private String[] listImages;
    private static List<String> listFilesImage = new ArrayList<>();
    private List<String> listPath = new ArrayList<>();
    private String pathSelected;
    private String fileNameSelected;
    private File[] listFiles;

    private final static int REQUEST_CODE=100;
    private static final String TAG ="coucou";
    private int imageSelected,oldDurationInt,newDurationInt;
    private String idChorale,idSS,oldTitreStr,oldGroupeStr,newTitreStr,newGroupeStr,newBackgroundStr;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Uri downloadUrl;
    private Uri fileSelected;
    private Map<String,Object> sourceSong;
    private List<Song> songs = new ArrayList<>();
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
        suppSS= findViewById(R.id.btn_supp_source_song);
        modifySS= findViewById(R.id.btn_modify_source_song);
        selectBackground= findViewById(R.id.btn_select_new_background);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        sharedPreferences =PreferenceManager.getDefaultSharedPreferences(this);
        idChorale=sharedPreferences.getString("idchorale"," ");
        Log.d(TAG, "MSSD onCreate: idChorale "+ idChorale );

        getIntentBundle();

        completeOld();

        suppSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new DialogSuppFragment();
                dialogFragment.show(getSupportFragmentManager(),TAG);
            }
        });

        modifySS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                   insertBackgroundInCloudStorage();
                   //insertSSinDb();

                }else{
                    Toast.makeText(ModifySourceSongDetails.this, "Il manque des éléments pour insérer", Toast.LENGTH_SHORT).show();
                }
            }
        });


        selectBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLists();
                selectBackground();
            }
        });

    }

    private void insertBackgroundInCloudStorage() {
        sourceSong = new HashMap<>();
        sourceSong.put("maj",Timestamp.now());
        Log.d(TAG, "MSSD insertSSinDb: "+idSS);

        if(!newBackgroundStr.equals("Select. Backgr.")){
            Log.d(TAG, "MSSD insertSSinDb: if background");

            fileSelected = Uri.fromFile(new File(pathSelected));
            StorageReference imageRef = mStorageRef.child("songs/photos_background/"+fileNameSelected);

            UploadTask uploadTask = imageRef.putFile(fileSelected);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "CSS onSuccess: bravo c'est uploadé !");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Log.d(TAG, "CSS onFailure: dommage c'est raté");
                        }
                    });


            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadUrl = task.getResult();
                        sourceSong.put("background",downloadUrl.toString());
                        //insertCloud();
                        insertSSinDb();
                        Log.d(TAG, "CSS onComplete: "+downloadUrl);
                    } else {
                        // Handle failures
                        // ...
                        Log.d(TAG, "CSS onComplete: Il y a eu un pb");
                    }
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MSSD onSuccess: maj sourceSong failed");
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
                        Log.d(TAG, "MSSD onSuccess: maj chorale done");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MSSD onSuccess: maj chorale failed");
                    }
                });
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

            for (File image:listFiles) {
                Log.d(TAG, "MSSD selectBackground: "+image.getName());
                listFilesImage.add(image.getName());
                listPath.add(image.getAbsolutePath());
            }

            Log.d(TAG, "MSSD selectBackground: "+listFilesImage.size()+" "+listFiles.length);

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

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    List<DocumentSnapshot> documents =  task.getResult().getDocuments();

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
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "MSSD onSuccess: Ok pour chgt de noms des songs ");
                                insertCloud();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "MSSD onFailure: raté chgt de noms des songs ");
                            }
                        });
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

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    List<DocumentSnapshot> documents =  task.getResult().getDocuments();

                    for(DocumentSnapshot document:documents){
                        listIds.add(document.getId());
                        Log.d(TAG, "MSSD onEvent: "+document.getId());
                    }
                    deleteSongs();

                    Log.d(TAG, "MSSD onDialogPositiveClick: "+query);

                }else{
                    Log.d(TAG, "MSSD onComplete: pb dans la query");
                }
            }
        });
    }

    private void deleteSongs() {
        Log.d(TAG, "MSSD DeleteSongs: ");

        for(String name:listIds){
            db.collection("songs").document(name)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "MSSD onSuccess: Ok pour deletesongs ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "MSSD onFailure: raté delte songs ");
                        }
                    });
        }
    }

    private void suppSourceSong() {

        db.collection("sourceSongs").document(idSS)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "MSSD onSuccess: réussi SS supprimé");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MSSD onFailure: SS pas supprimé");
                    }
                });
    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer cette Source Song", Toast.LENGTH_SHORT).show();
        finish();
    }
}
