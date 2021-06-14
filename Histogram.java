/**
 * @author Johann Loefflmann
 *
 * Updated on 14 June, 2021
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Histogram {
    private final HashMap<String, HistoEntry> hashMap;
    private String name;


    public Histogram() {
        hashMap = new HashMap<>();
    }

    private static boolean isHistoBeginning(String str) {
        return str.trim().equalsIgnoreCase("num     #instances         #bytes  class name");
    }

    public static Histogram readHistogramFile(String filePath) throws Exception {
        Histogram histogram = null;
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        String str = in.readLine();
        while (str != null) {
            if (isHistoBeginning(str)) {
                str = in.readLine();
                histogram = Histogram.readHistogram(in);
            } else {
                str = in.readLine();
            }
        }
        in.close();

        return histogram;
    }

    public static Histogram readHistogram(BufferedReader in) {
        Histogram histogram = new Histogram();
        try {
            String line = in.readLine();

            while ((line != null) && (line.length() > 0)) {
                List<String> entries = Arrays.stream(line.split(" {2,5}"))
                        .filter(e -> !e.isEmpty())
                        .collect(Collectors.toList());

                if (entries.get(0).trim().endsWith(":")) {
                    String instances = entries.get(1).trim();
                    String bytes = entries.get(2).trim();
                    String classname = entries.get(3).trim();

                    HistoEntry entry = new HistoEntry(Long.parseLong(bytes), Long.parseLong(instances), classname);

                    histogram.put(entry.getClassName(), entry);
                }

                line = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return histogram;
    }

    public void put(String string, HistoEntry histoEntry) {
        hashMap.put(string, histoEntry);
    }

    public HistoEntry get(String key) {
        return hashMap.get(key);
    }

    public Collection<HistoEntry> values() {
        return hashMap.values();
    }

    public boolean containsKey(String key) {
        return hashMap.containsKey(key);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
