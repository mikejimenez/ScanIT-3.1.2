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
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import alpha.com.ScanIT.databases.TinyDB;
import alpha.com.ScanIT.interfaces.Barcodes;
import alpha.com.ScanIT.interfaces.FormatString;

public class MainActivity extends Activity {
    //Todo: Add load old scan history for current
    //Todo: Convert dialogs to ButtKnife
    private static final String TAG = "MAKE_BARCODES";
    private final String[] log = new String[200];
    /**
     * Private Strings
     */

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
    String DataUpdate2;
    String Error = "";

    Integer HistoryCounter;
    Integer Counter = 0;

    BadgeView badge;
    BadgeView badge_history;

    View setCount;
    View setCount_history;


    /**
     *          Completed
     *  HideHistory(), ShowHistory()
     *  user_logout(), HistoryButton()
     *  loadPreferences(), savePreferences()
     *  ClearButtonData(), ClearHistoryData()
     *  ClearHistoryButton(), ClearButton()
     *  ExportDatabaseCSVTask()
     */

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

        final FloatingActionButton fabHistory = (FloatingActionButton) findViewById(R.id.fabHistory);
        final FloatingActionButton fabScan = (FloatingActionButton) findViewById(R.id.fabScan);
        final FloatingActionButton fabMan = (FloatingActionButton) findViewById(R.id.fabManual);
        final FloatingActionButton fabSend = (FloatingActionButton) findViewById(R.id.fabEmail);
        final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fabClearHistory);
        final FloatingActionButton fabAbout = (FloatingActionButton) findViewById(R.id.fabAbout);

        CounterTxt = (TextView) findViewById(R.id.textView2);
        setCount = findViewById(R.id.TotalCounter);
        setCount_history = findViewById(R.id.historyCounter);
        badge = new BadgeView(this, setCount);
        badge_history = new BadgeView(this, setCount_history);

        loadPreferences();
        fabClear.setVisibility(View.INVISIBLE);
        fabMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputManual();
            }
        });
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.initiateScan();
            }
        });
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                Snackbar.make(view, "Created by Michael Jimenez, for The Coeur D\'Alene Resort", Snackbar.LENGTH_LONG).setDuration(2500).show();
                user_logout();
            }
        });
    }

    /**
     * onStop - when activity stops
     * ----------------------------
     * - Save preferences
     * ----------------------------
     */
//ToDO Don't think it is working test
    protected boolean checkFocus() {
        String PackageName = "com.google.zxing.client.android";
        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ComponentName componentInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            List<ActivityManager.AppTask> tasks = manager.getAppTasks();
            componentInfo = tasks.get(0).getTaskInfo().topActivity;
        }
        else
        {
            List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
            componentInfo = tasks.get(0).topActivity;
        }

        if (componentInfo.getPackageName().equals(PackageName)) {
            //Log.e(TAG, "True2");
            return true;
        }

        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();

        if (checkFocus()) {
            //Log.e(TAG, "True");
            }
        else {
            startService(new Intent(this, Services.class));
        }

    }

    /**
     * onActivityResult - scanning
     * ---------------------------
     * - Creates data on screen
     * - Writes to database
     * ---------------------------
     */

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            @SuppressLint("InflateParams")

            final String scanContent = scanningResult.getContents();
            final LayoutInflater Results = LayoutInflater.from(this);
            final View textEntryView = Results.inflate(R.layout.scan_entry, null);
            final EditText infoCompany = (EditText) textEntryView.findViewById(R.id.infoCompany_scan);
            final EditText infoName = (EditText) textEntryView.findViewById(R.id.infoName_scan);
            final Spinner spinner = (Spinner) textEntryView.findViewById(R.id.infoDepartment_scan);
            final Spinner spinner_ = (Spinner) textEntryView.findViewById(R.id.infoPackage_count);
            final SQLite db = new SQLite(this);
            final History db2 = new History(this);
            final TinyDB tinydb = new TinyDB(this);
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            /**
             * Spinner Data
             *
             */

            /**
             *  Department listing
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
            for (int i=1; i<=100; i++){
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



            alert.setIcon(R.drawable.perm_group_user_dictionary).setTitle("Information").setView(textEntryView).setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            Error = "";


                            final String infoBarcode = Filter(scanContent);
                            final String scanCompany = infoCompany.getText().toString();
                            final String scanName = infoName.getText().toString();
                            final String Department = SpinnerValue;
                            final String PackageCount = SpinnerValue_;
                            final String Username = "User: " + tinydb.getString("LOGGED_ACTUAL") + " - " + tinydb.getString("LOGGED_IN");
                            final String ListView = scanCompany + " - " + scanName + " - " + Department + " - " + PackageCount;

                            if (!Error.contains("Y")) {
                                db.addBarcodes(new Barcodes(infoBarcode, scanCompany, scanName, Department, Username, ListView, PackageCount));
                                db2.addBarcodes(new Barcodes(infoBarcode, scanCompany, scanName, Department, Username, ListView, PackageCount));
                                Counter++;
                                db.close();
                                db2.close();
                                CreateListView();
                                String setText = Integer.valueOf(Counter).toString();
                                CounterTxt.setText(setText);
                                badge.increment(1);
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
        } else if (resultCode == RESULT_CANCELED) {
            ScanDataEmpty();
        }
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
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CreatePopup(id);
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
        final SQLite db5 = new SQLite(this);
        final History db4 = new History(this);
        //final TinyDB tinydb = new TinyDB(this);
        final Long _id = id;
        final SQLiteDatabase X = db.getReadableDatabase();
        final SQLiteDatabase X2 = db2.getReadableDatabase();
        final SQLiteDatabase X3 = db3.getReadableDatabase();
        final SQLiteDatabase X4 = db5.getReadableDatabase();
        final Cursor c;
        final Cursor c2;
        final Cursor c3;
        final Cursor c4;

        builder.setView(Manual);
        final AlertDialog alertDialog = builder.create();

        c = X.rawQuery("SELECT Company FROM Barcodes WHERE _id =" + _id, null);
        c2 = X2.rawQuery("SELECT Department FROM Barcodes WHERE _id =" + _id, null);
        c3 = X3.rawQuery("SELECT Name FROM Barcodes WHERE _id =" + _id, null);
        c4 = X4.rawQuery("SELECT Count FROM Barcodes WHERE _id =" + _id, null);

        c.moveToFirst();
        c2.moveToFirst();
        c3.moveToFirst();
        c4.moveToFirst();

        final String Company = c.getString(c.getColumnIndexOrThrow("Company"));
        final String Department = c2.getString(c2.getColumnIndexOrThrow("Department"));
        final String Name = c3.getString(c3.getColumnIndexOrThrow("Name"));
        final String OldCount = c4.getString(c4.getColumnIndexOrThrow("Count"));

        c.close();
        c2.close();
        c3.close();
        c4.close();

        DataUpdate2 = Company + " - " + Name + " - " + Department + " - " + OldCount;

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
        for (int i=1; i<=100; i++){
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
            //Todo: Add User Check or OVERIDE
            //Todo: Assistant can't update.
            //Todo: Admin can.

            @Override
            public void onClick(View view) {
                if (infoData.length() != 0) {
                    final Editable Data = infoData.getText();
                    final String Name = infoDataName.getText().toString();
                    final String Count = SpinnerValue_;
                    final String ListView = Data + " - " + Name + " - " + SpinnerValue + " - " + Count;

                    db.UpdateRecord(Data, _id, Editable.Factory.getInstance().newEditable(SpinnerValue), Name, ListView, Count);
                    db.close();
                    db4.UpdateRecord(Data, _id, Editable.Factory.getInstance().newEditable(SpinnerValue), Name, ListView, Count);
                    db4.close();
                    alertDialog.dismiss();
                    CreateListView();
                    //Log.e(TAG, "Old: " + DataUpdate2 + " - New: " + ListView);
                } else {
                    Snackbar.make(Manual, "Please check input", Snackbar.LENGTH_LONG).setDuration(2300).show();
                    //Toast toast = Toast.makeText(getApplicationContext(),
                    //        "Please check input", Toast.LENGTH_SHORT);
                    //toast.show();

                }
            }
        });
        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: Add User Check or OVERRIDE
                //Todo: Assistant can't delete
                //Todo: Admin can.
                db.DeleteRecord(_id);
                db4.DeleteRecord(_id);
                alertDialog.dismiss();
                CreateListView();
                db.close();
                db4.close();
                badge.decrement(1);
            }
        });

        departmentTxt.setText(DataUpdate2);
        //builder.setTitle("Update Scan").setView(Manual);
        alertDialog.show();
        db.close();
        db2.close();
        db3.close();
        db4.close();
        db5.close();
    }


    /**
     * Manual Functions
     */


    public void onDestroyView(View view) {

        if (view != null) {
            ViewGroup parentViewGroup = (ViewGroup) view.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
    }

    private void InputManual() {

        LayoutInflater Manual = LayoutInflater.from(this);
        @SuppressLint("AndroidLintInflateParams")

        final View textEntryView = Manual.inflate(R.layout.manual_entry, null);
        final EditText infoTrack = (EditText) textEntryView.findViewById(R.id.infoTracking_manual);
        final EditText infoData = (EditText) textEntryView.findViewById(R.id.infoCompany_manual);
        final EditText infoSender = (EditText) textEntryView.findViewById(R.id.infoName_manual);
        final Spinner spinner = (Spinner) textEntryView.findViewById(R.id.infoDepartment_manual);
        final Spinner spinner_ = (Spinner) textEntryView.findViewById(R.id.infoPackage_count_);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final TinyDB tinydb = new TinyDB(this);


        /**
         * Spinner Data
         *
         */

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
        for (int i=1; i<=100; i++){
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

        alert.setIcon(R.drawable.ic_dialog_alert_holo_light).setTitle("Tracking Number").setView(textEntryView).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        final String infoCompany = infoData.getText().toString();
                        final String infoTracking = infoTrack.getText().toString();
                        final String infoName = infoSender.getText().toString();
                        final String infoUsername = "User: " + tinydb.getString("LOGGED_ACTUAL") + " - " + tinydb.getString("LOGGED_IN");
                        final String Department = SpinnerValue;
                        final String Count = SpinnerValue_;
                        final String ListView = infoCompany + " - " + infoName + " - " + Department + " - " + Count;
                        String Done = "";

                        if (infoTrack.length() == 0) {
                            ScanDataEmpty();
                        }

                        /**
                         *      Fedex Ground Manual (No Spaces) - 21 Characters
                         *
                         *      Format: XXXXXXXXXXXXXXXXXXXXX
                         *
                         */

                        if (infoTrack.length() == 21 && TextUtils.isDigitsOnly(infoTrack.getText())) {
                            mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
                            Done = "1";
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
                                Done = "1";
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
                                Done = "1";
                            }
                            if (infoTrack.getText().toString().contains("1Z") || infoTrack.getText().toString().contains("1z")) {
                                mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
                                Done = "1";
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
                            Done = "1";
                        }

                        /**
                         *      Fedex Express Manual (Spaces) - 14 Characters
                         *
                         *      Format: XXXX XXXX XXXX
                         *
                         */

                        if (infoTrack.length() == 14 && TextUtils.isDigitsOnly(infoTrack.getText().toString().replaceAll("\\s+", ""))) {
                            mAdd(infoTracking, infoCompany, infoName, Department, infoUsername, ListView, Count);
                            Done = "1";
                        } else if (!Done.equals("1")) {
                            ScanDataEmpty();
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

        db.addBarcodes(new Barcodes(Result, Company, Name, Department, Username, ListView, Count));
        db2.addBarcodes(new Barcodes(Result, Company, Name, Department, Username, ListView, Count));
        //DisplayDebug(getWindow().getDecorView().getRootView(), Result, Company, Name, Department, Username, Count);
        Counter++;
        db.close();
        db2.close();
        UpdateLog();
        CreateListView();
        CounterTxt.setText(String.format(Integer.valueOf(Counter).toString()));
        badge.increment(1);
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

        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("/storage/emulated/0/Receiving-Data-" + formattedDate + ".csv")));
        i.putExtra(Intent.EXTRA_SUBJECT, "Tracking Numbers - " + formattedDate + "");
        i.putExtra(Intent.EXTRA_EMAIL, TO);
        UpdateLog();

        final String newString = Arrays.toString(log);
        final String FilterA = newString.replace(", null", "");
        final String FilterB = FilterA.replace("[", "");
        final String FilterC = FilterB.replace("]", "");
        final String FilterD = FilterC.replace(",", "");

        i.putExtra(Intent.EXTRA_TEXT, FilterD);
        Arrays.fill(log, null);
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

        int i = 0;
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
                // Old
                // log[i] = "\n" + cn.getDepartment() + " " + Output + " / " + cn.getCompany();
                // log[i] = "\n" + cn.getCompany() + " - " + cn.getName() + " - " + cn.getDepartment() + " : " + Output + " \n";
                //log[i] = "\n " + cn.getCompany() + " : " + cn.getDepartment() + "\n " + cn.getName() + " : " + Output + " \n";
                log[i] = "\nName: " + cn.getName()
                        + "\n" + "Company: " + cn.getCompany()
                        + "\n" + "Department: " + cn.getDepartment() + "\n" + "Tracking: "
                        + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                i++;
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
                    //log[i] = "\n" + cn.getCompany() + " - " + cn.getName() + " - " + cn.getDepartment() + " : " + Output + " \n";
                    //log[i] = "\n " + cn.getCompany() + " : " + cn.getDepartment() + "\n " + cn.getName() + " : " + Output + " \n";
                    log[i] = "\nName: " + cn.getName()
                            + "\n" + "Company: " + cn.getCompany()
                            + "\n" + "Department: " + cn.getDepartment() + "\n" + "Tracking: "
                            + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                    i++;
                }
            }
            /**
             *
             * Fedex Ground OLD (Scanner) - 22 Characters
             *
             */

            if (bar.length() == 22) {
                Output = FormatString.FedEXGO(bar);
                //log[i] = "\n" + cn.getCompany() + " - " + cn.getName() + " - " + cn.getDepartment() + " : " + Output + " \n";
                //log[i] = "\n " + cn.getCompany() + " : " + cn.getDepartment() + "\n " + cn.getName() + " : " + Output + " \n";
                log[i] = "\nName: " + cn.getName()
                        + "\n" + "Company: " + cn.getCompany()
                        + "\n" + "Department: " + cn.getDepartment() + "\n" + "Tracking: "
                        + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                i++;
            }

            /**
             *      Fedex Ground Manual (No Spaces) - 21 Characters
             *
             *      Format: XXXXXXXXXXXXXXXXXXXXX
             */

            if (bar.length() == 21 && !bar.contains("Z")) {
                Output = FormatString.ManualFedEXG(bar);
                //log[i] = "\n" + cn.getCompany() + " - " + cn.getName() + " - " + cn.getDepartment() + " : " + Output + " \n";
               // log[i] = "\n " + cn.getCompany() + " : " + cn.getDepartment() + "\n " + cn.getName() + " : " + Output + " \n";
                log[i] = "\nName: " + cn.getName()
                        + "\n" + "Company: " + cn.getCompany()
                        + "\n" + "Department: " + cn.getDepartment() + "\n" + "Tracking: "
                        + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                i++;
            }

            /**
             *      Fedex Express Manual (Spaces) - 14 Characters
             *
             *      Format: XXXX XXXX XXXX
             *
             */

            if (bar.length() == 14) {
                Output = FormatString.ManualFedEXE(bar);
                //log[i] = "\n" + cn.getCompany() + " - " + cn.getName() + " - " + cn.getDepartment() + " : " + Output + " \n";
                //log[i] = "\n " + cn.getCompany() + " : " + cn.getDepartment() + "\n " + cn.getName() + " : " + Output + " \n";
                log[i] = "\nName: " + cn.getName()
                        + "\n" + "Company: " + cn.getCompany()
                        + "\n" + "Department: " + cn.getDepartment() + "\n" + "Tracking: "
                        + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                i++;
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
                //log[i] = "\n" + cn.getCompany() + " - " + cn.getName() + " - " + cn.getDepartment() + " : " + Output + " \n";
                //log[i] = "\n " + cn.getCompany() + " : " + cn.getDepartment() + "\n " + cn.getName() + " : " + Output + " \n";
                log[i] = "\nName: " + cn.getName()
                        + "\n" + "Company: " + cn.getCompany()
                        + "\n" + "Department: " + cn.getDepartment() + "\n" + "Tracking: "
                        + Output + "\n" + "Packages: " + cn.getCount() + "\n";
                i++;
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
        if (Number.length() == 16) {

            /**
             * Scanner
             * System.out.println("Added Type Fedex Express US Airbill Paper");
             */

            ScanFromFedEXEAir = Number.substring(Number.length() - 16, Number.length() - 4);
            return ScanFromFedEXEAir;
        }
        // if (Number.length() == 12) { ScanFromFedEXE = Number.substring(Number.length() - 12, Number.length()); return ScanFromFedEXE; }
        if (Number.contains("1Z")) {
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
     * Error Message
     */

    private void ScanDataEmpty() {
        final ListView listView;
        listView = (ListView) findViewById(R.id.listView);

        Snackbar.make(listView, "No scan data received!", Snackbar.LENGTH_LONG).setDuration(2000).show();
    }

    /**
     * Hide History Buttons
     * Show Current Data
     */
    private void HideHistory() {
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

    private void ShowHistory() {
        final ListView listView;
        final ListView hideView;
        final TextView textView;
        final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fabClearHistory);

        listView = (ListView) findViewById(R.id.listView_history);
        hideView = (ListView) findViewById(R.id.listView);
        textView = (TextView) findViewById(R.id.historyCounter);
        listView.setVisibility(View.VISIBLE);
        hideView.setVisibility(View.GONE);
        fabClear.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        Snackbar.make(listView, "Switched to History", Snackbar.LENGTH_LONG).setDuration(2000).show();
    }

    /**
     * Save / Load State Data
     */
    private void user_logout() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final RelativeLayout customView = (RelativeLayout) View.inflate(this, R.layout.user_logout, null);
        final Button logout = (Button) customView.findViewById(R.id.logout);
        final Button cancel = (Button) customView.findViewById(R.id.cancel);
        final TinyDB tinydb = new TinyDB(this);

        builder.setView(customView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tinydb.remove("LOGIN_SKIP");
                tinydb.remove("LOGGED_IN");
                tinydb.remove("LOGGED_ACTUAL");
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

    }

    private void loadPreferences() {
        final TinyDB tinydb = new TinyDB(this);

        CounterTxtSave = (TextView) findViewById(R.id.textView2);
        UserLog = (TextView) findViewById(R.id.UserLog);

        String val = String.valueOf(tinydb.getInt("counter", 0));

        badge.setText(val);
        badge.setTextSize(16);
        badge.setBackgroundColor(Color.TRANSPARENT);
        badge.setTextColor(Color.BLACK);
        badge.show();

        CounterTxtSave.setText(val);
        Counter = Integer.valueOf(val);

        UserLog.setText("User: " + tinydb.getString("LOGGED_ACTUAL") + " - " + tinydb.getString("LOGGED_IN"));
       // tinydb.remove("counter");

        CreateListView();
        UpdateLog();

    }

    private void savePreferences() {
        final TinyDB tinydb = new TinyDB(this);
        tinydb.remove("counter");
        CounterTxtSet = (TextView) findViewById(R.id.textView2);
        Integer value = Integer.parseInt(CounterTxtSet.getText().toString());
        tinydb.putInt("counter", value);
    }

    /**
     * Buttons / Misc.
     */

    private void ClearButton() {
        if (Counter == 0) {
            ScanDataEmpty();
        } else {
            final ExportDatabaseCSVTask task = new ExportDatabaseCSVTask();
            task.execute();
            emailResults();
            ClearButtonData();
        }
    }

    private void ClearButtonData() {
        final SQLite db = new SQLite(this);

        db.deleteBarcodes();
        CreateListView();
        db.close();

        CounterTxt.setText("0");
        badge.setText("0");
    }

    private void HistoryButton() {
        final ListView listView;
        listView = (ListView) findViewById(R.id.listView_history);


        if (listView.getVisibility() == View.GONE) {
            ShowHistory();
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

            listView.setDivider(null);
            listView.setSelector(android.R.color.transparent);
            listView.setAdapter(adapter);
            db.close();

        } else {
            HideHistory();
        }
    }

    private void ClearHistoryData() {
        final History db = new History(this);
        final SQLite db2 = new SQLite(this);
        ListView listView;

        listView = (ListView) findViewById(R.id.listView);
        db.deleteBarcodes();
        db2.deleteBarcodes();
        db.close();
        db2.close();
        CreateListView();
        Snackbar.make(listView, "Deleted History", Snackbar.LENGTH_LONG).setDuration(2000).show();
    }

    private void ClearHistoryButton() {
        //Todo Add Permission checking to delete history
        //Todo Such as Assistant can't delete.
        //Todo Admin can.
        HideHistory();
        ClearHistoryData();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    public void DisplayDebug(View view, String Barcode, String Company, String Name, String Department, String Username, String PackageCount) {
        // View getWindow().getDecorView().getRootView()
        Snackbar.make(view, "Barcode: " + Barcode + ", Company: " + Company + ", Name: " + Name + ", Department: " + Department + ", Username: " + Username + ", Package Count: " + PackageCount, Snackbar.LENGTH_LONG).setDuration(20000).show();
    }

    public void copy(File src, File dst) throws IOException {
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
                final String KEY_ID = "_id";
                final String KEY_Product = "Barcode";
                final String KEY_Count = "Company";
                //c = X.rawQuery("SELECT * FROM " + TABLE_NAME, null);
                c = X.rawQuery("SELECT _id , Barcode, Company, Name, Department, Count FROM " + TABLE_NAME, null);
                c.moveToFirst();
                String arrStr1[] = {"#", "Barcode", "Company", "Name", "Department", "Count"};

                csvWrite.writeNext(arrStr1);
                while (!c.isAfterLast()) {
                    Log.v(TAG, "" + c.getString(c.getColumnIndex(KEY_ID)));
                    Log.v(TAG, "" + c.getString(c.getColumnIndex(KEY_Product)));
                    Log.v(TAG, "" + c.getString(c.getColumnIndex(KEY_Count)));
                    final String[] fields = getFieldsAsStringArray(c);
                    csvWrite.writeNext(fields);
                    c.moveToNext();
                }
                csvWrite.close();
                c.close();
                copy(file, exportDir2);
                return "";
            } catch (IOException e) {
                Log.e("MainActivity", e.getMessage(), e);
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
                // Toast.makeText(MainActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
            } else {
                //  Toast.makeText(MainActivity.this, "Export failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}