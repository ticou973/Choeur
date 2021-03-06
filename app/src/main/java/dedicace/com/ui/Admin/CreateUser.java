package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import dedicace.com.R;

//todo faire un process complet pour envoi d'email et vérification
public class CreateUser extends AppCompatActivity implements DialogNewSSFragment.DialogNewSSListener{

    //Firebase
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public static String current_user_id;
    private String idChorale,emailStr,pwdStr,nomStr,prenomStr,roleStr,userId, pupitreStr;
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;

    private TextView selectChorale;
    private EditText nom,prenom,email,pwd;
    private Button selectChoraleBtn,createUserBtn;
    private RadioGroup rgb,rgbPupitre;
    private RadioButton choristeRb, adminRb,superAdminRb,tuttirb,bassRb,tenorRb,altoRb,sopranoRb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        selectChorale = findViewById(R.id.tv_select_chorale);
        nom=findViewById(R.id.et_name_user);
        prenom=findViewById(R.id.et_prenom_user);
        email=findViewById(R.id.et_email_user);
        selectChoraleBtn=findViewById(R.id.btn_select_chorale);
        createUserBtn=findViewById(R.id.btn_create_user);
        pwd=findViewById(R.id.et_pwd_user);
        rgb=findViewById(R.id.rgb_role_user);
        choristeRb=findViewById(R.id.rb_choriste);
        adminRb=findViewById(R.id.rb_admin);
        superAdminRb=findViewById(R.id.rb_super_admin);
        choristeRb.setChecked(true);
        rgbPupitre=findViewById(R.id.rb_user_pupitre);
        tuttirb=findViewById(R.id.rb_user_tutti);
        bassRb=findViewById(R.id.rb_user_bass);
        tenorRb=findViewById(R.id.rb_user_tenor);
        altoRb=findViewById(R.id.rb_user_alto);
        sopranoRb=findViewById(R.id.rb_user_soprano);
        tuttirb.setChecked(true);


        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        selectChoraleBtn.setOnClickListener(view -> {
            Intent startModifySSActivity = new Intent(CreateUser.this,ModifyChorale.class);
            startModifySSActivity.putExtra("origine","CreateUser");
            startActivityForResult(startModifySSActivity,REQUEST_CODE_B);
        });

        createUserBtn.setOnClickListener(view -> {
            //obligatoire
            idChorale=selectChorale.getText().toString();
            emailStr = email.getText().toString();
            pwdStr=pwd.getText().toString();
            nomStr=nom.getText().toString();
            prenomStr=prenom.getText().toString();

            int idRb = rgb.getCheckedRadioButtonId();

            switch (idRb){
                case R.id.rb_choriste:
                    roleStr="Choriste";
                    break;
                case R.id.rb_admin:
                    roleStr="Admin";
                    break;
                case R.id.rb_super_admin:
                    roleStr="Super Admin";
                    break;
            }

            int idRbPupitre = rgbPupitre.getCheckedRadioButtonId();
            switch (idRbPupitre){
                case R.id.rb_user_tutti:
                    pupitreStr="TUTTI";
                    break;
                case R.id.rb_user_bass:
                    pupitreStr="BASS";
                    break;
                case R.id.rb_user_tenor:
                    pupitreStr="TENOR";
                    break;
                case R.id.rb_user_alto:
                    pupitreStr="ALTO";
                    break;
                case R.id.rb_user_soprano:
                    pupitreStr="SOPRANO";
                    break;
            }

            Log.d(TAG, "CU onClick: role "+roleStr);

            if(!idChorale.equals("Selection Chorale...")&&!TextUtils.isEmpty(emailStr)&&!TextUtils.isEmpty(pwdStr)&&!TextUtils.isEmpty(nomStr)&&!TextUtils.isEmpty(prenomStr)){
                Log.d(TAG, "CU onClick: conditions passées "+ idChorale+ " "+emailStr+" "+pwdStr+" "+nomStr+" "+prenomStr+" "+roleStr+" "+pupitreStr);

                insertUserinAuth();

            }else{
                Toast.makeText(CreateUser.this, "Il manque des éléments", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void insertUserinAuth() {

        mAuth.createUserWithEmailAndPassword(emailStr, pwdStr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "CU createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userId=user.getUid();
                            InsertUserInDb();
                        }else{
                            Log.d(TAG, "CU onComplete: pb de Iduser");
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "CU createUserWithEmail:failure", task.getException());
                        Toast.makeText(CreateUser.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void InsertUserInDb() {

        Map<String,Object> user = new HashMap<>();
        user.put("email",emailStr);
        user.put("id_chorale",idChorale);
        user.put("nom",nomStr);
        user.put("prenom",prenomStr);
        user.put("role",roleStr);
        user.put("url_photo","");
        user.put("pupitre",pupitreStr);
        user.put("maj", Timestamp.now());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "CU DocumentSnapshot added with ID: ");
                    newUser();
                }).addOnFailureListener(e -> Log.d(TAG, "CU Error adding document", e));
    }

    private void newUser() {
        DialogFragment dialog = new DialogNewSSFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_B) {
            Log.d(TAG, "CU onActivityResult: request_codeB");

            idChorale = data.getStringExtra("idselected");
            selectChorale.setText(idChorale);
            Log.d(TAG, "CU onActivityResult: request_codeB " + idChorale);

        } else {
            Log.d(TAG, "CU onActivityResult: petit problème au retour ");
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
        selectChorale.setText("Selection Chorale...");
        choristeRb.setChecked(true);
        email.setText("");
        pwd.setText("");
        nom.setText("");
        prenom.setText("");
    }

    @Override
    public void onDialogNegativeClick() {
        finish();
    }
}
