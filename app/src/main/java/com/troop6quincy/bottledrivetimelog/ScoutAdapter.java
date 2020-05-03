package com.troop6quincy.bottledrivetimelog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class ScoutAdapter extends ArrayAdapter<Scout> {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, MMM d");

    private final Context context;
    private final List<Scout> items;

    public ScoutAdapter(final Context _context, final int _resId, final List<Scout> _items) {
        super(_context, _resId, _items);
        context = _context;
        items = _items;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final Scout scout = items.get(position);

        View v;

        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (scout != null && scout.checkOut == null) {
            v = vi.inflate(R.layout.checked_in_entry, null);
        } else {
            v = vi.inflate(R.layout.checked_out_entry, null);
        }

        if (scout != null) {
            final TextView scoutName = v.findViewById(R.id.scoutEntryName);
            final TextView checkInDate = v.findViewById(R.id.scoutEntryCheckInDate);

            scoutName.setText(scout.name);
            checkInDate.setText("Checked in: " + dateFormat.format(scout.checkIn));

            if (scout.checkOut != null) {
                final TextView checkOutDate = v.findViewById(R.id.scoutEntryCheckOutDate);
                checkOutDate.setText("Checked out: " + dateFormat.format(scout.checkOut));
            }
        }

        return v;
    }


}
