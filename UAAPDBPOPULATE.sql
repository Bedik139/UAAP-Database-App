USE UAAPDBSQL;

-- ======= INSERT SAMPLE TEAMS =======
INSERT INTO team (team_name, seasons_played, standing_wins, standing_losses, total_games_played)
VALUES 
('DLSU Green Archers', 25, 18, 7, 25),
('ADMU Blue Eagles', 25, 20, 5, 25);

-- ======= INSERT SAMPLE PLAYERS =======
INSERT INTO player (team_id, player_first_name, player_last_name, player_number, age, position, weight, height)
VALUES
(1, 'Justine', 'Torres', 7, 21, 'Guard', 70.5, 178.2),
(1, 'Kyle', 'Reyes', 23, 22, 'Forward', 82.0, 185.3),
(2, 'Miguel', 'Cruz', 15, 21, 'Guard', 72.1, 177.5),
(2, 'Josh', 'Santos', 4, 23, 'Center', 90.0, 195.0);

-- ======= INSERT SAMPLE EVENT =======
INSERT INTO event (event_name, sport, match_date, event_time_start, event_time_end, venue_address)
VALUES 
('UAAP Season 87 - Basketball Elimination Round', 'Basketball', '2025-11-05', '15:00:00', '17:30:00', 'Mall of Asia Arena');

-- ======= INSERT SAMPLE MATCH =======
INSERT INTO `match` (event_id, match_type, match_date, match_time_start, match_time_end)
VALUES 
(1, 'Elimination_Round', '2025-11-05', '15:00:00', '17:30:00');

-- ======= LINK TEAMS TO MATCH =======
INSERT INTO match_team (match_id, team_id, is_home, team_score)
VALUES
(1, 1, TRUE, 85),
(1, 2, FALSE, 79);

-- ======= QUARTER SCORES (for Basketball) =======
INSERT INTO match_quarter_score (match_id, team_id, quarter_no, points)
VALUES
(1, 1, 1, 20),
(1, 1, 2, 18),
(1, 1, 3, 22),
(1, 1, 4, 25),
(1, 2, 1, 19),
(1, 2, 2, 20),
(1, 2, 3, 18),
(1, 2, 4, 22);

-- ======= INSERT SAMPLE TICKETS =======
INSERT INTO ticket (default_price, price, ticket_status)
VALUES
(500.00, 600.00, 'Active'),
(500.00, NULL, 'Active');

-- ======= INSERT SAMPLE SEATS =======
INSERT INTO seat (seat_type)
VALUES ('VIP'), ('Regular');

-- ======= INSERT SAMPLE CUSTOMERS =======
INSERT INTO customer (customer_first_name, customer_last_name, phone_number, email, organization, registration_date, preferred_sport, customer_status, payment_method)
VALUES
('Ben', 'Cruz', '09171234567', 'bencruz@example.com', 'CSB', '2025-10-30', 'Basketball', 'Active', 'GCash'),
('Jude', 'Tamayo', '09179876543', 'judetamayo@example.com', 'ADMU', '2025-10-30', 'Basketball', 'Active', 'Credit Card');

-- ======= SELL SAMPLE TICKETS =======
INSERT INTO seat_and_ticket (seat_id, event_id, customer_id, price_sold, ticket_id, match_id)
VALUES
(1, 1, 1, 600.00, 1, 1),
(2, 1, 2, 500.00, 2, 1);

-- ======= INSERT EVENT PERSONNEL =======
INSERT INTO event_personnel (personnel_first_name, personnel_last_name, availability_status, role, affiliation, contact_no, event_id, match_id)
VALUES
('Anna', 'Lopez', 'Available', 'Usher', 'DLSU', '09178889999', 1, 1),
('Mark', 'Garcia', 'Available', 'Referee', 'UAAP', '09179990000', 1, 1);

-- ======= TEST QUERIES =======
-- 🏀 Show match result
SELECT e.event_name, t.team_name, mt.team_score
FROM match_team mt
JOIN team t ON mt.team_id = t.team_id
JOIN `match` m ON mt.match_id = m.match_id
JOIN event e ON m.event_id = e.event_id
WHERE m.match_id = 1;

-- 🧾 Show customer ticket sales
SELECT c.customer_first_name, c.customer_last_name, s.seat_type, sat.price_sold, e.event_name
FROM seat_and_ticket sat
JOIN customer c ON sat.customer_id = c.customer_id
JOIN seat s ON sat.seat_id = s.seat_id
JOIN event e ON sat.event_id = e.event_id;

-- 👥 Show personnel assigned to event
SELECT personnel_first_name, personnel_last_name, role, affiliation
FROM event_personnel
WHERE event_id = 1;
