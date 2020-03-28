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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    String nomChoraleStr, idChorale, nomCsvStr,rueChoriste, villeChoriste, zipChoriste;
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
    private ArrayList<String> listUrlPhoto = new ArrayList<>();
    private File file;
    private List<List<String>> adresses = new ArrayList<>();
    private List<Uri> downloadUris = new ArrayList<>();


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
        Button downloadPhotos = findViewById(R.id.btn_download_photos_choristes);

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
            String folder = file.getAbsolutePath();
            nomCsvStr =nomCsv.getText().toString();
            if(!TextUtils.isEmpty(nomCsvStr)) {
                Log.d(TAG, "CCCsv onCreate: début visualisation");
                Intent startVisualisation = new Intent(CreateChoristeCsv.this, VisualisationCsv.class);
                startVisualisation.putExtra("CsvName", nomCsvStr);
                startVisualisation.putExtra("path",folder);
                startVisualisation.putStringArrayListExtra("listUrl",listUrlPhoto);
                startActivityForResult(startVisualisation,REQUEST_CODE_C);
            }else{
                Toast.makeText(this, "Il manque le fichier csv", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "CCsv onCreate: Il manque le fichier Csv");
            }

        });

        downloadPhotos.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(idChorale)&&listResult!=null&&listResult.size()!=0){
                int nbItem = listResult.size();
                Log.d(TAG, "onCreate: "+listResult.size());
                for (int i = 0; i < nbItem; i++) {
                    downloadUris.add(null);
                }
               insertPhotoInCloudStorage();
            }else{
                Log.d(TAG, "CCCsv onCreate download: il manque des éléments");
                Toast.makeText(this, "Il manque des éléments download !", Toast.LENGTH_SHORT).show();
            }
        });

        insertDb.setOnClickListener(view -> {
            //todo éventuellement ajouter une condition pour attendre la fin du storage
            if(!TextUtils.isEmpty(idChorale)&&listResult!=null&&listResult.size()!=0){
                insertChoristeinDb();
            }else{
                Log.d(TAG, "CCCsv onCreate insert : il manque des éléments");
                Toast.makeText(this, "Il manque des éléments db !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void insertPhotoInCloudStorage() {
        for(String pathSelected:listUrlPhoto) {
            if(!pathSelected.isEmpty()){

            File tempFile = new File(pathSelected);
            fileNameSelected =tempFile.getName();

            Uri fileSelected = Uri.fromFile(new File(pathSelected));
            StorageReference imageRef = mStorageRef.child("users/photos_choristes/" + fileNameSelected);

            UploadTask uploadTask = imageRef.putFile(fileSelected);

            uploadTask.addOnSuccessListener(taskSnapshot -> Log.d(TAG, "CCC onSuccess: bravo c'est uploadé"))
                    .addOnFailureListener(exception -> Log.d(TAG, "CCC onFailure: dommage c'est raté"));


            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int indexListPhoto = listUrlPhoto.indexOf(pathSelected);
                    Uri downloadUrl;
                    downloadUrl = task.getResult();
                    downloadUris.set(indexListPhoto,downloadUrl);
                    Log.d(TAG, "CCU onComplete: " + downloadUrl);
                } else {
                    Log.d(TAG, "CCU onComplete: Il y a eu un pb " + Objects.requireNonNull(task.getException()).getMessage());
                }
            });
             }else{
               // int indexListPhoto = listUrlPhoto.indexOf(pathSelected);
               // downloadUris.set(indexListPhoto,null);
                Log.d(TAG, "CCC insertPhotoInCloudStorage: pas de photos");
            }
        }

        Log.d(TAG, "CCSV insertPhotoInCloudStorage: "+downloadUris);
    }

    private void insertChoristeinDb() {
        for(Choriste tempChoriste:choristes) {
            int index = choristes.indexOf(tempChoriste);
            Map<String, Object> choriste = new HashMap<>();
            choriste.put("nom_choriste", tempChoriste.getNom());
            choriste.put("prenom_choriste", tempChoriste.getPrenom());
            if(downloadUris.get(index)!=null) {
                choriste.put("url_photo", downloadUris.get(index).toString());
            }else{
                choriste.put("url_photo", "");
            }
            choriste.put("pupitre", tempChoriste.getPupitre().toString());
            choriste.put("role_choeur", tempChoriste.getRoleChoeur());
            choriste.put("role_admin", tempChoriste.getRoleAdmin());
            choriste.put("email", tempChoriste.getEmail());
            choriste.put("tel_fixe", tempChoriste.getFixTel());
            choriste.put("tel_port", tempChoriste.getPortTel());
            choriste.put("adresse", adresses.get(index));
            choriste.put("maj", Timestamp.now());

            db.collection("chorale").document(idChorale).collection("choristes")
                    .add(choriste)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "CCC DocumentSnapshot added with ID: " + documentReference.getId());
                        File file = new File(pathSelected);
                       /* if (file.delete()) {
                            Log.d(TAG, "CCC onSuccess: le fichier est supprimé du local");
                        } else {
                            Log.d(TAG, "CCC onSuccess: problème de suppression en local du fichier");
                        }*/



                    }).addOnFailureListener(e -> Log.d(TAG, "CCC Error adding document", e));
        }
        majChoraleTrombi();
    }

    private void getAdresse() {
        int index;
        for(index =0;index<listResult.size();index++) {
            String adresse = listResult.get(index)[8];
            getAdresseSplit(adresse);
        }
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
                    List<String> tempadresse = new ArrayList<>();
                    tempadresse.add(adresse.substring(0, j - 1));
                    tempadresse.add(adresse.substring(j + 6));
                    tempadresse.add(String.valueOf(zipInt));
                    adresses.add(tempadresse);
                    Log.d(TAG, "CCC getAdresse: adresse "+ tempadresse);
                    boucle = false;
                } catch (NumberFormatException nfe) {
                    j++;
                }
            } else {
                j++;
            }
        }
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
        file = new File(path,"DedicaceAdmin/csv_Choristes");

        if(file.mkdirs()){ Log.d(TAG, "CCC insertCsvInCloudStorage: le dossier est fait");
        }else{
            Log.d(TAG, "CCC insertCsvInCloudStorage: dossier non réalisé ou déjà fait "+file.exists());
        }
        if(file.exists()) {
            listFiles = file.listFiles();
            Log.d(TAG, "CCU getLists: " + Arrays.toString(listFiles));

            if ((listFiles != null) && (listFiles.length != 0)) {
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
                Log.d(TAG, "CCC onActivityResult: name "+name);
                nomCsv.setText(name);
                pathSelected=listFiles[csvSelected].getAbsolutePath();
                Log.d(TAG, "CCC onCreate: path " + pathSelected);

                File myFile = new File(file,name);
                readCsv(myFile);
                Log.d(TAG, "CCC onCreate: " + pathSelected+" "+listResult.size());
                CompleteData();

            }
        }else if(requestCode==REQUEST_CODE_C){
            if (data != null) {
                listUrlPhoto = new ArrayList<>();
                listUrlPhoto = data.getStringArrayListExtra("listUrl");
                Log.d(TAG, "CCCcv onActivityResult: "+listUrlPhoto.size()+" "+listUrlPhoto);
            }

        }else{
            Log.d(TAG, "CCU onActivityResult: petit problème au retour ");
        }
    }

    private void CompleteData() {
        for(String[] row : listResult){
            Log.d(TAG, "CCC CompleteData: "+listResult.size());
            Log.d(TAG, "CCC readCsv: row "+ Arrays.toString(row));
            Log.d(TAG, "CCC readcsv: "+row.length+" "+row[0]);
            Pupitre pupitre = SongsUtilities.converttoPupitre(row[2]);

            Choriste newChoriste = new Choriste(idChorale,row[0],row[1],pupitre,row[8],row[7],row[6],row[5],row[3],row[4]);
            choristes.add(newChoriste);
        }
        Log.d(TAG, "CCC CompleteData: chorsites "+choristes.size());

        getAdresse();

        for (int j = 0; j <listResult.size() ; j++) {
            listUrlPhoto.add("");
        }
    }

    private void readCsv(File myFile) {
        Log.d(TAG, "CCSV readCsv: "+myFile);
        FileInputStream inputStream = null;
        try {
            Log.d(TAG, "CCSV readCsv: try");
            inputStream = new FileInputStream(myFile);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "CCSV readCsv: catch "+e.toString());
            e.printStackTrace();
        }

        Log.d(TAG, "CCCsv onActivityResult: "+inputStream);
        CsvReader csvReader = new CsvReader(inputStream);

        listResult = csvReader.read();

        Log.d(TAG, "CCCsv readCsv: list result size "+listResult.size());
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
