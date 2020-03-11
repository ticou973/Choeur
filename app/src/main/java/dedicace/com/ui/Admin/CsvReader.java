package dedicace.com.ui.Admin;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    private InputStream inputStream;
    private static final String TAG ="coucou";

    public CsvReader(InputStream inputStream) {

        this.inputStream = inputStream;
        Log.d(TAG, "CsvReader: "+inputStream.toString());
    }

    public List<String[]> read(){
        List<String[]> resultList = new ArrayList<String[]>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try{
            String csvLine;
            while ((csvLine=reader.readLine())!=null){
                String[] row = csvLine.split(";");
                resultList.add(row);
                Log.d(TAG, "Csv read: row "+row);
            }
            Log.d(TAG, "CsvR read: "+resultList);

        }catch(IOException e){
            throw new RuntimeException("Error in Reading CSV file "+e);

        }finally{
            try{
                inputStream.close();

            }catch(IOException ex) {
            throw new RuntimeException("Error While closing input stream "+ ex);
            }
        }
        return resultList;
    }
}
