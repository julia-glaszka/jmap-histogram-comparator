import java.util.Comparator;

/**
 *
 * @author Poonam Bajaj
 * Updated on 14 June, 2021
 */

public class NumComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        int ret = 0;
        Long i = (Long) o1;
        Long j = (Long) o2;

        if (i < j) {
            ret = -1;
        }  else if (i > j) {
            ret = 1;
        }

        return ret;
    }
}
