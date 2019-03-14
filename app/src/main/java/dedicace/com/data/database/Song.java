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
    private String sourceSongTitre;

    @ColumnInfo(name = "source_enregistrement")
    private RecordSource recordSource;

    @ColumnInfo(name = "pupitre")
    private Pupitre pupitre;

    @ColumnInfo(name = "fichier_mp3")
    private String songPath;

    @ColumnInfo(name = "url_cloud_mp3")
    private String urlCloudMp3;

    //date de la bdd song
    @ColumnInfo(name ="update_phone")
    private Date updatePhone;

    //date de la maj du mp3 sur tel
    @ColumnInfo(name = "update_mp3")
    private Date updatePhoneMp3;


//todo gérer lorsque le mediaplayer est lancé à 2 items différents
    @Ignore
    public Song(String sourceSongTitre, RecordSource recordSource, Pupitre pupitre, String songPath, Date updatePhone) {
        this.sourceSongTitre = sourceSongTitre;
        this.recordSource = recordSource;
        this.pupitre = pupitre;
        this.songPath = songPath;
        this.updatePhone=updatePhone;
    }

    @Ignore
    public Song() {
    }

    @Ignore
    public Song(String sourceSongTitre, RecordSource recordSource, Pupitre pupitre, Date updatePhone, Date updatePhoneMp3) {
        this.sourceSongTitre = sourceSongTitre;
        this.recordSource = recordSource;
        this.pupitre = pupitre;
        this.updatePhone = updatePhone;
        this.updatePhoneMp3 = updatePhoneMp3;
    }

    public Song(int songId, String sourceSongTitre, RecordSource recordSource, Pupitre pupitre, String songPath, String urlCloudMp3, Date updatePhone, Date updatePhoneMp3) {
        this.songId = songId;
        this.sourceSongTitre = sourceSongTitre;
        this.recordSource = recordSource;
        this.pupitre = pupitre;
        //todo voir pour renommer celuilà songLocalPath
        this.songPath = songPath;
        this.updatePhone=updatePhone;
        this.updatePhoneMp3=updatePhoneMp3;
        this.urlCloudMp3=urlCloudMp3;
    }

    @Ignore
    public Song(String sourceSongTitre, RecordSource recordSource, Pupitre pupitre, Date updatePhone, String urlCloudMp3) {
        this.sourceSongTitre = sourceSongTitre;
        this.recordSource = recordSource;
        this.pupitre = pupitre;
        this.updatePhone = updatePhone;
        this.urlCloudMp3 =urlCloudMp3;
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
        return sourceSongTitre;
    }

    public void setSourceSongId(int sourceSongId) {
        this.sourceSongTitre = sourceSongTitre;
    }

    public void setSourceSongTitre(String sourceSongTitre) {
        this.sourceSongTitre = sourceSongTitre;
    }

    public Date getUpdatePhone() {
        return updatePhone;
    }

    public void setUpdatePhone(Date updatePhone) {
        this.updatePhone = updatePhone;
    }

    public Date getUpdatePhoneMp3() {
        return updatePhoneMp3;
    }

    public void setUpdatePhoneMp3(Date updatePhoneMp3) {
        this.updatePhoneMp3 = updatePhoneMp3;
    }

    public String getUrlCloudMp3() {
        return urlCloudMp3;
    }

    public void setUrlCloudMp3(String urlCloudMp3) {
        this.urlCloudMp3 = urlCloudMp3;
    }
}
