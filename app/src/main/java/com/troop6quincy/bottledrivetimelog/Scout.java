package com.troop6quincy.bottledrivetimelog;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * A Boy Scout
 *
 * @author Joe Desmond
 */
public final class Scout implements Serializable {

    public final String name;
    public final Date checkIn;
    public Date checkOut = null;

    public Scout(final String _name, final Date _checkIn) {
        name = _name;
        checkIn = _checkIn;
    }

    public void setCheckOut(final Date _checkOut) {
        checkOut = _checkOut;
    }

    @Override
    public final String toString() {
        return name;
    }

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

    @Override
    public final int hashCode() {
        return name.hashCode();
    }
}
