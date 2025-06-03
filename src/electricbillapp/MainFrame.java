package electricbillapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private CustomerPanel customerPanel;
    private BillPanel billPanel;
    private AdminProfilePanel adminProfilePanel;
    private DashboardPanel dashboardPanel;
    private BillingHistoryPanel billingHistoryPanel;

    private int adminId;
    private String adminUsername;
    private String adminName;

    public MainFrame(int adminId, String adminUsername, String adminName) {
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.adminName = adminName;

        setTitle("Electric Bill Management - Admin: " + adminUsername);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, "Exit application?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        // Sidebar setup
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(230, 240, 255));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(170, getHeight()));

        JLabel lblLogo = new JLabel("E-Bill Admin", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(Color.BLUE);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblLogo);

        JButton btnDashboard = createSidebarButton(" Dashboard", null, "View system summary");
        JButton btnCustomers = createSidebarButton(" Customer List", null, "Manage customer records");
        JButton btnReading = createSidebarButton(" Reading List", null, "Manage reading records");
        JButton btnBill = createSidebarButton(" Print Bill", null, "Print bills for customers");
        JButton btnHistory = createSidebarButton(" Billing History", null, "View past bills");
        JButton btnProfile = createSidebarButton(" Profile", null, "Admin profile and details");
        JButton btnLogout = createSidebarButton(" Logout", null, "Logout of the system");

        btnDashboard.addActionListener(e -> showDashboard());
        btnCustomers.addActionListener(e -> showCustomers());
        btnReading.addActionListener(e -> showReading());
        btnBill.addActionListener(e -> showBillPanel());
        btnHistory.addActionListener(e -> showBillingHistory());
        btnProfile.addActionListener(e -> showProfile());
        btnLogout.addActionListener(e -> logout());

        sidebar.add(btnDashboard);
        sidebar.add(btnCustomers);
        sidebar.add(btnReading);
        sidebar.add(btnBill);
        sidebar.add(btnHistory);
        sidebar.add(btnProfile);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        // Initialize content panel and main views
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(230, 240, 255));

        
        Reading readingPanel = new Reading(this);
        customerPanel = new CustomerPanel(this, readingPanel);
        billPanel = new BillPanel(this);
        adminProfilePanel = new AdminProfilePanel(this, adminId);
        dashboardPanel = new DashboardPanel(this, adminName); // Use adminName
        billingHistoryPanel = new BillingHistoryPanel(this);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(customerPanel, "customers");
        contentPanel.add(readingPanel, "reading");
        contentPanel.add(billPanel, "bill");
        contentPanel.add(adminProfilePanel, "profile");
        contentPanel.add(billingHistoryPanel, "history");

        add(contentPanel, BorderLayout.CENTER);

        showDashboard(); // default view
    }

    private JButton createSidebarButton(String text, String iconPath, String tooltip) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(160, 40));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(230, 240, 255));
        button.setForeground(Color.BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setToolTipText(tooltip);

        Color defaultBg = button.getBackground();
        Color hoverBg = new Color(200, 220, 255);
        Color hoverFg = Color.WHITE;

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverBg);
                button.setForeground(hoverFg);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultBg);
                button.setForeground(Color.BLUE);
            }
        });

        return button;
    }

    public void showDashboard() {
        dashboardPanel.loadDashboardData();
        cardLayout.show(contentPanel, "dashboard");
    }

    public void showReading() {
    Reading reading = new Reading(this);
    cardLayout.show(contentPanel, "reading");
}

    public void showCustomers() {
        customerPanel.refreshCustomerTable();
        cardLayout.show(contentPanel, "customers");
    }

    public void showBillPanel() {
        Customer selected = customerPanel.getSelectedCustomer();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the Customer List first.", "No Customer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        billPanel.setCustomer(selected);
        cardLayout.show(contentPanel, "bill");
    }

    public void showBillingHistory() {
        billingHistoryPanel.loadHistory();
        cardLayout.show(contentPanel, "history");
    }

    public void showProfile() {
        adminProfilePanel.loadAdminDetails();
        cardLayout.show(contentPanel, "profile");
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    public int getAdminId() {
        return adminId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminName() {
        return adminName;
    }
}
