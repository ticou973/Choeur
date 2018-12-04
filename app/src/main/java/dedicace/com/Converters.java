package dedicace.com;

import android.arch.persistence.room.TypeConverter;

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
}
