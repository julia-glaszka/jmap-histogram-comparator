/**
 * @author Johann Loefflmann
 * <p>
 * Updated on 14 June, 2021
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class Histogram {
    private final Map<String, HistoEntry> hashMap;
    private final String name;

    public Histogram(String filePath, String name) throws Exception {
        this.hashMap = readHistogramFile(filePath);
        this.name = name;
    }

    public Map<String, HistoEntry> readHistogramFile(String filePath) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        Map<String, HistoEntry> histogram = readHistogram(in);
        in.close();
        return histogram;
    }

    private Map<String, HistoEntry> readHistogram(BufferedReader in) {
        return in.lines()
                .filter(line -> line != null && (line.length() > 0))
                .filter(line -> !line.contains("#bytes") && !line.contains("#instances"))
                .filter(line -> !line.contains("---"))
                .filter(line -> !line.startsWith("Total"))
                .map(line -> {
                            List<String> entries = Arrays.stream(line.split(" {2,5}"))
                                    .filter(e -> !e.isEmpty())
                                    .collect(Collectors.toList());

                            String instances = entries.get(1).trim();
                            String bytes = entries.get(2).trim();
                            String classname = entries.get(3).trim();

                            return new HistoEntry(Long.parseLong(bytes), Long.parseLong(instances), classname);
                        }
                ).collect(toMap(HistoEntry::getClassName, Function.identity()));
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

    @Override
    public String toString() {
        return name;
    }
}
