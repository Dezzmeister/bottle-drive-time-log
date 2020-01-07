package com.troop6quincy.bottledrivetimelog;

import java.io.Serializable;
import java.util.Date;

/**
 * A single Scout, represented by name and containing check-in and check-out times.
 *
 * @author Joe Desmond
 */
public final class Scout implements Serializable {

    public final String name;
    public final Date checkIn;
    public Date checkOut = null;

    /**
     * Creates a Scout with the given name and check-in date. The check-out date is null until set
     * by {@link Scout#setCheckOut(Date)}.
     *
     * @param _name first and last name, delimited by a space
     * @param _checkIn check-in date (seconds are ignored)
     */
    public Scout(final String _name, final Date _checkIn) {
        name = _name;
        checkIn = _checkIn;
    }

    /**
     * Sets the check-out date of this Scout.
     *
     * @param _checkOut check-out date
     */
    public void setCheckOut(final Date _checkOut) {
        checkOut = _checkOut;
    }

    /**
     * Returns the name of this Scout.
     *
     * @return name of this Scout
     */
    @Override
    public final String toString() {
        return name;
    }

    /**
     * Compares this Scout and another by name. Check-in and check-out dates are not considered,
     * and names are case sensitive.
     *
     * @param other Scout to compare <code>this</code> to
     * @return true if this Scout and <code>other</code> have the same name, false otherwise
     * (or if <code>other</code> is not a Scout object
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Scout)) {
            return false;
        }

        final Scout otherScout = (Scout) other;
        return name.equals(otherScout.name);
    }

    /**
     * Overridden, like {@link Scout#equals(Object)}.
     *
     * @return the hashcode of this Scout's name
     */
    @Override
    public final int hashCode() {
        return name.hashCode();
    }
}
