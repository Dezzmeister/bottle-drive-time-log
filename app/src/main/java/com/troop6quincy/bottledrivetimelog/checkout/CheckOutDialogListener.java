package com.troop6quincy.bottledrivetimelog.checkout;

import android.app.DialogFragment;
import android.widget.TimePicker;

public interface CheckOutDialogListener {

    void onTimeSet(final DialogFragment dialog, final TimePicker timePicker, final int hour, final int minute);
}
