package dedicace.com.ui.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import dedicace.com.R;

//todo faire dans préférences la maj de user si il y a eu un changement du côté de l'admin
public class ModifyUserDetails extends AppCompatActivity implements DialogSuppFragment.DialogSuppListener{
    private TextView oldEmail, oldPupitre, oldNom, oldPrenom, oldRole, oldChorale, newChorale;
    private EditText newEmail, newPupitre,newNom,newPrenom,newRole;
    private Button suppUser, modifyUser, selectChorale;

    private String idUser,oldEmailStr,oldIdChoraleStr,oldNomStr,oldPrenomStr,oldPupitreStr,oldRoleStr;
    private String newIdChorale,newEmailStr,newPupitreStr,newNomStr,newPrenomStr,newRoleStr;
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;
    private Map<String,Object> user;



    private SharedPreferences sharedPreferences;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_user_details);
        oldEmail=findViewById(R.id.tv_email_user_old_modify);
        oldPupitre=findViewById(R.id.tv_pupitre_user_old);
        oldNom=findViewById(R.id.tv_name_user_old);
        oldPrenom=findViewById(R.id.tv_prenom_user_old);
        oldRole=findViewById(R.id.tv_role_user_old);
        oldChorale=findViewById(R.id.tv_chorale_user_old);
        newChorale=findViewById(R.id.tv_chorale_user_new_modify);
        newEmail =findViewById(R.id.et_email_user_new);
        newPupitre =findViewById(R.id.et_pupitre_user_new);
        newNom=findViewById(R.id.et_nom_user_new);
        newPrenom=findViewById(R.id.et_prenom_user_new);
        newRole=findViewById(R.id.et_role_user_new);
        suppUser=findViewById(R.id.btn_supp_user);
        modifyUser=findViewById(R.id.btn_modify_user);
        selectChorale=findViewById(R.id.btn_select_chorale_user_new);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        getIntentBundle();

        completeOld();

        suppUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new DialogSuppFragment();
                dialogFragment.show(getSupportFragmentManager(),TAG);
            }
        });

        selectChorale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifySSActivity = new Intent(ModifyUserDetails.this,ModifyChorale.class);
                startModifySSActivity.putExtra("origine","CreateUser");
                startActivityForResult(startModifySSActivity,REQUEST_CODE_B);
            }
        });

        modifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newEmailStr=newEmail.getText().toString();
                newPupitreStr=newPupitre.getText().toString();
                newNomStr=newNom.getText().toString();
                newPrenomStr=newPrenom.getText().toString();
                newRoleStr=newRole.getText().toString();
                newIdChorale=newChorale.getText().toString();

                if(!TextUtils.isEmpty(newEmailStr)||!TextUtils.isEmpty(newPupitreStr)||!TextUtils.isEmpty(newNomStr)||!TextUtils.isEmpty(newPrenomStr)||!TextUtils.isEmpty(newRoleStr)||!newIdChorale.equals("Select. Chorale")){
                    Log.d(TAG, "MUD onClick: conditions passées ");
                    insertUserInDb();

                }else{
                    Toast.makeText(ModifyUserDetails.this, "Il manque des éléments pour insérer", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void insertUserInDb() {
        user = new HashMap<>();
        user.put("maj", Timestamp.now());

        if(!TextUtils.isEmpty(newEmailStr)){
            user.put("email",newEmailStr);
        }
        if(!TextUtils.isEmpty(newPupitreStr)){
            user.put("pupitre",newPupitreStr);
        }
        if(!TextUtils.isEmpty(newNomStr)){
            user.put("nom",newNomStr);
        }
        if(!TextUtils.isEmpty(newPrenomStr)){
            user.put("prenom",newPrenomStr);
        }
        if(!TextUtils.isEmpty(newRoleStr)){
            user.put("role",newRoleStr);
        }
        if(!newIdChorale.equals("Select. Chorale")){
            user.put("id_chorale",newIdChorale);
        }

        insertCloud();
    }

    private void insertCloud() {
        Log.d(TAG, "MUD insertCloud: ");
        db.collection("users").document(idUser)
                .update(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "MUD onComplete: upadate user done ");

                        Intent startMU = new Intent(ModifyUserDetails.this,ModifyUser.class);
                        startMU.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startMU.putExtra("origine","AdminHome");
                        startActivity(startMU);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_B) {
            Log.d(TAG, "MUD onActivityResult: request_codeB");
            newIdChorale = data.getStringExtra("idselected");
            newChorale.setText(newIdChorale);
            Log.d(TAG, "MUD onActivityResult: request_codeB " + newIdChorale);

        } else {
            Log.d(TAG, "MUD onActivityResult: petit problème au retour ");
        }
    }


    private void completeOld() {
        oldEmail.setText(oldEmailStr);
        oldPupitre.setText(oldPupitreStr);
        oldNom.setText(oldNomStr);
        oldPrenom.setText(oldPrenomStr);
        oldRole.setText(oldRoleStr);
        oldChorale.setText(oldIdChoraleStr);
    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleUser");
        idUser=args.getString("idUser");
        oldEmailStr=args.getString("oldEmail");
        oldIdChoraleStr=args.getString("oldIdChorale");
        oldNomStr=args.getString("oldNom");
        oldPrenomStr=args.getString("oldPrenom");
        oldPupitreStr=args.getString("oldPupitre");
        oldRoleStr=args.getString("oldRole");
    }

    @Override
    public void onDialogSuppPositiveClick() {
        Log.d(TAG, "MUD onDialogSuppPositiveClick: ");
        deleteUserInAuth();
        suppUsers();
        Intent startModifyUserActivity = new Intent(ModifyUserDetails.this,ModifyUser.class);
        startModifyUserActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(startModifyUserActivity);
    }

    private void deleteUserInAuth() {
       //todo voir comment le gérer programatically voir Admin https://firebase.google.com/docs/auth/admin/manage-users
    }

    private void suppUsers() {
        db.collection("users").document(idUser)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "MUD onSuccess: réussi user supprimé !");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MUD onFailure: user pas supprimé");
                    }
                });
    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer ce User", Toast.LENGTH_SHORT).show();
        finish();

    }
}
