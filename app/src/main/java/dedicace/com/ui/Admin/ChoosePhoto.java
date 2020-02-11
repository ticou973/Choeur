package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;

public class ChoosePhoto extends AppCompatActivity implements PhotoAdapter.OnItemListener{

    private RecyclerView recyclerImage;
    private PhotoAdapter photoAdapter;
    private List<String> listImages = new ArrayList<>();
    private String[] listArrayImages;
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG ="coucou";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo);

        recyclerImage = findViewById(R.id.recyclerview_local_photo);

        Intent intent = getIntent();
        listArrayImages=intent.getStringArrayExtra("listimages");

        for (String name:listArrayImages) {
            listImages.add(name);
        }
        Log.d(TAG, "CB onCreate: "+listImages);

        photoAdapter = new PhotoAdapter(listImages);
        layoutManager = new LinearLayoutManager(this);
        recyclerImage.setLayoutManager(layoutManager);
        recyclerImage.setHasFixedSize(true);
        recyclerImage.setAdapter(photoAdapter);
    }

    @Override
    public void onItemClick(int i) {
        Log.d(TAG, "CP onItemClick: "+i);
        Intent result = new Intent();
        result.putExtra("imageselected",i);
        setResult(RESULT_OK,result);
        finish();
    }
}