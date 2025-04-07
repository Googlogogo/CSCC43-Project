package services;

import java.util.*;
import java.sql.*;

// This class manages the stock list holdings for a user, allowing them to add, remove, and view their stock list holdings.
public class StockListHoldingManager {
    // Stock List Holding Dashboard
    public void stockListHoldingDashboard(int listId) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    
                    Welcome to Stock List Holding Management!
                    
                    1. Add Stock to List
                    2. Remove Stock from List
                    3. Update Stock in List
                    4. View All Stocks in List
                    5. Back to Stock List Dashboard
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
                    addStockToList(listId);
                    break;
                case 2:
                    removeStockFromList(listId);
                    break;
                case 3:
                    updateStockInList(listId);
                    break;
                case 4:
                    viewAllStocksInList(listId);
                    break;
                case 5:
                    return; // Go back to stock list dashboard
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Add a stock to the user's stock list
    public void addStockToList(int listId) {
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

        System.out.print("Enter the number of shares: ");
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
                System.out.println("Number of shares must be greater than zero.");
                continue;
            }
            break; // Valid input, exit the loop
        }

        // Check if the stock already exists in the user's stock list
        if (checkStockInList(listId, symbol)) {
            System.out.println("Stock already exists in the list! Do you want to update the shares? (yes/no)");
            String response = scanner.nextLine().toLowerCase();
            if (response.equalsIgnoreCase("yes")) {
                // Update the shares for the existing stock
                String sql = "UPDATE StockListHolding SET shares = shares + ? WHERE list_id = ? AND symbol = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, shares);
                    pstmt.setInt(2, listId);
                    pstmt.setString(3, symbol);
                    pstmt.executeUpdate();
                    System.out.println("Shares updated successfully!");
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }
            return;
        }

        // Add the stock to the user's stock list
        String sql = "INSERT INTO StockListHolding (list_id, symbol, shares) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setString(2, symbol);
            pstmt.setInt(3, shares);
            pstmt.executeUpdate();
            System.out.println("Stock added to list successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Remove a stock from the user's stock list
    public void removeStockFromList(int listId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for the stock symbol to remove
        System.out.print("Enter the stock symbol to remove (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();

        // Check if the stock exists in the user's stock list
        if (!checkStockInList(listId, symbol)) {
            System.out.println("Stock does not exist in the list!");
            return;
        }

        // Remove the stock from the user's stock list
        String sql = "DELETE FROM StockListHolding WHERE list_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setString(2, symbol);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Stock removed from list successfully!");
            } else {
                System.out.println("Stock not found in the list.");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Update the number of shares for a stock in the user's stock list
    public void updateStockInList(int listId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for the stock symbol to update
        System.out.print("Enter the stock symbol to update (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();

        // Check if the stock exists in the user's stock list
        if (!checkStockInList(listId, symbol)) {
            System.out.println("Stock does not exist in the list!");
            return;
        }

        System.out.print("Enter the new number of shares: ");
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
                System.out.println("Number of shares must be greater than zero.");
                continue;
            }
            break; // Valid input, exit the loop
        }

        // Update the number of shares for the stock in the user's stock list
        String sql = "UPDATE StockListHolding SET shares = ? WHERE list_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, shares);
            pstmt.setInt(2, listId);
            pstmt.setString(3, symbol);
            pstmt.executeUpdate();
            System.out.println("Shares updated successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // View all stocks in the user's stock list
    public void viewAllStocksInList(int listId) {
        String sql = "SELECT * FROM StockListHolding WHERE list_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Stocks in the list:");
            while (rs.next()) {
                System.out.println("Stock: " + rs.getString("symbol") + " - Shares: " + rs.getInt("shares"));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Check if the stock exists in the user's stock list
    public boolean checkStockInList(int listId, String symbol) {
        String sql = "SELECT * FROM StockListHolding WHERE list_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setString(2, symbol);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Returns true if the stock exists in the list

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock does not exist in the list
    }
}
