package dedicace.com;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities ={Song.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDataBase extends RoomDatabase {

    public abstract SongsDao songsDao();
}
