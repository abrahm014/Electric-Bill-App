package electricbillapp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class CustomerPanel extends JPanel {
    private JTable tblCustomers;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private MainFrame mainFrame;
    private Reading readingPanel;

    private JButton btnAddReading, btnAdd, btnEdit, btnDelete, btnRefresh, btnUpdateStatus;
    private JTextField searchField;

    public CustomerPanel(MainFrame mainFrame, Reading readingPanel) {
        this.mainFrame = mainFrame;
        this.readingPanel = readingPanel;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(230, 240, 255));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 240, 255));

        // Title label
        JLabel lblTitle = new JLabel("Customer Lists");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 80, 160));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel searchLabel = new JLabel("Search: ");
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

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(230, 240, 255));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(
                new Object[]{"Customer ID", "Name", "Address", "Contact", "Units Consumed", "Senior Citizen", "Meter No", "Month", "Status", "Payment"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCustomers = new JTable(tableModel) {
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

        rowSorter = new TableRowSorter<>((DefaultTableModel) tblCustomers.getModel());
        tblCustomers.setRowSorter(rowSorter);

        tblCustomers.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblCustomers.setRowHeight(28);
        tblCustomers.setForeground(Color.BLACK);
        tblCustomers.setSelectionForeground(Color.BLACK);
        tblCustomers.setGridColor(Color.LIGHT_GRAY);

        JTableHeader header = tblCustomers.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(60, 63, 65));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        TableColumnModel columnModel = tblCustomers.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);  // Customer ID
        columnModel.getColumn(1).setPreferredWidth(150);  // Name
        columnModel.getColumn(2).setPreferredWidth(200);  // Address
        columnModel.getColumn(3).setPreferredWidth(120);  // Contact
        columnModel.getColumn(4).setPreferredWidth(100);  // Units
        columnModel.getColumn(5).setPreferredWidth(120);  // Senior
        columnModel.getColumn(6).setPreferredWidth(100);  // Meter No
        columnModel.getColumn(7).setPreferredWidth(100);  // Month
        columnModel.getColumn(9).setPreferredWidth(100);  // Payment

        JScrollPane scrollPane = new JScrollPane(tblCustomers);
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 6, 10, 0));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btnPanel.setBackground(getBackground());

        btnAdd = new JButton("Add");
        btnEdit = new JButton("Edit");
        btnDelete = new JButton("Delete");
        btnRefresh = new JButton("Refresh");
        btnAddReading = new JButton("Add Reading");
        btnUpdateStatus = new JButton("Update Status");

        btnAddReading.addActionListener(e -> {
            Customer selected = getSelectedCustomer();
            if (selected != null) {
                JPanel panels = new JPanel(new GridLayout(2, 2, 10, 10));
                JTextField txtReading = new JTextField();
                JTextField txtDate = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

                panels.add(new JLabel("Reading:"));
                panels.add(txtReading);
                panels.add(new JLabel("Date (yyyy-MM-dd):"));
                panels.add(txtDate);

                int result = JOptionPane.showConfirmDialog(this, panels, "Add Reading", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        int newReading = Integer.parseInt(txtReading.getText().trim());
                        java.sql.Date sqlDate = java.sql.Date.valueOf(txtDate.getText().trim());

                        // Add the new reading to the database
                        addReading(selected.getMeterNo(), newReading, sqlDate);

                        // Update the customer's units consumed
                        updateCustomerUnitsConsumed(selected.getMeterNo(), newReading);

                        // Refresh the customer table to reflect changes
                        refreshCustomerTable();

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid reading input. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error adding reading: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton[] buttons = {btnAddReading, btnAdd, btnEdit, btnDelete, btnRefresh, btnUpdateStatus};
        for (JButton btn : buttons) {
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.setBackground(new Color(0, 120, 215));
            btn.setForeground(Color.WHITE);
            btnPanel.add(btn);
        }

        add(btnPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addCustomer());
        btnEdit.addActionListener(e -> editCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnRefresh.addActionListener(e -> refreshCustomerTable());
        btnUpdateStatus.addActionListener(e -> updateCustomerStatus());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void search() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2));
                }
            }
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });

        refreshCustomerTable();
    }

    public void refreshCustomerTable() {
        tableModel.setRowCount(0);

        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT c.*, b.is_paid, b.due_date 
                FROM customers c 
                LEFT JOIN bills b ON c.id = b.customer_id 
                ORDER BY c.id DESC
            """);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Date dueDate = rs.getDate("due_date");
                boolean isPaid = rs.getBoolean("is_paid");
                String status;

                if (isPaid) {
                    status = "Paid";
                } else if (dueDate != null && new java.util.Date().after(dueDate)) {
                    status = "Overdue";
                } else {
                    status = "Up to date";
                }

                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("contact"),
                        rs.getInt("units_consumed"),
                        rs.getBoolean("senior") ? "Yes" : "No",
                        rs.getString("meter_no"),
                        rs.getString("month"),
                        status,
                        rs.getString("action_status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error refreshing table: " + e.getMessage());
        }
    }

    private void updateCustomerStatus() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to update status.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = {"Up to date", "Overdue"};
        String newStatus = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Status", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (newStatus != null) {
            try (Connection conn = DBHelper.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE customers SET status = ? WHERE id = ?");
                ps.setString(1, newStatus);
                ps.setInt(2, selected.getId());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Status updated successfully.");
                refreshCustomerTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateCustomerUnitsConsumed(String meterNo, int newReading) {
        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement prevReadingStmt = conn.prepareStatement(
                "SELECT reading_value FROM readings WHERE meter_no = ? ORDER BY reading_date DESC LIMIT 1 OFFSET 1");
            prevReadingStmt.setString(1, meterNo);
            ResultSet prevReadingRs = prevReadingStmt.executeQuery();

            int previousReadingValue = 0;
            if (prevReadingRs.next()) {
                previousReadingValue = prevReadingRs.getInt("reading_value");
            }

            int consumed = Math.abs(newReading - previousReadingValue);

            PreparedStatement updateUnitsStmt = conn.prepareStatement(
                "UPDATE customers SET units_consumed = ? WHERE meter_no = ?");
            updateUnitsStmt.setInt(1, consumed);
            updateUnitsStmt.setString(2, meterNo);
            updateUnitsStmt.executeUpdate();

            System.out.println("Updated units consumed for meter_no " + meterNo + " = " + consumed);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating units consumed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateUniqueMeterNo() {
        try (Connection conn = DBHelper.getConnection()) {
            String newMeterNo;
            int nextId = 1;

            while (true) {
                newMeterNo = String.format("MTR-%05d", nextId);
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM customers c LEFT JOIN bills b ON c.id = b.customer_id WHERE c.meter_no = ?"
                );
                checkStmt.setString(1, newMeterNo);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    return newMeterNo;
                }
                nextId++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "MTR-00001";
    }

    public Customer getSelectedCustomer() {
        int row = tblCustomers.getSelectedRow();
        if (row == -1) return null;
        int modelRow = tblCustomers.convertRowIndexToModel(row);
        return new Customer(
            (int) tableModel.getValueAt(modelRow, 0),
            (String) tableModel.getValueAt(modelRow, 1),
            (String) tableModel.getValueAt(modelRow, 2),
            (String) tableModel.getValueAt(modelRow, 3),
            (int) tableModel.getValueAt(modelRow, 4),
            "Yes".equals(tableModel.getValueAt(modelRow, 5)),
            (String) tableModel.getValueAt(modelRow, 6),
            (String) tableModel.getValueAt(modelRow, 7),
            (String) tableModel.getValueAt(modelRow, 8),
            (String) tableModel.getValueAt(modelRow, 9)
        );
    }

    private void addReading(String meterNo, int newReadingValue, Date readingDate) {
        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement prevStmt = conn.prepareStatement(
                "SELECT reading_value FROM readings WHERE meter_no = ? ORDER BY reading_date ASC LIMIT 1"
            );
            prevStmt.setString(1, meterNo);
            ResultSet prevRs = prevStmt.executeQuery();

            int previousReadingValue = 0;
            if (prevRs.next()) {
                previousReadingValue = prevRs.getInt("reading_value");
            }

            int unitsConsumed = newReadingValue - previousReadingValue;
            if (unitsConsumed < 0) unitsConsumed = 0;

            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO readings (meter_no, reading_date, reading_value, previous_reading_value, units_consumed) " +
                "VALUES (?, ?, ?, ?, ?)"
            );
            insertStmt.setString(1, meterNo);
            insertStmt.setDate(2, new java.sql.Date(readingDate.getTime()));
            insertStmt.setInt(3, newReadingValue);
            insertStmt.setInt(4, previousReadingValue);
            insertStmt.setInt(5, unitsConsumed);
            insertStmt.executeUpdate();

            PreparedStatement updateCustomerStmt = conn.prepareStatement(
                "UPDATE customers SET units_consumed = ? WHERE meter_no = ?"
            );
            updateCustomerStmt.setInt(1, unitsConsumed);
            updateCustomerStmt.setString(2, meterNo);
            updateCustomerStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Reading added successfully.");

            readingPanel.refreshReadingsTable();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding reading: " + e.getMessage());
        }
    }

    private void addCustomer() {
        CustomerDialog dlg = new CustomerDialog(null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshCustomerTable();
        }
    }

    private void editCustomer() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CustomerDialog dlg = new CustomerDialog(selected);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshCustomerTable();
        }
    }

    private void deleteCustomer() {
        Customer selected = getSelectedCustomer();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete customer: " + selected.getName() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM bills WHERE customer_id = ?")) {
                    ps1.setInt(1, selected.getId());
                    ps1.executeUpdate();
                }

                try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM customers WHERE id = ?")) {
                    ps2.setInt(1, selected.getId());
                    ps2.executeUpdate();
                }

                conn.commit();
                refreshCustomerTable();
                JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class CustomerDialog extends JDialog {
        private JTextField txtName, txtAddress, txtMeterNo, txtContact;
        private JLabel lblUnitsConsumed, lblMonth;
        private JCheckBox chkSenior;
        private boolean saved = false;
        private final Customer customer;

        public CustomerDialog(Customer customer) {
            this.customer = customer;
            setTitle(customer == null ? "Add Customer" : "Edit Customer");
            setModal(true);
            setSize(400, 350);
            setLocationRelativeTo(null);
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;

            txtName = new JTextField(20);
            txtAddress = new JTextField(20);
            txtContact = new JTextField(20);
            txtMeterNo = new JTextField(20);
            lblUnitsConsumed = new JLabel();
            lblMonth = new JLabel();
            chkSenior = new JCheckBox();

            addField(gbc, "Name:", txtName, 0);
            addField(gbc, "Address:", txtAddress, 1);
            addField(gbc, "Contact:", txtContact, 2);
            addField(gbc, "Units Consumed:", lblUnitsConsumed, 3);
            addField(gbc, "Senior Citizen:", chkSenior, 4);
            addField(gbc, "Month:", lblMonth, 5);
            addField(gbc, "Meter No:", txtMeterNo, 6);

            JPanel btnPanel = new JPanel();
            JButton btnSave = new JButton("Save");
            JButton btnCancel = new JButton("Cancel");
            btnPanel.add(btnSave);
            btnPanel.add(btnCancel);

            gbc.gridx = 0; gbc.gridy = 7;
            gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            add(btnPanel, gbc);

            if (customer == null) {
                txtMeterNo.setText(generateUniqueMeterNo());
                txtMeterNo.setEditable(false);
                lblUnitsConsumed.setText("0");
                lblMonth.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            } else {
                txtName.setText(customer.getName());
                txtAddress.setText(customer.getAddress());
                txtContact.setText(customer.getContact());
                lblUnitsConsumed.setText(String.valueOf(customer.getUnitsConsumed()));
                lblMonth.setText(customer.getMonth());
                txtMeterNo.setText(customer.getMeterNo());
                txtMeterNo.setEditable(false);
                chkSenior.setSelected(customer.isSenior());
            }

            btnSave.addActionListener(e -> saveCustomer());
            btnCancel.addActionListener(e -> dispose());
        }

        private void addField(GridBagConstraints gbc, String label, JComponent component, int row) {
            gbc.gridx = 0; gbc.gridy = row;
            add(new JLabel(label), gbc);
            gbc.gridx = 1;
            add(component, gbc);
        }

        private void saveCustomer() {
            String name = txtName.getText().trim();
            String address = txtAddress.getText().trim();
            String contact = txtContact.getText().trim();
            String meterNo = txtMeterNo.getText().trim();
            boolean senior = chkSenior.isSelected();
            int units = customer != null ? customer.getUnitsConsumed() : 0;
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            String status = "Up to date";  // Default status for new customers

            if (name.isEmpty() || address.isEmpty() || meterNo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DBHelper.getConnection()) {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM customers WHERE meter_no = ? AND id != ?");
                checkStmt.setString(1, meterNo);
                checkStmt.setInt(2, customer != null ? customer.getId() : -1);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Meter No already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                PreparedStatement ps;
                if (customer == null) {
                    ps = conn.prepareStatement(
                        "INSERT INTO customers (name, address, contact, units_consumed, senior, meter_no, month, status, action_status, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE)"
                    );
                    ps.setString(1, name);
                    ps.setString(2, address);
                    ps.setString(3, contact);
                    ps.setInt(4, units);
                    ps.setBoolean(5, senior);
                    ps.setString(6, meterNo);
                    ps.setString(7, currentMonth);
                    ps.setString(8, status);
                    ps.setString(9, "Pending");
                } else {
                    // Preserve existing status when editing
                    String existingStatus = customer.status != null ? customer.status : "Up to date";
                    ps = conn.prepareStatement("UPDATE customers SET name = ?, address = ?, contact = ?, senior = ?, meter_no = ?, month = ?, status = ? WHERE id = ?");
                    ps.setString(1, name);
                    ps.setString(2, address);
                    ps.setString(3, contact);
                    ps.setBoolean(4, senior);
                    ps.setString(5, meterNo);
                    ps.setString(6, currentMonth);
                    ps.setString(7, existingStatus);
                    ps.setInt(8, customer.getId());
                }
                ps.executeUpdate();
                saved = true;
                dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving customer: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }
}

class Customer {
    private final int id;
    public final String name, address, contact, meterNo, month, status;
    private final int unitsConsumed;
    private final boolean senior;
    private String actionStatus;

    public Customer(int id, String name, String address, String contact, int unitsConsumed, boolean senior, String meterNo, String month, String status, String actionStatus) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.unitsConsumed = unitsConsumed;
        this.senior = senior;
        this.meterNo = meterNo;
        this.month = month;
        this.status = status;
        this.actionStatus = actionStatus;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getContact() { return contact; }
    public int getUnitsConsumed() { return unitsConsumed; }
    public boolean isSenior() { return senior; }
    public String getMeterNo() { return meterNo; }
    public String getMonth() { return month; }
    public String getStatus() { return status; }
    public String getActionStatus() { return actionStatus; }
    public void setActionStatus(String actionStatus) { this.actionStatus = actionStatus; }
}

