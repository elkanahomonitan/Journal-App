package com.journal.app;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.elkanahtech.widerpay.myutils.MyHandler;
import com.elkanahtech.widerpay.myutils.StringUtils;
import com.elkanahtech.widerpay.myutils.UIKits;
import com.elkanahtech.widerpay.myutils.listeners.ResponseListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.journal.app.adapter.EntryListAdapter;
import com.journal.app.data.MyContentProvider;
import com.journal.app.utils.JournalEntry;
import com.journal.app.data.MyFireBaseHelper;
import com.journal.app.data.MySharedPreference;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnCompleteListener<Void>, ResponseListener, ValueEventListener {

    private RecyclerView recyclerView;
    private EntryListAdapter adapter;
    private FloatingActionButton fab;
    private Dialog dialog;
    private MyFireBaseHelper helper;
    private MyHandler handler;
    private MySharedPreference pref;
    private List<JournalEntry> journalEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref = new MySharedPreference(this);
        if(pref.getLoggedInUser() == null)
            gotoLogin();
        helper = new MyFireBaseHelper(this);
        handler = new MyHandler(this, false);
        adapter = new EntryListAdapter(this, handler, helper);
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        loadFromLocalDB();
        fetchFromFB();
    }

    private void fetchFromFB() {
        helper.queryDB(getString(R.string.journal_entity), "userId", pref.getLoggedInUser(), this);
    }

    private void loadFromLocalDB() {
        journalEntries = new ArrayList();
        String selection = MyContentProvider.KEY_USER_ID + " = '" + pref.getLoggedInUser() + "'";
        Cursor cursor = getContentResolver().query(MyContentProvider.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()){
            JournalEntry entry = new JournalEntry();
            entry.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_TITLE)));
            entry.setDetail(cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_DETAIL)));
            entry.setRef(cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_REF)));
            entry.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_USER_ID)));
            journalEntries.add(entry);
        }
        adapter.swapEntries(journalEntries);
    }

    private void gotoLogin() {
        pref.setLoggedInUser(null);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        UIKits.Companion.promptLogout(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UIKits.Companion.promptLogout(this, this);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        addNewEntry();
    }

    private void addNewEntry() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.task_layout);
        final TextView txtTitle = dialog.findViewById(R.id.txtTitle);
        final TextView txtDetail = dialog.findViewById(R.id.txtDetail);
        Button btnContinue = dialog.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSubmit(txtTitle.getText().toString(), txtDetail.getText().toString());
            }
        });
        dialog.show();
    }

    private void attemptSubmit(String title, String detail) {
        if(TextUtils.isEmpty(title) || TextUtils.isEmpty(detail)){
            Toast.makeText(this, getString(R.string.required_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        JournalEntry entry = new JournalEntry();
        entry.setDetail(detail);
        entry.setTitle(title);
        entry.setRef(StringUtils.getTransactionRef());
        entry.setUserId(pref.getLoggedInUser());
        handler.sendEmptyMessage(0);
        saveEntry(entry);
    }

    private void saveEntry(JournalEntry entry) {
        helper.submitToDB(getString(R.string.journal_entity), entry.getRef(), entry, this);
        ContentValues values = new ContentValues();
        values.put(MyContentProvider.KEY_DETAIL, entry.getDetail());
        values.put(MyContentProvider.KEY_REF, entry.getRef());
        values.put(MyContentProvider.KEY_TITLE, entry.getTitle());
        values.put(MyContentProvider.KEY_USER_ID, entry.getUserId());
        getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
        journalEntries.add(entry);
        adapter.swapEntries(journalEntries);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if(task.isSuccessful()){
            handler.obtainMessage(1, "Added Successfully").sendToTarget();
            dialog.dismiss();
        }
        else
            handler.obtainMessage(1, task.getException().getMessage()).sendToTarget();

    }

    @Override
    public void responseReceived(boolean b) {
        if(b){
            gotoLogin();
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        journalEntries = new ArrayList();
        for (DataSnapshot data : dataSnapshot.getChildren()){
            journalEntries.add(data.getValue(JournalEntry.class));
        }
        saveToLocalDB(journalEntries);
        adapter.swapEntries(journalEntries);
    }

    private void saveToLocalDB(List<JournalEntry> journalEntries) {
        List<ContentValues> values = new ArrayList();
        for(JournalEntry entry : journalEntries){
            ContentValues value = new ContentValues();
            value.put(MyContentProvider.KEY_DETAIL, entry.getDetail());
            value.put(MyContentProvider.KEY_REF, entry.getRef());
            value.put(MyContentProvider.KEY_TITLE, entry.getTitle());
            value.put(MyContentProvider.KEY_USER_ID, entry.getUserId());
            values.add(value);
        }
        getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI, values.toArray(new ContentValues[]{}));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
