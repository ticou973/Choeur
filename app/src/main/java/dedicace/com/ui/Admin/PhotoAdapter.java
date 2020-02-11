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

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotosViewHolder> {
    private List<String> listImages;
    private static final String TAG="coucou";
    private OnItemListener mListener;

    public PhotoAdapter(List<String> listImages) {
        this.listImages = listImages;
        Log.d(TAG, "PA ImageAdapter: "+listImages);
    }

    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_photo, viewGroup, false);

        return new PhotosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder photosViewHolder, int i) {
        photosViewHolder.imageName.setText(listImages.get(i));
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

    private void selectPhoto(int numItem){
        if(mListener!=null){
            Log.d(TAG, "PA selectPhoto: "+numItem);
            mListener.onItemClick(numItem);
        }
    }


    class PhotosViewHolder extends RecyclerView.ViewHolder {
        TextView imageName;
        CardView cv;

        PhotosViewHolder(@NonNull View itemView) {
            super(itemView);
            imageName = itemView.findViewById(R.id.photoName);
            cv=itemView.findViewById(R.id.cv_list_photo);

            cv.setOnClickListener(view -> {
                Log.d(TAG, "PA onClick: "+getAdapterPosition());
                int i = getAdapterPosition();
                selectPhoto(i);
            });

        }
    }
}
