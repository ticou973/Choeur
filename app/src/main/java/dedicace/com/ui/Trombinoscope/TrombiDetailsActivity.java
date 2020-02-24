package dedicace.com.ui.Trombinoscope;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import dedicace.com.R;
import dedicace.com.ui.PlaySong.GlideApp;

public class TrombiDetailsActivity extends AppCompatActivity {
    String nom, prenom, pupitre, adresse, telFixe, telPort, email, roleChoeur, roleAdmin, urlPhoto;
    TextView tvNom, tvPupitre, tvAdresse, tvEmail, tvTelFixe, tvTelPort, tvRoleChoeur, tvRoleAdmin;
    ImageView imgChoriste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trombi_details);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tvNom = findViewById(R.id.tv_nom_choriste_details);
        tvRoleChoeur = findViewById(R.id.tv_choriste_role_choeur);
        tvRoleAdmin = findViewById(R.id.tv_choriste_role_admin);
        tvPupitre = findViewById(R.id.tv_pupitre_details);
        tvAdresse = findViewById(R.id.tv_choriste_adresse_details);
        tvEmail = findViewById(R.id.tv_email_choriste_details);
        tvTelFixe = findViewById(R.id.tv_tel_fixe_choriste_details);
        tvTelPort = findViewById(R.id.tv_tel_port_choriste_details);
        imgChoriste = findViewById(R.id.img_photo_destails);

        Intent intent = getIntent();
        nom = intent.getStringExtra("nom");
        prenom = intent.getStringExtra("prenom");
        pupitre = intent.getStringExtra("pupitre");
        roleChoeur =intent.getStringExtra("role_choeur");
        roleAdmin = intent.getStringExtra("role_admin");
        email = intent.getStringExtra("email");
        telFixe = intent.getStringExtra("tel_fixe");
        telPort = intent.getStringExtra("tel_port");
        adresse = intent.getStringExtra("adresse");
        urlPhoto = intent.getStringExtra("url_photo");


        GlideApp.with(this)
                .load(urlPhoto)
                .centerCrop() // scale to fill the ImageView and crop any extra
                .into(imgChoriste);

        String nomPrenom = nom + " "+ prenom;
        tvNom.setText(nomPrenom);

        if(pupitre.equals("Aucun")){
            tvRoleChoeur.setText(roleChoeur);
            tvPupitre.setVisibility(View.GONE);
        }else{
            tvPupitre.setText(pupitre);
            tvRoleChoeur.setVisibility(View.GONE);
        }

        if(!roleAdmin.equals("Aucun")) {
            tvRoleAdmin.setText(roleAdmin);
        }else{
            tvRoleAdmin.setVisibility(View.GONE);
        }
        tvEmail.setText(email);
        tvTelFixe.setText(telFixe);
        tvTelPort.setText(telPort);
        tvAdresse.setText(adresse);
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
