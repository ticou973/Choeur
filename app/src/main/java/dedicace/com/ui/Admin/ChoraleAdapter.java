package dedicace.com.ui.Admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dedicace.com.R;

public class ChoraleAdapter extends RecyclerView.Adapter<ChoraleAdapter.ChoraleViewHolder> {

    private List<String> listChorales;
    private OnItemListener mListener;
    private static final String TAG="coucou";

    public ChoraleAdapter(List<String> listChorales) {
        this.listChorales = listChorales;
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public ChoraleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_chorale_cloud, viewGroup, false);

        return new ChoraleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoraleAdapter.ChoraleViewHolder choraleViewHolder, int i) {

        choraleViewHolder.choraleName.setText(listChorales.get(i));
    }

    @Override
    public int getItemCount() {
        return listChorales.size();
    }

    public class ChoraleViewHolder extends RecyclerView.ViewHolder {

        TextView choraleName;
        CardView cv;

        ChoraleViewHolder(@NonNull View itemView) {
            super(itemView);
            choraleName =itemView.findViewById(R.id.choralesNames);
            cv= itemView.findViewById(R.id.cv_list_chorale);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "CA onClick: "+getAdapterPosition());
                    int i = getAdapterPosition();
                    selectChorale(i);
                }
            });
        }
    }

    private void selectChorale(int i) {
        if(mListener!=null){
            Log.d(TAG, "CA selectChorale: "+i);
            mListener.onItemClick(i);
        }
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
}
