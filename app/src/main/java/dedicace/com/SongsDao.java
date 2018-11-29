package dedicace.com;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SongsDao {

    @Query("SELECT * FROM song")
    List<Song> getAllSongs();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSong(Song song);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSongs(Song... songs);


}
