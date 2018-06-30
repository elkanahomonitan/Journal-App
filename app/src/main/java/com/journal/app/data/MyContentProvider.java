package com.journal.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MyContentProvider extends ContentProvider {
    public  static final String KEY_TITLE = "title";
    public  static final  String KEY_DETAIL = "detail";
    public  static final  String KEY_USER_ID = "userId";
    public  static final  String KEY_REF = "ref";
    public static  final String KEY_ID = "_ID";
    private static final int JOURNAL = 100;
    private MySqliteOpenHelper myOpenHelper;
    private UriMatcher uriMatcher;
    public static  final Uri CONTENT_URI = Uri.parse("content://com.journal.app/title");
    public MyContentProvider() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.journal.app", "title", JOURNAL);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        int rowsInserted = 0;
       try {
           db.beginTransaction();
           for (ContentValues content : values){
               long inserted = db.insert(MySqliteOpenHelper.JOURNAL_TABLE, null, content);
               if(inserted > 0)
                   rowsInserted++;
           }
           db.setTransactionSuccessful();
       }finally {
           db.endTransaction();
       }
        if(rowsInserted > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsInserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        int deleteCount;
        deleteCount = db.delete(MySqliteOpenHelper.JOURNAL_TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public String getType(Uri uri) {
       return "vnd.android.cursor.dir/com.journal.app.title";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        long id = db.insert(MySqliteOpenHelper.JOURNAL_TABLE, null,  values);
        Uri insertedId = ContentUris.withAppendedId(CONTENT_URI, id);
        getContext().getContentResolver().notifyChange(insertedId, null);
        return  insertedId;
    }

    @Override
    public boolean onCreate() {
        myOpenHelper = new MySqliteOpenHelper(getContext(), MySqliteOpenHelper.DATABASE_NAME, null, MySqliteOpenHelper.DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        return db.query(MySqliteOpenHelper.JOURNAL_TABLE,null,selection, selectionArgs,null,null, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        int ret = db.update(MySqliteOpenHelper.JOURNAL_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    public class MySqliteOpenHelper extends SQLiteOpenHelper{
        public static final String DATABASE_NAME = "Journal.db";
        public MySqliteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists "+ JOURNAL_TABLE);
            db.execSQL(CREATE_DATABASE);
        }
        public static final String JOURNAL_TABLE = "journal_entity";
        public static final int DATABASE_VERSION = 1;
        public String CREATE_DATABASE = ("create table if not exists "
                + JOURNAL_TABLE + " ("
                + KEY_ID + " integer primary key autoincrement, "
                + KEY_TITLE + " text  not null, "
                + KEY_DETAIL + " text  not null, "
                + KEY_USER_ID + " text  not null, "
                + KEY_REF + " text  not null, "
                + " UNIQUE ( "+ KEY_REF +" ) ON CONFLICT REPLACE);");

    }
}
