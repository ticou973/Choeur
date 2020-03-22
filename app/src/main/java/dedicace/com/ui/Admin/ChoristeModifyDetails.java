package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import dedicace.com.R;

public class ChoristeModifyDetails extends AppCompatActivity implements DialogSuppFragment.DialogSuppListener{
    private TextView oldNom, oldPrenom,oldPupitre,oldUrl,oldRoleC, oldRoleA,oldEmail,oldAdress,oldFixe,oldPort,newUrl;
    private EditText newNom, newPrenom,newPupitre,newRoleC,newRoleA,newEmail,newAdress,newFixe,newPort;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private String oldNomStr, oldPrenomStr, oldPupitreStr,oldUrlStr,oldRoleCStr,oldRoleAStr,oldEmailStr,oldAdressStr,oldFixeStr,oldPortStr,newUrlStr;
    private String newNomStr,newPrenomStr,newPupitreStr,newRoleCStr, newRoleAStr, newEmailStr, newAdressStr,newFixeStr,newPortStr,idChoriste,idChorale,nomChoraleStr,origine;
    private static final String TAG ="coucou";
    private final static int REQUEST_CODE=100;
    private String[] listImages;
    private String pathSelected;
    private String fileNameSelected;
    private File[] listFiles;
    private int imageSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choriste_modify_details);

        oldNom=findViewById(R.id.tv_nom_old);
        oldPrenom=findViewById(R.id.tv_prenom_choriste_old);
        oldPupitre=findViewById(R.id.tv_pupitre_choriste_old);
        oldUrl=findViewById(R.id.tv_url_old);
        oldRoleC=findViewById(R.id.tv_rolec_old);
        oldRoleA=findViewById(R.id.tv_rolea_old);
        oldEmail=findViewById(R.id.tv_email_choriste_modify_old);
        oldAdress=findViewById(R.id.tv_adresse_modify_choriste);
        oldFixe=findViewById(R.id.tv_fixe_old);
        oldPort=findViewById(R.id.tv_port_old);
        newNom=findViewById(R.id.et_nom_new);
        newPrenom=findViewById(R.id.et_prenom_new);
        newPupitre=findViewById(R.id.et_pupitre_choriste_new);
        newRoleC=findViewById(R.id.et_rolec_new);
        newRoleA=findViewById(R.id.et_rolea_new);
        newEmail=findViewById(R.id.et_email_modify_new);
        newAdress=findViewById(R.id.et_adresse_new_modify);
        newFixe=findViewById(R.id.et_fixe_new);
        newPort=findViewById(R.id.et_port_new);
        newUrl=findViewById(R.id.tv_photo_new);

        Button selectPhoto = findViewById(R.id.btn_select_new_photo);
        Button suppChoriste = findViewById(R.id.btn_supp_choriste);
        Button modifyChoriste = findViewById(R.id.btn_modify_choriste_modify);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getIntentBundle();

        completeOld();



        suppChoriste.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(idChorale)) {
                DialogFragment dialogFragment = new DialogSuppFragment();
                dialogFragment.show(getSupportFragmentManager(),TAG);
            }else{
                Log.d(TAG, "CMD onCreate: id chorale vide");
            }

        });

        selectPhoto.setOnClickListener(view -> {
            getLists();
            selectPhoto();

        });


        modifyChoriste.setOnClickListener(view -> {

        });

    }

    private void selectPhoto() {
        Log.d(TAG, "CMD selectPhotos : ");
        Intent startChoosePhotosActivity = new Intent(ChoristeModifyDetails.this,ChooseBackground.class);
        startChoosePhotosActivity.putExtra("listimages",listImages);
        startActivityForResult(startChoosePhotosActivity,REQUEST_CODE);
    }

    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/Photos_Choristes");

        if(file.mkdirs()){
            Log.d(TAG, "CMD insertBackgroundInCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "CMD insertBackgroundInCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()){
            listFiles = file.listFiles();

            Log.d(TAG, "CMD selectPhotos "+" "+listFiles.length);

            listImages = new String[listFiles.length];

            for (int i = 0; i < listFiles.length; i++) {
                listImages[i]=listFiles[i].getName();
                Log.d(TAG, "CMD selectPhotos: "+listFiles[i].getName()+listImages[i]);
            }
        }

        Log.d(TAG, "CMD : selectPhotos: "+ listImages.length);

    }

    private void completeOld() {
        oldNom.setText(oldNomStr);
        oldPrenom.setText(oldPrenomStr);
        oldPupitre.setText(oldPupitreStr);
        oldUrl.setText(oldUrlStr);
        oldRoleC.setText(oldRoleCStr);
        oldRoleA.setText(oldRoleAStr);
        oldEmail.setText(oldEmailStr);
        oldAdress.setText(oldAdressStr);
        oldFixe.setText(oldFixeStr);
        oldPort.setText(oldPortStr);
    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleChoriste");
        oldNomStr=args.getString("nom_choriste");
        oldPrenomStr=args.getString("prenom_choriste");
        oldPupitreStr=args.getString("pupitre");
        oldRoleCStr=args.getString("role_choeur");
        oldRoleAStr=args.getString("role_admin");
        oldEmailStr=args.getString("email");
        oldAdressStr=args.getString("adresse");
        oldFixeStr=args.getString("tel_fixe");
        oldPortStr=args.getString("tel_port");
        oldUrlStr=args.getString("url_photo");
        idChoriste=args.getString("idChoriste");
        idChorale=args.getString("idChorale");
        nomChoraleStr=args.getString("nomChorale");
        origine=args.getString("origine");
        Log.d(TAG, "CMD getIntentBundle: "+oldNomStr+" "+oldPrenomStr+" "+oldPupitreStr+" "+oldRoleCStr+" "+oldRoleAStr+" "+oldEmailStr+" "+oldAdressStr+" "+oldFixeStr+" "+oldPortStr+" "+oldUrlStr+" "+idChorale+ " "+idChoriste);
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
    public void onDialogSuppPositiveClick() {
        if(!TextUtils.isEmpty(idChorale)) {
            modifyMajChorale();
            suppChoristes();
            Bundle args = new Bundle();
            args.putString("idChorale",idChorale);
            args.putString("nomChorale",nomChoraleStr);
            args.putString("origine",origine);

            Intent startModifyChoristeActivity = new Intent(ChoristeModifyDetails.this,ModifyChoriste.class);
            startModifyChoristeActivity.putExtra("bundleChorale",args);
            startModifyChoristeActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(startModifyChoristeActivity);

        }else{
            Log.d(TAG, "MSSD onCreate: id chorale vide");
            Toast.makeText(this, "Il faut préciser la Chorale !", Toast.LENGTH_SHORT).show();
        }


    }

    private void suppChoristes() {
        try{
            db.collection("chorale").document(idChorale).collection("choristes").document(idChoriste)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "CMD onSuccess: réussi choriste supprimé"))
                    .addOnFailureListener(e -> Log.d(TAG, "CMD onFailure: Chorsite pas supprimé"));

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void modifyMajChorale() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj", Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "CMD onSuccess: maj chorale done"))
                .addOnFailureListener(e -> Log.d(TAG, "CMD onSuccess: maj chorale failed"));
    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer ce choriste", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Log.d(TAG, "CMD onActivityResult: ok cela marche");
            if(requestCode==REQUEST_CODE){

                if (data != null) {
                    imageSelected = data.getIntExtra("imageselected",-1);
                }

                if(imageSelected!=-1) {
                    String name = listImages[imageSelected];
                    newUrl.setText(name);
                    pathSelected=listFiles[imageSelected].getAbsolutePath();
                    fileNameSelected = name;
                    Log.d(TAG, "MSS onCreate: " + pathSelected);
                }
            }
        }else{
            Log.d(TAG, "MSSD onActivityResult: petit problème au retour ");
        }
    }
}
