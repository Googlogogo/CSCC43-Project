package managers;

import java.sql.*;

public class AccountManager {
    // Update the username of a user
    public static void updateUsername(int userId, String newUsername) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Users SET username = ? WHERE user_id = ?")) {
            pstmt.setString(1, newUsername);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            System.out.println("Username updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating username: " + e.getMessage());
        }
    }

    // Update the email of a user
    public static void updateEmail(int userId, String newEmail) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Users SET email = ? WHERE user_id = ?")) {
            pstmt.setString(1, newEmail);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            System.out.println("Email updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating email: " + e.getMessage());
        }
    }

    // Update the password of a user
    public static void updatePassword(int userId, String newPassword) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Users SET password = ? WHERE user_id = ?")) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            System.out.println("Password updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
    }

    // Delete a user account
    public static void deleteAccount(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Users WHERE user_id = ?")) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            System.out.println("Account deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
        }
    }
}
