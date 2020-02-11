package dedicace.com.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;


@Entity(indices = {@Index(value = {"id_choriste_cloud"}, unique = true)})
public class Choriste {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_choriste_local")
    private int choristeId;

    @ColumnInfo(name ="id_choriste_cloud")
    private String idChoristeCloud;

    @ColumnInfo(name="id_chorale")
    private String idChorale;

    @ColumnInfo(name = "nom")
    private String nom;

    @ColumnInfo(name = "prenom")
    private String prenom;

    @ColumnInfo(name = "pupitre")
    private Pupitre pupitre;

    @ColumnInfo(name = "adresse")
    private String adresse;

    @ColumnInfo(name = "fix_tel")
    private String fixTel;

    @ColumnInfo(name = "port_tel")
    private String portTel;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "role_choeur")
    private String roleChoeur;

    @ColumnInfo(name = "role_admin")
    private String roleAdmin;

    @ColumnInfo(name = "url_local_photo")
    private String urlLocalPhoto;

    @ColumnInfo(name = "url_cloud_photo")
    private String urlCloudPhoto;

    @ColumnInfo(name = "update_phone")
    private Date updatePhone;

    @ColumnInfo(name ="update_bg_phone")
    private Date updatePhotoPhone;

    public Choriste(int choristeId, String idChoristeCloud, String idChorale, String nom, String prenom, Pupitre pupitre, String adresse, String fixTel, String portTel, String email, String roleChoeur, String roleAdmin, String urlLocalPhoto, String urlCloudPhoto, Date updatePhone, Date updatePhotoPhone) {
        this.choristeId = choristeId;
        this.idChoristeCloud = idChoristeCloud;
        this.idChorale = idChorale;
        this.nom = nom;
        this.prenom = prenom;
        this.pupitre = pupitre;
        this.adresse = adresse;
        this.fixTel = fixTel;
        this.portTel = portTel;
        this.email = email;
        this.roleChoeur = roleChoeur;
        this.roleAdmin = roleAdmin;
        this.urlLocalPhoto = urlLocalPhoto;
        this.urlCloudPhoto = urlCloudPhoto;
        this.updatePhone = updatePhone;
        this.updatePhotoPhone = updatePhotoPhone;
    }

    @Ignore
    public Choriste(String nom) {
        this.nom = nom;
    }

    @Ignore
    public Choriste(String nom, Pupitre pupitre, Date updatePhotoPhone) {
        this.nom = nom;
        this.pupitre = pupitre;
        this.updatePhotoPhone = updatePhotoPhone;
    }

    @Ignore
    public Choriste() {
    }

    public String getIdChorale() {
        return idChorale;
    }

    public void setIdChorale(String idChorale) {
        this.idChorale = idChorale;
    }

    public int getChoristeId() {
        return choristeId;
    }

    public void setChoristeId(int choristeId) {
        this.choristeId = choristeId;
    }

    public String getIdChoristeCloud() {
        return idChoristeCloud;
    }

    public void setIdChoristeCloud(String idChoristeCloud) {
        this.idChoristeCloud = idChoristeCloud;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Pupitre getPupitre() {
        return pupitre;
    }

    public void setPupitre(Pupitre pupitre) {
        this.pupitre = pupitre;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getFixTel() {
        return fixTel;
    }

    public void setFixTel(String fixTel) {
        this.fixTel = fixTel;
    }

    public String getPortTel() {
        return portTel;
    }

    public void setPortTel(String portTel) {
        this.portTel = portTel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrlCloudPhoto() {
        return urlCloudPhoto;
    }

    public void setUrlCloudPhoto(String urlCloudPhoto) {
        this.urlCloudPhoto = urlCloudPhoto;
    }

    public Date getUpdatePhone() {
        return updatePhone;
    }

    public void setUpdatePhone(Date updatePhone) {
        this.updatePhone = updatePhone;
    }

    public Date getUpdatePhotoPhone() {
        return updatePhotoPhone;
    }

    public void setUpdatePhotoPhone(Date updatePhotoPhone) {
        this.updatePhotoPhone = updatePhotoPhone;
    }

    public String getUrlLocalPhoto() {
        return urlLocalPhoto;
    }

    public void setUrlLocalPhoto(String urlLocalPhoto) {
        this.urlLocalPhoto = urlLocalPhoto;
    }

    public String getRoleChoeur() {
        return roleChoeur;
    }

    public void setRoleChoeur(String roleChoeur) {
        this.roleChoeur = roleChoeur;
    }

    public String getRoleAdmin() {
        return roleAdmin;
    }

    public void setRoleAdmin(String roleAdmin) {
        this.roleAdmin = roleAdmin;
    }
}

