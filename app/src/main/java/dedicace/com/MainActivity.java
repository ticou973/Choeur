package dedicace.com;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SongsAdapter.ListemClickedListener,DialogRecordFragment.DialogRecordFragmentListener {

    private  RecyclerView recyclerView;
    private SongsAdapter songsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<SourceSong> songs = new ArrayList<>();
    private Toast mToast;
    private static final String TAG = "coucou";
    private SourceSong sourceSong1,sourceSong2,sourceSong3,sourceSong4,sourceSong5,sourceSong6, sourceSong7;
    private Pupitre recordPupitre=Pupitre.NA;
    private final int REQUEST_PERMISSION_CODE = 1000;
    private int position;

    private OnPositiveClickListener mPositiveClickListener;
    private SongsViewHolder songsViewHolder;

    public static AppDataBase choeurDataBase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        recyclerView = findViewById(R.id.recyclerview_media_item);
        songsAdapter =new SongsAdapter(songs,this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(songsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.reset_db:

                choeurDataBase.songsDao().deleteAll();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initData() {
        choeurDataBase = Room.databaseBuilder(this,AppDataBase.class,"ChoeurDataBase")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        sourceSong1 = new SourceSong("Des hommes pareils","Francis Cabrel",321,R.drawable.hand,"");
        sourceSong2 = new SourceSong("L'un pour l'autre","Maurane",266,R.drawable.yinyang,"");
        sourceSong3 = new SourceSong("L'eau","Jeanne Cherhal",143,R.drawable.water,"");
        sourceSong4 = new SourceSong("Le tissu","Jeanne Cherhal",236,R.drawable.femme_tissu,"");
        sourceSong5 = new SourceSong("Papaoutai","Stromae",232,R.drawable.papa,"");
        sourceSong6 = new SourceSong("Recitation 11","Georges Aperghis",243,R.drawable.pyramide_texte,"");
        sourceSong7 = new SourceSong("North Star","Philip Glas",160,R.drawable.etoile,"");

        Song song1 = new Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.TUTTI,"R.raw.des_hommes_pareils_tutti");
        Song song2 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.des_hommes_pareils_basse");
        Song song3 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.des_hommes_pareils_tenor");
        Song song4 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.des_hommes_pareils_alto");
        Song song5 = new  Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.SOPRANO,"R.raw.des_hommes_pareils_soprano");
        Song song6 = new Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.l_un_pour_l_autre_basse");
        Song song7 = new  Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.l_un_pour_l_autre_tenor");
        Song song8 = new  Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.l_un_pour_l_autre_alto");
        Song song9 = new  Song(sourceSong2,RecordSource.BANDE_SON,Pupitre.SOPRANO,"R.raw.l_un_pour_l_autre_soprano");
        Song song10 = new  Song(sourceSong3,RecordSource.BANDE_SON,Pupitre.TUTTI,"R.raw.l_eau_tutti");
        Song song11 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.BASS,"R.raw.le_tissu_basse");
        Song song12 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.TENOR,"R.raw.le_tissu_tenor");
        Song song13 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.ALTO,"R.raw.le_tissu_alto");
        Song song14 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.SOPRANO,"R.raw.le_tissu_soprano");

        choeurDataBase.songsDao().insertSongs(song1,song2,song3,song4,song5,song6,song7,song8,song9,song10,song11,song12,song13,song14);

        songs.add(sourceSong1);
        songs.add(sourceSong2);
        songs.add(sourceSong3);
        songs.add(sourceSong4);
        songs.add(sourceSong5);
        songs.add(sourceSong6);
        songs.add(sourceSong7);
    }

    @Override
    public void OnClickedItem(String titre, String message) {

        if(mToast!=null){
            mToast.cancel();
        }

        mToast=Toast.makeText(this, message +"-"+titre, Toast.LENGTH_SHORT);
        mToast.show();

    }

    @Override
    public void OnDialogRecord(int position, SongsViewHolder songsViewHolder) {
        this.position=position;
        mPositiveClickListener=songsViewHolder;
        DialogFragment dialog = new DialogRecordFragment();
        dialog.show(getSupportFragmentManager(),"TAG");
        Log.d(TAG, "MA OnDialogRecord: ");
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, Pupitre pupitre) {

        Log.d(TAG, "MA onDialogPositiveClick: enregistrer " + pupitre+ " "+ position);
        Song recordSong = new Song(songs.get(position),RecordSource.LIVE,pupitre,"NA");
        choeurDataBase.songsDao().insertSong(recordSong);
       // songsAdapter.notifyItemChanged(position);
        mPositiveClickListener.OnRecord(pupitre);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        Log.d(TAG, "MA onDialogPositiveClick: annuler");
    }


    @Override
    public void OnRequestPermission() {
        if(!checkPermissionFromDevice()){
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);

        return write_external_storage_result==PackageManager.PERMISSION_GRANTED &&record_audio_result==PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }

            }
            break;
        }
    }

    public interface OnPositiveClickListener {
        void OnRecord(Pupitre pupitre);
    }



}
