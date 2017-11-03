package alpha.com.ScanIT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.alpha.ZXing.android.IntentIntegrator;
import com.alpha.ZXing.android.IntentResult;
import com.opencsv.CSVWriter;
import com.readystatesoftware.viewbadger.BadgeView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import alpha.com.ScanIT.Notifications.Services;
import alpha.com.ScanIT.databases.History;
import alpha.com.ScanIT.databases.SQLite;
import alpha.com.ScanIT.databases.Senders;
import alpha.com.ScanIT.databases.TinyDB;
import alpha.com.ScanIT.interfaces.Barcodes;
import alpha.com.ScanIT.interfaces.FormatString;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    private static final String TAG = "MAKE_BARCODES";
    final Preferences pref = new Preferences();
    final HistoryData historyData = new HistoryData();

    @Bind(R.id.manual_entry_tracking)
    @Nullable
    EditText infoTrack;
    @Bind(R.id.manual_entry_company)
    @Nullable
    EditText infoData;
    @Bind(R.id.manual_entry_name)
    @Nullable
    EditText infoSender;
    @Nullable
    @Bind(R.id.scan_entry_department)
    Spinner scan_spinner;
    @Nullable
    @Bind(R.id.manual_entry_spinner_data)
    Spinner spinner_sender;
    @Nullable
    @Bind(R.id.scan_entry_count)
    Spinner scan_spinner_;
    @Nullable
    @Bind(R.id.spinner_data_departments)
    Spinner manual_spinner;
    @Nullable
    @Bind(R.id.spinner_data_package_count)

    Spinner manual_spinner_;
    TextView CounterTxt;
    TextView CounterTxtSet;
    TextView CounterTxtSave;
    TextView UserLog;
    String Output;
    String SpinnerValue;
    String SpinnerValue_;
    String ScanFromFedEXG;
    String ScanFromFedEXE;
    String ScanFromFedEXEAir;
    String ScanFromUPS;
    String OldBarcode;
    String DataUpdate2;
    String OldName;
    String OldCompany;
    String Error = "";
    Integer HistoryCounter;
    Integer Counter = 0;
    BadgeView badge;
    BadgeView badge_history;
    View setCount;
    View setCount_history;
    private ArrayList<String> log = new ArrayList<String>();

    /**
     * onCreate - create Activity
     * ---------------------------
     * - Set window flags
     * - Create Main View
     * - Create Counter Text
     * ---------------------------
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setCount = findViewById(R.id.TotalCounter);
        setCount_history = findViewById(R.id.historyCounter);
        CounterTxt = (TextView) findViewById(R.id.textView2);

        badge = new BadgeView(this, setCount);
        badge_history = new BadgeView(this, setCount_history);

        pref.Load();
        FloatingActionButtons();
    }

    /**
     * onStop - when activity stops
     * ----------------------------
     * - Save preferences
     * - Start notification service
     * ----------------------------
     */

    @Override
    protected void onStop() {
        super.onStop();
        pref.Save();

        if (checkFocus()) {
            //Log.e(TAG, "True");
        } else {
            startService(new Intent(this, Services.class));
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    /**
     * Code that processes the intent from barcode scanner
     *
     * @param requestCode Barcode scanner argument
     * @param resultCode  RESULT_OK, processes data
     *                    RESULT_CANCELED, we didn't get any data
     * @param intent      Barcode scanner argument
     */

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            @SuppressLint("InflateParams")

            final String scanContent = scanningResult.getContents();
            final String infoBarcode = Filter(scanContent);
            final String Username = "User: " + data_exec("tinyDB", "getString", "LOGGED_ACTUAL", false, null, null) + " - " + data_exec("tinyDB", "getString", "LOGGED_IN", false, null, null);

            scan_entry_view(infoBarcode, Username, false);

        } else if (resultCode == RESULT_CANCELED) {
            ScanDataEmpty();
        }
    }

    /**
     * Scan Entry to create view after receiving data from barcode scanner
     *
     * @param ScanContent Barcode to be saved
     * @param Username    Username to be saved
     * @param Back        True/False if we are going backwards
     */

    public void scan_entry_view(final String ScanContent, final String Username, final Boolean Back) {

        final LayoutInflater Results = LayoutInflater.from(this);
        final View textEntryView = Results.inflate(R.layout.scan_entry, null);
        final EditText infoCompany = (EditText) textEntryView.findViewById(R.id.scan_entry_company);
        final EditText infoName = (EditText) textEntryView.findViewById(R.id.scan_entry_name);
        final CheckBox ChkBox = (CheckBox) textEntryView.findViewById((R.id.scan_entry_checkBox));
        final CheckBox ChkBox_ = (CheckBox) textEntryView.findViewById((R.id.scan_entry_checkBox2));
        final Spinner spinner_sender_scan = (Spinner) textEntryView.findViewById(R.id.scan_entry_spinner_data);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (Back) {
            if (data_exec("tinyDB", "getString", "SCAN_SAVED", false, null, null).equals("TRUE")) {
                final String load_previous_company = data_exec("tinyDB", "getString", "SCAN_PREVIOUS_COMPANY", false, null, null);
                final String load_previous_name = data_exec("tinyDB", "getString", "SCAN_PREVIOUS_NAME", false, null, null);

                infoCompany.setText(load_previous_company);
                infoName.setText(load_previous_name);

                data_exec("tinyDB", "remove", "SCAN_PREVIOUS_COMPANY", false, null, null);
                data_exec("tinyDB", "remove", "SCAN_PREVIOUS_NAME", false, null, null);
            } else {
                final String load_previous_company = data_exec("tinyDB", "getString", "SCAN_PREVIOUS_COMPANY_BEFORE_SAVE", false, null, null);
                final String load_previous_name = data_exec("tinyDB", "getString", "SCAN_PREVIOUS_NAME_BEFORE_SAVE", false, null, null);

                infoCompany.setText(load_previous_company);
                infoName.setText(load_previous_name);

                data_exec("tinyDB", "remove", "SCAN_PREVIOUS_COMPANY_BEFORE_SAVE", false, null, null);
                data_exec("tinyDB", "remove", "SCAN_PREVIOUS_NAME_BEFORE_SAVE", false, null, null);
            }
        }

        /**
         * Sender Information
         */

        manual_spinner_load_senders(spinner_sender_scan);
        spinner_sender_scan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object Location = parent.getItemAtPosition(position);
                SpinnerValue = Location.toString();

                if (SpinnerValue.equals("")) {
                    infoCompany.setVisibility(View.VISIBLE);
                } else {
                    infoCompany.setText(SpinnerValue);
                    infoCompany.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**
         * Repeat information for last package scanned
         */

        ChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (ChkBox.isChecked() && !Back && !data_exec("tinyDB", "getString", "SCAN_PREVIOUS_COMPANY", false, null, null).isEmpty()) {
                    infoCompany.setText(data_exec("tinyDB", "getString", "SCAN_PREVIOUS_COMPANY", false, null, null));
                    infoName.setText(data_exec("tinyDB", "getString", "SCAN_PREVIOUS_NAME", false, null, null));

                } else if (ChkBox.isChecked() && Back && !data_exec("tinyDB", "getString", "SCAN_PREVIOUS_COMPANY", false, null, null).isEmpty()) {
                    infoCompany.setText(data_exec("tinyDB", "getString", "SCAN_PREVIOUS_COMPANY", false, null, null));
                    infoName.setText(data_exec("tinyDB", "getString", "SCAN_PREVIOUS_NAME", false, null, null));
                } else {

                }
            }
        });

        /**
         * Mark as an outgoing package
         */

        ChkBox_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (ChkBox_.isChecked()) {
                    final String getTextName = infoName.getText().toString();
                    final String newTextName = getTextName + " (Outgoing)";
                    infoName.setText(newTextName);
                } else {
                    final String getOldText = infoName.getText().toString();
                    final String newOutText = getOldText.replace(" (Outgoing)", "");
                    infoName.setText(newOutText);
                }
            }
        });
        alert.setIcon(R.drawable.perm_group_user_dictionary).setTitle("Information").setView(textEntryView).setPositiveButton("Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        if (ChkBox.isChecked()) {
                            data_exec("tinyDB", "putString", "SCAN_PREVIOUS_CHECKED", false, null, "TRUE");
                        } else {
                            data_exec("tinyDB", "putString", "SCAN_PREVIOUS_CHECKED", false, null, "FALSE");
                        }
                        Error = "";
                        if (Back) {
                            final String scanCompany = infoCompany.getText().toString();
                            final String scanName = infoName.getText().toString();
                            data_exec("tinyDB", "putString", "SCAN_SAVED", false, null, "FALSE");
                            data_exec("tinyDB", "putString", "SCAN_PREVIOUS_NAME_BEFORE_SAVE", false, null, scanName);
                            data_exec("tinyDB", "putString", "SCAN_PREVIOUS_COMPANY_BEFORE_SAVE", false, null, scanCompany);

                            if (!Error.contains("Y")) {
                                scan_entry_spinner_data(ScanContent, scanCompany, scanName, Username);
                            }
                        } else {
                            final String scanCompany = infoCompany.getText().toString();
                            final String scanName = infoName.getText().toString();

                            data_exec("tinyDB", "putString", "SCAN_SAVED", false, null, "FALSE");
                            data_exec("tinyDB", "putString", "SCAN_PREVIOUS_NAME_BEFORE_SAVE", false, null, scanName);
                            data_exec("tinyDB", "putString", "SCAN_PREVIOUS_COMPANY_BEFORE_SAVE", false, null, scanCompany);

                            if (!Error.contains("Y")) {
                                scan_entry_spinner_data(ScanContent, scanCompany, scanName, Username);
                            }
                        }
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        ScanDataEmpty();
                    }
                });
        alert.show();
    }

    /**
     * Scan Entry Spinner Data + Saving to database
     *
     * @param infoBarcode Barcode to be saved
     * @param scanCompany Company to be saved
     * @param scanName    Name to be saved
     * @param Username    Username to be saved
     */

    public void scan_entry_spinner_data(final String infoBarcode, final String scanCompany, final String scanName, final String Username) {

        LayoutInflater view_spinner = LayoutInflater.from(this);
        final View view_spinner_inflate = view_spinner.inflate(R.layout.scan_entry_spinner, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final SQLite db = new SQLite(this);
        final History db2 = new History(this);
        final Senders ship_db = new Senders(this);

        int resourceId = this.getResources().getIdentifier("scan_entry_department", "id", this.getPackageName());
        int resourceId2 = this.getResources().getIdentifier("scan_entry_count", "id", this.getPackageName());

        Spinners(view_spinner_inflate, true, resourceId, resourceId2);

        alert.setIcon(R.drawable.ic_dialog_alert_holo_light).setTitle("Package Information").setView(view_spinner_inflate).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        final String Department = SpinnerValue;
                        final String Count = SpinnerValue_;
                        final String ListView = scanCompany + " - " + scanName + " - " + Department + " - " + Count;

                        db.addBarcodes(new Barcodes(infoBarcode, scanCompany, scanName, Department, Username, ListView, Count));
                        db2.addBarcodes(new Barcodes(infoBarcode, scanCompany, scanName, Department, Username, ListView, Count));
                        ship_db.addSender(scanCompany);
                        Counter++;
                        db.close();
                        db2.close();
                        ship_db.close();
                        CreateListView();
                        String setText = Integer.valueOf(Counter).toString();
                        CounterTxt.setText(setText);
                        badge.increment(1);
                        data_exec("tinyDB", "putString", "SCAN_PREVIOUS_NAME", false, null, scanName);
                        data_exec("tinyDB", "putString", "SCAN_PREVIOUS_COMPANY", false, null, scanCompany);
                        data_exec("tinyDB", "putString", "SCAN_PREVIOUS_DEPARTMENT", false, null, Department);
                        data_exec("tinyDB", "putString", "SCAN_PREVIOUS_COUNT", false, null, Count);
                        data_exec("tinyDB", "putString", "SCAN_SAVED", false, null, "TRUE");
                        data_exec("tinyDB", "remove", "SCAN_PREVIOUS_NAME_BEFORE_SAVE", false, null, null);
                        data_exec("tinyDB", "remove", "SCAN_PREVIOUS_COMPANY_BEFORE_SAVE", false, null, null);

                    }
                }).setNegativeButton("Back",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        scan_entry_view(infoBarcode, Username, true);
                    }
                });

        alert.show();

    }

    /**
     * Reading from database and Displaying
     */

    private void CreateListView() {

        final ListView listView;
        final SQLite db = new SQLite(this);
        final Cursor cursor = db.getBarcodesRaw();

        listView = (ListView) findViewById(R.id.listView);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item,
                cursor,
                new String[]{"Barcode", "Listview"},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        listView.setDivider(null);
        listView.setSelector(android.R.color.transparent);
        listView.setAdapter(adapter);
        listView.setVerticalScrollBarEnabled(false);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CreatePopup(id);
                return false;
            }
        });
        db.close();

    }

    private void CreateListView_Senders() {

        final Senders db = new Senders(MainActivity.this);
        final Cursor cursor = db.getSendersRaw();
        final ListView view = (ListView) findViewById(R.id.sender_data);
        final ListView hideHistory = (ListView) findViewById(R.id.listView_history);
        final ListView hideView = (ListView) findViewById(R.id.listView);
        final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fabClearHistory);

        hideView.setVisibility(View.GONE);
        hideHistory.setVisibility(View.GONE);
        badge_history.hide();
        fabClear.setVisibility(View.INVISIBLE);
        view.setVisibility(View.VISIBLE);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[]{"Company"},
                new int[]{android.R.id.text1},
                0);

        view.setDivider(null);
        view.setSelector(android.R.color.transparent);
        view.setAdapter(adapter);
        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CreatePopup_sender(id);
                return false;
            }
        });
        db.close();
    }

    private void CreatePopup(Long id) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LinearLayout Manual = (LinearLayout) View.inflate(this, R.layout.update, null);
        final EditText infoData = (EditText) Manual.findViewById(R.id.InfoDataCompany);
        final EditText infoDataName = (EditText) Manual.findViewById(R.id.InfoDataName);
        final TextView departmentTxt = (TextView) Manual.findViewById(R.id.textView3);
        final Spinner spinner = (Spinner) Manual.findViewById(R.id.infoDepartments_update);
        final Spinner spinner_ = (Spinner) Manual.findViewById(R.id.infoPackage_count__);
        final Button Accept = (Button) Manual.findViewById(R.id.button);
        final Button Delete = (Button) Manual.findViewById(R.id.delete);
        final SQLite db = new SQLite(this);
        final SQLite db2 = new SQLite(this);
        final SQLite db3 = new SQLite(this);
        final History db4 = new History(this);
        final SQLite db5 = new SQLite(this);
        final SQLite db6 = new SQLite(this);
        final Long _id = id;
        final SQLiteDatabase X = db.getReadableDatabase();
        final SQLiteDatabase X2 = db2.getReadableDatabase();
        final SQLiteDatabase X3 = db3.getReadableDatabase();
        final SQLiteDatabase X4 = db5.getReadableDatabase();
        final SQLiteDatabase X5 = db6.getReadableDatabase();
        final Cursor c;
        final Cursor c2;
        final Cursor c3;
        final Cursor c4;
        final Cursor c5;

        builder.setView(Manual);
        final AlertDialog alertDialog = builder.create();

        c = X.rawQuery("SELECT Company FROM Barcodes WHERE _id =" + _id, null);
        c2 = X2.rawQuery("SELECT Department FROM Barcodes WHERE _id =" + _id, null);
        c3 = X3.rawQuery("SELECT Name FROM Barcodes WHERE _id =" + _id, null);
        c4 = X4.rawQuery("SELECT Count FROM Barcodes WHERE _id =" + _id, null);
        c5 = X5.rawQuery("SELECT Barcode FROM Barcodes WHERE _id =" + _id, null);


        c.moveToFirst();
        c2.moveToFirst();
        c3.moveToFirst();
        c4.moveToFirst();
        c5.moveToFirst();

        final String Company = c.getString(c.getColumnIndexOrThrow("Company"));
        final String Department = c2.getString(c2.getColumnIndexOrThrow("Department"));
        final String Name = c3.getString(c3.getColumnIndexOrThrow("Name"));
        final String OldCount = c4.getString(c4.getColumnIndexOrThrow("Count"));
        final String Barcode = c5.getString(c5.getColumnIndexOrThrow("Barcode"));

        c.close();
        c2.close();
        c3.close();
        c4.close();
        c5.close();


        DataUpdate2 = Company + " - " + Name + " - " + Department + " - " + OldCount;
        OldBarcode = Barcode;
        OldName = Name;
        OldCompany = Company;

        /**
         *  Spinners
         */

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.departments, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object Location = parent.getItemAtPosition(position);
                SpinnerValue = Location.toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**
         *  Package Count
         */

        List<String> Package_List = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Package_List.add("" + i);
        }
        ArrayAdapter<String> adapter_ = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Package_List);
        adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_.setAdapter(adapter_);
        spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object Location_ = parent.getItemAtPosition(position);
                SpinnerValue_ = Location_.toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Accept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (infoData.length() != 0) {
                    final Editable Data = infoData.getText();
                    final String Name = infoDataName.getText().toString();
                    final String Count = SpinnerValue_;
                    final String ListView = Data + " - " + Name + " - " + SpinnerValue + " - " + Count;
                    final String logged_a = data_exec("tinyDB", "getString", "LOGGED_ACTUAL", false, null, null);
                    final String logged_b = data_exec("tinyDB", "getString", "LOGGED_IN", false, null, null);
                    final String infoUsername = "User: " + logged_a + " - " + logged_b;
                    db.UpdateRecord(Data, _id, Editable.Factory.getInstance().newEditable(SpinnerValue), Name, ListView, Count, infoUsername);
                    db.close();
                    db4.UpdateRecord(Data, _id, Editable.Factory.getInstance().newEditable(SpinnerValue), Name, ListView, Count, infoUsername, OldBarcode, OldName, OldCompany);
                    db4.close();
                    alertDialog.dismiss();
                    CreateListView();
                    //Log.e(TAG, "Old: " + DataUpdate2 + " - New: " + ListView);
                } else {
                    Snackbar.make(Manual, "Please check input", Snackbar.LENGTH_LONG).setDuration(2300).show();
                }
            }
        });
        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.DeleteRecord(_id);
                db4.DeleteRecord(Barcode, Name, Company);
                alertDialog.dismiss();
                db.close();
                db4.close();
                CreateListView();
                badge.decrement(1);
                Counter = Counter - 1;
                CounterTxt.setText(String.format(Integer.valueOf(Counter).toString()));

            }
        });

        departmentTxt.setText(DataUpdate2);
        alertDialog.show();
        db.close();
        db2.close();
        db3.close();
        db4.close();
        db5.close();
    }

    private void CreatePopup_sender(Long id) {
        final Senders db = new Senders(MainActivity.this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final RelativeLayout Senders = (RelativeLayout) View.inflate(MainActivity.this, R.layout.sender_update, null);
        final Button delete_sender = (Button) Senders.findViewById(R.id.delete_sender);
        final Button update_sender = (Button) Senders.findViewById(R.id.update_sender);
        final EditText info_company = (EditText) Senders.findViewById(R.id.info_company);
        final TextView previous_data = (TextView) Senders.findViewById(R.id.previous_data);
        final long _id = id;
        builder.setView(Senders);

        final AlertDialog alertDialog = builder.create();
        final SQLiteDatabase X = db.getReadableDatabase();
        final Cursor c;

        c = X.rawQuery("SELECT Company FROM ShippingData WHERE _id =" + _id, null);
        c.moveToFirst();
        final String Company = c.getString(c.getColumnIndexOrThrow("Company"));
        c.close();

        previous_data.setText(Company);

        delete_sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.DeleteRecord(_id);
                alertDialog.dismiss();
                db.close();
                CreateListView_Senders();
            }
        });
        update_sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info_company.length() != 0) {
                    final Editable Data = info_company.getText();
                    db.UpdateRecord(Data, _id);
                    db.close();
                    alertDialog.dismiss();
                    CreateListView_Senders();
                } else {
                    Snackbar.make(Senders, "Please check input", Snackbar.LENGTH_LONG).setDuration(2300).show();
                }
            }
        });
        alertDialog.show();
    }

    /**
     * Manual Functions
     */

    /**
     * InputManual
     * Manually input a tracking number with information
     *
     * @param Back When True it will load the previous inputted information being proceeding to package information
     */

    private void InputManual(final Boolean Back) {

        LayoutInflater Manual = LayoutInflater.from(this);
        @SuppressLint("AndroidLintInflateParams")

        final View textEntryView = Manual.inflate(R.layout.manual_entry, null);

        ButterKnife.bind(this, textEntryView);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final String load_previous_company = data_exec("tinyDB", "getString", "MANUAL_PREVIOUS_COMPANY", false, null, null);
        final String load_previous_name = data_exec("tinyDB", "getString", "MANUAL_PREVIOUS_NAME", false, null, null);
        final String load_previous_tracking = data_exec("tinyDB", "getString", "MANUAL_PREVIOUS_TRACKING", false, null, null);

        data_exec("tinyDB", "remove", "MANUAL_PREVIOUS_COMPANY", false, null, null);
        data_exec("tinyDB", "remove", "MANUAL_PREVIOUS_NAME", false, null, null);
        data_exec("tinyDB", "remove", "MANUAL_PREVIOUS_TRACKING", false, null, null);
        data_exec("tinyDB", "remove", "MANUAL_SPINNER_DATA", false, null, null);

        if (Back) {
            infoData.setText(load_previous_company);
            infoSender.setText(load_previous_name);
            infoTrack.setText(load_previous_tracking);
        }
        manual_spinner_load_senders(spinner_sender);

        spinner_sender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object Location = parent.getItemAtPosition(position);
                SpinnerValue = Location.toString();

                if (SpinnerValue.equals("")) {
                    infoData.setVisibility(View.VISIBLE);
                } else {
                    infoData.setText(SpinnerValue);
                    infoData.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        alert.setIcon(R.drawable.ic_dialog_alert_holo_light).setTitle("Tracking Number").setView(textEntryView).setPositiveButton("Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        final String logged_a = data_exec("tinyDB", "getString", "LOGGED_ACTUAL", false, null, null);
                        final String logged_b = data_exec("tinyDB", "getString", "LOGGED_IN", false, null, null);
                        final String manual_infoCompany = infoData.getText().toString();
                        final String manual_infoTracking = infoTrack.getText().toString();
                        final String manual_infoName = infoSender.getText().toString();
                        final String manual_infoUsername = "User: " + logged_a + " - " + logged_b;

                        data_exec("tinyDB", "putString", "MANUAL_PREVIOUS_COMPANY", false, null, infoData.getText().toString());
                        data_exec("tinyDB", "putString", "MANUAL_PREVIOUS_NAME", false, null, infoSender.getText().toString());
                        data_exec("tinyDB", "putString", "MANUAL_PREVIOUS_TRACKING", false, null, infoTrack.getText().toString());

                        manual_spinner_data(manual_infoCompany, manual_infoTracking, manual_infoName, manual_infoUsername);

                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        ScanDataEmpty();
                    }
                });

        alert.show();

    }
//

    private void manual_spinner_load_senders(Spinner spinner) {
        final Senders ship_db = new Senders(this);
        final List<String> data = ship_db.getAllSenders();
        ship_db.close();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    /**
     * To add a barcode from manual input
     *
     * @param Result     String value from EditText (Tracking Number)
     * @param Company    String value from EditText (Company)
     * @param Name       String value from EditText (Name)
     * @param Department String value from Spinner  (Department)
     * @param Username   String value from TinyDB  (Username)
     */

    private void mAdd(String Result, String Company, String Name, String Department, String Username, String ListView, String Count) {
        final SQLite db = new SQLite(this);
        final History db2 = new History(this);
        final Senders ship_db = new Senders(this);

        db.addBarcodes(new Barcodes(Result, Company, Name, Department, Username, ListView, Count));
        db2.addBarcodes(new Barcodes(Result, Company, Name, Department, Username, ListView, Count));
        ship_db.addSender(Company);
        //DisplayDebug(getWindow().getDecorView().getRootView(), Result, Company, Name, Department, Username, Count);
        Counter++;
        db.close();
        db2.close();
        ship_db.close();
        CreateListView();
        CounterTxt.setText(String.format(Integer.valueOf(Counter).toString()));
        badge.increment(1);
    }

    /**
     * Creates alert dialog for manual spinner data
     *
     * @param infoCompany  Company to be saved
     * @param infoTracking Tracking number to be saved
     * @param infoName     Name to be saved
     * @param infoUsername Username to be saved
     */
    private void manual_spinner_data(final String infoCompany, final String infoTracking, final String infoName, final String infoUsername) {

        LayoutInflater view_spinner = LayoutInflater.from(this);
        final View view_spinner_inflate = view_spinner.inflate(R.layout.manual_entry_spinner, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        int resourceId = this.getResources().getIdentifier("spinner_data_departments", "id", this.getPackageName());
        int resourceId2 = this.getResources().getIdentifier("spinner_data_package_count", "id", this.getPackageName());

        Spinners(view_spinner_inflate, false, resourceId, resourceId2);

        alert.setIcon(R.drawable.ic_dialog_alert_holo_light).setTitle("Package Information").setView(view_spinner_inflate).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        final String Department = SpinnerValue;
                        final String Count = SpinnerValue_;
                        final String ListView = infoCompany + " - " + infoName + " - " + Department + " - " + Count;

                        if (manual_entry_view(infoCompany, infoTracking, infoName, infoUsername, Department, Count, ListView).equals("1")) {

                        } else {
                            ScanDataEmpty();
                        }
                    }
                }).setNegativeButton("Back",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        InputManual(true);
                    }
                });

        alert.show();

    }

    /**
     * Updates the Tracking number so it can be viewed on the list
     *
     * @param infoCompany  Company to be saved
     * @param infoTracking Tracking number to be saved
     * @param infoName     Name to be saved
     * @param infoUsername Username to be saved
     * @param Department   Department to be saved
     * @param Count        Package count
     * @param ListView     Format for the listView
     * @return Returns 1 if successful and 0 if not
     */

    private String manual_entry_view(String infoCompany, String infoTracking, String infoName, String infoUsername, String Department, String Count, String ListView) {

        if (infoTrack.length() == 0) {
            ScanDataEmpty();
            return "0";
        }

        /**
         *      Fedex Ground Manual (No Spaces) - 21 Characters
         *
         *      Format: XXXXXXXXXXXXXXXXXXXXX
         *
         */

        if (infoTrack.length() == 21 && TextUtils.isDigitsOnly(infoTrack.getText())) {
            mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
            return "1";
        }

        /**
         *      UPS Manual (No Spaces) - 18 Characters
         *
         *      Format: 1ZXXXXXXXXXXXXXXXX
         *              1zXXXXXXXXXXXXXXXX
         *
         */

        if (infoTrack.length() == 18) {
            if (infoTrack.getText().toString().contains("1Z") || infoTrack.getText().toString().contains("1z")) {
                mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
                return "1";
            }
        }

        /**
         *      UPS Manual (Spaces) - 23 Characters
         *
         *      Format: 1Z XXX XXX XX XXXX XXXX
         *              1z XXX XXX XX XXXX XXXX
         *
         *      Fedex Ground Manual (Spaces) - 23 Characters
         *
         *      Format: XXXXXXX XXXXXXX XXXXXXX
         *
         */

        if (infoTrack.length() == 23) {
            if (TextUtils.isDigitsOnly(infoTrack.getText().toString().replaceAll("\\s+", ""))) {
                mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
                return "1";
            }
            if (infoTrack.getText().toString().contains("1Z") || infoTrack.getText().toString().contains("1z")) {
                mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
                return "1";
            }
        }

        /**
         *      Fedex Express Manual (No Spaces) - 12 Characters
         *
         *      Format: XXXXXXXXXXXX
         *
         */

        if (infoTrack.length() == 12 && TextUtils.isDigitsOnly(infoTrack.getText())) {
            mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
            return "1";
        }

        /**
         *      Fedex Express Manual (Spaces) - 14 Characters
         *
         *      Format: XXXX XXXX XXXX
         *
         */

        if (infoTrack.length() == 14 && TextUtils.isDigitsOnly(infoTrack.getText().toString().replaceAll("\\s+", ""))) {
            mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
            return "1";
        }
        return "0";
    }

    /**
     * Creates spinner data for Manual & Scan entry
     *
     * @param view_spinner_inflate View to inflate
     * @param Scan                 True or false for previous scanning
     * @param resource             Resource ID #
     * @param resource_            Resource ID #2
     */

    public void Spinners(View view_spinner_inflate, Boolean Scan, int resource, int resource_) {

        final Spinner spinner = (Spinner) view_spinner_inflate.findViewById(resource);
        final Spinner spinner_ = (Spinner) view_spinner_inflate.findViewById(resource_);

        /**
         * Department data
         */

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.departments, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object Location = parent.getItemAtPosition(position);
                SpinnerValue = Location.toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**
         *  Package Count
         */

        List<String> Package_List = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Package_List.add("" + i);
        }
        ArrayAdapter<String> adapter_ = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Package_List);
        adapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_.setAdapter(adapter_);
        spinner_.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object Location_ = parent.getItemAtPosition(position);
                SpinnerValue_ = Location_.toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (Scan && data_exec("tinyDB", "getString", "SCAN_PREVIOUS_CHECKED", false, null, null).equals("TRUE")) {
            spinner.setSelection(adapter.getPosition(data_exec("tinyDB", "getString", "SCAN_PREVIOUS_DEPARTMENT", false, null, null)));
            spinner_.setSelection(adapter_.getPosition(data_exec("tinyDB", "getString", "SCAN_PREVIOUS_COUNT", false, null, null)));
        } else {

        }
    }

    /**
     * Email Log
     * Update log
     * Create List View
     * Filter results
     * Error Message
     */

    private void emailResults() {
        final Calendar c = Calendar.getInstance();
        Intent i = new Intent(Intent.ACTION_SEND);
        final SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        final String formattedDate = df.format(c.getTime());
        final String[] TO = {"Receiving@cdaresort.com"};
        i.setType("text/csv");
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("/storage/emulated/0/Receiving-Data-" + formattedDate + ".csv")));
        i.putExtra(Intent.EXTRA_SUBJECT, "Tracking Numbers - " + formattedDate + "");
        i.putExtra(Intent.EXTRA_EMAIL, TO);
        UpdateLog();
        final String newString = Arrays.toString(log.toArray());
        final String FilterA = newString.replace(", null", "");
        final String FilterB = FilterA.replace("[", "");
        final String FilterC = FilterB.replace("]", "");
        final String FilterD = FilterC.replace(",", "");
        i.putExtra(Intent.EXTRA_TEXT, FilterD);

        log.clear();
        Counter = 0;
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void UpdateLog() {

        /**
         * Display all bar codes
         * */

        final SQLite db = new SQLite(this);
        final List<Barcodes> Barcodes = db.getBarCodes();
        db.close();

        for (Barcodes cn : Barcodes) {
            final String bar = cn.getBarcode();

            /**
             *      UPS Manual (No Spaces) - 18 Characters
             *
             *      Format : 1ZXXXXXXXXXXXXXXXX
             *
             */

            if (bar.length() == 18) {
                Output = FormatString.UPS(bar);
                final String values =
                        "\nSender: " + cn.getCompany()
                                + "\n" + "Name: " + cn.getName()
                                + "\n" + "Dept.: " + cn.getDepartment() + "\n" + "Tracking: "
                                + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                log.add(values);
            }

            /**
             *      UPS Manual - 23 Characters
             *
             *      Format: 1Z XXX XXX XX XXXX XXXX
             *              1z XXX XXX XX XXXX XXXX
             *
             */

            if (bar.length() == 23) {
                if (bar.contains("1Z") || bar.contains("1z")) {
                    Output = FormatString.ManualUPS(bar);
                    final String values =
                            "\nSender: " + cn.getCompany()
                                    + "\n" + "Name: " + cn.getName()
                                    + "\n" + "Dept.: " + cn.getDepartment() + "\n" + "Tracking: "
                                    + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                    log.add(values);
                }
            }
            /**
             *
             * Fedex Ground OLD (Scanner) - 22 Characters
             *
             */

            if (bar.length() == 22) {
                Output = FormatString.FedEXGO(bar);
                final String values =
                        "\nSender: " + cn.getCompany()
                                + "\n" + "Name: " + cn.getName()
                                + "\n" + "Dept.: " + cn.getDepartment() + "\n" + "Tracking: "
                                + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                log.add(values);
            }

            /**
             *      Fedex Ground Manual (No Spaces) - 21 Characters
             *
             *      Format: XXXXXXXXXXXXXXXXXXXXX
             */

            if (bar.length() == 21 && !bar.contains("Z")) {
                Output = FormatString.ManualFedEXG(bar);
                final String values =
                        "\nSender: " + cn.getCompany()
                                + "\n" + "Name: " + cn.getName()
                                + "\n" + "Dept.: " + cn.getDepartment() + "\n" + "Tracking: "
                                + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                log.add(values);
            }

            /**
             *      Fedex Express Manual (Spaces) - 14 Characters
             *
             *      Format: XXXX XXXX XXXX
             *
             */

            if (bar.length() == 14) {
                Output = FormatString.ManualFedEXE(bar);
                final String values =
                        "\nSender: " + cn.getCompany()
                                + "\n" + "Name: " + cn.getName()
                                + "\n" + "Dept.: " + cn.getDepartment() + "\n" + "Tracking: "
                                + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                log.add(values);
            }


            /**
             *      Fedex Ground New (Scanner) - 34 Characters
             *      Fedex Express New (Scanner) - 34 Characters
             *      Fedex Express OLD (Scanner) - 32 Characters
             *      Fedex Express Manual (No Spaces) - 12 Characters
             *
             *      Format: XXXXXXXXXXXX
             *
             */

            if (bar.length() == 12) {
                Output = FormatString.FedEXE(bar);
                final String values =
                        "\nSender: " + cn.getCompany()
                                + "\n" + "Name: " + cn.getName()
                                + "\n" + "Dept.: " + cn.getDepartment() + "\n" + "Tracking: "
                                + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                log.add(values);
            }
        }
    }

    /**
     * Filter Barcode
     *
     * @param Number Barcode value to filter
     */

    private String Filter(String Number) {

        if (Number.length() == 34) {

            /**
             * Scanner
             * System.out.println("Added Type Fedex Express");
             * System.out.println("Added Type Fedex Ground New");
             */

            ScanFromFedEXE = Number.substring(Number.length() - 12, Number.length());
            return ScanFromFedEXE;
        }
        if (Number.length() == 32) {

            /**
             * Scanner
             * System.out.println("Added Type Fedex Express Old");
             * System.out.println("Added Type Fedex Ground");
             */

            ScanFromFedEXG = Number.substring(Number.length() - 16, Number.length() - 4);
            return ScanFromFedEXG;
        }
        if (Number.length() == 22) {

            /**
             * Scanner
             * System.out.println("Added Type Fedex Ground Old");
             */

            ScanFromFedEXG = Number.substring(Number.length() - 22, Number.length());
            return ScanFromFedEXG;
        }
        if (Number.length() == 16 && !Number.contains("Z")) {

            /**
             * Scanner
             * System.out.println("Added Type Fedex Express US Airbill Paper");
             */

            ScanFromFedEXEAir = Number.substring(Number.length() - 16, Number.length() - 4);
            return ScanFromFedEXEAir;
        }
        //if (Number.length() == 12) { ScanFromFedEXE = Number.substring(Number.length() - 12, Number.length()); return ScanFromFedEXE; }
        if (Number.contains("1Z") && Number.length() == 18) {
            ScanFromUPS = Number;
            return ScanFromUPS;
        } else {

            /**
             * Error
             */

            Error = "Y";
            ScanDataEmpty();
        }

        return Number;
    }

    /**
     * user_logout - Log user out and start intent for UserLogin
     * loadPreferences - Load user preferences
     * savePreferences - Save user preferences
     * data_exec - TinyDB, SQLite, History database methods
     * checkFocus() - Check for application focus or start notification service
     */
    private void user_logout() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final RelativeLayout customView = (RelativeLayout) View.inflate(this, R.layout.user_logout, null);
        final Button logout = (Button) customView.findViewById(R.id.logout);
        final Button cancel = (Button) customView.findViewById(R.id.cancel);
        final Button senders = (Button) customView.findViewById(R.id.senders);

        //final ListView view = (ListView) findViewById(R.id.sender_data);

        builder.setView(customView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data_exec("tinyDB", "remove", "LOGIN_SKIP", false, null, null);
                data_exec("tinyDB", "remove", "LOGGED_IN", false, null, null);
                data_exec("tinyDB", "remove", "LOGGED_ACTUAL", false, null, null);
                final String[] log = new String[200];
                Arrays.fill(log, "null");
                data_exec("deleteBarcodes", null, null, true, null, null);
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this, R.style.MyGravity);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Logging out...");
                progressDialog.show();

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(), UserLogin.class);
                                startActivityForResult(intent, 0);
                                progressDialog.dismiss();
                            }
                        }, 1500);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        senders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateListView_Senders();
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Database functions for current and history
     *
     * @param request Request what database to use/method
     * @param Options Options for databases if apply (TinyDB example)
     * @param Entry   Data to pass for entry to a database or settings file
     * @param history True or False to remove history database
     * @param DataInt Integer Data to be pass for entry to a database or settings file
     */
    private String data_exec(String request, String Options, String Entry, Boolean history, Integer DataInt, String DataString) {

        final History sql_history = new History(this);
        final SQLite sql_current = new SQLite(this);
        final TinyDB tinyDB_pref = new TinyDB(this);
        final String Error = "No method found.";

        if (request.equals("tinyDB")) {
            if (Options.equals("remove")) {
                tinyDB_pref.remove(Entry);
            }
            if (Options.equals("putInt")) {
                tinyDB_pref.putInt(Entry, DataInt);
            }
            if (Options.equals("getInt")) {
                final Integer getInt = tinyDB_pref.getInt(Entry, 0);
                return String.valueOf(getInt);
            }
            if (Options.equals("putString")) {
                tinyDB_pref.putString(Entry, DataString);
            }
            if (Options.equals("getString")) {
                final String getString = tinyDB_pref.getString(Entry);
                return getString;
            }

        }
        if (request.equals("deleteBarcodes")) {
            if (history) {
                sql_history.deleteBarcodes();
                sql_history.close();
            }

            sql_current.deleteBarcodes();
            sql_current.close();

            badge.setText("0");
            CounterTxt.setText("0");
            tinyDB_pref.remove("SCAN_SAVED");
            tinyDB_pref.remove("SCAN_PREVIOUS_NAME");
            tinyDB_pref.remove("SCAN_PREVIOUS_COMPANY");
            tinyDB_pref.remove("SCAN_PREVIOUS_DEPARTMENT");
            tinyDB_pref.remove("SCAN_PREVIOUS_COUNT");
            tinyDB_pref.remove("MANUAL_PREVIOUS_NAME");
            tinyDB_pref.remove("MANUAL_PREVIOUS_COMPANY");
            tinyDB_pref.remove("MANUAL_PREVIOUS_DEPARTMENT");

        }

        return Error;
    }

    protected boolean checkFocus() {

        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ComponentName componentInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<ActivityManager.AppTask> tasks = manager.getAppTasks();
            componentInfo = tasks.get(0).getTaskInfo().topActivity;
        } else {
            List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
            componentInfo = tasks.get(0).topActivity;
        }

        if (!componentInfo.getPackageName().equals(getApplicationContext().getPackageName())) {
            //Log.e(TAG, "True2");
            return true;
        }

        return false;
    }

    /**
     * Buttons / Misc.
     */

    public void FloatingActionButtons() {

        final FloatingActionButton fabHistory = (FloatingActionButton) findViewById(R.id.fabHistory);
        final FloatingActionButton fabScan = (FloatingActionButton) findViewById(R.id.fabScan);
        final FloatingActionButton fabMan = (FloatingActionButton) findViewById(R.id.fabManual);
        final FloatingActionButton fabSend = (FloatingActionButton) findViewById(R.id.fabEmail);
        final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fabClearHistory);
        final FloatingActionButton fabAbout = (FloatingActionButton) findViewById(R.id.fabAbout);

        fabClear.setVisibility(View.INVISIBLE);
        fabMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ListView sender = (ListView) findViewById(R.id.sender_data);
                final ListView current = (ListView) findViewById(R.id.listView);
                if (sender.getVisibility() == View.VISIBLE) {
                    sender.setVisibility(View.GONE);
                    current.setVisibility(View.VISIBLE);
                    Snackbar.make(current, "Switched to Current", Snackbar.LENGTH_LONG).setDuration(2000).show();
                }
                InputManual(false);
            }
        });
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ListView sender = (ListView) findViewById(R.id.sender_data);
                final ListView current = (ListView) findViewById(R.id.listView);
                if (sender.getVisibility() == View.VISIBLE) {
                    sender.setVisibility(View.GONE);
                    current.setVisibility(View.VISIBLE);
                    Snackbar.make(current, "Switched to Current", Snackbar.LENGTH_LONG).setDuration(2000).show();
                }
                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.initiateScan();
            }
        });
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ListView sender = (ListView) findViewById(R.id.sender_data);
                final ListView current = (ListView) findViewById(R.id.listView);
                if (sender.getVisibility() == View.VISIBLE) {
                    sender.setVisibility(View.GONE);
                    current.setVisibility(View.VISIBLE);
                    Snackbar.make(current, "Switched to Current", Snackbar.LENGTH_LONG).setDuration(2000).show();
                }
                ClearButton();
            }
        });
        fabHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryButton();
            }
        });
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClearHistoryButton();
            }
        });
        fabAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ListView sender = (ListView) findViewById(R.id.sender_data);
                final ListView current = (ListView) findViewById(R.id.listView);
                if (sender.getVisibility() == View.VISIBLE) {
                    sender.setVisibility(View.GONE);
                    current.setVisibility(View.VISIBLE);
                    Snackbar.make(current, "Switched to Current", Snackbar.LENGTH_LONG).setDuration(2000).show();
                }
                Snackbar.make(view, "Created by Michael Jimenez, for The Coeur D\'Alene Resort", Snackbar.LENGTH_LONG).setDuration(2500).show();
                user_logout();
            }
        });
    }

    private void ClearButton() {
        if (Integer.valueOf(CounterTxt.getText().toString()).equals(0)) {
            ScanDataEmpty();
        }
        if (Integer.valueOf(CounterTxt.getText().toString()) >= 1) {
            final ExportDatabaseCSVTask task = new ExportDatabaseCSVTask();
            task.execute();
            emailResults();
            ClearButtonData();
        }
    }

    private void ClearButtonData() {
        final String[] log = new String[200];
        Arrays.fill(log, "null");

        data_exec("deleteBarcodes", null, null, false, null, null);

        CreateListView();

    }

    private void HistoryButton() {
        ListView view;
        view = (ListView) findViewById(R.id.listView_history);

        if (view.getVisibility() == View.GONE) {
            historyData.Show();
            final History db = new History(MainActivity.this);
            final Cursor cursor = db.getBarcodesRaw();

            HistoryCounter = cursor.getCount();
            badge_history.setText(String.valueOf(HistoryCounter));
            badge_history.setTextSize(16);
            badge_history.setBackgroundColor(Color.TRANSPARENT);
            badge_history.setTextColor(Color.BLACK);
            badge_history.show();

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this,
                    android.R.layout.two_line_list_item,
                    cursor,
                    new String[]{"Barcode", "Listview"},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0);

            view.setDivider(null);
            view.setSelector(android.R.color.transparent);
            view.setAdapter(adapter);
            db.close();

        } else {
            historyData.Hide();
        }
    }

    private void ClearHistoryData() {
        data_exec("deleteBarcodes", null, null, true, null, null);
        CreateListView();
        Snackbar.make(getWindow().getDecorView().getRootView(), "Deleted History", Snackbar.LENGTH_LONG).setDuration(2000).show();
    }

    private void ClearHistoryButton() {
        historyData.Hide();
        ClearHistoryData();
    }

    private void DisplayDebug(View view, String Barcode, String Company, String Name, String Department, String Username, String PackageCount) {
        // View getWindow().getDecorView().getRootView()
        Snackbar.make(view, "Barcode: " + Barcode + ", Company: " + Company + ", Name: " + Name + ", Department: " + Department + ", Username: " + Username + ", Package Count: " + PackageCount, Snackbar.LENGTH_LONG).setDuration(20000).show();
    }

    /**
     * Error Message
     */

    public void ScanDataEmpty() {
        Snackbar.make(getWindow().getDecorView().getRootView(), "No scan data received!", Snackbar.LENGTH_LONG).setDuration(2000).show();
    }

    private class HistoryData {

        /**
         * Hide History Buttons
         * Show Current Data
         */

        void Hide() {
            final ListView listView;
            final ListView hideView;
            final TextView textView;
            final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fabClearHistory);

            listView = (ListView) findViewById(R.id.listView_history);
            hideView = (ListView) findViewById(R.id.listView);
            textView = (TextView) findViewById(R.id.historyCounter);

            hideView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            fabClear.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            badge_history.hide();
            Snackbar.make(listView, "Switched to Current", Snackbar.LENGTH_LONG).setDuration(2000).show();
        }

        /**
         * Show History Buttons
         * Hide Current Data
         */

        void Show() {
            final ListView listView;
            final ListView hideView;
            final ListView sender;
            final TextView textView;
            final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fabClearHistory);

            listView = (ListView) findViewById(R.id.listView_history);
            hideView = (ListView) findViewById(R.id.listView);
            textView = (TextView) findViewById(R.id.historyCounter);
            sender = (ListView) findViewById(R.id.sender_data);
            listView.setVisibility(View.VISIBLE);
            hideView.setVisibility(View.GONE);
            fabClear.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            sender.setVisibility(View.GONE);
            Snackbar.make(listView, "Switched to History", Snackbar.LENGTH_LONG).setDuration(2000).show();

        }
    }

    private class Preferences {


        void Save() {
            // Log.i(TAG, "Saved");

            CounterTxtSet = (TextView) findViewById(R.id.textView2);
            Integer value = Integer.parseInt(CounterTxtSet.getText().toString());

            data_exec("tinyDB", "remove", "counter", false, null, null);
            data_exec("tinyDB", "putInt", "counter", false, value, null);
        }

        void Load() {
            // Log.i(TAG,"Loaded");

            CounterTxtSave = (TextView) findViewById(R.id.textView2);
            UserLog = (TextView) findViewById(R.id.UserLog);

            String val = data_exec("tinyDB", "getInt", "counter", false, null, null);
            final String logged_a = data_exec("tinyDB", "getString", "LOGGED_ACTUAL", false, null, null);
            final String logged_b = data_exec("tinyDB", "getString", "LOGGED_IN", false, null, null);

            badge.setText(val);
            badge.setTextSize(16);
            badge.setBackgroundColor(Color.TRANSPARENT);
            badge.setTextColor(Color.BLACK);
            badge.show();

            CounterTxtSave.setText(val);
            Counter = Integer.valueOf(val);

            UserLog.setText("User: " + logged_a + " - " + logged_b);
            data_exec("tinyDB", "remove", "counter", false, null, null);

            CreateListView();
        }
    }

    /**
     * Exporting to Spreadsheet & Emailing
     */

    private class ExportDatabaseCSVTask extends AsyncTask<String, String, String> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
        }

        protected String doInBackground(final String... args) {
            //  Environment.getExternalStorageDirectory()
            //  /storage/emulated/0/
            final Calendar cal = Calendar.getInstance();
            final SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            final String formattedDate = df.format(cal.getTime());
            final File exportDir = new File(Environment.getExternalStorageDirectory(), "");
            final File exportDir2 = new File(Environment.getExternalStorageDirectory(), "Receiving-Data-" + formattedDate + ".csv");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            //Log.v(TAG, "" + exportDir);

            final String Random = UUID.randomUUID().toString();
            final String Trim = Random.substring(0, 8);
            final File file = new File(exportDir, "Receiving-Data-" + formattedDate + "-" + Trim + ".csv");
            try {

                file.createNewFile();
                final CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                final SQLite db = new SQLite(MainActivity.this);
                final SQLiteDatabase X = db.getReadableDatabase();
                final Cursor c;
                final String TABLE_NAME = "Barcodes";
                c = X.rawQuery("SELECT _id , Barcode, Company, Name, Department, Count FROM " + TABLE_NAME, null);
                c.moveToFirst();
                String arrStr1[] = {"#", "Barcode", "Company", "Name", "Department", "Count"};

                csvWrite.writeNext(arrStr1);
                while (!c.isAfterLast()) {
                    // Log.v(TAG, "" + c.getString(c.getColumnIndex(KEY_ID)));
                    // Log.v(TAG, "" + c.getString(c.getColumnIndex(KEY_Product)));
                    // Log.v(TAG, "" + c.getString(c.getColumnIndex(KEY_Count)));
                    final String[] fields = getFieldsAsStringArray(c);
                    csvWrite.writeNext(fields);
                    c.moveToNext();
                }
                csvWrite.close();
                c.close();
                copy(file, exportDir2);
                return "";
            } catch (IOException e) {
                // Log.e("MainActivity", e.getMessage(), e);
                return "";
            }
        }

        private String[] getFieldsAsStringArray(Cursor cursor) {
            final int columnCount = cursor.getColumnCount();
            final String[] result = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                result[i] = cursor.getString(i);
            }
            return result;
        }

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(final String success) {

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success.isEmpty()) {
                //Toast.makeText(MainActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainActivity.this, "Export failed!", Toast.LENGTH_SHORT).show();
            }
        }

        protected void copy(File src, File dst) throws IOException {
            final InputStream in = new FileInputStream(src);
            try {
                final OutputStream out = new FileOutputStream(dst);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        }
    }
}