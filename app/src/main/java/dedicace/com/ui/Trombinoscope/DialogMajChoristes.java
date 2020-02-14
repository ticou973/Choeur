package dedicace.com.ui.Trombinoscope;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DialogMajChoristes extends DialogFragment {

    public DialogMajChoristes() {
    }

    public interface DialogMajChoristesListener {
        void onDialogPositiveClick();
        void onDialogNegativeClick();
    }

    private DialogMajChoristesListener mListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (DialogMajChoristesListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " doit implémenter BasicDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Des modifications et/ou de nouveaux choristes sont disponibles, voulez-vous les mettre à jour ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if(mListener!=null){
                            mListener.onDialogPositiveClick();
                        }
                    }
                })
                .setNegativeButton("Plus tard...", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mListener!=null){
                            mListener.onDialogNegativeClick();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
