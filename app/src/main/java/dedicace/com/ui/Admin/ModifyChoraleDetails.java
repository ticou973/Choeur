package dedicace.com.ui.Admin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedicace.com.R;

public class ModifyChoraleDetails extends AppCompatActivity implements DialogSuppFragment.DialogSuppListener{
    private TextView oldName,oldLogo, newLogo;
    private EditText newName;
    private Button suppChorale, modifyChorale, selectLogo;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private static final String TAG ="coucou";
    private List<String> listIds = new ArrayList<>();
    private String idChorale,oldNameStr, newNameStr, newLogoStr;
    private Map<String,Object> chorale;
    private Uri downloadUrl;
    private Uri fileSelected;
    private String[] listImages;
    private static List<String> listFilesImage = new ArrayList<>();
    private List<String> listPath = new ArrayList<>();
    private String pathSelected;
    private String fileNameSelected;
    private File[] listFiles;
    private final static int REQUEST_CODE=100;
    private int imageSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_chorale_details);

        oldName=findViewById(R.id.tv_chorale_name_old);
        oldLogo=findViewById(R.id.tv_logo_old);
        newLogo=findViewById(R.id.tv_logo_new);
        newName =findViewById(R.id.et_name_chorale_new);
        suppChorale=findViewById(R.id.btn_supp_chorale);
        modifyChorale=findViewById(R.id.btn_modify_chorale);
        selectLogo=findViewById(R.id.btn_select_new_logo);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        getIntentBundle();

        completeOld();

        suppChorale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new DialogSuppFragment();
                dialogFragment.show(getSupportFragmentManager(),TAG);
            }
        });

        modifyChorale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //obligatoire
                newNameStr = newName.getText().toString();
                newLogoStr = newLogo.getText().toString();

                if(!newNameStr.equals("")||!newLogoStr.equals("Select. Logo")){
                    Log.d(TAG, "MCCD onClick: conditions passées "+ newNameStr+ " "+newLogoStr);
                    insertLogoInCloudStorage();

                }else{
                    Toast.makeText(ModifyChoraleDetails.this, "Il manque des éléments pour insérer", Toast.LENGTH_SHORT).show();
                }

            }
        });

        selectLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLists();
                selectLogo();

            }
        });

    }

    private void selectLogo() {
        Log.d(TAG, "MCCD selectLogo: ");

        Intent startChooseBackgroundActivity = new Intent(ModifyChoraleDetails.this,ChooseBackground.class);
        startChooseBackgroundActivity.putExtra("listimages",listImages);
        startActivityForResult(startChooseBackgroundActivity,REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK){
            Log.d(TAG, "MCCD onActivityResult: ok cela marche");
            if(requestCode==REQUEST_CODE){

                if (data != null) {
                    imageSelected = data.getIntExtra("imageselected",-1);
                }

                if(imageSelected!=-1) {
                    String name = listImages[imageSelected];
                    newLogo.setText(name);
                    pathSelected=listFiles[imageSelected].getAbsolutePath();
                    fileNameSelected = name;
                    Log.d(TAG, "MCCD onCreate: " + pathSelected);
                }
            }
        }else{
            Log.d(TAG, "MCCD onActivityResult: petit problème au retour ");
        }
    }

    private void getLists() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path,"DedicaceAdmin/Image_Background_Chorale");

        if(file.mkdirs()){
            Log.d(TAG, "MSSD insertBackgroundInCloudStorage: le dossier est fait");

        }else{
            Log.d(TAG, "MSSD insertBackgroundInCloudStorage: dossier non réalisé ou déjà fait");
        }

        if(file.exists()){
            listFiles = file.listFiles();

            for (File image:listFiles) {
                Log.d(TAG, "MSSD selectBackground: "+image.getName());
                listFilesImage.add(image.getName());
                listPath.add(image.getAbsolutePath());
            }

            Log.d(TAG, "MSSD selectBackground: "+listFilesImage.size()+" "+listFiles.length);

            listImages = new String[listFiles.length];

            for (int i = 0; i < listFiles.length; i++) {
                listImages[i]=listFiles[i].getName();

                Log.d(TAG, "MSSD selectBackground: "+listFiles[i].getName()+listImages[i]);
            }
        }

        Log.d(TAG, "MSSD : selectBackground: "+ listImages.length);


    }

    private void insertLogoInCloudStorage() {
        chorale = new HashMap<>();
        if(!newNameStr.equals("")){
           chorale.put("nom",newNameStr);
        }
        Log.d(TAG, "MCCD insertChoraleinDb: ");

        if(!newLogoStr.equals("Select. Logo")){
            Log.d(TAG, "MCCD insertChoraleinDb: if logo");

            fileSelected = Uri.fromFile(new File(pathSelected));
            StorageReference imageRef = mStorageRef.child("chorales/logo_titre/"+fileNameSelected);

            UploadTask uploadTask = imageRef.putFile(fileSelected);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "MCCD onSuccess: bravo c'est uploadé !");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Log.d(TAG, "MCCD onFailure: dommage c'est raté");
                        }
                    });


            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadUrl = task.getResult();
                        chorale.put("logo",downloadUrl.toString());
                        insertChoraleinDb();
                        Log.d(TAG, "MCCD onComplete: "+downloadUrl);
                    } else {
                        // Handle failures
                        // ...
                        Log.d(TAG, "MCCD onComplete: Il y a eu un pb");
                    }
                }
            });

        }else{
            //todo mettre insert plus bas et enlever les 2 du if else
            Log.d(TAG, "MCCD insertLogoInCloudStorage: else if logo");
            insertChoraleinDb();
        }
    }

    private void insertChoraleinDb() {

        Log.d(TAG, "MCCD insertSSinDb: else titre");
        insertCloud();
    }

    private void insertCloud() {
        Log.d(TAG, "MCCD insertCloud: "+chorale);
        db.collection("chorale").document(idChorale)
                .update(chorale)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "MCCD onSuccess: maj chorale done");
                        if(pathSelected!=null) {
                            File file = new File(pathSelected);
                            if (file.delete()) {
                                Log.d(TAG, "MCCD onSuccess: le fichier est supprimé du local");
                            } else {
                                Log.d(TAG, "MCCD onSuccess: problème de suppression en local du fichier");
                            }
                        }else{
                            Log.d(TAG, "MCCD onSuccess: il n'y a pas de fichier à supprimer car pas de modif de background");
                        }

                        Intent startMC = new Intent(ModifyChoraleDetails.this,ModifyChorale.class);
                        startMC.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startMC.putExtra("origine","AdminHome");
                        startActivity(startMC);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MSSD onSuccess: maj sourceSong failed");
                    }
                });


    }

    private void completeOld() {
        oldName.setText(oldNameStr);
        oldLogo.setText(" ");

    }

    private void getIntentBundle() {
        Intent intent = getIntent();
        Bundle args;
        args = intent.getBundleExtra("bundleChorale");
        idChorale=args.getString("idChorale");
        oldNameStr=args.getString("oldName");

        Log.d(TAG, "MCD getIntentBundle: "+idChorale+" "+oldNameStr);

    }

    @Override
    public void onDialogSuppPositiveClick() {
        getSuppUsers();
        suppChorales();
        Intent startModifyChoraleActivity = new Intent(ModifyChoraleDetails.this,ModifyChorale.class);
        startModifyChoraleActivity.putExtra("origine","AdminHome");
        startModifyChoraleActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(startModifyChoraleActivity);

    }

    //todo voir plus tard pour supprimer réellement les users de la base et âs que le lien à la chorale
    private void suppChorales() {
        db.collection("chorale").document(idChorale)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "MCCD onSuccess: réussi choral supprimée");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "MCCD onFailure: chorale pas supprimé");
                    }
                });
    }

    private void deleteUsersLink() {
        Log.d(TAG, "MCCD DeleteChoraleLinks: ");
        Map<String,Object>args = new HashMap<>();
        args.put("id_chorale", " ");

        for(String name:listIds){
            db.collection("users").document(name)
                    .update(args)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "MCCD onSuccess: Ok pour deletechoraleLinks ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "MCCD onFailure: raté delete chorales Links ");
                        }
                    });
        }
    }

    private void getSuppUsers() {
        CollectionReference userRef=db.collection("users");
        Query query = userRef.whereEqualTo("id_chorale",idChorale);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> documents =  task.getResult().getDocuments();

                    for(DocumentSnapshot document:documents){
                        listIds.add(document.getId());
                        Log.d(TAG, "MCCD onEvent: "+document.getId());
                    }
                    deleteUsersLink();

                    Log.d(TAG, "MCCD onDialogPositiveClick: "+query);

                }else{
                    Log.d(TAG, "MCCD onComplete: pb dans la query");
                }
            }
        });

    }

    @Override
    public void onDialogSuppNegativeClick() {
        Toast.makeText(this, "Vous avez souhaitez ne pas supprimer cette Chorale", Toast.LENGTH_SHORT).show();
        finish();

    }
}
