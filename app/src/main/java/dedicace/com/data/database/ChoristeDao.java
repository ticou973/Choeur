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
public interface ChoristeDao {

    @Query("SELECT * FROM choriste")
    List<Choriste> getAllChoristes();

    @Query("SELECT * FROM choriste")
    LiveData<List<Choriste>> getAllChoristesLive();

    @Query("SELECT * FROM choriste WHERE id_choriste_cloud in (:idCloud)")
    Choriste getChoristeByIdCloud(String idCloud);

//    @Query("SELECT * FROM choriste WHERE (nom IN (:nom) AND (prenom IN (:prenom))")
 //   Choriste getChorsiteByNomPrenom(String nom,String prenom);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<Choriste> choristes);

    @Query("DELETE FROM choriste")
    void deleteAll();

    @Update
    void updateChoriste(Choriste choriste);

    @Update
    int upDateChoristes(List<Choriste> choristes);

    @Delete
    int deleteChoristes(List<Choriste> choristes);
}
