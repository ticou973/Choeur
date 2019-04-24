package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import dedicace.com.R;

public class AdminHome extends AppCompatActivity {

    private Button createSS, modifySS, createSong, modifySong;

    //todo ajouter les chorales et users
    //todo penser à ajouter les gestions plus complexes de doublons en local et en base...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        createSS=findViewById(R.id.btn_create_ss);
        createSong=findViewById(R.id.btn_create_song);
        modifySS=findViewById(R.id.btn_modify_ss);
        modifySong=findViewById(R.id.btn_modify_song);


        createSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateSSActivity = new Intent(AdminHome.this,CreateSourceSong.class);
                startActivity(startCreateSSActivity);
            }
        });

        createSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateSongActivity = new Intent(AdminHome.this,CreateSong.class);
                startActivity(startCreateSongActivity);
            }
        });

        modifySS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifySSActivity = new Intent(AdminHome.this,ModifySourceSong.class);
                startModifySSActivity.putExtra("origine","AdminHome");
                startActivity(startModifySSActivity);
            }
        });

        modifySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
