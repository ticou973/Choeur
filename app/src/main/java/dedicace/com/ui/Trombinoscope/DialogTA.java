package dedicace.com.ui.Trombinoscope;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class DialogTA extends DialogFragment {

    private String origine;
    private AlertDialog.Builder builder;

    public DialogTA() {
    }

    public interface DialogTAListener {
        void onDialogTAPositiveClick();
        void onDialogTANegativeClick();
    }

    private DialogTAListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (DialogTAListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " doit implémenter BasicDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        origine = args.getString("origine","");
        builder = new AlertDialog.Builder(getActivity());

        if(origine.equals("waitChoristes")){
            Log.d("coucou", "DTA onCreateDialog: waitChoristes");
            builder.setMessage("Veuillez attendre la mise à jour des données... Lors de la première installation, cela peut prendre plusieurs minutes ! (temps de charge des chansons de votre pupitre)");
        }

        return builder.create();
    }
}
