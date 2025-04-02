-- Questions:
-- 1. need to add constraints for the tables?
-- 2. need to add triggers, indexes for the tables?

-- Remove this line in the end
DROP TABLE IF EXISTS
    User,
    Portfolio,
    PortfolioHolding,
    Stats,
    Stock,
    StockHistory,
    StockList,
    StockListHolding,
    Friend,
    Review;

-- User table
CREATE TABLE User (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL CHECK (LENGTH(username) BETWEEN 3 AND 50),
    email VARCHAR(100) UNIQUE NOT NULL CHECK (email LIKE '%@%.%' AND LENGTH(email) BETWEEN 5 AND 100),
    password VARCHAR(255) NOT NULL CHECK (LENGTH(password) BETWEEN 6 AND 255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Portfolio table
CREATE TABLE Portfolio (
    portfolio_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    cash_balance DECIMAL(15,2) DEFAULT 0.00 CHECK (cash_balance >= 0),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- PortfolioHolding table to store the stocks in each portfolio
CREATE TABLE PortfolioHolding (
    portfolio_id INT,
    symbol VARCHAR(10),
    shares INT NOT NULL CHECK (shares > 0 AND shares % 1 = 0),
    PRIMARY KEY (portfolio_id, symbol),
    FOREIGN KEY (portfolio_id) REFERENCES Portfolios(portfolio_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (symbol) REFERENCES Stocks(symbol)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
);

-- Stats table to store the statistics of portfolios
CREATE TABLE Stats (
    portfolio_id INT,
    cov NUMERIC(10, 4),
    beta NUMERIC(10, 4),
    matrix NUMERIC(10, 4),
    PRIMARY KEY (portfolio_id),
    FOREIGN KEY (portfolio_id) REFERENCES Portfolios(portfolio_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
);

-- Stock table
CREATE TABLE Stock (
    symbol VARCHAR(10) PRIMARY KEY CHECK (symbol = UPPER(symbol)),
    company_name VARCHAR(100) NOT NULL
);

-- StockHistory table to store historical data of stocks
-- Question: do we need to store the source of data: original or user_added?
CREATE TABLE StockHistory (
    timestamp DATE,
    open_price DECIMAL(10, 2) CHECK (open_price >= 0),
    high_price DECIMAL(10, 2) CHECK (high_price >= 0),
    low_price DECIMAL(10, 2) CHECK (low_price >= 0),
    close_price DECIMAL(10, 2) CHECK (close_price >= 0),
    volume BIGINT CHECK (volume >= 0),
    symbol VARCHAR(10),
    PRIMARY KEY (symbol, timestamp),
    FOREIGN KEY (symbol) REFERENCES Stocks(symbol)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Load all records from CSV file into the StockHistory table
COPY StockHistory (timestamp, open_price, high_price, low_price, close_price, volume, symbol)
FROM 'SP500History.csv'
DELIMITER ','
CSV HEADER;

-- StockList table to store the stock lists created by users
CREATE TABLE StockList (
    list_id SERIAL PRIMARY KEY,
    user_id INT,
    name VARCHAR(100) NOT NULL,
    visibility VARCHAR(10) CHECK (visibility IN ('private', 'public')),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- StockListHolding table to store the stocks in each stock list
CREATE TABLE StockListHolding (
    list_id INT,
    symbol VARCHAR(10),
    shares INT NOT NULL CHECK (shares > 0 AND shares % 1 = 0),
    PRIMARY KEY (list_id, symbol),
    FOREIGN KEY (list_id) REFERENCES StockLists(list_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (symbol) REFERENCES Stocks(symbol)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Friend table
CREATE TABLE Friend (
    friendship_id SERIAL PRIMARY KEY,
    requester_id INT,
    receiver_id INT,
    status VARCHAR(20) CHECK (status IN ('pending', 'accepted', 'denied')),
    last_request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CHECK (requester_id <> receiver_id),
);

-- Review table
CREATE TABLE Review (
    review_id SERIAL PRIMARY KEY,
    list_id INT,
    user_id INT,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (list_id) REFERENCES StockLists(list_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
