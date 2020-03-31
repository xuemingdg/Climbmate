package com.hokming.climbmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {


    public static final String USER_TABLE_NAME = "User";
    public static final String USER_COLUMN_ID = "id";
    public static final String USER_COLUMN_USERNAME = "username";
    public static final String USER_COLUMN_PASSWORD = "password";
    public static final String USER_COLUMN_NAME = "name";
    public static final String USER_COLUMN_RATING = "rating";

    public static final String CREATE_USER = "create table User ("
            + "id integer primary key autoincrement," + "username text,"
            + "password text," + "rating integer," + "info text)";

    private Context context;


    public MySQLiteOpenHelper(@Nullable Context context){
        super(context, "User.db", null, 1);
        this.context = context;
    }

    public MySQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
        createUsers(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertUser(String username, String password, int rating, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COLUMN_USERNAME, username);
        contentValues.put(USER_COLUMN_PASSWORD, password);
        contentValues.put(USER_COLUMN_RATING, rating);
        contentValues.put(USER_COLUMN_NAME, username);
        db.insert(USER_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + USER_TABLE_NAME, null );
        return res;
    }

    private void createUsers(SQLiteDatabase db){
        for(int i = 0 ; i < 20; i++) {
//            insertUser(DataSet.names[i], DataSet.apis[i], 0, db);
        }
    }

    public Cursor getUser(String username, SQLiteDatabase db) {
        return db.rawQuery("SELECT * FROM " + USER_TABLE_NAME + " WHERE " +
                USER_COLUMN_USERNAME + "=?", new String[]{username});
    }

    public boolean updateUser(Integer id,  int rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
//        contentValues.put(USER_COLUMN_RATING, rating);
        db.update(USER_TABLE_NAME, contentValues, USER_COLUMN_ID + " = ? ", new String[] { Integer.toString(id) } );
        return true;
    }
}
