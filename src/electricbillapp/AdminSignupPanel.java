package electricbillapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminSignupPanel extends JFrame {
    private JTextField txtUsername, txtName, txtContact, txtEmail;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnSignup, btnCancel;

    public AdminSignupPanel() {
        setTitle("Admin Signup");
        setSize(420, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Main Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(230, 240, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = new JLabel("Create Admin Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 30, 30));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(lblTitle, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(labelStyled("Username:"), gbc);
        txtUsername = new JTextField(18);
        fieldStyled(txtUsername);
        gbc.gridx = 1;
        mainPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(labelStyled("Password:"), gbc);
        txtPassword = new JPasswordField(18);
        fieldStyled(txtPassword);
        gbc.gridx = 1;
        mainPanel.add(txtPassword, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(labelStyled("Confirm Password:"), gbc);
        txtConfirmPassword = new JPasswordField(18);
        fieldStyled(txtConfirmPassword);
        gbc.gridx = 1;
        mainPanel.add(txtConfirmPassword, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(labelStyled("Name:"), gbc);
        txtName = new JTextField(18);
        fieldStyled(txtName);
        gbc.gridx = 1;
        mainPanel.add(txtName, gbc);

        // Contact
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(labelStyled("Contact:"), gbc);
        txtContact = new JTextField(18);
        fieldStyled(txtContact);
        gbc.gridx = 1;
        mainPanel.add(txtContact, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(labelStyled("Email:"), gbc);
        txtEmail = new JTextField(18);
        fieldStyled(txtEmail);
        gbc.gridx = 1;
        mainPanel.add(txtEmail, gbc);
        
        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(new Color(230, 240, 255));

        btnSignup = new JButton("Sign Up");
        btnSignup.setBackground(new Color(0, 120, 215));
        btnSignup.setForeground(Color.WHITE);
        btnSignup.setFocusPainted(false);
        btnSignup.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPanel.add(btnSignup);

        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setBackground(new Color(220, 220, 220));
        btnPanel.add(btnCancel);

        gbc.gridx = 1; gbc.gridy = 7;
        mainPanel.add(btnPanel, gbc);

        add(mainPanel);

        // Actions
        btnSignup.addActionListener(e -> signup());
        btnCancel.addActionListener(e -> dispose());
    }

    private JLabel labelStyled(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private void fieldStyled(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
    }

    private void signup() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password fields cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM admin WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists. Choose another.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO admin(username, password, name, contact, email) VALUES (?, ?, ?, ?, ?)"
            );
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, name);
            insertStmt.setString(4, contact);
            insertStmt.setString(5, email);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Admin account created successfully. You can now log in.");
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during signup: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
