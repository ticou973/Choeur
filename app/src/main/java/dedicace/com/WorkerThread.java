package dedicace.com;

import android.os.Handler;
import android.os.HandlerThread;

public class WorkerThread extends HandlerThread {

    public static final String TAG = "WorkerThread";
    private Handler handler;

    public WorkerThread(){
        super(TAG);
        start();
        handler = new Handler(getLooper());
    }

    public WorkerThread execute(Runnable task){

        handler.post(task);

        return this;
    }

}
