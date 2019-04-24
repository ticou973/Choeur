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

public class ChooseMp3 extends AppCompatActivity implements Mp3Adapter.OnItemListener{
    private RecyclerView recyclerMp3;
    private Mp3Adapter mP3Adapter;
    private List<String> listMp3s = new ArrayList<>();
    private String[] listArrayMp3s;
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG ="coucou";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mp3);

        recyclerMp3 = findViewById(R.id.recyclerview_local_mp3);

        Intent intent = getIntent();
        listArrayMp3s=intent.getStringArrayExtra("listMp3s");

        for (String name:listArrayMp3s) {
            listMp3s.add(name);
        }
        Log.d(TAG, "CMp3 onCreate: "+listMp3s);

        mP3Adapter = new Mp3Adapter(listMp3s);
        layoutManager = new LinearLayoutManager(this);
        recyclerMp3.setLayoutManager(layoutManager);
        recyclerMp3.setHasFixedSize(true);
        recyclerMp3.setAdapter(mP3Adapter);
    }

    @Override
    public void onItemClick(int i) {
        Log.d(TAG, "CMp3 onItemClick: "+i);
        Intent result = new Intent();
        result.putExtra("mp3selected",i);
        setResult(RESULT_OK,result);
        finish();
    }
}
