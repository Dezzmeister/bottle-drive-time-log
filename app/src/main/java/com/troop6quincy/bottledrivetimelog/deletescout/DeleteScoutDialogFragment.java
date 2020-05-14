package com.troop6quincy.bottledrivetimelog.deletescout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.troop6quincy.bottledrivetimelog.R;

/**
 * Confirmation dialog box for removing a Scout from the list. Scout to be removed should be mapped
 * to String key {@link R.string#scout_obj_key} in a {@link Bundle}, and passed to the fragment
 * with {@link #setArguments(Bundle)}.
 *
 * @author Joe Desmond
 */
public class DeleteScoutDialogFragment extends DialogFragment {
    private DialogListener listener;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        listener = (DialogListener) context;
    }

    /**
     * Sets the listener object to receive the result of this dialog. In later Android APIs this would
     * be set in {@link #onAttach(Context)}, but in earlier APIs the listener object is null
     * (tested in Android 19).
     *
     * @param _listener listener object
     */
    public void setListener(final DialogListener _listener) {
        listener = _listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(R.string.dialog_delete_scout)
                .setMessage("This action cannot be undone.")
                .setPositiveButton(R.string.delete, (dialog, id) -> listener.onDialogPositiveClick(this))
                .setNegativeButton(R.string.cancel, (dialog, id) -> listener.onDialogNegativeClick(this));
        return builder.create();
    }
}
