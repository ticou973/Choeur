package dedicace.com.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dedicace.com.R;

public class LoginActivity extends AppCompatActivity {

    //ui
    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private ProgressBar loginProgress;

    //Firebase
    private FirebaseAuth mAuth;
    public static String current_user_id;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    //todo gérer si 2 personnes se loggent sur un même tel
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mAuth = FirebaseAuth.getInstance();

        loginEmailText = findViewById(R.id.reg_email);
        loginPassText = findViewById(R.id.reg_confirm_pass);
        loginBtn = findViewById(R.id.login_btn);
        loginProgress = findViewById(R.id.login_progress);
        Log.d("coucou", "LA Login onCreate: ");


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("coucou", "LA onClick: ");

                String loginEmail = loginEmailText.getText().toString();
                String loginPass = loginPassText.getText().toString();
                Log.d("coucou", "LA onClick: "+loginEmail+" "+loginPass);

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){
                    loginProgress.setVisibility(View.VISIBLE);
                    Log.d("coucou", "LA onClick: go Auth");

                    mAuth.signInWithEmailAndPassword(loginEmail, loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                current_user_id = currentUser.getUid();
                                Log.d("coucou", "LA onComplete Login: "+current_user_id);

                                sendToMain();

                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("coucou", "LA onStart: "+currentUser);
        if(currentUser != null){
            Log.d("coucou", "LA onStart: non null");
            sendToMain();
        }else{
            Log.d("coucou", "LA onStart: current null");
        }
    }

    private void sendToMain() {
        editor = sharedPreferences.edit();
        editor.putBoolean("installationAuth", false);
        editor.apply();
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
