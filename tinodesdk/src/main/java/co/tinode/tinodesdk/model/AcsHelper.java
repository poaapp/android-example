package co.tinode.tinodesdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Helper class for access mode parser/generator.
 */
public class AcsHelper implements Serializable {
    private static final String TAG = "AcsHelper";

    // User access to topic
    private static final int MODE_JOIN = 0x01;      // J - join topic
    private static final int MODE_READ = 0x02;      // R - read broadcasts
    private static final int MODE_WRITE = 0x04;     // W - publish
    private static final int MODE_PRES = 0x08;      // P - receive presence notifications
    private static final int MODE_APPROVE = 0x10;   // A - approve requests
    private static final int MODE_SHARE = 0x20;     // S - user can invite other people to join (S)
    private static final int MODE_DELETE = 0x40;    // D - user can hard-delete messages (D), only owner can completely delete
    private static final int MODE_OWNER = 0x80;     // O - user is the owner (O) - full access

    private static final int MODE_NONE = 0; // No access, requests to gain access are processed normally (N)

    // Invalid mode to indicate an error
    private static final int MODE_INVALID = 0x100000;

    private Integer a;

    public AcsHelper(String str) {
        a = decode(str);
    }

    public AcsHelper(AcsHelper ah) {
        a = ah != null ? ah.a : null;
    }

    @Override
    public String toString() {
        return a == null ? "" : encode(a);
    }

    public boolean update(String umode) {
        Integer old = a;
        a = update(a, umode);
        return !a.equals(old);
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (!(o instanceof AcsHelper)) {
            return false;
        }

        AcsHelper ah = (AcsHelper) o;

        return (a == null && ah.a == null) || (a != null && a.equals(ah.a));
    }

    public boolean equals(String s) {
        Integer ah = decode(s);
        return (a == null && ah == null) || (a != null && a.equals(ah));
    }

    public boolean isReader() {
        return (a != null) && ((a & MODE_READ) != 0);
    }
    public boolean isWriter() {
        return (a != null) && ((a & MODE_WRITE) != 0);
    }
    public boolean isMuted() {
        return (a != null) && ((a & MODE_PRES) == 0);
    }
    @JsonIgnore
    public void setMuted(boolean v) {
        if (a == null) {
            a = MODE_NONE;
        }
        a = !v ? a | MODE_PRES : (a & ~MODE_PRES);
    }
    public boolean isAdmin() {
        return (a != null) && ((a & MODE_APPROVE) != 0);
    }
    public boolean isDeleter() {
        return (a != null) && ((a & MODE_DELETE) != 0);
    }

    public boolean isOwner() {
        return (a != null) && ((a & MODE_OWNER) != 0);
    }

    public boolean isJoiner() {
        return (a != null) && ((a & MODE_JOIN) != 0);
    }

    public boolean isDefined() {
        return a != null && a != MODE_NONE && a != MODE_INVALID;
    }
    public boolean isInvalid() {
        return a != null && a == MODE_INVALID;
    }
    private static Integer decode(String mode) {
        if (mode == null || mode.length() == 0) {
            return null;
        }

        int m0 = MODE_NONE;

        for (char c : mode.toCharArray()) {
            switch (c) {
                case 'J':
                case 'j':
                    m0 |= MODE_JOIN;
                    continue;
                case 'R':
                case 'r':
                    m0 |= MODE_READ;
                    continue;
                case 'W':
                case 'w':
                    m0 |= MODE_WRITE;
                    continue;
                case 'A':
                case 'a':
                    m0 |= MODE_APPROVE;
                    continue;
                case 'S':
                case 's':
                    m0 |= MODE_SHARE;
                    continue;
                case 'D':
                case 'd':
                    m0 |= MODE_DELETE;
                    continue;
                case 'P':
                case 'p':
                    m0 |= MODE_PRES;
                    continue;
                case 'O':
                case 'o':
                    m0 |= MODE_OWNER;
                    continue;
                case 'N':
                case 'n':
                    return MODE_NONE;
                default:
                    return MODE_INVALID;
            }
        }

        return m0;
    }

    private static String encode(Integer val) {
        // Need to distinguish between "not set" and "no access"
        if (val == null || val == MODE_INVALID) {
            return null;
        }

        if (val == 0) {
            return "N";
        }

        StringBuilder res = new StringBuilder(6);
        char[] modes = new char[]{'J', 'R', 'W', 'P', 'A', 'S', 'D', 'O'};
        for (int i = 0; i < modes.length; i++) {
            if ((val & (1 << i)) != 0) {
                res.append(modes[i]);
            }
        }
        return res.toString();
    }

    /**
     * Apply changes, defined as a string, to the given internal representation.
     *
     * @param val value to change.
     * @param umode change to the value, '+' or '-' followed by the letter(s) being set or unset,
     *              or an explicit new value.
     * @return updated value.
     */
    private static Integer update(Integer val, String umode) {
        if (umode == null || umode.length() == 0) {
            return val;
        }
        char sign = umode.charAt(0);
        int m0;
        if (sign == '+' || sign == '-') {
            if (umode.length() < 2) {
                throw new IllegalArgumentException();
            }
            m0 = decode(umode.substring(1));
        } else {
            m0 = decode(umode);
        }

        if (m0 == MODE_INVALID) {
            throw new IllegalArgumentException();
        }
        if (m0 == MODE_NONE) {
            return val;
        }

        if (val == null) {
            val = MODE_NONE;
        }

        if (sign == '+') {
            val |= m0;
        } else if (sign == '-') {
            val &= ~m0;
        } else {
            val = m0;
        }

        return val;
    }

    public boolean merge(AcsHelper ah) {
        if (ah != null && ah.a != null && ah.a != MODE_INVALID) {
            if (!ah.a.equals(a)) {
                a = ah.a;
                return true;
            }
        }
        return false;
    }
}
