package com.troop6quincy.bottledrivetimelog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

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
