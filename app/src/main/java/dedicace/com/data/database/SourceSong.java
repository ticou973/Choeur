package dedicace.com.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

//todo mettre un contructeur avec les éléments de firebase

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
    private int background;

    @ColumnInfo(name = "original_song")
    private String original_song;

    @ColumnInfo(name = "update_phone")
    private Date update_phone;

    //todo à voir comment compléter et faire un constructeur
    @ColumnInfo(name = "maj")
    private Date maj;

    @Ignore
    public SourceSong(String titre, String groupe, int duration, int bgSong, String baseUrlOriginalSong, Date updatePhone) {
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.background = bgSong;
        this.original_song = baseUrlOriginalSong;
        this.update_phone=updatePhone;
    }

    public SourceSong(int sourceSongId, String titre, String groupe, int duration, int bgSong, String baseUrlOriginalSong,Date updatePhone) {
        this.sourceSongId = sourceSongId;
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.background = bgSong;
        this.original_song = baseUrlOriginalSong;
        this.update_phone=updatePhone;
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
        return background;
    }

    public void setBgSong(int bgSong) {
        this.background = bgSong;
    }

    public String getBaseUrlOriginalSong() {
        return original_song;
    }

    public void setBaseUrlOriginalSong(String baseUrlOriginalSong) {
        this.original_song = baseUrlOriginalSong;
    }

    public int getSourceSongId() {
        return sourceSongId;
    }

    public Date getUpdatePhone() {
        return update_phone;
    }

    public void setUpdatePhone(Date updatePhone) {
        this.update_phone = updatePhone;
    }
}
