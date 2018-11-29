package dedicace.com;


import android.arch.persistence.room.Database;

@Database(entities ={Song.class}, version = 1, exportSchema = false)
public abstract class AppDataBase {

    public abstract SongsDao songsDao();
}
