package electricbillapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.TableCellRenderer;

public class BillingHistoryPanel extends JPanel {
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton markAsPaidButton;

    public BillingHistoryPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 255)); 

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 240, 255));

        JLabel titleLabel = new JLabel("Billing History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 80, 160));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JLabel searchLabel = new JLabel("Search Meter No. : ");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchLabel.setForeground(Color.DARK_GRAY);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(Color.BLACK);
        searchField.setCaretColor(Color.BLACK);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadHistory(searchField.getText().trim());
            }
        });
        
        markAsPaidButton = new JButton("Mark as Paid");
        markAsPaidButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        markAsPaidButton.addActionListener(e -> markSelectedAsPaid());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(230, 240, 255));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {
            "Customer ID", "Customer Name", "Address", "Meter No", "Billing Date",
            "kWh Used", "Energy Cost", "Discount", "VAT", "Penalty", "Total", "Due Date", "Payment"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(245, 245, 245) : Color.WHITE);
                } else {
                    c.setBackground(new Color(200, 230, 255));
                }
                return c;
            }
        };

        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyTable.setRowHeight(28);
        historyTable.setForeground(Color.BLACK);
        historyTable.setSelectionForeground(Color.BLACK);
        historyTable.setGridColor(Color.LIGHT_GRAY);

        historyTable.getTableHeader().setBackground(new Color(60, 63, 65));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTable.getTableHeader().setReorderingAllowed(false);

        // === Bottom panel with Mark as Paid button ===
        
        markAsPaidButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        markAsPaidButton.setBackground(new Color(40, 150, 90));
        markAsPaidButton.setForeground(Color.WHITE);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(230, 240, 255));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        bottomPanel.add(markAsPaidButton);

        add(bottomPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        add(scrollPane, BorderLayout.CENTER);

        loadHistory("");
    }

    public void loadHistory() {
        loadHistory("");
    }

    public void loadHistory(String searchText) {
        tableModel.setRowCount(0);

        String url = "jdbc:mysql://localhost:3306/electric_billing";
        String user = "root";
        String password = "";

        String query = "SELECT customer_id, customer_name, customer_address, meter_no, billing_date, kwh_used, energy_cost, discount, vat_amount, penalty, total_amount, due_date, is_paid " +
                       "FROM bills WHERE meter_no LIKE ? ORDER BY billing_date DESC";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + searchText + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_address"),
                        rs.getString("meter_no"),
                        rs.getDate("billing_date"),
                        rs.getInt("kwh_used"),
                        "₱" + rs.getDouble("energy_cost"),
                        "₱-" + rs.getDouble("discount"),
                        "₱" + rs.getDouble("vat_amount"),
                        "₱" + rs.getDouble("penalty"),
                        "₱" + rs.getDouble("total_amount"),
                        rs.getDate("due_date"),
                        rs.getBoolean("is_paid") ? "Paid" : "Unpaid"
                    };
                    tableModel.addRow(row);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markSelectedAsPaid() {
    int selectedRow = historyTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to mark as paid.");
        return;
    }

    int customerId = (int) tableModel.getValueAt(selectedRow, 0);
    java.sql.Date billingDate = (java.sql.Date) tableModel.getValueAt(selectedRow, 4);

    try (Connection conn = DBHelper.getConnection()) {
        conn.setAutoCommit(false); // Start transaction

        // Update bill as paid
        PreparedStatement billStmt = conn.prepareStatement(
            "UPDATE bills SET is_paid = 1 WHERE customer_id = ? AND billing_date = ?"
        );
        billStmt.setInt(1, customerId);
        billStmt.setDate(2, billingDate);
        int updated = billStmt.executeUpdate();

        if (updated > 0) {
            // Update customer action_status
            PreparedStatement customerStmt = conn.prepareStatement(
                "UPDATE customers SET action_status = 'Paid' WHERE id = ?"
            );
            customerStmt.setInt(1, customerId);
            customerStmt.executeUpdate();

            conn.commit(); // Commit both updates
            JOptionPane.showMessageDialog(this, "Marked as paid successfully.");
            loadHistory(searchField.getText().trim());
        } else {
            conn.rollback(); // Revert if nothing was updated
            JOptionPane.showMessageDialog(this, "No matching bill found to update.");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error updating bill: " + e.getMessage());
    }
}
}
