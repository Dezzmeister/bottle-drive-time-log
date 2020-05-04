package com.troop6quincy.bottledrivetimelog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.troop6quincy.bottledrivetimelog.checkout.CheckOutDialogFragment;
import com.troop6quincy.bottledrivetimelog.checkout.CheckOutDialogListener;
import com.troop6quincy.bottledrivetimelog.deletescout.DeleteScoutDialogFragment;
import com.troop6quincy.bottledrivetimelog.deletescout.DialogListener;
import com.troop6quincy.bottledrivetimelog.export.CSVExporter;
import com.troop6quincy.bottledrivetimelog.export.ExcelExporter;
import com.troop6quincy.bottledrivetimelog.menufunctions.ClearEntriesDialogFragment;
import com.troop6quincy.bottledrivetimelog.menufunctions.SetTotalMoneyActivity;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Main activity, contains a list of currently checked-in Scouts.
 *
 * @author Joe Desmond
 * @since 1.0
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements DialogListener, CheckOutDialogListener {

    /**
     * Local path to the serialized previous session (local to internal app storage)
     */
    private static final String SCOUT_RECORD_NAME = "bottle-drive-records.joe";

    /**
     * Full path to the serialized previous session
     */
    private File recordFile;

    /**
     * Main entry list
     */
    private ListView scoutListView;

    /**
     * Current session data: list entries and total money earned
     */
    private SessionObject session;

    /**
     * Adapter to display entries
     */
    private ArrayAdapter<Scout> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cleanExternalStorage();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        final File dir = getApplicationContext().getFilesDir();
        recordFile = new File(dir, SCOUT_RECORD_NAME);

        if (recordFile.exists()) {
            session = SessionObject.loadPreviousSession(this, recordFile);
        } else {
            session = new SessionObject(new LinkedList<>(), -1);
        }

        scoutListView = findViewById(R.id.scoutNameView);
        adapter = new ScoutAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, session.listItems);
        scoutListView.setAdapter(adapter);

        registerForContextMenu(scoutListView);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            final Intent intent = new Intent(view.getContext(), CheckInActivity.class);
            ((Activity) view.getContext()).startActivityForResult(intent, ActivityRequests.CHECKIN_NEW_REQUEST);
        });
    }

    /**
     * Deletes all files and folders in the external storage. When a user exports a file, it is saved
     * to external storage. Another intent is created to send the file, so the file cannot be deleted
     * after starting a new activity. To prevent temporary export files from accumulating in external storage, the
     * external storage is cleared in {@link #onCreate(Bundle)}.
     */
    private void cleanExternalStorage() {
        final File dir = getExternalFilesDir(null);
        final File[] files = dir.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];

                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    /**
     * Deletes a directory and all files/directories within that directory.
     *
     * @param directory directory to be deleted
     */
    private void deleteDirectory(final File directory) {
        final File[] files = directory.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];

                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        directory.delete();
    }

    /**
     * Runs when the user clicks the positive option in a dialog box. This happens either when the user
     * confirms that they want to delete a single entry, or when the user confirms that they want to delete
     * all entries. The ID of the dialog box is provided as an integer argument to the dialog
     * fragment, and can be retrieved with {@linkplain DialogListener#DIALOG_ID_KEY this key}.
     *
     * @param dialog dialog box
     */
    @Override
    public void onDialogPositiveClick(final DialogFragment dialog) {
        final Bundle arguments = dialog.getArguments();
        final int dialogID = arguments.getInt(DialogListener.DIALOG_ID_KEY);

        switch (dialogID) {
            case DialogListener.CONFIRM_DELETE_SCOUT: {
                final Scout scout = (Scout) arguments.get(getResources().getString(R.string.scout_obj_key));
                removeItem(scout);

                final Toast toast = Toast.makeText(this, "Deleted " + scout.name + "!", Toast.LENGTH_SHORT);
                toast.show();
                break;
            }
            case DialogListener.CONFIRM_CLEAR_ENTRIES: {
                mainMenuClearEntries();
                break;
            }
        }
    }

    @Override
    public void onDialogNegativeClick(final DialogFragment dialog) {

    }

    /**
     * Runs when the user holds an entry in the list. Presents a menu giving the option to either
     * delete the entry, or check out the scout.
     *
     * @param menu menu
     * @param view view
     * @param menuInfo menu info
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View view, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scoutlist, menu);
    }

    /**
     * Runs when the user clicks one of the two menu options presented when holding an entry in the
     * list. If the "Check out" option is selected, this function will check the selected scout out
     * only if that scout has not checked out yet. If the "Delete" option is selected, this function
     * will delete the given entry from the list.
     *
     * @param item menu item, with info
     * @return true, if one of the two options is selected
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Scout selectedScout = (Scout) scoutListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.checkout_scout:
                if (selectedScout.checkOut == null) {
                    showScoutDialog(new CheckOutDialogFragment(), selectedScout, "CheckOutDialogFragment", 0);
                } else {
                    final Toast toast = Toast.makeText(this, selectedScout.name + " has already checked out!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            case R.id.delete_scout:
                showScoutDialog(new DeleteScoutDialogFragment(), selectedScout, "DeleteScoutDialogFragment", DialogListener.CONFIRM_DELETE_SCOUT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Shows a dialog box. If the dialog box is associated with a specific Scout, that Scout is
     * passed as an argument. A dialog ID is also passed as an argument so that event handler functions
     * know which dialog is active.
     *
     * @param dialog dialog fragment
     * @param scout associated Scout
     * @param dialogName dialog name
     * @param dialogID dialog ID
     */
    private void showScoutDialog(final DialogFragment dialog, final Scout scout, final String dialogName, final int dialogID) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(getResources().getString(R.string.scout_obj_key), scout);
        bundle.putInt(DialogListener.DIALOG_ID_KEY, dialogID);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), dialogName);
    }

    /**
     * Checks a scout out at the given checkout time. Updates the scout's record in the record file,
     * and saves the updated file.
     *
     * @param dialog dialog box checkout dialog box
     * @param picker checkout time picker
     * @param hour hour hour
     * @param minute minute minute
     */
    @Override
    public void onTimeSet(final DialogFragment dialog, final TimePicker picker, int hour, int minute) {
        final Bundle bundle = dialog.getArguments();
        final Scout scout = (Scout) bundle.get(getResources().getString(R.string.scout_obj_key));

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, hour, minute);

        scout.checkOut(calendar.getTime());
        checkOutScout(scout);
        final Toast toast = Toast.makeText(this, scout.name + " has checked out!", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Called when a child activity returns. This activity can create two activities: one to add a
     * Scout to the list, or one to set the total money earned. This callback handles both.
     *
     * @param aRequestCode intent request code (what the activity was asked to do)
     * @param aResultCode
     * @param aData
     */
    @Override
    protected void onActivityResult(final int aRequestCode, final int aResultCode, final Intent aData) {
        switch (aRequestCode) {
            case ActivityRequests.CHECKIN_NEW_REQUEST: {
                if (aResultCode == RESULT_OK) {
                    final Scout scout = (Scout) aData.getSerializableExtra(getResources().getString(R.string.scout_obj_key));
                    if (!tryAdd(scout)) {
                        final Toast toast = Toast.makeText(this, scout.name + " is already checked in!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                break;
            }
            case ActivityRequests.SET_TOTAL_MONEY_REQUEST: {
                if (aResultCode == RESULT_OK) {
                    final long currency = aData.getLongExtra(SetTotalMoneyActivity.SESSION_CURRENCY_KEY, -1);
                    session.totalMoney = currency;
                }

                break;
            }
        }
    }

    /**
     * Attempts to add a Scout to the list, fails if the Scout is already in the list.
     *
     * @param item Scout to add
     * @return true if the operation succeeded, false if not
     */
    private final boolean tryAdd(final Scout item) {
        for (Scout scout : session.listItems) {
            if (item.equals(scout)) {
                return false;
            }
        }

        addItem(item);

        return true;
    }

    /**
     * Adds a Scout to the list and updates the record file.
     *
     * @param item Scout to add
     */
    private final void addItem(final Scout item) {
        session.listItems.offerFirst(item);
        adapter.notifyDataSetChanged();
        session.save(this, recordFile);
    }

    /**
     * Removes a Scout from the list and updates the record file.
     *
     * @param item Scout to add
     */
    private final void removeItem(final Scout item) {
        session.listItems.remove(item);
        adapter.notifyDataSetChanged();
        session.save(this, recordFile);
    }

    /**
     * Checks a Scout out in the list and updates the record file. The checked-out Scout is moved to
     * the end of the queue. The entry is still displayed in the list, but it's grayed out. This
     * creates the visual effect of having all checked-in Scouts at the beginning of the list,
     * and all checked-out Scouts grayed out at the end of the list.
     * <p>
     * <b>NOTE: </b> This function does not update the Scout itself. To update the status of the Scout,
     * use {@link #onTimeSet(DialogFragment, TimePicker, int, int)}. The status of the Scout should
     * be updated before calling this function, otherwise the Scout will still appear to be checked in.
     *
     * @param scout Scout to check out
     */
    private final void checkOutScout(final Scout scout) {
        session.listItems.remove(scout);
        session.listItems.offerLast(scout);
        adapter.notifyDataSetChanged();
        session.save(this, recordFile);
    }

    /**
     * Clears the list of Scouts and deletes the record file.
     */
    private final void clearScoutList() {
        session.listItems.clear();
        adapter.notifyDataSetChanged();
        session.save(this, recordFile);

        if (recordFile.exists() && !recordFile.delete()) {
            final Toast toast = Toast.makeText(this, "Unable to delete session data!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Runs when one of the menu options in the main menu is selected.
     *
     * @param item selected menu item
     * @return result of {@link Activity#onOptionsItemSelected(MenuItem) super.onOptionsItemSelected(item)}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case R.id.main_menu_export_excel: {
                exportExcelXLSX();
                break;
            }
            case R.id.main_menu_export_excel_old: {
                exportExcelXLS();
                break;
            }
            case R.id.main_menu_export_text: {
                exportCSV();
                break;
            }
            case R.id.main_menu_set_money_earned: {
                mainMenuSetTotalMoney();
                break;
            }
            case R.id.main_menu_clear_entries: {
                if (!session.listItems.isEmpty()) {
                    showScoutDialog(new ClearEntriesDialogFragment(), null, "ClearEntriesDialogFragment", DialogListener.CONFIRM_CLEAR_ENTRIES);
                } else {
                    final Toast toast = Toast.makeText(this, "There are no entries!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens a new activity to set the currency.
     */
    private void mainMenuSetTotalMoney() {
        final Intent intent = new Intent(this, SetTotalMoneyActivity.class);
        intent.putExtra(SetTotalMoneyActivity.SESSION_CURRENCY_KEY, session.totalMoney);
        startActivityForResult(intent, ActivityRequests.SET_TOTAL_MONEY_REQUEST);
    }

    /**
     * Creates a CSV document with the list entries and creates an intent to allow another app to
     * send the document.
     */
    private void exportCSV() {
        final String document = CSVExporter.exportCSV(session.listItems, session.totalMoney / 100.0, ScoutAdapter.dateFormat);
        final String fileName = "timesheet-" + new SimpleDateFormat("MMM-d").format(new Date()) + ".csv";
        final File file = new File(getExternalFilesDir(null), fileName);

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(document);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Error while exporting CSV file!", Toast.LENGTH_SHORT);
            toast.show();
        }

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Export CSV using"));
    }

    /**
     * Creates an XLS spreadsheet with the list entries and creates an intent to allow another app to
     * send the document.
     */
    private void exportExcelXLS() {
        final HSSFWorkbook workbook = ExcelExporter.exportXLS(session.listItems, session.totalMoney / 100.0, "h:mm AM/PM, MMM d");
        final String fileName = "timesheet-" + new SimpleDateFormat("MMM-d").format(new Date()) + ".xls";
        final File file = new File(getExternalFilesDir(null), fileName);

        try (final FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Error while exporting XLS file!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.ms-excel");
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Export spreadsheet using"));
    }

    /**
     * Creates an XLSX spreadsheet with the list entries and creates an intent to allow another app
     * to send the document.
     */
    private void exportExcelXLSX() {
        final XSSFWorkbook workbook = ExcelExporter.exportXLSX(session.listItems, session.totalMoney / 100.0, "h:mm AM/PM, MMM d");
        final String fileName = "timesheet-" + new SimpleDateFormat("MMM-d").format(new Date()) + ".xlsx";
        final File file = new File(getExternalFilesDir(null), fileName);

        try (final FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Error while exporting XLSX file!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.ms-excel");
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Export spreadsheet using"));
    }

    /**
     * Clears all entries.
     */
    private void mainMenuClearEntries() {
        clearScoutList();

        final Toast toast = Toast.makeText(this, "Deleted all entries!", Toast.LENGTH_SHORT);
        toast.show();
    }
}
