package dedicace.com.data.networkdatabase;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dedicace.com.AppExecutors;
import dedicace.com.R;
import dedicace.com.WorkerThread;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Saison;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;
import dedicace.com.data.database.Spectacle;
import dedicace.com.ui.DialogDownload;
import dedicace.com.utilities.SongsUtilities;

public class ChoraleNetWorkDataSource {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "coucou";
    private static ChoraleNetWorkDataSource sInstance;
    private final Context mContext;
    private boolean aloneCreate, deleted = false;
    private DialogFragment dialog;

    //Utils
    private AppExecutors mExecutors;
    private WorkerThread workerThread;

    //todo remplacer par un join ?
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String msgStr = (String) msg.obj;
            if(msgStr.equals("OK")){
                Log.d(LOG_TAG, "NDS handleMessage: OK "+songs.size());
            mDownloaderSourceSongs.postValue(sourceSongs);
            }else{
                Log.d(LOG_TAG, "handleMessage: pas OK");
            }
        }
    };

    //Songs
    private final MutableLiveData<List<SourceSong>> mDownloaderSourceSongs;
    private final MutableLiveData<Long> mMajDbCloudLong;
    private final MutableLiveData<String> downloads;
    private final MutableLiveData<List<Saison>> mDownLoadSaisons;
    private List<SourceSong> sourceSongs = new ArrayList<>();
    private List<Saison> saisons = new ArrayList<>();
    private List<Spectacle> spectacles = new ArrayList<>();
    private List<Song> listDownloadMp3;
    private List<Song> oldSongs = new ArrayList<>();
    private List<Song> songs = new ArrayList<>();
    private String titre;
    private String currentPupitreStr;
    private List<Pupitre> pupitreToUpload = new ArrayList<>();
    private List<SourceSong> bgDownload = new ArrayList<>();
    private List<SourceSong> newBgDownload = new ArrayList<>();
    private List<Song> mp3Download = new ArrayList<>();
    private List<Song> newMp3Download = new ArrayList<>();


    //DB
    private FirebaseFirestore db;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private String current_user_id;
    private Pupitre pupitreUser;
    private FirebaseAuth mAuth;


    private Date majDateCloudDataBase;
    private String idChorale;
    private int increment,increment1;

    //Local Storage
    private File localFileMp3;
    private File localFileImage;


    private Thread threadMaj;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Long majCloudDBLong;

    private OnNDSListener mlistener;

    //todo penser à changer les règles de sécurité de la base de données.

    private ChoraleNetWorkDataSource(Context context, Context mAContext, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloaderSourceSongs = new MutableLiveData<>();
        mMajDbCloudLong = new MutableLiveData<>();
        downloads = new MutableLiveData<>();
        mDownLoadSaisons = new MutableLiveData<>();
        Log.d(LOG_TAG, "NetworkDataSource: constructor " + mDownloaderSourceSongs);
        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        Log.d(LOG_TAG, "NDS ChoraleNetWorkDataSource: constructor ref de storage et db");
        mAuth=FirebaseAuth.getInstance();
        current_user_id= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Log.d(LOG_TAG, "NDS ChoraleNetWorkDataSource: constructor "+ current_user_id);
        workerThread = new WorkerThread();
        sharedPreferences =PreferenceManager.getDefaultSharedPreferences(mContext);
        dialog = new DialogDownload();

        //todo voir si cela fonctionne dans tous les cas et supprimer si pas utile dans le progress
        if(mAContext!=context){
        mlistener = (OnNDSListener) mAContext;
        }
    }

    public interface OnNDSListener {
        void OnProgressLoading(int progress);
    }

    /**
     * Get the singleton for this class
     */
    public static ChoraleNetWorkDataSource getInstance(Context context, Context mAContext, AppExecutors executors) {
        Log.d(LOG_TAG, "NDS Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleNetWorkDataSource(context.getApplicationContext(), mAContext, executors);

                Log.d(LOG_TAG, "NDS Made new network data source");
            }
        }
        return sInstance;
    }


    public LiveData<List<SourceSong>> getSourceSongs() {
        Log.d(LOG_TAG, "NDS network getSourceSongs: "+mDownloaderSourceSongs);
        return mDownloaderSourceSongs;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public LiveData<List<Saison>> getSaisonsCloud() {
        Log.d(LOG_TAG, "NDS network getSaisons: "+mDownLoadSaisons);
        return mDownLoadSaisons;
    }

    public List<Spectacle> getSpectacles() {
        return spectacles;
    }


    public void getMajDateCloudDataBase() {

        Log.d(LOG_TAG, "NDS startFetchSongsService: début pour maj");
        Intent intentToFetchMaj = new Intent(mContext, ChoraleSyncIntentService.class);
        intentToFetchMaj.putExtra("origine","maj");
        mContext.startService(intentToFetchMaj);
        Log.d(LOG_TAG, "NDS Service created pour maj");

    }


    void fetchMajClouDb() {

        idChorale = sharedPreferences.getString("idchorale","");
        //idChorale="jFHncTuYleIHhZtL2PmT";
        Log.d(LOG_TAG, "NDS getMajDateCloudDataBase: "+idChorale);

        if(!idChorale.equals("")) {
            try {
                db.collection("chorale").document(idChorale)
                        .get()
                        .addOnCompleteListener(task -> {
                            Log.d(LOG_TAG, "NDS onComplete avant existe: "+task+" "+task.getResult());
                            if(Objects.requireNonNull(task.getResult()).exists()){
                            threadMaj =Thread.currentThread();

                            Log.d(LOG_TAG, "NDS onComplete threadMaj: "+threadMaj+" "+task+" "+task.getResult());

                            Log.d(LOG_TAG, "NDS onComplete: "+ Objects.requireNonNull(task.getResult().get("maj")).getClass().toString());

                            Timestamp majDCBB =(Timestamp) task.getResult().get("maj");
                            Log.d(LOG_TAG, "NDS onComplete: "+majDCBB);

                            majDateCloudDataBase = Objects.requireNonNull(majDCBB).toDate();
                            Log.d(LOG_TAG, "NDS onComplete: majDBCloud " + majDateCloudDataBase);

                            majCloudDBLong = majDateCloudDataBase.getTime();
                            Log.d(LOG_TAG, "NDS onComplete: majDBCloud Long " + majCloudDBLong);

                            mMajDbCloudLong.postValue(majCloudDBLong);
                            }else{
                                Log.d(LOG_TAG, "NDS onComplete: taskresult n'existe pas");
                            }

                        })
                        .addOnSuccessListener(documentSnapshot -> Log.d(LOG_TAG, "NDS onSuccess: majCloudDate "+documentSnapshot+" "+" "+documentSnapshot.getString("nom")))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(LOG_TAG, "NDS onFailure: "+e);
                    }
                });

            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
            }

        }else{
            Log.d(LOG_TAG, "NDS getMajDateCloudDataBase: pas d'Id chorale");
        }
    }

    public void startDownloadService(List<SourceSong> bgSourcesToDownLoad, List<SourceSong> newSourceSongsList, List<Song> mp3SongsToDownload, List<Song> newSongsList){
        Log.d(LOG_TAG, "NDS startFetchSongsService: début pour Download");
        bgDownload=bgSourcesToDownLoad;
       newBgDownload=newSourceSongsList;
        mp3Download=mp3SongsToDownload;
        newMp3Download=newSongsList;

        Intent intentToDowload = new Intent(mContext, ChoraleSyncIntentService.class);
        intentToDowload.putExtra("origine","download");
        mContext.startService(intentToDowload);
        Log.d(LOG_TAG, "NDS Service created pour Download");
    }


    public void startFetchSongsService() {
        Log.d(LOG_TAG, "NDS startFetchSongsService: début pour SS");
        Intent intentToFetch = new Intent(mContext, ChoraleSyncIntentService.class);
        intentToFetch.putExtra("origine","sources");
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "NDS Service created pour SS");
    }


    //todo voir le cas où il n'y a pas de réseau pour charger les données en attente.
    void fetchSongs() {
            try {
                db.collection("sourceSongs")
                        .get()
                        .addOnCompleteListener(task -> {
                            Log.d(LOG_TAG, "NDS onComplete: sourceSongs " + Thread.currentThread().getName());
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    Log.d(LOG_TAG, "NDS deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));
                                    //todo voir comment écrire une seule ligne avec ToObject

                                    String titre, groupe, baseUrlOriginalSong, urlCloudBackground,idCloud;
                                    Date maj;
                                    int duration;
                                    Timestamp majss;

                                    idCloud= document.getId();
                                    titre = (String) document.getData().get("titre");
                                    groupe = (String) document.getData().get("groupe");
                                    duration = ((Long) Objects.requireNonNull(document.getData().get("duration"))).intValue();
                                    baseUrlOriginalSong = (String) document.getData().get("original_song");

                                    majss = (Timestamp) document.getData().get("maj");

                                    maj = Objects.requireNonNull(majss).toDate();
                                    urlCloudBackground = (String) document.getData().get("background");

                                    Log.d(LOG_TAG, "NDS-exec onComplete:A SourceSongs " + titre + " " + groupe + " " + duration + " " + baseUrlOriginalSong + " " + maj + " " + urlCloudBackground);
                                    SourceSong sourceSong = new SourceSong(idCloud,titre,groupe,duration,urlCloudBackground,baseUrlOriginalSong,maj);

                                    sourceSongs.add(sourceSong);
                                }

                                Log.d(LOG_TAG, "NDS fetchSourceSongs: après fetch");
                            } else {
                                Log.w(LOG_TAG, "NDS Error getting documents.", task.getException());
                            }

                            try {
                                db.collection("songs")
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            Log.d(LOG_TAG, "NDS onComplete: Songs " + Thread.currentThread().getName());
                                            if (task1.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task1.getResult())) {
                                                    Log.d(LOG_TAG, "NDS " + document.getId() + " => " + document.getData().get("pupitre"));

                                                    final String pupitre, recordSource, urlMp3, idCloud;
                                                    final Date maj;
                                                    final Timestamp majs;

                                                    idCloud = document.getId();

                                                    pupitre = (String) document.getData().get("pupitre");

                                                    final Pupitre pupitreObj = SongsUtilities.converttoPupitre(Objects.requireNonNull(pupitre));

                                                    recordSource = (String) document.getData().get("recordSource");
                                                    final RecordSource sourceObj = SongsUtilities.convertToRecordSource(Objects.requireNonNull(recordSource));

                                                    urlMp3 = (String) document.getData().get("songPath");

                                                    majs= (Timestamp) document.getData().get("maj");
                                                    maj = Objects.requireNonNull(majs).toDate() ;

                                                    //todo comment faire pour faire une référence à sourceSong
                                                    titre = (String) document.getData().get("titre_song");
                                                    Log.d(LOG_TAG, "NDS : onComplete:B Songs " + titre + " " + sourceObj + " " + pupitreObj + " " + maj);
                                                    Song song = new Song(idCloud,titre,sourceObj,pupitreObj,urlMp3,maj);
                                                    songs.add(song);
                                                }
                                                Log.d(LOG_TAG, "NDS onComplete: avant post "+songs.size()+"  "+ songs);
                                                //todo à vérifier surement inutile maintenant
                                                Message message = Message.obtain();
                                                message.obj = "OK";
                                                handler.sendMessage(message);
                                            } else {
                                                Log.w(LOG_TAG, "NDS Error getting documents.", task1.getException());
                                            }
                                        });
                            } catch (Exception e) {
                                // Server probably invalid
                                e.printStackTrace();
                            }
                        });

            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
            }

    }

    //todo faire des download qu'avec wifi ou suivant préférences
    public void downloadBgImage(List<SourceSong> sourceSongs, boolean test) {
        aloneCreate = test;
        if (sourceSongs != null&&sourceSongs.size()!=0) {
            Log.d(LOG_TAG, "NDS downloadBgImage: uploadImage "+sourceSongs);
            uploadOnPhoneBgImages(sourceSongs);
        } else {
            Log.d(LOG_TAG, "NDS downloadBgImage: pas d'images de Background à sauvegarder");
            downloads.postValue("Done");
        }
    }


    private void uploadOnPhoneBgImages(List<SourceSong> sources) {
        increment1 =0;
        int bgSize = sources.size();

        if(sources.size()==0&&aloneCreate){
                Log.d(LOG_TAG, "NDS size0 pour ne pas louper les BG: ");
                downloads.postValue("Done");
        }
        for (SourceSong source : sources) {
            String cloudPath = source.getUrlCloudBackground();
            mStorageRef = mStorage.getReferenceFromUrl(cloudPath);
            String filename = mStorageRef.getName();
            localFileImage = new File(mContext.getFilesDir(), filename);
            String pathImage = localFileImage.getAbsolutePath();
            source.setBackground(pathImage);
            source.setUpdateBgPhone(new Date(System.currentTimeMillis()));

            Log.d(LOG_TAG, "NDS uploadOnPhoneBgImages: "+" "+source.getBackground() + localFileImage.getParent() + " " + filename + " " + localFileImage.getPath() + " " + localFileImage.getAbsolutePath()+" "+Thread.currentThread().getName());

            mStorageRef.getFile(localFileImage)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Successfully downloaded data to local file
                        //todo modifier le texte pour l'utilisateur
                        increment1++;
                        if(increment1==bgSize&&aloneCreate){
                            Log.d(LOG_TAG, "NDS onSuccess pour ne pas louper les BG: ");
                            downloads.postValue("Done");
                        }
                        Log.d(LOG_TAG, "NDS onSuccess: storage upload bg "+Thread.currentThread().getName());
                       // Toast.makeText(mContext, "Vos images de fond sont enregistrées sur votre téléphone", Toast.LENGTH_LONG).show();
                        // ...
                    }).addOnFailureListener(exception -> {
                        // Handle failed download
                        // ...
                        Toast.makeText(mContext, "Il y a eu un problème de téléchargement, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
                    });
        }
    }

    public void downloadMp3(List<Song> songs) {

        listDownloadMp3 =getListDownloadMp3(songs);
        Log.d(LOG_TAG, "NDS downloadMp3: coucou "+listDownloadMp3);

        if (listDownloadMp3 != null&&listDownloadMp3.size()!=0) {
            Log.d(LOG_TAG, "NDS : uploadMP3 "+ listDownloadMp3.size()+" "+Thread.currentThread().getName());
            uploadOnPhoneMp3(listDownloadMp3);
        }else {
            Log.d(LOG_TAG, "NDS downloadMP3: pas d'images de fichiers audio à sauvegarder");
            downloads.postValue("Done");
        }
    }


    private List<Song> getListDownloadMp3(List<Song> songs) {

        final List<Song> tempList = new ArrayList<>();

        Set<String> pupitreDownload;
        pupitreDownload=sharedPreferences.getStringSet(mContext.getString(R.string.pref_pupitre_key),null);

        if(pupitreDownload!=null) {
            Log.d(LOG_TAG, "NDS getListDownloadMp3: ppupitreToDownload "+pupitreDownload);
            for (String pupitreStr : pupitreDownload) {
                Pupitre tempPupitre = SongsUtilities.converttoPupitre(pupitreStr);
                pupitreToUpload.add(tempPupitre);
            }
        }else {
            pupitreToUpload.add(pupitreUser);
        }

        Log.d(LOG_TAG, "NDS getListDownloadMp3 user: "+current_user_id);
        Log.d(LOG_TAG, "NDS getListDownloadMp3 songs: "+songs.size());
        Log.d(LOG_TAG, "NDS getListDownloadMp3 oldsongs: "+oldSongs.size());
        Log.d(LOG_TAG, "NDS getListDownloadMp3 currentpupitre: "+currentPupitreStr);
        Log.d(LOG_TAG, "NDS getListDownloadMp3: pupitreUser "+pupitreUser);
        Log.d(LOG_TAG, "NDS getListDownloadMp3: pupitre To Load "+pupitreToUpload);

        for (Song newSong : songs) {
            Log.d(LOG_TAG, "NDS onComplete: newsongs "+ newSong.getSourceSongTitre()+" " + newSong.getRecordSource()+" "+newSong.getPupitre() );
            //todo tri sur le pupitre plus tard il faudra être capable de s'adapter aux préférences.faire une liste des pupitres à enregistrer
            //todo préparer une page de setUp pour mettre les infos complémentaires sur le user comme son pupitre

            for(Pupitre pupitre: pupitreToUpload) {
                if (newSong.getPupitre() == pupitre) {
                    Log.d(LOG_TAG, "NDS getListDownloadMp3: test pupitre passé " + oldSongs.size() + " " + oldSongs);

                    tempList.add(newSong);
                }
            }
        }
        Log.d(LOG_TAG, "NDS getListDownloadMp3: "+tempList.size());

        return tempList;
    }


    //todo faire dans le service data cloud
    private void uploadOnPhoneMp3(List<Song> listMp3) {

        Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: fct upload "+ listMp3.size()+" "+Thread.currentThread().getName());
        increment =0;
        int mp3Size = listMp3.size();

        if(listMp3.size()==0) {
            Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: size 0 ");
            downloads.postValue("Done");
        }

        for (Song song : listMp3) {
            String cloudPath = song.getUrlCloudMp3();
            mStorageRef = mStorage.getReferenceFromUrl(cloudPath);

            String filename = mStorageRef.getName();

            //todo essayer de mettre un dossier sons.mp3
            localFileMp3 = new File(mContext.getFilesDir(), filename);
            String pathLocalMp3 = localFileMp3.getAbsolutePath();
            song.setSongPath(pathLocalMp3);
            song.setUpdatePhoneMp3(new Date(System.currentTimeMillis()));
            Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: " + localFileMp3.getParent() + " " + filename + " " + localFileMp3.getPath() + " " + localFileMp3.getAbsolutePath()+" "+ mContext.getFilesDir()+" "+Thread.currentThread().getName());

            //todo voir comment utiliser download manager
            //todo voir comment intégrer l'avancement des données dans un retour utilisateur
            mStorageRef.getFile(localFileMp3)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Successfully downloaded data to local file
                        increment++;
                        Log.d(LOG_TAG, "NDS onSuccess: "+Thread.currentThread().getName()+" "+ filename);

                        if(increment ==mp3Size) {
                            Log.d(LOG_TAG, "NDS onSuccess :lancement de postValue Done "+mp3Size);
                            downloads.postValue("Done");
                        }
                        // ...
                    }).addOnFailureListener(exception -> {
                        // Handle failed download
                        // ...
                        Toast.makeText(mContext, "Il y a eu un problème de téléchargement, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();

                    }).addOnProgressListener(taskSnapshot -> {
                        if(taskSnapshot.getTotalByteCount()!=0) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred()) / (int) taskSnapshot.getTotalByteCount();
                            Log.d(LOG_TAG, "NDS onProgress: " + filename + " " + progress + "%");
                            mlistener.OnProgressLoading(progress);
                        }
                    });
        }

        Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: pour voir si cela passe avant la fin des chargements");
    }

    public String getCurrentPupitreStr() {

        currentPupitreStr=sharedPreferences.getString("pupitre","TUTTI");
        if (currentPupitreStr != null) {
            pupitreUser=SongsUtilities.converttoPupitre(currentPupitreStr);
        }

        return currentPupitreStr;
    }

    public Context getContext() {

        return mContext;
    }


    public LiveData<Long> getMajDBCloudLong() {
        return mMajDbCloudLong;
    }

    //todo pourquoi dans le NDS ?
    public void deleteSongsMp3OnPhone(List<Song> deletedSongsList) {
        deleted=true;
        mExecutors.storageIO().execute(() -> {
            for (Song song:deletedSongsList) {
                String path = song.getSongPath();
                File tempFile = new File(path);
                String name = tempFile.getName();
                Log.d(LOG_TAG, "NDS deleteMp3OnPhone: name "+ name);

                if (tempFile.exists()) {
                    Log.d(LOG_TAG, "NDS run: le fichier existe  donc sera supprimé !");
                    if(mContext.deleteFile(name)){
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: mp3 effacé "+song.getSourceSongTitre());
                    }else{
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: erreur d'effacement de Mp3 ");
                    }

                }else{
                    Log.d(LOG_TAG, "NDS run: le fichier n'existe pas donc ne peut être supprimé !");
                }
                song.setUpdatePhoneMp3(null);
            }
        });
    }

    public void deleteBgOnPhone(List<SourceSong> oldSources) {
        //todo voir si cela fonctionne avec cela
        deleted=false;
        mExecutors.storageIO().execute(() -> {
            for (SourceSong source:oldSources) {
                String path = source.getBackground();

                File tempfile = new File(path);
                String name = tempfile.getName();

                Log.d(LOG_TAG, "NDS deleteBgOnPhone: name "+ name);

                if(tempfile.exists()){
                    Log.d(LOG_TAG, "NDS run: le fichier existe  donc sera supprimé !");
                    if(mContext.deleteFile(name)){
                        Log.d(LOG_TAG, "NDS deleteBgOnPhone: background effacé "+source.getTitre());
                    }else{
                        Log.d(LOG_TAG, "NDS deleteBgOnPhone: erreur d'effcement de background ");
                    }
                }else{
                    Log.d(LOG_TAG, "NDS run: e fichier n'existe pas donc ne peut être supprimé !");
                }
                source.setUpdateBgPhone(null);
            }
        });


    }

    public MutableLiveData<String> getDownloads() {


        return downloads;
    }

    public boolean isDeleted() {
        return deleted;
    }

    //download single song
    public void downloadSingleMp3(Song song) {

        String cloudPath = song.getUrlCloudMp3();
        mStorageRef = mStorage.getReferenceFromUrl(cloudPath);

        String filename = mStorageRef.getName();

        //todo essayer de mettre un dossier sons.mp3
        localFileMp3 = new File(mContext.getFilesDir(), filename);
        String pathLocalMp3 = localFileMp3.getAbsolutePath();
        song.setSongPath(pathLocalMp3);
        song.setUpdatePhoneMp3(new Date(System.currentTimeMillis()));
        Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: " + localFileMp3.getParent() + " " + filename + " " + localFileMp3.getPath() + " " + localFileMp3.getAbsolutePath()+" "+ mContext.getFilesDir()+" "+Thread.currentThread().getName());

        //todo voir comment utiliser download manager
        //todo voir comment intégrer l'avancement des données dans un retour utilisateur
        mStorageRef.getFile(localFileMp3)
                .addOnSuccessListener(taskSnapshot -> {
                    // Successfully downloaded data to local file

                    Log.d(LOG_TAG, "NDS onSuccess single: "+Thread.currentThread().getName()+" "+ filename);

                        downloads.postValue("SingleDownload");
                }).addOnFailureListener(exception -> {
                    // Handle failed download
                    // ...
                    Toast.makeText(mContext, "Il y a eu un problème de téléchargement, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();

                }).addOnProgressListener(taskSnapshot -> {
                    int progress = (int) (100.0 * taskSnapshot.getBytesTransferred()) / (int) taskSnapshot.getTotalByteCount();
                    Log.d(LOG_TAG, "NDS onProgress: "+ filename +" "+progress+"%");
                });
    }

    //todo factorisation des parties communes
    public void downloadMp3PupitresSongs(List<Song> songsToDownload) {
        increment =0;
        int mp3Size = songsToDownload.size();
        if(songsToDownload.size()==0){
            Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: size 0 ");
            downloads.postValue("MultipleDownloads");
        }

        for (Song song : songsToDownload) {

            String cloudPath = song.getUrlCloudMp3();
            mStorageRef = mStorage.getReferenceFromUrl(cloudPath);

            String filename = mStorageRef.getName();

            //todo essayer de mettre un dossier sons.mp3
            localFileMp3 = new File(mContext.getFilesDir(), filename);
            String pathLocalMp3 = localFileMp3.getAbsolutePath();
            song.setSongPath(pathLocalMp3);
            song.setUpdatePhoneMp3(new Date(System.currentTimeMillis()));
            Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: " + localFileMp3.getParent() + " " + filename + " " + localFileMp3.getPath() + " " + localFileMp3.getAbsolutePath()+" "+ mContext.getFilesDir()+" "+Thread.currentThread().getName());

            //todo voir comment utiliser download manager
            //todo voir comment intégrer l'avancement des données dans un retour utilisateur
            mStorageRef.getFile(localFileMp3)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Successfully downloaded data to local file
                        increment++;
                        Log.d(LOG_TAG, "NDS onSuccess: "+Thread.currentThread().getName()+" "+ filename);

                        if(increment ==mp3Size) {
                            Log.d(LOG_TAG, "NDS onSuccess :lancement de postValue Done "+mp3Size);
                            downloads.postValue("MultipleDownloads");
                        }
                        // ...
                    }).addOnFailureListener(exception -> {
                        // Handle failed download
                        // ...
                        Toast.makeText(mContext, "Il y a eu un problème de téléchargement, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();

                    }).addOnProgressListener(taskSnapshot -> {
                        if(taskSnapshot.getTotalByteCount()!=0) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred()) / (int) taskSnapshot.getTotalByteCount();
                            Log.d(LOG_TAG, "NDS onProgress: multiple" + filename + " " + progress + "%");

                        }
                    });
        }
    }

    public void deleteSingleSong(Song song) {
        deleted=true;
        mExecutors.storageIO().execute(() -> {
                String path = song.getSongPath();
                File tempFile = new File(path);
                String name = tempFile.getName();
                Log.d(LOG_TAG, "NDS deleteMp3OnPhone single: name "+ name);

                if (tempFile.exists()) {
                    Log.d(LOG_TAG, "NDS run: le fichier existe  donc sera supprimé single !");
                    if(mContext.deleteFile(name)){
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: mp3 effacé single"+song.getSourceSongTitre());
                    }else{
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: erreur d'effacement de Mp3 ");
                    }

                }else{
                    Log.d(LOG_TAG, "NDS run: le fichier n'existe pas donc ne peut être supprimé !");
                }
                song.setUpdatePhoneMp3(null);
                song.setSongPath(null);
                downloads.postValue("deleteSingle");

        });

    }

    public void deleteMultipleSong(List<Song> songsToDelete) {
        deleted=true;
        mExecutors.storageIO().execute(() -> {
            int incr =0;
            int size = songsToDelete.size();
            for (Song song:songsToDelete) {
                String path = song.getSongPath();
                File tempFile = new File(path);
                String name = tempFile.getName();
                Log.d(LOG_TAG, "NDS deleteMp3OnPhone: multiple name "+ name);

                if (tempFile.exists()) {
                    Log.d(LOG_TAG, "NDS run: le fichier existe  donc sera supprimé !");
                    if(mContext.deleteFile(name)){
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: mp3 effacé multiple"+song.getSourceSongTitre());
                        incr++;

                        if(incr==size){
                            downloads.postValue("deleteMultiple");
                        }

                    }else{
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: erreur d'effacement de Mp3 ");
                    }

                }else{
                    Log.d(LOG_TAG, "NDS run: le fichier n'existe pas donc ne peut être supprimé !");
                }
                song.setUpdatePhoneMp3(null);
                song.setSongPath(null);
            }
        });


    }

    //todo mettre des try catch
    public void getData(String current_user_id) {
        Log.d(LOG_TAG, "NDS getData: "+current_user_id);
        db.collection("users").document(current_user_id)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(Objects.requireNonNull(task.getResult()).exists()){
                            String role, pupitre,idChorale,email,nom,prenom;
                            role =(String) task.getResult().get("role");
                            pupitre =(String) task.getResult().get("pupitre");
                            idChorale=(String) task.getResult().get("id_chorale");
                            this.idChorale=idChorale;
                            //todo voir à quoi pourront servir ces éléments (peut être dans le préférence compte)
                            email = (String) task.getResult().get("email");
                            nom=(String) task.getResult().get("nom");
                            prenom=(String) task.getResult().get("prenom");
                            editor = sharedPreferences.edit();
                            Log.d(LOG_TAG, "NDS setUpSharedPreferences: installation"+role+" "+pupitre+" "+idChorale+" "+email+" "+nom+" "+prenom);
                            editor.putBoolean("installation",false);
                            editor.putString("role",role);
                            editor.putString("idchorale",idChorale);
                            editor.putBoolean(mContext.getString(R.string.maj_auto),true);
                            editor.putString("pupitre",pupitre);
                            Set<String> pupitreToDownload = new HashSet<>();
                            pupitreToDownload.add(pupitre);
                            editor.putStringSet(mContext.getString(R.string.pref_pupitre_key),pupitreToDownload);
                            editor.putBoolean("initializeData",true);
                            Log.d(LOG_TAG, "NDS setUpSharedPreferences: fin");
                            editor.apply();
                        }

                        db.collection("chorale").document(idChorale).collection("saisons")
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task1.getResult())) {
                                            Log.d(LOG_TAG, "NDS Oncomplete saisons" + document.getId() + " => " + document.getData().get("maj"));

                                            String saisonName,idCloud;
                                            boolean currentSaison;
                                            Date maj;
                                            Timestamp majss;
                                            ArrayList<String> idSpectacles;

                                            idCloud= document.getId();
                                            saisonName = (String) document.getData().get("nom");
                                            currentSaison=(boolean) document.getData().get("current_saison");

                                            majss = (Timestamp) document.getData().get("maj");
                                            maj = Objects.requireNonNull(majss).toDate();

                                            Log.d(LOG_TAG, "NDS onComplete:A saisons " + saisonName + " " + maj + " "+idCloud );

                                            idSpectacles =(ArrayList<String>) document.getData().get("spectacles");

                                            Log.d(LOG_TAG, "NDS onComplete:B saisons " + saisonName + " " + maj + " "+idCloud+ " "+idSpectacles+" "+ currentSaison );

                                            Saison saison = new Saison(idCloud,saisonName,idSpectacles,maj, currentSaison);
                                            saisons.add(saison);
                                        }

                                        Log.d(LOG_TAG, "NDS getData: saisons " + saisons);

                                        db.collection("chorale").document(idChorale).collection("spectacles")
                                                        .get()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {
                                                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task2.getResult())) {

                                                                    String spectacleName,idSpectacle;
                                                                    Date maj;
                                                                    Timestamp majss;
                                                                    ArrayList<String> idSourceSongs;
                                                                    ArrayList<String> spectacleLieux;
                                                                    ArrayList<Timestamp> spectaclesTimeStamps;
                                                                    ArrayList<Date> spectacleDates= new ArrayList<>();

                                                                    idSpectacle=document.getId();
                                                                    spectacleName=(String) document.getData().get("nom");
                                                                    majss=(Timestamp) document.getData().get("maj");
                                                                    maj = Objects.requireNonNull(majss).toDate();

                                                                    idSourceSongs=(ArrayList<String>) document.getData().get("id_titres");

                                                                    spectacleLieux=(ArrayList<String>) document.getData().get("concerts_lieux");

                                                                    //todo voir quand il n'y a pas de concert car array Timstamp est vide et ne marche pas alors
                                                                    spectaclesTimeStamps = (ArrayList<Timestamp>) document.getData().get("concerts_dates");

                                                                    for(Timestamp timestamp : spectaclesTimeStamps){
                                                                        Date date = Objects.requireNonNull(timestamp).toDate();
                                                                        spectacleDates.add(date);
                                                                    }

                                                                    Log.d(LOG_TAG, "NDS getData: spectacles "+ idSpectacle+ " "+ spectacleName+ " "+ idSourceSongs+ " "+ spectacleLieux+ " "+ spectacleDates+ " "+ maj );

                                                                    Spectacle spectacle = new Spectacle(idSpectacle,spectacleName,idSourceSongs,spectacleLieux,spectacleDates,maj);
                                                                    spectacles.add(spectacle);

                                                                }
                                                                Log.d(LOG_TAG, "NDS getData: spectacles "+ spectacles);

                                                                mDownLoadSaisons.postValue(saisons);

                                                            }else{
                                                                Log.d(LOG_TAG, "NDS onComplete: help pb sur documents spectacles");
                                                            }


                                                        }).addOnFailureListener(e -> {
                                                        });

                                    }else{
                                        Log.d(LOG_TAG, "NDS onComplete: help pb sur documents saisons");
                                    }

                                })
                                .addOnFailureListener(e -> {

                                });

                    }else{
                        Log.d(LOG_TAG, "NDS onComplete: help pb sur documents users");
                    }
                })
                .addOnFailureListener(e -> {

                });
    }
}


