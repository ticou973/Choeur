package dedicace.com.utilities;

import android.content.Context;

import dedicace.com.data.database.AppDataBase;
import dedicace.com.ui.PlaySong.ChoraleNetWorkDataSource;
import dedicace.com.ui.PlaySong.ChoraleRepository;
import dedicace.com.ui.PlaySong.MainActivityViewModelFactory;
import dedicace.com.ui.Trombinoscope.TrombiActivityViewModelFactory;
import dedicace.com.ui.Trombinoscope.TrombiNetWorkDataSource;
import dedicace.com.ui.Trombinoscope.TrombiRepository;

public class InjectorUtils {

    private static ChoraleRepository provideRepository(Context context, Context mAContext) {
        AppDataBase database = AppDataBase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        ChoraleNetWorkDataSource networkDataSource =
                ChoraleNetWorkDataSource.getInstance(context.getApplicationContext(), mAContext,executors);
        return ChoraleRepository.getInstance(database.songsDao(), database.sourceSongDao(),database.saisonDao(),database.spectacleDao(), networkDataSource);
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


    private static TrombiRepository provideTrombiRepository(Context context, Context mAContext) {
        AppDataBase database = AppDataBase.getInstance(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        TrombiNetWorkDataSource networkDataSource =
                TrombiNetWorkDataSource.getInstance(context.getApplicationContext(), mAContext,executors);
        return TrombiRepository.getInstance(database.choristeDao(), networkDataSource);
    }

    public static TrombiActivityViewModelFactory provideChoristeViewModelFactory(Context context, Context mAContext) {
        TrombiRepository repository = provideTrombiRepository(context.getApplicationContext(),mAContext);
        return new TrombiActivityViewModelFactory(repository);
    }

    public static TrombiNetWorkDataSource provideChoristeNetworkDataSource(Context context, Context mAContext) {
        // This call to provide repository is necessary if the app starts from a service - in this
        // case the repository will not exist unless it is specifically created.
        provideTrombiRepository(context.getApplicationContext(),mAContext);
        AppExecutors executors = AppExecutors.getInstance();
        return TrombiNetWorkDataSource.getInstance(context.getApplicationContext(), mAContext,executors);
    }
}
