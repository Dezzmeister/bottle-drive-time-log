package com.troop6quincy.bottledrivetimelog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.troop6quincy.bottledrivetimelog.checkout.CheckOutDialogFragment;
import com.troop6quincy.bottledrivetimelog.checkout.CheckOutDialogListener;
import com.troop6quincy.bottledrivetimelog.deletescout.DeleteScoutDialogFragment;
import com.troop6quincy.bottledrivetimelog.deletescout.DialogListener;
import com.troop6quincy.bottledrivetimelog.export.ExcelExporter;
import com.troop6quincy.bottledrivetimelog.menufunctions.ClearEntriesDialogFragment;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Main activity, contains a list of currently checked-in Scouts.
 *
 * @author Joe Desmond
 */
public class MainActivity extends AppCompatActivity implements DialogListener, CheckOutDialogListener {

    private static final String SCOUT_RECORD_NAME = "bottle-drive-records.txt";
    private File recordFile;

    private ListView scoutListView;
    private LinkedList<Scout> listItems = new LinkedList<Scout>();
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

        final File dir = getApplicationContext().getFilesDir();
        recordFile = new File(dir, SCOUT_RECORD_NAME);

        if (recordFile.exists()) {
            listItems = loadRecordFile(recordFile);
        } else {
            listItems = new LinkedList<>();
        }

        scoutListView = findViewById(R.id.scoutNameView);
        adapter = new ScoutAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listItems);
        scoutListView.setAdapter(adapter);

        registerForContextMenu(scoutListView);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            final Intent intent = new Intent(view.getContext(), CheckInActivity.class);
            ((Activity) view.getContext()).startActivityForResult(intent, CheckInActivity.CHECKIN_NEW_REQUEST);
        });
    }

    /**
     * Loads a record file at the given path. This is used to load the data in the previous session.
     * The file is just a serialized {@link LinkedList} of {@link Scout Scouts}.
     *
     * @param file path to the record file
     * @return list of Scouts
     */
    private LinkedList<Scout> loadRecordFile(final File file) {
        LinkedList<Scout> object = new LinkedList<>();

        try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            object = (LinkedList<Scout>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Unable to load previous session data!", Toast.LENGTH_SHORT);
            toast.show();
        }

        return object;
    }

    /**
     * Saves a record file to the given path. This is used to save data for the next session, so that
     * the user can exit the app and reopen it without losing the list of checked-in and checked-out
     * scouts.
     *
     * @param file file to save to
     * @param record list of scouts
     */
    private void saveRecordFile(final File file, final LinkedList<Scout> record) {
        try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(record);
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Unable to save session data!", Toast.LENGTH_SHORT);
            toast.show();
        }
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

    @Override
    protected void onActivityResult(final int aRequestCode, final int aResultCode, final Intent aData) {
        switch (aRequestCode) {
            case CheckInActivity.CHECKIN_NEW_REQUEST:
                if (aResultCode == RESULT_OK) {
                    final Scout scout = (Scout) aData.getSerializableExtra(getResources().getString(R.string.scout_obj_key));
                    if (!tryAdd(scout)) {
                        final Toast toast = Toast.makeText(this, scout.name + " is already checked in!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                break;
        }
    }

    /**
     * Attempts to add a Scout to the list, fails if the Scout is already in the list.
     *
     * @param item Scout to add
     * @return true if the operation succeeded, false if not
     */
    private final boolean tryAdd(final Scout item) {
        for (Scout scout : listItems) {
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
        listItems.offerFirst(item);
        adapter.notifyDataSetChanged();
        saveRecordFile(recordFile, listItems);
    }

    /**
     * Removes a Scout from the list and updates the record file.
     *
     * @param item Scout to add
     */
    private final void removeItem(final Scout item) {
        listItems.remove(item);
        adapter.notifyDataSetChanged();
        saveRecordFile(recordFile, listItems);
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
        listItems.remove(scout);
        listItems.offerLast(scout);
        adapter.notifyDataSetChanged();
        saveRecordFile(recordFile, listItems);
    }

    /**
     * Clears the list of Scouts and deletes the record file.
     */
    private final void clearScoutList() {
        listItems.clear();
        adapter.notifyDataSetChanged();
        saveRecordFile(recordFile, listItems);

        recordFile.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.main_menu_export_excel:
                exportExcel();
                break;
            case R.id.main_menu_export_text:

                break;
            case R.id.main_menu_set_money_earned:

                break;
            case R.id.main_menu_clear_entries:
                if (!listItems.isEmpty()) {
                    showScoutDialog(new ClearEntriesDialogFragment(), null, "ClearEntriesDialogFragment", DialogListener.CONFIRM_CLEAR_ENTRIES);
                } else {
                    final Toast toast = Toast.makeText(this, "There are no entries!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportExcel() {
        final XSSFWorkbook workbook = ExcelExporter.export(listItems, -1, "h:mm AM/PM, MMM d");
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

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/vnd.ms-excel");
        emailIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(emailIntent, "Export spreadsheet using"));
    }

    private void mainMenuClearEntries() {
        clearScoutList();

        final Toast toast = Toast.makeText(this, "Deleted all entries!", Toast.LENGTH_SHORT);
        toast.show();
    }
}
