package dedicace.com.utilities;

import android.util.Log;

public class Processor {

    private final Object lock = new Object();

    public void produce(){
        synchronized (lock){

            try {
                Log.d("coucou", "Processor produce: avant wait");
                lock.wait();
                Log.d("coucou", "Processor produce: après wait");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void consume(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (lock){
            Log.d("coucou", "Processor consume: avant notify");
            lock.notify();
            Log.d("coucou", "Processor consume: après notify");
        }
    }
}
