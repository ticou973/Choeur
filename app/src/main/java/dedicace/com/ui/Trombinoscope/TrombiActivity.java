package dedicace.com.ui.Trombinoscope;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Choriste;

public class TrombiActivity extends AppCompatActivity {

    private List<Choriste> choristes= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trombi);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //faitr une liste de chorsites pour que cela marche

        RecyclerView recyclerView = findViewById(R.id.recyclerview_trombi);
        //Adapter
        TrombiAdapter trombiAdapter = new TrombiAdapter(this, choristes);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(trombiAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
