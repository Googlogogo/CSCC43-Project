package managers;

import java.sql.*;
import java.util.*;

// This class manages the stock lists for a user, allowing them to create, share, delete, and view their stock lists.
public class StockListManager {
    // StockList Dashboard
    public void stockListDashboard(int userId) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    
                    Welcome to Stock List Dashboard!
                    
                    1. Create New Stock List
                    2. View All Stock Lists
                    3. Delete Stock List
                    4. Change Stock List Name
                    5. Change Stock List Visibility / Share Stock List
                    6. Manage Personal Stock List Holdings
                    7. Manage Stock List Reviews
                    8. Display Stock List Statistics
                    """);
            System.out.println("Type 'exit' to exit the dashboard.");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();
            if (option.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the Stock List Dashboard...");
                return;
            }

            switch (option) {
                case "1":
                    // Create a new stock list
                    System.out.println("\nCreating a new stock list...");
                    createStockList(userId);
                    break;
                case "2":
                    // View all stock lists
                    System.out.println("\nViewing all stock lists...");
                    viewAllStockLists(userId);
                    break;
                case "3":
                    // Delete a stock list
                    System.out.println("\nDeleting a stock list...");
                    deleteStockList(userId);
                    break;
                case "4":
                    // Update the stock list name
                    System.out.println("\nUpdating a stock list name...");
                    updateStockListName(userId);
                    break;
                case "5":
                    // Update the stock list visibility
                    System.out.println("\nUpdating a stock list visibility...");
                    updateVisibility(userId);
                    break;
                case "6":
                    // Check the stock list can only be managed if it is owned by the user
                    System.out.println("\nManaging stock list holdings...");
                    manageStockListHoldings(userId);
                    break;
                case "7":
                    // Check if the stock list belongs to the user
                    System.out.println("\nManaging stock list reviews...");
                    manageStockListReviews(userId);
                    break;
                case "8":
                    // Display stock list statistics
                    System.out.println("\nDisplaying stock list statistics...");
                    manageStatistics(userId);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Create a new stock list
    public void createStockList(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt the user for the stock list name and visibility
        System.out.print("Enter the name of the stock list: ");
        String listName = scanner.nextLine();

        // Validate the stock list name
        if (isValidStockListName(listName)) {
            System.out.println("Invalid stock list name!");
            return;
        }
        // Check if the stock list already exists
        if (stockListExists(getStockListIdByName(userId, listName))) {
            System.out.println("Stock list already exists! Please choose a different name.");
            return;
        }

        System.out.print("Enter the visibility (public/private/shared): ");
        String visibility = scanner.nextLine().toLowerCase();

        // Validate the visibility input
        if (!visibility.equals("public") && !visibility.equals("private") && !visibility.equals("shared")) {
            System.out.println("Invalid visibility option. Please choose \"public\", \"private\" or \"shared\".");
            return;
        }

        // SQL query to insert a new stock list into the database
        int listId = insertStockList(userId, listName, visibility);
        if (listId == -1) {
            System.out.println("Failed to create stock list.");
            return;
        }

        // Check if the user wants to share the stock list
        if (visibility.equalsIgnoreCase("shared")) {
            shareStockList(userId, listId);
        }
    }

    // Insert a new stock list into the database in SQL
    private int insertStockList(int userId, String listName, String visibility) {
        String sql = "INSERT INTO StockList (user_id, name, visibility) VALUES (?, ?, ?) RETURNING list_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, listName);
            pstmt.setString(3, visibility);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Stock list created successfully!");
                return rs.getInt("list_id");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return -1; // Stock list not created
    }

    // View all stock lists
    public void viewAllStockLists(int userId) {
        Scanner scanner = new Scanner(System.in);

        // SQL query to fetch all stock lists for the user
        String sql = """
            SELECT *
            FROM StockList sl
            LEFT JOIN SharedStockList ssl ON sl.list_id = ssl.list_id
            WHERE sl.user_id = ? OR sl.visibility = 'public' OR (sl.visibility = 'shared' AND ssl.shared_user_id = ?)
            ORDER BY sl.list_id ASC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            List<Integer> listIds = new ArrayList<>();
            while (rs.next()) {
                int listId = rs.getInt("list_id");
                int ownerId = rs.getInt("user_id");
                String listName = rs.getString("name");
                String visibility = rs.getString("visibility");
                listIds.add(listId);
                System.out.println("Stock List ID: " + listId + " - Owner: " + UserManager.getUsernameByID(ownerId) +
                        " - Stock List: " + listName + " - Visibility: " + visibility);
            }

            if (listIds.isEmpty()) {
                System.out.println("No stock lists available.");
                return;
            } else {
                System.out.println("Total stock lists: " + listIds.size());
            }

            // Prompt the user for the stock id to view
            System.out.println("\nYou can view the details of a stock list.");
            System.out.println("Enter '-1' to go back.");
            System.out.print("\nEnter the ID of the stock list to view: ");

            // Prevent users from inputting non-integer values
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid option. Please try again.");
                scanner.nextLine();
                return;
            }
            int selectedListId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            // Check if the user wants to exit
            if (selectedListId == -1) {
                System.out.println("Exiting the stock list view...");
                return;
            }

            if (listIds.contains(selectedListId)) {
                // View stock list holding details
                StockListHoldingManager stockListHoldingManager = new StockListHoldingManager();
                stockListHoldingManager.viewAllStocksInList(selectedListId);
            } else {
                System.out.println("Invalid stock list ID.");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Delete a stock list
    public void deleteStockList(int userId) {
        // Display the stock lists owned by the user
        if (!canDisplayUserStockLists(userId)) return;

        Scanner scanner = new Scanner(System.in);
        // Prompt the user for the stock list name to delete
        System.out.print("Enter the ID of the stock list to delete: ");
        int listId;
        // Prevent users from inputting non-integer values
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid option. Please try again.");
            scanner.nextLine();
            return;
        }
        listId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Prompt the user for confirmation
        System.out.print("Are you sure you want to delete this stock list? (y/n): ");
        String confirmation = scanner.nextLine();
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Stock list deletion cancelled.");
            return;
        }

        // Check if the stock list exists
        if (!stockListExists(listId)) {
            System.out.println("Stock list does not exist!");
            return;
        }
        // Check if the stock list belongs to the user
        if (!checkStockListBelongsToUser(userId, listId)) {
            System.out.println("You do not own this stock list!");
            return;
        }

        // SQL queries to delete the associated entries
        String deleteReviewsSql = "DELETE FROM Review WHERE list_id = ?";
        String deleteSharedStockListSql = "DELETE FROM SharedStockList WHERE list_id = ?";
        String deleteStockListHoldingSql = "DELETE FROM StockListHolding WHERE list_id = ?";
        String deleteStockListSql = "DELETE FROM StockList WHERE list_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteReviewsStmt = conn.prepareStatement(deleteReviewsSql);
             PreparedStatement deleteSharedStockListStmt = conn.prepareStatement(deleteSharedStockListSql);
             PreparedStatement deleteStockListHoldingStmt = conn.prepareStatement(deleteStockListHoldingSql);
             PreparedStatement deleteStockListStmt = conn.prepareStatement(deleteStockListSql)) {

            // Start transaction
            conn.setAutoCommit(false);

            // Delete reviews
            deleteReviewsStmt.setInt(1, listId);
            deleteReviewsStmt.executeUpdate();

            // Delete shared stock list entries
            deleteSharedStockListStmt.setInt(1, listId);
            deleteSharedStockListStmt.executeUpdate();

            // Delete stock list holdings
            deleteStockListHoldingStmt.setInt(1, listId);
            deleteStockListHoldingStmt.executeUpdate();

            // Delete stock list
            deleteStockListStmt.setInt(1, listId);
            deleteStockListStmt.executeUpdate();

            // Commit transaction
            conn.commit();
            System.out.println("Stock list and associated entries deleted successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Rollback transaction in case of error
                conn.rollback();
                System.out.println("Transaction rolled back due to an error.");
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        }
    }

    // Update the stock list name
    public void updateStockListName(int userId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt the user for the stock list name to update
        System.out.print("Enter the name of the stock list to update: ");
        String oldListName = scanner.nextLine();
        int listId = getStockListIdByName(userId, oldListName);

        // Check if the stock list exists
        if (!stockListExists(listId)) {
            System.out.println("Stock list does not exist!");
            return;
        }
        // Check if the stock list belongs to the user
        if (!checkStockListBelongsToUser(userId, listId)) {
            System.out.println("You do not own this stock list!");
            return;
        }

        System.out.print("Enter the new name for the stock list: ");
        String newListName = scanner.nextLine();

        // Validate the new stock list name
        if (isValidStockListName(newListName)) {
            System.out.println("Invalid stock list name!");
            return;
        }
        // Check if the new stock list name already exists
        if (stockListExists(getStockListIdByName(userId, newListName))) {
            System.out.println("Stock list with this name already exists!");
            return;
        }

        // SQL query to update the stock list name
        String sql = "UPDATE StockList SET name = ? WHERE list_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newListName);
            pstmt.setInt(2, listId);
            pstmt.executeUpdate();
            System.out.println("Stock list name updated successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Update the stock list visibility
    public void updateVisibility(int userId) {
        // Display the stock lists owned by the user
        if (!canDisplayUserStockLists(userId)) return;

        Scanner scanner = new Scanner(System.in);
        // Prompt the user for the stock list name to update
        System.out.print("\nEnter the ID of the stock list to update: ");
        int listId;
        // Prevent users from inputting non-integer values
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid option. Please try again.");
            scanner.nextLine();
            return;
        }
        listId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Check if the stock list exists
        if (!stockListExists(listId)) {
            System.out.println("Stock list does not exist!");
            return;
        }
        // Check if the stock list belongs to the user
        if (!checkStockListBelongsToUser(userId, listId)) {
            System.out.println("You do not own this stock list!");
            return;
        }

        System.out.print("Enter the new visibility (public/private/shared): ");
        String newVisibility = scanner.nextLine().toLowerCase();

        // Validate the visibility input
        if (!newVisibility.equals("public") && !newVisibility.equals("private") && !newVisibility.equals("shared")) {
            System.out.println("Invalid visibility option. Please choose 'public', 'private' or 'shared'.");
            return;
        }

        // Check if user wants to share the stock list
        if (newVisibility.equalsIgnoreCase("shared")) {
            shareStockList(userId, listId);
        } else {
            // Update the stock list visibility only
            updateStockListVisibility(listId, newVisibility);
        }
    }

    // Share a stock list with another user, while not changing the visibility
    private boolean shareStockListWithUser(int userId, int listId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt the user for the username to share with
        System.out.print("Enter the username to share with: ");
        String username = scanner.nextLine();
        int friendId = UserManager.getUserIDbyUsername(username);

        // Check if the user exists
        if (friendId == -1) {
            System.out.println("User does not exist!");
            return false;
        }
        // Check if the stock list is already shared with the user
        if (isSharedAndAccessible(userId, listId)) {
            System.out.println("Stock list is already shared with this user!");
            return false;
        }
        // Check if the input username is the same as the logged-in user
        if (friendId == userId) {
            System.out.println("You cannot share the stock list with yourself!");
            return false;
        }
        // Check if the input user is friend with the logged-in user
        if (!FriendManager.areFriends(userId, friendId)) {
            System.out.println("This user is not your friend! You cannot share the stock list with them.");
            return false;
        }

        // SQL query to share the stock list
        String sql = "INSERT INTO SharedStockList (list_id, shared_user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setInt(2, friendId);
            pstmt.executeUpdate();
            System.out.println("Stock list shared successfully!");
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock list not shared
    }

    // Share a stock list with another user, while updating its visibility
    private void shareStockList(int userId, int listId) {
        // Attempt to share the stock list with another user
        if (!shareStockListWithUser(userId, listId)) return;

        // SQL query to update the visibility of the stock list
        updateStockListVisibility(listId, "shared");
    }

    // SQL query to update the stock list visibility
    public void updateStockListVisibility(int listId, String visibility) {
        // SQL query to update the stock list visibility
        String sql = "UPDATE StockList SET visibility = ? WHERE list_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, visibility);
            pstmt.setInt(2, listId);
            pstmt.executeUpdate();
            System.out.println("Stock list visibility updated successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Manage stock list holdings
    public void manageStockListHoldings(int userId) {
        // Display the stock lists owned by the user
        if (!canDisplayUserStockLists(userId)) return;

        Scanner scanner = new Scanner(System.in);
        // Prompt the user for the stock list name to manage
        System.out.print("\nEnter the ID of the stock list to view details: ");
        int listId;
        // Prevent users from inputting non-integer values
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid option. Please try again.");
            scanner.nextLine();
            return;
        }
        listId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Check if the stock list exists
        if (!stockListExists(listId)) {
            System.out.println("Stock list does not exist!");
            return;
        }
        // Check if the user has access to the stock list
        if (!canAccessStockList(userId, listId)) {
            System.out.println("You do not have access to this stock list!");
            return;
        }

        // Manage stock list holdings
        System.out.println("Going to the stock list holding dashboard...");
        StockListHoldingManager stockListHoldingManager = new StockListHoldingManager();
        stockListHoldingManager.stockListHoldingDashboard(listId);
    }

    // Manage stock list reviews
    public void manageStockListReviews(int userId) {
        // Display the stock lists owned by the user
        if (!canDisplayUserStockLists(userId)) return;

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the ID of the stock list to manage reviews: ");
        int reviewListId;
        // Prevent users from inputting non-integer values
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid option. Please try again.");
            scanner.nextLine();
            return;
        }
        reviewListId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Check if the stock list exists
        if (!stockListExists(reviewListId)) {
            System.out.println("Stock list does not exist!");
            return;
        }
        // Check if the user has access to the stock list
        if (!canAccessStockList(userId, reviewListId)) {
            System.out.println("You do not have access to this stock list!");
            return;
        }

        System.out.println("Going to the review dashboard...");
        ReviewManager reviewManager = new ReviewManager();
        reviewManager.reviewDashboard(userId, reviewListId);
    }

    // Display stock list statistics
    public void manageStatistics(int userId) {
        // Display the stock lists owned by the user
        if (!canDisplayUserStockLists(userId)) return;

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the ID of the stock list to view statistics: ");
        int listId;
        // Prevent users from inputting non-integer values
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid option. Please try again.");
            scanner.nextLine();
            return;
        }
        listId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Check if the stock list exists
        if (!stockListExists(listId)) {
            System.out.println("Stock list does not exist!");
            return;
        }
        // Check if the user has access to the stock list
        if (!canAccessStockList(userId, listId)) {
            System.out.println("You do not have access to this stock list!");
            return;
        }

        StatisticsManager.handleStockListStatistics(userId, getStockListNameById(listId));
    }

    // Display stock list for the user
    public boolean canDisplayUserStockLists(int userId) {
        String checksql = """
                SELECT *
                FROM StockList s
                WHERE s.user_id = ? OR s.visibility = 'public' OR (s.visibility = 'shared' AND s.list_id IN (
                    SELECT list_id
                    FROM SharedStockList
                    WHERE list_id = s.list_id AND shared_user_id = ?
                ))
                ORDER BY s.list_id ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checksql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("No stock lists available.");
                return false;
            }

            System.out.println("\nYour Available Stock Lists:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("list_id") +
                        " - Owner: " + UserManager.getUsernameByID(rs.getInt("user_id")) +
                        " - Stock List: " + rs.getString("name") +
                        " - Visibility: " + rs.getString("visibility"));
            }
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Cannot display stock lists
    }

    // Check if the stock list is shared and accessible to the user
    private boolean isSharedAndAccessible(int userId, int listId) {
        String sql = """
            SELECT *
            FROM StockList sl
            JOIN SharedStockList ssl ON sl.list_id = ssl.list_id
            WHERE sl.visibility = 'shared' AND sl.list_id = ? AND (sl.user_id = ? OR ssl.shared_user_id = ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Returns true if the stock list is shared and accessible

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock list is not shared or not accessible
    }

    // Check if the stock list exists
    private boolean stockListExists(int listId) {
        String sql = "SELECT * FROM StockList WHERE list_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Returns true if the stock list exists

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock list does not exist
    }

    // Get stock list ID by name
    public static int getStockListIdByName(int userId, String listName) {
        String sql = "SELECT list_id FROM StockList WHERE user_id = ? AND name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, listName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("list_id");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1; // Stock list not found
    }

    // Get stock list name by ID
    public static String getStockListNameById(int listId) {
        String sql = "SELECT name FROM StockList WHERE list_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null; // Stock list not found
    }

    // Get holding ID

    // Check if the stock list belongs to the user
    private boolean checkStockListBelongsToUser(int userId, int listId) {
        String sql = "SELECT * FROM StockList WHERE list_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Returns true if the stock list belongs to the user

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // Stock list does not belong to the user
    }

    // Validate the stock list name
    private boolean isValidStockListName(String listName) {
        // Check if the stock list name is valid (e.g., not empty, not too long)
        return listName == null || listName.trim().isEmpty() ||
                !listName.matches("^[a-zA-Z0-9_ ]+$") || listName.length() > 50;
    }

    // Check if the user can access the stock list
    public static boolean canAccessStockList(int userId, int listId) {
        String sql = """
                SELECT *
                FROM StockList sl
                LEFT JOIN SharedStockList ssl ON sl.list_id = ssl.list_id
                WHERE sl.list_id = ? AND (sl.user_id = ? OR ssl.shared_user_id = ? OR sl.visibility = 'public')
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Returns true if the user can access the stock list

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false; // User cannot access the stock list
    }
}
