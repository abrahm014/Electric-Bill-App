package electricbillapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBHelper {

    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/electric_billing";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // default XAMPP password

    /**
     * Initializes the database connection to test if everything is working.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database initialization failed: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns a new database connection.
     * Loads the JDBC driver and connects to the database.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found: " + e.getMessage(),
                    "Driver Error", JOptionPane.ERROR_MESSAGE);
        }

        // Return the connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
