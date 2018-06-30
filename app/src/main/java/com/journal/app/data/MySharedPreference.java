package com.journal.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.journal.app.R;

public class MySharedPreference {
    private Context context;
    public  MySharedPreference(Context context){
        this.context = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        editor  = pref.edit();
    }
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public String getLoggedInUser() {
        return pref.getString(context.getString(R.string.loggedInUser), null);
    }

    public void setLoggedInUser(String loggedInUser) {
        editor.putString(context.getString(R.string.loggedInUser), loggedInUser);
        editor.commit();
    }
}
