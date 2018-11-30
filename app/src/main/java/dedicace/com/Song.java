package dedicace.com;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Song {

    @PrimaryKey(autoGenerate = true)
    private int songId;

    @Embedded
    private SourceSong sourceSong;

    @ColumnInfo(name = "source_enregistrement")
    private RecordSource recordSource;

    @ColumnInfo(name = "pupitre")
    Pupitre pupitre;

    @ColumnInfo(name = "fichier_mp3")
    private int songResId;


    public Song(SourceSong sourceSong, RecordSource recordSource, Pupitre pupitre, int songResId) {
        this.sourceSong = sourceSong;
        this.recordSource = recordSource;
        this.pupitre = pupitre;
        this.songResId = songResId;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public SourceSong getSourceSong() {
        return sourceSong;
    }

    public void setSourceSong(SourceSong sourceSong) {
        this.sourceSong = sourceSong;
    }

    public int getSongResId() {
        return songResId;
    }

    public void setSongResId(int songResId) {
        this.songResId = songResId;
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
}
