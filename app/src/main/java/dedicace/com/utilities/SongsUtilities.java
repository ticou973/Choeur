package dedicace.com.utilities;

import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;

public class SongsUtilities {

    public static RecordSource convertToRecordSource (String codeRecord) {

        if (codeRecord.equals("LIVE")) {

            return RecordSource.LIVE;

        }else if (codeRecord.equals("BANDE_SON") ) {

            return RecordSource.BANDE_SON;

        }else if (codeRecord.equals("ORIGINAL")) {

            return RecordSource.ORIGINAL;

        }
        return RecordSource.NA;

    }

    public static Pupitre converttoPupitre (String codePupitre) {

        if (codePupitre.equals("BASS")) {

            return Pupitre.BASS;

        }else if (codePupitre.equals("TENOR") ) {

            return Pupitre.TENOR;

        }else if (codePupitre.equals("ALTO")) {

            return Pupitre.ALTO;

        }else if (codePupitre.equals("SOPRANO")) {

            return Pupitre.SOPRANO;

        }else if(codePupitre.equals("TUTTI")){

            return Pupitre.TUTTI;
        }

        return Pupitre.NA;

    }




}
