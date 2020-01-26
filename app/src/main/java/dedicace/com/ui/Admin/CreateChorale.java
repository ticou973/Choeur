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
import android.text.TextUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dedicace.com.R;

public class CreateChorale extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener, OnFailureListener {

    //Firebase
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private String idChorale, idSpectacle1, idCurrentSaison;
    private static final String TAG ="coucou";
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

        Log.d(TAG, "onCreate: idChorale "+ idChorale );

        selectLogo.setOnClickListener(view -> {
            getLists();
            selectLogo();
        });

        createChorale.setOnClickListener(view -> {

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
        });
    }

    private void insertLogosInCloudStorage() {

        fileSelected = Uri.fromFile(new File(pathSelected));
        StorageReference imageRef = mStorageRef.child("chorales/logo_titre/"+fileNameSelected);

        UploadTask uploadTask = imageRef.putFile(fileSelected);
        uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "CC onSuccess: bravo c'est uploadé"))
                .addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    // ...
                    Log.d(TAG, "CC onFailure: dommage c'est raté");
                });


        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            // Continue with the task to get the download URL
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                downloadUrl = task.getResult();
                insertChoraleInDb();
                Log.d(TAG, "CC onComplete: "+downloadUrl);
            } else {
                Log.d(TAG, "CC onComplete: Il y a eu un pb "+ Objects.requireNonNull(task.getException()).getMessage());
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
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    idChorale=documentReference.getId();

                    if(pathSelected!=null) {
                        File file = new File(pathSelected);
                        if (file.delete()) {
                            Log.d(TAG, "CC onSuccess: le fichier est supprimé du local");
                        } else {
                            Log.d(TAG, "CC onSuccess: problème de suppression en local du fichier");
                        }
                    }
                    CreateSpectacle1();
                    newChorale();
                })
                .addOnFailureListener(e -> Log.d(TAG, "CC Error adding document", e));

    }

    private void CreateSpectacle1() {

        List<String> newLieuxStr = new ArrayList<>();
        List<String> newTitreStr = new ArrayList<>();
        List<Date> newdates = new ArrayList<>();

        newLieuxStr.add("lieu 1");
        newTitreStr.add("5lRdsWgJY4wYgVA4aJaC");
        Calendar calendar = Calendar.getInstance();
        newdates.add(calendar.getTime());

        Map<String,Object> spectacle = new HashMap<>();
        spectacle.put("maj",Timestamp.now());
        spectacle.put("nom", "spectacle 1");
        spectacle.put("concerts_dates",newdates);
        spectacle.put("concerts_lieux",newLieuxStr);
        spectacle.put("id_titres",newTitreStr);

        db.collection("chorale").document(idChorale).collection("spectacles")
                .add(spectacle)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateChorale.this, "Youpi spectacle 1", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: Spectacle 1");
                    idSpectacle1 =documentReference.getId();
                    createSaison1();

                }).addOnFailureListener(e -> Log.d(TAG, "CC onSuccess: problème de création de spectacle"));
    }

    private void createSaison1() {
        Map<String,Object> saison = new HashMap<>();
        saison.put("maj", Timestamp.now());
        saison.put("nom","Saison 1");

        List<String> listIdSpectacles = new ArrayList<>();
        listIdSpectacles.add(idSpectacle1);

        saison.put("spectacles",listIdSpectacles);

        db.collection("chorale").document(idChorale).collection("saisons")
                .add(saison)
                .addOnSuccessListener(documentReference -> {
                    idCurrentSaison=documentReference.getId();
                    Log.d(TAG, "CC insertChoraleInDb: Youpi saison 1 "+idCurrentSaison);
                    Toast.makeText(this, "Youpi succès saison 1", Toast.LENGTH_SHORT).show();

                    PutCurrentSaisonInChorale();

                }).addOnFailureListener(e -> Log.d(TAG, "CC onSuccess: problème de création de saison"));
    }

    private void PutCurrentSaisonInChorale() {
        Map<String,Object> choraleSuite = new HashMap<>();
        choraleSuite.put("current_saison",idCurrentSaison);

        db.collection("chorale").document(idChorale)
                .update(choraleSuite)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "CC insertChoraleInDb: youpi current saison "+idCurrentSaison);
                    Toast.makeText(this, "Youpi current saisons ", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> Log.d(TAG, "CC onSuccess: problème de création de currentsaison"));
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

            Log.d(TAG, "CC getLists: " + Arrays.toString(listFiles));

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
