package dedicace.com;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private  RecyclerView recyclerView;
    private SongsAdapter songsAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<SourceSong> songs = new ArrayList<>();

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

    private void initData() {
        choeurDataBase = Room.databaseBuilder(this,AppDataBase.class,"ChoeurDataBase")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        choeurDataBase.songsDao().deleteAll();

        SourceSong sourceSong1 = new SourceSong("Menuet","Krieger",90,R.drawable.hand,"");
        SourceSong sourceSong2 = new SourceSong("Concerto","Van Den BudenMayer",270,R.drawable.water,"");
        SourceSong sourceSong3 = new SourceSong("variation bwv988","GoldBerg",150,R.drawable.yinyang,"");
        SourceSong sourceSong4 = new SourceSong("tempête en mer","Vivaldi",90,R.drawable.hand,"");
        SourceSong sourceSong5 = new SourceSong("Agnus Dei","Zbigniew Preisner",192,R.drawable.yinyang,"");
        SourceSong sourceSong6 = new SourceSong("L'eau","Jeanne Cherhal",190,R.drawable.water,"");

        Song song1 = new Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.TUTTI,R.raw.menuett_krieger);
        Song song2 = new  Song(sourceSong2,RecordSource.LIVE,Pupitre.ALTO,R.raw.van_den_budenmayer_concerto);
        Song song3 = new  Song(sourceSong3,RecordSource.LIVE,Pupitre.SOPRANO,R.raw.goldberg_variation_bwv988);
        Song song4 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.ALTO,R.raw.vivaldi_tempete_en_mer);
        Song song5 = new  Song(sourceSong5,RecordSource.LIVE,Pupitre.TENOR,R.raw.zbigniew_preisner_agnus_dei);
        Song song6 = new Song(sourceSong1,RecordSource.BANDE_SON,Pupitre.TENOR,R.raw.menuett_krieger);
        Song song7 = new  Song(sourceSong1,RecordSource.LIVE,Pupitre.ALTO,R.raw.menuett_krieger);
        Song song8 = new  Song(sourceSong1,RecordSource.LIVE,Pupitre.SOPRANO,R.raw.menuett_krieger);
        Song song9 = new  Song(sourceSong4,RecordSource.BANDE_SON,Pupitre.BASS,R.raw.vivaldi_tempete_en_mer);
        Song song10 = new  Song(sourceSong5,RecordSource.LIVE,Pupitre.TUTTI,R.raw.zbigniew_preisner_agnus_dei);

        choeurDataBase.songsDao().insertSongs(song1,song2,song3,song4,song5,song6,song7,song8,song9,song10);

        songs.add(sourceSong1);
        songs.add(sourceSong2);
        songs.add(sourceSong3);
        songs.add(sourceSong4);
        songs.add(sourceSong5);
        songs.add(sourceSong6);


    }
}
