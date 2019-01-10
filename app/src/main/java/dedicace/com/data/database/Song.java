package dedicace.com.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(foreignKeys = @ForeignKey(entity = SourceSong.class,
        parentColumns = "titre",
        childColumns = "titre_song"),indices = {@Index(value = {"titre_song"})})
public class Song {

    @PrimaryKey(autoGenerate = true)
    private int songId;

    @ColumnInfo (name ="titre_song")
    private String titre_song;

    @ColumnInfo(name = "source_enregistrement")
    private RecordSource recordSource;

    @ColumnInfo(name = "pupitre")
    private Pupitre pupitre;

    @ColumnInfo(name = "fichier_mp3")
    private String songPath;

    @ColumnInfo(name ="update_phone")
    private Date updatePhone;


//todo gérer lorsque le mediaplayer est lancé à 2 items différents
    @Ignore
    public Song(String sourceSongTitre, RecordSource recordSource, Pupitre pupitre, String songPath, Date updatePhone) {
        this.titre_song = sourceSongTitre;
        this.recordSource = recordSource;
        this.pupitre = pupitre;
        this.songPath = songPath;
        this.updatePhone=updatePhone;
    }

    @Ignore
    public Song() {
    }

    public Song(int songId, String sourceSongTitre, RecordSource recordSource, Pupitre pupitre, String songPath, Date updatePhone) {
        this.songId = songId;
        this.titre_song = sourceSongTitre;
        this.recordSource = recordSource;
        this.pupitre = pupitre;
        this.songPath = songPath;
        this.updatePhone=updatePhone;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public RecordSource getRecordSource() {
        return recordSource;
    }

    public void setRecordSource(RecordSource recordSource) {
        this.recordSource = recordSource;
    }

    public Pupitre getPupitre() {
        return pupitre;
    }

    public void setPupitre(Pupitre pupitre) {
        this.pupitre = pupitre;
    }

    public String getSourceSongTitre() {
        return titre_song;
    }

    public void setSourceSongTitre(String sourceSongTitre) {
        this.titre_song = sourceSongTitre;
    }

    public Date getUpdatePhone() {
        return updatePhone;
    }

    public void setUpdatePhone(Date updatePhone) {
        this.updatePhone = updatePhone;
    }
}
