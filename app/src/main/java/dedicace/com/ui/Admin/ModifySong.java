package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.utilities.SongsUtilities;

public class ModifySong extends AppCompatActivity implements SongAdapter.OnItemListener {

    private RecyclerView recyclerSong;
    private FloatingActionButton fab;
    private SongAdapter songAdapter;
    private List<Song> listSongs = new ArrayList<>();
    private List<String> listId = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG ="coucou";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_song);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        fab = findViewById(R.id.fab_Song);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateSongActivity = new Intent(ModifySong.this,CreateSong.class);
                startActivity(startCreateSongActivity);
            }
        });

        db=FirebaseFirestore.getInstance();

        getListSongs();

    }

    //todo voir pour passer par NDS pour aller chercher les données
    private void getListSongs() {
        try {
            db.collection("songs")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, "MS onComplete: Songs " + Thread.currentThread().getName());
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "MS-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));
                                    //todo voir comment écrire une seule ligne avec ToObject

                                    String titre, pupitreStr, recordSource, songPath,idDocument;
                                    Date maj;
                                    RecordSource source;
                                    Pupitre pupitre;
                                    Timestamp majs;

                                    idDocument = document.getId();
                                    listId.add(idDocument);

                                    titre = (String) document.getData().get("titre_song");
                                    pupitreStr = (String) document.getData().get("pupitre");
                                    recordSource = (String) document.getData().get("recordSource");
                                    majs = (Timestamp) document.getData().get("maj");
                                    maj = majs.toDate() ;
                                    songPath = (String) document.getData().get("songPath");

                                    source = SongsUtilities.convertToRecordSource(recordSource);
                                    pupitre = SongsUtilities.converttoPupitre(pupitreStr);

                                    Log.d(TAG, "MS-exec onComplete:A Songs " + titre + " " + pupitreStr + " " + songPath + " " + maj + " " + recordSource);
                                    Song song = new Song(titre,source,pupitre,songPath,maj);
                                    listSongs.add(song);
                                }

                                recyclerSong = findViewById(R.id.recyclerview_cloud_Song);
                                songAdapter = new SongAdapter(listSongs);
                                layoutManager = new LinearLayoutManager(ModifySong.this);
                                recyclerSong.setLayoutManager(layoutManager);
                                recyclerSong.setHasFixedSize(true);
                                recyclerSong.setAdapter(songAdapter);

                                Log.d(TAG, "MSS fetchSourceSongs: après fetch");
                            } else {
                                Log.w(TAG, "MSS-exec Error getting documents.", task.getException());
                            }
                        }
                    });

        } catch (Exception e) {
            // Server probably invalid
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(int i) {
        Log.d(TAG, "MS onItemClick: "+i);
        Intent startDetailsSongActivity = new Intent(ModifySong.this, ModifySongDetails.class);
        Bundle args = new Bundle();
        args.putString("idSong", listId.get(i));
        args.putString("oldTitre",listSongs.get(i).getSourceSongTitre());
        args.putString("oldPupitre", listSongs.get(i).getPupitre().toString());
        args.putString("oldSource", listSongs.get(i).getRecordSource().toString());
        args.putString("oldPath",listSongs.get(i).getSongPath());
        startDetailsSongActivity.putExtra("bundleSong", args);
        startActivity(startDetailsSongActivity);
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
