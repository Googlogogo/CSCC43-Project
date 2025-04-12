package managers;

import java.sql.*;
import java.util.*;

// This class manages all portfolios of a user, allowing them to create, view, delete portfolios, and manage cash balances.
public class PortfolioManager {
    // Portfolio dashboard
    public void portfolioDashboard(int userId) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    
                    Welcome to the Portfolio Dashboard!
                    
                    1. Create a New Portfolio
                    2. Get Portfolios Overview
                    3. Rename Portfolio
                    4. Delete Portfolio
                    5. Deposit Cash
                    6. View Cash Balance
                    7. Withdraw Cash
                    8. Transfer Cash between Portfolios
                    9. Manage Portfolio Holdings
                    10. Display Portfolio Statistics
                    """);
            System.out.println("Type 'exit' to exit the dashboard.");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();
            if (option.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the portfolio dashboard...");
                return;
            }

            switch (option) {
                case "1":
                    // Create portfolio
                    System.out.println("\nCreating portfolio...");
                    addPortfolio(userId);
                    break;
                case "2":
                    // View portfolios
                    System.out.println("\nFetching portfolios...");
                    getPortfolios(userId);
                    break;
                case "3":
                    // Rename portfolio
                    System.out.println("\nRenaming portfolio...");
                    renamePortfolio(userId);
                    break;
                case "4":
                    // Delete portfolio
                    System.out.println("\nDeleting portfolio...");
                    deletePortfolio(userId);
                    break;
                case "5":
                    // Deposit cash
                    System.out.println("\nDepositing cash to portfolio...");
                    depositCash(userId);
                    break;
                case "6":
                    // View cash balance
                    System.out.println("\nViewing cash balance...");
                    viewCashBalance(userId);
                    break;
                case "7":
                    // Withdraw cash
                    System.out.println("\nWithdrawing cash from portfolio...");
                    withdrawCash(userId);
                    break;
                case "8":
                    // Transfer cash between portfolios
                    System.out.println("\nTransferring cash between portfolios...");
                    transferCash(userId);
                    break;
                case "9":
                    // Manage portfolio holdings
                    System.out.println("\nManaging portfolio holdings...");
                    managePortfolioHoldings(userId);
                    break;
                case "10":
                    System.out.println("\nDisplaying portfolio statistics...");
                    System.out.print("Enter portfolio name: ");
                    String name = scanner.nextLine();
                    int portfolioId = getPortfolioIDbyName(userId, name);
                    // Check if portfolio exists
                    if (!portfolioExists(portfolioId)) {
                        System.out.println("Portfolio does not exist!");
                        break;
                    }
                    // Check if portfolio belongs to the user
                    if (!isPortfolioOwnedByUser(userId, portfolioId)) {
                        System.out.println("You do not own this portfolio!");
                        break;
                    }
                    StatisticsManager.handlePortfolioStatistics(userId, name);
                    break;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    // Add a new portfolio
    public void addPortfolio(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for portfolio name and cash balance
        System.out.print("Enter portfolio name: ");
        String name = scanner.nextLine();
        System.out.print("Enter initial cash balance: ");
        double cashBalance = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        // Validate portfolio name
        if (isValidPortfolioName(name)) {
            System.out.println("Invalid portfolio name!");
            return;
        }
        // Check if portfolio name already exists
        if (portfolioExists(getPortfolioIDbyName(userId, name))) {
            System.out.println("Portfolio name already exists! Please choose a different name.");
            return;
        }

        // Insert portfolio into database
        String sql = "INSERT INTO Portfolio (user_id, name, cash_balance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            pstmt.setDouble(3, cashBalance);
            pstmt.executeUpdate();
            System.out.println("Portfolio created successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // View all portfolios of a user
    public void getPortfolios(int userId) {
        String sql = "SELECT * FROM Portfolio WHERE user_id = ? ORDER BY portfolio_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            List<Integer> portfolioIds = new ArrayList<>();
            while (rs.next()) {
                int portfolioId = rs.getInt("portfolio_id");
                String name = rs.getString("name");
                double cashBalance = rs.getDouble("cash_balance");
                portfolioIds.add(portfolioId);
                System.out.println("Portfolio ID: " + portfolioId + " - Name: " + name +
                        " - Cash Balance: $" + cashBalance);
            }

            if (portfolioIds.isEmpty()) {
                System.out.println("No portfolios found.");
            } else {
                System.out.println("Total portfolios: " + portfolioIds.size());
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Rename a portfolio
    public void renamePortfolio(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for portfolio name
        System.out.print("Enter current portfolio name: ");
        String currentName = scanner.nextLine();
        int portfolioId = getPortfolioIDbyName(userId, currentName);

        // Check if portfolio exists
        if (!portfolioExists(portfolioId)) {
            System.out.println("Portfolio does not exist!");
            return;
        }
        // Check if portfolio belongs to the user
        if (!isPortfolioOwnedByUser(userId, portfolioId)) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        // Prompt user for new portfolio name
        System.out.print("Enter new portfolio name: ");
        String newName = scanner.nextLine();

        // Validate new portfolio name
        if (isValidPortfolioName(newName)) {
            System.out.println("Invalid portfolio name!");
            return;
        }
        // Check if new portfolio name already exists
        if (portfolioExists(getPortfolioIDbyName(userId, newName))) {
            System.out.println("Portfolio name already exists! Please choose a different name.");
            return;
        }

        // Update portfolio name in database
        String sql = "UPDATE Portfolio SET name = ? WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setInt(2, getPortfolioIDbyName(userId, currentName));
            pstmt.executeUpdate();
            System.out.println("Portfolio renamed successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Delete a portfolio
    public void deletePortfolio(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for portfolio name
        System.out.print("Enter portfolio name to delete: ");
        String name = scanner.nextLine();
        int portfolioId = getPortfolioIDbyName(userId, name);

        // Check if portfolio exists
        if (!portfolioExists(portfolioId)) {
            System.out.println("Portfolio does not exist!");
            return;
        }
        // Check if portfolio belongs to the user
        if (!isPortfolioOwnedByUser(userId, portfolioId)) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        // Prompt user for confirmation
        System.out.print("Are you sure you want to delete this portfolio? (y/n): ");
        String confirmation = scanner.nextLine();
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Portfolio deletion cancelled.");
            return;
        }

        // SQL queries to delete the associated entries
        String deletePortfolioHoldingSql = "DELETE FROM PortfolioHolding WHERE portfolio_id = ?";
        String deletePortfolioSql = "DELETE FROM Portfolio WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deletePortfolioHoldingStmt = conn.prepareStatement(deletePortfolioHoldingSql);
             PreparedStatement deletePortfolioStmt = conn.prepareStatement(deletePortfolioSql)) {

            // Start transaction
            conn.setAutoCommit(false);

            // Delete portfolio holdings
            deletePortfolioHoldingStmt.setInt(1, portfolioId);
            deletePortfolioHoldingStmt.executeUpdate();

            // Delete portfolio
            deletePortfolioStmt.setInt(1, portfolioId);
            deletePortfolioStmt.executeUpdate();

            // Commit transaction
            conn.commit();
            System.out.println("Portfolio and associated holdings deleted successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Deposit cash to a portfolio
    public void depositCash(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for portfolio name and cash amount
        System.out.print("Enter portfolio name: ");
        String portfolioName = scanner.nextLine();
        int portfolioId = getPortfolioIDbyName(userId, portfolioName);

        // Check if portfolio exists
        if (!portfolioExists(portfolioId)) {
            System.out.println("Portfolio does not exist!");
            return;
        }
        // Check if portfolio belongs to the user
        if (!isPortfolioOwnedByUser(userId, portfolioId)) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        System.out.print("Enter deposit amount: ");
        if (!scanner.hasNextDouble()) {
            System.out.println("Invalid amount! Please enter a positive value.");
            scanner.nextLine(); // Consume invalid input
            return;
        }
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        // Validate amount
        if (amount <= 0) {
            System.out.println("Invalid amount! Please enter a positive value.");
            scanner.nextLine(); // Consume invalid input
            return;
        }

        // Update cash balance in database
        String sql = "UPDATE Portfolio SET cash_balance = cash_balance + ? WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, amount);
            pstmt.setInt(2, getPortfolioIDbyName(userId, portfolioName));
            pstmt.executeUpdate();
            System.out.println("Cash added successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // View cash balance of a portfolio
    public void viewCashBalance(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for portfolio name
        System.out.print("Enter portfolio name: ");
        String portfolioName = scanner.nextLine();
        int portfolioId = getPortfolioIDbyName(userId, portfolioName);

        // Check if portfolio exists
        if (!portfolioExists(portfolioId)) {
            System.out.println("Portfolio does not exist!");
            return;
        }
        // Check if portfolio belongs to the user
        if (!isPortfolioOwnedByUser(userId, portfolioId)) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        double cashBalance = getCashBalance(userId, portfolioName);
        if (cashBalance == -1) {
            System.out.println("Unable to retrieve cash balance!");
            return;
        }
        System.out.println("Cash balance of portfolio " + portfolioName + ": $" + cashBalance);
    }

    // Remove cash from a portfolio
    public void withdrawCash(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for portfolio name
        System.out.print("Enter portfolio name: ");
        String portfolioName = scanner.nextLine();
        int portfolioId = getPortfolioIDbyName(userId, portfolioName);

        // Check if portfolio exists
        if (!portfolioExists(portfolioId)) {
            System.out.println("Portfolio does not exist!");
            return;
        }
        // Check if portfolio belongs to the user
        if (!isPortfolioOwnedByUser(userId, portfolioId)) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        System.out.print("Enter withdrawal amount: ");
        if (!scanner.hasNextDouble()) {
            System.out.println("Invalid amount! Please enter a positive value.");
            scanner.nextLine(); // Consume invalid input
            return;
        }
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        // Validate amount
        if (amount <= 0) {
            System.out.println("Invalid amount! Please enter a positive value.");
            scanner.nextLine(); // Consume invalid input
            return;
        }

        // Check if cash balance is sufficient
        double currentBalance = getCashBalance(userId, portfolioName);
        if (currentBalance == -1) {
            System.out.println("Unable to retrieve cash balance!");
            return;
        } else if (currentBalance < amount) {
            System.out.println("Insufficient cash balance! Transaction failed.");
            return;
        }

        // Update cash balance in database
        String sql = "UPDATE Portfolio SET cash_balance = cash_balance - ? WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, amount);
            pstmt.setInt(2, getPortfolioIDbyName(userId, portfolioName));
            pstmt.executeUpdate();
            System.out.println("Cash removed successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Transfer cash between portfolios
    public void transferCash(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for portfolio names and cash amount
        System.out.print("Enter source portfolio name: ");
        String sourcePortfolioName = scanner.nextLine();
        int sourcePortfolioId = getPortfolioIDbyName(userId, sourcePortfolioName);

        // Check if source portfolio exists
        if (!portfolioExists(sourcePortfolioId)) {
            System.out.println("Source portfolio does not exist!");
            return;
        }
        // Check if source portfolio belongs to the user
        if (!isPortfolioOwnedByUser(userId, sourcePortfolioId)) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        System.out.print("Enter destination portfolio name: ");
        String destPortfolioName = scanner.nextLine();
        int destPortfolioId = getPortfolioIDbyName(userId, destPortfolioName);
        // Check if destination portfolio exists
        if (!portfolioExists(destPortfolioId)) {
            System.out.println("Destination portfolio does not exist!");
            return;
        }
        // Check if destination portfolio belongs to the user
        if (!isPortfolioOwnedByUser(userId, destPortfolioId)) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        // Check if source and destination portfolios are the same
        if (sourcePortfolioName.equals(destPortfolioName)) {
            System.out.println("Source and destination portfolios cannot be the same!");
            return;
        }

        System.out.print("Enter transfer amount: ");
        if (!scanner.hasNextDouble()) {
            System.out.println("Invalid amount! Please enter a positive value.");
            scanner.nextLine(); // Consume invalid input
            return;
        }
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        // Validate amount
        if (amount <= 0) {
            System.out.println("Invalid amount! Please enter a positive value.");
            scanner.nextLine(); // Consume invalid input
            return;
        }

        // Check if cash balance is sufficient
        double currentBalance = getCashBalance(userId, sourcePortfolioName);
        if (currentBalance == -1) {
            System.out.println("Unable to retrieve cash balance!");
            return;
        } else if (currentBalance < amount) {
            System.out.println("Insufficient cash balance! Transaction failed.");
            return;
        }

        // Update cash balances in database
        String withdrawSql = "UPDATE Portfolio SET cash_balance = cash_balance - ? WHERE portfolio_id = ?";
        String depositSql = "UPDATE Portfolio SET cash_balance = cash_balance + ? WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt1 = conn.prepareStatement(withdrawSql);
             PreparedStatement pstmt2 = conn.prepareStatement(depositSql)) {

            pstmt1.setDouble(1, amount);
            pstmt1.setInt(2, getPortfolioIDbyName(userId, sourcePortfolioName));
            pstmt1.executeUpdate();

            pstmt2.setDouble(1, amount);
            pstmt2.setInt(2, getPortfolioIDbyName(userId, destPortfolioName));
            pstmt2.executeUpdate();
            System.out.println("Cash transferred successfully!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Manage portfolio holdings
    public void managePortfolioHoldings(int userId) {
        // Display portfolios owned by the user
        if (!canDisplayUserPortfolios(userId)) return;

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the ID of the portfolio to manage: ");
        int portfolioId;
        // Prevent users from inputting non-integer values
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid option. Please try again.");
            scanner.nextLine();
            return;
        }
        portfolioId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Check if portfolio exists
        if (!portfolioExists(portfolioId)) {
            System.out.println("Portfolio does not exist!");
            return;
        }
        // Check if portfolio belongs to the user
        if (getPortfolioIDbyName(userId, getPortfolioNamebyID(portfolioId)) != portfolioId) {
            System.out.println("You do not own this portfolio!");
            return;
        }

        // Manage portfolio holdings
        System.out.println("Going to the portfolio holding dashboard...");
        PortfolioHoldingManager portfolioHoldingManager = new PortfolioHoldingManager();
        portfolioHoldingManager.portfolioHoldingDashboard(portfolioId);
    }

    // Display portfolios owned by the user
    private boolean canDisplayUserPortfolios(int userId) {
        String checkSql = "SELECT * FROM Portfolio WHERE user_id = ? ORDER BY portfolio_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No portfolios found.");
                return false;
            }

            while (rs.next()) {
                int portfolioId = rs.getInt("portfolio_id");
                String name = rs.getString("name");
                double cashBalance = rs.getDouble("cash_balance");
                System.out.println("Portfolio ID: " + portfolioId + " - Name: " + name +
                        " - Cash Balance: $" + cashBalance);
            }
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Cannot display portfolios
    }

    // Get cash balance of a portfolio
    public double getCashBalance(int userId, String portfolioName) {
        // Get cash balance from database
        String sql = "SELECT cash_balance FROM Portfolio WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, getPortfolioIDbyName(userId, portfolioName));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("cash_balance");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return -1;
    }

    // Check if a portfolio exists
    private boolean portfolioExists(int portfolioId) {
        String sql = "SELECT * FROM Portfolio WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Returns true if a portfolio exists

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return false; // Portfolio does not exist
    }

    // Get portfolio ID by name
    public static int getPortfolioIDbyName(int userId, String name) {
        String sql = "SELECT portfolio_id FROM Portfolio WHERE user_id = ? AND name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("portfolio_id");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return 0;
    }

    // Get portfolio name by ID
    public static String getPortfolioNamebyID(int portfolioId) {
        String sql = "SELECT name FROM Portfolio WHERE portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    // Portfolio name validation
    private boolean isValidPortfolioName(String name) {
        // Check if the name is empty, contains invalid characters, or exceeds length
        return name.trim().isEmpty() || !name.matches("[a-zA-Z0-9_ ]+") || name.length() > 50;
    }

    // Check if portfolio belongs to the user
    public boolean isPortfolioOwnedByUser(int userId, int portfolioId) {
        String sql = "SELECT * FROM Portfolio WHERE user_id = ? AND portfolio_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, portfolioId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Returns true if the portfolio belongs to the user

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return false; // Portfolio does not belong to the user
    }
}
