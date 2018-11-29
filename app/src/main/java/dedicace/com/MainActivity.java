package dedicace.com;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongsAdapter songsAdapter;

    private static int SPLASH_TIME_OUT = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intentToSplash = new Intent(MainActivity.this,SplashActivity.class);
                startActivity(intentToSplash);
                finish();
            }
             },
                SPLASH_TIME_OUT);

        recyclerView = findViewById(R.id.recyclerview_media_item);




    }
}
