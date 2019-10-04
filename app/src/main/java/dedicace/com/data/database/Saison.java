package dedicace.com.data.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(indices = {@Index(value = {"saison_name"})})
public class Saison {

    @PrimaryKey(autoGenerate = true)
    private int saisonId;

    @ColumnInfo(name = "id_saison_cloud")
    private String idsaisonCloud;

    @ColumnInfo(name = "saison_name")
    private String saisonName;

    @ColumnInfo(name = "update_phone")
    private Date updatePhone;

    @ColumnInfo(name = "id_spectacles")
    private ArrayList<String> idSpectacles = new ArrayList<>();


    public Saison(int saisonId, String idsaisonCloud, String saisonName, Date updatePhone, ArrayList<String> idSpectacles) {
        this.saisonId = saisonId;
        this.idsaisonCloud = idsaisonCloud;
        this.saisonName = saisonName;
        this.updatePhone = updatePhone;
        this.idSpectacles = idSpectacles;

    }

    @Ignore
    public Saison(String idCloud, String saisonName, List<String> idSpectacles, Date maj) {
        this.idsaisonCloud=idCloud;
        this.saisonName=saisonName;
        this.idSpectacles.addAll(idSpectacles);
        this.updatePhone=maj;
    }

    @Ignore
    public Saison() {
    }

    public int getSaisonId() {
        return saisonId;
    }

    public void setSaisonId(int saisonId) {
        this.saisonId = saisonId;
    }

    public String getIdsaisonCloud() {
        return idsaisonCloud;
    }

    public void setIdsaisonCloud(String idsaisonCloud) {
        this.idsaisonCloud = idsaisonCloud;
    }

    public String getSaisonName() {
        return saisonName;
    }

    public void setSaisonName(String saisonName) {
        this.saisonName = saisonName;
    }

    public Date getUpdatePhone() {
        return updatePhone;
    }

    public void setUpdatePhone(Date updatePhone) {
        this.updatePhone = updatePhone;
    }

    public ArrayList<String> getIdSpectacles() {
        return idSpectacles;
    }

    public void setIdSpectacles(ArrayList<String> idSpectacles) {
        this.idSpectacles = idSpectacles;
    }
}
