package services;

import java.util.*;
import java.sql.*;
import java.time.LocalDate;

// This class manages stock information, allowing users to add, view, update, and remove stocks from their portfolio.
public class StockManager {
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
        System.out.print("Enter the stock's company name: ");
        while (!scanner.hasNextLine()) {
            System.out.println("Invalid input. Please enter a valid company name.");
            scanner.nextLine();
        }
        String companyName = scanner.nextLine();

        // Check if the stock already exists
        if (checkStockExists(symbol)) {
            System.out.println("Stock already exists!");
            return;
        }

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
        System.out.print("Do you also wish to add stock into stock history? (yes/no)");
        String answer = scanner.nextLine().toLowerCase();
        if (answer.equals("yes")) {
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
        String sql = "DELETE FROM Stock WHERE symbol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.executeUpdate();
            System.out.println("Stock deleted successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Insert a new stock into the database
    public void insertStockintoHistory(String symbol) {
        // Prompt user for stock details
        Scanner scanner = new Scanner(System.in);
        java.sql.Date date = java.sql.Date.valueOf(LocalDate.now());
        int open;
        System.out.print("Enter the stock's open price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the open price.");
            scanner.nextLine();
        }
        open = scanner.nextInt();
        int high;
        System.out.print("Enter the stock's high price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the high price.");
            scanner.nextLine();
        }
        high = scanner.nextInt();
        int low;
        System.out.print("Enter the stock's low price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the low price.");
            scanner.nextLine();
        }
        low = scanner.nextInt();
        int close;
        System.out.print("Enter the stock's close price: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for the close price.");
            scanner.nextLine();
        }
        close = scanner.nextInt();
        long volume;
        System.out.print("Enter the stock's volume: ");
        while (!scanner.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid long integer for the volume.");
            scanner.nextLine();
        }
        volume = scanner.nextLong();

        // Insert stock into the database
        String sql = """
                INSERT INTO StockHistory (symbol, date, open, high, low, close, volume)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (symbol, date) DO NOTHING
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.setDate(2, date);
            pstmt.setInt(3, open);
            pstmt.setInt(4, high);
            pstmt.setInt(5, low);
            pstmt.setInt(6, close);
            pstmt.setLong(7, volume);
            pstmt.executeUpdate();
            System.out.println("Stock added successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // View all stocks in the portfolio
    public void viewAllStocks() {
        String sql = "SELECT * FROM Stock";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("Stock: " + rs.getString("symbol") + " - Company: " + rs.getString("company_name"));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get stock details
    public void getStockDetails(String symbol) {
        String sql = "SELECT * FROM Stock WHERE symbol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Stock: " + rs.getString("symbol") + " - Company: " + rs.getString("company_name"));
            } else {
                System.out.println("Stock not found!");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Update stock details
    public void updateStock(String symbol, String newCompanyName) {
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
    }

    // Get stock history for a specific stock
    public void getStockHistoryBySymbol(String symbol) {
        String sql = "SELECT * FROM StockHistory WHERE symbol = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Date: " + rs.getDate("date") + " - Open: " + rs.getInt("open") +
                        " - High: " + rs.getInt("high") + " - Low: " + rs.getInt("low") +
                        " - Close: " + rs.getInt("close") + " - Volume: " + rs.getLong("volume"));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get stock history for a specific date
    public void getStockHistoryByDate(String symbol, LocalDate date) {
        String sql = "SELECT * FROM StockHistory WHERE symbol = ? AND timestamp = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Date: " + rs.getDate("date") + " - Open: " + rs.getInt("open") +
                        " - High: " + rs.getInt("high") + " - Low: " + rs.getInt("low") +
                        " - Close: " + rs.getInt("close") + " - Volume: " + rs.getLong("volume"));
            } else {
                System.out.println("No data found for the specified date!");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get stock history for a specific date range
    public void getStockHistoryByDateRange(String symbol, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM StockHistory WHERE symbol = ? AND timestamp BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Date: " + rs.getDate("date") + " - Open: " + rs.getInt("open") +
                        " - High: " + rs.getInt("high") + " - Low: " + rs.getInt("low") +
                        " - Close: " + rs.getInt("close") + " - Volume: " + rs.getLong("volume"));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
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


    // Buy stocks
    public void buyStock(int portfolioId, String symbol, int shares, double pricePerShare) {
        double totalCost = shares * pricePerShare;

        // Check if portfolio has enough cash
        String balanceCheck = "SELECT cash_balance FROM Portfolios WHERE portfolio_id = ?";
        String updateBalance = "UPDATE Portfolios SET cash_balance = cash_balance - ? WHERE portfolio_id = ?";
        String insertStock = "INSERT INTO Portfolio_Holdings (portfolio_id, symbol, shares_held) " +
                "VALUES (?, ?, ?) ON CONFLICT (portfolio_id, symbol) DO UPDATE " +
                "SET shares_held = Portfolio_Holdings.shares_held + EXCLUDED.shares_held";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement checkStmt = conn.prepareStatement(balanceCheck);
                 PreparedStatement updateStmt = conn.prepareStatement(updateBalance);
                 PreparedStatement insertStmt = conn.prepareStatement(insertStock)) {

                // Check cash balance
                checkStmt.setInt(1, portfolioId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getDouble("cash_balance") >= totalCost) {

                    // Deduct cash
                    updateStmt.setDouble(1, totalCost);
                    updateStmt.setInt(2, portfolioId);
                    updateStmt.executeUpdate();

                    // Add stock to portfolio
                    insertStmt.setInt(1, portfolioId);
                    insertStmt.setString(2, symbol);
                    insertStmt.setInt(3, shares);
                    insertStmt.executeUpdate();

                    conn.commit(); // Commit transaction
                    System.out.println("Stock purchased successfully!");

                } else {
                    System.out.println("Insufficient balance!");
                }
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction on error
                System.err.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Sell stocks
    public void sellStock(int portfolioId, String symbol, int shares) {
        String checkShares = "SELECT shares_held FROM Portfolio_Holdings WHERE portfolio_id = ? AND symbol = ?";
        String updateShares = "UPDATE Portfolio_Holdings SET shares_held = shares_held - ? WHERE portfolio_id = ? AND symbol = ?";
        String deleteStock = "DELETE FROM Portfolio_Holdings WHERE portfolio_id = ? AND symbol = ? AND shares_held = 0";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkShares);
                 PreparedStatement updateStmt = conn.prepareStatement(updateShares);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteStock)) {

                // Check shares owned
                checkStmt.setInt(1, portfolioId);
                checkStmt.setString(2, symbol);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt("shares_held") >= shares) {
                    // Deduct shares
                    updateStmt.setInt(1, shares);
                    updateStmt.setInt(2, portfolioId);
                    updateStmt.setString(3, symbol);
                    updateStmt.executeUpdate();

                    // Delete entry if shares become 0
                    deleteStmt.setInt(1, portfolioId);
                    deleteStmt.setString(2, symbol);
                    deleteStmt.executeUpdate();

                    conn.commit();
                    System.out.println("Stock sold successfully!");
                } else {
                    System.out.println("Not enough shares to sell!");
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}

