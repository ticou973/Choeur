 package dedicace.com.ui;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import dedicace.com.R;

public class SettingsActivity extends AppCompatActivity {

    //Attention, il ne faut pas oublier de demettre un preferenceTheme dans le style sinon crash

    //todo penser lorsque l'on delete une source song de supprimer les songs qui sont avec cela effera aussi lorsque l'utilisateur mettra à jour
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = this.getSupportActionBar();

         if(actionBar != null){
             actionBar.setDisplayHomeAsUpEnabled(true);
         }
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
