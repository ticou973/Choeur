package dedicace.com.utilities;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.data.database.Pupitre;
import dedicace.com.data.database.RecordSource;
import dedicace.com.data.database.Song;
import dedicace.com.data.database.SongsDao;
import dedicace.com.data.database.SourceSong;
import dedicace.com.ui.SongsAdapter;

public class SongsUtilities {
    private static List<List<RecordSource>> RecordSources = new ArrayList<>();
    private static SongsDao mSongDao;


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


    public static List<List<RecordSource>> getRecordSources(List<SourceSong> sourceSongs, SongsDao songsDao){
        mSongDao=songsDao;
        if(sourceSongs!=null){
            for (SourceSong sourceSong : sourceSongs) {
                String titre = sourceSong.getTitre();
                RecordSources.add(getRecordSources(titre));
            }
            Log.d(SongsAdapter.TAG, "CR run: sourceSongs dans la database après B "+RecordSources.size());
        }
        return RecordSources;
    }

    public static List<RecordSource> getRecordSources(String titre) {

        List<RecordSource> sources= new ArrayList<>();

        List<Song> listBS;
        List<Song> listLIVE;

        listBS = mSongDao.getSongsBySourceTitre(RecordSource.BANDE_SON,titre);
        listLIVE=mSongDao.getSongsBySourceTitre(RecordSource.LIVE,titre);

        if(listBS.size()!=0&&listLIVE.size()!=0){
            sources.add(RecordSource.BANDE_SON);
            sources.add(RecordSource.LIVE);
        }else if(listBS.size()==0&&listLIVE.size()!=0){
            sources.add(RecordSource.LIVE);
        }else if(listBS.size()!=0&&listLIVE.size()==0){
            sources.add(RecordSource.BANDE_SON);
        }else{
            sources.add(RecordSource.NA);
        }
        return sources;
    }
}
