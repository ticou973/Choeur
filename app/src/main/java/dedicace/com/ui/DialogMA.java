package dedicace.com.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;
import dedicace.com.data.database.Song;

public class DialogMA extends DialogFragment  {
    private String origine;
    private Song song, songToDelete;
    private String messageIntro;
    private String messagePositif;
    private String messageNegatif;
    private int position,nbSong,nbSongTotal;
    private List<Integer> selectedItems= new ArrayList<>();

    public DialogMA() {

    }

    public interface DialogMAListener {
        void onDialogMAPositiveClick(int position, Song song);
        void onDialogMANegativeClick();
        void onDialogMADeletePositiveClick(int position, Song song);
        void onDialogMADeleteNegativeClick();
        void onDialogMADownloadPupitresPositiveClick(List<Integer> selectedItems);
        void onDialogMADownloadPupitresNegativeClick();
        void onDialogMADeletePupitresPositiveClick(List<Integer> selectedItems);
        void onDialogMADeletePupitresNegativeClick();
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
        nbSong = args.getInt("nombreSongs",0);
        nbSongTotal = args.getInt("nombreTotalSongs",0);


        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(origine.equals("downloadSingle")) {
            Log.d("coucou", "DMA onCreateDialog:  download single "+position);
            messageIntro = "Voulez-vous charger cette chanson sur votre téléphone ?";
            messagePositif = "Oui";
            messageNegatif = "Non";
            builder.setMessage(messageIntro);
        }else if(origine.equals("deleteSingle")) {
            messageIntro = "Voulez-vous effacer cette chanson sur votre téléphone?";
            messagePositif = "Oui";
            messageNegatif = "Non";
            builder.setMessage(messageIntro);
        }else if(origine.equals("downloadPupitres")){
            builder.setTitle("Pupitres à télécharger")
                    .setMultiChoiceItems(R.array.pupitre_download_delete, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                    if (isChecked) {
                        Log.d("coucou", "DMA onClick if" +
                                ": "+which+isChecked);
                        // If the user checked the item, add it to the selected items
                        selectedItems.add(which);
                    } else if (selectedItems.contains(which)) {
                        Log.d("coucou", "DMA onClick else: "+which+isChecked);
                        // Else, if the item is already in the array, remove it
                        selectedItems.remove(Integer.valueOf(which));
                    }

                    if(selectedItems.size()!=0&&selectedItems!=null) {
                        Log.d("coucou", "DMA onCreateDialog: " + selectedItems);
                    }else {
                        Log.d("coucou", "DMA onCreateDialog: pb selectedItems ");
                    }
                }
            });
            messagePositif = "OK";
            messageNegatif = "Annuler";
            if(selectedItems.size()!=0&&selectedItems!=null) {
                Log.d("coucou", "DMA onCreateDialog: " + selectedItems.size());
            }else {
                Log.d("coucou", "DMA onCreateDialog: pb selectedItems ");
            }

        }else if(origine.equals("deletePupitres")){
            builder.setTitle("Pupitres à supprimer")
                    .setMultiChoiceItems(R.array.pupitre_download_delete, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                    if (isChecked) {
                        // If the user checked the item, add it to the selected items
                        selectedItems.add(which);
                    } else if (selectedItems.contains(which)) {
                        // Else, if the item is already in the array, remove it
                        selectedItems.remove(Integer.valueOf(which));
                    }
                }
            });

            messagePositif = "Ok";
            messageNegatif = "Annuler";

        }else if(origine.equals("waitSongs")){
            Log.d("coucou", "DMA onCreateDialog: waitsongs");
            builder.setMessage("Veuillez attendre la mise à jour des données... Lors de la première installation, cela peut prendre plusieurs minutes ! (temps de charge des chansons de votre pupitre)");

        }else if(origine.equals("progress")){
            Log.d("coucou", "DMA onCreateDialog: progress "+nbSong+ " "+nbSongTotal);

            builder.setMessage("Veuillez attendre la mise à jour des données..."+nbSong+"/"+nbSongTotal+" chansons chargées sur votre téléphone !");

        } else{
            Log.d("coucou", "DMA onCreateDialog: pas de messages...");
        }

        if(!origine.equals("waitSongs")) {
            builder.setPositiveButton(messagePositif, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (mListener != null) {
                        Log.d("coucou", "DMA onClick: (position) " + position);

                        if (origine.equals("downloadSingle")) {
                            mListener.onDialogMAPositiveClick(position, song);
                        } else if (origine.equals("deleteSingle")) {
                            mListener.onDialogMADeletePositiveClick(position, song);
                        } else if (origine.equals("downloadPupitres")) {
                            mListener.onDialogMADownloadPupitresPositiveClick(selectedItems);
                        } else if (origine.equals("deletePupitres")) {
                            mListener.onDialogMADeletePupitresPositiveClick(selectedItems);
                        }
                    }
                }
            })
                    .setNegativeButton(messageNegatif, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (mListener != null) {

                                if (origine.equals("downloadSingle")) {
                                    mListener.onDialogMANegativeClick();
                                } else if (origine.equals("deleteSingle")) {
                                    mListener.onDialogMADeleteNegativeClick();
                                } else if (origine.equals("downloadPupitres")) {

                                    mListener.onDialogMADownloadPupitresNegativeClick();

                                } else if (origine.equals("deletePupitres")) {

                                    mListener.onDialogMADeletePupitresNegativeClick();
                                }
                            }
                        }
                    });
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setSong(Song song){
        this.song = song;
        Log.d("coucou", "DMA setSong: "+song);
    }



}
