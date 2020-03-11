package dedicace.com.ui.Admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dedicace.com.R;
import dedicace.com.data.database.Pupitre;
import dedicace.com.ui.PlaySong.GlideApp;
import dedicace.com.utilities.SongsUtilities;

public class VisualisationCsv extends AppCompatActivity implements ChoristeAdapter.clickedListener{

    private List<String[]> listResult;
    private static final String TAG = "coucou";
    private File[] listFiles;
    private String[] listImages;
    private static final int REQUEST_CODE_B = 200;
    private int imageSelected;
    private String pathSelected;
    private String fileNameSelected;
    private ChoristeAdapter choristeAdapter;
    private int position;
    private FloatingActionButton fab;
    private ArrayList<String> listUrlPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualisation_csv);
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String nameCsv = intent.getStringExtra("CsvName");
        listUrlPhoto  = intent.getStringArrayListExtra("listUrl");

        fab =findViewById(R.id.fab_choriste);

        fab.setOnClickListener(view -> {
            Log.d(TAG, "Vcsv onCreate: On Click fab");
            Intent result = new Intent();
            result.putStringArrayListExtra("listUrl",listUrlPhoto);
            setResult(RESULT_OK,result);
            finish();
        });

        readCsv(nameCsv);

        RecyclerView recycler = findViewById(R.id.recyclerview_local_csv_choristes);
        choristeAdapter = new ChoristeAdapter(listResult, listUrlPhoto,this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        recycler.setAdapter(choristeAdapter);
    }

    private void readCsv(String name) {
        InputStream inputStream = null;
        try {
            inputStream = openFileInput(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "CCCsv onActivityResult: " + inputStream);
        CsvReader csvReader = new CsvReader(Objects.requireNonNull(inputStream));

        listResult = csvReader.read();

        for (String[] row : listResult) {
            Log.d(TAG, "CCC onActivityResult: " + row[1]);
            Pupitre pupitre = SongsUtilities.converttoPupitre(row[2]);
        }
    }

    @Override
    public void selectPhoto(int i, String[] listImages, File[] listFiles) {

        this.listImages=listImages;
        this.listFiles=listFiles;
        Log.d(TAG, "VSsv selectPhoto: "+listImages.length+" "+listFiles.length);


        Intent startChooseBackgroundActivity = new Intent(VisualisationCsv.this,ChoosePhoto.class);
        startChooseBackgroundActivity.putExtra("origine","csv");
        startChooseBackgroundActivity.putExtra("listimages",listImages);
        startChooseBackgroundActivity.putExtra("position",i);
        startActivityForResult(startChooseBackgroundActivity,REQUEST_CODE_B);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Vcsv onActivityResult: "+requestCode+" "+resultCode);
        if (requestCode == REQUEST_CODE_B) {

            if (data != null) {
                imageSelected = data.getIntExtra("imageselected", -1);
                position = data.getIntExtra("position",0);
                Log.d(TAG, "CS onActivityResult: " + imageSelected);
            }

            if (imageSelected != -1) {
                String name = listImages[imageSelected];
                //pathSelected = listPath.get(imageSelected);
                pathSelected = listFiles[imageSelected].getAbsolutePath();
                fileNameSelected = name;
                choristeAdapter.setUrlPhoto(pathSelected,position);
                listUrlPhoto.set(position,pathSelected);
                Log.d(TAG, "CSS onCreate: " + pathSelected);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }



}


//Adapter pour le recyclerView de Choristes

    class ChoristeAdapter extends RecyclerView.Adapter<ChoristeAdapter.ChoristeViewHolder> {
        private List<String[]> listResult;
        private Context mContext;
        private static final String TAG = "coucou";
        private File[] listFiles;
        private String[] listImages;
        private String position;
        private ArrayList listUrlPhoto;

        public void setUrlPhoto(String urlPhoto, int position) {

            listUrlPhoto.set(position,urlPhoto);
            notifyItemChanged(position);
        }

        private String urlPhoto;

        private clickedListener mClickedListener;


        public ChoristeAdapter(List<String[]> listResult, ArrayList<String> listUrlPhoto, Context context) {
            this.listResult = listResult;
            this.listUrlPhoto=listUrlPhoto;
            mContext = context;
        }

        interface clickedListener {
            void selectPhoto(int i, String[] listImages, File[] listFiles);
        }

        @NonNull
        @Override
        public ChoristeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_csv, viewGroup, false);

            return new ChoristeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChoristeViewHolder choristeViewHolder, int i) {
            choristeViewHolder.nom.setText(listResult.get(i)[0]);
            choristeViewHolder.prenom.setText(listResult.get(i)[1]);
            choristeViewHolder.pupitre.setText(listResult.get(i)[2]);
            choristeViewHolder.roleChoeur.setText(listResult.get(i)[3]);
            choristeViewHolder.roleAdmin.setText(listResult.get(i)[4]);
            choristeViewHolder.email.setText(listResult.get(i)[5]);
            choristeViewHolder.telPort.setText(listResult.get(i)[6]);
            choristeViewHolder.telFixe.setText(listResult.get(i)[7]);
            choristeViewHolder.adresse.setText(listResult.get(i)[8]);

            GlideApp.with(mContext)
                    .load(listUrlPhoto.get(i))
                    .centerCrop() // scale to fill the ImageView and crop any extra
                    .into(choristeViewHolder.imgChoriste);

            choristeViewHolder.ajoutPhoto.setOnClickListener(view -> {
                getLists();
                selectPhoto(i);
            });
        }

        private void getLists() {
            File path = Environment.getExternalStorageDirectory();
            File file = new File(path, "DedicaceAdmin/Photos_Choristes");

            if (file.mkdirs()) {
                Log.d(TAG, "CA insertPhotoInCloudStorage: le dossier est fait");

            } else {
                Log.d(TAG, "CA insertPhotoInCloudStorage: dossier non réalisé ou déjà fait");
            }

            if (file.exists()) {
                listFiles = file.listFiles();

                Log.d(TAG, "CA getLists: " + Arrays.toString(listFiles));

                if (listFiles != null && listFiles.length != 0) {

                    Log.d(TAG, "CA selectBackground: " + " " + listFiles.length);

                    listImages = new String[listFiles.length];

                    for (int i = 0; i < listFiles.length; i++) {
                        listImages[i] = listFiles[i].getName();

                        Log.d(TAG, "CA selectBackground: " + listFiles[i].getName());
                    }
                } else {
                    Log.d(TAG, "CA getLists: pas de listFiles ");
                }
            }
        }

        private void selectPhoto(int i) {
            if (mClickedListener != null) {
                Log.d(TAG, "CA selectPhoto: "+i+" "+ listImages);
                mClickedListener.selectPhoto(i,listImages,listFiles);

            }
        }

        @Override
        public int getItemCount() {
            return listResult.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);

            Context context = recyclerView.getContext();

            if (context instanceof clickedListener) {
                mClickedListener = (clickedListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnFragmentInteractionListener");
            }
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            mClickedListener = null;
        }


        public class ChoristeViewHolder extends RecyclerView.ViewHolder {
            TextView nom, prenom, pupitre, roleChoeur, roleAdmin, telFixe, telPort, adresse, email;
            Button ajoutPhoto;
            ImageView imgChoriste;

            public ChoristeViewHolder(@NonNull View itemView) {
                super(itemView);
                nom = itemView.findViewById(R.id.tv_listcsv_nom);
                prenom = itemView.findViewById(R.id.tv_listcsv_prenom);
                pupitre = itemView.findViewById(R.id.tv_listcsv_pupitre);
                roleChoeur = itemView.findViewById(R.id.tv_listcsv_role_choeur);
                roleAdmin = itemView.findViewById(R.id.tv_listcsv_role_admin);
                telFixe = itemView.findViewById(R.id.tv_listcsv_tel_fixe);
                telPort = itemView.findViewById(R.id.tv_listcsv_tel_port);
                adresse = itemView.findViewById(R.id.tv_listcsv_adresse);
                email = itemView.findViewById(R.id.tv_listcsv_email);
                ajoutPhoto = itemView.findViewById(R.id.btn_ajout_photo);
                imgChoriste = itemView.findViewById(R.id.img_choriste_list_csv);
            }
        }
    }

