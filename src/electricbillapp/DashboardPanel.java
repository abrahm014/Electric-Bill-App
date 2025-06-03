package electricbillapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardPanel extends JPanel {
    private JLabel customerCountLabel;
    private JLabel billCountLabel;
    private JLabel revenueLabel;
    private JLabel monthlyRevenueLabel;
    private JLabel welcomeLabel;
    private JPanel contentPanel;

    public DashboardPanel(MainFrame frame, String adminName) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(230, 240, 255));
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("BRASH ELECTRIC BILL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 80, 160));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(245, 245, 245));

        // Welcome message
        welcomeLabel = new JLabel("Welcome, Admin " + adminName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 0));
        centerPanel.add(welcomeLabel);

        // Cards panel
        contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel customerCard = createCardPanel("Total Customers", "0", "icons/customer.png");
        customerCountLabel = (JLabel) customerCard.getClientProperty("valueLabel");

        JPanel billCard = createCardPanel("Total Bills", "0", "icons/bills.png");
        billCountLabel = (JLabel) billCard.getClientProperty("valueLabel");
        
        JPanel monthlyRevenueCard = createCardPanel("Monthly Revenue", "₱0.00", "icons/calendar.png");
        monthlyRevenueLabel = (JLabel) monthlyRevenueCard.getClientProperty("valueLabel");

        JPanel revenueCard = createCardPanel("Total Revenue", "₱0.00", "icons/revenue.png");
        revenueLabel = (JLabel) revenueCard.getClientProperty("valueLabel");


        // Hide initially for animation
        customerCard.setVisible(false);
        billCard.setVisible(false);
        revenueCard.setVisible(false);
        monthlyRevenueCard.setVisible(false);

        contentPanel.add(customerCard);
        contentPanel.add(billCard);
        contentPanel.add(monthlyRevenueCard);
        contentPanel.add(revenueCard);
 

        centerPanel.add(contentPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Animate card appearance
        animateCards(new JPanel[]{customerCard, billCard, revenueCard, monthlyRevenueCard});
    }

  private JPanel createCardPanel(String title, String value, String iconPath) {
    JPanel card = new JPanel(new BorderLayout(8, 8));
    card.setPreferredSize(new Dimension(140, 90));  // Smaller box
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    ImageIcon icon = new ImageIcon(iconPath);
    Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
    JLabel iconLabel = new JLabel(new ImageIcon(img));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    titleLabel.setForeground(new Color(100, 100, 100));

    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    valueLabel.setForeground(new Color(33, 150, 243));

    JPanel textPanel = new JPanel(new BorderLayout());
    textPanel.setOpaque(false);
    textPanel.add(titleLabel, BorderLayout.NORTH);
    textPanel.add(valueLabel, BorderLayout.CENTER);

    card.add(iconLabel, BorderLayout.WEST);
    card.add(textPanel, BorderLayout.CENTER);
    card.putClientProperty("valueLabel", valueLabel);

    return card;
}


    private void animateCards(JPanel[] cards) {
        Timer timer = new Timer();
        final int[] index = {0};

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (index[0] < cards.length) {
                    int currentIndex = index[0];
                    SwingUtilities.invokeLater(() -> {
                        cards[currentIndex].setVisible(true);
                        contentPanel.revalidate();
                        contentPanel.repaint();
                    });
                    index[0]++;
                } else {
                    timer.cancel();
                }
            }
        }, 300, 250);
    }

   public void loadDashboardData() {
    SwingUtilities.invokeLater(() -> {
        customerCountLabel.setText(String.valueOf(getCustomerCount()));
        billCountLabel.setText(String.valueOf(getBillCount()));
        revenueLabel.setText("₱" + String.format("%,.2f", getTotalRevenue()));

        // Calculate for current month
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        double monthlyRevenue = getMonthlyRevenueByMonth(currentMonth);
        monthlyRevenueLabel.setText("₱" + String.format("%,.2f", monthlyRevenue));
    });
}


    private int getCustomerCount() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/electric_billing", "root", "");
             PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) AS total FROM customers");
             ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching customer count: " + e.getMessage());
            return 0;
        }
    }

    private int getBillCount() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/electric_billing", "root", "");
             PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) AS total FROM bills");
             ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching bill count: " + e.getMessage());
            return 0;
        }
    }

    private double getTotalRevenue() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/electric_billing", "root", "");
             PreparedStatement pst = con.prepareStatement("SELECT SUM(total_amount) AS revenue FROM bills");
             ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getDouble("revenue") : 0.0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching revenue: " + e.getMessage());
            return 0.0;
        }
    }
     private double getMonthlyRevenueByMonth(int month) {
    String sql = "SELECT SUM(total_amount) AS monthly FROM bills " +
                 "WHERE MONTH(billing_date) = ? AND YEAR(billing_date) = YEAR(CURDATE())";

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/electric_billing", "root", "");
         PreparedStatement pst = con.prepareStatement(sql)) {
        pst.setInt(1, month);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getDouble("monthly") : 0.0;
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error fetching monthly revenue: " + e.getMessage());
        return 0.0;
    }
}
}
