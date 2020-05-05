package com.troop6quincy.bottledrivetimelog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Activity that is used to construct Scout objects, presents name and check-in time fields.
 *
 * @author Joe Desmond
 * @since 1.0
 * @version 1.0
 */
public class CheckInActivity extends AppCompatActivity {
    private SessionObject session;
    private EditText nameInput;
    private TimePicker timeInput;

    /**
     * Accepts a new session and updates the theme of the activity accordingly.
     *
     * @param _session new session
     */
    public void updateSession(final SessionObject _session) {
        session = _session;

        final ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
        final EditText nameText = findViewById(R.id.scoutName);
        final TimePicker timePicker = findViewById(R.id.checkInTime);
        final Button submitButton = findViewById(R.id.submitCheckIn);
        final Button cancelButton = findViewById(R.id.cancelCheckIn);

        if (session.darkThemeEnabled) {
            mainLayout.setBackgroundColor(getResources().getColor(R.color.darkThemeBackground));

            nameText.setBackgroundColor(getResources().getColor(R.color.darkThemeTextBoxBackground));
            nameText.setTextColor(getResources().getColor(R.color.darkThemeText));
            nameText.setHintTextColor(getResources().getColor(R.color.darkThemeHint));

            timePicker.setBackgroundColor(getResources().getColor(R.color.darkThemeBackground));
        } else {
            mainLayout.setBackgroundColor(getResources().getColor(R.color.lightThemeBackground));

            nameText.setBackgroundColor(getResources().getColor(R.color.lightThemeBackground));
            nameText.setTextColor(getResources().getColor(R.color.lightThemeText));
            nameText.setHintTextColor(getResources().getColor(R.color.lightThemeHint));

            timePicker.setBackgroundColor(getResources().getColor(R.color.lightThemeBackground));
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final SessionObject sessionObject = (SessionObject) intent.getSerializableExtra(getResources().getString(R.string.session_object_key));
        //updateSession(sessionObject);

        if (sessionObject.darkThemeEnabled) {
            setContentView(R.layout.activity_checkin_dark);
        } else {
            setContentView(R.layout.activity_checkin);
        }

        nameInput = findViewById(R.id.scoutName);
        timeInput = findViewById(R.id.checkInTime);

        final Button submitButton = findViewById(R.id.submitCheckIn);
        final Button cancelButton = findViewById(R.id.cancelCheckIn);

        submitButton.setOnClickListener(this::submitAction);
        cancelButton.setOnClickListener(this::cancelAction);

        final AdView adView = findViewById(R.id.lowerBannerAd);
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
    }

    /**
     * Submit button click listener. When the submit button is pressed, the name and time are
     * used to create a {@link Scout} object, which is passed back to the calling activity.
     *
     * @param view view, unused
     */
    private final void submitAction(final View view) {
        final Scout scout = getCurrentScout();

        // Make sure the user is not trying to create a scout with no name or an empty name
        if (scout.name.trim().isEmpty()) {
            final Toast toast = Toast.makeText(view.getContext(), "Invalid name!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        final Intent resultIntent = new Intent();
        resultIntent.putExtra(getResources().getString(R.string.scout_obj_key), scout);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Cancel button click listener. When the cancel button is pressed, this activity should finish
     * without providing a scout to be added to the checked in list.
     *
     * @param view view, unused
     */
    private final void cancelAction(final View view) {
        final Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
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
