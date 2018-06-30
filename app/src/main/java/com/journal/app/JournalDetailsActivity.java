package com.journal.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.journal.app.utils.JournalEntry;

public class JournalDetailsActivity extends AppCompatActivity {

    private JournalEntry entry;
    private TextView txtTitle, txtDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        entry = (JournalEntry)getIntent().getSerializableExtra(getString(R.string.data));
        txtTitle = findViewById(R.id.txtTitle);
        txtDetail = findViewById(R.id.txtDetail);
        txtDetail.setText(entry.getDetail());
        txtTitle.setText(entry.getTitle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
