package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;

public class CreateSpectacle extends AppCompatActivity {

    private Button addTitres, addConcerts, createSpectacleInDb, selectChorale;
    private EditText nomSpectacle, nomChorale;
    private TextView listTitres;
    private String nomSpectacleStr, idChorale, titreSong, nomChoraleStr;
    private static final String TAG ="coucou";
    private static final int REQUEST_CODE_B = 200;
    private static final int REQUEST_CODE_C = 300;

    private SharedPreferences sharedPreferences;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;

    private List<String> titres = new ArrayList<>();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_spectacle);

        addTitres=findViewById(R.id.btn_add_titres);
        addConcerts=findViewById(R.id.btn_add_concerts);
        createSpectacleInDb=findViewById(R.id.btn_create_spectacle_db);
        nomSpectacle=findViewById(R.id.et_nom_spectacle);
        selectChorale=findViewById(R.id.btn_select_chorale_spectacle);
        nomChorale=findViewById(R.id.et_select_chorale_spectacle);
        listTitres=findViewById(R.id.tv_list_titres_spectacle);
        selectChorale=findViewById(R.id.btn_select_chorale_spectacle);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        createSpectacleInDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        addTitres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifySSActivity = new Intent(CreateSpectacle.this,ModifySourceSong.class);
                startModifySSActivity.putExtra("origine","CreateSpectacle");
                startActivityForResult(startModifySSActivity,REQUEST_CODE_B);
            }
        });

        addConcerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startAddConcert= new Intent(CreateSpectacle.this,AddConcert.class);
                startAddConcert.putExtra("origine","CreateSpectacle");
                startActivityForResult(startAddConcert,REQUEST_CODE_B);
            }
        });

        selectChorale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startModifySSActivity = new Intent(CreateSpectacle.this,ModifyChorale.class);
                startModifySSActivity.putExtra("origine","CreateSpectacle");
                startActivityForResult(startModifySSActivity,REQUEST_CODE_C);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK) {

            if (requestCode == REQUEST_CODE_B) {

                Log.d(TAG, "CSP onActivityResult: request_codeB");
                if (data != null) {
                    titreSong = data.getStringExtra("titreselected");
                }
                Log.d(TAG, "CS onActivityResult: request_codeB " + titreSong);
                titres.add(titreSong);

                StringBuilder sb = new StringBuilder(" ");
                String newLine = System.getProperty("line.separator");

                int i=0;
                for(String titre:titres){
                    i++;

                    String listTitres = i+". "+titre+newLine;

                    sb.append(listTitres+newLine);

                }

                Log.d(TAG, "CSP onActivityResult: "+ sb.toString() );

                listTitres.setText(sb.toString());
            }else if(requestCode==REQUEST_CODE_C){

                if (data != null) {
                    idChorale = data.getStringExtra("idselected");
                    nomChoraleStr=data.getStringExtra("nomChorale");
                }

                nomChorale.setText(nomChoraleStr);
            }

        }else{
            Log.d(TAG, "CSP onActivityResult: petit problème au retour ");
        }
    }
}
