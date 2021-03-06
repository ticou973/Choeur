package dedicace.com.ui.PlaySong;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import dedicace.com.R;

public class LoginActivity extends AppCompatActivity {

    //ui
    private EditText loginEmailText;
    private EditText loginPassText;
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
        loginEmailText = findViewById(R.id.reg_email);
        loginPassText = findViewById(R.id.reg_confirm_pass);
        Button loginBtn = findViewById(R.id.login_btn);
        loginProgress = findViewById(R.id.login_progress);
        Log.d("coucou", "LA Login onCreate: ");

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){
            Log.d("coucou", "LA onCreate: current user non null");
            mAuth.signOut();
            mAuth = FirebaseAuth.getInstance();
        }

        Log.d("coucou", "LA onCreate: pas installationAuth "+ mAuth);
        loginBtn.setOnClickListener(v -> {
            Log.d("coucou", "LA onClick: ");

            String loginEmail = loginEmailText.getText().toString();
            String loginPass = loginPassText.getText().toString();

            editor = sharedPreferences.edit();
            editor.putString("loginEmail",loginEmail);
            editor.putString("loginMdp",loginPass);
            editor.apply();

            Log.d("coucou", "LA onClick: "+loginEmail+" "+loginPass);

            if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){
                loginProgress.setVisibility(View.VISIBLE);
                Log.d("coucou", "LA onClick: go Auth");

                signIn(loginEmail, loginPass);

            }
        });
    }

    private void signIn(String loginEmail, String loginPass) {
        mAuth.signInWithEmailAndPassword(loginEmail, loginPass).addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                Log.d("coucou", "LA onComplete: signIn réussi ");
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                current_user_id = Objects.requireNonNull(currentUser).getUid();
                Log.d("coucou", "LA onComplete Login: "+current_user_id);
                editor = sharedPreferences.edit();
                editor.putString("userId", current_user_id);
                editor.apply();
                sendToMain();

            } else {
                String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                Toast.makeText(LoginActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
            }
            loginProgress.setVisibility(View.INVISIBLE);
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
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
