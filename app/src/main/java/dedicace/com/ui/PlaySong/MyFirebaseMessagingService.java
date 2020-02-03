package dedicace.com.ui.PlaySong;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import dedicace.com.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG ="coucou";
    private static final String CANAL = "MyNotifCanal";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String message = remoteMessage.getNotification().getBody();
        Log.d(TAG, "MFMS onMessageReceived: "+message);


        //créer une notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CANAL);
        notificationBuilder.setContentTitle("MAJ Chansons");
        notificationBuilder.setContentText(message);
        notificationBuilder.setSmallIcon(R.drawable.logo_dedicace);

        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);


        //todo voir comment faire pour que cela relance l'ensemble du processus avec la maj
        //permet de vérifie qu'une application donné par son package est en cours
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals("dedicace.com")) { //ici le nom du package recherché
                Log.d(TAG, "MFMS onMessageReceived: passage dedicace ouverte ");

            }else{
                Log.d(TAG, "MFMS onMessageReceived: passage dedicace fermé ");
                Intent intent = new Intent(getApplicationContext(),SplashActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
                notificationBuilder.setContentIntent(pendingIntent);
            }
        }

        //envoyer la notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            String channelId = getString(R.string.notification_channel_id);
            String channelTitle = getString(R.string.notification_channel_title);
            String channelDescription = getString(R.string.notification_channel_description);

            NotificationChannel channel = new NotificationChannel(channelId,channelTitle,NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(channelId);
        }
        notificationManager.notify(1,notificationBuilder.build());
    }
}

