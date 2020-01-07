package com.troop6quincy.bottledrivetimelog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity that is used to construct Scout objects, presents name and check-in time fields.
 *
 * @author Joe Desmond
 */
public class CheckInActivity extends AppCompatActivity {
    public static final int CHECKIN_NEW_REQUEST = 1;

    private EditText nameInput;
    private TimePicker timeInput;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        nameInput = findViewById(R.id.scoutName);
        timeInput = findViewById(R.id.checkInTime);

        final Button submitButton = findViewById(R.id.submitCheckIn);

        submitButton.setOnClickListener(this::submitAction);
    }

    /**
     * Submit button click listener. When the submit button is pressed, the name and time are
     * used to create a {@link Scout} object, which is passed back to the calling activity.
     *
     * @param view view
     */
    private final void submitAction(final View view) {
        final Scout scout = getCurrentScout();
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(getResources().getString(R.string.scout_obj_key), scout);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Use the currently set name and time to construct a {@link Scout} object.
     *
     * @return current Scout
     */
    private final Scout getCurrentScout() {
        final String scoutName = nameInput.getText().toString().trim();
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = timeInput.getCurrentHour();
        final int minute = timeInput.getCurrentMinute();
        calendar.set(year, month, day, hour, minute);
        final Date date = calendar.getTime();

        return new Scout(scoutName, date);
    }
}
