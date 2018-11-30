package dedicace.com;

import android.arch.persistence.room.ColumnInfo;

public class SourceSong {

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

    public SourceSong(String titre, String groupe, int duration, int bgSong, String baseUrlOriginalSong) {
        this.titre = titre;
        this.groupe = groupe;
        this.duration = duration;
        this.bgSong = bgSong;
        this.baseUrlOriginalSong = baseUrlOriginalSong;
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
}
