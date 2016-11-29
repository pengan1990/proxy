/**
 * (created at 2011-10-24)
 */
package parse.util;

/**

 */
public class ArrayUtil {
    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    public static boolean contains(String[] list, String str) {
        if (list == null) return false;
        for (String string : list) {
            if (equals(str, string)) {
                return true;
            }
        }
        return false;
    }

}
