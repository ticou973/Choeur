package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import dedicace.com.R;

//todo prévoir un dév pour les doublons en base (pour les éviter lors des créations de ss et de songs
public class AdminHome extends AppCompatActivity {

    //todo ajouter les chorales et users
    //todo penser à ajouter les gestions plus complexes de doublons en local et en base...
    //todo faire le cas Admin où il n'y encore aucun SS et song de créer dans la base cloud... surement à voir dans le ChoraleNetWorkDataSource

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Button createSS = findViewById(R.id.btn_create_ss);
        Button createSong = findViewById(R.id.btn_create_song);
        Button modifySS = findViewById(R.id.btn_modify_ss);
        Button modifySong = findViewById(R.id.btn_modify_song);
        Button createUser = findViewById(R.id.btn_create_user);
        Button modifyUser = findViewById(R.id.btn_modify_user);
        Button createChorale = findViewById(R.id.btn_create_chorale);
        Button modifyChorale = findViewById(R.id.btn_modify_chorale);
        Button createSpectacle = findViewById(R.id.btn_create_spectacle);
        Button createSaison = findViewById(R.id.btn_create_saison);
        Button modifySpectacle = findViewById(R.id.btn_modify_spectacle);
        Button modifySaison = findViewById(R.id.btn_modify_saison);
        Button createChoriste = findViewById(R.id.btn_create_choriste);
        Button modifyChoriste = findViewById(R.id.btn_modify_choriste);


        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        createSS.setOnClickListener(view -> {
            Intent startCreateSSActivity = new Intent(AdminHome.this,CreateSourceSong.class);
            startActivity(startCreateSSActivity);
        });

        createSong.setOnClickListener(view -> {
            Intent startCreateSongActivity = new Intent(AdminHome.this,CreateSong.class);
            startActivity(startCreateSongActivity);
        });

        modifySS.setOnClickListener(view -> {
            Intent startModifySSActivity = new Intent(AdminHome.this,ModifySourceSong.class);
            startModifySSActivity.putExtra("origine","AdminHome");
            startActivity(startModifySSActivity);
        });

        modifySong.setOnClickListener(view -> {
            Intent startModifySongActivity = new Intent(AdminHome.this,ModifySong.class);
            startActivity(startModifySongActivity);

        });

        createUser.setOnClickListener(view -> {
            Intent startCreateUserActivity = new Intent(AdminHome.this,CreateUser.class);
            startActivity(startCreateUserActivity);
        });

        modifyUser.setOnClickListener(view -> {
            Intent startModifyUsersActivity = new Intent(AdminHome.this,ModifyUser.class);
            startActivity(startModifyUsersActivity);

        });

        createChorale.setOnClickListener(view -> {
            Intent startCreateChoraleActivity = new Intent(AdminHome.this,CreateChorale.class);
            startActivity(startCreateChoraleActivity);
        });

        modifyChorale.setOnClickListener(view -> {
            Intent startModifyChoraleActivity = new Intent(AdminHome.this,ModifyChorale.class);
            startModifyChoraleActivity.putExtra("origine","AdminHome");
            startActivity(startModifyChoraleActivity);
        });

        createSpectacle.setOnClickListener(view -> {
            Intent startCreateSpectacleActivity = new Intent(AdminHome.this,CreateSpectacle.class);
            startActivity(startCreateSpectacleActivity);

        });

        createSaison.setOnClickListener(view -> {
            Log.d("coucou", "AH onClick: createSaison");
            Intent startCreateSaisonActivity = new Intent(AdminHome.this,CreateSaison.class);
            startActivity(startCreateSaisonActivity);
        });

        modifySpectacle.setOnClickListener(view -> {
            Intent startModifySpectacleActivity = new Intent(AdminHome.this,ChooseChorale.class);
            startModifySpectacleActivity.putExtra("origine","AdminHomeModifSpectacle");
            startActivity(startModifySpectacleActivity);
        });

        modifySaison.setOnClickListener(view -> {
            Intent startModifySaisonActivity = new Intent(AdminHome.this,ChooseChorale.class);
            startModifySaisonActivity.putExtra("origine","AdminHomeModifSaison");
            startActivity(startModifySaisonActivity);
        });

        createChoriste.setOnClickListener(view -> {
            Intent startCreateChoriste = new Intent(AdminHome.this,CreateChoristeMode.class);
            startActivity(startCreateChoriste);

        });

        modifyChoriste.setOnClickListener(view -> {

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
