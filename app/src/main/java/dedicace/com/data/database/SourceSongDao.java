package dedicace.com.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SourceSongDao {
    @Query("SELECT * FROM sourcesong")
    LiveData<List<SourceSong>> getAllSourceSongs();

    @Query("SELECT * FROM sourcesong")
    List<SourceSong> getAllSources();

    @Query("SELECT * FROM sourcesong WHERE titre in (:titre)")
    SourceSong getSourceSongByTitre(String titre);

    @Query("SELECT * FROM sourcesong WHERE id_source_song in (:idTitre)")
    SourceSong getSourceSongByIdCloud(String idTitre);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<SourceSong> sourceSongs);

    @Query("DELETE FROM sourcesong")
    void deleteAll();

    @Update
    void updateSourceSong(SourceSong sourceSong);

    @Update
    int upDateSourceSongs(List<SourceSong> sourceSongs);

    @Delete
    void deleteSourceSongs(List<SourceSong> sourceSongs);
}
