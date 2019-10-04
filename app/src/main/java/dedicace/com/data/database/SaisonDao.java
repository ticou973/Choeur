package dedicace.com.data.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SaisonDao {

    @Query("SELECT * FROM saison")
    List<Saison> getAllSaisons();

    @Query("SELECT * FROM saison WHERE (id_saison_cloud IN (:idSaison))")
    Saison getSaisonById(String idSaison);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<Saison> saisons);


    @Delete
    int deleteSaisons(List<Saison> saisons);

    @Update
    int upDateSaisons(List<Saison> saisons);

}
