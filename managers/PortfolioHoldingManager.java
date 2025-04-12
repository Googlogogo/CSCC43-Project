package managers;

import java.sql.*;
import java.util.*;

// This class manages individual portfolio holdings for a user, allowing them to add, remove, and view their stock holdings.
public class PortfolioHoldingManager {
    // Portfolio Holding Dashboard
    public void portfolioHoldingDashboard(int portfolioId) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    
                    Welcome to Portfolio Holding Management!
                    
                    1. Buy Stock and Add to Portfolio
                    2. Sell Stock and Remove from Portfolio
                    3. View All Stocks in Portfolio
                    4. View Current Value of each Stock and Total Value of Portfolio
                    5. Back to Portfolio Dashboard
                    """);
            System.out.print("Choose an option: ");

            // Prevent users from inputting non-integer values
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid option. Please try again.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    buyStockToPortfolio(portfolioId);
                    break;
                case 2:
                    sellStockFromPortfolio(portfolioId);
                    break;
                case 3:
                    viewAllStocksInPortfolio(portfolioId);
                    break;
                case 4:
                    displayTotalAndCurrentValue(portfolioId);
                    break;
                case 5:
                    return; // Go back to portfolio dashboard
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Add a stock to the user's portfolio
    public void buyStockToPortfolio(int portfolioId) {
        Scanner scanner = new Scanner(System.in);
        StockManager stockManager = new StockManager();
        // Prompt user for the stock symbol
        System.out.print("Enter the stock symbol (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();

        // Check if the stock exists in the database
        if (!stockManager.checkStockExists(symbol)) {
            System.out.println("Stock does not exist! Please add it first!");
            return;
        }

        // Prompt user for the number of shares
        System.out.print("Enter the number of shares to buy: ");
        int shares;
        // Prevent users from inputting non-integer values
        while (true) {
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number of shares.");
                scanner.nextLine(); // Clear the invalid input
                continue;
            }
            shares = scanner.nextInt();
            if (shares <= 0) {
                System.out.println("Number of shares must be greater than zero. Please try again.");
            } else {
                break; // Valid input
            }
        }

        // Check if the number of shares to buy is less than or equal to the user's cash balance
        if (!hasSufficientFunds(stockManager, portfolioId, symbol, shares)) {
            System.out.println("You do not have enough funds to buy this stock!");
            return;
        }

        // Check if the stock already exists in the user's portfolio
        if (checkStockInPortfolio(portfolioId, symbol)) {
            System.out.print("Stock already exists in your portfolio! Do you want to update the number of shares? (yes/no)");
            scanner.nextLine(); // Clear the newline character
            String response = scanner.nextLine().toLowerCase();
            if (response.equalsIgnoreCase("yes")) {
                // Update the number of shares in the user's portfolio
                String updateSql = "UPDATE PortfolioHolding SET shares = shares + ? WHERE portfolio_id = ? AND symbol = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                    updateStmt.setInt(1, shares);
                    updateStmt.setInt(2, portfolioId);
                    updateStmt.setString(3, symbol);
                    updateStmt.executeUpdate();

                    System.out.println("Stock shares updated successfully!");

                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            } else {
                System.out.println("Stock not added to portfolio.");
                return;
            }
            return;
        }

        String sql = "INSERT INTO PortfolioHolding (portfolio_id, symbol, shares) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            pstmt.setString(2, symbol);
            pstmt.setInt(3, shares);
            pstmt.executeUpdate();
            System.out.println("Stock added to portfolio successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Deduct money from the user's cash balance
        deductMoneyFromCashBalance(portfolioId, symbol);
    }

    // Deduct money from the user's cash balance after buying a stock
    public void deductMoneyFromCashBalance(int portfolioId, String symbol) {
        String sql = """
                UPDATE Portfolio
                SET cash_balance = cash_balance - (
                    SELECT shares * close
                    FROM PortfolioHolding ph
                    JOIN Stock s ON ph.symbol = s.symbol
                    JOIN StockHistory sh ON s.symbol = sh.symbol
                    WHERE ph.portfolio_id = ? AND ph.symbol = ?
                    AND sh.timestamp = (
                        SELECT MAX(timestamp)
                        FROM StockHistory
                        WHERE symbol = ph.symbol
                    )
                )
                WHERE portfolio_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            pstmt.setString(2, symbol);
            pstmt.setInt(3, portfolioId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Check if the user has sufficient funds to buy the stock
    public boolean hasSufficientFunds(StockManager stockManager, int portfolioId, String symbol, int shares) {
        String sql = "SELECT cash_balance FROM Portfolio WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double cashBalance = rs.getDouble("cash_balance");
                double stockPrice = stockManager.getStockPrice(symbol);
                double totalCost = stockPrice * shares;

                return !(totalCost > cashBalance); // Sufficient funds
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Unable to check funds
    }

    // Sell a stock from the user's portfolio
    public void sellStockFromPortfolio(int portfolioId) {
        Scanner scanner = new Scanner(System.in);
        StockManager stockManager = new StockManager();
        // Prompt user for the stock symbol to sell
        System.out.print("Enter the stock symbol (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();
        System.out.print("Enter the number of shares to sell: ");
        int shares;
        // Prevent users from inputting non-integer values
        while (true) {
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number of shares.");
                scanner.nextLine(); // Clear the invalid input
                continue;
            }
            shares = scanner.nextInt();
            if (shares <= 0) {
                System.out.println("Number of shares must be greater than zero. Please try again.");
            } else {
                break; // Valid input
            }
        }

        // Check if the number of shares to sell is less than or equal to the number of shares owned
        if (!checkSharesToSell(portfolioId, symbol, shares)) {
            System.out.println("You do not own enough shares to sell!");
            return;
        }

        // Check if the stock exists in the database
        if (!stockManager.checkStockExists(symbol)) {
            System.out.println("Stock does not exist! Please add it first.");
            return;
        }

        // Check if the stock exists in the user's portfolio
        if (!checkStockInPortfolio(portfolioId, symbol)) {
            System.out.println("Stock does not exist in your portfolio!");
            return;
        }

        // Update the number of shares in the user's portfolio
        String updateSql = "UPDATE PortfolioHolding SET shares = shares - ? WHERE portfolio_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setInt(1, shares);
            updateStmt.setInt(2, portfolioId);
            updateStmt.setString(3, symbol);
            updateStmt.executeUpdate();

            System.out.println("Stock shares updated successfully!");

            // Check if shares is 0 after selling
            String checkSql = "SELECT shares FROM PortfolioHolding WHERE portfolio_id = ? AND symbol = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, portfolioId);
                checkStmt.setString(2, symbol);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int remainingShares = rs.getInt("shares");
                    if (remainingShares == 0) {
                        // Prompt user for confirmation to remove the stock from the portfolio
                        System.out.print("You have sold all shares of " + symbol + ". Do you want to remove it from your portfolio? (y/n) ");
                        String response = scanner.nextLine();
                        if (!response.equalsIgnoreCase("y")) {
                            System.out.println("Stock not removed from portfolio.");
                            return;
                        }
                        // Remove the stock from the user's portfolio
                        String deleteSql = "DELETE FROM PortfolioHolding WHERE portfolio_id = ? AND symbol = ?";
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, portfolioId);
                            deleteStmt.setString(2, symbol);
                            deleteStmt.executeUpdate();
                            System.out.println("Stock removed from portfolio successfully!");
                        } catch (SQLException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Add sold money to the user's cash balance
        addMoneyToCashBalance(portfolioId, symbol);
        System.out.println("Money added to cash balance successfully!");
    }

    // Add money to the user's cash balance after selling a stock
    public void addMoneyToCashBalance(int portfolioId, String symbol) {
        String sql = """
                UPDATE Portfolio
                SET cash_balance = cash_balance + (
                    SELECT shares * close
                    FROM PortfolioHolding ph
                    JOIN Stock s ON ph.symbol = s.symbol
                    JOIN StockHistory sh ON s.symbol = sh.symbol
                    WHERE ph.portfolio_id = ? AND ph.symbol = ?
                    AND sh.timestamp = (
                        SELECT MAX(timestamp)
                        FROM StockHistory
                        WHERE symbol = ph.symbol
                    )
                )
                WHERE portfolio_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            pstmt.setString(2, symbol);
            pstmt.setInt(3, portfolioId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Check if the number of shares to sell is less than or equal to the number of shares owned
    public boolean checkSharesToSell(int portfolioId, String symbol, int shares) {
        String checkSql = "SELECT shares FROM PortfolioHolding WHERE portfolio_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, portfolioId);
            checkStmt.setString(2, symbol);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int ownedShares = rs.getInt("shares");
                return shares <= ownedShares; // Returns true if shares to sell are less than or equal to owned shares
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock does not exist in the user's portfolio
    }

    // View all stocks in the user's portfolio
    public void viewAllStocksInPortfolio(int portfolioId) {
        String sql = "SELECT * FROM PortfolioHolding WHERE portfolio_id = ? ORDER BY shares DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Stocks in your portfolio:");
            while (rs.next()) {
                System.out.println("Stock: " + rs.getString("symbol") + " - Shares: " + rs.getInt("shares"));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Display the current value of each stock in the user's portfolio and the total value
    public void displayTotalAndCurrentValue(int portfolioId) {
        String sql = """
            SELECT ph.symbol AS symbol, ph.shares AS shares, sh.close AS price
            FROM PortfolioHolding ph
            JOIN Stock s ON ph.symbol = s.symbol
            JOIN StockHistory sh ON s.symbol = sh.symbol
            WHERE ph.portfolio_id = ? AND sh.timestamp = (
                SELECT MAX(timestamp)
                FROM StockHistory
                WHERE symbol = ph.symbol
            )
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            double totalValue = 0;
            System.out.println("\nCurrent value of each stock in your portfolio:");
            while (rs.next()) {
                String symbol = rs.getString("symbol");
                int shares = rs.getInt("shares");
                double price = rs.getDouble("price");
                double currentValue = shares * price;
                totalValue += currentValue;
                System.out.println("Stock: " + symbol + " - Current Value: $" + currentValue + " (Shares: " + shares + ")");
            }
            System.out.println("\nTotal value of your portfolio: $" + totalValue);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Check if the stock already exists in the user's portfolio
    public boolean checkStockInPortfolio(int portfolioId, String symbol) {
        String checkSql = "SELECT * FROM PortfolioHolding WHERE portfolio_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, portfolioId);
            checkStmt.setString(2, symbol);
            ResultSet rs = checkStmt.executeQuery();

            return rs.next(); // Returns true if the stock exists in the user's portfolio

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock does not exist in the user's portfolio
    }
}
