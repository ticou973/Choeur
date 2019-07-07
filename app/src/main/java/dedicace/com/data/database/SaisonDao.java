package dedicace.com.data.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SaisonDao {

    @Query("SELECT * FROM saison")
    List<Saison> getAllSaisons();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<Saison> saisons);

}
