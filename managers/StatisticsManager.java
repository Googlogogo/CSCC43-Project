package managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StatisticsManager {

    public static void handlePortfolioStatistics(int userId, String name) {
        Scanner scanner = new Scanner(System.in);
        boolean active = true;

        while (active) {
            System.out.println("Enter:\n 1 to View Past Statistics\n 2 to Predict Future Prices\n 0 to Back");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter start date (YYYY-MM-DD): ");
                    String startDateStr = scanner.nextLine() + " 00:00:00";
                    Timestamp startTimestamp = Timestamp.valueOf(startDateStr);

                    System.out.print("Enter end date (YYYY-MM-DD): ");
                    String endDateStr = scanner.nextLine() + " 23:59:59";
                    Timestamp endTimestamp = Timestamp.valueOf(endDateStr);

                    displayPortfolioStatistics(userId, name, startTimestamp, endTimestamp);
                    break;

                case 2:
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        int portfolioId = getPortfolioId(userId, name);
                        if (portfolioId == -1) {
                            System.out.println("Portfolio not found.");
                            break;
                        }

                        String sql = "SELECT DISTINCT stock_id FROM Holding WHERE portfolio_id = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, portfolioId);
                        ResultSet rs = stmt.executeQuery();

                        List<Integer> stocks = new ArrayList<>();
                        System.out.println("Available stocks in portfolio:");
                        while (rs.next()) {
                            int stockId = rs.getInt("stock_id");
                            stocks.add(stockId);
                            System.out.println("Stock ID: " + stockId);
                        }

                        if (stocks.isEmpty()) {
                            System.out.println("No stocks found in portfolio.");
                            break;
                        }

                        System.out.print("Enter stock ID to predict: ");
                        int stockId = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Enter number of future days to predict: ");
                        int futureDays = scanner.nextInt();
                        scanner.nextLine();

                        predictStockPrice(stockId, futureDays);

                    } catch (SQLException e) {
                        System.out.println("Error during prediction setup: " + e.getMessage());
                    }
                    break;

                case 0:
                    System.out.println("Going Back.");
                    active = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please enter 0, 1, or 2.");
                    break;
            }
        }
    }

    public static void predictStockPrice(int stockId, int futureDays) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT timestamp, close FROM StockHistory WHERE stock_id = ? ORDER BY timestamp";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();

            List<Double> prices = new ArrayList<>();
            while (rs.next()) {
                prices.add(rs.getDouble("close"));
            }

            if (prices.size() < 2) {
                System.out.println("Not enough data to perform prediction.");
                return;
            }

            int n = prices.size();
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

            for (int i = 0; i < n; i++) {
                double x = i + 1;
                double y = prices.get(i);
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
            }

            double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            double intercept = (sumY - slope * sumX) / n;

            System.out.println("\nPredicted future close prices:");
            for (int i = 1; i <= futureDays; i++) {
                int futureX = n + i;
                double predictedPrice = slope * futureX + intercept;
                System.out.printf("Day %d: %.2f\n", i, predictedPrice);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving stock history for prediction: " + e.getMessage());
        }
    }

    public static void handleStockListStatistics(int userId, String name) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDateStr = scanner.nextLine() + " 00:00:00";
        Timestamp startTimestamp = Timestamp.valueOf(startDateStr);

        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDateStr = scanner.nextLine() + " 23:59:59";
        Timestamp endTimestamp = Timestamp.valueOf(endDateStr);

        displayStockListStatistics(userId, name, startTimestamp, endTimestamp);
    }

    public static void displayPortfolioStatistics(int userId, String portfolioName, Timestamp start, Timestamp end) {
        try {
            int portfolioId = getPortfolioId(userId, portfolioName);
            if (portfolioId == -1) {
                System.out.println("Portfolio not found.");
                return;
            }

            System.out.println("\nCalculating COV, Beta, and Covariance Matrix for portfolio: " + portfolioName);
            displayCOVs(portfolioId, start, end, true);
            displayBetas(portfolioId, start, end, true);
            displayCovarianceMatrix(portfolioId, start, end, true);

        } catch (SQLException e) {
            System.out.println("Error retrieving portfolio statistics: " + e.getMessage());
        }
    }

    public static void displayStockListStatistics(int userId, String listName, Timestamp start, Timestamp end) {
        try {
            int listId = getListId(userId, listName);
            if (listId == -1) {
                System.out.println("Stock list not found.");
                return;
            }

            System.out.println("\nCalculating COV, Beta, and Covariance Matrix for stock list: " + listName);
            displayCOVs(listId, start, end, false);
            displayBetas(listId, start, end, false);
            displayCovarianceMatrix(listId, start, end, false);

        } catch (SQLException e) {
            System.out.println("Error retrieving stock list statistics: " + e.getMessage());
        }
    }

    private static int getPortfolioId(int userId, String portfolioName) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT portfolio_id FROM Portfolio WHERE user_id = ? AND name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, portfolioName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("portfolio_id");
            }
        }
        return -1;
    }

    private static int getListId(int userId, String listName) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT list_id FROM StockList WHERE user_id = ? AND name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, listName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("list_id");
            }
        }
        return -1;
    }

    private static void displayCOVs(int id, Timestamp start, Timestamp end, boolean isPortfolio)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String table = isPortfolio ? "Holding" : "ListHold";
            String sql = "SELECT h.stock_id, AVG(s.close) AS mean, STDDEV(s.close) AS stddev " +
                    "FROM " + table + " h " +
                    "JOIN StockHistory s ON h.stock_id = s.stock_id " +
                    "WHERE h." + (isPortfolio ? "portfolio_id" : "list_id") + " = ? AND s.timestamp BETWEEN ? AND ? " +
                    "GROUP BY h.stock_id";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setTimestamp(2, start);
            stmt.setTimestamp(3, end);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nCoefficient of Variation (COV):");
            while (rs.next()) {
                int stockId = rs.getInt("stock_id");
                double mean = rs.getDouble("mean");
                double stddev = rs.getDouble("stddev");
                if (mean != 0) {
                    double cov = stddev / mean;
                    System.out.printf("Stock %d: COV = %.4f\n", stockId, cov);
                } else {
                    System.out.printf("Stock %d: COV = undefined (mean = 0)\n", stockId);
                }
            }
        }
    }

    private static void displayBetas(int id, Timestamp start, Timestamp end, boolean isPortfolio)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int marketIndexId = 999;
            String table = isPortfolio ? "Holding" : "ListHold";
            String sql = "SELECT h.stock_id, " +
                    "COVAR_POP(s.close, m.close) / VAR_POP(m.close) AS beta " +
                    "FROM " + table + " h " +
                    "JOIN StockHistory s ON h.stock_id = s.stock_id " +
                    "JOIN StockHistory m ON s.timestamp = m.timestamp AND m.stock_id = ? " +
                    "WHERE h." + (isPortfolio ? "portfolio_id" : "list_id") + " = ? AND s.timestamp BETWEEN ? AND ? " +
                    "GROUP BY h.stock_id";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, marketIndexId);
            stmt.setInt(2, id);
            stmt.setTimestamp(3, start);
            stmt.setTimestamp(4, end);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nBeta Values (vs. Market Index ID " + marketIndexId + "):");
            while (rs.next()) {
                int stockId = rs.getInt("stock_id");
                double beta = rs.getDouble("beta");
                System.out.printf("Stock %d: Beta = %.4f\n", stockId, beta);
            }
        }
    }

    private static void displayCovarianceMatrix(int id, Timestamp start, Timestamp end,
            boolean isPortfolio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String table = isPortfolio ? "Holding" : "ListHold";
            String sqlStocks = "SELECT DISTINCT stock_id FROM " + table + " WHERE "
                    + (isPortfolio ? "portfolio_id" : "list_id") + " = ?";
            PreparedStatement stmtStocks = conn.prepareStatement(sqlStocks);
            stmtStocks.setInt(1, id);
            ResultSet rsStocks = stmtStocks.executeQuery();

            Map<Integer, String> stockIdMap = new HashMap<>();
            int index = 0;
            while (rsStocks.next()) {
                int stockId = rsStocks.getInt("stock_id");
                stockIdMap.put(index++, String.valueOf(stockId));
            }

            System.out.println("\nCovariance Matrix:");
            for (int i = 0; i < stockIdMap.size(); i++) {
                for (int j = 0; j < stockIdMap.size(); j++) {
                    String sqlCov = "SELECT COVAR_POP(s1.close, s2.close) AS cov " +
                            "FROM StockHistory s1 " +
                            "JOIN StockHistory s2 ON s1.timestamp = s2.timestamp " +
                            "WHERE s1.stock_id = ? AND s2.stock_id = ? " +
                            "AND s1.timestamp BETWEEN ? AND ?";

                    PreparedStatement stmt = conn.prepareStatement(sqlCov);
                    stmt.setInt(1, Integer.parseInt(stockIdMap.get(i)));
                    stmt.setInt(2, Integer.parseInt(stockIdMap.get(j)));
                    stmt.setTimestamp(3, start);
                    stmt.setTimestamp(4, end);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        double cov = rs.getDouble("cov");
                        System.out.printf("%.4f ", cov);
                    } else {
                        System.out.print("0.0000 ");
                    }
                }
                System.out.println();
            }
        }
    }
}