package com.troop6quincy.bottledrivetimelog.deletescout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.troop6quincy.bottledrivetimelog.R;

/**
 * Confirmation dialog box for removing a Scout from the list. Scout to be removed should be mapped
 * to String key {@link R.string#scout_obj_key} in a {@link Bundle}, and passed to the fragment
 * with {@link #setArguments(Bundle)}.
 *
 * @author Joe Desmond
 */
public class DeleteScoutDialogFragment extends DialogFragment {
    private DeleteScoutDialogListener listener;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        listener = (DeleteScoutDialogListener) context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_delete_scout)
                .setPositiveButton(R.string.delete, (dialog, id) -> listener.onDialogPositiveClick(this))
                .setNegativeButton(R.string.cancel, (dialog, id) -> listener.onDialogNegativeClick(this));
        return builder.create();
    }
}
