package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import dedicace.com.data.database.Choriste;
import dedicace.com.data.database.Pupitre;
import dedicace.com.ui.PlaySong.ChoristeSyncIntentService;
import dedicace.com.utilities.AppExecutors;
import dedicace.com.utilities.SongsUtilities;

public class TrombiNetWorkDataSource {
    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "coucou";
    private static TrombiNetWorkDataSource sInstance;
    private final Context mContext;
    private AppExecutors mExecutors;
    private OnChoristeNDSListener mlistener;

    //DB
    private FirebaseFirestore db;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String idChorale;
    private Date majDateCloudDataBase;
    private Long majCloudDBLong;
    private final MutableLiveData<Long> mMajDbCloudLong;


    private final MutableLiveData<List<Choriste>> mChoristes;
    private boolean deleted = false;
    private int increment,increment1;
    private final MutableLiveData<String> downloads;


    private List<Choriste> choristes = new ArrayList<>();



    private TrombiNetWorkDataSource(Context context, Context mAContext, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mChoristes = new MutableLiveData<>();
        mMajDbCloudLong = new MutableLiveData<>();
        downloads = new MutableLiveData<>();

        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        //todo voir si cela fonctionne dans tous les cas et supprimer si pas utile dans le progress
        if(mAContext!=context){
            mlistener = (TrombiNetWorkDataSource.OnChoristeNDSListener) mAContext;
        }
    }

    public Context getContext() {
        return mContext;
    }

    public void getMajDateCloudDataBase() {

        Log.d(LOG_TAG, "TDS startFetchSongsService: début pour maj");
        Intent intentToFetchMaj = new Intent(mContext, ChoristeSyncIntentService.class);
        intentToFetchMaj.putExtra("origine","maj");
        mContext.startService(intentToFetchMaj);
        Log.d(LOG_TAG, "TDS Service created pour maj");
    }

    public void fetchMajClouDb() {

        idChorale = sharedPreferences.getString("idchorale","");
        Log.d(LOG_TAG, "TDS getMajDateCloudDataBase: "+idChorale);

        if(!TextUtils.isEmpty(idChorale)) {
            try {
                db.collection("chorale").document(idChorale)
                        .get()
                        .addOnCompleteListener(task -> {
                            Log.d(LOG_TAG, "TDS onComplete avant existe: "+task+" "+task.getResult());
                            if(Objects.requireNonNull(task.getResult()).exists()){

                                Timestamp majDCBB =(Timestamp) task.getResult().get("maj_trombi");
                                majDateCloudDataBase = Objects.requireNonNull(majDCBB).toDate();
                                majCloudDBLong = majDateCloudDataBase.getTime();
                                mMajDbCloudLong.postValue(majCloudDBLong);
                            }else{
                                Log.d(LOG_TAG, "NDS onComplete: taskresult n'existe pas");
                            }

                        })
                        .addOnSuccessListener(documentSnapshot -> Log.d(LOG_TAG, "TDS onSuccess: majCloudDate "+documentSnapshot+" "+" "+documentSnapshot.getString("nom")))
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(LOG_TAG, "TDS onFailure: "+e);
                            }
                        });

            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
            }

        }else{
            Log.d(LOG_TAG, "TDS getMajDateCloudDataBase: pas d'Id chorale");
        }

    }

    public void deletePhotoOnPhone(List<Choriste> oldChoristes) {
        deleted=false;
        mExecutors.storageIO().execute(() -> {
            for (Choriste choriste:oldChoristes) {
                String path = choriste.getUrlLocalPhoto();

                File tempfile = new File(path);
                String name = tempfile.getName();

                Log.d(LOG_TAG, "TDS deletePhotoOnPhone: name "+ name);

                if(tempfile.exists()){
                    Log.d(LOG_TAG, "TDS run: le fichier existe  donc sera supprimé !");
                    if(mContext.deleteFile(name)){
                        Log.d(LOG_TAG, "TDS deletePhotoOnPhone: photo effacée "+choriste.getNom()+" "+choriste.getPrenom());
                    }else{
                        Log.d(LOG_TAG, "TDS deletePhotoOnPhone: erreur d'effcement de background ");
                    }
                }else{
                    Log.d(LOG_TAG, "NDS run: le fichier n'existe pas donc ne peut être supprimé !");
                }
                choriste.setUpdatePhotoPhone(null);
            }
        });
    }

    public void downloadPhoto(List<Choriste> choristes) {

        if (choristes != null&&choristes.size()!=0) {
            Log.d(LOG_TAG, "TDS downloadphotod: uploadImage "+choristes);
            uploadOnPhonePhotos(choristes);
        } else {
            Log.d(LOG_TAG, "NDS downloadBgImage: pas d'images de Background à sauvegarder");
            downloads.postValue("Done");
        }


    }

    private void uploadOnPhonePhotos(List<Choriste> choristes) {
        increment1 =0;
        int photoSize = choristes.size();

        if(choristes.size()==0){
            Log.d(LOG_TAG, "TDS size 0 pour ne pas louper les BG: ");
            downloads.postValue("Done");
        }
        for (Choriste choriste : choristes) {
            String cloudPath = choriste.getUrlCloudPhoto();
            mStorageRef = mStorage.getReferenceFromUrl(cloudPath);
            String filename = mStorageRef.getName();
            File localFileImage = new File(mContext.getFilesDir(), filename);
            String pathImage = localFileImage.getAbsolutePath();
            choriste.setUrlLocalPhoto(pathImage);
            choriste.setUpdatePhotoPhone(new Date(System.currentTimeMillis()));

            Log.d(LOG_TAG, "TDS uploadOnPhoneBgImages: "+" "+choriste.getUrlLocalPhoto() + localFileImage.getParent() + " " + filename + " " + localFileImage.getPath() + " " + localFileImage.getAbsolutePath()+" "+Thread.currentThread().getName());

            mStorageRef.getFile(localFileImage)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Successfully downloaded data to local file
                        //todo modifier le texte pour l'utilisateur
                        increment1++;
                        if(increment1==photoSize){
                            Log.d(LOG_TAG, "TDS onSuccess pour ne pas louper les photos: ");
                            downloads.postValue("Done");
                        }
                        Log.d(LOG_TAG, "TDS onSuccess: storage upload photos "+Thread.currentThread().getName());
                        // Toast.makeText(mContext, "Vos images de fond sont enregistrées sur votre téléphone", Toast.LENGTH_LONG).show();
                        // ...
                    }).addOnFailureListener(exception -> {
                // Handle failed download
                // ...
                Toast.makeText(mContext, "Il y a eu un problème de téléchargement, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
            });
        }
    }



    public void startFetchChoristesService() {

        Log.d(LOG_TAG, "TDS startFetchChoristesService: début ");
        Intent intentToFetch = new Intent(mContext, ChoristeSyncIntentService.class);
        intentToFetch.putExtra("origine","choristes");
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "TDS Service created pour choristes");
    }

    public void fetchChoristes() {
        Log.d(LOG_TAG, "TDS fetchChoristes: début");
        try{
            db.collection("chorale").document(idChorale).collection("choristes")
                    .get()
                    .addOnCompleteListener(task -> {
                       if(task.isSuccessful()) {
                           for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                               String nomChoriste, prenomChoriste,idCloudChoriste,pupitreStr,adresse,fixTel,portTel,email,roleChoeur,roleAdmin,urlCloudPhoto;

                               ArrayList<String> adresses;
                               Timestamp majChoriste;
                               Date maj;

                               idCloudChoriste=document.getId();
                               majChoriste=(Timestamp) document.getData().get("maj");
                               maj = Objects.requireNonNull(majChoriste).toDate();

                               nomChoriste =(String) document.getData().get("nom_choriste");
                               prenomChoriste=(String) document.getData().get("prenom_choriste");
                               pupitreStr =(String) document.getData().get("pupitre");

                               Pupitre pupitreObj = SongsUtilities.converttoPupitre(Objects.requireNonNull(pupitreStr));


                               adresses = (ArrayList<String>) document.getData().get("adresse");
                               adresse = adresses.get(0)+" "+adresses.get(2)+ " "+ adresses.get(1);
                               email = (String) document.getData().get("email");
                               roleAdmin = (String) document.getData().get("role_admin");
                               roleChoeur = (String) document.getData().get("role_choeur");
                               fixTel = (String) document.getData().get("tel_fixe");
                               portTel = (String) document.getData().get("tel_port");
                               urlCloudPhoto = (String) document.getData().get("url_photo");

                               Choriste choriste = new Choriste(idCloudChoriste,idChorale,nomChoriste,prenomChoriste,pupitreObj,adresse,fixTel,portTel,email,roleChoeur,roleAdmin,urlCloudPhoto,maj);
                               choristes.add(choriste);
                           }

                           Log.d(LOG_TAG, "TDS fetchChoristes: avant post ");
                           mChoristes.postValue(choristes);
                       }

                    }).addOnFailureListener(e -> Log.d(LOG_TAG, "TDS onFailure: "));

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public LiveData<List<Choriste>> getChoristes() {
        Log.d(LOG_TAG, "TDS getChoristes: "+mChoristes);
        return mChoristes;

    }

    public boolean isDeleted() {
        return deleted;
    }


    public interface OnChoristeNDSListener {
        void OnProgressLoading(int progress);
        void OnProgressSongs(int nbSong, int nbSongTotal);
    }



    /**
     * Get the singleton for this class
     */
    public static TrombiNetWorkDataSource getInstance(Context context, Context mAContext, AppExecutors executors) {
        Log.d(LOG_TAG, "TDS Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new TrombiNetWorkDataSource(context.getApplicationContext(), mAContext, executors);

                Log.d(LOG_TAG, "NDS Made new network data source");
            }
        }
        return sInstance;
    }


    public LiveData<Long> getMajDBCloudLong() {
        return mMajDbCloudLong;
    }

    public MutableLiveData<String> getDownloads() {
        return downloads;
    }
}
