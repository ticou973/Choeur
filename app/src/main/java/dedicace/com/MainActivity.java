package dedicace.com;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private  RecyclerView recyclerView;
    private SongsAdapter songsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Song> songs;

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

        recyclerView.setAdapter(songsAdapter);

    }

    private void initData() {
        choeurDataBase = Room.databaseBuilder(this,AppDataBase.class,"ChoeurDataBase")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        choeurDataBase.songsDao().deleteAll();

        SourceSong sourceSong1 = new SourceSong("Menuet","Krieger",90,R.drawable.hand,"");
        SourceSong sourceSong2 = new SourceSong("Concerto","Van Den BudenMayer",270,R.drawable.water,"");

        Song song1 = new Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.TUTTI,R.raw.menuett_krieger);
        Song song2 = new  Song(sourceSong2,RecordSource.LIVE,Pupitre.ALTO,R.raw.van_den_budenmayer_concerto);

        choeurDataBase.songsDao().insertSongs(song1,song2);

        songs=choeurDataBase.songsDao().getAllSongs();

    }
}
