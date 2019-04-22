package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;

public class CreateSourceSong extends AppCompatActivity {

    private Button createSSInDb,selectBackground;
    private EditText titre, groupe, duration;
    private TextView background;
    private static final String TAG ="coucou";
    private static List<String> listFilesImage = new ArrayList<>();
    private List<String> listPath = new ArrayList<>();
    private File[] listFiles;
    private String[] listImages;
    private int imageSelected;
    private String pathSelected;
    private final static int REQUEST_CODE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_source_song);

        createSSInDb = findViewById(R.id.btn_create_ss_db);
        selectBackground = findViewById(R.id.btn_select_background);
        titre = findViewById(R.id.et_titre_ss);
        groupe = findViewById(R.id.et_groupe_ss);
        duration = findViewById(R.id.et_duration_ss);
        background = findViewById(R.id.tv_background);

        getLists();

        createSSInDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertBackgroundInCloudStorage();
                insertSSinDb();
                newSS();
            }
        });

        selectBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBackground();
            }
        });


    }

    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/Image_Background_Chorale");

        if(file.mkdirs()){
            Log.d(TAG, "CSS insertBackgroundInCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "CSS insertBackgroundInCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()){
            listFiles = file.listFiles();

            for (File image:listFiles) {
                Log.d(TAG, "CSS selectBackground: "+image.getName());
                listFilesImage.add(image.getName());
                listPath.add(image.getAbsolutePath());
            }

            Log.d(TAG, "CSS selectBackground: "+listFilesImage.size()+" "+listFiles.length);

            listImages = new String[listFiles.length];

            for (int i = 0; i < listFiles.length; i++) {
                listImages[i]=listFiles[i].getName();

                Log.d(TAG, "selectBackground: "+listFiles[i].getName());
            }
        }

        Log.d(TAG, "CSS : selectBackground: "+ listImages.length);
    }

    private void insertBackgroundInCloudStorage() {



    }

    private void newSS() {

        //voulez vous en insérer d'autres ?


    }

    private void selectBackground() {
        Log.d(TAG, "CSS selectBackground: ");

        Intent startChooseBackgroundActivity = new Intent(CreateSourceSong.this,ChooseBackground.class);
        startChooseBackgroundActivity.putExtra("listimages",listImages);
        startActivityForResult(startChooseBackgroundActivity,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Log.d(TAG, "CSS onActivityResult: ok cela marche");
            if(requestCode==REQUEST_CODE){

                int imageSelected= 0;
                if (data != null) {
                    imageSelected = data.getIntExtra("imageselected",-1);
                }

                if(imageSelected!=-1) {
                    background.setText(listFilesImage.get(imageSelected));
                    pathSelected = listPath.get(imageSelected);
                    Log.d(TAG, "CSS onCreate: " + pathSelected);
                }
            }
        }else{
            Log.d(TAG, "CSS onActivityResult: petit problème au retour ");
        }
    }

    private void insertSSinDb() {
        //obligatoire
        String titreSS = titre.getText().toString();
        String groupeSS = groupe.getText().toString();
        String backgroundSS = background.getText().toString();

        //facultatif
        int durationSS = Integer.parseInt(duration.getText().toString());

        if(!titreSS.equals("")&&!groupeSS.equals("")&&!backgroundSS.equals("")){


        }else{
            Toast.makeText(this, "Il manque des éléments", Toast.LENGTH_SHORT).show();
        }

    }

    //todo voir communication entre activity
    public static List<String> getListFilesImage() {
        return listFilesImage;
    }
}
