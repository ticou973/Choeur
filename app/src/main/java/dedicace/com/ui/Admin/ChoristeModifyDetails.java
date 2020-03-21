package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import dedicace.com.R;

public class ChoristeModifyDetails extends AppCompatActivity implements DialogSuppFragment.DialogSuppListener{
    private TextView oldNom, oldPrenom,oldPupitre,oldUrl,oldRoleC, oldRoleA,oldEmail,oldAdress,oldFixe,oldPort,newUrl;
    private EditText newNom, newPrenom,newPupitre,newRoleC,newRoleA,newEmail,newAdress,newFixe,newPort;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private String oldNomStr, oldPrenomStr, oldPupitreStr,oldUrlStr,oldRoleCStr,oldRoleAStr,oldEmailStr,oldAdressStr,oldFixeStr,oldPortStr,newUrlStr;
    private String newNomStr,newPrenomStr,newPupitreStr,newRoleCStr, newRoleAStr, newEmailStr, newAdressStr,newFixeStr,newPortStr,idChoriste,idChorale;
    private static final String TAG ="coucou";

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

        selectPhoto.setOnClickListener(view -> {

        });

        suppChoriste.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(idChorale)) {
                DialogFragment dialogFragment = new DialogSuppFragment();
                dialogFragment.show(getSupportFragmentManager(),TAG);
            }else{
                Log.d(TAG, "CMD onCreate: id chorale vide");
            }

        });

        modifyChoriste.setOnClickListener(view -> {

        });

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


    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer ce choriste", Toast.LENGTH_SHORT).show();
        finish();

    }
}
