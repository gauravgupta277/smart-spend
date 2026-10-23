// Persistence Layer: MySQL Database Manager
// Safely connects to MySQL without breaking DSA logic.
// If MySQL is online, it persists data across app restarts.
// If MySQL password is wrong or offline, it gracefully falls back to in-memory mode.

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {
    private static Connection conn = null;
    private static boolean connected = false;

    static {
        initDatabase();
    }

    public static void initDatabase() {
        Properties props = new Properties();
        String url = "jdbc:mysql://localhost:3306/smartspend?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "root";

        // Read db.properties if available
        String[] possiblePaths = {"db.properties", "backend/db.properties", "../db.properties"};
        for (String p : possiblePaths) {
            File pf = new File(p);
            if (pf.exists()) {
                try (InputStream input = new FileInputStream(pf)) {
                    props.load(input);
                    url = props.getProperty("db.url", url);
                    user = props.getProperty("db.user", user);
                    pass = props.getProperty("db.password", pass);
                    break;
                } catch (Exception ignored) {}
            }
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            connected = true;
            createTables();
            System.out.println("[MySQL] Connected successfully to database: smartspend");
        } catch (Exception e) {
            connected = false;
            System.out.println("[MySQL] Notice: " + e.getMessage());
            System.out.println("[MySQL] Running in high-speed In-Memory DSA Mode. (Configure db.properties to connect)");
        }
    }

    private static void createTables() {
        if (!connected) return;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "  id INT PRIMARY KEY," +
                "  title VARCHAR(255) NOT NULL," +
                "  category VARCHAR(100) NOT NULL," +
                "  amount DOUBLE NOT NULL" +
                ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS bills (" +
                "  id INT PRIMARY KEY," +
                "  name VARCHAR(255) NOT NULL," +
                "  amount DOUBLE NOT NULL," +
                "  due_date VARCHAR(50) NOT NULL," +
                "  urgency INT NOT NULL" +
                ")"
            );
        } catch (SQLException e) {
            System.err.println("[MySQL] Error creating tables: " + e.getMessage());
        }
    }

    public static boolean isConnected() {
        return connected;
    }

    // --- TRANSACTIONS PERSISTENCE ---
    public static List<Transaction> fetchAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        if (!connected) return list;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, category, amount FROM transactions ORDER BY id DESC")) {
            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getDouble("amount")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[MySQL] Fetch error: " + e.getMessage());
        }
        return list;
    }

    public static void insertTransaction(Transaction t) {
        if (!connected) return;
        String sql = "INSERT INTO transactions (id, title, category, amount) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE title=VALUES(title), category=VALUES(category), amount=VALUES(amount)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.id);
            pstmt.setString(2, t.title);
            pstmt.setString(3, t.category);
            pstmt.setDouble(4, t.amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MySQL] Insert transaction error: " + e.getMessage());
        }
    }

    public static void deleteTransaction(int id) {
        if (!connected) return;
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MySQL] Delete transaction error: " + e.getMessage());
        }
    }

    // --- BILLS PERSISTENCE ---
    public static List<Bill> fetchAllBills() {
        List<Bill> list = new ArrayList<>();
        if (!connected) return list;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, amount, due_date, urgency FROM bills ORDER BY id ASC")) {
            while (rs.next()) {
                list.add(new Bill(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("amount"),
                    rs.getString("due_date"),
                    rs.getInt("urgency")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[MySQL] Fetch bills error: " + e.getMessage());
        }
        return list;
    }

    public static void insertBill(Bill b) {
        if (!connected) return;
        String sql = "INSERT INTO bills (id, name, amount, due_date, urgency) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), amount=VALUES(amount), due_date=VALUES(due_date), urgency=VALUES(urgency)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, b.id);
            pstmt.setString(2, b.name);
            pstmt.setDouble(3, b.amount);
            pstmt.setString(4, b.dueDate);
            pstmt.setInt(5, b.urgency);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MySQL] Insert bill error: " + e.getMessage());
        }
    }

    public static void deleteBill(int id) {
        if (!connected) return;
        String sql = "DELETE FROM bills WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MySQL] Delete bill error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Is connected to MySQL? " + isConnected());
    }
}
