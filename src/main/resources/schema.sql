-- customers table (NO AUTO_INCREMENT)
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    pincode VARCHAR(10)
);

-- drivers table
CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    cash_in_hand DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- batches table
CREATE TABLE IF NOT EXISTS batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cluster_label VARCHAR(50),
    driver_id BIGINT,
    total_weight DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'PENDING',
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL
);

-- orders table (NO AUTO_INCREMENT)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    batch_id BIGINT,
    driver_id BIGINT,
    weight_kg DECIMAL(10,2) NOT NULL,
    distance_km DECIMAL(10,2),
    amount DECIMAL(10,2) NOT NULL,
    payment_type ENUM('PREPAID', 'CASH', 'UPI', 'UNSPECIFIED') NOT NULL,
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    delivery_status ENUM('PENDING', 'ARRIVED', 'DELIVERED') DEFAULT 'PENDING',
    address TEXT NOT NULL,
    pincode VARCHAR(10),
    lat DOUBLE,
    lon DOUBLE,
    otp_status ENUM('EXPIRED', 'SENT', 'VERIFIED') DEFAULT "NULL",
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE SET NULL,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL
);


-- payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    method ENUM('COD', 'UPI', 'PREPAID') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- otp_verification table
CREATE TABLE IF NOT EXISTS otp_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    otp VARCHAR(10) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    status ENUM('SENT', 'VERIFIED', 'EXPIRED') DEFAULT 'SENT',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- cash_ledger table
CREATE TABLE IF NOT EXISTS cash_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    order_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    type ENUM('COLLECT', 'SETTLE') NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);
