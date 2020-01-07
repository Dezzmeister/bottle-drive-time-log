package com.troop6quincy.bottledrivetimelog.checkout;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class CheckOutDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private CheckOutDialogListener timeListener;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        timeListener = (CheckOutDialogListener) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(final TimePicker picker, final int hour, final int minute) {
        timeListener.onTimeSet(this, picker, hour, minute);
    }
}
