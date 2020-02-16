package dedicace.com.ui.Admin;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dedicace.com.R;

public class CreateChoristeUnit extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener, OnFailureListener {

    TextView tvNomChorale, tvUrlPhoto;
    EditText etNomChoriste, etPrenomChoriste, etMailChoriste, etTelFixe, etTelPort, etRue, etVille,etCodePostal;
    private static final int REQUEST_CODE_B = 200;
    private static final int REQUEST_CODE_A = 100;
    private static final String TAG ="coucou";
    private String idChorale;
    private File[] listFiles;
    private String[] listImages;
    private int imageSelected;
    private String pathSelected;
    private String fileNameSelected;
    private String nomChorale, nomChoriste, prenomChoriste, mailChoriste, telFixeChoriste, telPortChoriste, rueChoriste, villeChoriste, zipChoriste, roleChoeurStr, roleAdminStr, pupitreStr;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Uri downloadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_choriste_unit);
        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        tvNomChorale = findViewById(R.id.tv_nom_chorale_create_choriste);
        tvUrlPhoto = findViewById(R.id.tv_url_photo_choriste);
        etNomChoriste = findViewById(R.id.et_nom_choriste_create_choriste);
        etPrenomChoriste = findViewById(R.id.et_prenom_choriste_create_choriste);
        etMailChoriste = findViewById(R.id.et_create_choriste_email);
        etTelFixe = findViewById(R.id.et_tel_fixe_choriste);
        etTelPort = findViewById(R.id.et_tel_port_choriste);
        etRue = findViewById(R.id.et_adresse_choriste);
        etVille = findViewById(R.id.et_ville_choriste);
        etCodePostal = findViewById(R.id.et_zip_code_choriste);
        Button btnNomChorale = findViewById(R.id.btn_select_chorale_create_choriste);
        Button btnUrlPhoto = findViewById(R.id.btn_select_photo_choriste);
        Button btnCreateChoriste = findViewById(R.id.btn_create_choriste_cloud);
        RadioGroup rgChoriste = findViewById(R.id.rg_choriste_pupitre);
        RadioGroup rgChoeur = findViewById(R.id.rg_role_choeur);
        RadioGroup rgAdmin = findViewById(R.id.rg_role_admin);
        RadioButton rbChoristeAucun = findViewById(R.id.rb_choriste_aucun);
        rbChoristeAucun.setChecked(true);
        RadioButton rbChoeurChoriste = findViewById(R.id.rb_role_choeur_choriste);
        rbChoeurChoriste.setChecked(true);
        RadioButton rbAdminAucun = findViewById(R.id.rb_role_admin_aucun);
        rbAdminAucun.setChecked(true);


        btnNomChorale.setOnClickListener(view -> {
            Intent startModifyChoraleActivity = new Intent(CreateChoristeUnit.this,ModifyChorale.class);
            startModifyChoraleActivity.putExtra("origine","CreateChoristeUnit");
            startActivityForResult(startModifyChoraleActivity,REQUEST_CODE_A);
        });

        btnUrlPhoto.setOnClickListener(view -> {
            getLists();
            selectPhoto();
        });

        btnCreateChoriste.setOnClickListener(view -> {
            nomChoriste = etNomChoriste.getText().toString();
            prenomChoriste = etPrenomChoriste.getText().toString();
            telFixeChoriste =etTelFixe.getText().toString();
            telPortChoriste =etTelPort.getText().toString();
            rueChoriste=etRue.getText().toString();
            villeChoriste=etVille.getText().toString();
            zipChoriste=etCodePostal.getText().toString();
            mailChoriste =etMailChoriste.getText().toString();

            int idRbChoeur = rgChoeur.getCheckedRadioButtonId();
            switch (idRbChoeur){
                case R.id.rb_role_choeur_choriste:
                    roleChoeurStr="Choriste";
                    break;
                case R.id.rb_role_choeur_chef_choeur:
                    roleChoeurStr="Chef de Choeur";
                    break;
                case R.id.rb_role_choeur_musicien:
                    roleChoeurStr="Musicien";
                    break;
            }

            int idRbAdmin = rgAdmin.getCheckedRadioButtonId();
            switch (idRbAdmin){
                case R.id.rb_role_admin_admin:
                    roleAdminStr="Administrateur";
                    break;
                case R.id.rb_role_admin_aucun:
                    roleAdminStr="Aucun";
                    break;
                case R.id.rb_role_admin_president:
                    roleAdminStr="Président";
                    break;
                case R.id.rb_role_admin_tresorier:
                    roleAdminStr="Trésorier";
                    break;
                case R.id.rb_role_admin_secretaire:
                    roleAdminStr="Secrétaire";
                    break;
            }

            int idRbPupitre = rgChoriste.getCheckedRadioButtonId();
            switch (idRbPupitre){
                case R.id.rb_choriste_aucun:
                    pupitreStr="Aucun";
                    break;
                case R.id.rb_choriste_basse:
                    pupitreStr="BASS";
                    break;
                case R.id.rb_choriste_tenor:
                    pupitreStr="TENOR";
                    break;
                case R.id.rb_choriste_alto:
                    pupitreStr="ALTO";
                    break;
                case R.id.rb_choriste_soprano:
                    pupitreStr="SOPRANO";
                    break;
            }


            if(!TextUtils.isEmpty(idChorale)&&!TextUtils.isEmpty(nomChoriste)&&!TextUtils.isEmpty(prenomChoriste)){
                Log.d(TAG, "CCU onCreate: conditions réunies pour create Choristes ! ");
                insertPhotoInCloudStorage();

            }else{
                Log.d(TAG, "CCU onCreate: il manque des éléments");
                Toast.makeText(this, "Il manque des éléments !", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void insertPhotoInCloudStorage() {

        Uri fileSelected = Uri.fromFile(new File(pathSelected));
        StorageReference imageRef = mStorageRef.child("songs/photos_choristes/"+fileNameSelected);

        UploadTask uploadTask = imageRef.putFile(fileSelected);

        uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "CSU onSuccess: bravo c'est uploadé"))
                .addOnFailureListener(exception -> Log.d(TAG, "CSU onFailure: dommage c'est raté"));


        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }

            // Continue with the task to get the download URL
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                downloadUrl = task.getResult();
                insertChoristeinDb();
                Log.d(TAG, "CCU onComplete: "+downloadUrl);
            } else {
                Log.d(TAG, "CCU onComplete: Il y a eu un pb "+ Objects.requireNonNull(task.getException()).getMessage());
            }
        });

    }

    private void insertChoristeinDb() {
        List<String> adresse = new ArrayList<>();
        adresse.add(rueChoriste);
        adresse.add(villeChoriste);
        adresse.add(zipChoriste);


        Map<String,Object> choriste = new HashMap<>();
        choriste.put("nom_choriste",nomChoriste);
        choriste.put("prenom_choriste",prenomChoriste);
        choriste.put("url_photo",downloadUrl.toString());
        choriste.put("pupitre",pupitreStr);
        choriste.put("role_choeur",roleChoeurStr);
        choriste.put("role_admin",roleAdminStr);
        choriste.put("email",mailChoriste);
        choriste.put("tel_fixe",telFixeChoriste);
        choriste.put("tel_port",telPortChoriste);
        choriste.put("adresse",adresse);
        choriste.put("maj", Timestamp.now());

        db.collection("chorale").document(idChorale).collection("choristes")
                .add(choriste)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "CCU DocumentSnapshot added with ID: " + documentReference.getId());
                    File file = new File(pathSelected);
                    if(file.delete()){
                        Log.d(TAG, "CCU onSuccess: le fichier est supprimé du local");
                    }else{
                        Log.d(TAG, "CCU onSuccess: problème de suppression en local du fichier");
                    }

                    majChoraleTrombi();

                    newChoriste();

                }).addOnFailureListener(e -> Log.d(TAG, "CCU Error adding document", e));
    }

    private void majChoraleTrombi() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj_trombi",Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "CSU onSuccess: maj chorale done"))
                .addOnFailureListener(e -> Log.d(TAG, "CSU onSuccess: maj chorale failed"));
    }

    private void newChoriste() {
        DialogFragment dialog = new DialogNewSSFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
    }

    @Override
    public void onDialogPositiveClick() {
        etNomChoriste.setText("");
        etPrenomChoriste.setText("");
        etCodePostal.setText("");
        etVille.setText("");
        etTelPort.setText("");
        etTelFixe.setText("");
        etMailChoriste.setText("");
        etRue.setText("");
    }

    @Override
    public void onDialogNegativeClick() {
        finish();
    }

    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/Photos_Choristes");

        if(file.mkdirs()){
            Log.d(TAG, "CCU insertPhotoInCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "CCU insertPhotoInCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()) {
            listFiles = file.listFiles();

            Log.d(TAG, "CCU getLists: " + Arrays.toString(listFiles));

            if (listFiles != null && listFiles.length != 0) {

                Log.d(TAG, "CCU selectBackground: " + " " + listFiles.length);

                listImages = new String[listFiles.length];

                for (int i = 0; i < listFiles.length; i++) {
                    listImages[i] = listFiles[i].getName();

                    Log.d(TAG, "CCU selectBackground: " + listFiles[i].getName());
                }
            }else{
                Log.d(TAG, "CCU getLists: pas de listFiles ");
            }
        }
    }

    private void selectPhoto() {
        Log.d(TAG, "CSS selectBackground: ");
        Intent startChooseBackgroundActivity = new Intent(CreateChoristeUnit.this,ChoosePhoto.class);
        startChooseBackgroundActivity.putExtra("listimages",listImages);
        startActivityForResult(startChooseBackgroundActivity,REQUEST_CODE_B);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_A) {
            Log.d(TAG, "CCU onActivityResult: request_codeB");

            idChorale = data.getStringExtra("idselected");
            nomChorale = data.getStringExtra("nomChorale");
            tvNomChorale.setText(nomChorale);
            Log.d(TAG, "CCU onActivityResult: request_codeB " + idChorale);

        } else if(requestCode==REQUEST_CODE_B){

            if (data != null) {
                imageSelected = data.getIntExtra("imageselected",-1);
                Log.d(TAG, "CS onActivityResult: "+imageSelected);
            }

            if(imageSelected!=-1) {
                String name = listImages[imageSelected];
                tvUrlPhoto.setText(name);
                //pathSelected = listPath.get(imageSelected);
                pathSelected=listFiles[imageSelected].getAbsolutePath();
                fileNameSelected = name;
                Log.d(TAG, "CSS onCreate: " + pathSelected);
            }
        }else{
            Log.d(TAG, "CCU onActivityResult: petit problème au retour ");
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        int errorCode = ((StorageException) e).getErrorCode();
        String errorMessage = e.getMessage();

        Log.d(TAG, "CCU onFailure: "+ errorMessage+" "+errorCode);
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
