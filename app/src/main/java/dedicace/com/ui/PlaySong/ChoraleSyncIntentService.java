package dedicace.com.ui.PlaySong;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import dedicace.com.utilities.InjectorUtils;

public class ChoraleSyncIntentService extends IntentService {

    public ChoraleSyncIntentService() {
        super("ChoraleSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(SongsAdapter.TAG, "CSIS Intent service started "+Thread.currentThread().getName());
        ChoraleNetWorkDataSource networkDataSource = InjectorUtils.provideNetworkDataSource(this.getApplicationContext(),this.getApplicationContext());

        switch (intent.getStringExtra("origine")) {
            case "sources":
                networkDataSource.fetchSongs();
                Log.d("coucou", "CSIS onHandleIntent: fin du travail SS " + Thread.currentThread().getName());
                break;
            case "maj":
                networkDataSource.fetchMajClouDb();
                Log.d("coucou", "CSIS onHandleIntent: fin du travail maj " + Thread.currentThread().getName());
                break;
            case "download":
                // networkDataSource.downloadImagesMp3();
                Log.d("coucou", "CSIS onHandleIntent: fin du travail download " + Thread.currentThread().getName());
                break;
        }
    }
}
