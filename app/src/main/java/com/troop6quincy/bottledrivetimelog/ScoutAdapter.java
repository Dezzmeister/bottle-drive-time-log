package com.troop6quincy.bottledrivetimelog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * A custom adapter class to draw a ListView containing Scout objects. Each element of the list is
 * drawn with the Scout's name on the first row, the check-in date on the second row, and the
 * check-out date on the third row (if the Scout has checked out).
 *
 * @author Joe Desmond
 * @version 1.0
 * @since 1.0
 */
public class ScoutAdapter extends ArrayAdapter<Scout> {

    /**
     * Date format used to display check-in and check-out dates for each entry
     */
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, MMM d");

    /**
     * Context of the listview
     */
    private final Context context;

    /**
     * Entries in the list
     */
    private final List<Scout> items;

    /**
     * Creates a ScoutAdapter with the given context, TextView id (unused), and list.
     *
     * @param _context context
     * @param _resId unused
     * @param _items list
     */
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
