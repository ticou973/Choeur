package dedicace.com.ui.Admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dedicace.com.R;

class Mp3Adapter extends RecyclerView.Adapter<Mp3Adapter.Mp3ViewHolder> {

    private List<String> listMp3;
    private static final String TAG="coucou";
    private OnItemListener mListener;

    public Mp3Adapter(List<String> listImages) {
        this.listMp3 = listImages;
        Log.d(TAG, "Mp3A Mp3Adapter: constructor"+listImages);
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public Mp3ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_mp3, viewGroup, false);

        Mp3ViewHolder mp3ViewHolder = new Mp3ViewHolder(view);
        return mp3ViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Mp3ViewHolder mp3ViewHolder, int i) {

        mp3ViewHolder.mp3Name.setText(listMp3.get(i));
    }

    @Override
    public int getItemCount() {
        return listMp3.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        Context context = recyclerView.getContext();

        if (context instanceof Mp3Adapter.OnItemListener) {
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

    private void selectMp3(int numItem) {
        if(mListener!=null){
            Log.d(TAG, "mp3A selectBackground: "+numItem);
            mListener.onItemClick(numItem);
        }
    }


    public class Mp3ViewHolder extends RecyclerView.ViewHolder {
        TextView mp3Name;
        public Mp3ViewHolder(@NonNull View itemView) {
            super(itemView);
            mp3Name =itemView.findViewById(R.id.mp3Name);

            mp3Name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "mp3A onClick: "+getAdapterPosition());
                    int i = getAdapterPosition();
                    selectMp3(i);
                }
            });
        }
    }


}
