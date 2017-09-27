package alpha.com.ScanIT.databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;
import java.util.ArrayList;
import java.util.List;

import alpha.com.ScanIT.interfaces.Barcodes;


public class SQLite extends SQLiteOpenHelper {


    @SuppressLint("SpellCheckingInspection")

    /**
     * Database Variables
     */

    private static final int DATABASE_VERSION = 1;
   // private static final String DATABASE_NAME = "BCData.db";
    private static final String DATABASE_NAME = "BC-Data.db";
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

    public SQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creating Tables
     */

    //ToDo Add a editText to capture company/sender to save to DB

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
     * Getting All Barcodes
     */

    public List<Barcodes> getBarCodes() {

        List<Barcodes> BarcodesList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        if (cursor.moveToFirst()) {
            do {
                Barcodes Barcodes = new Barcodes();
                Barcodes.setID(Integer.parseInt(cursor.getString(0)));
                Barcodes.setBarcode(cursor.getString(1));
                Barcodes.setCompany(cursor.getString(2));
                Barcodes.setName(cursor.getString(3));
                Barcodes.setDepartment(cursor.getString(4));
                Barcodes.setUsername(cursor.getString(5));
                Barcodes.setListView(cursor.getString(6));
                Barcodes.setCount(cursor.getString(7));

                BarcodesList.add(Barcodes);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return BarcodesList;
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

    public void DeleteRecord(Long _id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE _id=" + _id);
        db.close();
    }

    /**
     * Updating
     */

    public void UpdateRecord(Editable Data, Long _id, Editable Departments, String Name, String Listview, String Count) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET Company='" + Data + "' WHERE _id=" + _id);
        db.execSQL("UPDATE " + TABLE_NAME + " SET Name='" + Name + "' WHERE _id=" + _id);
        db.execSQL("UPDATE " + TABLE_NAME + " SET Department='" + Departments.toString() + "' WHERE _id=" + _id);
        db.execSQL("UPDATE " + TABLE_NAME + " SET Listview='" + Listview + "' WHERE _id=" + _id);
        db.execSQL("UPDATE " + TABLE_NAME + " SET Count='" + Count + "' WHERE _id=" + _id);

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