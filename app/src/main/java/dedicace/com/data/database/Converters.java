package dedicace.com.data.database;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class Converters {

    @TypeConverter
    public static String pupitreToCodePupitre(Pupitre pupitre) {

        if (pupitre == Pupitre.BASS) {

            return "1_Bass";

        }else if (pupitre == Pupitre.TENOR) {

            return "2_Tenor";
        }else if (pupitre == Pupitre.ALTO) {

            return "3_Alto";
        }else if (pupitre == Pupitre.SOPRANO) {

            return "4_Soprano";

        }else if(pupitre==Pupitre.TUTTI){
            return "0_Tutti";
        }

        return "N/A";
    }

    @TypeConverter
    public static Pupitre CodePupitreToPupitre (String codePupitre) {

        if (codePupitre.equals("1_Bass")) {

            return Pupitre.BASS;

        }else if (codePupitre.equals("2_Tenor") ) {

            return Pupitre.TENOR;

        }else if (codePupitre.equals("3_Alto")) {

            return Pupitre.ALTO;

        }else if (codePupitre.equals("4_Soprano")) {

            return Pupitre.SOPRANO;

        }else if(codePupitre.equals("0_Tutti")){

            return Pupitre.TUTTI;
        }

        return Pupitre.NA;

    }


    @TypeConverter
    public static String recordToCodeRecord(RecordSource recordSource) {

        if (recordSource == RecordSource.LIVE) {

            return "Live";

        }else if (recordSource == RecordSource.BANDE_SON) {

            return "Bande Son";
        }else if (recordSource == RecordSource.ORIGINAL) {

            return "Original";
        }

        return "N/A";
    }

    @TypeConverter
    public static RecordSource CodeRecordToRecord (String codeRecord) {

        if (codeRecord.equals("Live")) {

            return RecordSource.LIVE;

        }else if (codeRecord.equals("Bande Son") ) {

            return RecordSource.BANDE_SON;

        }else if (codeRecord.equals("Original")) {

            return RecordSource.ORIGINAL;

        }
        return RecordSource.NA;

    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }


    @TypeConverter
    public static ArrayList<String> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    @TypeConverter
    public static String fromArrayList(ArrayList<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

    @TypeConverter
    public static ArrayList<Date> fromStringDate(String value) {
        ArrayList<Date> dates= new ArrayList<>();
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> datesString = new ArrayList();
        datesString =new Gson().fromJson(value, listType);
        Long dateLong;
        Date date;

        for(String dateString : datesString){
            dateLong = Long.parseLong(dateString);
            date = new Date(dateLong);
            dates.add(date);
        }
        Log.d("coucou", "C fromStringDate: "+dates);

        return dates;
    }
    @TypeConverter
    public static String fromArrayListDate(ArrayList<Date> list) {
        ArrayList<String> datesString= new ArrayList<>();
        Long dateLong = null;
        String dateString;
        for(Date date:list){
            if(date==null){
                date=null;
            }else{
                dateLong=date.getTime();
            }
            dateString = String.valueOf(dateLong);

            datesString.add(dateString);
            Log.d("coucou", "C fromArrayListDate: "+ datesString);
        }
        Gson gson = new Gson();
        String json = gson.toJson(datesString);
        Log.d("coucou", "C fromArrayListDate: "+json);
        return json;
    }
}


