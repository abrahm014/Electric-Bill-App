package electricbillapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

  public class AdminProfilePanel extends JPanel {
    private MainFrame mainFrame;
    private int adminId;

    private JTextField txtUsername, txtName, txtContact, txtEmail;
    private JPasswordField txtPassword;
    private JButton btnEdit, btnSave, btnCancel;


    private String originalUsername, originalPassword, originalName, originalContact, originalEmail;

    public AdminProfilePanel(MainFrame mainFrame, int adminId) {
        this.mainFrame = mainFrame;
        this.adminId = adminId;

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Profile picture panel
        JPanel picPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        picPanel.setBackground(Color.WHITE);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        int row = 0;
         
        //Username
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        txtUsername.setEditable(false);
        txtUsername.setFont(fieldFont);
        formPanel.add(txtUsername, gbc);

        // Password
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(fieldFont);
        txtPassword.setEditable(false);
        formPanel.add(txtPassword, gbc);

        // Name
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(20);
        txtName.setFont(fieldFont);
        txtName.setEditable(false);
        formPanel.add(txtName, gbc);

        // Contact
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1;
        txtContact = new JTextField(20);
        txtContact.setFont(fieldFont);
        txtContact.setEditable(false);
        formPanel.add(txtContact, gbc);

        // Email
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setFont(fieldFont);
        txtEmail.setEditable(false);
        formPanel.add(txtEmail, gbc);

        // Button panel (Edit, Save, Cancel)
        row++;
        gbc.gridx = 1; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnEdit = new JButton("Edit");
        btnEdit.setBackground(new Color(0, 123, 255));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFocusPainted(false);

        btnSave = new JButton("Save");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setVisible(false);

        btnCancel = new JButton("Cancel");
        btnCancel.setBackground(new Color(220, 53, 69));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setVisible(false);

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Action listeners
        btnEdit.addActionListener(e -> enableEditing(true));
        btnCancel.addActionListener(e -> cancelEditing());
        btnSave.addActionListener(e -> saveProfile());

        loadAdminDetails();
    }

    private void enableEditing(boolean editing) {
        txtUsername.setEditable(editing);
        txtPassword.setEditable(editing);
        txtName.setEditable(editing);
        txtContact.setEditable(editing);
        txtEmail.setEditable(editing);

        btnEdit.setVisible(!editing);
        btnSave.setVisible(editing);
        btnCancel.setVisible(editing);
    }

    private void cancelEditing() {
        txtUsername.setText(originalUsername);
        txtPassword.setText(originalPassword);
        txtName.setText(originalName);
        txtContact.setText(originalContact);
        txtEmail.setText(originalEmail);
        enableEditing(false);
    }

    public void loadAdminDetails() {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM admin WHERE id = ?")) {
            ps.setInt(1, adminId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtUsername.setText(rs.getString("username"));
                txtPassword.setText(rs.getString("password"));
                txtName.setText(rs.getString("name"));
                txtContact.setText(rs.getString("contact"));
                txtEmail.setText(rs.getString("email"));

                // Backup original values for Cancel
                originalUsername = txtUsername.getText();
                originalPassword = txtPassword.getText();
                originalName = txtName.getText();
                originalContact = txtContact.getText();
                originalEmail = txtEmail.getText();

            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading admin profile: " + e.getMessage());
        }
    }

   private void saveProfile() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword()).trim();
    String name = txtName.getText().trim();
    String contact = txtContact.getText().trim();
    String email = txtEmail.getText().trim();

    if (password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Password cannot be empty.");
        return;
    }

    try (Connection conn = DBHelper.getConnection();
         PreparedStatement ps = conn.prepareStatement("UPDATE admin SET username=?, password=?, name=?, contact=?, email=? WHERE id=?")) {
        ps.setString(1, username);
        ps.setString(2, password);
        ps.setString(3, name);
        ps.setString(4, contact);
        ps.setString(5, email);
        ps.setInt(6, adminId);
        ps.executeUpdate();

        // Update backup values
        originalUsername = username;
        originalPassword = password;
        originalName = name;
        originalContact = contact;
        originalEmail = email;

        enableEditing(false);
        JOptionPane.showMessageDialog(this, "Profile updated successfully.");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error saving profile: " + e.getMessage());
    }
}
  }