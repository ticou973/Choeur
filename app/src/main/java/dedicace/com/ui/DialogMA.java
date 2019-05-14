package dedicace.com.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import dedicace.com.data.database.Song;

public class DialogMA extends DialogFragment {
    private String origine;
    private Song songToDownload;
    private String messageIntro;
    private String messagePositif;
    private String messageNegatif;
    private int position;

    public DialogMA() {

    }

    public interface DialogMAListener {
        void onDialogMAPositiveClick(int position, Song song);
        void onDialogMANegativeClick();
    }

    private DialogMAListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (DialogMAListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " doit implémenter BasicDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        origine = args.getString("origine","");
        position =args.getInt("position",0);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(origine.equals("downloadSingle")) {
            messageIntro = "Voulez-vous charger sur votre téléphone cette chanson ?";
            messagePositif = "Oui";
            messageNegatif = "Non";
        }else{
            Log.d("coucou", "DMA onCreateDialog: pas de messages...");
        }
        builder.setMessage(messageIntro)
                .setPositiveButton(messagePositif, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if(mListener!=null){
                            Log.d("coucou", "DMA onClick: (position) "+position);
                            mListener.onDialogMAPositiveClick(position,songToDownload);
                        }
                    }
                })
                .setNegativeButton(messageNegatif, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mListener!=null){
                            mListener.onDialogMANegativeClick();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setSong(Song song){
        songToDownload = song;
        Log.d("coucou", "DMA setSong: "+songToDownload);
    }
}
