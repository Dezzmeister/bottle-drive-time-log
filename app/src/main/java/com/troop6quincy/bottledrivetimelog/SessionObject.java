package com.troop6quincy.bottledrivetimelog;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Contains data to be saved/restored between sessions.
 */
public class SessionObject implements Serializable {

    /**
     * Scout entries (list of checked-in and checked-out Scouts)
     */
    public LinkedList<Scout> listItems;

    /**
     * Total money earned, or -1 to indicate this value is not being used or is uninitialized
     */
    public long totalMoney;

    /**
     * True if dark theme is enabled
     */
    public boolean darkThemeEnabled = false;

    /**
     * Creates a SessionObject with the given Scout entries and total money.
     *
     * @param _listItems checked-in and checked-out Scouts
     * @param _totalMoney total money earned or -1
     * @see #totalMoney
     */
    public SessionObject(final LinkedList<Scout> _listItems, final long _totalMoney) {
        listItems = _listItems;
        totalMoney = _totalMoney;
    }

    /**
     * Toggles dark theme. Changes {@link #darkThemeEnabled}.
     */
    public void toggleDarkTheme() {
        darkThemeEnabled = !darkThemeEnabled;
    }

    /**
     * Saves this object to the given path. This is used to save data for the next session, so that
     * the user can exit the app and reopen it without losing the list of checked-in and checked-out
     * scouts or the total money earned.
     *
     * @param context context to show {@linkplain Toast toast} in if error occurs
     * @param file file to save to
     */
    public void save(final Context context, final File file) {
        try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(context, "Unable to save session data!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Loads a file at the given path. This is used to load the data in the previous session.
     * The file is just a serialized {@link SessionObject}.
     *
     * @param context context to show {@linkplain Toast toast} in if error occurs
     * @param file path to the previous session
     * @return previous session
     */
    public static SessionObject loadPreviousSession(final Context context, final File file) {
        SessionObject object = new SessionObject(new LinkedList<>(), -1);

        try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            object = (SessionObject) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(context, "Unable to load previous session data!", Toast.LENGTH_SHORT);
            toast.show();
        }

        return object;
    }
}
