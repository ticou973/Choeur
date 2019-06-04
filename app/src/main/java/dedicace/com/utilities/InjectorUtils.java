package dedicace.com.utilities;

import android.content.Context;

import dedicace.com.AppExecutors;
import dedicace.com.data.ChoraleRepository;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.networkdatabase.ChoraleNetWorkDataSource;
import dedicace.com.ui.MainActivityViewModelFactory;

public class InjectorUtils {

    public static ChoraleRepository provideRepository(Context context,Context mAContext) {
        AppDataBase database = AppDataBase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        ChoraleNetWorkDataSource networkDataSource =
                ChoraleNetWorkDataSource.getInstance(context.getApplicationContext(), mAContext,executors);
        return ChoraleRepository.getInstance(database.songsDao(), database.sourceSongDao(),networkDataSource, executors);
    }

    public static ChoraleNetWorkDataSource provideNetworkDataSource(Context context, Context mAContext) {
        // This call to provide repository is necessary if the app starts from a service - in this
        // case the repository will not exist unless it is specifically created.
        provideRepository(context.getApplicationContext(),mAContext);
        AppExecutors executors = AppExecutors.getInstance();
        return ChoraleNetWorkDataSource.getInstance(context.getApplicationContext(), mAContext,executors);
    }

    public static MainActivityViewModelFactory provideViewModelFactory(Context context,Context mAContext) {
        ChoraleRepository repository = provideRepository(context.getApplicationContext(),mAContext);
        return new MainActivityViewModelFactory(repository);
    }
}
