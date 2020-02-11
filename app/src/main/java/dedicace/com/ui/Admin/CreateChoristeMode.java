package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;

import dedicace.com.R;

public class CreateChoristeMode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_choriste_mode);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button createListCsv = findViewById(R.id.btn_list_csv);
        Button createChoristeUnit = findViewById(R.id.btn_create_unit_choriste);

        createListCsv.setOnClickListener(view -> {

            Intent startCreateListCSV = new Intent (CreateChoristeMode.this, CreateChoristeCsv.class);
            startActivity(startCreateListCSV);

        });

        createChoristeUnit.setOnClickListener(view -> {
            Intent startCreateChoristeUnit = new Intent(CreateChoristeMode.this, CreateChoristeUnit.class);
            startActivity(startCreateChoristeUnit);

        });
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
