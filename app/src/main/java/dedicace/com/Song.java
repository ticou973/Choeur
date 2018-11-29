package dedicace.com;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
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

    /*@ColumnInfo(name = "fichier_mp3")
    private Resources songRaw;

    @ColumnInfo(name = "image_fond")
    private Bitmap bitmap;*/


    public Song(String titre, String groupe) {
        this.titre = titre;
        this.groupe = groupe;
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
}
