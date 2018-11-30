package dedicace.com;

import android.arch.persistence.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static String pupitreToCodePupitre(Pupitre pupitre) {

        if (pupitre == Pupitre.BASS) {

            return "Bass";

        }else if (pupitre == Pupitre.TENOR) {

            return "Tenor";
        }else if (pupitre == Pupitre.ALTO) {

            return "Alto";
        }else if (pupitre == Pupitre.SOPRANO) {

            return "Soprano";
        }else if(pupitre==Pupitre.TUTTI){
            return "Tutti";
        }

        return "N/A";
    }

    @TypeConverter
    public static Pupitre CodePupitreToPupitre (String codePupitre) {

        if (codePupitre.equals("Bass")) {

            return Pupitre.BASS;

        }else if (codePupitre.equals("Tenor") ) {

            return Pupitre.TENOR;

        }else if (codePupitre.equals("Alto")) {

            return Pupitre.ALTO;

        }else if (codePupitre.equals("Soprano")) {

            return Pupitre.SOPRANO;

        }else if(codePupitre=="Tutti"){

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
