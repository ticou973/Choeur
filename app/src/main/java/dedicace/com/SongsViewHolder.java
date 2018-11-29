package dedicace.com;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class SongsViewHolder extends RecyclerView.ViewHolder {

    private TextView titre, groupe;




    public SongsViewHolder(@NonNull View itemView) {
        super(itemView);

        titre = itemView.findViewById(R.id.tv_titre);
        groupe = itemView.findViewById(R.id.tv_groupe);

    }

    public void setTitre(String titre){

        this.titre.setText(titre);
    }

    public void setGroupe(String groupe){

        this.groupe.setText(groupe);
    }
}
