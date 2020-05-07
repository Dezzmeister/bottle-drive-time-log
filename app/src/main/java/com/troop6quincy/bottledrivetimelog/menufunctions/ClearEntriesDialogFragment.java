package com.troop6quincy.bottledrivetimelog.menufunctions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.troop6quincy.bottledrivetimelog.R;
import com.troop6quincy.bottledrivetimelog.deletescout.DialogListener;

public class ClearEntriesDialogFragment extends DialogFragment {
    private DialogListener listener;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        listener = (DialogListener) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(R.string.dialog_delete_all_scouts)
                .setMessage("This action cannot be undone.")
                .setPositiveButton(R.string.delete_all_entries, (dialog, id) -> listener.onDialogPositiveClick(this))
                .setNegativeButton(R.string.cancel, (dialog, id) -> listener.onDialogNegativeClick(this));
        return builder.create();
    }
}
