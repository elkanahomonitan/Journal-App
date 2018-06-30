package com.journal.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.elkanahtech.widerpay.myutils.MyHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.journal.app.data.MyFireBaseHelper;
import com.journal.app.data.MySharedPreference;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnContinue;
    private MyFireBaseHelper helper;
    private MyHandler handler;
    private MySharedPreference pref;
    private ProgressBar progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnContinue = findViewById(R.id.btnContinue);
        progress = findViewById(R.id.progress);
        btnContinue.setOnClickListener(this);
        helper = new MyFireBaseHelper(this);
        handler = new MyHandler(this, false);
        pref= new MySharedPreference(this);
    }

    @Override
    public void onClick(View v) {
        attemptLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            progress.setVisibility(View.GONE);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            pref.setLoggedInUser(account.getEmail());
            startActivity(new Intent(this, MainActivity.class));
            helper.signOut();
            finish();
        } catch (ApiException e ) {
            handler.obtainMessage(3, 405,1,"You need to update your Google pay services to use this feature.").sendToTarget();
        }
    }

    private void attemptLogin() {
        progress.setVisibility(View.VISIBLE);
        helper.googleLogin();
    }
}
