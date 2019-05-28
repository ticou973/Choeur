package dedicace.com.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import dedicace.com.R;

//todo voir comment prendre en compte ses données pour l'utilisateur si il y a modifications de ces données (dans les préférences pour éviter les appels données ?)
public class ModifyUser extends AppCompatActivity implements UserAdapter.OnItemListener{

    private RecyclerView recyclerUsers;
    private FloatingActionButton fab;
    private UserAdapter userAdapter;
    private List<String> listUsers = new ArrayList<>();
    private List<String> listId = new ArrayList<>();
    private List<String> listEmails = new ArrayList<>();
    private List<String> listIdChorales = new ArrayList<>();
    private List<String> listNoms = new ArrayList<>();
    private List<String> listPrenoms = new ArrayList<>();
    private List<String> listRoles = new ArrayList<>();
    private List<String> listPupitres = new ArrayList<>();

    private RecyclerView.LayoutManager layoutManager;
    private static final String TAG ="coucou";
    private FirebaseFirestore db;
    private String role, pupitre,idChorale,email,nom,prenom,idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_user);
        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        fab = findViewById(R.id.fab_user);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateUserActivity = new Intent(ModifyUser.this,CreateUser.class);
                startActivity(startCreateUserActivity);
            }
        });

        db=FirebaseFirestore.getInstance();

        getListUsers();
    }

    private void getListUsers() {
        try {
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, "MU onComplete: Users " + Thread.currentThread().getName());
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "MU-exec deb Oncomplete " + document.getId() + " => " + document.getData().get("nom"));

                                    role =(String) document.get("role");
                                    pupitre =(String)document.get("pupitre");
                                    idChorale=(String) document.get("id_chorale");
                                    //todo voir à quoi pourront servir ces éléments (peut être dans le préférence compte)
                                    email = (String) document.get("email");
                                    nom=(String) document.get("nom");
                                    prenom=(String) document.get("prenom");

                                    idUser= document.getId();
                                    listId.add(idUser);
                                    Log.d(TAG, "MU-exec onComplete: users " + role + " " + prenom + " " + nom + " " + idChorale + " " + pupitre+" "+email);
                                    listUsers.add(nom+" "+prenom);
                                    listEmails.add(email);
                                    listIdChorales.add(idChorale);
                                    listNoms.add(nom);
                                    listPrenoms.add(prenom);
                                    listPupitres.add(pupitre);
                                    listRoles.add(role);
                                }

                                recyclerUsers = findViewById(R.id.recyclerview_cloud_user);
                                userAdapter = new UserAdapter(listUsers,listId);
                                layoutManager = new LinearLayoutManager(ModifyUser.this);
                                recyclerUsers.setLayoutManager(layoutManager);
                                recyclerUsers.setHasFixedSize(true);
                                recyclerUsers.setAdapter(userAdapter);

                                Log.d(TAG, "MU users: après");
                            } else {
                                Log.w(TAG, "MU-exec Error getting documents.", task.getException());
                            }
                        }
                    });

        } catch (Exception e) {
            // Server probably invalid
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int i) {
        Log.d(TAG, "MU onItemClick: "+i);
        Intent startDetailsUserActivity = new Intent(ModifyUser.this, ModifyUserDetails.class);
        Bundle args = new Bundle();
        args.putString("idUser", listId.get(i));
        args.putString("oldEmail",listEmails.get(i));
        args.putString("oldIdChorale", listIdChorales.get(i));
        args.putString("oldNom", listNoms.get(i));
        args.putString("oldPrenom",listPrenoms.get(i));
        args.putString("oldPupitre",listPupitres.get(i));
        args.putString("oldRole",listRoles.get(i));
        startDetailsUserActivity.putExtra("bundleUser", args);
        startActivity(startDetailsUserActivity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
