package unxutils.format;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility functions for presentation, etc.
 */
public class Format {

    // This method trims if necessary to get to the target length
    public static final String format(String string, int length) {
        return format(string, length, true);
    }

    /**
     * This method trims or pad fills with spaces at left if necessary
     * to get to the target length
     * @param string String to get formatted
     * @param length Total maximum length
     * @param fill Should we fill by the left side with spaces?
     * @return Formatted string
     */
    public static String format(String string, int length, boolean fill) {
        String ret = string;
        if (string.length() > length) {
            ret = string.substring(0, length);
        }
        else if (string.length() < length && fill) {
            // Pad with spaces
            ret = StringUtils.leftPad(string, length, " ");
        }
        return ret;
    }
}
