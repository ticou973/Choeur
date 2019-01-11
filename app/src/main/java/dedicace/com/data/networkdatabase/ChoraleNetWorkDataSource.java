package dedicace.com.data.networkdatabase;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dedicace.com.AppExecutors;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SourceSong;
import dedicace.com.ui.SongsAdapter;
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

    //Songs
    private final MutableLiveData<List<SourceSong>> mDownloaderSourceSongs;
    private List<SourceSong> sourceSongs = new ArrayList<>();
    private List<SourceSong> oldSourcesSongs = new ArrayList<>();
    private List<SourceSong> listDownLoadImages;
    private List<Song> listDownloadMp3;
    private List<Song> oldSongs = new ArrayList<>();
    private List<Song> songs = new ArrayList<>();
    private Song song3, song4, song6, song5, song2;
    private SourceSong sourceSong1, sourceSong2, sourceSong3, sourceSong4, sourceSong5, sourceSong6, sourceSong7, sourceSong8;
    private Pupitre recordPupitre = Pupitre.NA;
    private String titre;

    //DB
    private FirebaseFirestore db;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private String current_user_id;
    private Pupitre pupitreUser;
    private FirebaseAuth mAuth;

    private final static String BASEURI = "storage/emulated/0/Android/data/dedicace.com/files/";

    private ChoraleNetWorkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        //mDownloadedSongs = new MutableLiveData<>();
        mDownloaderSourceSongs = new MutableLiveData<>();
        Log.d("coucou", "NetworkDataSource: constructor ");
        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        Log.d("coucou", "ChoraleNetWorkDataSource: ref de storage et db");
        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();
        Log.d("coucou", "ChoraleNetWorkDataSource: "+current_user_id);
    }

    /**
     * Get the singleton for this class
     */
    public static ChoraleNetWorkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        Log.d("coucou", "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChoraleNetWorkDataSource(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
                Log.d("coucou", "Made new network data source");
            }
        }
        return sInstance;
    }


    public LiveData<List<SourceSong>> getSourceSongs() {
        Log.d("coucou", "network getSourceSongs: ");
        return mDownloaderSourceSongs;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void startFetchSongsService() {
        Intent intentToFetch = new Intent(mContext, ChoraleSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "Service created");
        Log.d(LOG_TAG, "Service created");

    }

    public void fetchSongs() {
        Log.d("coucou", "network fetch started: ");
        //initData();

        mExecutors.networkIO().execute(new Runnable() {
            @Override
            public void run() {

                try {
                    db.collection("sourceSongs")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("coucou", "deb Oncomplete"+document.getId() + " => " + document.getData().get("maj"));
                                            //todo voir comment écrire une seule ligne avec ToObject

                                            String titre, groupe, baseUrlOriginalSong, urlCloudBackground;
                                            Date maj;
                                            int duration;

                                            titre = (String) document.getData().get("titre");
                                            groupe = (String) document.getData().get("groupe");
                                            duration = ((Long) document.getData().get("duration")).intValue();
                                            baseUrlOriginalSong = (String) document.getData().get("original_song");
                                            maj = (Date) document.getData().get("maj");
                                            urlCloudBackground = (String) document.getData().get("background");

                                            Log.d("coucou", "onComplete:A SourceSongs " + titre + " " + groupe + " " + duration + " " + baseUrlOriginalSong + " " + maj + " " + urlCloudBackground);
                                            SourceSong sourceSong = new SourceSong(titre, groupe, duration, urlCloudBackground, baseUrlOriginalSong, new Date(System.currentTimeMillis()));
                                            sourceSongs.add(sourceSong);
                                        }
                                        downloadBgImage();
                                    } else {
                                        Log.w("coucou", "Error getting documents.", task.getException());
                                    }
                                }
                            });

                    db.collection("songs")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("coucou", document.getId() + " => " + document.getData().get("pupitre"));

                                            final String pupitre, recordSource, urlMp3;
                                            final Date maj;

                                            pupitre = (String) document.getData().get("pupitre");

                                            final Pupitre pupitreObj = SongsUtilities.converttoPupitre(pupitre);

                                            recordSource = (String) document.getData().get("recordSource");
                                            final RecordSource sourceObj = SongsUtilities.convertToRecordSource(recordSource);

                                            urlMp3 = (String) document.getData().get("songPath");

                                            maj = (Date) document.getData().get("maj");

                                            DocumentReference titreRef = (DocumentReference) document.getData().get("titre_song");
                                            titreRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                    if (task.isSuccessful()) {
                                                        titre = (String) task.getResult().get("titre");
                                                        Log.d("coucou", "onComplete:B Songs " + titre + " " + sourceObj + " " + pupitreObj + " " + maj);
                                                        Song song = new Song(titre, sourceObj, pupitreObj, new Date(System.currentTimeMillis()), urlMp3);
                                                        songs.add(song);

                                                        downloadMp3();

                                                        Log.d(SongsAdapter.TAG, "fetchSongs: " + sourceSongs.get(0).getTitre());
                                                        mDownloaderSourceSongs.postValue(sourceSongs);
                                                        Log.d("coucou", "fetchSongs: après post");

                                                    } else {
                                                        Log.w("coucou", "Error getting documents.", task.getException());
                                                    }
                                                }
                                            });

                                        }

                                    } else {
                                        Log.w("coucou", "Error getting documents.", task.getException());
                                    }
                                }
                            });



                } catch (Exception e) {
                    // Server probably invalid
                    e.printStackTrace();
                }
            }
        });
    }

    private void downloadBgImage() {

        listDownLoadImages = getListDownloadBgImages();

        if (listDownLoadImages != null) {
            //uploadOnPhoneBgImages(listDownLoadImages);
        } else {
            Log.d("coucou", "downloadBgImage: pas d'images de Background à sauvegarder");
        }
    }

    private List<SourceSong> getListDownloadBgImages() {
        List<SourceSong> tempList = new ArrayList<>();
        int i;

        for (SourceSong newSource : sourceSongs) {
            i = 0;
            for (SourceSong oldSource : oldSourcesSongs) {

                if (oldSource.getTitre() == newSource.getTitre() && oldSource.getUrlCloudBackground() != newSource.getUrlCloudBackground()) {

                    tempList.add(newSource);
                }

                if (newSource.getTitre() != oldSource.getTitre()) {
                    i++;
                }
            }

            if (i == oldSourcesSongs.size()) {
                tempList.add(newSource);
            }
        }

        return tempList;
    }

    private void uploadOnPhoneBgImages(List<SourceSong> sources) {

        for (SourceSong source : sources) {

            String cloudPath = source.getUrlCloudBackground();

            String nom = mStorage.getReference().child("songs/photos_background/").getName();

            mStorageRef = mStorage.getReferenceFromUrl(cloudPath);

            String filename = mStorageRef.getName();

            File localFile = null;
            localFile = new File(mContext.getFilesDir(), filename);

            Log.d("coucou", "uploadOnPhoneBgImages: " + localFile.getParent() + " " + filename + " " + nom + " " + localFile.getPath() + " " + localFile.getAbsolutePath());

            mStorageRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file

                            Toast.makeText(mContext, "Vos images de fond sont enregistrées sur votre téléphone", Toast.LENGTH_LONG).show();
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

    private void downloadMp3() {
        listDownloadMp3 = getListDownloadMp3();
        if (listDownloadMp3 != null) {
            //uploadOnPhoneMp3(listDownloadMp3);
        }else {
            Log.d("coucou", "downloadMP3: pas d'images de fichiers audio à sauvegarder");
        }
    }


    private List<Song> getListDownloadMp3() {

        final List<Song> tempList = new ArrayList<>();


        Log.d("coucou", "getListDownloadMp3: "+current_user_id);

        db.collection("users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    String pupitreStr = (String) task.getResult().get("pupitre");

                    Log.d("coucou", "onComplete: "+pupitreStr);

                    pupitreUser =SongsUtilities.converttoPupitre(pupitreStr);

                    Log.d("coucou", "getListDownloadMp3: "+pupitreUser);
                    Log.d("coucou", "getListDownloadMp3: "+songs.size());
                    Log.d("coucou", "getListDownloadMp3: "+oldSongs.size());


                    //todo tri sur le pupitre plus tard il faudra être capable de s'adapter aux préférences
                    //todo préparer une page de setUp pour mettre les infos complémentaires sur le user comme son pupitre

                    for (Song newSong : songs) {
                        int i = 0;
                        Log.d("coucou", "onComplete: newsongs"+ newSong.getSourceSongTitre()+" " + newSong.getRecordSource()+" "+newSong.getPupitre() );
                        if(newSong.getPupitre()==pupitreUser) {
                            for (Song oldSong : oldSongs) {

                                Log.d("coucou", "onComplete:  oldsong"+oldSong.getSourceSongTitre()+" "+oldSong.getRecordSource()+" "+oldSong.getPupitre());
                                if (oldSong.getSourceSongTitre() == newSong.getSourceSongTitre() && oldSong.getPupitre() == newSong.getPupitre() && oldSong.getRecordSource() == newSong.getRecordSource() && oldSong.getUrlCloudMp3() != newSong.getUrlCloudMp3()) {
                                    tempList.add(newSong);
                                    Log.d("coucou", "onComplete:premier if ");
                                }

                                if ((newSong.getSourceSongTitre() != newSong.getSourceSongTitre()) || (oldSong.getSourceSongTitre() == newSong.getSourceSongTitre() && oldSong.getPupitre() != newSong.getPupitre()) || (oldSong.getSourceSongTitre() == newSong.getSourceSongTitre() && oldSong.getRecordSource() != newSong.getRecordSource())) {
                                    i++;
                                    Log.d("coucou", "onComplete:deuxième if "+i+" "+ oldSongs.size());
                                }

                            }
                            if (i == oldSongs.size()) {
                                tempList.add(newSong);
                            }
                        }
                    }
                    Log.d("coucou", "getListDownloadMp3: "+tempList.size());

                }else{
                    Log.d("coucou", "onComplete: erreur de récupération du pupitre");
                }
            }
        });

        return tempList;
    }

    private void uploadOnPhoneMp3(List<Song> listMp3) {

        for (Song song : listMp3) {

            String cloudPath = song.getUrlCloudMp3();

            //todo retirer car inutile idem au dessus
            String nom = mStorage.getReference().child("songs/fichier_mp3/").getName();

            mStorageRef = mStorage.getReferenceFromUrl(cloudPath);

            String filename = mStorageRef.getName();

            File localFile = null;
            localFile = new File(mContext.getFilesDir(), filename);

            Log.d("coucou", "uploadOnPhoneMp3: " + localFile.getParent() + " " + filename + " " + nom + " " + localFile.getPath() + " " + localFile.getAbsolutePath()+" "+ mContext.getFilesDir());

            mStorageRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file

                            Toast.makeText(mContext, "Vos chants sont enregistrés sur votre téléphone", Toast.LENGTH_LONG).show();
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
}


