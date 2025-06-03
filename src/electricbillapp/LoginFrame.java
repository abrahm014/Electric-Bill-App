package electricbillapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnSignup;

    public LoginFrame() {
        setTitle("Admin Login");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(230, 240, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 10, 14, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = new JLabel("Electric Bill Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 30, 30));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(lblTitle, gbc);
        gbc.anchor = GridBagConstraints.WEST; gbc.gridwidth = 1;

        // Username label and field
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        mainPanel.add(lblUsername, gbc);

        txtUsername = new JTextField(18);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 1;
        mainPanel.add(txtUsername, gbc);

        // Password label and field
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        mainPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(18);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 1;
        mainPanel.add(txtPassword, gbc);

        // Forgot password
        JLabel lblForgotPassword = new JLabel("      Forgot Password?");
        lblForgotPassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblForgotPassword.setForeground(Color.BLUE.darker());
        lblForgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
        });
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(lblForgotPassword, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(230, 240, 255));

        btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(0, 120, 215));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        buttonPanel.add(btnLogin);

        btnSignup = new JButton("Sign Up");
        btnSignup.setBackground(new Color(0, 120, 215));
        btnSignup.setForeground(Color.WHITE);
        btnSignup.setFocusPainted(false);
        btnSignup.setFont(new Font("Segoe UI", Font.BOLD, 15));
        buttonPanel.add(btnSignup);

        gbc.gridx = 1; gbc.gridy = 4;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);

        // Action listeners
        btnLogin.addActionListener(e -> doLogin());
        btnSignup.addActionListener(e -> openSignup());
        txtPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter username and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try (Connection conn = DBHelper.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT * FROM admin WHERE username = ?")) {

        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String dbPassword = rs.getString("password");
            if (dbPassword.equals(password)) {
                int adminId = rs.getInt("id");
                String adminName = rs.getString("name");
                String dbUsername = rs.getString("username");

                dispose();
                MainFrame mainFrame = new MainFrame(adminId, dbUsername, adminName);
                mainFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this, "User not found.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }

        } catch (SQLException ex) {
           JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        }

    private void openSignup() {
        AdminSignupPanel signupFrame = new AdminSignupPanel();
        signupFrame.setVisible(true);
    }

    private void showForgotPasswordDialog() {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog(this);
        dialog.setVisible(true);
    }
}
