/**
 *
 * @author Poonam Bajaj
 *
 * HistoEntry.java
 *
 * Created on May 3, 2007, 10:46 AM
 * Updated on 14 June, 2021
 */

class HistoEntry implements Comparable {
    private final long size;
    private final long count;
    private final String className;

    public HistoEntry(long size, long count, String className) {
        this.size = size;
        this.count = count;
        this.className = className;
    }

    @Override
    public int compareTo(Object o) throws ClassCastException {
        HistoEntry e = (HistoEntry) o;

        int ret = 0;
        if (size < e.getSize()) {
            ret = -1;
        } else if (size > e.getSize()) {
            ret = 1;
        }

        return ret;
    }

    public long getSize() {
        return size;
    }

    public long getCount() {
        return count;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "HistoEntry{" +
                " className='" + className + '\'' +
                '}';
    }
}

