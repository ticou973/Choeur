package dedicace.com.utilities;

import android.os.Environment;

import java.io.File;

public class StorageUtilities {

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String getInternalPath(String nomFichier){

        return "";
    }


    public static String getExternalPath(String nomFichier,String extension){

        File directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC);

        return directory.getAbsolutePath()+"/"+nomFichier+"."+extension;
    }

    public static File getExternalPathFile(String nomFichier){

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        return new File(path,nomFichier);
    }


}
