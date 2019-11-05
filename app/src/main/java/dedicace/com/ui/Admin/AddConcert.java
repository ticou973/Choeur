package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dedicace.com.R;

public class AddConcert extends AppCompatActivity {

    private EditText etLieu;
    private TextView tvDate, tvLieu;
    private Button addConcert, addConcertSpectacle;
    private String lieuConcert;
    private Date dateConcert;
    private Calendar calendar;
    private CalendarView cvdateConcert;
    private static final String TAG ="coucou";
    private ArrayList<String> lieux = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();
    private ArrayList<String> datesStr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_concert);



        etLieu = findViewById(R.id.et_lieu_concert);
        tvDate = findViewById(R.id.tv_date_concert);
        addConcert = findViewById(R.id.btn_add_concert);
        addConcertSpectacle = findViewById(R.id.btn_ajout_concert_spectacle);
        cvdateConcert = findViewById(R.id.calendarView_concert);
        tvLieu = findViewById(R.id.tv_list_concerts);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addConcert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lieuConcert = etLieu.getText().toString();

                if(!TextUtils.isEmpty(lieuConcert)&&dateConcert!=null){
                    Log.d(TAG, "AC onClick: "+ lieuConcert+" "+ dateConcert.toString());
                    lieux.add(lieuConcert);
                    dates.add(dateConcert);

                    StringBuilder sbLieu = new StringBuilder(" ");
                    String newLine = System.getProperty("line.separator");

                    int i=0;
                    for(String lieu:lieux){
                        i++;

                        String listLieux = i+". "+lieu+newLine;
                        sbLieu.append(listLieux+ " " + dates.get(i-1).toString()+newLine);
                    }

                    tvLieu.setText(sbLieu.toString());
                    etLieu.setText("");
                    tvDate.setText("");

                }else{
                    Toast.makeText(AddConcert.this, "Vous devez remplir les 2 champs !", Toast.LENGTH_SHORT).show();
                }

            }
        });


        addConcertSpectacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "AC onClick: début OnClick "+dates);
                int j =0;
                for(Date date : dates){
                    Log.d(TAG, "onClick: ");
                    long dateLong = date.getTime();
                    datesStr.add(String.valueOf(dateLong));
                    j++;
                    Log.d(TAG, "onClick2: ");
                    Log.d(TAG, "AC1 onClick: envoi à Create spectacle "+datesStr+" "+lieux);
                }

                Log.d(TAG, "AC onClick: envoi à Create spectacle "+datesStr+" "+lieux);
                Intent result = new Intent();
                result.putExtra("datesconcerts",datesStr);
                result.putStringArrayListExtra("lieuxconcerts",lieux);
                result.putStringArrayListExtra("datesconcerts",datesStr);
                setResult(RESULT_OK,result);
                finish();
            }
        });


        cvdateConcert.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {

                calendar = Calendar.getInstance();
                calendar.set(i,i1,i2);
                dateConcert = new Date();
                dateConcert = calendar.getTime();
                Log.d(TAG, "AC onSelectedDayChange: "+ i+" "+i1+" "+i2+" "+dateConcert);
                tvDate.setText(dateConcert.toString());
            }
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
