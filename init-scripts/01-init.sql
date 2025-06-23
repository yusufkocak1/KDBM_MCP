-- Airline Booking System - Database Tables

-- 1. Airports Table
CREATE TABLE airports (
                          airport_id INT PRIMARY KEY AUTO_INCREMENT,
                          airport_code VARCHAR(3) NOT NULL UNIQUE, -- IATA code (IST, ANK, IZM)
                          airport_name VARCHAR(100) NOT NULL,
                          city VARCHAR(50) NOT NULL,
                          country VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Airlines Table
CREATE TABLE airlines (
                          airline_id INT PRIMARY KEY AUTO_INCREMENT,
                          airline_code VARCHAR(3) NOT NULL UNIQUE, -- IATA code (TK, PC, XQ)
                          airline_name VARCHAR(100) NOT NULL,
                          country VARCHAR(50) NOT NULL,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Aircraft Table
CREATE TABLE aircraft (
                          aircraft_id INT PRIMARY KEY AUTO_INCREMENT,
                          aircraft_model VARCHAR(50) NOT NULL,
                          capacity INT NOT NULL,
                          airline_id INT NOT NULL,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (airline_id) REFERENCES airlines(airline_id)
);

-- 4. Flights Table
CREATE TABLE flights (
                         flight_id INT PRIMARY KEY AUTO_INCREMENT,
                         flight_number VARCHAR(10) NOT NULL UNIQUE,
                         airline_id INT NOT NULL,
                         aircraft_id INT NOT NULL,
                         departure_airport_id INT NOT NULL,
                         arrival_airport_id INT NOT NULL,
                         departure_time DATETIME NOT NULL,
                         arrival_time DATETIME NOT NULL,
                         price DECIMAL(10,2) NOT NULL,
                         available_seats INT NOT NULL,
                         status ENUM('scheduled', 'delayed', 'cancelled', 'completed') DEFAULT 'scheduled',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (airline_id) REFERENCES airlines(airline_id),
                         FOREIGN KEY (aircraft_id) REFERENCES aircraft(aircraft_id),
                         FOREIGN KEY (departure_airport_id) REFERENCES airports(airport_id),
                         FOREIGN KEY (arrival_airport_id) REFERENCES airports(airport_id)
);

-- 5. Customers Table
CREATE TABLE customers (
                           customer_id INT PRIMARY KEY AUTO_INCREMENT,
                           first_name VARCHAR(50) NOT NULL,
                           last_name VARCHAR(50) NOT NULL,
                           email VARCHAR(100) NOT NULL UNIQUE,
                           phone VARCHAR(15),
                           national_id VARCHAR(11) UNIQUE,
                           birth_date DATE,
                           gender ENUM('M', 'F'),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Bookings Table
CREATE TABLE bookings (
                          booking_id INT PRIMARY KEY AUTO_INCREMENT,
                          booking_code VARCHAR(10) NOT NULL UNIQUE,
                          customer_id INT NOT NULL,
                          flight_id INT NOT NULL,
                          seat_number VARCHAR(5),
                          booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          status ENUM('confirmed', 'pending', 'cancelled') DEFAULT 'pending',
                          total_price DECIMAL(10,2) NOT NULL,
                          payment_status ENUM('unpaid', 'paid', 'refunded') DEFAULT 'unpaid',
                          FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                          FOREIGN KEY (flight_id) REFERENCES flights(flight_id)
);

-- 7. Payments Table
CREATE TABLE payments (
                          payment_id INT PRIMARY KEY AUTO_INCREMENT,
                          booking_id INT NOT NULL,
                          payment_method ENUM('credit_card', 'debit_card', 'bank_transfer', 'cash') NOT NULL,
                          payment_amount DECIMAL(10,2) NOT NULL,
                          payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          transaction_status ENUM('successful', 'failed', 'pending') DEFAULT 'pending',
                          transaction_number VARCHAR(50),
                          FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- SAMPLE DATA INSERTIONS

-- Airports
INSERT INTO airports (airport_code, airport_name, city, country) VALUES
                                                                     ('IST', 'Istanbul Airport', 'Istanbul', 'Turkey'),
                                                                     ('SAW', 'Sabiha Gokcen Airport', 'Istanbul', 'Turkey'),
                                                                     ('ANK', 'Esenboga Airport', 'Ankara', 'Turkey'),
                                                                     ('IZM', 'Adnan Menderes Airport', 'Izmir', 'Turkey'),
                                                                     ('AYT', 'Antalya Airport', 'Antalya', 'Turkey'),
                                                                     ('ESB', 'Esenboga Airport', 'Ankara', 'Turkey');

-- Airlines
INSERT INTO airlines (airline_code, airline_name, country) VALUES
                                                               ('TK', 'Turkish Airlines', 'Turkey'),
                                                               ('PC', 'Pegasus Airlines', 'Turkey'),
                                                               ('XQ', 'SunExpress', 'Turkey'),
                                                               ('AJ', 'AnadoluJet', 'Turkey');

-- Aircraft
INSERT INTO aircraft (aircraft_model, capacity, airline_id) VALUES
                                                                ('Boeing 737', 180, 1),
                                                                ('Airbus A320', 174, 1),
                                                                ('Boeing 737-800', 189, 2),
                                                                ('Airbus A320neo', 180, 2),
                                                                ('Boeing 737-800', 189, 3),
                                                                ('Boeing 737-700', 149, 4);

-- Flights
INSERT INTO flights (flight_number, airline_id, aircraft_id, departure_airport_id, arrival_airport_id,
                     departure_time, arrival_time, price, available_seats) VALUES
                                                                               ('TK101', 1, 1, 1, 3, '2025-06-25 08:00:00', '2025-06-25 09:15:00', 450.00, 150),
                                                                               ('TK102', 1, 2, 3, 1, '2025-06-25 10:30:00', '2025-06-25 11:45:00', 475.00, 140),
                                                                               ('PC201', 2, 3, 1, 4, '2025-06-25 14:00:00', '2025-06-25 15:20:00', 320.00, 160),
                                                                               ('PC202', 2, 4, 4, 1, '2025-06-25 16:45:00', '2025-06-25 18:05:00', 340.00, 155),
                                                                               ('XQ301', 3, 5, 2, 5, '2025-06-25 12:15:00', '2025-06-25 13:30:00', 280.00, 170),
                                                                               ('AJ401', 4, 6, 1, 2, '2025-06-25 19:30:00', '2025-06-25 20:45:00', 250.00, 120);

-- Customers
INSERT INTO customers (first_name, last_name, email, phone, national_id, birth_date, gender) VALUES
                                                                                                 ('Ahmet', 'Yilmaz', 'ahmet.yilmaz@email.com', '05551234567', '12345678901', '1985-03-15', 'M'),
                                                                                                 ('Ayse', 'Demir', 'ayse.demir@email.com', '05559876543', '12345678902', '1990-07-22', 'F'),
                                                                                                 ('Mehmet', 'Kaya', 'mehmet.kaya@email.com', '05555555555', '12345678903', '1988-11-10', 'M'),
                                                                                                 ('Fatma', 'Sahin', 'fatma.sahin@email.com', '05554444444', '12345678904', '1992-01-05', 'F'),
                                                                                                 ('Ali', 'Celik', 'ali.celik@email.com', '05553333333', '12345678905', '1987-09-18', 'M');

-- Bookings
INSERT INTO bookings (booking_code, customer_id, flight_id, seat_number,
                      status, total_price, payment_status) VALUES
                                                               ('BK001', 1, 1, '12A', 'confirmed', 450.00, 'paid'),
                                                               ('BK002', 2, 3, '15B', 'confirmed', 320.00, 'paid'),
                                                               ('BK003', 3, 2, '8C', 'pending', 475.00, 'unpaid'),
                                                               ('BK004', 4, 5, '22A', 'confirmed', 280.00, 'paid'),
                                                               ('BK005', 5, 4, '5D', 'confirmed', 340.00, 'paid');

-- Payments
INSERT INTO payments (booking_id, payment_method, payment_amount, transaction_status, transaction_number) VALUES
                                                                                                              (1, 'credit_card', 450.00, 'successful', 'TXN001234567'),
                                                                                                              (2, 'debit_card', 320.00, 'successful', 'TXN001234568'),
                                                                                                              (4, 'credit_card', 280.00, 'successful', 'TXN001234569'),
                                                                                                              (5, 'bank_transfer', 340.00, 'successful', 'TXN001234570');

-- USEFUL QUERIES

-- 1. List all flights for a specific date
-- SELECT f.flight_number, a.airline_name, ap1.city as departure, ap2.city as arrival,
--        f.departure_time, f.price, f.available_seats
-- FROM flights f
-- JOIN airlines a ON f.airline_id = a.airline_id
-- JOIN airports ap1 ON f.departure_airport_id = ap1.airport_id
-- JOIN airports ap2 ON f.arrival_airport_id = ap2.airport_id
-- WHERE DATE(f.departure_time) = '2025-06-25';

-- 2. Customer booking details
-- SELECT c.first_name, c.last_name, b.booking_code, f.flight_number,
--        b.seat_number, b.total_price, b.payment_status
-- FROM customers c
-- JOIN bookings b ON c.customer_id = b.customer_id
-- JOIN flights f ON b.flight_id = f.flight_id
-- WHERE c.customer_id = 1;

-- 3. Daily revenue by airline
-- SELECT a.airline_name, DATE(f.departure_time) as flight_date,
--        SUM(b.total_price) as daily_revenue
-- FROM airlines a
-- JOIN flights f ON a.airline_id = f.airline_id
-- JOIN bookings b ON f.flight_id = b.flight_id
-- WHERE b.payment_status = 'paid'
-- GROUP BY a.airline_name, DATE(f.departure_time);

-- 4. Available seats by flight
-- SELECT f.flight_number, a.airline_name, f.departure_time, f.available_seats
-- FROM flights f
-- JOIN airlines a ON f.airline_id = a.airline_id
-- WHERE f.status = 'scheduled' AND f.available_seats > 0
-- ORDER BY f.departure_time;

-- 5. Customer booking history
-- SELECT c.first_name, c.last_name, COUNT(b.booking_id) as total_bookings,
--        SUM(b.total_price) as total_spent
-- FROM customers c
-- LEFT JOIN bookings b ON c.customer_id = b.customer_id
-- GROUP BY c.customer_id, c.first_name, c.last_name;