package dedicace.com.data.networkdatabase;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import dedicace.com.ui.SongsAdapter;
import dedicace.com.utilities.InjectorUtils;

public class ChoraleSyncIntentService extends IntentService {

    public ChoraleSyncIntentService() {
        super("ChoraleSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(SongsAdapter.TAG, "CSIS Intent service started "+Thread.currentThread().getName());
        ChoraleNetWorkDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(this.getApplicationContext());

        networkDataSource.fetchSongs();
        Log.d("coucou", "CSIS onHandleIntent: fin du travail "+Thread.currentThread().getName());
    }
}
