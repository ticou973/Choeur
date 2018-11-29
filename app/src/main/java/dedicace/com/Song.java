package dedicace.com;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;

import java.net.URI;

@Entity
public class Song {

    @PrimaryKey(autoGenerate = true)
    private int songId;

    @ColumnInfo(name = "titre")
    private String titre;

    @ColumnInfo(name = "groupe")
    private String groupe;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "uri")
    private URI uri;

    @ColumnInfo(name = "image_fond")
    private Bitmap bitmap;

}
