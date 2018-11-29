package dedicace.com;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SongsDao {

    @Query("SELECT * FROM song")
    List<Song> getAllSongs();


}
