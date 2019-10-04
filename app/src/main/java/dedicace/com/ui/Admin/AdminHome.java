package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import dedicace.com.R;

//todo prévoir un dév pour les doublons en base (pour les éviter lors des créations de ss et de songs
public class AdminHome extends AppCompatActivity {

    private Button createSS, modifySS, createSong, modifySong, createUser, modifyUser, createChorale,modifyChorale, createSpectacle, createSaison, modifySpectacle, modifySaison;

    //todo ajouter les chorales et users
    //todo penser à ajouter les gestions plus complexes de doublons en local et en base...
    //todo faire le cas Admin où il n'y encore aucun SS et song de créer dans la base cloud... surement à voir dans le ChoraleNetWorkDataSource

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        createSS=findViewById(R.id.btn_create_ss);
        createSong=findViewById(R.id.btn_create_song);
        modifySS=findViewById(R.id.btn_modify_ss);
        modifySong=findViewById(R.id.btn_modify_song);
        createUser = findViewById(R.id.btn_create_user);
        modifyUser=findViewById(R.id.btn_modify_user);
        createChorale=findViewById(R.id.btn_create_chorale);
        modifyChorale=findViewById(R.id.btn_modify_chorale);
        createSpectacle= findViewById(R.id.btn_create_spectacle);
        createSaison = findViewById(R.id.btn_create_saison);
        modifySpectacle=findViewById(R.id.btn_modify_spectacle);
        modifySaison = findViewById(R.id.btn_modify_saison);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


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
                Intent startModifySongActivity = new Intent(AdminHome.this,ModifySong.class);
                startActivity(startModifySongActivity);

            }
        });

        createUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateUserActivity = new Intent(AdminHome.this,CreateUser.class);
                startActivity(startCreateUserActivity);
            }
        });

        modifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifyUsersActivity = new Intent(AdminHome.this,ModifyUser.class);
                startActivity(startModifyUsersActivity);

            }
        });

        createChorale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateChoraleActivity = new Intent(AdminHome.this,CreateChorale.class);
                startActivity(startCreateChoraleActivity);
            }
        });

        modifyChorale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifyChoraleActivity = new Intent(AdminHome.this,ModifyChorale.class);
                startModifyChoraleActivity.putExtra("origine","AdminHome");
                startActivity(startModifyChoraleActivity);
            }
        });

        createSpectacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateSpectacleActivity = new Intent(AdminHome.this,CreateSpectacle.class);
                startActivity(startCreateSpectacleActivity);

            }
        });

        createSaison.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateSaisonActivity = new Intent(AdminHome.this,CreateSaison.class);
                startActivity(startCreateSaisonActivity);
            }
        });

        modifySpectacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifySpectacleActivity = new Intent(AdminHome.this,ModifySpectacle.class);
                startModifySpectacleActivity.putExtra("origine","AdminHome");
                startActivity(startModifySpectacleActivity);
            }
        });

        modifySaison.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifySaisonActivity = new Intent(AdminHome.this,ModifySaison.class);
                startActivity(startModifySaisonActivity);
            }
        });
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
