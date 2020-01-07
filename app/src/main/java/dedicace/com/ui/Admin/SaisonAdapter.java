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
import dedicace.com.data.database.Saison;

public class SaisonAdapter extends RecyclerView.Adapter<SaisonAdapter.SaisonViewHolder> {

    private List<Saison> listSaisons;
    private OnItemListener mListener;
    private static final String TAG="coucou";

    public SaisonAdapter(List<Saison> listSaisons) {
        this.listSaisons = listSaisons;
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }


    @NonNull
    @Override
    public SaisonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_saison_cloud,viewGroup,false);
        return new SaisonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaisonViewHolder saisonViewHolder, int i) {
        saisonViewHolder.nom.setText(listSaisons.get(i).getSaisonName());
    }

    @Override
    public int getItemCount() {
        return listSaisons.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        Context context = recyclerView.getContext();

        if (context instanceof SaisonAdapter.OnItemListener) {
            mListener = (SaisonAdapter.OnItemListener) context;
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

    public class SaisonViewHolder extends RecyclerView.ViewHolder {
        TextView nom;
        CardView cv;
        public SaisonViewHolder(@NonNull View itemView) {
            super(itemView);

            nom = itemView.findViewById(R.id.saisonName);
            cv=itemView.findViewById(R.id.cv_list_saisons);

            cv.setOnClickListener(view -> {
                Log.d(TAG, "SpA onClick: "+getAdapterPosition());
                int i = getAdapterPosition();
                selectSaison(i);
            });

        }

        private void selectSaison(int i) {
            if(mListener!=null){
                mListener.onItemClick(i);
            }
        }
    }
}
