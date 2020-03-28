package dedicace.com.ui.PlaySong;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import dedicace.com.R;

public class DialogInitialDownloadFragment extends DialogFragment {

    public DialogInitialDownloadFragment() {
    }

    public interface DialogInitialDownloadListener{
        void onDialogInitialDownloadPositiveClick();
        void onDialogInitialDownloadNegativeClick();
    }

    private DialogInitialDownloadListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (DialogInitialDownloadListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " doit implémenter BasicDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //todo faire avec une vue spéciale pour être plus propre.
        // Get the layout inflater
       // LayoutInflater inflater = getActivity().getLayoutInflater();

       // View view = inflater.inflate(R.layout.fragment_dialog_spectacle, null);

      //  builder.setView(view);
        builder.setTitle("Chargement des chansons");
        builder.setIcon(R.drawable.logo_dedicace);
        builder.setMessage("Voulez-vous charger toutes les chansons de votre pupitre dès maintenant ?. Attention à la première installation, il est préférable d'être en Wifi et avoir suffisamment de place sur son téléphone pour installer les chansons (200 MO minimum");

        builder.setPositiveButton("Charger les Chansons", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onDialogInitialDownloadPositiveClick();
            }
        })
                .setNegativeButton("Charger plus tard les chansons", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogInitialDownloadNegativeClick();
                    }
                });

        return builder.create();
    }
}
