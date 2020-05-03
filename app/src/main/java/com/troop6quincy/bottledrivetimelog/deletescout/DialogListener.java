package com.troop6quincy.bottledrivetimelog.deletescout;

import android.app.DialogFragment;

public interface DialogListener {
    public static final String DIALOG_ID_KEY = "dialogID";

    public static final int CONFIRM_DELETE_SCOUT = 1;
    public static final int CONFIRM_CLEAR_ENTRIES = 2;

    void onDialogPositiveClick(final DialogFragment dialog);
    void onDialogNegativeClick(final DialogFragment dialog);
}
