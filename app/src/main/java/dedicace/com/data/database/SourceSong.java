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
//todo à retirer dès que inutile
    @ColumnInfo(name = "background")
    private int bgSong;

    @ColumnInfo(name="image_background")
    private String background;

    @ColumnInfo(name = "url_cloud_background")
    private String urlCloudBackground;

    @ColumnInfo(name = "original_song")
    private String baseUrlOriginalSong;

    @ColumnInfo(name = "update_phone")
    private Date updatePhone;


    @Ignore
    public SourceSong(String titre, String groupe, int duration, int bgSong, String baseUrlOriginalSong, Date updatePhone) {
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.bgSong = bgSong;
        this.baseUrlOriginalSong = baseUrlOriginalSong;
        this.updatePhone=updatePhone;
    }

    public SourceSong(int sourceSongId, String titre, String groupe, int duration, int bgSong, String background, String urlCloudBackground, String baseUrlOriginalSong,Date updatePhone) {
        this.sourceSongId = sourceSongId;
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.bgSong = bgSong;
        this.baseUrlOriginalSong = baseUrlOriginalSong;
        this.updatePhone=updatePhone;
        this.urlCloudBackground=urlCloudBackground;
        this.background=background;

    }

    @Ignore
    public SourceSong(String titre, String groupe, int duration, String urlCloudBackground, String baseUrlOriginalSong, Date updatePhone) {
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.baseUrlOriginalSong = baseUrlOriginalSong;
        this.updatePhone = updatePhone;
        this.urlCloudBackground=urlCloudBackground;
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

    public Date getUpdatePhone() {
        return updatePhone;
    }

    public void setUpdatePhone(Date updatePhone) {
        this.updatePhone = updatePhone;
    }

    public String getUrlCloudBackground() {
        return urlCloudBackground;
    }

    public void setUrlCloudBackground(String urlCloudBackground) {
        this.urlCloudBackground = urlCloudBackground;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }
}
