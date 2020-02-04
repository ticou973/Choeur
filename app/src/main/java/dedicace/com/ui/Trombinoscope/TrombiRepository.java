package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import dedicace.com.data.database.Choriste;
import dedicace.com.data.database.ChoristeDao;

public class TrombiRepository {

    private static TrombiRepository sInstance;
    private static final Object LOCK = new Object();
    private static final String LOG_TAG ="coucou" ;


    private ChoristeDao mChoristeDao;
    private TrombiNetWorkDataSource trombiNetWorkDataSource;


    public TrombiRepository(ChoristeDao mChoristeDao, TrombiNetWorkDataSource trombiNetWorkDataSource) {
        this.mChoristeDao = mChoristeDao;
        this.trombiNetWorkDataSource = trombiNetWorkDataSource;
    }

    public static synchronized TrombiRepository getInstance(ChoristeDao choristeDao, TrombiNetWorkDataSource networkDataSource) {

        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new TrombiRepository(choristeDao,networkDataSource);

                Log.d(LOG_TAG, "CR getInstance: new repository");
            }
        }
        return sInstance;
    }

    private void initializeData() {

        if(isFetchNeeded()){



        }else{

        }


    }

    private boolean isFetchNeeded() {




        return true;
    }



    public LiveData<List<Choriste>> getChoristes() {

        initializeData();

        return mChoristeDao.getAllChoristesLive();
    }
}
