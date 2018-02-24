package com.nerdgeeks.cinetodo.todolist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by IMRAN on 9/13/2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "CineDB";
    private static final int DB_VER = 1;
    private static final String DB_TABLE = "Movies";
    private static final String DB_COLUMN = "MovieName";
    private static final String DB_CHECKED = "MovieCheck";
    private static final String DB_ID = "_id";
    private static final String DB_IMG = "ImageCover";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL);"
                , DB_TABLE, DB_ID, DB_COLUMN, DB_CHECKED, DB_IMG);
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = String.format("DROP TABLE IF EXISTS %s", DB_TABLE);
        db.execSQL(query);
        onCreate(db);
    }

    public void insertNewMovie(String title, String isChecked){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_COLUMN, title);
        values.put(DB_CHECKED, isChecked);
        values.put(DB_IMG, "");
        db.insertWithOnConflict(DB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void insertNewMovie(int id, String title, String isChecked, String encodedImage){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_ID, id);
        values.put(DB_COLUMN, title);
        values.put(DB_CHECKED, isChecked);
        values.put(DB_IMG, encodedImage);
        db.insertWithOnConflict(DB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteByEncodedTitle(String title){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DB_TABLE, DB_COLUMN + " = ?", new String[]{title});
        db.close();
    }

    private void deleteByEncodedImage(String encode){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DB_TABLE, DB_IMG + " = ?", new String[]{encode});
        db.close();
    }

    private void deleteById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DB_TABLE, DB_ID + " = ?", new String[]{Integer.toString(id)});
        db.close();
    }

    public void updateCheckedRow(String title, String bool){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_CHECKED, bool);
        db.update(DB_TABLE, values, DB_COLUMN + " = ?", new String[]{title});
        db.close();
    }

    public ArrayList<String> getMovieList(){
        ArrayList<String> movieList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DB_TABLE, new String[]{DB_COLUMN}, null, null, null, null, null);
        if (cursor != null){
            while (cursor.moveToNext()){
                int index = cursor.getColumnIndex(DB_COLUMN);
                movieList.add(cursor.getString(index));
            }
            cursor.close();
            db.close();
            return movieList;
        } else {
            return null;
        }
    }

    public String getEncodedImageById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.query(DB_TABLE, new String[]{DB_IMG}, DB_ID + " =? ",
                    new String[]{String.valueOf(id)}, null, null, null, null);
            if (cursor != null){
                cursor.moveToFirst();
                String encode = cursor.getString(cursor.getColumnIndex(DB_IMG));
                cursor.close();
                return encode;
            } else
                return "";
        } catch (CursorIndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
        return "";
    }

    public int getIdByTitle(String title){
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.query(DB_TABLE, new String[]{DB_ID}, DB_COLUMN + " =? ",
                    new String[]{title}, null, null, null, null);
            if (cursor != null){
                cursor.moveToFirst();
                int id = cursor.getInt(cursor.getColumnIndex(DB_ID));
                cursor.close();
                return id;
            } else
                return 0;
        } catch (CursorIndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
        return 0;
    }

    public ArrayList<String> getCheckList(){
        ArrayList<String> checkList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DB_TABLE, new String[]{DB_CHECKED}, null, null, null, null, null);
        if (cursor != null){
            while (cursor.moveToNext()){
                int index = cursor.getColumnIndex(DB_CHECKED);
                checkList.add(cursor.getString(index));
            }
            cursor.close();
            db.close();
            return checkList;
        } else {
            return null;
        }
    }

    public ArrayList<String> getImageList(){
        ArrayList<String> imgList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DB_TABLE, new String[]{DB_IMG}, null, null, null, null, null);
        if (cursor != null){
            while (cursor.moveToNext()){
                int index = cursor.getColumnIndex(DB_IMG);
                imgList.add(cursor.getString(index));
            }
            cursor.close();
            db.close();
            return imgList;
        } else {
            return null;
        }
    }

    public boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    public boolean isTableExists(String tableName) {
        SQLiteDatabase mDatabase = this.getReadableDatabase();

        Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public boolean existsColumnInTable(String inTable, String columnToCheck) {
        Cursor mCursor = null;
        SQLiteDatabase inDatabase = this.getReadableDatabase();
        try {
            // Query 1 row
            mCursor = inDatabase.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            return false;
        } finally {
            if (mCursor != null) mCursor.close();
        }
    }

    public boolean isDataExists(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DB_TABLE + " WHERE "
                + DB_ID + " = " + id;
        Cursor cursor = db.rawQuery(query, null);
        boolean exist = (cursor.getCount() > 0);
        cursor.close();
        return exist;
    }
}
