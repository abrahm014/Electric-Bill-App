package electricbillapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;

public class ForgotPasswordDialog extends JDialog {
    private JTextField txtUsername;
    private JTextField txtOtp;
    private JPasswordField txtNewPassword;
    private JButton btnSendOtp, btnVerify;
    private String generatedOtp = "";

    public ForgotPasswordDialog(JFrame parent) {
        super(parent, "Forgot Password", true);
        setSize(460, 340);  // slightly larger for comfort
        setLocationRelativeTo(parent);
        setLayout(new CardLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(245, 245, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

        JLabel lblTitle = new JLabel("Forgot Password");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Username Field
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(labelFont);
        panel.add(lblUser, gbc);
        txtUsername = new JTextField(20);
        txtUsername.setFont(fieldFont);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        btnSendOtp = new JButton("Send OTP");
        btnSendOtp.setFont(buttonFont);
        btnSendOtp.setBackground(new Color(0, 120, 215));
        btnSendOtp.setForeground(Color.WHITE);
        btnSendOtp.setFocusPainted(false);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(btnSendOtp, gbc);

        // OTP Field
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblOtp = new JLabel("Enter OTP:");
        lblOtp.setFont(labelFont);
        panel.add(lblOtp, gbc);
        txtOtp = new JTextField(10);
        txtOtp.setFont(fieldFont);
        gbc.gridx = 1;
        panel.add(txtOtp, gbc);

        // New Password Field
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblNewPass = new JLabel("New Password:");
        lblNewPass.setFont(labelFont);
        panel.add(lblNewPass, gbc);
        txtNewPassword = new JPasswordField(20);
        txtNewPassword.setFont(fieldFont);
        gbc.gridx = 1;
        panel.add(txtNewPassword, gbc);

        // Verify Button
        btnVerify = new JButton("Reset Your Password");
        btnVerify.setFont(buttonFont);
        btnVerify.setBackground(new Color(46, 204, 113));
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setFocusPainted(false);
        gbc.gridx = 1; gbc.gridy = 5;
        panel.add(btnVerify, gbc);

        add(panel);

        btnSendOtp.addActionListener(e -> sendOtp());
        btnVerify.addActionListener(e -> verifyAndReset());
    }

    private void sendOtp() {
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter your username.");
            return;
        }

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT email FROM admin WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");
                generatedOtp = generateOtp();
                EmailUtil.sendEmail(email, "Password Reset OTP", "Your OTP is: " + generatedOtp);
                JOptionPane.showMessageDialog(this, "OTP sent to your email.");
            } else {
                JOptionPane.showMessageDialog(this, "Username not found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void verifyAndReset() {
        String username = txtUsername.getText().trim();
        String userOtp = txtOtp.getText().trim();
        String newPassword = new String(txtNewPassword.getPassword());

        if (!userOtp.equals(generatedOtp)) {
            JOptionPane.showMessageDialog(this, "Invalid OTP.");
            return;
        }

        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a new password.");
            return;
        }

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE admin SET password = ? WHERE username = ?")) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Password reset successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private String generateOtp() {
        Random rand = new Random();
        return String.valueOf(100000 + rand.nextInt(900000));
    }
}
