package managers;

import java.sql.*;
import java.util.*;

// This class manages user registration, login, and displays user dashboard.
public class UserManager {
    // Dashboard for logged-in users
    public void userDashboard(int userId) {
        PortfolioManager portfolioManager = new PortfolioManager();
        StockListManager stockListManager = new StockListManager();
        FriendManager friendManager = new FriendManager();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("""
                    
                    Welcome to the User Dashboard!
                    
                    1. Manage Portfolios
                    2. Manage Stock Lists
                    3. Manage Friends
                    4. Logout
                    """);
            System.out.print("Choose an option: ");

            int option;
            // Prevent users from inputting non-integer values
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid option. Please try again.");
                scanner.nextLine();
                continue;
            }
            option = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (option) {
                case 1:
                    portfolioManager.portfolioDashboard(userId);
                    break;
                case 2:
                    stockListManager.stockListDashboard(userId);
                    break;
                case 3:
                    friendManager.friendDashboard(userId);
                    break;
                case 4:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // User registration
    public void userRegistration() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nUser Registration:");
            System.out.println("Enter 'back' to return to the main menu.");

            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            if (username.equals("back")) {
                return;
            } else if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                continue;
            } else if (username.length() < 3) {
                System.out.println("Username is too short. Minimum 3 characters required.");
                continue;
            } else if (username.length() > 50) {
                System.out.println("Username is too long. Maximum 50 characters allowed.");
                continue;
            } else if (username.contains(" ")) {
                System.out.println("Username cannot contain spaces.");
                continue;
            } else if (userExists(username)) {
                System.out.println("Username already exists. Please choose a different username.");
                continue;
            }

            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            if (email.equals("back")) {
                return;
            } else if (email.isEmpty()) {
                System.out.println("Email cannot be empty.");
                continue;
            } else if (email.length() < 5) {
                System.out.println("Email is too short. Minimum 5 characters required.");
                continue;
            } else if (email.length() > 100) {
                System.out.println("Email is too long. Maximum 100 characters allowed.");
                continue;
            } else if (email.contains(" ") || !email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                System.out.println("Invalid email format. Please enter a valid email address.");
                continue;
            } else if (emailExists(email)) {
                System.out.println("Email already exists. Please choose a different email.");
                continue;
            }

            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            if (password.equals("back")) {
                return;
            } else if (password.isEmpty()) {
                System.out.println("Password cannot be empty.");
                continue;
            } else if (password.length() < 6) {
                System.out.println("Password is too short. Minimum 6 characters required.");
                continue;
            } else if (password.length() > 255) {
                System.out.println("Password is too long. Maximum 255 characters allowed.");
                continue;
            } else if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
                System.out.println("Password must contain both letters and numbers.");
                continue;
            }

            registerUser(username, email, password);
            break;
        }
    }

    // Register a new user
    public void registerUser(String username, String email, String password) {
        String sql = "INSERT INTO Users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
            System.out.println("User registered successfully!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // User Login
    public int userLogin() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nUser Login:");
            System.out.println("Enter 'back' to return to the main menu.");

            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            if (username.equals("back")) {
                return -1;
            }

            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            if (password.equals("back")) {
                return -1;
            }

            int userId = loginUser(username, password);
            if (userId == -1) {
                System.out.println("Invalid credentials. Try again.");
            } else {
                System.out.println("Login successful!");
                return userId;
            }
        }
    }

    // Login a user
    public int loginUser(String username, String password) {
        String sql = "SELECT user_id FROM Users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id"); // Return user_id if credentials match
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return -1; // no match found (login failed)
    }



    // Get username by ID
    public static String getUsernameByID(int userId) {
        String sql = "SELECT username FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    // Get ID by username
    public static int getIDbyUsername(String username) {
        String sql = "SELECT user_id FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return -1;
    }

    // Check if a user exists
    public static boolean userExists(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Return true if a row is found

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return false; // Return false if no row is found
    }

    // Check if an email exists
    public static boolean emailExists(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Return true if a row is found

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return false; // Return false if no row is found
    }

    // View all users (for testing, remove later)
    public void viewAllUsers() {
        String sql = "SELECT * FROM Users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("User: " + rs.getString("username") +
                        " - Email: " + rs.getString("email") +
                        " - Password: " + rs.getString("password"));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
