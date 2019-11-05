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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<String> listUsers,listIds;
    private OnItemListener mListener;

    private static final String TAG="coucou";
    public UserAdapter(List<String> listUsers,List<String> listIds) {
        this.listUsers=listUsers;
        this.listIds=listIds;
    }
    public interface OnItemListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_user_cloud, viewGroup, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i) {

        userViewHolder.nom.setText(listUsers.get(i));
        userViewHolder.userId.setText(listIds.get(i));
    }

    @Override
    public int getItemCount() {
        return listUsers.size();
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

    private void selectUser(int i) {
        if(mListener!=null){
            mListener.onItemClick(i);
        }
    }


    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nom,userId;
        CardView cv;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.userName);
            userId =itemView.findViewById(R.id.userId);
            cv=itemView.findViewById(R.id.cv_list_users);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "MU onClick: "+getAdapterPosition());
                    int i = getAdapterPosition();
                    selectUser(i);
                }
            });
        }
    }


}
