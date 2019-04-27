package dedicace.com;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static AppExecutors sInstance;
    private final Executor diskIO;
    private final Executor mainThread;
    private final Executor networkIO;
    private final Executor storageIO;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread, Executor storageIO) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
        this.storageIO=storageIO;
    }

    public static AppExecutors getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                //todo vérifier le changement à 2 thread sur Disk
                sInstance = new AppExecutors(Executors.newSingleThreadExecutor(),
                        Executors.newSingleThreadExecutor(),
                        new MainThreadExecutor(),
                        Executors.newFixedThreadPool(2));
            }
        }
        return sInstance;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor storageIO() {
        return storageIO;
    }




    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }


    }
}
