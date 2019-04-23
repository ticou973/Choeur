package dedicace.com.utilities;

import android.os.Environment;

import java.io.File;

public class StorageUtilities {

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getInternalPath(String nomFichier){

        String path="";



        return path;
    }


    public static String getExternalPath(String nomFichier,String extension){

        String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+nomFichier+"."+extension;

        return path;
    }

    public static File getExternalPathFile(String nomFichier){

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        File file = new File(path,nomFichier);

        return file;
    }


}
