package managers;

import java.sql.*;
import java.util.*;

// This class manages the reviews for a user's stock list, allowing them to add, view, and delete reviews.
public class ReviewManager {
    // Review Management Dashboard
    public void reviewDashboard(int userId, int listId) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""

                    Welcome to Review Management!

                    1. Add Review
                    2. View All Reviews
                    3. Delete Review
                    4. Back to Stock List Dashboard
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
                    addReview(userId, listId);
                    break;
                case 2:
                    viewAllReviews(userId, listId);
                    break;
                case 3:
                    deleteReview(userId, listId);
                    break;
                case 4:
                    return; // Go back to portfolio dashboard
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Add a review to the user's stock list
    public void addReview(int userId, int listId) {
        // Check if the user has access to the review
        if (notHasAccessToReview(userId, listId)) {
            System.out.println("You do not have access to this review.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        // Prompt user for review content
        System.out.print("Enter your review: ");
        String review = scanner.nextLine();

        String query = "INSERT INTO Review (list_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, listId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, review);
            pstmt.executeUpdate();
            System.out.println("Review added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
        }
    }

    // View all reviews for a specific stock list
    public void viewAllReviews(int userId, int listId) {
        // Check if the user has access to the review
        if (notHasAccessToReview(userId, listId)) {
            System.out.println("You do not have access to this review.");
            return;
        }

        // Fetch and display reviews
        String query;
        if (getOwnerId(listId) == userId) {
            // If the user is the owner, they can view all reviews
            query = "SELECT * FROM Review WHERE list_id = ?";
            try (PreparedStatement pstmt1 = DatabaseConnection.getConnection().prepareStatement(query)) {
                pstmt1.setInt(1, listId);
                ResultSet rs = pstmt1.executeQuery();
                if (!rs.isBeforeFirst()) {
                    System.out.println("No reviews found.");
                    return;
                }
                while (rs.next()) {
                    int rId = rs.getInt("review_id");
                    int lId = rs.getInt("list_id");
                    int uId = rs.getInt("user_id");
                    String listName = StockListManager.getStockListNameById(lId);
                    String content = rs.getString("content");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    // Format the createdAt timestamp to a readable format
                    String createdAtFormatted = createdAt.toLocalDateTime().toLocalDate().toString();
                    System.out.printf("""
                            
                            Review ID: %d
                            Stock List Name: %s
                            Created By: %s
                            Content: %s
                            Created At: %s
                            """, rId, listName, UserManager.getUsernameByID(uId), content, createdAtFormatted);
                }
            } catch (SQLException e) {
                System.err.println("Error preparing statement: " + e.getMessage());
            }
        } else {
            // If the user is not the owner, they can only view their own reviews and the owner's reviews
            query = """
                SELECT *
                FROM Review r
                JOIN StockList sl ON r.list_id = sl.list_id
                WHERE r.list_id = ? AND (r.user_id = ? OR (r.user_id = sl.user_id))
                """;
            try (PreparedStatement pstmt2 = DatabaseConnection.getConnection().prepareStatement(query)) {
                pstmt2.setInt(1, listId);
                pstmt2.setInt(2, userId);
                ResultSet rs = pstmt2.executeQuery();
                if (!rs.isBeforeFirst()) {
                    System.out.println("No reviews found.");
                    return;
                }
                while (rs.next()) {
                    int rId = rs.getInt("review_id");
                    int lId = rs.getInt("list_id");
                    int uId = rs.getInt("user_id");
                    String listName = StockListManager.getStockListNameById(lId);
                    String content = rs.getString("content");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    // Format the createdAt timestamp to a readable format
                    String createdAtFormatted = createdAt.toLocalDateTime().toLocalDate().toString();
                    System.out.printf("""
                            Review ID: %d
                            Stock List Name: %s
                            Created By: %s
                            Content: %s
                            Created At: %s
                            """, rId, listName, UserManager.getUsernameByID(uId), content, createdAtFormatted);
                }
            } catch (SQLException e) {
                System.err.println("Error preparing statement: " + e.getMessage());
            }
        }
    }

    // Delete a review
    public void deleteReview(int userId, int listId) {
        Scanner scanner = new Scanner(System.in);
        // Prompt user for the review ID to delete
        System.out.print("Enter the review ID to delete: ");
        int reviewId = scanner.nextInt();
        scanner.nextLine();

        // Check if the review belongs to the user
        if (!checkReviewOwnership(reviewId, userId, listId)) {
            System.out.println("You cannot delete a review that does not belong to you.");
            return;
        }

        String query = "DELETE FROM Review WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Review deleted successfully!");
            } else {
                System.out.println("No review found to delete.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
        }
    }

    // Check if the review belongs to the user
    public boolean checkReviewOwnership(int reviewId, int userId, int listId) {
        String query = "SELECT * FROM Review WHERE review_id = ? AND user_id = ? AND list_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, listId);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Return true if the review exists and belongs to the user
        } catch (SQLException e) {
            System.err.println("Error checking review ownership: " + e.getMessage());
        }
        return false; // Return false if the review does not exist or does not belong to the user
    }

    // Check if the user has access to the review
    public boolean notHasAccessToReview(int userId, int listId) {
        String query = """
               SELECT *
               FROM StockList sl
               LEFT JOIN SharedStockList ssl ON sl.list_id = ssl.list_id
               WHERE sl.list_id = ? AND (sl.user_id = ? OR ssl.shared_user_id = ? OR sl.visibility = 'public')
               """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, listId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);

            ResultSet rs = pstmt.executeQuery();
            return !rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking access to review: " + e.getMessage());
            return true;
        }
    }

    // Get the owner ID of the stock list
    public int getOwnerId(int listId) {
        String query = "SELECT user_id FROM StockList WHERE list_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, listId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching owner ID: " + e.getMessage());
        }
        return -1; // Return -1 if the owner ID is not found
    }
}
