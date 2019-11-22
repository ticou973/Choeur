package dedicace.com.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import dedicace.com.R;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean installationAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        installationAuth = sharedPreferences.getBoolean("installationAuth", true);

        new Handler().postDelayed(new Runnable() {
                                      @Override
                                      public void run() {
                                          Log.d("coucou", "SplA run: Splash ");

                                          if(installationAuth){
                                              Intent intentToSplash = new Intent(SplashActivity.this, LoginActivity.class);
                                              startActivity(intentToSplash);
                                              finish();
                                          }else{
                                              Intent intentToSplash = new Intent(SplashActivity.this, MainActivity.class);
                                              startActivity(intentToSplash);
                                              finish();
                                          }

                                      }
                                  },
                SPLASH_TIME_OUT);
    }
}
