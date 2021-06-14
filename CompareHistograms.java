/**
 * @author Poonam Bajaj
 * <p>
 * Contributions: Johann Loefflmann
 * Contributions: Claudio Massi
 * Contributions: Julia Glaszka
 * CompareHistograms.java
 * <p>
 * Created on May 3, 2007, 10:46 AM
 * Updated on 14 June, 2021
 */

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

public class CompareHistograms extends JFrame {

    private final DefaultListModel<Histogram> histograms = new DefaultListModel<>();
    private final JList<Histogram> list = new JList<>(histograms);
    private final Comparator<HistoEntry> histoEntryComparator = Comparator.reverseOrder();
    List<String> COLUMN_LABELS = Arrays.asList("Size (diff)", "Count (diff)", "Class description");

    public CompareHistograms() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("JVM Histograms Comparator");
        setSize(640, 480);
        this.setLocationRelativeTo(null);

        setupMenuBar();

        final JButton compareButton = new JButton("Compare Histograms");

        setLayout(new BorderLayout());
        JPanel p = new JPanel();
        getContentPane().add(p, BorderLayout.NORTH);
        p.setLayout(new FlowLayout());

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout());
        panel.add(new Label("List of Histograms, double click to preview"), BorderLayout.NORTH);
        panel.add(list, BorderLayout.CENTER);
        list.addListSelectionListener(e -> {
            int[] indexes = list.getSelectedIndices();
            compareButton.setEnabled(indexes.length == 2);
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    showHistogram(index);
                }
            }
        });

        JPanel pl = new JPanel();
        getContentPane().add(pl, BorderLayout.SOUTH);
        pl.add(new Label("Select two histograms to compare"));
        pl.add(compareButton);

        compareButton.setEnabled(false);
        compareButton.addActionListener(evt -> {
            int[] indexes = list.getSelectedIndices();
            System.out.println("indexes " + Arrays.toString(indexes));
            showHistoCompareWindow(indexes[0], indexes[1]);
        });
        setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(CompareHistograms::new);
    }

    private void setupMenuBar() {

        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);

        JMenu menuFile = new JMenu("File");
        bar.add(menuFile);

        JMenuItem menuItemOpen = new JMenuItem("Open Histogram File");
        menuFile.add(menuItemOpen);

        menuItemOpen.addActionListener(e -> openHistogramFile());

        menuFile.add(new JSeparator());

        JMenuItem menuItemExit = new JMenuItem("Exit");
        menuFile.add(menuItemExit);

        menuItemExit.addActionListener(e -> System.exit(0));

        JMenu menuEdit = new JMenu("Edit");
        bar.add(menuEdit);

        JMenuItem menuItemClear = new JMenuItem("Clear");
        menuEdit.add(menuItemClear);

        menuItemClear.addActionListener(e -> histograms.clear());
    }

    public File[] chooseHistogramFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Histogram File");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.showOpenDialog(this);
        fileChooser.setDragEnabled(true);
        return fileChooser.getSelectedFiles();
    }

    public void openHistogramFile() {
        List<File> files = Arrays.asList(chooseHistogramFiles());
        files.forEach(file -> {
            String filename = file.toString();
            try {
                readHistogram(filename);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "No histogram has been found in\n" + filename, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void readHistogram(String filename) throws Exception {
        Histogram histogram = Histogram.readHistogramFile(filename);
        int index = histograms.size() + 1;
        histogram.setName(index + ". " + filename);
        histograms.addElement(histogram);
    }

    void showHistogram(int id) {
        final Histogram histo = histograms.get(id);

        JFrame histoFrame = new JFrame(histo.getName());
        histoFrame.setSize(700, 700);
        histoFrame.setLocationRelativeTo(this);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("Tools");

        JMenuItem menuItem = new JMenuItem("Export Histogram");
        menu.add(menuItem);

        menubar.add(menu);
        histoFrame.setJMenuBar(menubar);
        menuItem.addActionListener(e -> saveHistogram(histo));

        Vector rows = new Vector();
        Collection<HistoEntry> a = histo.values();
        TreeSet t = new TreeSet(a);

        Iterator it = t.iterator();
        while (it.hasNext()) {
            HistoEntry entry = (HistoEntry) it.next();
            Vector rowData = new Vector();
            rowData.add(entry.getSize());
            rowData.add(entry.getCount());
            rowData.add(entry.getClassName());
            rows.add(rowData);
        }

        JTable table = getTable(rows);

        JScrollPane scrollPane = new JScrollPane(table);
        histoFrame.add(scrollPane);
        histoFrame.setVisible(true);
    }

    private JTable getTable(Vector rows) {
        Vector colData = new Vector(COLUMN_LABELS);
        JTable table = new JTable(rows, colData);
        table.setVisible(true);
        table.setFillsViewportHeight(true);

        TableRowSorter<TableModel> sorter = getRowSorter(table);
        table.setRowSorter(sorter);
        return table;
    }

    private TableRowSorter<TableModel> getRowSorter(JTable table) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        sorter.setComparator(0, new NumComparator());
        sorter.setComparator(1, new NumComparator());
        sorter.toggleSortOrder(0);
        return sorter;
    }

    void showHistoCompareWindow(int id1, int id2) {
        Histogram histo1 = histograms.get(id1);
        Histogram histo2 = histograms.get(id2);
        final HashMap<String, HistoEntry> histo_diff = new HashMap<>();

        int i = id1 + 1;
        int j = id2 + 1;
        JFrame histoFrame = new JFrame("Compare Histograms: Histogram " + i + " - Histogram " + j);
        histoFrame.setSize(700, 700);
        histoFrame.setLocationRelativeTo(this);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("Tools");

        JMenuItem menuItem = new JMenuItem("Export Histograms diffs");
        menu.add(menuItem);

        menubar.add(menu);
        histoFrame.setJMenuBar(menubar);

        Collection<HistoEntry> a = histo1.values();
        TreeSet t = new TreeSet(a);

        Vector<Vector> rows = new Vector();
        for (Object o : t) {
            HistoEntry entry1 = (HistoEntry) o;
            if (histo2.containsKey(entry1.getClassName())) {
                Vector rowdata = new Vector();
                rowdata.add(entry1.getClassName());

                HistoEntry entry2 = histo2.get(entry1.getClassName());
                long diff_size = entry2.getSize() - entry1.getSize();
                rowdata.add(diff_size);

                long diff_count = entry2.getCount() - entry1.getCount();
                rowdata.add(diff_count);
                rows.add(rowdata);

                HistoEntry entry_diff = new HistoEntry(diff_size, diff_count, entry1.getClassName());

                histo_diff.put(entry_diff.getClassName(), entry_diff);
            }
        }

        menuItem.addActionListener(e -> saveHistogramDiff(histo_diff));

        renderScrollPane(histoFrame, rows);
        histoFrame.setVisible(true);
    }

    private void renderScrollPane(JFrame histoFrame, Vector<Vector> rows) {
        JTable table = renderTable(rows);

        JScrollPane scrollPane = new JScrollPane(table);
        histoFrame.add(scrollPane);
    }

    private JTable renderTable(Vector rows) {
        Vector<String> columns = new Vector(COLUMN_LABELS);
        JTable table = new JTable(rows, columns);

        table.setVisible(true);
        table.setFillsViewportHeight(true);


        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        sorter.setComparator(1, new NumComparator());
        sorter.setComparator(2, new NumComparator());
        sorter.toggleSortOrder(1);
        return table;
    }

    void saveHistogram(Histogram histo) {
        try {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Histogram to File...");
            fileChooser.setDragEnabled(true);
            int retval = fileChooser.showSaveDialog(new JFrame());
            if (retval != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = fileChooser.getSelectedFile();
            saveHistogramToFile(histo, file);
            JOptionPane.showMessageDialog(this, "Exported Histogram to file", "Saved File", 1);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void saveHistogramToFile(Histogram histo, File file) throws IOException {
        if (file == null) {
            return;
        }
        if (file.exists()) {
            int ret = JOptionPane.showConfirmDialog(null, "File already exists, Do you want to overwrite this file");
            if (ret != 0) {
                return;
            }
        }

        Collection a = histo.values();
        TreeSet t = new TreeSet(histoEntryComparator);
        t.addAll(a);
        Iterator it = t.iterator();

        BufferedWriter writer;
        writer = new BufferedWriter(new FileWriter(file));
        String lineSeparator = System.getProperty("line.separator");

        writer.write("Size");
        writer.write('\t');
        writer.write('\t');
        writer.write("Count");
        writer.write('\t');
        writer.write('\t');
        writer.write("Class description");
        writer.write(lineSeparator);

        while (it.hasNext()) {
            try {
                HistoEntry entry = (HistoEntry) it.next();

                writer.write(Long.toString(entry.getSize()));
                writer.write('\t');
                writer.write(Long.toString(entry.getCount()));
                writer.write('\t');
                writer.write(entry.getClassName());
                writer.write(lineSeparator);
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }

        writer.close();
    }

    void saveHistogramDiff(HashMap histo) {
        try {
            BufferedWriter writer;

            JFileChooser d = new JFileChooser();
            d.setDialogTitle("Save Histogram Diffs to File...");
            d.setDragEnabled(true);

            int retval = d.showSaveDialog(new JFrame());
            if (retval != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = d.getSelectedFile();
            if (file == null) {
                return;
            }

            if (file.exists()) {
                int ret = JOptionPane.showConfirmDialog(null, "File already exists, do you want to overwtite this file");
                if (ret != 0) {
                    return;
                }
            }
            Collection a = histo.values();
            TreeSet t = new TreeSet(histoEntryComparator);
            t.addAll(a);
            Iterator it = t.iterator();
            writer = new BufferedWriter(new FileWriter(file));
            String lineSeparator = System.getProperty("line.separator");

            writer.write("Size diff");
            writer.write('\t');
            writer.write("Count diff");
            writer.write('\t');
            writer.write("Class description");
            writer.write(lineSeparator);

            while (it.hasNext()) {
                try {
                    HistoEntry entry = (HistoEntry) it.next();

                    writer.write(Long.toString(entry.getSize()));
                    writer.write('\t');
                    writer.write('\t');
                    writer.write(Long.toString(entry.getCount()));
                    writer.write('\t');
                    writer.write('\t');
                    writer.write(entry.getClassName());
                    writer.write(lineSeparator);
                } catch (IOException ignored) {}
            }

            writer.close();

            JOptionPane.showMessageDialog(null, "Exported Histogram Diffs to file", "Saved File", 1);
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }
}
