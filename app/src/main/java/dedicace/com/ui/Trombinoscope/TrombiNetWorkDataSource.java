package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

import dedicace.com.data.database.Choriste;
import dedicace.com.data.database.Pupitre;
import dedicace.com.utilities.AppExecutors;

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
    private String current_user_id;
    private Pupitre pupitreUser;
    private FirebaseAuth mAuth;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final MutableLiveData<List<Choriste>> mChoristes;


    private TrombiNetWorkDataSource(Context context, Context mAContext, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mChoristes = new MutableLiveData<>();

        db = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        current_user_id= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        current_user_id =sharedPreferences.getString("userId", "");


        //todo voir si cela fonctionne dans tous les cas et supprimer si pas utile dans le progress
        if(mAContext!=context){
            mlistener = (TrombiNetWorkDataSource.OnChoristeNDSListener) mAContext;
        }
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
}
