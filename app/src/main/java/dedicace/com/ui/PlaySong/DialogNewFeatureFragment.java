package dedicace.com.ui.PlaySong;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import dedicace.com.R;

public class DialogNewFeatureFragment extends DialogFragment {

    private DialogNewFeatureListener mListener;
    private ImageView menu,trombi,details;
    private Context context;
    private TextView prezDepart, prezDetails, prezTrombi;

    public DialogNewFeatureFragment() {
    }
    public interface DialogNewFeatureListener {
        void onDialogNewFeaturePositiveClick();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (DialogNewFeatureListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " doit implémenter BasicDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_new_feature, null);
        builder.setView(view);

        menu =view.findViewById(R.id.img_trombi_menu);
        trombi=view.findViewById(R.id.img_trombi);
        details =view.findViewById(R.id.img_trombi_details);
        prezDepart=view.findViewById(R.id.tv_depart);
        prezDetails=view.findViewById(R.id.tv_prez_details);
        prezTrombi=view.findViewById(R.id.tv_intermédiaire_trombi);

        GlideApp.with(context)
                .load(R.drawable.menu_trombi)
                //.centerCrop()
                .centerInside()// scale to fill the ImageView and crop any extra
                .into(menu);

        GlideApp.with(context)
                .load(R.drawable.trombifiltre)
               // .centerCrop() //
                .centerInside()// scale to fill the ImageView and crop any extra
                .into(trombi);

        GlideApp.with(context)
                .load(R.drawable.details_trombi)
               // .centerCrop()
                .centerInside()// scale to fill the ImageView and crop any extra
                .into(details);

        prezDepart.setText("Pour utiliser le trombinoscope, ouvrir le menu en haut à droite (Les 3 points verticaux).");
        prezDetails.setText("Pour obtenir les détails sur un choriste, appuyer sur sa fiche. \n Sa fiche détaillée permet d'avoir ses coordonnées ainsi que de l'appeler ou de lui écrire directement à partir de l'application.");
        prezTrombi.setText("Dans la fenêtre Trombinoscope, la liste des choristes est affichée. \n Pour filtrer par pupitre, appuyer sur les boutons correspondants en bas (Basse (B), Tenor (T), Alti (A) ou Soprano (S).");


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onDialogNewFeaturePositiveClick();
            }
        });

        return builder.create();
    }
}
