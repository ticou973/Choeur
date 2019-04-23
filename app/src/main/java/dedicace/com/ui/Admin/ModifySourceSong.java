package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.SourceSong;

//todo voir lorsque finish que l'on reinitialise les données changées
public class ModifySourceSong extends AppCompatActivity implements SourceSongAdapter.OnItemListener{

    private RecyclerView recyclerSS;
    private SourceSongAdapter sourceSongAdapter;
    private List<SourceSong> listSourceSongs = new ArrayList<>();
    private List<String> listId = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG ="coucou";
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_source_song);

        db=FirebaseFirestore.getInstance();

        getListSourceSongs();
    }

    private void getListSourceSongs() {
        try {
            db.collection("sourceSongs")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, "MSS onComplete: sourceSongs " + Thread.currentThread().getName());
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "NDS-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));
                                    //todo voir comment écrire une seule ligne avec ToObject

                                    String titre, groupe, baseUrlOriginalSong, urlCloudBackground,idDocument;
                                    Date maj;
                                    int duration;

                                    idDocument = document.getId();
                                    listId.add(idDocument);

                                    titre = (String) document.getData().get("titre");
                                    groupe = (String) document.getData().get("groupe");
                                    duration = ((Long) document.getData().get("duration")).intValue();
                                    baseUrlOriginalSong = (String) document.getData().get("original_song");
                                    maj = (Date) document.getData().get("maj");
                                    urlCloudBackground = (String) document.getData().get("background");

                                    Log.d(TAG, "MSS-exec onComplete:A SourceSongs " + titre + " " + groupe + " " + duration + " " + baseUrlOriginalSong + " " + maj + " " + urlCloudBackground);
                                    SourceSong sourceSong = new SourceSong(titre, groupe, duration, urlCloudBackground, baseUrlOriginalSong, maj);
                                    listSourceSongs.add(sourceSong);
                                }

                                recyclerSS = findViewById(R.id.recyclerview_cloud_SS);
                                sourceSongAdapter = new SourceSongAdapter(listSourceSongs);
                                layoutManager = new LinearLayoutManager(ModifySourceSong.this);
                                recyclerSS.setLayoutManager(layoutManager);
                                recyclerSS.setHasFixedSize(true);
                                recyclerSS.setAdapter(sourceSongAdapter);

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

        Intent startDetailsSSActivity = new Intent(ModifySourceSong.this,ModifySourceSongDetails.class);
        Bundle args = new Bundle();
        args.putString("idSS", listId.get(i));
        args.putString("oldTitre",listSourceSongs.get(i).getTitre());
        args.putString("oldGroupe",listSourceSongs.get(i).getGroupe());
        args.putInt("oldDuration",listSourceSongs.get(i).getDuration());
        startDetailsSSActivity.putExtra("bundleSS",args);
        startActivity(startDetailsSSActivity);

    }
}
