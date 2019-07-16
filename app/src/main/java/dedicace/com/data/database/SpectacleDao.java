package dedicace.com.data.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SpectacleDao {

    @Query("SELECT * FROM spectacle")
    List<Spectacle> getAllSpectacles();

    @Query("SELECT * FROM spectacle WHERE (id_spectacle_cloud IN (:idSpectacle))")
    Spectacle getSpectacleById(String idSpectacle);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<Spectacle> spectacles);
}
