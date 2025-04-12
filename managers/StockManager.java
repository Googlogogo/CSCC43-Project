package managers;

import java.util.*;
import java.sql.*;

// This class manages stock information, allowing users to add, view, update, and remove stocks from their portfolio.
public class StockManager {
    // Stock Dashboard
    public void stockDashboard() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("""
                   
                    Welcome to the Stock Management Dashboard!

                    1. Add Stock / Insert New Daily Stock Information
                    2. Delete Stock / Remove Daily Stock Information
                    3. View Specific Stock Information
                    4. Update Stock Information
                    5. View All Stocks
                    6. Back to Main Menu
                    """);
            System.out.print("Choose an option: ");
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid option. Please try again.");
                scanner.nextLine();
                continue;
            }
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addStock();
                    break;
                case 2:
                    deleteStock();
                    break;
                case 3:
                    System.out.print("Enter the stock's symbol: ");
                    String symbol = scanner.nextLine().toUpperCase();
                    // Prevent users from inputting non-alphabetic characters
                    if (!symbol.matches("[A-Z]+")) {
                        System.out.println("Invalid stock symbol. Please enter a valid symbol.");
                        break;
                    }
                    // Check if the stock exists
                    if (!checkStockExists(symbol)) {
                        System.out.println("Stock does not exist!");
                        break;
                    }

                    System.out.print("Enter the date (YYYY-MM-DD) to view stock information: ");
                    String date = scanner.nextLine();
                    // Prevent users from inputting invalid date format
                    if (!StatisticsManager.isValidDate(date)) {
                        System.out.println("Invalid date format. Please enter a valid date.");
                        break;
                    }
                    // Check if the stock exists
                    if (!checkStockExists(symbol)) {
                        System.out.println("Stock does not exist!");
                        break;
                    }
                    // Get stock history details
                    getStockHistory(symbol, date);
                    break;
                case 4:
                    updateStock();
                    break;
                case 5:
                    System.out.println("List of All stocks:");
                    String sql = "SELECT * FROM Stock";
                    try (Connection conn = DatabaseConnection.getConnection();
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {

                        while (rs.next()) {
                            System.out.println("Stock: " + rs.getString("symbol") +
                                    " - Company: " + rs.getString("company_name"));
                        }

                    } catch (SQLException e) {
                        System.err.println(e.getMessage());
                    }
                    break;
                case 6:
                    System.out.println("Exiting the stock management dashboard.");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

    }

    // Add stock to Stock table
    public void addStock() {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for stock details
        System.out.print("Enter the stock's symbol: ");
        while (!scanner.hasNextLine()) {
            System.out.println("Invalid input. Please enter a valid stock symbol.");
            scanner.nextLine();
        }
        String symbol = scanner.nextLine().toUpperCase();
        // Check if the stock already exists
        if (checkStockExists(symbol)) {
            System.out.print("Stock already exists! Do you want to add new stock history information? (y/n) ");
            String answer = scanner.nextLine();
            if (answer.equalsIgnoreCase("y")) {
                insertStockintoHistory(symbol);
            } else {
                System.out.println("Stock history not updated.");
            }
            return;
        }

        System.out.print("Enter the stock's company name: ");
        while (!scanner.hasNextLine()) {
            System.out.println("Invalid input. Please enter a valid company name.");
            scanner.nextLine();
        }
        String companyName = scanner.nextLine();

        // Insert stock into the database
        String sql = "INSERT INTO Stock (symbol, company_name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.setString(2, companyName);
            pstmt.executeUpdate();
            System.out.println("Stock added successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Insert stock into history
        System.out.print("Do you also wish to add stock into stock history? (y/n) ");
        String answer = scanner.nextLine();
        if (answer.equalsIgnoreCase("y")) {
            insertStockintoHistory(symbol);
        } else {
            System.out.println("Stock history not added.");
        }
    }

    // Delete stock from Stock table
    public void deleteStock() {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for stock symbol
        System.out.print("Enter the stock's symbol to delete: ");
        while (!scanner.hasNextLine()) {
            System.out.println("Invalid input. Please enter a valid stock symbol.");
            scanner.nextLine();
        }
        String symbol = scanner.nextLine().toUpperCase();

        // Check if the stock exists
        if (!checkStockExists(symbol)) {
            System.out.println("Stock does not exist!");
            return;
        }

        // Delete stock from the database
        removeStock(symbol);
    }

    // Remove a stock from the portfolio
    public void removeStock(String symbol) {
        String sql = "DELETE FROM Stock WHERE symbol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.executeUpdate();
            System.out.println("Stock removed successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Insert a new stock into the database
    public void insertStockintoHistory(String symbol) {
        // Prompt user for stock details
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine();
        while (true) {
            if (!StatisticsManager.isValidDate(dateInput)) {
                System.out.println("Invalid date format. Please enter a valid date.");
                System.out.print("Enter date (YYYY-MM-DD): ");
                dateInput = scanner.nextLine();
            } else {
                break;
            }
        }

        double open;
        System.out.print("Enter the stock's open price: ");
        while (true) {
            if (scanner.hasNextDouble()) {
                open = scanner.nextDouble();
                break;
            } else {
                System.out.println("Invalid input. Please enter a valid decimal for the open price.");
                System.out.print("Enter the stock's open price: ");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        double high;
        System.out.print("Enter the stock's high price: ");
        while (true) {
            if (scanner.hasNextDouble()) {
                high = scanner.nextDouble();
                break;
            } else {
                System.out.println("Invalid input. Please enter a valid decimal for the high price.");
                System.out.print("Enter the stock's high price: ");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        double low;
        System.out.print("Enter the stock's low price: ");
        while (true) {
            if (scanner.hasNextDouble()) {
                low = scanner.nextDouble();
                break;
            } else {
                System.out.println("Invalid input. Please enter a valid decimal for the low price.");
                System.out.print("Enter the stock's low price: ");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        double close;
        System.out.print("Enter the stock's close price: ");
        while (true) {
            if (scanner.hasNextDouble()) {
                close = scanner.nextDouble();
                break;
            } else {
                System.out.println("Invalid input. Please enter a valid decimal for the close price.");
                System.out.print("Enter the stock's close price: ");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        long volume;
        System.out.print("Enter the stock's volume: ");
        while (!scanner.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid long integer for the volume.");
            scanner.nextLine();
        }
        volume = scanner.nextLong();

        // Insert stock into the database
        String sql = """
                INSERT INTO StockHistory (symbol, timestamp, open, high, low, close, volume)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (symbol, timestamp) DO NOTHING
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(dateInput));
            pstmt.setDouble(3, open);
            pstmt.setDouble(4, high);
            pstmt.setDouble(5, low);
            pstmt.setDouble(6, close);
            pstmt.setLong(7, volume);
            pstmt.executeUpdate();
            System.out.println("Stock added successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get stock details
    public static void getStockDetails(String symbol) {
        String sql = "SELECT * FROM Stock WHERE symbol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Stock: " + rs.getString("symbol") +
                        " - Company: " + rs.getString("company_name"));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Update stock details
    public void updateStock() {
        // Prompt user for stock information
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the stock symbol: ");
        while (!scanner.hasNextLine()) {
            System.out.println("Invalid input. Please enter a valid stock symbol.");
            scanner.nextLine();
        }
        String symbol = scanner.nextLine().toUpperCase();
        // Check if the stock exists
        if (!checkStockExists(symbol)) {
            System.out.println("Stock does not exist!");
            return;
        }

        System.out.print("Enter the new company name: ");
        while (!scanner.hasNextLine()) {
            System.out.println("Invalid input. Please enter a valid company name.");
            scanner.nextLine();
        }
        String newCompanyName = scanner.nextLine();
        // Update stock in the database
        String sql = "UPDATE Stock SET company_name = ? WHERE symbol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newCompanyName);
            pstmt.setString(2, symbol);
            pstmt.executeUpdate();
            System.out.println("Stock updated successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Prompt user for new stock history
        System.out.print("Do you also wish to update stock history? (y/n) ");
        String answer = scanner.nextLine();
        if (answer.equalsIgnoreCase("y")) {
            System.out.print("Enter the date (YYYY-MM-DD) to update: ");
            String date = scanner.nextLine();
            if (!StatisticsManager.isValidDate(date)) {
                System.out.println("Invalid date format. Please enter a valid date.");
                return;
            }
            updateStockHistory(symbol, date);
        } else {
            System.out.println("Stock history not updated.");
        }
    }

    // Update StockHistory
    public void updateStockHistory(String symbol, String date) {
        // Prompt user for new stock history
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the new open price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the open price.");
            scanner.nextLine();
        }
        int open = scanner.nextInt();
        System.out.print("Enter the new high price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the high price.");
            scanner.nextLine();
        }
        int high = scanner.nextInt();
        System.out.print("Enter the new low price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the low price.");
            scanner.nextLine();
        }
        int low = scanner.nextInt();
        System.out.print("Enter the new close price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the close price.");
            scanner.nextLine();
        }
        int close = scanner.nextInt();
        System.out.print("Enter the new volume: ");
        while (!scanner.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid long integer for the volume.");
            scanner.nextLine();
        }
        long volume = scanner.nextLong();

        // Update stock in the database
        String sql = """
                UPDATE StockHistory
                SET open = ?, high = ?, low = ?, close = ?, volume = ?
                WHERE symbol = ? AND date = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, open);
            pstmt.setInt(2, high);
            pstmt.setInt(3, low);
            pstmt.setInt(4, close);
            pstmt.setLong(5, volume);
            pstmt.setString(6, symbol);
            pstmt.setDate(7, java.sql.Date.valueOf(date));
            pstmt.executeUpdate();
            System.out.println("Stock history updated successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Check if the stock table exists
    public boolean checkStockExists(String symbol) {
        String checkSql = """
                SELECT *
                FROM Stock s
                JOIN StockHistory sh ON s.symbol = sh.symbol
                WHERE s.symbol = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, symbol);
            ResultSet rs = checkStmt.executeQuery();

            // Check if the stock exists
            return rs.next();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock does not exist
    }

    // Get stock price for a specific symbol
    public double getStockPrice(String symbol) {
        String sql = "SELECT close FROM StockHistory WHERE symbol = ? ORDER BY timestamp DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("close");
            } else {
                System.out.println("Stock not found!");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return 0.0; // Default value if stock not found
    }

    // Get stock history for a specific symbol for a specific date
    public void getStockHistory(String symbol, String date) {
        List<Double> stockHistory = new ArrayList<>();
        String sql = "SELECT * FROM StockHistory WHERE symbol = ? AND timestamp = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                stockHistory.add(rs.getDouble("open"));
                stockHistory.add(rs.getDouble("high"));
                stockHistory.add(rs.getDouble("low"));
                stockHistory.add(rs.getDouble("close"));
                stockHistory.add((double) rs.getLong("volume"));
            }

            if (stockHistory.isEmpty()) {
                System.out.println("No stock history found for the given date.");
            } else {
                System.out.println("\nStock history for " + symbol + " on " + date + ":");
                System.out.println("Open: " + stockHistory.get(0));
                System.out.println("High: " + stockHistory.get(1));
                System.out.println("Low: " + stockHistory.get(2));
                System.out.println("Close: " + stockHistory.get(3));
                System.out.println("Volume: " + stockHistory.get(4));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}

