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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedicace.com.R;

public class CreateChorale extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener, OnFailureListener {
    private SharedPreferences sharedPreferences;

    //Firebase
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public static String current_user_id;
    private String idChorale;
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;
    private EditText nomChorale;
    private TextView logo;
    private Button selectLogo, createChorale;
    private File[] listFiles;
    private static List<String> listFilesImage = new ArrayList<>();
    private List<String> listPath = new ArrayList<>();
    private String[] listImages;
    private final static int REQUEST_CODE=100;
    private int imageSelected;
    private String pathSelected;
    private String fileNameSelected;
    private String logoStr;
    private String nomChoraleStr,logoImageStr;
    private Uri fileSelected;
    private Uri downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chorale);


        nomChorale=findViewById(R.id.et_nom_chorale);
        logo=findViewById(R.id.tv_logo_chorale);
        selectLogo=findViewById(R.id.btn_select_logo_chorale);
        createChorale=findViewById(R.id.btn_create_chorale);


        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        idChorale=sharedPreferences.getString("idchorale"," ");
        Log.d(TAG, "onCreate: idChorale "+ idChorale );

        selectLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoStr="Logo";
                getLists();
                selectLogo();
            }
        });


        createChorale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               nomChoraleStr=nomChorale.getText().toString();
               logoImageStr=logo.getText().toString();

               if(!TextUtils.isEmpty(nomChoraleStr)){
                   Log.d(TAG, "CC,onClick: conditions passées "+nomChoraleStr+" "+logoImageStr);
                   if(!TextUtils.isEmpty(logoImageStr)) {
                           Log.d(TAG, "CC onClick: if logo");
                           insertLogosInCloudStorage();

                   }else{
                       Log.d(TAG, "CC onClick: else logo");
                       insertChoraleInDb();
                   }

               }else{
                   Toast.makeText(CreateChorale.this, "Il manque des éléments", Toast.LENGTH_SHORT).show();
               }

            }
        });
    }

    private void insertLogosInCloudStorage() {

        fileSelected = Uri.fromFile(new File(pathSelected));
        StorageReference imageRef = mStorageRef.child("chorales/logo_titre/"+fileNameSelected);

        UploadTask uploadTask = imageRef.putFile(fileSelected);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "CC onSuccess: bravo c'est uploadé");

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d(TAG, "CC onFailure: dommage c'est raté");
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
                    insertChoraleInDb();
                    Log.d(TAG, "CC onComplete: "+downloadUrl);
                } else {
                    // Handle failures

                    // ...
                    Log.d(TAG, "CC onComplete: Il y a eu un pb "+task.getException().getMessage());
                }
            }
        });
    }

    private void insertChoraleInDb() {
        Log.d(TAG, "CC insertChoraleInDb: ");
        Map<String,Object> chorale = new HashMap<>();
        chorale.put("nom",nomChoraleStr);
        chorale.put("maj", Timestamp.now());
        if(!TextUtils.isEmpty(logoImageStr)) {
            chorale.put("logo", downloadUrl.toString());
        }else{
            chorale.put("logo","");
        }

        db.collection("chorale")
                .add(chorale)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        if(pathSelected!=null) {
                            File file = new File(pathSelected);
                            if (file.delete()) {
                                Log.d(TAG, "CC onSuccess: le fichier est supprimé du local");
                            } else {
                                Log.d(TAG, "CC onSuccess: problème de suppression en local du fichier");
                            }
                        }

                        newChorale();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "CC Error adding document", e);
                    }
                });


    }

    private void newChorale() {
        DialogFragment dialog = new DialogNewSSFragment();
        dialog.show(getSupportFragmentManager(),"TAG");

    }

    //todo compléter le storage utilities pour chercher les lists sur le téléphone ou cloud
    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/Image_Background_Chorale");

        if(file.mkdirs()){
            Log.d(TAG, "CC getLists: le dossier est fait");

        }else{
            Log.d(TAG, "CC getLists: dossier non réalisé ou déjà fait");
        }

        if(file.exists()) {
            listFiles = file.listFiles();

            Log.d(TAG, "CC getLists: " + listFiles);

            if (listFiles != null && listFiles.length != 0) {
                for (File image : listFiles) {
                    Log.d(TAG, "CC selectBackground: " + image.getName());
                    listFilesImage.add(image.getName());
                    listPath.add(image.getAbsolutePath());
                }

                Log.d(TAG, "CC selectBackground: " + listFilesImage.size() + " " + listFiles.length);

                listImages = new String[listFiles.length];

                for (int i = 0; i < listFiles.length; i++) {
                    listImages[i] = listFiles[i].getName();

                    Log.d(TAG, "CC selectBackground: " + listFiles[i].getName());
                }
            }else{
                Log.d(TAG, "CC getLists: pas de listFiles ");
            }
        }
    }

    private void selectLogo() {
        Log.d(TAG, "CC selectlogo: ");

        Intent startChooseBackgroundActivity = new Intent(CreateChorale.this,ChooseBackground.class);
        startChooseBackgroundActivity.putExtra("listimages",listImages);
        startActivityForResult(startChooseBackgroundActivity,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Log.d(TAG, "CC onActivityResult: ok cela marche");
            if(requestCode==REQUEST_CODE){

                if (data != null) {
                    imageSelected = data.getIntExtra("imageselected",-1);
                    Log.d(TAG, "CC onActivityResult: "+imageSelected);
                }

                if(imageSelected!=-1) {
                    String name = listImages[imageSelected];
                    logo.setText(name);
                    //pathSelected = listPath.get(imageSelected);
                    pathSelected=listFiles[imageSelected].getAbsolutePath();
                    fileNameSelected = name;
                    Log.d(TAG, "CC onCreate: " + pathSelected);
                }
            }
        }else{
            Log.d(TAG, "CC onActivityResult: petit problème au retour ");
        }


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
    public void onDialogPositiveClick() {
        nomChorale.setText("");
        logo.setText("");
    }

    @Override
    public void onDialogNegativeClick() {
        finish();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        int errorCode = ((StorageException) e).getErrorCode();
        String errorMessage = e.getMessage();

        Log.d(TAG, "CC onFailure: "+ errorMessage+" "+errorCode);


    }
}
