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
import dedicace.com.data.database.Spectacle;

class SpectacleAdapter extends RecyclerView.Adapter<SpectacleAdapter.SpectacleViewHolder>{

    private List<Spectacle> listSpectacles;
    private OnItemListener mListener;
    private static final String TAG="coucou";

    public SpectacleAdapter(List<Spectacle> listSpectacles) {
        this.listSpectacles = listSpectacles;
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public SpectacleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_spectacle_cloud,viewGroup,false);
        return new SpectacleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpectacleViewHolder spectacleViewHolder, int i) {
        spectacleViewHolder.nom.setText(listSpectacles.get(i).getSpectacleName());
    }

    @Override
    public int getItemCount() { return listSpectacles.size(); }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        Context context = recyclerView.getContext();

        if (context instanceof SpectacleAdapter.OnItemListener) {
            mListener = (SpectacleAdapter.OnItemListener) context;
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


    private void selectSpectacle(int i) {
        if(mListener!=null){
            mListener.onItemClick(i);
        }
    }

    public class SpectacleViewHolder extends RecyclerView.ViewHolder {
        TextView nom;
        CardView cv;
        public SpectacleViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.spectacleName);
            cv=itemView.findViewById(R.id.cv_list_spectacle);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "SpA onClick: "+getAdapterPosition());
                    int i = getAdapterPosition();
                    selectSpectacle(i);
                }
            });
        }
    }


}
