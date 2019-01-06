package dedicace.com.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"titre"},
        unique = true)})
public class SourceSong {
    @PrimaryKey(autoGenerate = true)
    private int sourceSongId;

    @ColumnInfo(name = "titre")
    private String titre;

    @ColumnInfo(name = "groupe")
    private String groupe;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "background")
    private int bgSong;

    @ColumnInfo(name = "original_song")
    private String baseUrlOriginalSong;

    @Ignore
    public SourceSong(String titre, String groupe, int duration, int bgSong, String baseUrlOriginalSong) {
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.bgSong = bgSong;
        this.baseUrlOriginalSong = baseUrlOriginalSong;
    }

    public SourceSong(int sourceSongId, String titre, String groupe, int duration, int bgSong, String baseUrlOriginalSong) {
        this.sourceSongId = sourceSongId;
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.bgSong = bgSong;
        this.baseUrlOriginalSong = baseUrlOriginalSong;
    }

    @Ignore
    public SourceSong(){

    }



    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBgSong() {
        return bgSong;
    }

    public void setBgSong(int bgSong) {
        this.bgSong = bgSong;
    }

    public String getBaseUrlOriginalSong() {
        return baseUrlOriginalSong;
    }

    public void setBaseUrlOriginalSong(String baseUrlOriginalSong) {
        this.baseUrlOriginalSong = baseUrlOriginalSong;
    }

    public int getSourceSongId() {
        return sourceSongId;
    }

}
