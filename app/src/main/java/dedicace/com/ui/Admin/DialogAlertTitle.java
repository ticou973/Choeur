package dedicace.com.ui.Admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DialogAlertTitle extends DialogFragment {

    public DialogAlertTitle() {
    }

    public interface DialogAlertTitleListener {
        void onDialogPositiveClick();
        void onDialogNegativeClick();
    }

    private DialogAlertTitleListener mListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (DialogAlertTitleListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " doit implémenter BasicDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Attention, vous allez aussi changer le titre des songs ?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if(mListener!=null){
                            mListener.onDialogPositiveClick();
                        }
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
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
