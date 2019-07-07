package dedicace.com.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;

@Entity(indices = {@Index(value = {"spectacle_name"})})
public class Spectacle {

    @PrimaryKey(autoGenerate = true)
    private int spectacleId;

    @ColumnInfo(name = "id_spectacle_cloud")
    private String idSpectacleCloud;

    @ColumnInfo(name = "spectacle_name")
    private String spectacleName;

    @ColumnInfo(name = "update_phone")
    private Date updatePhone;

    @ColumnInfo(name = "id_titres")
    private ArrayList<String> idTitresSongs= new ArrayList<>();

    @ColumnInfo(name = "spectacle_dates")
    private ArrayList<Date> spectacleDates= new ArrayList<>();

    @ColumnInfo(name = "spectacle_lieux")
    private ArrayList<String> spectacleLieux= new ArrayList<>();


    public Spectacle(int spectacleId, String idSpectacleCloud, String spectacleName, Date updatePhone, ArrayList<String> idTitresSongs, ArrayList<Date> spectacleDates, ArrayList<String> spectacleLieux) {
        this.spectacleId = spectacleId;
        this.idSpectacleCloud = idSpectacleCloud;
        this.spectacleName = spectacleName;
        this.updatePhone = updatePhone;
        this.idTitresSongs = idTitresSongs;
        this.spectacleDates = spectacleDates;
        this.spectacleLieux = spectacleLieux;
    }

    @Ignore
    public Spectacle() {
    }

    @Ignore
    public Spectacle(String idSpectacle, String spectacleName, ArrayList<String> idSourceSongs, ArrayList<String> spectacleLieux, ArrayList<Date> spectacleDates, Date maj) {
        this.idSpectacleCloud=idSpectacle;
        this.spectacleName=spectacleName;
        this.idTitresSongs=idSourceSongs;
        this.spectacleLieux=spectacleLieux;
        this.spectacleDates=spectacleDates;
        this.updatePhone=maj;
    }

    public int getSpectacleId() {
        return spectacleId;
    }

    public void setSpectacleId(int spectacleId) {
        this.spectacleId = spectacleId;
    }

    public String getIdSpectacleCloud() {
        return idSpectacleCloud;
    }

    public void setIdSpectacleCloud(String idSpectacleCloud) {
        this.idSpectacleCloud = idSpectacleCloud;
    }

    public String getSpectacleName() {
        return spectacleName;
    }

    public void setSpectacleName(String spectacleName) {
        this.spectacleName = spectacleName;
    }

    public Date getUpdatePhone() {
        return updatePhone;
    }

    public void setUpdatePhone(Date updatePhone) {
        this.updatePhone = updatePhone;
    }

    public ArrayList<String> getIdTitresSongs() {
        return idTitresSongs;
    }

    public void setIdTitresSongs(ArrayList<String> idTitresSongs) {
        this.idTitresSongs = idTitresSongs;
    }

    public ArrayList<Date> getSpectacleDates() {
        return spectacleDates;
    }

    public void setSpectacleDates(ArrayList<Date> spectacleDates) {
        this.spectacleDates = spectacleDates;
    }

    public ArrayList<String> getSpectacleLieux() {
        return spectacleLieux;
    }

    public void setSpectacleLieux(ArrayList<String> spectacleLieux) {
        this.spectacleLieux = spectacleLieux;
    }
}
