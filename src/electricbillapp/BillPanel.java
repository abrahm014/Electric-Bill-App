package electricbillapp;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BillPanel extends JPanel implements Printable {
    private JTextArea txtBillArea;
    private JButton btnPrint, btnBack;
    private MainFrame mainFrame;
    private Customer customer;
    private double currentBillAmount;
    private double previousBillAmount;

    // Constants
    private static final double RATE_PER_UNIT = 12.0;
    private static final double SENIOR_DISCOUNT = 0.05;
    private static final double VAT_RATE = 0.12;
    private static final double PENALTY_AMOUNT = 50.0;

    public BillPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtBillArea = new JTextArea();
        txtBillArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtBillArea.setEditable(false);
        txtBillArea.setBackground(new Color(245, 245, 245));
        txtBillArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(txtBillArea);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnPrint = createButton(" Print Bill", new Color(46, 204, 113));
        btnBack = createButton("  Back", new Color(231, 76, 60));

        btnPanel.add(btnBack);
        btnPanel.add(btnPrint);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        btnPrint.addActionListener(e -> printBill());
        btnBack.addActionListener(e -> mainFrame.showCustomers());
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public void setCustomer(Customer customer) {
        this.customer = fetchCustomerById(customer.getId());
        if (this.customer != null) {
            fetchCurrentAndPreviousBills(customer.getMeterNo());
            generateBillText();
        } else {
            txtBillArea.setText(" Error loading customer data.");
        }
    }

    private Customer fetchCustomerById(int id) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM customers WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("contact"),
                        rs.getInt("units_consumed"),
                        rs.getBoolean("senior"),
                        rs.getString("meter_no"),
                        rs.getString("month"),
                        rs.getString("status"),
                        rs.getString("action_status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch customer data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

  private void fetchCurrentAndPreviousBills(String meterNo) {
    try (Connection conn = DBHelper.getConnection()) {
        // Step 1: Fetch units consumed from customer table for current bill
        String currentUnitsQuery = "SELECT units_consumed FROM customers WHERE meter_no = ?";
        PreparedStatement currentStmt = conn.prepareStatement(currentUnitsQuery);
        currentStmt.setString(1, meterNo);
        ResultSet currentRs = currentStmt.executeQuery();

        if (currentRs.next()) {
            int unitsConsumed = currentRs.getInt("units_consumed");
            currentBillAmount = calculateBill(unitsConsumed);
        } else {
            currentBillAmount = 0;
        }

        // Step 2: Fetch previous reading from readings table
        String readingsQuery = "SELECT reading_value FROM readings WHERE meter_no = ? ORDER BY reading_date DESC LIMIT 2";
        PreparedStatement stmt = conn.prepareStatement(readingsQuery);
        stmt.setString(1, meterNo);
        ResultSet rs = stmt.executeQuery();

        int[] readings = new int[2];
        int index = 0;

        while (rs.next() && index < 2) {
            readings[index++] = rs.getInt("reading_value");
        }

        if (index == 2) {
            int previousUnits = readings[1]; // Previous reading used for approximation
            previousBillAmount = calculateBill(previousUnits);
        } else {
            previousBillAmount = 0;
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error fetching bills: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private double calculateBill(int reading) {
        return reading * RATE_PER_UNIT;
    }

    private void generateBillText() {
        if (customer == null) {
            txtBillArea.setText("");
            return;
        }

        int kWh = customer.getUnitsConsumed();
        double energyCost = kWh * RATE_PER_UNIT;
        double discount = customer.isSenior() ? energyCost * SENIOR_DISCOUNT : 0;
        double vatSales = energyCost - discount;
        double vatAmount = vatSales * VAT_RATE;
        double penalty = 0;

        LocalDate billingDate = LocalDate.now();
        LocalDate dueDate = billingDate.plusDays(5);

        // Check if late
        if (LocalDate.now().isAfter(dueDate)) {
            penalty = PENALTY_AMOUNT;
        }

        double total = vatSales + vatAmount + penalty;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String billingMonth = billingDate.format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("              BRASH ELECTRIC BILL \n");
        sb.append("          Billing Statement Notice\n");
        sb.append("==============================================\n");
        sb.append("  Name        : ").append(customer.getName()).append("\n");
        sb.append("  Address     : ").append(customer.getAddress()).append("\n");
        sb.append("  Current Bill : ₱").append(String.format("%.2f", currentBillAmount)).append("\n");
        sb.append("  Previous Bill: ₱").append(String.format("%.2f", previousBillAmount)).append("\n");
        sb.append("  Billing For : ").append(billingMonth).append("\n");
        sb.append("  Meter No.   : ").append(customer.getMeterNo()).append("\n");
        sb.append("  Bill No.    : BA-").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).append("01\n");
        sb.append("==============================================\n");
        sb.append("  Reading Summary\n");
        sb.append(String.format("    Units Consumed  : %d kWh\n", kWh));
        sb.append(String.format("    Rate per kWh    : ₱%.2f\n", RATE_PER_UNIT));
        sb.append(String.format("    Energy Cost     : ₱%.2f\n", energyCost));
        if (customer.isSenior()) {
            sb.append(String.format("    - Senior Discount (5%%): -₱%.2f\n", discount));
        }

        sb.append("==============================================\n");
        sb.append("   Charges & Tax\n");
        sb.append(String.format("    VATable Sales   : ₱%.2f\n", vatSales));
        sb.append(String.format("    VAT (12%%)       : ₱%.2f\n", vatAmount));
        if (penalty > 0) {
            sb.append(String.format("    Late Penalty    : ₱%.2f\n", penalty));
        }
        sb.append("==============================================\n");
        sb.append(String.format("    Total Amount Due: ₱%.2f\n", total));
        sb.append("==============================================\n");
        sb.append("   Due Date : ").append(dueDate).append("\n");
        sb.append("   Avoid disconnection. Pay before due date.\n");

        txtBillArea.setText(sb.toString());
    }

    private void saveBillToDatabase() {
        if (customer == null) return;

        int kWh = customer.getUnitsConsumed();
        double rate = RATE_PER_UNIT;
        double energyCost = kWh * rate;
        double discount = customer.isSenior() ? energyCost * SENIOR_DISCOUNT : 0;
        double vatSales = energyCost - discount;
        double vatAmount = vatSales * VAT_RATE;
        double penalty = 0;

        LocalDate billingDate = LocalDate.now();
        LocalDate dueDate = billingDate.plusDays(5);
        if (LocalDate.now().isAfter(dueDate)) {
            penalty = PENALTY_AMOUNT;
        }

        double total = vatSales + vatAmount + penalty;

        String currentMonth = billingDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);

        if (customer.getMonth() != null && customer.getMonth().equalsIgnoreCase(currentMonth)) {
            JOptionPane.showMessageDialog(this, "Customer has already been billed for " + currentMonth + ".", "Duplicate Billing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);

            String updateMonthSQL = "UPDATE customers SET month = ? WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateMonthSQL)) {
                updateStmt.setString(1, currentMonth);
                updateStmt.setInt(2, customer.getId());
                updateStmt.executeUpdate();
            }

            String insertBillSQL = "INSERT INTO bills (customer_id, customer_name, customer_address, meter_no, kwh_used, rate_per_kwh, energy_cost, discount, vat_sales, vat_amount, penalty, total_amount, billing_date, due_date) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertBillSQL)) {
                stmt.setInt(1, customer.getId());
                stmt.setString(2, customer.getName());
                stmt.setString(3, customer.getAddress());
                stmt.setString(4, customer.getMeterNo());
                stmt.setInt(5, kWh);
                stmt.setDouble(6, rate);
                stmt.setDouble(7, energyCost);
                stmt.setDouble(8, discount);
                stmt.setDouble(9, vatSales);
                stmt.setDouble(10, vatAmount);
                stmt.setDouble(11, penalty);
                stmt.setDouble(12, total);
                stmt.setDate(13, java.sql.Date.valueOf(billingDate));
                stmt.setDate(14, java.sql.Date.valueOf(dueDate));
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save bill: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printBill() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Print Electric Bill");
        job.setPrintable(this);

        if (job.printDialog()) {
            try {
                job.print();
                saveBillToDatabase();
                JOptionPane.showMessageDialog(this, "Bill successfully printed.", "Print Complete", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.showBillingHistory();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        txtBillArea.printAll(g);
        return PAGE_EXISTS;
    }
}
