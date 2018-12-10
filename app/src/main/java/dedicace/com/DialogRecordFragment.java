package dedicace.com;


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

public class DialogRecordFragment extends DialogFragment {

    private RadioButton tuttiRbtn,bassRbtn,tenoreRbtn,altoRbtn,sopranoRbtn;
    private RadioGroup radioGroup;
    private Pupitre pupitre;

    private int position;
    public DialogRecordFragment() {
        // Required empty public constructor
    }


    public interface DialogRecordFragmentListener {
        void onDialogPositiveClick(DialogFragment dialog,Pupitre pupitre);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    private DialogRecordFragmentListener mListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (DialogRecordFragmentListener) context;
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

        View view = inflater.inflate(R.layout.fragment_dialog_record, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        tuttiRbtn = view.findViewById(R.id.tutti_rbtn);
        bassRbtn = view.findViewById(R.id.bass_rbtn);
        tenoreRbtn = view.findViewById(R.id.tenor_rbtn);
        altoRbtn = view.findViewById(R.id.alto_rbtn);
        sopranoRbtn = view.findViewById(R.id.soprano_rbtn);
        radioGroup =view.findViewById(R.id.rg_pupitres);

        tuttiRbtn.setChecked(true);
        pupitre=Pupitre.TUTTI;

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                Log.d(SongsAdapter.TAG, "DRF onCheckedChanged:A "+pupitre+" "+position);

                switch (i){
                    case R.id.tutti_rbtn:
                        pupitre=Pupitre.TUTTI;
                        break;
                    case R.id.bass_rbtn:
                        pupitre=Pupitre.BASS;
                        break;
                    case R.id.tenor_rbtn:
                        pupitre=Pupitre.TENOR;
                        break;
                    case R.id.alto_rbtn:
                        pupitre=Pupitre.ALTO;
                        break;
                    case R.id.soprano_rbtn:
                        pupitre=Pupitre.SOPRANO;
                        break;
                }
            }
        });

        Log.d(SongsAdapter.TAG, "DRF onCreateDialog: "+pupitre);

        builder.setMessage("Veuillez choisir un pupitre pour l'enregistrement de votre morceau :");

        builder.setTitle("Enregistrement Live");

        builder.setIcon(R.drawable.logo_dedicace);


        builder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onDialogPositiveClick(DialogRecordFragment.this,pupitre);
            }
        })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(DialogRecordFragment.this);
                    }
                });

        return builder.create();
    }
}
