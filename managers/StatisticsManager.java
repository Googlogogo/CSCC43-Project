package managers;

import java.sql.*;
import java.time.*;
import java.util.*;

// This class handles the statistics, historic prices and predictions for portfolios and stock lists.
public class StatisticsManager {

    public static void handlePortfolioStatistics(int userId, String name) {
        Scanner scanner = new Scanner(System.in);
        boolean active = true;

        while (active) {
            System.out.println("""
                    
                    Enter:
                     1 to View Statistics
                     2 to View Historical Prices
                     3 to Predict Future Prices
                     0 to Back""");
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid option. Please try again.");
                scanner.nextLine();
                continue;
            }
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter start date (YYYY-MM-DD): ");
                    // Check date format
                    String startDate = scanner.nextLine();
                    if (!isValidDate(startDate)) {
                        System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                        break;
                    }
                    // Append time to date string
                    String startDateStr = startDate + " 00:00:00";
                    Timestamp startTimestamp = Timestamp.valueOf(startDateStr);

                    System.out.print("Enter end date (YYYY-MM-DD): ");
                    // Check date format
                    String endDate = scanner.nextLine();
                    if (!isValidDate(endDate)) {
                        System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                        break;
                    }
                    // Check end date is after start date
                    if (LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))) {
                        System.out.println("End date must be after start date.");
                        break;
                    }
                    // Append time to date string
                    String endDateStr = endDate + " 23:59:59";
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

                        // Get stocks in portfolio
                        String sql = "SELECT DISTINCT symbol FROM PortfolioHolding WHERE portfolio_id = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, portfolioId);
                        ResultSet rs = stmt.executeQuery();

                        List<String> stocks = new ArrayList<>();
                        System.out.println("Available stocks in portfolio:");
                        while (rs.next()) {
                            String symbol = rs.getString("symbol");
                            stocks.add(symbol);
                            StockManager.getStockDetails(symbol);
                        }

                        if (stocks.isEmpty()) {
                            System.out.println("No stocks found in portfolio.");
                            break;
                        }

                        System.out.print("Enter stock symbol to view: ");
                        String stockSymbol = scanner.nextLine();

                        if (!stocks.contains(stockSymbol)) {
                            System.out.println("Stock not found in portfolio.");
                            break;
                        }

                        System.out.println("Select time interval:");
                        System.out.println("1. Past week");
                        System.out.println("2. Past month");
                        System.out.println("3. Past quarter (3 months)");
                        System.out.println("4. Past year");
                        System.out.println("5. Past 5 years");
                        System.out.println("6. All time intervals");
                        System.out.println("7. Custom interval");
                        int intervalChoice = scanner.nextInt();
                        scanner.nextLine();

                        Timestamp newStartTimestamp = null;
                        Timestamp newEndTimestamp = Timestamp.valueOf(LocalDateTime.now());

                        switch (intervalChoice) {
                            case 1:
                                newStartTimestamp = Timestamp.valueOf(LocalDateTime.now().minusWeeks(1));
                                break;
                            case 2:
                                newStartTimestamp = Timestamp.valueOf(LocalDateTime.now().minusMonths(1));
                                break;
                            case 3:
                                newStartTimestamp = Timestamp.valueOf(LocalDateTime.now().minusMonths(3));
                                break;
                            case 4:
                                newStartTimestamp = Timestamp.valueOf(LocalDateTime.now().minusYears(1));
                                break;
                            case 5:
                                newStartTimestamp = Timestamp.valueOf(LocalDateTime.now().minusYears(5));
                                break;
                            case 6:
                                // Display all time intervals
                                System.out.println("Displaying all historical prices for " + stockSymbol + ":");
                                newStartTimestamp = Timestamp.valueOf("1900-01-01 00:00:00");
                                break;
                            case 7:
                                System.out.print("Enter start date (YYYY-MM-DD): ");
                                String newStartDate = scanner.nextLine();
                                if (!isValidDate(newStartDate)) {
                                    System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                                    break;
                                }

                                System.out.print("Enter end date (YYYY-MM-DD): ");
                                String newEndDate = scanner.nextLine();
                                if (!isValidDate(newEndDate)) {
                                    System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                                    break;
                                }
                                // Check end date is after start date
                                if (LocalDate.parse(newEndDate).isBefore(LocalDate.parse(newStartDate))) {
                                    System.out.println("End date must be after start date.");
                                    break;
                                }
                                newStartTimestamp = Timestamp.valueOf(newStartDate + " 00:00:00");
                                newEndTimestamp = Timestamp.valueOf(newEndDate + " 23:59:59");
                                break;
                            default:
                                System.out.println("Invalid choice. Please try again.");
                                break;
                        }

                        if (newStartTimestamp != null) {
                            displayHistoricalPrices(stockSymbol, newStartTimestamp, newEndTimestamp);
                        }

                    } catch (SQLException e) {
                        System.out.println("Error retrieving historical data: " + e.getMessage());
                    }
                    break;

                case 3:
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        int portfolioId = getPortfolioId(userId, name);
                        if (portfolioId == -1) {
                            System.out.println("Portfolio not found.");
                            break;
                        }

                        String sql = "SELECT DISTINCT symbol FROM PortfolioHolding WHERE portfolio_id = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, portfolioId);
                        ResultSet rs = stmt.executeQuery();

                        List<String> stocks = new ArrayList<>();
                        System.out.println("Available stocks in portfolio:");
                        while (rs.next()) {
                            String symbol = rs.getString("symbol");
                            stocks.add(symbol);
                            StockManager.getStockDetails(symbol);
                        }

                        if (stocks.isEmpty()) {
                            System.out.println("No stocks found in portfolio.");
                            break;
                        }

                        System.out.print("Enter stock symbol to predict: ");
                        String stockSymbol = scanner.nextLine();

                        if (!stocks.contains(stockSymbol)) {
                            System.out.println("Stock not found in portfolio.");
                            break;
                        }

                        System.out.print("Enter number of future days to predict: ");
                        int futureDays = scanner.nextInt();
                        scanner.nextLine();

                        // Check if futureDays is a positive integer
                        if (futureDays <= 0) {
                            System.out.println("Invalid number of days. Please enter a positive integer.");
                            break;
                        }

                        System.out.println("Select prediction model:");
                        System.out.println("1. Linear Regression");
                        System.out.println("2. Moving Average");
                        System.out.println("3. Exponential Smoothing");
                        int modelType = scanner.nextInt();
                        scanner.nextLine();

                        predictStockPrice(stockSymbol, futureDays, modelType);

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

    public static void predictStockPrice(String stockSymbol, int futureDays, int modelType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query that combines both historical and new stock data
            String sql = """
                    SELECT timestamp, close
                    FROM StockHistory
                    WHERE symbol = ?
                    ORDER BY timestamp
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, stockSymbol);
            ResultSet rs = stmt.executeQuery();

            List<Double> prices = new ArrayList<>();
            List<Timestamp> dates = new ArrayList<>();

            while (rs.next()) {
                prices.add(rs.getDouble("close"));
                dates.add(rs.getTimestamp("timestamp"));
            }

            if (prices.size() < 10) {
                System.out.println("Not enough data to perform reliable prediction.");
                return;
            }

            // Get the most recent date
            Timestamp lastDate = dates.get(dates.size() - 1);
            LocalDate baseDate = lastDate.toLocalDateTime().toLocalDate();

            System.out.println("\nPredicted future close prices for " + stockSymbol + ":");
            System.out.println("Date\t\tPredicted Price");
            System.out.println("-------------------------");

            switch (modelType) {
                case 1: // Simple Linear Regression
                    predictLinearRegression(prices, baseDate, futureDays);
                    break;

                case 2: // Moving Average
                    predictMovingAverage(prices, baseDate, futureDays);
                    break;

                case 3: // Exponential Smoothing
                    predictExponentialSmoothing(prices, baseDate, futureDays);
                    break;

                default:
                    System.out.println("Invalid model type. Using linear regression.");
                    predictLinearRegression(prices, baseDate, futureDays);
                    break;
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving stock history for prediction: " + e.getMessage());
        }
    }

    private static void predictLinearRegression(List<Double> prices, LocalDate baseDate, int futureDays) {
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

        for (int i = 1; i <= futureDays; i++) {
            int futureX = n + i;
            double predictedPrice = slope * futureX + intercept;
            LocalDate futureDate = baseDate.plusDays(i);
            System.out.printf("%s\t$%.2f\n", futureDate, predictedPrice);
        }
    }

    private static void predictMovingAverage(List<Double> prices, LocalDate baseDate, int futureDays) {
        int windowSize = 10; // 10-day moving average

        // Calculate initial moving average
        List<Double> predictions = new ArrayList<>();
        double sum = 0;
        for (int i = prices.size() - windowSize; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        double currentAvg = sum / windowSize;

        // Generate predictions
        for (int i = 1; i <= futureDays; i++) {
            predictions.add(currentAvg);
            LocalDate futureDate = baseDate.plusDays(i);
            System.out.printf("%s\t$%.2f\n", futureDate, currentAvg);
        }
    }

    private static void predictExponentialSmoothing(List<Double> prices, LocalDate baseDate, int futureDays) {
        double alpha = 0.3; // Smoothing factor

        // Initialize with the last actual value
        double lastValue = prices.get(prices.size() - 1);

        // Generate predictions
        for (int i = 1; i <= futureDays; i++) {
            double prediction = alpha * lastValue + (1 - alpha) * prices.get(prices.size() - 2);
            lastValue = prediction; // Update the lastValue for the next iteration
            LocalDate futureDate = baseDate.plusDays(i);
            System.out.printf("%s\t$%.2f\n", futureDate, prediction);
        }
    }

    public static void handleStockListStatistics(int userId, String name) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter start date (YYYY-MM-DD): ");
        // Check date format
        String startDate = scanner.nextLine();
        if (!isValidDate(startDate)) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }
        // Append time to date string
        String startDateStr = startDate + " 00:00:00";
        Timestamp startTimestamp = Timestamp.valueOf(startDateStr);

        System.out.print("Enter end date (YYYY-MM-DD): ");
        // Check date format
        String endDate = scanner.nextLine();
        if (!isValidDate(endDate)) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }
        // Append time to date string
        String endDateStr = endDate + " 23:59:59";
        Timestamp endTimestamp = Timestamp.valueOf(endDateStr);

        // Check end date is after before date
        if (LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))) {
            System.out.println("End date must be after start date.");
            return;
        }

        displayStockListStatistics(userId, name, startTimestamp, endTimestamp);
    }

    public static void displayPortfolioStatistics(int userId, String portfolioName, Timestamp start, Timestamp end) {
        try {
            int portfolioId = getPortfolioId(userId, portfolioName);
            if (portfolioId == -1) {
                System.out.println("Portfolio not found.");
                return;
            }

            // Check if the portfolio contains any stocks
            String sqlCheck = "SELECT COUNT(*) AS count FROM PortfolioHolding WHERE portfolio_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {
                stmtCheck.setInt(1, portfolioId);
                ResultSet rsCheck = stmtCheck.executeQuery();
                if (rsCheck.next() && rsCheck.getInt("count") == 0) {
                    System.out.println("Portfolio is empty. No statistics available.");
                    return;
                }
            }

            // Display statistics for the portfolio
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

            // Check if the stock list contains any stocks
            String sqlCheck = "SELECT COUNT(*) AS count FROM StockListHolding WHERE list_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {
                stmtCheck.setInt(1, listId);
                ResultSet rsCheck = stmtCheck.executeQuery();
                if (rsCheck.next() && rsCheck.getInt("count") == 0) {
                    System.out.println("Stock list is empty. No statistics available.");
                    return;
                }
            }

            // Display statistics for the stock list
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
            String sql = "SELECT list_id FROM StockList WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, listName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int listId = rs.getInt("list_id");
                if (StockListManager.canAccessStockList(userId, listId)) {
                    return listId;
                }
            }
        }
        return -1;
    }

    private static void displayCOVs(int id, Timestamp start, Timestamp end, boolean isPortfolio)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String table = isPortfolio ? "PortfolioHolding" : "StockListHolding";
            String sql = "SELECT h.symbol, AVG(s.close) AS mean, STDDEV(s.close) AS stddev " +
                    "FROM " + table + " h " +
                    "JOIN StockHistory s ON h.symbol = s.symbol " +
                    "WHERE h." + (isPortfolio ? "portfolio_id" : "list_id") + " = ? AND s.timestamp BETWEEN ? AND ? " +
                    "GROUP BY h.symbol";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setTimestamp(2, start);
            stmt.setTimestamp(3, end);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nCoefficient of Variation (COV):");
            while (rs.next()) {
                String symbol = rs.getString("symbol");
                double mean = rs.getDouble("mean");
                double stddev = rs.getDouble("stddev");
                if (mean != 0) {
                    double cov = stddev / mean;
                    System.out.printf("Stock %s: COV = %.4f\n", symbol, cov);
                } else {
                    System.out.printf("Stock %s: COV = undefined (mean = 0)\n", symbol);
                }
            }
        }
    }

    private static void displayBetas(int id, Timestamp start, Timestamp end, boolean isPortfolio)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String table = isPortfolio ? "PortfolioHolding" : "StockListHolding";

            String sql = "WITH Returns AS (\n" +
                    "    SELECT\n" +
                    "        h.symbol,\n" +
                    "        sh.timestamp,\n" +
                    "        sh.close,\n" +
                    "        LAG(sh.close) OVER (PARTITION BY h.symbol ORDER BY sh.timestamp) AS prev_close\n" +
                    "    FROM " + table + " h\n" +
                    "    JOIN StockHistory sh ON h.symbol = sh.symbol\n" +
                    "    WHERE h." + (isPortfolio ? "portfolio_id" : "list_id") + " = ?\n" +
                    "        AND sh.timestamp BETWEEN ? AND ?\n" +
                    ")\n" +
                    ", StockReturns AS (\n" +
                    "    SELECT\n" +
                    "        symbol,\n" +
                    "        timestamp,\n" +
                    "        (close - prev_close) / prev_close AS return\n" +
                    "    FROM Returns\n" +
                    "    WHERE prev_close IS NOT NULL\n" +
                    ")\n" +
                    ", MarketReturns AS (\n" +
                    "    SELECT\n" +
                    "        timestamp,\n" +
                    "        AVG(return) AS market_return\n" +
                    "    FROM StockReturns\n" +
                    "    GROUP BY timestamp\n" +
                    ")\n" +
                    "SELECT\n" +
                    "    s.symbol,\n" +
                    "    COVAR_POP(s.return, m.market_return) / VAR_POP(m.market_return) AS beta\n" +
                    "FROM StockReturns s\n" +
                    "JOIN MarketReturns m ON s.timestamp = m.timestamp\n" +
                    "GROUP BY s.symbol\n" +
                    "ORDER BY s.symbol";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setTimestamp(2, start);
            stmt.setTimestamp(3, end);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nBeta Values (vs. Dynamic Market Average):");
            while (rs.next()) {
                String symbol = rs.getString("symbol");
                double beta = rs.getDouble("beta");
                System.out.printf("Stock %s: Beta = %.4f\n", symbol, beta);
            }
        }
    }

    private static void displayCovarianceMatrix(int id, Timestamp start, Timestamp end,
                                                boolean isPortfolio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String table = isPortfolio ? "PortfolioHolding" : "StockListHolding";
            String sqlStocks = "SELECT DISTINCT symbol FROM " + table + " WHERE "
                    + (isPortfolio ? "portfolio_id" : "list_id") + " = ?";
            PreparedStatement stmtStocks = conn.prepareStatement(sqlStocks);
            stmtStocks.setInt(1, id);
            ResultSet rsStocks = stmtStocks.executeQuery();

            Map<Integer, String> symbolMap = new HashMap<>();
            int index = 0;
            while (rsStocks.next()) {
                String symbol = rsStocks.getString("symbol");
                symbolMap.put(index++, symbol);
            }

            System.out.println("\nCovariance Matrix:");
            System.out.print("\t\t");
            for (int i = 0; i < symbolMap.size(); i++) {
                System.out.print(symbolMap.get(i) + "\t");
            }
            System.out.println();

            for (int i = 0; i < symbolMap.size(); i++) {
                System.out.print(symbolMap.get(i) + "\t");
                for (int j = 0; j < symbolMap.size(); j++) {
                    String sqlCov = "SELECT COVAR_POP(s1.close, s2.close) AS cov " +
                            "FROM StockHistory s1 " +
                            "JOIN StockHistory s2 ON s1.timestamp = s2.timestamp " +
                            "WHERE s1.symbol = ? AND s2.symbol = ? " +
                            "AND s1.timestamp BETWEEN ? AND ?";

                    PreparedStatement stmt = conn.prepareStatement(sqlCov);
                    stmt.setString(1, symbolMap.get(i));
                    stmt.setString(2, symbolMap.get(j));
                    stmt.setTimestamp(3, start);
                    stmt.setTimestamp(4, end);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        double cov = rs.getDouble("cov");
                        System.out.printf("%.4f\t", cov);
                    } else {
                        System.out.print("0.0000\t");
                    }
                }
                System.out.println();
            }
        }
    }

    public static void displayHistoricalPrices(String stockSymbol, Timestamp start, Timestamp end) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query that combines both historical and new stock data
            String sql = """
                    SELECT *
                    FROM StockHistory
                    WHERE symbol = ? AND timestamp BETWEEN ? AND ?
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, stockSymbol);
            stmt.setTimestamp(2, start);
            stmt.setTimestamp(3, end);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nHistorical Data for " + stockSymbol + ":");
            System.out.println("Date\t\t\tOpen\t\tHigh\t\tLow\t\tClose\t\tVolume");
            System.out.println("----------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.println(rs.getDate("timestamp") + "\t\t" + rs.getInt("open") +
                        "\t\t\t" + rs.getInt("high") + "\t\t\t" + rs.getInt("low") +
                        "\t\t\t" + rs.getInt("close") + "\t\t\t" + rs.getLong("volume"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving historical prices: " + e.getMessage());
        }
    }

    // Check date format
    public static boolean isValidDate(String date) {
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        return date.matches(regex);
    }
}