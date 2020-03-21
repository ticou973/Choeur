package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dedicace.com.R;

public class GenList extends AppCompatActivity {

    private GenAdapter genAdapter;
    private ArrayList<String> listGen = new ArrayList<>();
    private ArrayList<String> listGenComp = new ArrayList<>();
    private ArrayList<String> listGenTemp = new ArrayList<>();

    private ArrayList<String> lieux = new ArrayList();
    private ArrayList<Date> dates = new ArrayList<>();
    private List<String> datesStr = new ArrayList<>();
    private static final String TAG ="coucou";
    private final static int REQUEST_CODE=100;
    private final static int REQUEST_CODEB=200;
    private final static int REQUEST_CODEC=300;
    private String titreSong,idSong, origine, idChorale, nomChoraleStr,idSpectacle,nomSpectacle;
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gen_list);
        FloatingActionButton fab = findViewById(R.id.fab_modif_gen);
        RecyclerView recyclerView = findViewById(R.id.recycler_gen);
        LinearLayoutManager layoutManager = new LinearLayoutManager(GenList.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        getBundleIntent();

        ItemTouchHelper helper= new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(listGen,position_dragged,position_target);
                Collections.swap(listGenComp,position_dragged,position_target);
                Collections.swap(listGenTemp,position_dragged,position_target);

                genAdapter.notifyItemMoved(position_dragged,position_target);
                Log.d(TAG, "GL onMove: "+listGen+" "+listGenComp+" "+listGenTemp);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                listGen.remove(viewHolder.getAdapterPosition());
                listGenComp.remove(viewHolder.getAdapterPosition());
                listGenTemp.remove(viewHolder.getAdapterPosition());
                genAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                Log.d(TAG, "GL onSwiped: "+listGen+" "+listGenComp+" "+listGenTemp);
            }
        });

        helper.attachToRecyclerView(recyclerView);

        genAdapter = new GenAdapter(listGenComp);
        recyclerView.setAdapter(genAdapter);


        fab.setOnClickListener(view -> {
            Log.d(TAG, "GL onCreate: On Click fab");
            Intent result = new Intent();
            if(origine.equals("modifTitres")||origine.equals("modifNomsSaisons")) {
                result.putStringArrayListExtra("listGenModif", listGen);
            }else{
                result.putStringArrayListExtra("listGenModif", listGenTemp);
            }
            result.putStringArrayListExtra("listGenCompModif",listGenComp);
            setResult(RESULT_OK,result);
            finish();
        });
    }

    private void getBundleIntent() {
        listGen.clear();
        listGenComp.clear();
        listGenTemp.clear();
        Intent intent = getIntent();
        Log.d(TAG, "GL getBundleIntent: ");
        if(intent!=null) {
            origine =intent.getStringExtra("origine");
            if (origine.equals("modifTitres")) {
                listGen.addAll(intent.getStringArrayListExtra("titres"));
                listGenComp.addAll(intent.getStringArrayListExtra("oldTitreNames"));
                listGenTemp.addAll(intent.getStringArrayListExtra("oldTitreNames"));
                Log.d(TAG, "GL Titres getBundleIntent: "+listGenComp+" "+listGen+" "+listGenTemp);

            } else if (origine.equals("modifConcerts")) {
                listGenComp.addAll(intent.getStringArrayListExtra("lieux"));
                listGenTemp.addAll(intent.getStringArrayListExtra("datesLong"));
                Log.d(TAG, "GL concerts getBundleIntent: 1 "+listGenComp+" "+listGen+" "+listGenTemp);
                for(String datelong:listGenTemp){
                    long datel= Long.parseLong(datelong);
                    listGen.add(new Date(datel).toString());
                }

                Log.d(TAG, "GL concerts getBundleIntent: "+listGenComp+" "+listGen+" "+listGenTemp);
            } else if (origine.equals("modifNomsSaisons")){
                listGen.addAll(intent.getStringArrayListExtra("nomsSpectacles"));
                listGenComp.addAll(intent.getStringArrayListExtra("oldSpectaclesNames"));
                listGenTemp.addAll(intent.getStringArrayListExtra("oldSpectaclesNames"));

                Bundle args;
                args = intent.getBundleExtra("bundleChorale");
                idChorale= args.getString("idChorale");
                nomChoraleStr =args.getString("nomChoraleStr");
            }
        }
    }

    private class GenAdapter extends RecyclerView.Adapter<GenViewHolder>{
        ArrayList<String> genListComp;

        public GenAdapter(ArrayList<String> genList) {
            this.genListComp = genList;
        }

        @NonNull
        @Override
        public GenViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_gen_list, viewGroup, false);
            return new GenViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GenViewHolder genViewHolder, int i) {
            if(listGen!=null&&listGen.size()!=0&&listGenComp!=null&&listGenComp.size()!=0) {
                genViewHolder.genName.setText(listGenComp.get(i));
                genViewHolder.genSubName.setText(listGen.get(i));
            }
        }

        @Override
        public int getItemCount() {
            return genListComp.size();
        }
    }

    private class GenViewHolder extends RecyclerView.ViewHolder {
        TextView genName,genSubName;
        CardView cvGen;
        public GenViewHolder(@NonNull View itemView) {
            super(itemView);
            genName = itemView.findViewById(R.id.genName);
            genSubName = itemView.findViewById(R.id.genSubName);
            cvGen = itemView.findViewById(R.id.cv_list_gen);
            genName.setOnLongClickListener(view -> {
                position = getAdapterPosition();
                Log.d(TAG, "GL onLongClick: "+position);
                if(origine.equals("modifTitres")) {
                    Intent startModifySSActivity = new Intent(GenList.this, ModifySourceSong.class);
                    startModifySSActivity.putExtra("origine", "GenList");
                    startActivityForResult(startModifySSActivity, REQUEST_CODE);
                }else if(origine.equals("modifConcerts")) {
                    Log.d(TAG, "GL onLongClick: modifconcerts");
                    Intent startAddConcert = new Intent(GenList.this, AddConcert.class);
                    startAddConcert.putExtra("origine", "GenList");
                    startActivityForResult(startAddConcert,REQUEST_CODEB);
                }else if(origine.equals("modifNomsSaisons")){
                    Log.d(TAG, "GL onLongClick: modifNomsSpectacles");
                    Intent startModifSpectacle = new Intent(GenList.this, ModifySpectacle.class);
                    startModifSpectacle.putExtra("origine", "GenList");
                    Bundle args = new Bundle();
                    args.putString("idChorale",idChorale);
                    args.putString("nomChorale",nomChoraleStr);
                    args.putString("origine","GenList");
                    startModifSpectacle.putExtra("bundleChorale",args);
                    startActivityForResult(startModifSpectacle,REQUEST_CODEC);
                }
                return false;
            });

            genName.setOnClickListener(view -> Log.d(TAG, "GL onClick: pour modif"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            if(requestCode==REQUEST_CODE) {
                if (data != null) {
                    Log.d(TAG, "GL onActivityResult: request_codeB");
                    titreSong = data.getStringExtra("titreselected");
                    idSong = data.getStringExtra("idselected");

                    listGenComp.add(position,titreSong);
                    listGen.add(position,idSong);
                    listGenTemp.add(position,titreSong);

                    genAdapter.notifyItemInserted(position);
                    Log.d(TAG, "GL onActivityResult: request_codeB " + titreSong+" "+idSong+" "+listGenComp+" "+ listGen);
                }
            } else if (requestCode==REQUEST_CODEB) {
                if(data!= null){
                    Log.d(TAG, "GL onActivityResult: 1");

                        lieux = data.getStringArrayListExtra("lieuxconcerts");
                        datesStr = data.getStringArrayListExtra("datesconcerts");

                        for (String dateStr: datesStr){
                            dates.add(new Date(Long.parseLong(dateStr)));
                        }
                        Log.d(TAG, "GL onActivityResult: "+lieux+" "+ dates);
                    for(String lieu:lieux) {
                        int index = lieux.indexOf(lieu);
                        listGenComp.add(position,lieu);
                        listGen.add(position,dates.get(index).toString());
                        listGenTemp.add(position,String.valueOf(dates.get(index).getTime()));
                        genAdapter.notifyItemInserted(position);
                    }

                    Log.d(TAG, "GL onActivityResult: request_codeB " + titreSong+" "+idSong+" "+listGenComp+" "+ listGen);
                }
            }else if (requestCode==REQUEST_CODEC) {
                if (data != null) {
                    Log.d(TAG, "GL onActivityResult: 2 ");

                    idSpectacle=data.getStringExtra("idselected");
                    nomSpectacle=data.getStringExtra("nomselected");

                    Log.d(TAG, "GL onActivityResult: 2" + idSpectacle + " " + nomSpectacle);

                    listGenComp.add(position,nomSpectacle);
                    listGen.add(position,idSpectacle);
                    listGenTemp.add(position,nomSpectacle);

                    genAdapter.notifyItemInserted(position);

                    Log.d(TAG, "GL onActivityResult: request_codeC " +listGenComp + " " + listGen);
                }
            }
        }

    }
}


