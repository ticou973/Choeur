package dedicace.com.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SourceSongDao {
    @Query("SELECT * FROM sourcesong")
    LiveData<List<SourceSong>> getAllSourceSongs();

    @Query("SELECT * FROM sourcesong")
    List<SourceSong> getAllSources();

    @Query("SELECT * FROM sourcesong WHERE titre in (:titre)")
    SourceSong getSourceSongByTitre(String titre);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<SourceSong> sourceSongs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSourceSong(SourceSong sourceSong);

    @Query("DELETE FROM sourcesong")
    void deleteAll();
}
