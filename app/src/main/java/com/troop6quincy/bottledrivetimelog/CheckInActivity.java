package com.troop6quincy.bottledrivetimelog;

import android.app.ActionBar;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Activity that is used to construct Scout objects, presents name and check-in time fields.
 *
 * @author Joe Desmond
 * @since 1.0
 * @version 1.0
 */
public class CheckInActivity extends AppCompatActivity {
    private EditText nameInput;
    private TimePicker timeInput;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Intent intent = getIntent();
        final SessionObject session = (SessionObject) intent.getSerializableExtra(getResources().getString(R.string.session_object_key));

        if (session.darkThemeEnabled) {
            getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_YES));
        } else {
            getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_NO));
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkin);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
