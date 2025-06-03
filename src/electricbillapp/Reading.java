package electricbillapp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;
import java.sql.*;

public class Reading extends JPanel {
    private JTable tblReadings;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;

    public Reading(MainFrame mainFrame) {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 255));

        // === Header panel with title and search ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 240, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("Readings List");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 80, 160));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JLabel searchLabel = new JLabel("Search Meter No. : ");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(Color.DARK_GRAY);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 30));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setForeground(Color.BLACK);
        txtSearch.setCaretColor(Color.BLACK);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(230, 240, 255));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        searchPanel.add(searchLabel);
        searchPanel.add(txtSearch);

        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // === Table setup ===
        String[] columns = {"ID", "Meter No", "Reading Date", "Current Reading", "Previous Reading"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevents editing any cell
            }
        };
        tblReadings = new JTable(tableModel) {
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(245, 245, 245) : Color.WHITE);
                } else {
                    c.setBackground(new Color(200, 230, 255));
                }
                return c;
            }
        };
        tblReadings.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblReadings.setRowHeight(28);
        tblReadings.setForeground(Color.BLACK);
        tblReadings.setSelectionForeground(Color.BLACK);
        tblReadings.setGridColor(Color.LIGHT_GRAY);

        tblReadings.getTableHeader().setBackground(new Color(60, 63, 65));
        tblReadings.getTableHeader().setForeground(Color.WHITE);
        tblReadings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblReadings.getTableHeader().setReorderingAllowed(false);

        JButton btnDelete = new JButton("Delete Selected Reading");
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnDelete.setBackground(new Color(200, 50, 50));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btnDelete.addActionListener(e -> deleteSelectedReading());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(230, 240, 255));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        bottomPanel.add(btnDelete);

        add(bottomPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(tblReadings);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        add(scrollPane, BorderLayout.CENTER);

        // === Search functionality ===
        rowSorter = new TableRowSorter<>(tableModel);
        tblReadings.setRowSorter(rowSorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void filterTable() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Load data
        loadReadings();
    }

    private void loadReadings() {
        tableModel.setRowCount(0);

        // Query for all readings ordered by meter_no and reading_date ascending to track previous readings correctly
        String query = "SELECT meter_no, reading_date, reading_value FROM readings ORDER BY meter_no ASC, reading_date ASC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            Map<String, Integer> meterLastReadingMap = new HashMap<>();
            int idCounter = 1;

            while (rs.next()) {
                String meterNo = rs.getString("meter_no");
                Date readingDate = rs.getDate("reading_date");
                int currentReading = rs.getInt("reading_value");

                // Get previous reading for this meter, default to 0 if none exists yet
                int previousReading = meterLastReadingMap.getOrDefault(meterNo, 0);

                // Add row with current and previous readings
                Object[] row = {
                    idCounter++,
                    meterNo,
                    readingDate,
                    currentReading,
                    previousReading
                };
                tableModel.addRow(row);

                // Update map with current reading for next iteration
                meterLastReadingMap.put(meterNo, currentReading);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading readings: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedReading() {
        int selectedRow = tblReadings.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reading to delete.");
            return;
        }

        int modelRow = tblReadings.convertRowIndexToModel(selectedRow);
        String meterNo = tableModel.getValueAt(modelRow, 1).toString();
        Date readingDate = (Date) tableModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the reading for meter " + meterNo + " on " + readingDate + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection()) {
                String deleteQuery = "DELETE FROM readings WHERE meter_no = ? AND reading_date = ?";
                PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                stmt.setString(1, meterNo);
                stmt.setDate(2, new java.sql.Date(readingDate.getTime()));
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Reading deleted successfully.");
                    loadReadings();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete reading.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting reading: " + ex.getMessage());
            }
        }
    }

    public void refreshReadingsTable() {
        // Optional method to reload table, not related to current display logic
        loadReadings();
    }
}

