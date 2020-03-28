package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.data.database.Choriste;
import dedicace.com.data.database.ChoristeDao;

public class TrombiRepository {

    private static TrombiRepository sInstance;
    private static final Object LOCK = new Object();
    private static final String TAG ="coucou" ;
    private Thread threadOldData,threadOldData1,threadSynchro, currentThread, threadDoworkInRoom,threadReinit;
    private boolean isFromLocal;
    private String typeChoriste;
    private static Context context;

    private List<Choriste> oldChoristes = new ArrayList<>();


    private ChoristeDao mChoristeDao;
    private TrombiNetWorkDataSource trombiNetWorkDataSource;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Long majCloudDBLong;
    private Long majLocalDBLong;

    private List<String> listOldIdChoristes = new ArrayList<>();
    private List<String> listIdChoristes = new ArrayList<>();
    private List<Choriste> deletedChoristesList = new ArrayList<>();
    private List<Choriste> deletedPhotoSongsList = new ArrayList<>();
    private List<Choriste> modifiedChoristesList = new ArrayList<>();
    private List<Choriste> photoChoristesToDelete = new ArrayList<>();
    private List<Choriste> photoChoristesToDownload = new ArrayList<>();
    private List<Choriste> newChoristesList = new ArrayList<>();
    private List<Choriste> totalPhotoToDelete = new ArrayList<>();
    private List<Choriste> totalPhotoToDownload = new ArrayList<>();
    private List<Choriste> tempChoristes = new ArrayList<>();
    private List<Choriste> choristesAfterSync = new ArrayList<>();
    private List<Choriste> choristes1;



    public TrombiRepository(ChoristeDao mChoristeDao, TrombiNetWorkDataSource trombiNetWorkDataSource) {
        this.mChoristeDao = mChoristeDao;
        this.trombiNetWorkDataSource = trombiNetWorkDataSource;
        Log.d(TAG, "TrombiRepository: ");

        context = trombiNetWorkDataSource.getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        threadOldData = new Thread(() -> {
            oldChoristes = mChoristeDao.getAllChoristes();

            for(Choriste choriste :oldChoristes){
                Log.d(TAG, "TR TrombiRepository: old data "+choriste.getNom()+" "+choriste.getPrenom());
            }
        });
        threadOldData.start();


        LiveData<Long> majDBCloudLong = trombiNetWorkDataSource.getMajDBCloudLong();
        Log.d(TAG, "TR ChoraleRepository: getmajDBCCloud "+ majDBCloudLong);

        majDBCloudLong.observeForever(majclouddblong -> {
            majCloudDBLong = majclouddblong;
            Log.d(TAG, "TR Alerte Maj TRombiRepository: majCloudLong "+ majclouddblong);
            if(majLocalDBLong<majCloudDBLong){
                if(oldChoristes!=null&&oldChoristes.size()!=0){
                    typeChoriste="oldChoriste";
                    Log.d(TAG, "TR TrombiRepository modification : ok on lance startFetchChoristeService");
                    isFromLocal=true;
                    DoSynchronization(oldChoristes);
                    Log.d(TAG, "TR run: getOldChoristes: données initiales "+oldChoristes);
                    try {
                        threadSynchro.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(TAG, "TR join: interrupted exception");
                    }
                        Log.d(TAG, "TR ChoraleRepository: getData");
                        startFetchChoristesService();
                }else{
                    typeChoriste="newChoriste";
                    Log.d(TAG, "CR TrombiRepository new choriste : ok on lance startFetchSongService");
                    startFetchChoristesService();
                }
                Log.d(TAG, "TR TrombiRepository: ok on lance startFetchSongService "+threadSynchro);


            }else{
                //pour le cas aucune modif

                //maj des old après une relancement suite à modification
                threadOldData1 = new Thread(() -> {
                    oldChoristes = mChoristeDao.getAllChoristes();

                    for(Choriste choriste :oldChoristes){
                        Log.d(TAG, "TR TrombiRepository: old data B "+choriste.getNom()+" "+choriste.getPrenom());
                    }
                });
                threadOldData1.start();

                try{
                    threadOldData1.join();
                }catch  (InterruptedException e) {
                    e.printStackTrace();
                }
                if(oldChoristes!=null&&oldChoristes.size()!=0){
                    //chemin A aucune modification et données initiales
                    isFromLocal=true;
                    typeChoriste="oldChoriste";
                    //todo voir comment retirer les arguments qui sont inutiles
                    DoSynchronization(oldChoristes);
                    Log.d(TAG, "TR run: getOldChoristes : données initiales "+oldChoristes);
                }else{
                    //cas de réinitialisation de spectacles lors de la première installation
                    Log.d(TAG, "TR run: getOldChoristes : pas de données initiales else majCloud");
                    threadReinit = new Thread(() -> {
                        threadReinit = Thread.currentThread();
                        oldChoristes = mChoristeDao.getAllChoristes();
                    });
                    threadReinit.start();

                    try {
                        threadReinit.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(oldChoristes!=null&&oldChoristes.size()!=0) {
                        Log.d(TAG, "TR run:  old Cjoristes " + oldChoristes.size());
                        isFromLocal = true;
                        typeChoriste = "oldChoriste";
                        //todo voir comment retirer les arguments qui sont inutiles
                        DoSynchronization(oldChoristes);
                        Log.d(TAG, "TR run: getOldChoristes : données initiales B" + oldChoristes);
                    }
                }
                Log.d(TAG, "TR TrombiRepository: Stop startFetcch pas lancé !");
            }
        });


        final LiveData<String> downloads =trombiNetWorkDataSource.getDownloads();
        downloads.observeForever(message -> {
            if(message.equals("Done")){
                Log.d(TAG, "TR TrombiRepository: observer Done pour downloads");
                DoWorkInRoomAndLists();
           }else {
                Log.d(TAG, "TR TrombiRepository: il faut encore attendre... ");
            }
        });


        final LiveData<List<Choriste>> networkDataChoristes = trombiNetWorkDataSource.getChoristes();
        Log.d(TAG, "TR TrombiRepository: LiveData mChoraleNetworkdtasource Choriste "+trombiNetWorkDataSource+" "+ networkDataChoristes);
        networkDataChoristes.observeForever(choristes -> {
            //todo vérifier utilité de sourceSongs1
            if(choristes1!=null&&choristes1.size()!=0){
                Log.d(TAG, "TR TrombiRepository: "+choristes1.size()+choristes.size());
            }
            choristes1=choristes;

            Log.d(TAG, "TR Repository: observers Alerte cela bouge ! "+" "+choristes1.size()+" "+choristes1+Thread.currentThread().getName());

            isFromLocal=false;
            DoSynchronization(choristes);
        });
    }

    private void startFetchChoristesService() {
        Log.d(TAG, "TR repo startService: début");
        trombiNetWorkDataSource.startFetchChoristesService();
        Log.d(TAG, "TR repo startService: fin ");
    }

    private void DoWorkInRoomAndLists() {

        threadDoworkInRoom = new Thread(() -> {
            currentThread = Thread.currentThread();
            DoWorkInRoom();
            Log.d(TAG, "TR do workInRoom  ");
        });
        threadDoworkInRoom.start();
    }


    private void DoWorkInRoom() {
        Log.d(TAG, "TR DoWorkInRoom: entrée ");
        if(deletedChoristesList!=null&&deletedChoristesList.size()!=0){
            int temp = mChoristeDao.deleteChoristes(deletedChoristesList);
            Log.d(TAG, "TR DoWorkInRoom: deleteChoristes "+temp);
        }

        if(modifiedChoristesList!=null&&modifiedChoristesList.size()!=0){
            Log.d(TAG, "TR DoWorkInRoom: modify choristes");
            for (Choriste choriste:modifiedChoristesList) {
                Log.d(TAG, "TR synchronisationLocalDataBase: milieu choriste "+choriste.getNom()+" "+choriste.getUpdatePhone()+" "+choriste.getPrenom()+" "+choriste.getUrlCloudPhoto());
                String tempIdCloud = choriste.getIdChoristeCloud();

                Log.d(TAG, "TR DoWorkInRoom : idCloud : "+tempIdCloud);
                Choriste tempChoriste = mChoristeDao.getChoristeByIdCloud(tempIdCloud);
                Log.d(TAG, "TR DoWorkInRoom: tempChoriste "+tempChoriste);
                if(tempChoriste!=null) {
                    tempChoriste.setNom(choriste.getNom());
                    tempChoriste.setPrenom(choriste.getPrenom());
                    tempChoriste.setPupitre(choriste.getPupitre());
                    tempChoriste.setAdresse(choriste.getAdresse());
                    tempChoriste.setFixTel(choriste.getFixTel());
                    tempChoriste.setPortTel(choriste.getPortTel());
                    tempChoriste.setEmail(choriste.getEmail());
                    tempChoriste.setRoleChoeur(choriste.getRoleChoeur());
                    tempChoriste.setRoleAdmin(choriste.getRoleAdmin());
                    tempChoriste.setUpdatePhone(choriste.getUpdatePhone());
                    tempChoriste.setUrlCloudPhoto(choriste.getUrlCloudPhoto());

                    if(choriste.getUrlLocalPhoto()!=null) {
                        tempChoriste.setUrlLocalPhoto(choriste.getUrlLocalPhoto());
                    }
                    tempChoristes.add(tempChoriste);
                }else{
                    Log.d(TAG, "TR DoWorkInRoom: pb sur TempChoriste");
                }
            }
            if(tempChoristes!=null) {
                //relance la bête pour s'afficher
                int tempInt = mChoristeDao.upDateChoristes(tempChoristes);
                Log.d(TAG, "TR synchronisationLocalDataBase: nb d'update choristes " + tempInt);

                for(Choriste choriste:tempChoristes){
                    Log.d(TAG, "TR DoWorkInRoom: choriste corrigé "+choriste.getNom());
                }
            }
        }


        if(newChoristesList!=null&&newChoristesList.size()!=0){
            Log.d(TAG, "TR DoWorkInRoom: create choristes "+newChoristesList);
            mChoristeDao.bulkInsert(newChoristesList);
        }

        //chercher les Sourcesongs sur Room
        choristesAfterSync=mChoristeDao.getAllChoristes();

    }

    public static synchronized TrombiRepository getInstance(ChoristeDao choristeDao, TrombiNetWorkDataSource networkDataSource) {

        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new TrombiRepository(choristeDao,networkDataSource);

                Log.d(TAG, "CR getInstance: new repository");
            }
        }
        return sInstance;
    }



    private void DoSynchronization(List<Choriste> choristes) {

        threadSynchro = new Thread(() -> {
            currentThread = Thread.currentThread();
            Log.d(TAG, "CR run: currentThread "+currentThread+" "+isFromLocal);
            if(!isFromLocal) {
                if(typeChoriste.equals("oldChoriste")){
                    typeChoriste="modificationChoriste";
                }
                Log.d(TAG, "TR run: if from local avant synchronisation db "+typeChoriste);
                synchronisationLocalDataBase(choristes);

            }else{
                //chemin A : pas de modification
                //todo trouver une méthode un peu moins artificielle ? cf modèle architecture.
                Log.d(TAG, "TR run: else isFrom Local pas de synchronisation");
                //mis pour que alerte se déclenche
                mChoristeDao.updateChoriste(oldChoristes.get(0));
                choristesAfterSync=oldChoristes;
            }
            Log.d(TAG, "TR TrombiRepository LiveData après sync choristes : "+choristes.size()+ " "+choristesAfterSync.size()+" "+Thread.currentThread().getName());

        });

        Log.d(TAG, "CR DoSynchronization: juste avant T2 start");
        threadSynchro.start();



    }

    private void synchronisationLocalDataBase(List<Choriste> choristes) {
        editor = sharedPreferences.edit();
        editor.putLong("majTrombiDB",majCloudDBLong);
        editor.apply();

        Log.d(TAG, "TR synchronisationLocalDataBase: ");

        getModificationLists(choristes);
        DoWorkInLocalStorage();
        DoWorkDownloadCloud();

        if(threadDoworkInRoom!=null) {
            try {
                threadDoworkInRoom.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<Choriste> choristesAfter = mChoristeDao.getAllChoristes();
        Log.d(TAG, "TR synchronisationLocalDataBase: bilan des courses : Choristes "+choristesAfter.size());
    }



    private void DoWorkInLocalStorage() {
        //delete photos images
        if(deletedChoristesList!=null&&deletedChoristesList.size()!=0){
            Log.d(TAG, "TR DoWorkInLocalStorage: deletedChoristess "+deletedChoristesList);
            totalPhotoToDelete.addAll(deletedChoristesList);
        }

        if(photoChoristesToDelete!=null&&photoChoristesToDelete.size()!=0) {
            Log.d(TAG, "TR DoWorkInLocalStorage: photoChoristesTo delete "+photoChoristesToDelete);
            totalPhotoToDelete.addAll(photoChoristesToDelete);
        }
        Log.d(TAG, "TR DoWorkInLocalStorage: delete choriste et bg"+deletedChoristesList+" "+photoChoristesToDelete);


        if(totalPhotoToDelete!=null&&totalPhotoToDelete.size()!=0){
                Log.d(TAG, "TR DoWorkInLocalStorage: que Phototodelete "+photoChoristesToDelete);
                if(deletedPhotoSongsList!=null&&deletedPhotoSongsList.size()!=0) {
                    trombiNetWorkDataSource.deletePhotoOnPhone(deletedPhotoSongsList);
                }
        }else {
                Log.d(TAG, "TR DoWorkInLocalStorage: Aucun à delete "+photoChoristesToDelete);
        }
    }

    private void DoWorkDownloadCloud() {
        //Download Photo
        if(photoChoristesToDownload!=null&&photoChoristesToDownload.size()!=0) {
            Log.d(TAG, "CR DoWorkDownloadCloud:  photoChoristetoDownload "+photoChoristesToDownload);
            totalPhotoToDownload.addAll(photoChoristesToDownload);
        }
        if(newChoristesList!=null&&newChoristesList.size()!=0){
            Log.d(TAG, "TR DoWorkDownloadCloud:  newChoriste "+newChoristesList);
            totalPhotoToDownload.addAll(newChoristesList);
        }

        if(totalPhotoToDownload!=null&&totalPhotoToDownload.size()!=0){
                Log.d(TAG, "TR DoWorkDownloadCloud :  que photo download"+ totalPhotoToDownload);
                trombiNetWorkDataSource.downloadPhoto(totalPhotoToDownload);

        }else {
                Log.d(TAG, "CR DoWorkDownloadCloud: aucun download "+totalPhotoToDownload);
                DoWorkInRoomAndLists();
        }
    }

    private void getModificationLists(List<Choriste> choristes) {
        Log.d(TAG, "TR getModificationLists: "+" "+choristes.size());
        getListIdChoristes(choristes);
        deletedChoristesList();
        modifiedSongsList(choristes);
        newSongsList(choristes);
    }


    private void getListIdChoristes(List<Choriste> choristes) {
        for(Choriste choriste:oldChoristes){
            listOldIdChoristes.add(choriste.getIdChoristeCloud());
        }

        Log.d(TAG, "TR getListIdSourceSongs: size old "+listOldIdChoristes.size());

        for(Choriste choriste:choristes){
            listIdChoristes.add(choriste.getIdChoristeCloud());
        }

        Log.d(TAG, "CR getListIdSourceSongs: size new "+listIdChoristes.size());

    }

    private void deletedChoristesList() {

        List<String> tempIdChoristes = new ArrayList<>(listOldIdChoristes);

        tempIdChoristes.removeAll(listIdChoristes);

        for(String idStr:tempIdChoristes){
            int indexidStr = listOldIdChoristes.indexOf(idStr);
            deletedChoristesList.add(oldChoristes.get(indexidStr));
        }

        Log.d(TAG, "TR deletedSourceSongsList: "+ deletedChoristesList.size());
        for(Choriste choriste:deletedChoristesList){
            Log.d(TAG, "TR deletedSourceSongsList: "+choriste.getUpdatePhotoPhone());
            if(choriste.getUpdatePhotoPhone()!=null){
                deletedPhotoSongsList.add(choriste)  ;
            }
        }

    }

    private void modifiedSongsList(List<Choriste> choristes) {
        for (Choriste choriste:choristes) {
            for (Choriste oldChoriste: oldChoristes) {
                if(oldChoriste.getIdChoristeCloud().equals(choriste.getIdChoristeCloud())){

                    if(oldChoriste.getUpdatePhone().getTime()<choriste.getUpdatePhone().getTime()){
                        modifiedChoristesList.add(choriste);
                        if(!oldChoriste.getUrlCloudPhoto().equals(choriste.getUrlCloudPhoto())){
                            photoChoristesToDelete.add(oldChoriste);
                            photoChoristesToDownload.add(choriste);
                            Log.d(TAG, "TR modifiedChoristeList:  delete download ");
                        }
                    }
                }
            }
        }

        Log.d(TAG, "TR modifiedChoristesList: "+modifiedChoristesList.size());
        for(Choriste choriste:photoChoristesToDelete){
            Log.d(TAG, "TR deletedChoristesList: "+choriste.getUpdatePhotoPhone());
            if(choriste.getUpdatePhotoPhone()!=null){
                deletedPhotoSongsList.add(choriste);
            }
        }
    }

    private void newSongsList(List<Choriste> choristes) {
        newChoristesList=new ArrayList<>();
        List<String> tempIdChoristes = new ArrayList<>(listIdChoristes);

        tempIdChoristes.removeAll(listOldIdChoristes);

        Log.d(TAG, "TR newChoristesList: tempIDSS "+tempIdChoristes.size());

        for(String idStr:tempIdChoristes){
            int indexidStr = listIdChoristes.indexOf(idStr);
            newChoristesList.add(choristes.get(indexidStr));
        }
        Log.d(TAG, "CR newSourceSongsList: "+ newChoristesList.size());
    }


    private void initializeData() {
        Log.d(TAG, "TR initializeData: début ");

        if(isFetchNeeded()){

            getMajDateLocalDataBase();
            //lance la recherche d'une mise à jour et condition le lancement de startFetchData
            LoadMajCloudDB();

        }else{
            Log.d(TAG, "TR initializeData: inutile les données n'ont pas changées ");
            try {
                threadOldData.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "TR join: interrupted exception");
            }

            if(oldChoristes!=null&&oldChoristes.size()!=0){
                isFromLocal=true;
                typeChoriste="oldChoriste";
                DoSynchronization(oldChoristes);
                Log.d(TAG, "TR run: getOldSongs et SourcesSongs : données initiales "+oldChoristes);
            }else{
                Log.d(TAG, "TR run: getOldSongs et SourcesSongs : pas de données initiales ");
            }
            Log.d(TAG, "TR ChoraleRepository: Stop startFectch pas lancé !");
        }
    }

    private boolean isFetchNeeded() {
        return true;
    }

    private void getMajDateLocalDataBase() {
        majLocalDBLong =sharedPreferences.getLong("majTrombiDB",0);
    }

    private void LoadMajCloudDB() {
        trombiNetWorkDataSource.getMajDateCloudDataBase();
    }

    public LiveData<List<Choriste>> getChoristes() {
        initializeData();
        return mChoristeDao.getAllChoristesLive();
    }


    public List<Choriste> getListChoristes(){
        return choristesAfterSync;
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

    public String getTypeChoriste() {
        return typeChoriste;
    }

    public boolean getDeleted() {
        return trombiNetWorkDataSource.isDeleted();
    }
}
