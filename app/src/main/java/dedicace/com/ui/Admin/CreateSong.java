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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dedicace.com.R;

public class CreateSong extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener {

    private TextView fileMp3,titre, nomChorale;
    private RadioGroup rgb;
    private RadioButton rbTutti;
    private static final String TAG ="coucou";
    private File[] listFiles;
    private String[] listMp3;
    private int mp3Selected;
    private String pathSelected;
    private String fileNameSelected;
    private final static int REQUEST_CODE=100;
    private static final int REQUEST_CODE_B = 200;
    private static final int REQUEST_CODE_C = 300;
    private String titreSong;
    private String pupitreSong;
    private String mp3Song;
    private Uri downloadUrl;
    private String idChorale, nomChoraleStr;
    private String idSourceSong;


    private StorageReference mStorageRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_song);

        Button selectChorale = findViewById(R.id.btn_select_chorale_create_song);
        Button createSongInDb = findViewById(R.id.btn_create_song_db);
        Button selectMp3 = findViewById(R.id.btn_select_mp3);
        titre = findViewById(R.id.tv_titre_song);
        fileMp3 = findViewById(R.id.tv_mp3);
        nomChorale = findViewById(R.id.tv_select_chorale_create_song);
        Button selectTitre = findViewById(R.id.btn_select_titre);
        rgb=findViewById(R.id.rgb);
        rbTutti=findViewById(R.id.rb_tutti_cs);
        rbTutti.setChecked(true);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        createSongInDb.setOnClickListener(view -> {
            //obligatoire
            titreSong=titre.getText().toString();
            mp3Song = fileMp3.getText().toString();
            int idRb = rgb.getCheckedRadioButtonId();

            switch (idRb){
                case R.id.rb_tutti_cs:
                    pupitreSong="TUTTI";
                    break;
                case R.id.rb_bass_cs:
                    pupitreSong="BASS";
                    break;
                case R.id.rb_tenor_cs:
                    pupitreSong="TENOR";
                    break;
                case R.id.rb_alto_cs:
                    pupitreSong="ALTO";
                    break;
                case R.id.rb_soprano_cs:
                    pupitreSong="SOPRANO";
                    break;
            }

            Log.d(TAG, "CS onClick: pupitre "+pupitreSong);

            if(!titreSong.equals("Selection titre...")&&!mp3Song.equals("Selection mp3...")){
                Log.d(TAG, "CS onClick: conditions passées "+ titreSong+ " "+pupitreSong+" "+mp3Song);

                if(!TextUtils.isEmpty(idChorale)) {
                    insertMp3InCloudStorage();
                }else{
                    Log.d(TAG, "MSSD onCreate: id chorale vide");
                    Toast.makeText(this, "Il faut préciser la Chorale !", Toast.LENGTH_SHORT).show();
                }


            }else{
                Toast.makeText(CreateSong.this, "Il manque des éléments", Toast.LENGTH_SHORT).show();
            }
        });

        selectTitre.setOnClickListener(view -> {
            Intent startModifySSActivity = new Intent(CreateSong.this,ModifySourceSong.class);
            startModifySSActivity.putExtra("origine","CreateSong");
            startActivityForResult(startModifySSActivity,REQUEST_CODE_B);
        });


        selectMp3.setOnClickListener(view -> {
            getLists();
            selectMp3();
        });

        selectChorale.setOnClickListener(view -> {
            Intent startModifyChorale = new Intent(CreateSong.this,ModifyChorale.class);
            startModifyChorale.putExtra("origine","CreateSong");
            startActivityForResult(startModifyChorale,REQUEST_CODE_C);
        });
    }

    private void selectMp3() {
        Log.d(TAG, "CS selectMp3: "+ Arrays.toString(listMp3));

        Intent startChooseMp3ctivity = new Intent(CreateSong.this,ChooseMp3.class);
        startChooseMp3ctivity.putExtra("listMp3s",listMp3);
        startActivityForResult(startChooseMp3ctivity,REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "CS onActivityResult: ok cela marche");
            if (requestCode == REQUEST_CODE) {


                if (data != null) {
                    mp3Selected = data.getIntExtra("mp3selected", -1);
                }

                if (mp3Selected != -1) {
                    String name = listMp3[mp3Selected];
                    fileMp3.setText(name);
                   // pathSelected = listPath.get(mp3Selected);
                    pathSelected =listFiles[mp3Selected].getAbsolutePath();
                    fileNameSelected = name;
                    Log.d(TAG, "CS onCreate: " + pathSelected);
                }
            } else if (requestCode == REQUEST_CODE_B) {
                Log.d(TAG, "CS onActivityResult: request_codeB");

                titreSong = Objects.requireNonNull(data).getStringExtra("titreselected");
                titre.setText(titreSong);
                Log.d(TAG, "CS onActivityResult: request_codeB " + titreSong);

            } else if (requestCode == REQUEST_CODE_C) {
                Log.d(TAG, "CS onActivityResult: request_codeC");

                if (data != null) {
                    idChorale = data.getStringExtra("idselected");
                    nomChoraleStr=data.getStringExtra("nomChorale");
                }

                nomChorale.setText(nomChoraleStr);

            }else {
                Log.d(TAG, "CS onActivityResult: petit problème au retour ");
            }
        }
    }

    private void insertMp3InCloudStorage() {
        Uri fileSelected = Uri.fromFile(new File(pathSelected));
        StorageReference mp3Ref = mStorageRef.child("songs/fichier_mp3/"+fileNameSelected);

        UploadTask uploadTask = mp3Ref.putFile(fileSelected);

        uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "CS onSuccess: bravo c'est uploadé"))
                .addOnFailureListener(exception -> Log.d(TAG, "CS onFailure: dommage c'est raté"));


        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            // Continue with the task to get the download URL
            return mp3Ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                downloadUrl = task.getResult();
                insertSonginDb();
                Log.d(TAG, "CS onComplete: "+downloadUrl);
            } else {
                Log.d(TAG, "CS onComplete: Il y a eu un pb");
            }
        });


    }

    private void insertSonginDb() {
        Map<String,Object> song = new HashMap<>();
        song.put("songPath",downloadUrl.toString());
        song.put("pupitre",pupitreSong);
        song.put("recordSource","BANDE_SON");
        song.put("titre_song",titreSong);
        song.put("maj",Timestamp.now());

        db.collection("songs")
                .add(song)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    File file = new File(pathSelected);
                    if(file.delete()){
                        Log.d(TAG, "CS onSuccess: le fichier est supprimé du local");
                    }else{
                        Log.d(TAG, "CS onSuccess: problème de suppression en local du fichier");
                    }

                    modifyMajChorale();
                    modifyMajSourceSong();
                    newSong();
                })
                .addOnFailureListener(e -> Log.d(TAG, "CS Error adding document", e));
    }

    private void newSong() {
        DialogFragment dialog = new DialogNewSSFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
    }

    private void modifyMajSourceSong() {

        getSourceSong();

    }

    private void getSourceSong() {
        CollectionReference sourceSongRef=db.collection("sourceSongs");
        Query query = sourceSongRef.whereEqualTo("titre",titreSong);

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                List<DocumentSnapshot> documents =  Objects.requireNonNull(task.getResult()).getDocuments();

                if(documents.size()==1){
                    idSourceSong=documents.get(0).getId();
                    Log.d(TAG, "CS onComplete: il y a 1 SS correspondante "+idSourceSong);

                    Map<String,Object> data = new HashMap<>();
                    data.put("maj",Timestamp.now());
                    db.collection("sourceSongs").document(idSourceSong)
                            .update(data)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "CS onSuccess: maj SS done"))
                            .addOnFailureListener(e -> Log.d(TAG, "CS onFailure: maj SS failed"));
                }else{
                    Log.d(TAG, "CS onComplete: il n'y a pas de SS correspondante");
                }
                Log.d(TAG, "MSSD onDialogPositiveClick: "+query);

            }else{
                Log.d(TAG, "MSSD onComplete: pb dans la query");
            }
        });

    }

    private void modifyMajChorale() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj",Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "CSS onSuccess: maj chorale done"))
                .addOnFailureListener(e -> Log.d(TAG, "CSS onSuccess: maj chorale failed"));
    }

    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/fichier_mp3_Chorale");

        if(file.mkdirs()){
            Log.d(TAG, "CS  le dossier est fait");

        }else{
            Log.d(TAG, "CS dossier non réalisé ou déjà fait");
        }

        if(file.exists()){
            listFiles = file.listFiles();

            Log.d(TAG, "CSS selectMp3: "+listFiles.length);

            listMp3 = new String[listFiles.length];

            for (int i = 0; i < listFiles.length; i++) {
                listMp3[i]=listFiles[i].getName();

                Log.d(TAG, "selectMp3: "+listFiles[i].getName());
            }
        }

        Log.d(TAG, "CSS : selectMp3: "+ listMp3.length);

    }

    @Override
    public void onDialogPositiveClick() {
        titre.setText("Selection titre...");
        rbTutti.setChecked(true);
        fileMp3.setText("Selection mp3...");
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
