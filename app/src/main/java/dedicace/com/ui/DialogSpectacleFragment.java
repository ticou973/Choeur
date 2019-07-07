package dedicace.com.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import dedicace.com.R;

public class DialogSpectacleFragment extends DialogFragment {
    private RadioButton tousRbtn,autresRbtn;
    private RadioGroup radioGroup;
    private String spectacle;


    public DialogSpectacleFragment() {
        // Required empty public constructor
    }


    public interface DialogSpectacleFragmentListener {
        void onDialogSpectaclePositiveClick(String spectacle);
        void onDialogSpectacleNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    private DialogSpectacleFragmentListener mListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (DialogSpectacleFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " doit implémenter BasicDialogListener");
        }
    }

    //todo V1a voir nouveau framework pour boite dialogue
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_dialog_spectacle, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        tousRbtn = view.findViewById(R.id.tous_rbtn);
        autresRbtn = view.findViewById(R.id.autres_rbtn);
        radioGroup =view.findViewById(R.id.rg_spectacles);

        tousRbtn.setChecked(true);
        spectacle="Tous";

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                Log.d(SongsAdapter.TAG, "DSF onCheckedChanged:A "+spectacle);

                switch (i){
                    case R.id.tous_rbtn:
                        spectacle="Tous";
                        break;
                    case R.id.autres_rbtn:
                        spectacle="Autres";
                        break;
                }
            }
        });

        Log.d(SongsAdapter.TAG, "DSF onCreateDialog: "+spectacle);

        builder.setMessage("Veuillez choisir un spectacle :");

        builder.setTitle("Choix des chansons");

        builder.setIcon(R.drawable.logo_dedicace);


        builder.setPositiveButton("Choisir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onDialogSpectaclePositiveClick(spectacle);
            }
        })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogSpectacleNegativeClick();
                    }
                });

        return builder.create();
    }












}
