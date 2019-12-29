package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
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

import dedicace.com.R;

public class GenList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private GenAdapter genAdapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> listGen = new ArrayList<>();
    private ArrayList<String> listGenComp = new ArrayList<>();
    private static final String TAG ="coucou";
    private final static int REQUEST_CODE=100;
    private String titreSong;
    private String idSong;
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gen_list);
        fab = findViewById(R.id.fab_modif_gen);
        recyclerView =findViewById(R.id.recycler_gen);
        layoutManager = new LinearLayoutManager(GenList.this);
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

                genAdapter.notifyItemMoved(position_dragged,position_target);
                Log.d(TAG, "GL onMove: "+listGen+" "+listGenComp);
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                listGen.remove(viewHolder.getAdapterPosition());
                listGenComp.remove(viewHolder.getAdapterPosition());
                genAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                Log.d(TAG, "GL onSwiped: "+listGen+" "+listGenComp);
            }
        });

        helper.attachToRecyclerView(recyclerView);

        genAdapter = new GenAdapter(listGenComp);
        recyclerView.setAdapter(genAdapter);


        fab.setOnClickListener(view -> {
            Log.d(TAG, "GL onCreate: On Click fab");
            Intent result = new Intent();
            result.putStringArrayListExtra("listGenModif",listGen);
            result.putStringArrayListExtra("listGenCompModif",listGenComp);
            setResult(RESULT_OK,result);
            finish();
        });
    }

    private void getBundleIntent() {
        Intent intent = getIntent();
        Log.d(TAG, "GL getBundleIntent: ");
        if(intent!=null) {
            if (intent.getStringExtra("origine").equals("modifTitres")) {
                listGen.addAll(intent.getStringArrayListExtra("titres"));
                listGenComp.addAll(intent.getStringArrayListExtra("oldTitreNames"));
                Log.d(TAG, "GL getBundleIntent: "+listGenComp+" "+listGen);

            } else if (intent.getStringArrayListExtra("origine").equals("modifConcerts")) {

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
            genViewHolder.genName.setText(listGenComp.get(i));
        }

        @Override
        public int getItemCount() {
            return genListComp.size();
        }
    }

    private class GenViewHolder extends RecyclerView.ViewHolder {
        TextView genName;
        public GenViewHolder(@NonNull View itemView) {
            super(itemView);
            genName = itemView.findViewById(R.id.genName);
            genName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    position = getAdapterPosition();
                    Intent startModifySSActivity = new Intent(GenList.this,ModifySourceSong.class);
                    startModifySSActivity.putExtra("origine","GenList");
                    startActivityForResult(startModifySSActivity,REQUEST_CODE);
                    return false;
                }
            });
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

                    genAdapter.notifyItemInserted(position);
                    Log.d(TAG, "GL onActivityResult: request_codeB " + titreSong+" "+idSong+" "+listGenComp+" "+ listGen);
                }
            }
        }

    }
}


