package dedicace.com.ui.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dedicace.com.R;
import dedicace.com.data.database.Choriste;
import dedicace.com.data.database.Pupitre;
import dedicace.com.utilities.SongsUtilities;

public class CreateChoristeCsv extends AppCompatActivity {

    TextView nomChorale, nomCsv;
    String nomChoraleStr, idChorale, nomCsvStr;
    private static final int REQUEST_CODE_B = 200;
    private static final int REQUEST_CODE_A = 100;
    private static final int REQUEST_CODE_C = 300;
    private static final String TAG ="coucou";
    private File[] listFiles;
    private String[] listCsv;
    private int csvSelected;
    private String pathSelected;
    private List<String> paths = new ArrayList<>();
    private String fileNameSelected;
    private List<String[]> listResult;
    private List<Choriste> choristes = new ArrayList<>();
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Uri downloadUrl;
    private ArrayList<String> listUrlPhoto = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_choriste_csv);
        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        nomChorale = findViewById(R.id.tv_nom_chorale_trombi_csv);
        nomCsv = findViewById(R.id.tv_name_csv);
        Button selectChorale = findViewById(R.id.btn_select_chorale_trombi_csv);
        Button selectCsv = findViewById(R.id.btn_select_csv);
        Button visualisationCsv = findViewById(R.id.btn_visualisation_csv);

        Button insertDb = findViewById(R.id.btn_create_in_db_csv);

        selectChorale.setOnClickListener(view -> {
            Intent startModifyChoraleActivity = new Intent(CreateChoristeCsv.this,ModifyChorale.class);
            startModifyChoraleActivity.putExtra("origine","CreateChoristeCSV");
            startActivityForResult(startModifyChoraleActivity,REQUEST_CODE_A);
        });

        selectCsv.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(idChorale)) {
                getLists();
                selectCsv();
            }else{
                Toast.makeText(this, "Il manque l'id Chorale", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "CCCsv onCreate: manque id chorale");
            }
        });


        visualisationCsv.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(fileNameSelected)) {
                Log.d(TAG, "CCCsv onCreate: début visualisation");
                Intent startVisualisation = new Intent(CreateChoristeCsv.this, VisualisationCsv.class);
                startVisualisation.putExtra("CsvName", fileNameSelected);
                startVisualisation.putStringArrayListExtra("listUrl",listUrlPhoto);
                startActivityForResult(startVisualisation,REQUEST_CODE_C);
            }else{
                Toast.makeText(this, "Il manque le fichier csv", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "CCsv onCreate: Il manque le fichier Csv");
            }

        });


        insertDb.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(idChorale)&&listResult!=null&&listResult.size()!=0){
               // insertPhotoInCloudStorage();


            }else{
                Log.d(TAG, "CCCsv onCreate: il manque des éléments");
                Toast.makeText(this, "Il manque des éléments !", Toast.LENGTH_SHORT).show();
                String essai = "bonjour/monsieur/le/président";
                String[] morceaux = essai.split("/");

                Log.d(TAG, "onCreate: "+morceaux[0]+" "+morceaux[1]+" "+morceaux[2]);
            }
        });

    }

    private void insertPhotoInCloudStorage() {

        for(String pathSelected:paths) {

            Uri fileSelected = Uri.fromFile(new File(pathSelected));
            StorageReference imageRef = mStorageRef.child("songs/photos_choristes/" + fileNameSelected);

            UploadTask uploadTask = imageRef.putFile(fileSelected);

            uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "CSU onSuccess: bravo c'est uploadé"))
                    .addOnFailureListener(exception -> Log.d(TAG, "CSU onFailure: dommage c'est raté"));


            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult();
                    insertChoristeinDb(pathSelected);
                    Log.d(TAG, "CCU onComplete: " + downloadUrl);
                } else {
                    Log.d(TAG, "CCU onComplete: Il y a eu un pb " + Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }
    }

    private void insertChoristeinDb(String pathSelected) {
       /*
        List<String> adresse = new ArrayList<>();
        adresse.add(rueChoriste);
        adresse.add(villeChoriste);
        adresse.add(zipChoriste);


        Map<String,Object> choriste = new HashMap<>();
        choriste.put("nom_choriste",nomChoriste);
        choriste.put("prenom_choriste",prenomChoriste);
        choriste.put("url_photo",downloadUrl.toString());
        choriste.put("pupitre",pupitreStr);
        choriste.put("role_choeur",roleChoeurStr);
        choriste.put("role_admin",roleAdminStr);
        choriste.put("email",mailChoriste);
        choriste.put("tel_fixe",telFixeChoriste);
        choriste.put("tel_port",telPortChoriste);
        choriste.put("adresse",adresse);
        choriste.put("maj", Timestamp.now());

        db.collection("chorale").document(idChorale).collection("choristes")
                .add(choriste)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "CCU DocumentSnapshot added with ID: " + documentReference.getId());
                    File file = new File(pathSelected);
                    if(file.delete()){
                        Log.d(TAG, "CCU onSuccess: le fichier est supprimé du local");
                    }else{
                        Log.d(TAG, "CCU onSuccess: problème de suppression en local du fichier");
                    }

                    majChoraleTrombi();


                }).addOnFailureListener(e -> Log.d(TAG, "CCU Error adding document", e));*/
    }

    private void majChoraleTrombi() {
        Map<String,Object> data = new HashMap<>();
        data.put("maj_trombi",Timestamp.now());

        db.collection("chorale").document(idChorale)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "CSU onSuccess: maj chorale done"))
                .addOnFailureListener(e -> Log.d(TAG, "CSU onSuccess: maj chorale failed"));
    }


    private void selectCsv() {
        Log.d(TAG, "CCC selectCsv: ");
        Intent startChooseCsvActivity = new Intent(CreateChoristeCsv.this,ChooseCsv.class);
        startChooseCsvActivity.putExtra("listCsv",listCsv);
        startActivityForResult(startChooseCsvActivity,REQUEST_CODE_B);
    }

    //todo voir comment ne mettre le csv qu'à un seul endroit au lieu de 2 (DedicaceAdmin et le interne data/data/files)
    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/csv_Choristes");

        if(file.mkdirs()){
            Log.d(TAG, "CCC insertCsvInCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "CCC insertCsvInCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()) {
            listFiles = file.listFiles();

            Log.d(TAG, "CCU getLists: " + Arrays.toString(listFiles));

            if (listFiles != null && listFiles.length != 0) {

                Log.d(TAG, "CCU selectBackground: " + " " + listFiles.length);

                listCsv = new String[listFiles.length];

                for (int i = 0; i < listFiles.length; i++) {
                    listCsv[i] = listFiles[i].getName();

                    Log.d(TAG, "CCC selectBackground: " + listFiles[i].getName());
                }
            }else{
                Log.d(TAG, "CCC getLists: pas de listFiles ");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_A) {
            Log.d(TAG, "CCC onActivityResult: request_codeA");

            idChorale = data.getStringExtra("idselected");
            nomChoraleStr = data.getStringExtra("nomChorale");
            nomChorale.setText(nomChoraleStr);
            Log.d(TAG, "CCC onActivityResult: request_codeA " + idChorale);
        }else if(requestCode==REQUEST_CODE_B){

            if (data != null) {
                csvSelected = data.getIntExtra("csvselected",-1);
                Log.d(TAG, "CCC onActivityResult: "+csvSelected);
            }

            if(csvSelected!=-1) {
                String name = listCsv[csvSelected];
                nomCsv.setText(name);
                pathSelected=listFiles[csvSelected].getAbsolutePath();
                fileNameSelected = name;

                readCsv(name);
                Log.d(TAG, "CCC onCreate: " + pathSelected+" "+listResult.size());

                for (int j = 0; j <listResult.size() ; j++) {
                    listUrlPhoto.add("");
                }
            }
        }else if(requestCode==REQUEST_CODE_C){
            if (data != null) {
                listUrlPhoto = new ArrayList<>();
                listUrlPhoto = data.getStringArrayListExtra("listUrl");
                Log.d(TAG, "CCCcv onActivityResult: "+listUrlPhoto.size());
            }

        }else{
            Log.d(TAG, "CCU onActivityResult: petit problème au retour ");
        }
    }

    private void readCsv(String name) {
        InputStream inputStream = null;
        try {
            inputStream = openFileInput(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "CCCsv onActivityResult: "+inputStream);
        CsvReader csvReader = new CsvReader(inputStream);

        listResult = csvReader.read();

        for(String[] row : listResult){
            Log.d(TAG, "CCC onActivityResult: "+row[1]);
            Pupitre pupitre = SongsUtilities.converttoPupitre(row[2]);

            Choriste newChoriste = new Choriste(idChorale,row[0],row[1],pupitre,row[8],row[7],row[6],row[5],row[3],row[4]);
            choristes.add(newChoriste);
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
