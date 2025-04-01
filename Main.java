import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void registerUser(UserManager userManager, Scanner scanner) {
        System.out.println("Register a new user");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Name: ");
        String name = scanner.nextLine();
        userManager.createUser(username, password, email, name);
    }

    public static int loginUser(UserManager userManager, Scanner scanner) {
        System.out.println("Login an existing user");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        return userManager.login(username, password);
    }

    public static void handleViewPortfolio(
            PortfolioManager portfolioManager, int userId, Scanner scanner) {
        boolean stay = true;

        while (stay) {
            System.out.println("Enter:\n" +
                    "  portfolio id to check a specific portfolio" +
                    "  0 to return to last page");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice != 0) {
                portfolioManager.calculateCovarianceMatrix(
                        userId, choice, scanner);
                portfolioManager.viewStocksInList(
                        userId, choice, scanner);
            } else {
                System.out.println("Returning to last page.");
                stay = false;
            }
        }
    }

    /* Presents the portfolio page. */
    public static void handlePortfolioOperations(
            PortfolioManager portfolioManager, HoldManager holdManager,
            Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("Enter:\n" +
                    "  1 to view portfolios\n" +
                    "  2 to create portfolios\n" +
                    "  3 to update portfolio\n" +
                    "  4 to delete portfolio\n" +
                    "  5 to add Stock_Holding\n" +
                    "  6 to sell Stock_Holding\n" +
                    "  0 to return to last page");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.println("Returning to last page.");
                    stay = false;
                    break;
                case 1:
                    System.out.println("Viewing portfolios.");
                    System.out.println("Enter:\n" +
                            "  portfolio id to check a specific portfolio" +
                            "  0 to return to last page");
                    int viewPortfolioId = scanner.nextInt();
                    scanner.nextLine();
                    if (viewPortfolioId != 0) {
                        portfolioManager.calculateCovarianceMatrix(
                                viewPortfolioId);
                        portfolioManager.viewStocksInList(
                                viewPortfolioId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 2:
                    System.out.println("Creating portfolios.");
                    System.out.print("Enter portfolio name: ");
                    String portfolioName = scanner.nextLine();
                    System.out.print("Enter cash balance: ");
                    double cashBalance = scanner.nextDouble();
                    portfolioManager.createPortfolio(
                            portfolioName, cashBalance);
                    break;
                case 3:
                    System.out.println("Updating portfolios.");
                    System.out.println("Enter:\n" +
                            "  portfolio id to update a specific portfolio" +
                            "  0 to return to last page");
                    int updatePortfolioId = scanner.nextInt();
                    scanner.nextLine();
                    if (updatePortfolioId != 0) {
                        System.out.print("Enter Portfolio Name: ");
                        portfolioName = scanner.nextLine();
                        System.out.print("Enter cash balance: ");
                        cashBalance = scanner.nextDouble();
                        portfolioManager.updatePortfolio(
                                updatePortfolioId, portfolioName, cashBalance);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 4:
                    System.out.println("Deleting portfolios.");
                    System.out.println("Enter:\n" +
                            "  portfolio id to delete a specific portfolio" +
                            "  0 to return to last page");
                    int deletePortfolioId = scanner.nextInt();
                    if (deletePortfolioId != 0) {
                        portfolioManager.deletePortfolio(deletePortfolioId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 5:
                    System.out.println("Add Stock_Holding");
                    System.out.println("Enter:\n" +
                            "  portfolio id to specify a portfolio" +
                            "  0 to return to last page");
                    int addPortfolioId = scanner.nextInt();
                    if (addPortfolioId != 0) {
                        System.out.print("Enter stock id: ");
                        int stockId = scanner.nextInt();
                        System.out.print("Enter number of shares: ");
                        int shares = scanner.nextInt();
                        holdManager.createHolding(
                                addPortfolioId, stockId, shares);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 6:
                    System.out.println("Sell Stock_Holding");
                    System.out.println("Enter:\n" +
                            "  portfolio id to specify a portfolio" +
                            "  0 to return to last page");
                    int sellPortfolioId = scanner.nextInt();
                    if (sellPortfolioId != 0) {
                        System.out.print("Enter stock id: ");
                        int stockId = scanner.nextInt();
                        System.out.print("Number of Shares to Sell: ");
                        int shares = scanner.nextInt();
                        holdManager.sellHolding(
                                sellPortfolioId, stockId, shares);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                default:
                    System.out.println("Invalid operation.");
                    break;
            }
        }
    }

    /* Presents the stock page. */
    public static void handleStockListOperations(
            StockManager stockManager, ListManager listManager,
            ListHoldManager listHoldManager, Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("Enter:\n" +
                    "  1 to view stock lists\n" +
                    "  2 to view public stock lists\n" +
                    "  3 to create stock lists\n" +
                    "  4 to update stock lists\n" +
                    "  5 to share stock lists\n" +
                    "  6 to add stocks to lists\n" +
                    "  0 to return to last page");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.println("Returning to last page.");
                    stay = false;
                    break;
                case 1:
                    System.out.println("Viewing stock lists.");
                    System.out.println("Enter:\n" +
                            "  list id to check a specific list" +
                            "  0 to return to last page");
                    int viewStockListId = scanner.nextInt();
                    scanner.nextLine();
                    if (viewStockListId != 0) {
                        listHoldManager.calculateCovarianceMatrix(
                                viewStockListId);
                        listHoldManager.viewStocksInList(
                                viewStockListId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 2:
                    System.out.println("Viewing public stock lists.");
                    System.out.println("Enter:\n" +
                            "  stock list id to view a specific stock list" +
                            "  0 to return to last page");
                    int viewPublicStockListId = scanner.nextInt();
                    scanner.nextLine();
                    if (viewPublicStockListId != 0) {
                        listHoldManager.calculateCovarianceMatrix(
                                viewPublicStockListId);
                        listHoldManager.viewStocksInList(
                                viewPublicStockListId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 3:
                    System.out.println("Creating stock lists.");
                    System.out.print("Enter Stock List Name: ");
                    String createListName = scanner.nextLine();
                    System.out.print("Enter 1 for public, 0 for private): ");
                    boolean createIsPublic = scanner.nextInt() == 1;
                    listManager.createList(createListName, createIsPublic);
                    break;
                case 4:
                    System.out.println("Update stock lists.");
                    System.out.println("Enter:\n" +
                            "  stock list id to update a specific stock list" +
                            "  0 to return to last page");
                    int updateStockListId = scanner.nextInt();
                    scanner.nextLine();
                    if (updateStockListId != 0) {
                        System.out.print("Enter Stock List Name: ");
                        String updatelistName = scanner.nextLine();
                        System.out.print("Enter 1 for public, 0 for private): ");
                        boolean updateIsPublic = scanner.nextInt() == 1;
                        listManager.updateList(updatelistName, updateIsPublic);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 5:
                    System.out.println("Sharing stock lists.");
                    System.out.println("Enter:\n" +
                            "  list id to share a list" +
                            "  0 to return to last page");
                    int shareListId = scanner.nextInt();
                    if (shareListId != 0) {
                        System.out.print("Enter friend's user id: ");
                        int friendUserId = scanner.nextInt();
                        listManager.shareStockListWithFriend(
                                shareListId, friendUserId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 6:
                    System.out.println("Adding stocks to lists.");
                    System.out.println("Enter:\n" +
                            "  list id for adding stock" +
                            "  0 to return to last page");
                    int addListId = scanner.nextInt();
                    if (addListId != 0) {
                        System.out.print("Enter stock id: ");
                        int addStockId = scanner.nextInt();
                        System.out.print("Enter number of Shares: ");
                        int shares = scanner.nextInt();
                        listHoldManager.createListHold(
                                addListId, addStockId, shares);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                default:
                    System.out.println("Invalid operation.");
                    break;
            }
        }
    }

    /* Presents the friend page. */
    public static void handleFriendOperations(
            FriendManager friendManager, Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("Enter:\n" +
                    "  1 to send friend request\n" +
                    "  2 to accept friend request\n" +
                    "  3 to reject friend request\n" +
                    "  4 to view incoming requests\n" +
                    "  5 to view outgoing requests\n" +
                    "  6 to cancel friend request\n" +
                    "  7 to delete friend" +
                    "  8 to view friends" +
                    "  0 to return to last page");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.println("Returning to last page.");
                    stay = false;
                    break;
                case 1:
                    System.out.println("Sending friend request.");
                    System.out.println("Enter:\n" +
                            "  user id to send friend request" +
                            "  0 to return to last page");
                    int sendUserId = scanner.nextInt();
                    scanner.nextLine();
                    if (sendUserId != 0) {
                        friendManager.sendFriendRequest(sendUserId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 2:
                    System.out.println("Accepting friend request.");
                    System.out.println("Enter:\n" +
                            "  user id to accept friend request" +
                            "  0 to return to last page");
                    int acceptUserId = scanner.nextInt();
                    scanner.nextLine();
                    if (acceptUserId != 0) {
                        friendManager.acceptFriendRequest(acceptUserId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 3:
                    System.out.println("Rejecting friend request.");
                    System.out.println("Enter:\n" +
                            "  user id to reject friend request" +
                            "  0 to return to last page");
                    int rejectUserId = scanner.nextInt();
                    scanner.nextLine();
                    if (rejectUserId != 0) {
                        friendManager.rejectFriendRequest(rejectUserId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 4:
                    System.out.println("Viewing incoming requests.");
                    friendManager.viewIncomingRequest();
                    break;
                case 5:
                    System.out.println("Viewing outgoing requests.");
                    friendManager.viewOutgoingRequest();
                    break;
                case 6:
                    System.out.println("Cancelling friend request.");
                    System.out.println("Enter:\n" +
                            "  user id to cancel friend request" +
                            "  0 to return to last page");
                    int cancelUserId = scanner.nextInt();
                    scanner.nextLine();
                    if (cancelUserId != 0) {
                        friendManager.cancelFriendRequest(cancelUserId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 7:
                    System.out.println("Deleting friend.");
                    System.out.println("Enter:\n" +
                            "  user id to delete friend" +
                            "  0 to return to last page");
                    int deleteUserId = scanner.nextInt();
                    scanner.nextLine();
                    if (deleteUserId != 0) {
                        friendManager.deleteFriend(deleteUserId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 8:
                    System.out.println("Viewing friends.");
                    friendManager.viewFriends();
                    break;
                default:
                    System.out.println("Invalid operation.");
                    break;
            }
        }
    }

    /* Presents the review page. */
    public static void handleReviewOperations(
            ReviewManager reviewManager, Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("Enter:\n" +
                    "  1 to view stock's reviews\n" +
                    "  2 to create review\n" +
                    "  3 to update review\n" +
                    "  4 to delete review\n" +
                    "  0 to return to last page");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.println("Returning to last page.");
                    stay = false;
                    break;
                case 1:
                    System.out.println("Viewing stock's reviews.");
                    System.out.println("Enter:\n" +
                            "  stock id to check reviews" +
                            "  0 to return to last page");
                    int viewReviewStockId = scanner.nextInt();
                    scanner.nextLine();
                    if (viewReviewStockId != 0) {
                        reviewManager.viewReviewByStockId(viewReviewStockId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 2:
                    System.out.println("Creating review.");
                    System.out.println("Enter:\n" +
                            "  stock list id to create review" +
                            "  0 to return to last page");
                    int createReviewlistId = scanner.nextInt();
                    scanner.nextLine();
                    if (createReviewlistId != 0) {
                        System.out.print("Enter review text: ");
                        String reviewText = scanner.nextLine();
                        reviewManager.createReview(
                                createReviewlistId, reviewText);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 3:
                    System.out.println("Updating review.");
                    System.out.println("Enter:\n" +
                            "  stock list id to update reviews" +
                            "  0 to return to last page");
                    int updateReviewId = scanner.nextInt();
                    scanner.nextLine();
                    if (updateReviewId != 0) {
                        System.out.print("Enter new review text: ");
                        String reviewText = scanner.nextLine();
                        reviewManager.updateReview(
                                updateReviewId, reviewText);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                case 4:
                    System.out.println("Deleting review.");
                    System.out.println("Enter:\n" +
                            "  stock list id to delete reviews" +
                            "  0 to return to last page");
                    int deleteReviewId = scanner.nextInt();
                    scanner.nextLine();
                    if (deleteReviewId != 0) {
                        reviewManager.deleteReview(deleteReviewId);
                    } else {
                        System.out.println("Returning to last page.");
                    }
                    break;
                default:
                    System.out.println("Invalid operation.");
                    break;
            }
        }
    }

    /* Presents the main page after login. */
    public static void showOperations(PortfolioManager portfolioManager,
            ListManager listManager, StockManager stockManager,
            HoldManager holdManager, ListHoldManager listHoldManager,
            FriendManager friendManager, ReviewManager reviewManager,
            DailyManager dailyManager, Scanner scanner) {
        boolean stay = true;
        while (stay) {
            System.out.println("Enter:\n" +
                    "  1 to access portfolios\n" +
                    "  2 to manage stock lists\n" +
                    "  3 to manage friends\n" +
                    "  4 to request stock reviews\n" +
                    "  5 to view stock analysis and prediction\n" +
                    "  0 to logout");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.println("Logging out.");
                    stay = false;
                    break;
                case 1:
                    System.out.println("Accessing portfolios.");
                    handlePortfolioOperations(
                            portfolioManager, holdManager, scanner);
                    break;
                case 2:
                    System.out.println("Managing stock lists.");
                    handleStockListOperations(
                            stockManager, listHoldManager, scanner);
                    break;
                case 3:
                    System.out.println("Managing friends.");
                    handleFriendOperations(friendManager, scanner);
                    break;
                case 4:
                    System.out.println("Requesting stock reviews.");
                    handleReviewOperations(reviewManager, scanner);
                    break;
                case 5:
                    System.out.println("Viewing stock analysis and prediction.");
                    System.out.print("Enter stock ID: ");
                    int stockId = scanner.nextInt();
                    holdManager.checkStockPerformance(stockId);
                    break;
                default:
                    System.out.println("Invalid operation.");
                    break;
            }
        }
    }

    public static void Main(String[] args) {
        /* Connect to database. */
        Connection conn = null;
        // Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/mydb",
                    "postgres",
                    "postgres123");
            System.out.println("Opened database successfully");
            // stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Initialize managers. */
        Scanner scanner = new Scanner(System.in);
        UserManager userManager = new UserManager(conn, scanner);
        PortfolioManager portfolioManager;
        ListManager listManager;
        StockManager stockManager;
        HoldManager holdManager;
        ListHoldManager listHoldManager;
        FriendManager friendManager;
        ReviewManager reviewManager;
        DailyManager dailyManager;

        /* Set up command line interaction. */
        boolean running = true;
        while (running) {
            System.out.println("Enter\n" +
                    "  1 to login\n" +
                    "  2 to register\n" +
                    "  0 to quit: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 0:
                    running = false;
                    break;
                case 1:
                    int userId = loginUser(userManager, scanner);
                    if (userId != -1) {
                        System.out.println("Logged in with User ID: " + userId);
                        portfolioManager = new PortfolioManager(
                                conn, scanner, userId);
                        listManager = new listManager(
                                conn, scanner, userId);
                        stockManager = new StockManager(
                                conn, scanner, userId);
                        holdManager = new HoldManager(
                                conn, scanner, userId);
                        listHoldManager = new listHoldManager(
                                conn, scanner, userId);
                        friendManager = new FriendManager(
                                conn, scanner, userId);
                        reviewManager = new ReviewManager(
                                conn, scanner, userId);
                        dailyManager = new DailyManager(
                                conn, scanner, userId);
                        showOperations(portfolioManager, listManager,
                                stockManager, holdManager, listHoldManager,
                                friendManager, reviewManager, dailyManager,
                                scanner);
                    } else {
                        System.out.println("Invalid username or password.");
                    }
                    break;
                case 2:
                    registerUser(userManager, scanner);
                    break;
                default:
                    System.out.println("Invalid operation.");
                    break;
            }
        }
        scanner.close();
    }
}
