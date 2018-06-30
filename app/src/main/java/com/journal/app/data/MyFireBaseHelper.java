package com.journal.app.data;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journal.app.R;

public class MyFireBaseHelper {
    public  MyFireBaseHelper(Activity context){
        this.context = context;
        database = FirebaseDatabase.getInstance().getReference(context.getString(R.string.app_name));
    }
    private Activity context;
    private DatabaseReference database ;
    private int RC_SIGN_IN = 100;
    private GoogleSignInClient mGoogleSignInClient;
    public void googleLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        context.startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);

    }

    public void signOut() {
        mGoogleSignInClient.signOut();
    }
    public void queryDB(String table_title, String query, String value,ValueEventListener listener) {
        database.child(table_title).orderByChild(query).equalTo(value).addValueEventListener(listener);
    }
    public void submitToDB(String table_title, String ref,Object T,OnCompleteListener<Void> listener){
        database.child(table_title).child(ref).setValue(T).addOnCompleteListener(listener);
    }
    public void deleteDB(String table_title, String ref, DatabaseReference.CompletionListener listener) {
        database.child(table_title).child(ref).removeValue(listener);
    }
}
