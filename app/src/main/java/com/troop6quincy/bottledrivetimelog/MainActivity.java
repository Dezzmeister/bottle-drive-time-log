package com.troop6quincy.bottledrivetimelog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.troop6quincy.bottledrivetimelog.deletescout.DeleteScoutDialogListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

/**
 * Main activity, contains a list of currently checked-in Scouts.
 *
 * @author Joe Desmond
 */
public class MainActivity extends AppCompatActivity implements DeleteScoutDialogListener, CheckOutDialogListener {

    private static final String SCOUT_RECORD_NAME = "bottle-drive-records.txt";
    private List<String> recordFile;

    private ListView scoutListView;
    private final LinkedList<Scout> listItems = new LinkedList<Scout>();
    private ArrayAdapter<Scout> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scoutListView = (ListView) findViewById(R.id.scoutNameView);
        adapter = new ScoutAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listItems);
        scoutListView.setAdapter(adapter);

        registerForContextMenu(scoutListView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            final Intent intent = new Intent(view.getContext(), CheckInActivity.class);
            ((Activity) view.getContext()).startActivityForResult(intent, CheckInActivity.CHECKIN_NEW_REQUEST);
        });

        final File dir = getApplicationContext().getFilesDir();
        final File path = new File(dir, SCOUT_RECORD_NAME);

        if (path.exists()) {
            recordFile = loadRecordFile(path);
        } else {
            recordFile = new ArrayList<>();
        }
    }

    /**
     * Loads a record file at the given path. This is used to load the data in the previous session.
     * Each line of the file includes a record for one scout. The scout name, check in date, and check
     * out date are present in that order, delimited by tabs.
     *
     * @param path path to the record file
     * @return the file, line by line
     */
    private List<String> loadRecordFile(final File path) {
        try (final BufferedReader reader = new BufferedReader(new FileReader(path))) {
            final List<String> out = new ArrayList<>();

            while (reader.ready()) {
                out.add(reader.readLine());
            }

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            final Toast toast = Toast.makeText(this, "Unable to load previous session data!", Toast.LENGTH_SHORT);
            toast.show();
        }

        return null;
    }

    /**
     * Deletes a scout from the list. Runs when confirmation is given to delete an entry.
     *
     * @param dialog dialog box
     */
    @Override
    public void onDialogPositiveClick(final DialogFragment dialog) {
        final Bundle arguments = dialog.getArguments();
        final Scout scout = (Scout) arguments.get(getResources().getString(R.string.scout_obj_key));
        removeItem(scout);

        final Toast toast = Toast.makeText(this, "Deleted " + scout.name + "!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onDialogNegativeClick(final DialogFragment dialog) {

    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View view, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scoutlist, menu);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Scout selectedScout = (Scout) scoutListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.checkout_scout:
                if (selectedScout.checkOut == null) {
                    showScoutDialog(new CheckOutDialogFragment(), selectedScout, "CheckOutDialogFragment");
                } else {
                    final Toast toast = Toast.makeText(this, selectedScout.name + " has already checked out!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            case R.id.delete_scout:
                showScoutDialog(new DeleteScoutDialogFragment(), selectedScout, "DeleteScoutDialogFragment");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showScoutDialog(final DialogFragment dialog, final Scout scout, final String dialogName) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(getResources().getString(R.string.scout_obj_key), scout);
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
                        final Toast toast = Toast.makeText(this, "Scout already checked in!", Toast.LENGTH_SHORT);
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
     * Adds a Scout to the list.
     *
     * @param item Scout to add
     */
    private final void addItem(final Scout item) {
        listItems.offerFirst(item);
        adapter.notifyDataSetChanged();
    }

    private final void removeItem(final Scout item) {
        listItems.remove(item);
        adapter.notifyDataSetChanged();
    }

    private final void checkOutScout(final Scout scout) {
        listItems.remove(scout);
        listItems.offerLast(scout);
        adapter.notifyDataSetChanged();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
