package com.troop6quincy.bottledrivetimelog.checkout;

import android.app.DialogFragment;
import android.widget.TimePicker;

/**
 * Listener interface for a checkout dialog, with a time picker.
 *
 * @author Joe Desmond
 * @since 1.0
 * @version 1.0
 */
public interface CheckOutDialogListener {

    /**
     * Called when the time is set (OK is pressed).
     *
     * @param dialog dialog box
     * @param timePicker dialog time picker
     * @param hour hour
     * @param minute minute
     */
    void onTimeSet(final DialogFragment dialog, final TimePicker timePicker, final int hour, final int minute);
}
