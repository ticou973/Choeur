package dedicace.com.data.database;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

@Database(entities ={Song.class, SourceSong.class}, version = 8, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDataBase extends RoomDatabase {

    private static final String LOG_TAG = AppDataBase.class.getSimpleName();
    private static final String DATABASE_NAME = "ChoeurDataBase";

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static AppDataBase sInstance;

    public static AppDataBase getInstance(Context context) {
        Log.d(LOG_TAG, "Getting the database");
        Log.d("coucou", "getInstance: database");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDataBase.class, AppDataBase.DATABASE_NAME)
                        //todo voir comment supprimer le fallback
                        .fallbackToDestructiveMigration().build();
                Log.d(LOG_TAG, "Made new database");
                Log.d("coucou", "getInstance: new database");
            }
        }
        return sInstance;
    }
    public abstract SongsDao songsDao();
    public abstract  SourceSongDao sourceSongDao();
}
