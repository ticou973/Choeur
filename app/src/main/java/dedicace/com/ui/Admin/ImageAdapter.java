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

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImagesViewHolder> {

    private List<String> listImages;
    private static final String TAG="coucou";
    private OnItemListener mListener;

    public ImageAdapter(List<String> listImages) {
        this.listImages = listImages;
        Log.d(TAG, "IA ImageAdapter: "+listImages);
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_background, viewGroup, false);

        ImagesViewHolder imagesViewHolder = new ImagesViewHolder(view);
        return imagesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder imagesViewHolder, int i) {

        imagesViewHolder.imageName.setText(listImages.get(i));

    }

    @Override
    public int getItemCount() {
        return listImages.size();
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

    private void selectBackground(int numItem){
        if(mListener!=null){
            Log.d(TAG, "IA selectBackground: "+numItem);
            mListener.onItemClick(numItem);
        }
    }

    public class ImagesViewHolder extends RecyclerView.ViewHolder {
        TextView imageName;
        CardView cv;
        public ImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            imageName = itemView.findViewById(R.id.imageName);
            cv=itemView.findViewById(R.id.cv_list_image);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "IA onClick: "+getAdapterPosition());
                    int i = getAdapterPosition();
                    selectBackground(i);
                }
            });
        }
    }
}
