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
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import dedicace.com.AppExecutors;
import dedicace.com.R;
import dedicace.com.WorkerThread;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;
import dedicace.com.utilities.SongsUtilities;

public class ChoraleNetWorkDataSource {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "coucou";
    private static ChoraleNetWorkDataSource sInstance;
    private final Context mContext;

    //Utils
    private AppExecutors mExecutors;
    public static AppDataBase choeurDataBase;
    private WorkerThread workerThread;

    //todo remplacer par un join ?
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String msgStr = (String) msg.obj;
            if(msgStr.equals("OK")){
                Log.d(LOG_TAG, "handleMessage: OK");
            //downloadBgImage();
            //downloadMp3();
            mDownloaderSourceSongs.postValue(sourceSongs);
            }else{
                Log.d(LOG_TAG, "handleMessage: pas OK");
            }
        }
    };

    //Songs
    private final MutableLiveData<List<SourceSong>> mDownloaderSourceSongs;
    private final MutableLiveData<Long> mMajDbCloudLong;
    private List<SourceSong> sourceSongs = new ArrayList<>();
    private List<SourceSong> oldSourcesSongs = new ArrayList<>();
    private List<SourceSong> listDownLoadImages;
    private List<Song> listDownloadMp3;
    private List<Song> oldSongs = new ArrayList<>();
    private List<Song> songs = new ArrayList<>();
    private String titre;
    private String currentPupitreStr;
    private List<Pupitre> pupitreToUpload = new ArrayList<>();

    //DB
    private FirebaseFirestore db;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private String current_user_id;
    private Pupitre pupitreUser;
    private FirebaseAuth mAuth;

    private String mCurrentAuthRole;
    private Date majDateCloudDataBase;
    private String idChorale;
    private List<String> idChorales= new ArrayList<>();

    //Local Storage
    private File localFileMp3;
    private File localFileImage;

    private final static String BASEURI = "storage/emulated/0/Android/data/dedicace.com/files/";
    private Thread threadMaj;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Date majCloudDB;
    private Long majCloudDBLong;
    private Long majLocalDBLong;

    //todo penser à changer les règles de sécurité de la base de données.

    private ChoraleNetWorkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloaderSourceSongs = new MutableLiveData<>();
        mMajDbCloudLong = new MutableLiveData<>();
        Log.d(LOG_TAG, "NetworkDataSource: constructor " + mDownloaderSourceSongs);
        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        Log.d(LOG_TAG, "NDS ChoraleNetWorkDataSource: constructor ref de storage et db");
        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();
        Log.d(LOG_TAG, "NDS ChoraleNetWorkDataSource: constructor "+ current_user_id);
        workerThread = new WorkerThread();
        sharedPreferences =PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * Get the singleton for this class
     */
    public static ChoraleNetWorkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "NDS Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleNetWorkDataSource(context.getApplicationContext(), executors);

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


    //todo voir s'il faut le gérer avec un service
    public void getMajDateCloudDataBase() {

        //todo revoir pourquoi cela ne marche pas !
        //idChorale=sharedPreferences.getString("idChorale"," ");
        idChorale="jFHncTuYleIHhZtL2PmT";

        if(!idChorale.equals(" ")) {

            try {
                db.collection("chorale").document(idChorale).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        threadMaj =Thread.currentThread();
                        Log.d(LOG_TAG, "NDS onComplete thrzadMaj: "+threadMaj);

                        Log.d(LOG_TAG, "onComplete: "+task.getResult().get("maj").getClass().toString());

                        Timestamp majDCBB =(Timestamp) task.getResult().get("maj");
                        Log.d(LOG_TAG, "onComplete: "+majDCBB);

                        majDateCloudDataBase = majDCBB.toDate();
                       // majDateCloudDataBase = (Date) task.getResult().get("maj");
                        Log.d(LOG_TAG, "NDS onComplete: majDBCloud " + majDateCloudDataBase);

                        majCloudDBLong = majDateCloudDataBase.getTime();
                        Log.d(LOG_TAG, "NDS onComplete: majDBCloud Long " + majCloudDBLong);

                        mMajDbCloudLong.postValue(majCloudDBLong);
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

    private void getMajDateLocalDataBase() {
        sharedPreferences =PreferenceManager.getDefaultSharedPreferences(mContext);

        majLocalDBLong =sharedPreferences.getLong("majDB",0);
        Log.d(LOG_TAG, "NDS : datelong : local "+new Date(majLocalDBLong)+" cloud "+majCloudDB+ " comparaison Long-local/Cloud "+ majLocalDBLong+" "+majCloudDBLong);

    }

    public void startFetchSongsService() {
        Log.d(LOG_TAG, "NDS startFetchSongsService: début");
        Intent intentToFetch = new Intent(mContext, ChoraleSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "NDS Service created");
    }


    //todo voir le cas où il n'y a pas de réseau pour charger les données en attente.
    public void fetchSongs() {
            try {
                db.collection("sourceSongs")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                Log.d(LOG_TAG, "NDS onComplete: sourceSongs " + Thread.currentThread().getName());
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(LOG_TAG, "NDS-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("maj"));
                                        //todo voir comment écrire une seule ligne avec ToObject

                                        String titre, groupe, baseUrlOriginalSong, urlCloudBackground;
                                        Date maj;
                                        int duration;
                                        Timestamp majss;

                                        titre = (String) document.getData().get("titre");
                                        groupe = (String) document.getData().get("groupe");
                                        duration = ((Long) document.getData().get("duration")).intValue();
                                        baseUrlOriginalSong = (String) document.getData().get("original_song");

                                        majss = (Timestamp) document.getData().get("maj");

                                        maj = majss.toDate();
                                        urlCloudBackground = (String) document.getData().get("background");

                                        Log.d(LOG_TAG, "NDS-exec onComplete:A SourceSongs " + titre + " " + groupe + " " + duration + " " + baseUrlOriginalSong + " " + maj + " " + urlCloudBackground);
                                        SourceSong sourceSong = new SourceSong(titre, groupe, duration, urlCloudBackground, baseUrlOriginalSong, maj);
                                        sourceSongs.add(sourceSong);
                                    }

                                    Log.d(LOG_TAG, "NDS-exec fetchSourceSongs: après fetch");
                                } else {
                                    Log.w(LOG_TAG, "NDS-exec Error getting documents.", task.getException());
                                }

                                try {
                                    db.collection("songs")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    Log.d(LOG_TAG, "NDS onComplete: Songs " + Thread.currentThread().getName());
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Log.d(LOG_TAG, "NDS-exec " + document.getId() + " => " + document.getData().get("pupitre"));

                                                            final String pupitre, recordSource, urlMp3;
                                                            final Date maj;
                                                            final Timestamp majs;

                                                            pupitre = (String) document.getData().get("pupitre");

                                                            final Pupitre pupitreObj = SongsUtilities.converttoPupitre(pupitre);

                                                            recordSource = (String) document.getData().get("recordSource");
                                                            final RecordSource sourceObj = SongsUtilities.convertToRecordSource(recordSource);

                                                            urlMp3 = (String) document.getData().get("songPath");

                                                            majs= (Timestamp) document.getData().get("maj");
                                                            maj = majs.toDate() ;

                                                            //todo comment faire pour faire une référence à sourceSong
                                                            titre = (String) document.getData().get("titre_song");
                                                            Log.d(LOG_TAG, "NDS-exec : onComplete:B Songs " + titre + " " + sourceObj + " " + pupitreObj + " " + maj);
                                                            Song song = new Song(titre, sourceObj, pupitreObj, maj, urlMp3);
                                                            songs.add(song);
                                                        }
                                                        Log.d(LOG_TAG, "NDS-exec onComplete: avant post " + songs);
                                                        //todo à vérifier surement inutile maintenant
                                                        Message message = Message.obtain();
                                                        message.obj = "OK";
                                                        handler.sendMessage(message);
                                                    } else {
                                                        Log.w(LOG_TAG, "NDS-exec Error getting documents.", task.getException());
                                                    }
                                                }
                                            });
                                } catch (Exception e) {
                                    // Server probably invalid
                                    e.printStackTrace();
                                }
                            }
                        });

            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
            }

    }

    //todo faire des download qu'avec wifi ou suivant préférences
    public void downloadBgImage(List<SourceSong> sourceSongs) {

        if (sourceSongs != null) {
            Log.d(LOG_TAG, "NDS downloadBgImage: uploadImage ");
            uploadOnPhoneBgImages(sourceSongs);
        } else {
            Log.d(LOG_TAG, "NDS downloadBgImage: pas d'images de Background à sauvegarder");
        }
    }

    private List<SourceSong> getListDownloadBgImages() {

        List<SourceSong> tempList = new ArrayList<>();
        int i;

        for (SourceSong newSource : sourceSongs) {
            i = 0;
            Log.d(LOG_TAG, "NDS getListDownloadBgImages: "+ newSource.getUrlCloudBackground());
            //todo gérer les oldsourcessongs
            if(oldSourcesSongs.size()!=0) {
                Log.d(LOG_TAG, "NDS getListDownloadBgImages: il y a des old sources songs");
                for (SourceSong oldSource : oldSourcesSongs) {

                    if (oldSource.getTitre().equals(newSource.getTitre()) && !oldSource.getUrlCloudBackground().equals(newSource.getUrlCloudBackground())) { tempList.add(newSource); }

                    if (!newSource.getTitre().equals(oldSource.getTitre())) { i++; }
                }

                if (i == oldSourcesSongs.size()) {
                    tempList.add(newSource);
                }
            }else{
                tempList.add(newSource);
            }
        }

        Log.d(LOG_TAG, "NDS getListDownloadBgImages: "+ tempList.size());

        for(SourceSong sourceSong:tempList){
            Log.d(LOG_TAG, "NDS getListDownloadBgImages: "+sourceSong.getTitre()+" "+sourceSong.getSourceSongId());
        }

        return tempList;
    }

    private void uploadOnPhoneBgImages(List<SourceSong> sources) {
        for (SourceSong source : sources) {
            String cloudPath = source.getUrlCloudBackground();
            mStorageRef = mStorage.getReferenceFromUrl(cloudPath);
            String filename = mStorageRef.getName();
            localFileImage = new File(mContext.getFilesDir(), filename);
            String pathImage = localFileImage.getAbsolutePath();
            //todo ajouter pathimage à la place de bgSong int
            source.setBackground(pathImage);

            Log.d(LOG_TAG, "NDS uploadOnPhoneBgImages: " + localFileImage.getParent() + " " + filename + " " + localFileImage.getPath() + " " + localFileImage.getAbsolutePath());

            mStorageRef.getFile(localFileImage)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file
                            //todo modifier le texte pour l'utilisateur
                           // Toast.makeText(mContext, "Vos images de fond sont enregistrées sur votre téléphone", Toast.LENGTH_LONG).show();
                            // ...
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                    Toast.makeText(mContext, "Il y a eu un problème de téléchargement, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void downloadMp3(List<Song> songs) {

        listDownloadMp3 =getListDownloadMp3(songs);

        if (listDownloadMp3 != null) {
            Log.d(LOG_TAG, "NDS : uploadMP3 "+ listDownloadMp3.size()+" "+Thread.currentThread().getName());
            uploadOnPhoneMp3(listDownloadMp3);
        }else {
            Log.d(LOG_TAG, "NDS downloadMP3: pas d'images de fichiers audio à sauvegarder");
        }
    }


    private List<Song> getListDownloadMp3(List<Song> songs) {

        final List<Song> tempList = new ArrayList<>();

        //getPupitreToLoad();

        pupitreToUpload.add(pupitreUser);

        Log.d(LOG_TAG, "NDS getListDownloadMp3 user: "+current_user_id);
        Log.d(LOG_TAG, "NDS getListDownloadMp3 songs: "+songs.size());
        Log.d(LOG_TAG, "NDS getListDownloadMp3 oldsongs: "+oldSongs.size());
        Log.d(LOG_TAG, "NDS getListDownloadMp3 currentpupitre: "+currentPupitreStr);
        Log.d(LOG_TAG, "NDS getListDownloadMp3: pupitreUser "+pupitreUser);
        Log.d(LOG_TAG, "NDS getListDownloadMp3: pupitre To Load "+pupitreToUpload);

        for (Song newSong : songs) {
            int i = 0;
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

    private void getPupitreToLoad() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        Log.d(LOG_TAG, "getPupitreToLoad: "+currentPupitreStr);

        Set<String> pupitreToUploadSet = sharedPreferences.getStringSet(mContext.getString(R.string.pref_pupitre_key),Collections.singleton(currentPupitreStr));

        for (String s: pupitreToUploadSet) {
            Pupitre pupToUpload = SongsUtilities.converttoPupitre(s);

            if(pupToUpload!=Pupitre.NA){
                pupitreToUpload.add(pupToUpload);
            }
        }

        Log.d(LOG_TAG, "getPupitreToLoad: "+ pupitreToUpload);


    }

    private void uploadOnPhoneMp3(List<Song> listMp3) {

        Log.d(LOG_TAG, "NDS uploadOnPhoneMp3: fct upload "+ listMp3.size()+" "+Thread.currentThread().getName());

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

            mStorageRef.getFile(localFileMp3)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file

                           // Toast.makeText(mContext, "Vos chants sont enregistrés sur votre téléphone", Toast.LENGTH_LONG).show();

                            Log.d(LOG_TAG, "NDS onSuccess: "+Thread.currentThread().getName()+" "+ filename);

                            // ...
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                    Toast.makeText(mContext, "Il y a eu un problème de téléchargement, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();

                }
            });
        }

    }


    public void setOldSourceSongs(List<SourceSong> oldSourcesSongs) {
        this.oldSourcesSongs = oldSourcesSongs;
    }

    public void setOldSongs(List<Song> oldSongs) {
        this.oldSongs = oldSongs;
    }

    //todo gérer les oldSongs
    public void oldSongs(){
        String url = "https://firebasestorage.googleapis.com/v0/b/dedicace-778c9.appspot.com/o/songs%2Ffichier_mp3%2Fzoom%20photo1.mp3?alt=media&token=b06bc1d0-5954-4146-ac82-567b8ebf9770";
        Song song = new  Song("Des hommes pareils",RecordSource.BANDE_SON,Pupitre.TENOR,null,url);
        oldSongs.add(song);

    }

    public String getCurrentPupitreStr() {

        //todo mettre dans la bdd locale users avec les éléments le concernant dont le pupitre, ce qui évitera cette partie un peu lourde
        db.collection("users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(LOG_TAG, "NDS getCurrentPupitre onComplete: "+Thread.currentThread().getName());
                if(task.isSuccessful()){

                    //todo voir comment mettre les rôles en DB local
                    currentPupitreStr = (String) task.getResult().get("pupitre");
                    pupitreUser=SongsUtilities.converttoPupitre(currentPupitreStr);


                    Log.d(LOG_TAG, "NDS getCurrentPupitre "+currentPupitreStr);



                }else{
                    Log.d(LOG_TAG, "NDS onComplete: erreur de récupération du pupitre");
                }
            }
        });

        return currentPupitreStr;
    }

    public String getmCurrentAuthRole() {

        db.collection("users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(LOG_TAG, "NDS getCurrentPupitre onComplete: "+Thread.currentThread().getName());
                if(task.isSuccessful()){

                    //todo voir comment mettre les rôles en DB local

                    mCurrentAuthRole= (String) task.getResult().get("role");

                    Log.d(LOG_TAG, "NDS getCurrentAuthRole "+mCurrentAuthRole);


                }else{
                    Log.d(LOG_TAG, "NDS onComplete: erreur de récupération du role");
                }
            }
        });

        return mCurrentAuthRole;
    }

    public Context getContext() {

        return mContext;
    }

    public Thread getThreadMaj() {
        return threadMaj;
    }

    public LiveData<Long> getMajDBCloudLong() {
        return mMajDbCloudLong;
    }

    public void deleteSongsMp3OnPhone(List<Song> deletedSongsList) {

        mExecutors.storageIO().execute(new Runnable() {
            @Override
            public void run() {
                for (Song song:deletedSongsList) {
                    String path = song.getSongPath();
                    File tempFile = new File(path);
                    String name = tempFile.getName();
                    Log.d(LOG_TAG, "NDS deleteMp3OnPhone: name "+ name);

                    //todo vérifier si le fichier existe

                    if(mContext.deleteFile(name)){
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: mp3 effacé "+song.getSourceSongTitre());
                    }else{
                        Log.d(LOG_TAG, "NDS deleteSongsMp3OnPhone: erreur d'effacement de Mp3 ");
                    }
                }
            }
        });
    }

    public void deleteBgOnPhone(List<SourceSong> oldSources) {
        mExecutors.storageIO().execute(new Runnable() {
            @Override
            public void run() {
                for (SourceSong source:oldSources) {
                    String path = source.getBackground();

                    File tempfile = new File(path);
                    String name = tempfile.getName();
                    Log.d(LOG_TAG, "NDS deleteBgOnPhone: name "+ name);

                    //todo vérifier si le fichier existe
                    if(mContext.deleteFile(name)){
                        Log.d(LOG_TAG, "NDS deleteBgOnPhone: background effacé "+source.getTitre());
                    }else{
                        Log.d(LOG_TAG, "NDS deleteBgOnPhone: erreur d'effcement de background ");
                    }
                }
            }
        });


    }
}


