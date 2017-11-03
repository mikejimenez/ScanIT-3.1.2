package alpha.com.ScanIT.databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikej on 10/28/2017.
 */

public class Senders extends SQLiteOpenHelper {

    @SuppressLint("SpellCheckingInspection")
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BC-SD.db";
    private static final String TABLE_NAME = "ShippingData";
    private static final String KEY_ID = "_id";
    private static final String KEY_COMPANY = "Company";

    private static final String TAG = "DbHelper-SD";

    public Senders(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creating Tables
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + KEY_ID + " INTEGER PRIMARY KEY,"
                        + KEY_COMPANY + " TEXT UNIQUE" + ")";
        db.execSQL(CREATE_TABLE);
        ContentValues values = new ContentValues();
        values.put(KEY_COMPANY , "");
        db.insert(TABLE_NAME, null, values);
    }


    /**
     * Inserting new Sender into Spinner database
     * Same input will throw SQLiteConstraintException
     * We want only 1 entry, no duplicates
     * */

    public void addSender(String label){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COMPANY, label);
        try {
            db.insertOrThrow(TABLE_NAME , null, values);
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "SQLiteConstraintException for " + label);
        }
        db.close();
    }

    /**
     * Getting all labels
     * returns list of labels
     * */
    public List<String> getAllSenders(){
        List<String> labels = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
    }
    public Cursor getSendersRaw() {
        Cursor c;
        //String countQuery = "SELECT * FROM " + TABLE_NAME;
        String countQuery = "SELECT * FROM " + TABLE_NAME + " WHERE _id > 1";
        SQLiteDatabase db = this.getReadableDatabase();
        c = db.rawQuery(countQuery, null);
        return c;
    }

    /**
     * Upgrading database
     */

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Deleting
     */

    public void DeleteRecord(Long _id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "_id = ?", new String[] { String.valueOf(_id) });
        db.close();
    }

    /**
     * Updating
     */

    public void UpdateRecord(Editable Data, Long _id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Company", Data.toString());
        db.update(TABLE_NAME, cv, "_id="+_id, null);
        db.close();
    }
}
