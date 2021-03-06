package alpha.com.ScanIT.databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;

import alpha.com.ScanIT.MainActivity;
import alpha.com.ScanIT.interfaces.Barcodes;

public class History extends SQLiteOpenHelper {


    @SuppressLint("SpellCheckingInspection")

    /**
     * Database Variables
     */

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BC-Data-History.db";
    private static final String TABLE_NAME = "Barcodes";
    private static final String KEY_ID = "_id";
    private static final String KEY_Barcode = "Barcode";
    private static final String KEY_COMPANY = "Company";
    private static final String KEY_NAME = "Name";
    private static final String KEY_Department = "Department";
    private static final String KEY_USERNAME = "Username";
    private static final String KEY_LISTVIEW = "Listview";
    private static final String KEY_COUNT = "Count";

    String TAG = "DbHelper";

    public History(Context context) {
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
                        + KEY_Barcode + " TEXT,"
                        + KEY_COMPANY + " TEXT,"
                        + KEY_NAME + " TEXT,"
                        + KEY_Department + " TEXT,"
                        + KEY_USERNAME + " TEXT,"
                        + KEY_LISTVIEW + " TEXT,"
                        + KEY_COUNT + " TEXT"  + ")";
        db.execSQL(CREATE_TABLE);
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
     * Adding new barcode
     */

    public void addBarcodes(Barcodes Barcodes) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_Barcode, Barcodes.getBarcode());
        values.put(KEY_COMPANY, Barcodes.getCompany());
        values.put(KEY_NAME, Barcodes.getName());
        values.put(KEY_Department, Barcodes.getDepartment());
        values.put(KEY_USERNAME, Barcodes.getUsername());
        values.put(KEY_LISTVIEW, Barcodes.getListView());
        values.put(KEY_COUNT, Barcodes.getCount());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    /**
     * Deleting all
     */

    public void deleteBarcodes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE " + TABLE_NAME);
        onCreate(db);
        db.close();
    }

    /**
     * Deleting
     */

    public void DeleteRecord(String _id,String _name,String _company) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "Barcode = ? AND Name = ? AND Company = ?", new String[] { _id, _name, _company });
        db.close();
    }
    /**
     * Updating
     */

    public void UpdateRecord(Editable Data, Long _id, Editable Department, String Name, String Listview, String Count, String Username, String Barcode, String _name, String _company) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Company", Data.toString());
        cv.put("Name", Name);
        cv.put("Department", Department.toString());
        cv.put("Username", Username);
        cv.put("Listview", Listview);
        cv.put("Count", Count);
        db.update(TABLE_NAME, cv, "Barcode = ? AND Name = ? AND Company = ?", new String[] { Barcode, _name, _company });
        db.close();
    }
    /**
     * Get All Barcodes
     */

    public Cursor getBarcodesRaw() {
        Cursor c;
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        c = db.rawQuery(countQuery, null);

        return c;
    }
}

