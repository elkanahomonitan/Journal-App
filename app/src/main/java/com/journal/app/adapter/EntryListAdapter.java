package com.journal.app.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.elkanahtech.widerpay.myutils.MyHandler;
import com.elkanahtech.widerpay.myutils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.journal.app.JournalDetailsActivity;
import com.journal.app.data.MyContentProvider;
import com.journal.app.data.MyFireBaseHelper;
import com.journal.app.utils.JournalEntry;
import com.journal.app.R;

import java.util.List;

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.MyViewHolder> {
    public List<JournalEntry>journalEntryList;
    public Activity context;
    private MyFireBaseHelper helper;
    private MyHandler handler;

    public  EntryListAdapter(Activity context, MyHandler handler, MyFireBaseHelper helper){
        this.context = context;
        this.handler = handler;
        this.helper = helper;
    }
    public void swapEntries(List<JournalEntry> journalEntries){
        this.journalEntryList = journalEntries;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        JournalEntry journalEntry = journalEntryList.get(position);
        holder.txtDetail.setText(journalEntry.getDetail());
        holder.txtTitle.setText(journalEntry.getTitle());
    }

    @Override
    public int getItemCount() {
        return journalEntryList == null ? 0 : journalEntryList.size();
    }

    public  class MyViewHolder extends RecyclerView.ViewHolder implements DatabaseReference.CompletionListener, OnCompleteListener<Void>, PopupMenu.OnMenuItemClickListener, View.OnClickListener {
        private TextView txtTitle, txtDetail, optionMenu;
        private Dialog dialog;
        public MyViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDetail = itemView.findViewById(R.id.txtDetail);
            optionMenu = itemView.findViewById(R.id.optionMenu);
            optionMenu.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewEntry(journalEntryList.get(getPosition()));
                }
            });
        }

        private void editEntry(final JournalEntry journalEntry, final int position) {
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.task_layout);
            final TextView txtTitle = dialog.findViewById(R.id.txtTitle);
            final TextView txtDetail = dialog.findViewById(R.id.txtDetail);
            txtTitle.setText(journalEntry.getTitle());
            txtDetail.setText(journalEntry.getDetail());
            Button btnContinue = dialog.findViewById(R.id.btnContinue);
            btnContinue.setText("Update Entry");
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptUpdate(txtTitle.getText().toString(), txtDetail.getText().toString(), journalEntry, position);
                }
            });
            dialog.show();
        }

        private void attemptUpdate(String title, String detail, JournalEntry journalEntry, int position) {
            if(TextUtils.isEmpty(title) || TextUtils.isEmpty(detail)){
                Toast.makeText(context, context.getString(R.string.required_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            journalEntry.setDetail(detail);
            journalEntry.setTitle(title);
            journalEntryList.set(position, journalEntry);
            notifyDataSetChanged();
            updateDB(journalEntry);
        }

        private void updateDB(JournalEntry journalEntry) {
            handler.sendEmptyMessage(0);
            String where = MyContentProvider.KEY_REF + " = '" + journalEntry.getRef() + "'";
            ContentValues values = new ContentValues();
            values.put(MyContentProvider.KEY_TITLE, journalEntry.getTitle());
            values.put(MyContentProvider.KEY_DETAIL, journalEntry.getDetail());
            context.getContentResolver().update(MyContentProvider.CONTENT_URI, values, where, null);
            helper.submitToDB(context.getString(R.string.journal_entity), journalEntry.getRef(), journalEntry, this);
        }

        private void deleteEntry(int position) {
            JournalEntry entry = journalEntryList.get(position);
            journalEntryList.remove(position);
            notifyDataSetChanged();
            String where = MyContentProvider.KEY_REF + " = '" + entry.getRef() + "'";
            context.getContentResolver().delete(MyContentProvider.CONTENT_URI, where, null);
            helper.deleteDB(context.getString(R.string.journal_entity), entry.getRef(), this);
        }

        private void viewEntry(JournalEntry journalEntry) {
            Intent intent = new Intent(context, JournalDetailsActivity.class);
            intent.putExtra(context.getString(R.string.data), journalEntry);
            context.startActivity(intent);
        }

        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

        }
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful()){
                handler.obtainMessage(1, "Updated Successfully").sendToTarget();
                dialog.dismiss();
            }
            else
                handler.obtainMessage(1, task.getException().getMessage()).sendToTarget();
        }
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.view:
                    viewEntry(journalEntryList.get(getPosition()));
                    break;
                case R.id.delete:
                    deleteEntry(getPosition());
                    break;
                case R.id.edit:
                    editEntry(journalEntryList.get(getPosition()), getPosition());
                    break;
            }
            return true;
        }
        @Override
        public void onClick(View v) {
            PopupMenu menu = new PopupMenu(context, v);
            menu.inflate(R.menu.entry_option_menu);
            menu.setOnMenuItemClickListener(this);
            menu.show();
        }
    }
}
