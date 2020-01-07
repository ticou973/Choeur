package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import dedicace.com.R;

public class ChooseChorale extends AppCompatActivity {

    private static final int REQUEST_CODE_A = 100;
    private String idChorale, nomChoraleStr,origine;
    private Intent startModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_chorale);

        getIntentOrigine();

        Intent startModifyChorale = new Intent(ChooseChorale.this,ModifyChorale.class);
        startModifyChorale.putExtra("origine","ChooseChorale");
        startActivityForResult(startModifyChorale,REQUEST_CODE_A);
    }

    private void getIntentOrigine() {
        Intent intent = getIntent();
        origine = intent.getStringExtra("origine");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK) {

            if (requestCode == REQUEST_CODE_A) {
                if (data != null) {
                    idChorale = data.getStringExtra("idselected");
                    nomChoraleStr=data.getStringExtra("nomChorale");

                    if(origine.equals("AdminHomeModifSpectacle")){
                        startModify = new Intent(ChooseChorale.this,ModifySpectacle.class);
                    }else if(origine.equals("AdminHomeModifSaison")){
                        startModify = new Intent(ChooseChorale.this,ModifySaison.class);
                    }
                    Bundle args = new Bundle();
                    args.putString("idChorale",idChorale);
                    args.putString("nomChorale",nomChoraleStr);
                    args.putString("origine","ChooseChorale");
                    startModify.putExtra("bundleChorale",args);
                    startActivity(startModify);
                }
            }
        }
    }
}
