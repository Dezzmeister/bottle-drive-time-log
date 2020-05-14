package com.troop6quincy.bottledrivetimelog.checkout;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.troop6quincy.bottledrivetimelog.R;

import java.util.Calendar;
import java.util.Locale;

/**
 * Dialog for picking a check-out time for a Scout. Scout to be removed should be mapped
 * to String key {@link R.string#scout_obj_key} in a {@link Bundle}, and passed to the fragment
 * with {@link #setArguments(Bundle)}.
 *
 * @author Joe Desmond
 */
public class CheckOutDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private CheckOutDialogListener timeListener;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        timeListener = (CheckOutDialogListener) context;
    }

    /**
     * Sets the listener object to receive the result of this dialog. In later Android APIs this would
     * be set in {@link #onAttach(Context)}, but in earlier APIs the listener object is null
     * (tested in Android 19).
     *
     * @param _timeListener listener object
     */
    public void setListener(final CheckOutDialogListener _timeListener) {
        timeListener = _timeListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), R.style.TimePickerTheme, this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(final TimePicker picker, final int hour, final int minute) {
        timeListener.onTimeSet(this, picker, hour, minute);
    }
}
