package dedicace.com.ui.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import dedicace.com.R;

public class CreateUser extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    //Firebase
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public static String current_user_id;
    private String idChorale;
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;

    private TextView selectChorale;
    private EditText nom,prenom,email;
    private Button selectChoraleBtn,createUserBtn;
    private RadioGroup rgb;
    private RadioButton choristeRb, adminRb,superAdminRb;


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
        rgb=findViewById(R.id.rgb_role_user);
        choristeRb=findViewById(R.id.rb_choriste);
        adminRb=findViewById(R.id.rb_admin);
        superAdminRb=findViewById(R.id.rb_super_admin);
        choristeRb.setChecked(true);

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



        selectChoraleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Intent startModifySSActivity = new Intent(CreateSong.this,ModifySourceSong.class);
               // startModifySSActivity.putExtra("origine","CreateSong");
               // startActivityForResult(startModifySSActivity,REQUEST_CODE_B);
            }
        });

        createUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


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
