package com.troop6quincy.bottledrivetimelog.export;

import com.troop6quincy.bottledrivetimelog.Scout;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Exports Scout data in the CSV text format. Entries are delimited by commas, lines are terminated
 * with CRLF, and a column header line is included.
 *
 * @author Joe Desmond
 * @since 1.0
 * @version 1.0
 */
public class CSVExporter {

    /**
     * Line termination sequence
     */
    public static final String ENDLINE = "\r\n";

    /**
     * Exports the given Scout data as a single String in the CSV format. If <code>totalMoney</code>
     * is negative, a money column is not included. The date formatter is used to format check-in
     * and check-out dates.
     * <p>
     * This function generates a CSV document in which entries are delimited by commas and lines are
     * terminated with CRLF. A column header line is included.
     *
     * @param items list of Scouts
     * @param totalMoney total money earned
     * @param dateFormatter formatter for Scout check-in and check-out dates
     * @return a CSV text document
     */
    public static final String exportCSV(final List<Scout> items, final double totalMoney, final SimpleDateFormat dateFormatter) {
        final StringBuilder sb = new StringBuilder();

        sb.append("Name,Check-in Time,Check-out Time,Total Hours");

        if (items.size() != 0) {
            sb.append(ENDLINE);
        } else {
            return sb.toString();
        }

        final double[] moneyEarned;

        if (totalMoney >= 0) {
            sb.append(",Money Earned");

            moneyEarned = new double[items.size()];

            // Calculate total minutes worked
            int totalMinutesWorked = 0;
            for (Scout scout : items) {
                if (scout.minutesCheckedIn >= 0) {
                    totalMinutesWorked += scout.minutesCheckedIn;
                }
            }

            // Calculate minutes worked for each Scout
            for (int i = 0; i < moneyEarned.length; i++) {
                final Scout scout = items.get(i);

                if (scout.minutesCheckedIn >= 0) {
                    final double minuteRatio = scout.minutesCheckedIn/(double)totalMinutesWorked;
                    moneyEarned[i] = minuteRatio * totalMoney;
                } else {
                    moneyEarned[i] = -1;
                }
            }
        } else {
            moneyEarned = null;
        }

        for (int i = 0; i < items.size(); i++) {
            final Scout scout = items.get(i);
            final String cleanName = "\"" + scout.name.replace("\"", "\"\"") + "\"";
            final String cleanCheckInDate = dateFormatter.format(scout.checkIn).replace("\"", "\"\"");
            final String cleanCheckOutDate;
            final String totalHours;
            final String moneyEarnedString;

            if (scout.checkOut != null) {
                cleanCheckOutDate = dateFormatter.format(scout.checkOut).replace("\"", "\"\"");
                totalHours = String.format(Locale.US, "%.3f", (scout.minutesCheckedIn / 60.0));

                if (moneyEarned != null) {
                    moneyEarnedString = "$" + String.format(Locale.US, "%.2f", moneyEarned[i]);
                } else {
                    moneyEarnedString = "";
                }
            } else {
                cleanCheckOutDate = "";
                totalHours = "";
                moneyEarnedString = "";
            }

            String line = cleanName + ",\"" + cleanCheckInDate + "\",\"" + cleanCheckOutDate + "\"," + totalHours;

            if (moneyEarned != null) {
                line += "," + moneyEarnedString;
            }

            sb.append(line);

            if (i < items.size() - 1) {
                sb.append(ENDLINE);
            }
        }

        return sb.toString();
    }
}
