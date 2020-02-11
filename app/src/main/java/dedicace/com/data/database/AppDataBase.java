package dedicace.com.data.database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;

@Database(entities ={Song.class, SourceSong.class, Saison.class, Spectacle.class, Choriste.class}, version = 13, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDataBase extends RoomDatabase {

    private static final String LOG_TAG = AppDataBase.class.getSimpleName();
    private static final String DATABASE_NAME = "ChoeurDataBase";

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static AppDataBase sInstance;


    private static final Migration MIGRATION_11_12 = new Migration(11,12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d(LOG_TAG, "ADB migrate: entrée");
            database.execSQL("CREATE TABLE `Choriste`"+ " ("
                    +"`id_choriste_local` INTEGER PRIMARY KEY NOT NULL, "
                    + "`id_choriste_cloud` TEXT, "
                    + "`nom` TEXT, "
                    + "`prenom` TEXT, "
                    + "`pupitre` TEXT, "
                    + "`adresse` TEXT, "
                    + "`fix_tel` TEXT, "
                    + "`port_tel` TEXT, "
                    + "`email` TEXT, "
                    + "`role_choeur` TEXT, "
                    + "`role_admin` TEXT, "
                    + "`url_local_photo` TEXT, "
                    + "`url_cloud_photo` TEXT, "
                    + "`update_phone` INTEGER, "
                    + "`update_bg_phone` INTEGER)");

            database.execSQL("CREATE UNIQUE INDEX `index_Choriste_id_choriste_cloud` ON `Choriste`"+" ("+"`id_choriste_cloud`"+")");
        }
    };

    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Choriste "
                    + " ADD COLUMN id_chorale TEXT");
        }
    };

    public static AppDataBase getInstance(Context context) {
        Log.d(LOG_TAG, "Getting the database");
        Log.d("coucou", "DB getInstance: database");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDataBase.class, AppDataBase.DATABASE_NAME)
                        .addMigrations(MIGRATION_11_12,MIGRATION_12_13)
                        .fallbackToDestructiveMigration().build();
                Log.d(LOG_TAG, "Made new database");
                Log.d("coucou", "DB getInstance: new database");
            }
        }
        return sInstance;
    }
    public abstract ChoristeDao choristeDao();
    public abstract SongsDao songsDao();
    public abstract  SourceSongDao sourceSongDao();
    public abstract  SaisonDao saisonDao();
    public abstract SpectacleDao spectacleDao();

}
