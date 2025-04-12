-- Remove this line in the end
DROP TABLE IF EXISTS
    Users,
    Portfolio,
    PortfolioHolding,
    Stats,
    Stock,
    StockHistory,
    StockList,
    SharedStockList,
    StockListHolding,
    Friend,
    Review
CASCADE;

-- Users table, to distinguish from the User keyword in SQL
CREATE TABLE Users (
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
    name VARCHAR(100) NOT NULL,
    cash_balance DECIMAL(15,2) NOT NULL CHECK (cash_balance >= 0),
    UNIQUE (user_id, name),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Stock table
CREATE TABLE Stock (
    symbol VARCHAR(10) PRIMARY KEY CHECK (symbol = UPPER(symbol)),
    company_name VARCHAR(100) NOT NULL
);

-- Insert stocks in StockHistory table to Stock table, from an external CSV file consisting of all stocks
COPY Stock (symbol, company_name)
FROM '/Users/gogo/C43ProjectTest/src/main/java/constituents.csv'
DELIMITER ','
CSV HEADER;

-- PortfolioHolding table to store the stocks in each portfolio
CREATE TABLE PortfolioHolding (
    portfolio_id INT,
    symbol VARCHAR(10),
    shares INT NOT NULL CHECK (shares > 0 AND shares % 1 = 0),
    PRIMARY KEY (portfolio_id, symbol),
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (symbol) REFERENCES Stock(symbol)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- StockList table to store the stock lists created by users
CREATE TABLE StockList (
    list_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    visibility VARCHAR(10) CHECK (visibility IN ('private', 'public', 'shared')),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE StockHistory (
    timestamp DATE,
    open DECIMAL(10, 2) CHECK (open >= 0),
    high DECIMAL(10, 2) CHECK (high >= 0),
    low DECIMAL(10, 2) CHECK (low >= 0),
    close DECIMAL(10, 2) CHECK (close >= 0),
    volume BIGINT CHECK (volume >= 0),
    symbol VARCHAR(10),
    PRIMARY KEY (symbol, timestamp)
);

-- Load all records from CSV file into the StockHistory table
COPY StockHistory (timestamp, open, high, low, close, volume, symbol)
FROM '/Users/gogo/C43ProjectTest/src/main/java/SP500History.csv'
DELIMITER ','
CSV HEADER;

-- SharedStockList table to store the shared stock lists
CREATE TABLE SharedStockList (
    list_id INT,
    shared_user_id INT,
    PRIMARY KEY (list_id, shared_user_id),
    FOREIGN KEY (list_id) REFERENCES StockList(list_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (shared_user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- StockListHolding table to store the stocks in each stock list
CREATE TABLE StockListHolding (
    list_id INT,
    symbol VARCHAR(10),
    shares INT NOT NULL CHECK (shares > 0 AND shares % 1 = 0),
    PRIMARY KEY (list_id, symbol),
    FOREIGN KEY (list_id) REFERENCES StockList(list_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (symbol) REFERENCES Stock(symbol)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Friend table
CREATE TABLE Friend (
    friendship_id SERIAL PRIMARY KEY,
    requester_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status VARCHAR(20) CHECK (status IN ('pending', 'accepted', 'denied')),
    last_request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CHECK (requester_id <> receiver_id)
);

-- Review table
CREATE TABLE Review (
    review_id SERIAL PRIMARY KEY,
    list_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (list_id) REFERENCES StockList(list_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Create a trigger to delete friendships after 5 minutes if status is 'denied'
CREATE OR REPLACE FUNCTION delete_denied_friendships()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM Friend
    WHERE ((requester_id = NEW.requester_id AND receiver_id = NEW.receiver_id)
           OR (requester_id = NEW.receiver_id AND receiver_id = NEW.requester_id))
      AND status = 'denied'
      AND last_request_time <= CURRENT_TIMESTAMP - INTERVAL '5 minutes';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_delete_denied_friendships
AFTER UPDATE ON Friend
FOR EACH ROW
WHEN (NEW.status = 'denied')
EXECUTE FUNCTION delete_denied_friendships();
