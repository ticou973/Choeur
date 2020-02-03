package dedicace.com.ui.PlaySong;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DialogDownload extends DialogFragment {


    public DialogDownload() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Chargement des Données")
                .setMessage("Veuillez patienter quelques instants...");

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
