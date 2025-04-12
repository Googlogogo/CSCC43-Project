import managers.*;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Main {
  // Execute SQL file for creating tables
  private static void executeSqlFile(String sqlFile) {
    try (Connection conn = DatabaseConnection.getConnection();
         Statement statement = conn.createStatement();
         BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {

      StringBuilder sql = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Skip comments and empty lines
        if (line.isEmpty() || line.startsWith("--")) {
          continue;
        }

        sql.append(line).append(" ");

        // Execute when the statement is complete (ends with semicolon)
        if (line.endsWith(";")) {
          statement.execute(sql.toString());
          sql.setLength(0); // Clear buffer
        }
      }

      // Check if any remaining SQL without semicolon
      if (!sql.isEmpty()) {
        statement.execute(sql.toString());
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    UserManager userManager = new UserManager();

//        // Create tables, remove if they exist
//        String create_database_file = "src/main/java/create_database.sql";
//        executeSqlFile(create_database_file);

    while (true) {
      System.out.println("""
                    
                    Welcome to the Portfolio and Stock List Management System!
                    
                    1. Register
                    2. Login
                    3. Quit
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
          userManager.userRegistration();
          break;
        case 2:
          int userId = userManager.userLogin();
          if (userId != -1) {
            userManager.userDashboard(userId);
          }
          break;
        case 3:
          System.out.println("Exiting the system. Goodbye!");
          System.exit(0);
//                case 4:
//                    UserManager.viewAllUsers();
//                    break;
        default:
          System.out.println("Invalid option. Please try again.");
      }
    }
  }
}
