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

    @Query("SELECT * FROM song WHERE titre IN (:titre)")
    List<Song> getSongsBySourceSong(String titre);

    @Query("SELECT * FROM song WHERE (titre IN (:titre)) AND (pupitre IN (:pupitre)) AND (source_enregistrement IN (:source))")
    Song getSongsByTitrePupitreSource(String titre, Pupitre pupitre, RecordSource source);

    @Query("SELECT * FROM song WHERE (titre IN (:titre)) AND (source_enregistrement IN (:source))")
    List<Song> getSongsByTitreSource(String titre, RecordSource source);


    @Query("SELECT * FROM song WHERE (titre IN (:titre)) AND (source_enregistrement IN (:source)) ORDER BY pupitre ASC ")
    List<Song> getSongOrderedByPupitre(String titre, RecordSource source);

    @Query("SELECT * FROM song ORDER BY songId DESC LIMIT 1")
    Song getLastSong();




    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSong(Song song);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSongs(Song... songs);

    @Query("DELETE FROM song")
    void deleteAll();


}
