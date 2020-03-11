package dedicace.com.ui.Admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;

public class ChooseCsv extends AppCompatActivity implements CsvAdapter.OnItemListener {

    private RecyclerView recyclerCsv;
    private CsvAdapter csvAdapter;
    private List<String> listCsv = new ArrayList<>();
    private String[] listArrayCsv;
    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG ="coucou";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_csv);

        recyclerCsv = findViewById(R.id.recyclerview_local_csv);

        Intent intent = getIntent();
        listArrayCsv=intent.getStringArrayExtra("listCsv");

        Log.d(TAG, "CCsv onCreate: "+listArrayCsv);

        for (String name:listArrayCsv) {
            listCsv.add(name);
        }
        Log.d(TAG, "CC onCreate: "+listCsv);

        csvAdapter = new CsvAdapter(listCsv);
        layoutManager = new LinearLayoutManager(this);
        recyclerCsv.setLayoutManager(layoutManager);
        recyclerCsv.setHasFixedSize(true);
        recyclerCsv.setAdapter(csvAdapter);
    }

    @Override
    public void onItemClick(int i) {
        Log.d(TAG, "CCA onItemClick: "+i);
        Intent result = new Intent();
        result.putExtra("csvselected",i);
        setResult(RESULT_OK,result);
        finish();
    }
}


class CsvAdapter extends RecyclerView.Adapter<CsvAdapter.CsvViewHolder>{
    private List<String> listCsv;
    private static final String TAG="coucou";
    private OnItemListener mListener;

    public CsvAdapter(List<String> listCsv) {
        this.listCsv = listCsv;
        Log.d(TAG, "CsvA ImageAdapter: "+listCsv);
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public CsvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_csv_file, viewGroup, false);

        return new CsvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CsvViewHolder csvViewHolder, int i) {
        csvViewHolder.csvName.setText(listCsv.get(i));
    }

    @Override
    public int getItemCount() {
        return listCsv.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        Context context = recyclerView.getContext();

        if (context instanceof OnItemListener) {
            mListener = (OnItemListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        mListener = null;
    }

    public class CsvViewHolder extends RecyclerView.ViewHolder {
        TextView csvName;
        CardView cv;

        public CsvViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv_csv);
            csvName = itemView.findViewById(R.id.tv_csv_name);

            cv.setOnClickListener(view -> {
                Log.d(TAG, "CCA onClick: "+getAdapterPosition());
                int i = getAdapterPosition();
                selectCsv(i);
            });
        }

        private void selectCsv(int numItem) {
            if(mListener!=null){
                Log.d(TAG, "CCA selectCsv: "+numItem);
                mListener.onItemClick(numItem);
            }
        }
    }
}
