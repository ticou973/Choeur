package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dedicace.com.R;

public class CreateSourceSong extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener, OnFailureListener {

    private EditText titre, groupe, duration;
    private TextView background;
    private static final String TAG ="coucou";
    private File[] listFiles;
    private String[] listImages;
    private int imageSelected;
    private String pathSelected;
    private String fileNameSelected;
    private final static int REQUEST_CODE=100;
    private String titreSS;
    private String groupeSS;
    private String backgroundSS;
    private Uri downloadUrl;
    private int durationSS;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_source_song);

        Button createSSInDb = findViewById(R.id.btn_create_ss_db);
        Button selectBackground = findViewById(R.id.btn_select_background);
        titre = findViewById(R.id.et_titre_ss);
        groupe = findViewById(R.id.et_groupe_ss);
        duration = findViewById(R.id.et_duration_ss);
        background = findViewById(R.id.tv_background);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();



        createSSInDb.setOnClickListener(view -> {
            //obligatoire
            titreSS = titre.getText().toString();
            groupeSS = groupe.getText().toString();
            backgroundSS = background.getText().toString();

            if(duration.getText().toString().equals("")){
                durationSS=0;
            }else{
                durationSS = Integer.parseInt(duration.getText().toString());
            }

            if(!titreSS.equals("")&&!groupeSS.equals("")&&!backgroundSS.equals("Selection background...")&&durationSS!=0){
                insertBackgroundInCloudStorage();

            }else{
                Toast.makeText(CreateSourceSong.this, "Il manque des éléments", Toast.LENGTH_SHORT).show();
            }
        });

        selectBackground.setOnClickListener(view -> {
            getLists();
            selectBackground();
        });
    }



    //todo compléter le storage utilities pour chercher les lists sur le téléphone ou cloud
     private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/Image_Background_Chorale");

        if(file.mkdirs()){
            Log.d(TAG, "CSS insertBackgroundInCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "CSS insertBackgroundInCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()) {
            listFiles = file.listFiles();

            Log.d(TAG, "CSS getLists: " + Arrays.toString(listFiles));

            if (listFiles != null && listFiles.length != 0) {

                Log.d(TAG, "CSS selectBackground: " + " " + listFiles.length);

                listImages = new String[listFiles.length];

                for (int i = 0; i < listFiles.length; i++) {
                    listImages[i] = listFiles[i].getName();

                    Log.d(TAG, "CSS selectBackground: " + listFiles[i].getName());
                }
            }else{
                Log.d(TAG, "CSS getLists: pas de listFiles ");
            }
        }

    }

    private void insertBackgroundInCloudStorage() {

        Uri fileSelected = Uri.fromFile(new File(pathSelected));
        StorageReference imageRef = mStorageRef.child("songs/photos_background/"+fileNameSelected);

        UploadTask uploadTask = imageRef.putFile(fileSelected);

        uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "CSS onSuccess: bravo c'est uploadé"))
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
                insertSSinDb();
                Log.d(TAG, "CSS onComplete: "+downloadUrl);
            } else {
                Log.d(TAG, "CSS onComplete: Il y a eu un pb "+ Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    private void newSS() {
        DialogFragment dialog = new DialogNewSSFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
    }

    private void selectBackground() {
        Log.d(TAG, "CSS selectBackground: ");

        Intent startChooseBackgroundActivity = new Intent(CreateSourceSong.this,ChooseBackground.class);
        startChooseBackgroundActivity.putExtra("listimages",listImages);
        startActivityForResult(startChooseBackgroundActivity,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Log.d(TAG, "CSS onActivityResult: ok cela marche");
            if(requestCode==REQUEST_CODE){

                if (data != null) {
                    imageSelected = data.getIntExtra("imageselected",-1);
                    Log.d(TAG, "CS onActivityResult: "+imageSelected);
                }

                if(imageSelected!=-1) {
                    String name = listImages[imageSelected];
                    background.setText(name);
                    //pathSelected = listPath.get(imageSelected);
                    pathSelected=listFiles[imageSelected].getAbsolutePath();
                    fileNameSelected = name;
                    Log.d(TAG, "CSS onCreate: " + pathSelected);
                }
            }

        }else{
            Log.d(TAG, "CSS onActivityResult: petit problème au retour ");
        }
    }

    private void insertSSinDb() {
        Map<String,Object> sourceSong = new HashMap<>();
        sourceSong.put("background",downloadUrl.toString());
        sourceSong.put("duration",durationSS);
        sourceSong.put("groupe",groupeSS);
        sourceSong.put("original_song","");
        sourceSong.put("titre",titreSS);
        sourceSong.put("maj",Timestamp.now());

        db.collection("sourceSongs")
                .add(sourceSong)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    File file = new File(pathSelected);
                    if(file.delete()){
                        Log.d(TAG, "CSS onSuccess: le fichier est supprimé du local");
                    }else{
                        Log.d(TAG, "CSS onSuccess: problème de suppression en local du fichier");
                    }

                    newSS();
                })
                .addOnFailureListener(e -> Log.d(TAG, "CSS Error adding document", e));
    }


    @Override
    public void onDialogPositiveClick() {
        titre.setText("");
        groupe.setText("");
        duration.setText("");
        background.setText("Selection background...");
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

    @Override
    public void onFailure(@NonNull Exception e) {
        int errorCode = ((StorageException) e).getErrorCode();
        String errorMessage = e.getMessage();

        Log.d(TAG, "CSS onFailure: "+ errorMessage+" "+errorCode);

    }
}
