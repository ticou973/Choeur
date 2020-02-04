package dedicace.com.ui.Trombinoscope;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Choriste;
import dedicace.com.ui.PlaySong.GlideApp;


class TrombiAdapter extends RecyclerView.Adapter<TrombiAdapter.TrombiViewHolder> {

    private List<Choriste> choristes;
    private Context mContext;
    private ListItemClickListener listItemClickListener;


    public TrombiAdapter(Context context,ListItemClickListener handler) {

        this.mContext =context;
        this.listItemClickListener=handler;
    }

    public interface ListItemClickListener {
        void OnClickItem();
    }

    @NonNull
    @Override
    public TrombiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);

        view = mInflater.inflate(R.layout.list_item_chorist,parent,false);
        return new TrombiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrombiViewHolder trombiViewHolder, int i) {

        trombiViewHolder.tvPupitre.setText(choristes.get(i).getPupitre().toString());
        trombiViewHolder.tvNom.setText(choristes.get(i).getNom());

        GlideApp.with(mContext)
                .load(choristes.get(i).getUrlLocalPhoto())
                .centerCrop() // scale to fill the ImageView and crop any extra
                .into(trombiViewHolder.imgChoriste);

        trombiViewHolder.cv.setOnClickListener(view -> {
            Intent intent = new Intent(mContext,TrombiDetailsActivity.class);
            intent.putExtra("nom", choristes.get(i).getNom());
            intent.putExtra("prenom", choristes.get(i).getPrenom());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return choristes.size();
    }

    public class TrombiViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvPupitre;
        ImageView imgChoriste;
        CardView cv;

        public TrombiViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNom = itemView.findViewById(R.id.tv_choriste_nom);
            tvPupitre = itemView.findViewById(R.id.tv_choriste_pupitre);
            imgChoriste = itemView.findViewById(R.id.img_choriste);
            cv = itemView.findViewById(R.id.cardview_choriste);


        }
    }
}

