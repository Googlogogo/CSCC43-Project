package managers;

import java.sql.*;
import java.util.*;

// This class manages the friend requests and friendships between users.
public class FriendManager {
    // Friend dashboard
    public void friendDashboard(int userId) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""

                    Welcome to the Friends Dashboard!

                    1. Add friend
                    2. View existing friends
                    3. View incoming friend requests
                    4. View outgoing friend requests
                    5. Delete friend
                    """);
            System.out.println("Type 'exit' to go back to home page.");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();
            if (option.equalsIgnoreCase("exit")) {
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(option);
            } catch (NumberFormatException e) {
                System.out.println("Invalid option. Please try again.");
                continue;
            }

            switch (choice) {
                case 1:
                    addFriends(userId);
                    break;
                case 2:
                    viewExistingFriends(userId);
                    break;
                case 3:
                    viewIncomingRequests(userId);
                    break;
                case 4:
                    viewOutgoingRequests(userId);
                    break;
                case 5:
                    deleteFriend(userId);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Add a friend, using areFriends and sendFriendRequest methods
    public void addFriends(int userId) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter friend's username: ");
        String friendUsername = scanner.nextLine();

        if (!UserManager.userExists(friendUsername)) {
            System.out.println("User does not exist!");
            return;
        } else if (friendUsername.equals(UserManager.getUsernameByID(userId))) {
            System.out.println("You cannot add yourself as a friend!");
            return;
        }

        int friendId = -1;
        String sql = "SELECT user_id FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, friendUsername);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                friendId = rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (friendId == -1) {
            System.out.println("User does not exist!");
            return;
        }

        if (areFriends(userId, friendId)) {
            System.out.println("You are already friends!");
            return;
        }

        sendFriendRequest(userId, friendId);
    }

    public void viewExistingFriends(int userID) {
        String sql = "SELECT username FROM Users WHERE user_id IN " +
                "(SELECT receiver_id FROM Friend WHERE requester_id = ? AND status = 'accepted' " +
                "UNION SELECT requester_id FROM Friend WHERE receiver_id = ? AND status = 'accepted')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userID);
            pstmt.setInt(2, userID);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nExisting Friends:");
            if (!rs.isBeforeFirst()) {
                System.out.println("You have no friends yet.");
            } else {
                while (rs.next()) {
                    System.out.println("Friend: " + rs.getString("username"));
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void viewIncomingRequests(int userID) {
        Scanner scanner = new Scanner(System.in);
        String incoming_sql = "SELECT username, user_id FROM Users WHERE user_id IN " +
                "(SELECT requester_id FROM Friend WHERE receiver_id = ? AND status = 'pending')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(incoming_sql)) {

            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nIncoming Friend Requests:");
            if (!rs.isBeforeFirst()) {
                System.out.println("You have no incoming friend requests.");
            } else {
                while (rs.next()) {
                    System.out.println("Friend request from: " + rs.getString("username"));
                    boolean validResponse = false;
                    while (!validResponse) {
                        System.out.print("Accept? (y/n): ");
                        String response = scanner.nextLine();

                        if (response.equalsIgnoreCase("y")) {
                            respondToFriendRequest(userID, rs.getInt("user_id"), true);
                            validResponse = true;
                        } else if (response.equalsIgnoreCase("n")) {
                            respondToFriendRequest(userID, rs.getInt("user_id"), false);
                            validResponse = true;
                        } else {
                            System.out.println("Invalid response. Please enter 'y' or 'n'.");
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void viewOutgoingRequests(int userID) {
        String outgoing_sql = "SELECT username, user_id FROM Users WHERE user_id IN " +
                "(SELECT receiver_id FROM Friend WHERE requester_id = ? AND status = 'pending')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(outgoing_sql)) {

            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nOutgoing Friend Requests:");
            if (!rs.isBeforeFirst()) {
                System.out.println("You have no outgoing friend requests.");
            } else {
                while (rs.next()) {
                    System.out.println("Request to: " + rs.getString("username"));
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Delete a friend
    public void deleteFriend(int userId) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter friend's username to delete: ");
        String friendUsername = scanner.nextLine();
        int friendId = UserManager.getUserIDbyUsername(friendUsername);
        if (friendId == -1) {
            System.out.println("User does not exist!");
            return;
        } else if (friendId == userId) {
            System.out.println("You cannot delete yourself as a friend!");
            return;
        } else if (!areFriends(userId, friendId)) {
            System.out.println("You are not friends!");
            return;
        }

        // SQL query to delete the friend
        String sql = """
                DELETE FROM Friend
                WHERE ((requester_id = ? AND receiver_id = ?) OR (requester_id = ? AND receiver_id = ?))
                AND status = 'accepted'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
            System.out.println("Friend deleted!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Remove shared stock list with the friend
        removeSharedStockList(userId, friendId);
    }

    // Send a friend request
    public void sendFriendRequest(int userId, int friendId) {
        String checkSql = "SELECT status, last_request_time FROM Friend WHERE requester_id = ? AND receiver_id = ?";
        String insertSql = """
                INSERT INTO Friend (requester_id, receiver_id, status, last_request_time)
                VALUES (?, ?, 'pending', CURRENT_TIMESTAMP)
                """;
        String updateSql = """
                UPDATE Friend SET status = 'pending', last_request_time = CURRENT_TIMESTAMP
                WHERE requester_id = ? AND receiver_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {

            checkPstmt.setInt(1, userId);
            checkPstmt.setInt(2, friendId);
            ResultSet rs = checkPstmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                Timestamp lastRequestTime = rs.getTimestamp("last_request_time");
                long currentTime = System.currentTimeMillis();
                long lastRequestMillis = lastRequestTime.getTime();
                long fiveMinutesInMillis = 5 * 60 * 1000;

                if (status.equals("pending") || (status.equals("denied") &&
                        (currentTime - lastRequestMillis < fiveMinutesInMillis))) {
                    System.out.println("Cannot send duplicate request or re-send within five minutes of rejection.");
                    return;
                }

                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    updatePstmt.setInt(1, userId);
                    updatePstmt.setInt(2, friendId);
                    updatePstmt.executeUpdate();
                    System.out.println("Friend request re-sent!");
                }
            } else {
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    insertPstmt.setInt(1, userId);
                    insertPstmt.setInt(2, friendId);
                    insertPstmt.executeUpdate();
                    System.out.println("Friend request sent!");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Accept/deny a friend request
    public void respondToFriendRequest(int userId, int friendId, boolean accept) {
        String update_sql = """
                UPDATE Friend SET status = ?, last_request_time = CURRENT_TIMESTAMP
                WHERE requester_id = ? AND receiver_id = ?""";
        String write_sql = "INSERT INTO Friend (requester_id, receiver_id, status) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(update_sql)) {

            pstmt.setString(1, accept ? "accepted" : "denied");
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
            System.out.println("Friend request " + (accept ? "accepted!" : "denied!"));

            // TODO: If friend request is accepted, add the user as a friend?
            if (accept) {
                try (PreparedStatement pstmt2 = conn.prepareStatement(write_sql)) {
                    pstmt2.setInt(1, userId);
                    pstmt2.setInt(2, friendId);
                    pstmt2.setString(3, "accepted");
                    pstmt2.executeUpdate();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Check if two users are friends
    public static boolean areFriends(int userId, int friendId) {
        String sql = """
                SELECT *
                FROM Friend
                WHERE ((requester_id = ? AND receiver_id = ?) OR (requester_id = ? AND receiver_id = ?))
                AND status = 'accepted'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status").equals("accepted");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return false; // Return false if no row is found
    }

    // Remove shared stock list if friendship is removed
    public void removeSharedStockList(int userId, int friendId) {
        String sql = """
                DELETE FROM SharedStockList
                WHERE shared_user_id = ? AND list_id IN (
                    SELECT list_id
                    FROM StockList
                    WHERE user_id = ?
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, friendId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            System.out.println("Shared stock lists removed successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
