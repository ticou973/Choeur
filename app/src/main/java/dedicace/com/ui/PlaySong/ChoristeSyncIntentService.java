package dedicace.com.ui.PlaySong;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import dedicace.com.ui.Trombinoscope.TrombiNetWorkDataSource;
import dedicace.com.utilities.InjectorUtils;


public class ChoristeSyncIntentService extends IntentService {


    public ChoristeSyncIntentService() {
        super("ChoristeSyncIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            TrombiNetWorkDataSource trombiNetWorkDataSource = InjectorUtils.provideChoristeNetworkDataSource(this.getApplicationContext(),this.getApplicationContext());

            switch (intent.getStringExtra("origine")) {
                case "maj":
                    trombiNetWorkDataSource.fetchMajClouDb();
                    Log.d("coucou", "CSISchoriste onHandleIntent: fin du travail maj " + Thread.currentThread().getName());
                    break;
                case "choristes":
                    trombiNetWorkDataSource.fetchChoristes();
                    Log.d("coucou", "CSISchoriste onHandleIntent: fin du travail choristes " + Thread.currentThread().getName());
                    break;
            }
        }
    }
}
