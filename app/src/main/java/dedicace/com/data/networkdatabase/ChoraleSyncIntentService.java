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
        Log.d(SongsAdapter.TAG, "Intent service started");
        ChoraleNetWorkDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(this.getApplicationContext());

        networkDataSource.fetchSongs();
        Log.d("coucou", "onHandleIntent: fin du travail ");
    }
}
