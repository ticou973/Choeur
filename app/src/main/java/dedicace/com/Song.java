package dedicace.com;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Song {

    @PrimaryKey(autoGenerate = true)
    private int songId;

    @ColumnInfo(name = "titre")
    private String titre;

    @ColumnInfo(name = "groupe")
    private String groupe;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "fichier_mp3")
    private int songResId;

    @ColumnInfo(name = "background")
    private int bgSong;




    @Ignore
    public Song(String titre, String groupe) {
        this.titre = titre;
        this.groupe = groupe;
    }

    @Ignore
    public Song(String titre, String groupe, int songResId) {
        this.titre = titre;
        this.groupe = groupe;
        this.songResId = songResId;
    }

    public Song(String titre, String groupe, int songResId, int bgSong) {
        this.titre = titre;
        this.groupe = groupe;
        this.songResId = songResId;
        this.bgSong = bgSong;
    }

    public String getTitre() {
        return titre;
    }

    public String getGroupe() {
        return groupe;
    }

    public int getSongId() {
        return songId;
    }

    public int getDuration() {
        return duration;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSongResId() {
        return songResId;
    }

    public void setSongResId(int songResId) {
        this.songResId = songResId;
    }

    public int getBgSong() {
        return bgSong;
    }

    public void setBgSong(int bgSong) {
        this.bgSong = bgSong;
    }
}
