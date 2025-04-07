-- Questions:
-- 1. need to add constraints for the tables?
-- 2. need to add triggers, indexes for the tables?

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

-- Users table, to distinguish between User keyword in SQL
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

-- Stats table to store the statistics of portfolios
CREATE TABLE Stats (
    portfolio_id INT,
    list_id INT,
    cov NUMERIC(10, 4),
    beta NUMERIC(10, 4),
    matrix NUMERIC(10, 4),
    PRIMARY KEY (portfolio_id, list_id),
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (list_id) REFERENCES StockList(list_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Insert stocks in StockHistory table to Stock table (partial)
INSERT INTO Stock (symbol, company_name)
VALUES
    ('AAPL', 'Apple Inc.'),
    ('MSFT', 'Microsoft Corporation'),
    ('AMZN', 'Amazon.com Inc.'),
    ('NVDA', 'NVIDIA Corporation'),
    ('GOOGL', 'Alphabet Inc. Class A'),
    ('GOOG', 'Alphabet Inc. Class C'),
    ('META', 'Meta Platforms Inc.'),
    ('BRK.B', 'Berkshire Hathaway Inc. Class B'),
    ('TSLA', 'Tesla Inc.'),
    ('UNH', 'UnitedHealth Group Inc.'),
    ('LLY', 'Eli Lilly and Company'),
    ('JPM', 'JPMorgan Chase & Co.'),
    ('V', 'Visa Inc.'),
    ('PG', 'Procter & Gamble Company'),
    ('XOM', 'Exxon Mobil Corporation'),
    ('MA', 'Mastercard Incorporated'),
    ('AVGO', 'Broadcom Inc.'),
    ('HD', 'Home Depot Inc.'),
    ('CVX', 'Chevron Corporation'),
    ('COST', 'Costco Wholesale Corporation'),
    ('MRK', 'Merck & Co. Inc.'),
    ('ABBV', 'AbbVie Inc.'),
    ('PEP', 'PepsiCo Inc.'),
    ('KO', 'Coca-Cola Company'),
    ('ADBE', 'Adobe Inc.'),
    ('WMT', 'Walmart Inc.'),
    ('CRM', 'Salesforce Inc.'),
    ('BAC', 'Bank of America Corporation'),
    ('TMO', 'Thermo Fisher Scientific Inc.'),
    ('MCD', 'McDonald''s Corporation'),
    ('CSCO', 'Cisco Systems Inc.'),
    ('ACN', 'Accenture plc'),
    ('ABT', 'Abbott Laboratories'),
    ('DHR', 'Danaher Corporation'),
    ('NFLX', 'Netflix Inc.'),
    ('AMD', 'Advanced Micro Devices Inc.'),
    ('DIS', 'Walt Disney Company'),
    ('CMCSA', 'Comcast Corporation'),
    ('VZ', 'Verizon Communications Inc.'),
    ('IBM', 'International Business Machines Corporation'),
    ('INTC', 'Intel Corporation'),
    ('PFE', 'Pfizer Inc.'),
    ('NKE', 'Nike Inc.'),
    ('TXN', 'Texas Instruments Incorporated'),
    ('PM', 'Philip Morris International Inc.'),
    ('ORCL', 'Oracle Corporation'),
    ('RTX', 'Raytheon Technologies Corporation'),
    ('NEE', 'NextEra Energy Inc.'),
    ('JNJ', 'Johnson & Johnson'),
    ('HON', 'Honeywell International Inc.'),
    ('UPS', 'United Parcel Service Inc.'),
    ('T', 'AT&T Inc.'),
    ('CAT', 'Caterpillar Inc.'),
    ('WFC', 'Wells Fargo & Company'),
    ('LOW', 'Lowe''s Companies Inc.'),
    ('MS', 'Morgan Stanley'),
    ('SPGI', 'S&P Global Inc.'),
    ('QCOM', 'QUALCOMM Incorporated'),
    ('GS', 'Goldman Sachs Group Inc.'),
    ('MDT', 'Medtronic plc'),
    ('BMY', 'Bristol-Myers Squibb Company'),
    ('INTU', 'Intuit Inc.'),
    ('SCHW', 'Charles Schwab Corporation'),
    ('CVS', 'CVS Health Corporation'),
    ('AXP', 'American Express Company'),
    ('BLK', 'BlackRock Inc.'),
    ('AMGN', 'Amgen Inc.'),
    ('AMT', 'American Tower Corporation'),
    ('COP', 'ConocoPhillips'),
    ('DE', 'Deere & Company'),
    ('UNP', 'Union Pacific Corporation'),
    ('LIN', 'Linde plc'),
    ('C', 'Citigroup Inc.'),
    ('BA', 'Boeing Company'),
    ('GILD', 'Gilead Sciences Inc.'),
    ('ISRG', 'Intuitive Surgical Inc.'),
    ('ADI', 'Analog Devices Inc.'),
    ('SBUX', 'Starbucks Corporation'),
    ('MDLZ', 'Mondelez International Inc.'),
    ('BKNG', 'Booking Holdings Inc.'),
    ('AMAT', 'Applied Materials Inc.'),
    ('MMC', 'Marsh & McLennan Companies Inc.'),
    ('TJX', 'TJX Companies Inc.'),
    ('PLD', 'Prologis Inc.'),
    ('VRTX', 'Vertex Pharmaceuticals Incorporated'),
    ('SYK', 'Stryker Corporation'),
    ('MO', 'Altria Group Inc.'),
    ('GE', 'General Electric Company'),
    ('ZTS', 'Zoetis Inc.'),
    ('NOW', 'ServiceNow Inc.'),
    ('DUK', 'Duke Energy Corporation'),
    ('ADP', 'Automatic Data Processing Inc.'),
    ('SO', 'Southern Company'),
    ('BDX', 'Becton, Dickinson and Company'),
    ('LRCX', 'Lam Research Corporation'),
    ('ELV', 'Elevance Health Inc.'),
    ('REGN', 'Regeneron Pharmaceuticals Inc.'),
    ('SLB', 'Schlumberger Limited'),
    ('CB', 'Chubb Limited'),
    ('EOG', 'EOG Resources Inc.'),
    ('CME', 'CME Group Inc.'),
    ('CI', 'Cigna Group'),
    ('PGR', 'Progressive Corporation'),
    ('ITW', 'Illinois Tool Works Inc.'),
    ('TGT', 'Target Corporation'),
    ('MPC', 'Marathon Petroleum Corporation');

-- StockHistory table to store historical data of stocks
-- Question: do we need to store the source of data: original or user_added?
CREATE TABLE StockHistory (
    timestamp DATE,
    open DECIMAL(10, 2) CHECK (open >= 0),
    high DECIMAL(10, 2) CHECK (high >= 0),
    low DECIMAL(10, 2) CHECK (low >= 0),
    close DECIMAL(10, 2) CHECK (close >= 0),
    volume BIGINT CHECK (volume >= 0),
    symbol VARCHAR(10),
    PRIMARY KEY (symbol, timestamp)
-- Cannot use the same name as the column in Stock table because Stock table doesn't contain all symbols in StockHistory
--    FOREIGN KEY (symbol) REFERENCES Stock(symbol)
--        ON DELETE CASCADE
--        ON UPDATE CASCADE
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
