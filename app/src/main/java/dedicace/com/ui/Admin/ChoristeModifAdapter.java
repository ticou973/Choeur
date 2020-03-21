package dedicace.com.ui.Admin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Choriste;
import dedicace.com.ui.PlaySong.GlideApp;

class ChoristeModifAdapter extends RecyclerView.Adapter<ChoristeModifAdapter.ChoristeViewHolder> {
    private List<String[]> listResult;
    private Context mContext;
    private static final String TAG = "coucou";
    private File[] listFiles;
    private String[] listImages;
    private String position;
    private ArrayList listUrlPhoto;
    private List<Choriste> listChoristes;

    private String urlPhoto;

    private clickedListener mClickedListener;

    public ChoristeModifAdapter(Context mContext, List<Choriste> listChoristes) {
        this.mContext = mContext;
        this.listChoristes = listChoristes;
    }

    interface clickedListener {
        void onItemClick(int i);
    }

    @NonNull
    @Override
    public ChoristeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_choristes_modify, viewGroup, false);

        return new ChoristeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoristeViewHolder choristeViewHolder, int i) {

        choristeViewHolder.nom.setText(listChoristes.get(i).getNom());
        choristeViewHolder.prenom.setText(listChoristes.get(i).getPrenom());
        choristeViewHolder.pupitre.setText(listChoristes.get(i).getPupitre().toString());
        choristeViewHolder.roleChoeur.setText(listChoristes.get(i).getRoleChoeur());
        choristeViewHolder.roleAdmin.setText(listChoristes.get(i).getRoleAdmin());
        choristeViewHolder.email.setText(listChoristes.get(i).getEmail());
        choristeViewHolder.telPort.setText(listChoristes.get(i).getPortTel());
        choristeViewHolder.telFixe.setText(listChoristes.get(i).getFixTel());
        choristeViewHolder.adresse.setText(listChoristes.get(i).getAdresse());

        GlideApp.with(mContext)
                .load(listChoristes.get(i).getUrlLocalPhoto())
                .centerCrop() // scale to fill the ImageView and crop any extra
                .placeholder(R.drawable.avatar)
                .into(choristeViewHolder.imgChoriste);
    }

    @Override
    public int getItemCount() {
        return listResult.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        Context context = recyclerView.getContext();

        if (context instanceof clickedListener) {
            mClickedListener = (clickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mClickedListener = null;
    }

    private void selectChoriste(int i) {

        if(mClickedListener!=null){
            mClickedListener.onItemClick(i);
        }
    }

    public class ChoristeViewHolder extends RecyclerView.ViewHolder {
        TextView nom, prenom, pupitre, roleChoeur, roleAdmin, telFixe, telPort, adresse, email;
        ImageView imgChoriste;
        CardView cv;

        public ChoristeViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.tv_listchoristes_nom);
            prenom = itemView.findViewById(R.id.tv_listchoristes_prenom);
            pupitre = itemView.findViewById(R.id.tv_listchoristes_pupitre);
            roleChoeur = itemView.findViewById(R.id.tv_listchoristes_role_choeur);
            roleAdmin = itemView.findViewById(R.id.tv_listchoristes_role_admin);
            telFixe = itemView.findViewById(R.id.tv_listchoristes_tel_fixe);
            telPort = itemView.findViewById(R.id.tv_listchoristes_tel_port);
            adresse = itemView.findViewById(R.id.tv_listchoristes_adresse);
            email = itemView.findViewById(R.id.tv_listchoristes_email);
            imgChoriste = itemView.findViewById(R.id.img_choriste_list_choristes);
            cv = itemView.findViewById(R.id.cv_listchoristesmoddify);

            cv.setOnClickListener(view -> {
                Log.d(TAG, "CMA onClick: "+getAdapterPosition());
                int i = getAdapterPosition();
                selectChoriste(i);
            });
        }
    }
}
