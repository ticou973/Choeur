package dedicace.com.ui.Trombinoscope;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import dedicace.com.R;
import dedicace.com.ui.PlaySong.GlideApp;

public class TrombiDetailsActivity extends AppCompatActivity {
    String nom, prenom, pupitre, adresse, telFixe, telPort, email, roleChoeur, roleAdmin, urlPhoto;
    TextView tvNom, tvPupitre, tvAdresse, tvEmail, tvTelFixe, tvTelPort, tvRoleChoeur, tvRoleAdmin, intEmail,intTelFixe,intTelPort,intAdresse,tvZip;
    ImageView imgChoriste, imgMail, imgTelFixe,imgTelPort;
    String zipVille, rue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trombi_details);

        Toolbar toolbar = findViewById(R.id.toolbar_trombi_details);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
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
        imgMail = findViewById(R.id.img_mail);
        imgTelFixe=findViewById(R.id.img_tel_fixe);
        imgTelPort=findViewById(R.id.img_tel_port);
        intEmail = findViewById(R.id.tv_intitule_email);
        intTelPort = findViewById(R.id.tv_intitule_port);
        intTelFixe = findViewById(R.id.tv_intitulé_tel_fixe);
        intAdresse = findViewById(R.id.tv_intitule_adresse);
        tvZip=findViewById(R.id.tv_zip_ville);


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

        getAdresseSplit(adresse);

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

        if(adresse.isEmpty()){
            intAdresse.setVisibility(View.GONE);
            tvZip.setVisibility(View.GONE);
            tvAdresse.setVisibility(View.GONE);
        }else{
            tvAdresse.setText(rue);
            tvZip.setText(zipVille);
        }



        if(email.isEmpty()){
            imgMail.setVisibility(View.GONE);
            intEmail.setVisibility(View.GONE);
            tvEmail.setVisibility(View.GONE);
        }else{
            tvEmail.setText(email);
        }

        imgMail.setOnClickListener(view -> {
            Intent contactMail = new Intent(Intent.ACTION_SENDTO);
            contactMail.setData(Uri.parse("mailto:"));
            contactMail.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { email });
            contactMail.putExtra(Intent.EXTRA_SUBJECT, "Contact Chorale ");
            if (contactMail.resolveActivity(getPackageManager()) != null) {
                startActivity(contactMail);
            }
        });

        if(telFixe.isEmpty()){
            imgTelFixe.setVisibility(View.GONE);
            intTelFixe.setVisibility(View.GONE);
            tvTelFixe.setVisibility(View.GONE);
        }else{
            tvTelFixe.setText(telFixe);
        }

        imgTelFixe.setOnClickListener(view -> {
            Intent appel = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+telFixe));
            startActivity(appel);
        });

        if(telPort.isEmpty()){
            imgTelPort.setVisibility(View.GONE);
            intTelPort.setVisibility(View.GONE);
            tvTelPort.setVisibility(View.GONE);
        }else{
            tvTelPort.setText(telPort);
        }

        imgTelPort.setOnClickListener(view -> {
            Intent appel = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+telPort));
            startActivity(appel);
        });
    }

    private void getAdresseSplit(String adresse) {
        char[] cs = adresse.toCharArray();
        boolean boucle = true;
        int j = 0;
        while (boucle && j < cs.length) {
            if (Character.isDigit(cs[j])) {
                String zip = adresse.substring(j, j + 5);
                try {
                    int zipInt = Integer.parseInt(zip);
                    zipVille =adresse.substring(j);
                    rue = adresse.substring(0,j-1);
                    Log.d("coucou", "TDA getAdresseSplit: "+zipInt+" "+zipVille+rue);
                    boucle = false;
                } catch (NumberFormatException nfe) {
                    j++;
                }
            } else {
                j++;
            }
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
}
