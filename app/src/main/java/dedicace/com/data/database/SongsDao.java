package dedicace.com.data.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SongsDao {

    @Query("SELECT * FROM song")
    List<Song> getAllSongs();

    @Query("SELECT * FROM song WHERE (titre_song IN (:titre)) AND (source_enregistrement IN (:recordSource) AND (update_phone IS null))")
    List<Song> getSongsOnCloud(String titre, RecordSource recordSource);

    @Query("SELECT * FROM song WHERE titre_song IN (:titre)")
    List<Song> getSongsBySourceSong(String titre);

    @Query("SELECT * FROM song WHERE source_enregistrement IN (:source)")
    List<Song> getSongsBySource(RecordSource source);

    @Query("SELECT * FROM song WHERE (source_enregistrement IN (:source)) AND (titre_song IN (:titre))")
    List<Song> getSongsBySourceTitre(RecordSource source, String titre);

    @Query("SELECT * FROM song WHERE (titre_song IN (:titre)) AND (pupitre IN (:pupitre)) AND (source_enregistrement IN (:source))")
    Song getSongsByTitrePupitreSource(String titre, Pupitre pupitre, RecordSource source);

    @Query("SELECT * FROM song WHERE (titre_song IN (:titre)) AND (source_enregistrement IN (:source))")
    List<Song> getSongsByTitreSource(String titre, RecordSource source);

    @Query("SELECT * FROM song WHERE (titre_song IN (:titre)) AND (source_enregistrement IN (:recordSource) AND (fichier_mp3 NOT null)) ORDER BY pupitre ASC")
    List<Song> getSongsOnPhone(String titre, RecordSource recordSource);

    @Query("SELECT * FROM song WHERE (titre_song IN (:titre)) AND (source_enregistrement IN (:recordSource) AND (update_phone NOT null)) ORDER BY pupitre ASC")
    List<Song> getSongsOnPhoneA(String titre, RecordSource recordSource);

    @Query("SELECT * FROM song WHERE (titre_song IN (:titre)) AND (source_enregistrement IN (:source)) ORDER BY pupitre ASC ")
    List<Song> getSongOrderedByPupitre(String titre, RecordSource source);

    @Query("SELECT * FROM song ORDER BY songId DESC LIMIT 1")
    Song getLastSong();

    @Query("SELECT * FROM song WHERE songId IN (:songId)")
    Song getSongById(int songId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(Song song);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSongs(Song... songs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<Song> songs);

    @Query("DELETE FROM song")
    void deleteAll();

    @Update
    void updateSong(Song song);

    @Update
    void updatesSongs(List<Song> songs);

    @Update
    int updateSongs(Song...songs);

    @Delete
    void deleteSong(Song song);

    @Delete
    void deleteSongs(List<Song> songs);


}
