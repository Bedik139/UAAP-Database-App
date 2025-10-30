USE UAAPDBSQL;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE seat_and_ticket;
TRUNCATE TABLE event_personnel;
TRUNCATE TABLE match_quarter_score;
TRUNCATE TABLE match_set_score;
TRUNCATE TABLE match_team;
TRUNCATE TABLE `match`;
TRUNCATE TABLE player;
TRUNCATE TABLE team;
TRUNCATE TABLE customer;
TRUNCATE TABLE ticket;
TRUNCATE TABLE seat;
TRUNCATE TABLE event;
SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE team AUTO_INCREMENT = 101;
ALTER TABLE player AUTO_INCREMENT = 5001;
ALTER TABLE event AUTO_INCREMENT = 2001;
ALTER TABLE `match` AUTO_INCREMENT = 6001;
ALTER TABLE ticket AUTO_INCREMENT = 9001;
ALTER TABLE seat AUTO_INCREMENT = 3001;
ALTER TABLE customer AUTO_INCREMENT = 7001;
ALTER TABLE event_personnel AUTO_INCREMENT = 12001;

-- ======= TEAMS =======
INSERT INTO team (team_name, seasons_played, standing_wins, standing_losses, total_games_played) VALUES
('DLSU Green Archers', 26, 18, 8, 26),
('ADMU Blue Eagles', 26, 20, 6, 26),
('NU Bulldogs', 18, 15, 11, 26),
('UP Fighting Maroons', 22, 17, 9, 26),
('UST Growling Tigers', 24, 12, 14, 26),
('FEU Tamaraws', 25, 14, 12, 26);

-- ======= PLAYERS =======
INSERT INTO player (team_id, player_first_name, player_last_name, player_number, age, position, weight, height, individual_score) VALUES
(101, 'Kevin', 'Escandor', 7, 21, 'Point Guard', 73.5, 178.4, 215),
(101, 'Rico', 'Valdez', 12, 22, 'Small Forward', 82.0, 188.1, 189),
(102, 'Anton', 'Rivera', 5, 23, 'Shooting Guard', 76.2, 183.2, 231),
(102, 'Lance', 'Espiritu', 14, 21, 'Center', 94.8, 201.0, 205),
(103, 'Harvey', 'Go', 11, 22, 'Power Forward', 85.5, 195.5, 178),
(103, 'Jerome', 'Santos', 3, 20, 'Wing', 79.4, 189.2, 164),
(104, 'CJ', 'Andrada', 9, 22, 'Guard', 71.6, 180.3, 222),
(104, 'Jolo', 'Manalili', 2, 21, 'Forward', 80.3, 190.7, 199),
(105, 'Paolo', 'Domingo', 25, 24, 'Center', 98.1, 204.5, 153),
(105, 'Franz', 'Aquino', 8, 20, 'Guard', 69.8, 176.3, 167),
(106, 'Kyle', 'Toledo', 10, 22, 'Forward', 83.7, 192.4, 208),
(106, 'Marc', 'Yap', 4, 23, 'Forward', 87.9, 194.0, 184);

-- ======= EVENTS =======
INSERT INTO event (event_name, sport, match_date, event_time_start, event_time_end, venue_address) VALUES
('UAAP Season 87 Basketball - Opening Day', 'Basketball', '2025-10-12', '14:30:00', '18:00:00', 'Mall of Asia Arena'),
('UAAP Season 87 Volleyball - Rivalry Weekend', 'Volleyball', '2025-10-19', '16:00:00', '19:30:00', 'Smart Araneta Coliseum'),
('UAAP Season 87 Basketball - Finals Game 1', 'Basketball', '2025-12-05', '18:00:00', '21:00:00', 'PhilSports Arena');

-- ======= MATCHES =======
INSERT INTO `match` (event_id, match_type, match_date, match_time_start, match_time_end) VALUES
(2001, 'Elimination_Round', '2025-10-12', '14:30:00', '16:45:00'),
(2001, 'Elimination_Round', '2025-10-12', '17:00:00', '19:15:00'),
(2002, 'Semifinals', '2025-10-19', '16:00:00', '18:30:00'),
(2002, 'Semifinals', '2025-10-19', '18:30:00', '19:30:00'),
(2003, 'Finals', '2025-12-05', '18:00:00', '21:00:00');

-- ======= MATCH TEAMS =======
INSERT INTO match_team (match_id, team_id, is_home, team_score) VALUES
(6001, 101, TRUE, 89),
(6001, 102, FALSE, 82),
(6002, 103, TRUE, 76),
(6002, 104, FALSE, 78),
(6003, 105, TRUE, 3),
(6003, 106, FALSE, 1),
(6004, 104, TRUE, 3),
(6004, 101, FALSE, 2),
(6005, 102, TRUE, 88),
(6005, 104, FALSE, 84);

-- ======= QUARTER SCORES (Basketball Matches) =======
INSERT INTO match_quarter_score (match_id, team_id, quarter_no, points) VALUES
(6001, 101, 1, 20), (6001, 101, 2, 24), (6001, 101, 3, 22), (6001, 101, 4, 23),
(6001, 102, 1, 18), (6001, 102, 2, 20), (6001, 102, 3, 22), (6001, 102, 4, 22),
(6002, 103, 1, 19), (6002, 103, 2, 18), (6002, 103, 3, 20), (6002, 103, 4, 19),
(6002, 104, 1, 21), (6002, 104, 2, 20), (6002, 104, 3, 19), (6002, 104, 4, 18),
(6005, 102, 1, 23), (6005, 102, 2, 19), (6005, 102, 3, 22), (6005, 102, 4, 24),
(6005, 104, 1, 18), (6005, 104, 2, 22), (6005, 104, 3, 21), (6005, 104, 4, 23);

-- ======= SET SCORES (Volleyball Matches) =======
INSERT INTO match_set_score (match_id, team_id, set_no, points) VALUES
(6003, 105, 1, 25), (6003, 105, 2, 22), (6003, 105, 3, 26),
(6003, 106, 1, 19), (6003, 106, 2, 25), (6003, 106, 3, 24),
(6004, 104, 1, 25), (6004, 104, 2, 22), (6004, 104, 3, 25), (6004, 104, 4, 28),
(6004, 101, 1, 20), (6004, 101, 2, 25), (6004, 101, 3, 21), (6004, 101, 4, 26);

-- ======= TICKETS =======
INSERT INTO ticket (default_price, price, ticket_status) VALUES
(600.00, 750.00, 'Active'),
(600.00, NULL, 'Active'),
(850.00, 950.00, 'Active'),
(850.00, NULL, 'Active');

-- ======= SEATS =======
INSERT INTO seat (seat_type) VALUES
('Courtside'), ('Lower Box'), ('Upper Box'), ('General Admission');

-- ======= CUSTOMERS =======
INSERT INTO customer (customer_first_name, customer_last_name, phone_number, email, organization, registration_date, preferred_sport, customer_status, payment_method) VALUES
('Alyssa', 'Lopez', '09171234567', 'aly.lopez@gmail.com', 'DLSU', '2025-09-25', 'Volleyball', 'Active', 'Credit Card'),
('Miguel', 'Bautista', '09179874562', 'migs.bautista@outlook.com', 'UP', '2025-09-28', 'Basketball', 'Active', 'GCash'),
('Jasmine', 'Sy', '09173214321', 'jas.sy@yahoo.com', 'ADMU', '2025-10-01', 'Volleyball', 'Active', 'PayMaya'),
('Rafael', 'Navarro', '09175556677', 'rafa.navarro@gmail.com', 'NU', '2025-10-03', 'Basketball', 'Inactive', 'Bank Transfer');

-- ======= SEAT AND TICKET SALES =======
INSERT INTO seat_and_ticket (seat_id, event_id, customer_id, price_sold, ticket_id, match_id) VALUES
(3001, 2001, 7001, 750.00, 9001, 6001),
(3002, 2001, 7002, 600.00, 9002, 6002),
(3001, 2002, 7003, 950.00, 9003, 6003),
(3003, 2003, 7004, 850.00, 9004, 6005);

-- ======= EVENT PERSONNEL =======
INSERT INTO event_personnel (personnel_first_name, personnel_last_name, availability_status, role, affiliation, contact_no, event_id, match_id) VALUES
('Isabel', 'Santiago', 'Available', 'Usher', 'DLSU Student Council', '09171110001', 2001, 6001),
('Noel', 'Villanueva', 'Assigned', 'Ticketing Agent', 'UAAP Secretariat', '09171110002', 2001, 6001),
('Patricia', 'Cortez', 'Assigned', 'Security', 'MOA Arena Security', '09171110003', 2001, 6001),
('Andre', 'Ramos', 'Standby', 'Referee', 'UAAP Officiating Pool', '09171110004', 2001, 6001),
('Carla', 'Reyes', 'Assigned', 'Scorekeeper', 'UAAP Stat Crew', '09171110005', 2001, 6001),
('Miguel', 'Tan', 'Available', 'Stat Crew', 'UAAP Stat Crew', '09171110006', 2001, 6002),
('Jared', 'Ocampo', 'Assigned', 'Usher', 'ADMU Volunteers', '09171110007', 2001, 6002),
('Lara', 'Lim', 'Assigned', 'Security', 'MOA Arena Security', '09171110008', 2001, 6002),
('Bianca', 'Gutierrez', 'Available', 'Cheerleader', 'DLSU Animo Squad', '09171110009', 2001, 6002),
('Ken', 'Santos', 'Assigned', 'Band Member', 'Green Archers Pep Band', '09171110010', 2001, 6002),
('Samantha', 'Uy', 'Assigned', 'Usher', 'Araneta Event Services', '09171110011', 2002, 6003),
('Louie', 'De Vera', 'Assigned', 'Referee', 'UAAP Officiating Pool', '09171110012', 2002, 6003),
('Hazel', 'Mendoza', 'Available', 'Scorekeeper', 'UAAP Stat Crew', '09171110013', 2002, 6003),
('Rico', 'Fernando', 'Standby', 'Security', 'Araneta Event Services', '09171110014', 2002, 6003),
('Grace', 'Pacquing', 'Assigned', 'Dance Troupe', 'NU Pep Squad', '09171110015', 2002, 6003),
('Paula', 'Ignacio', 'Assigned', 'Usher', 'Araneta Event Services', '09171110016', 2002, 6004),
('Jonas', 'Villareal', 'Assigned', 'Ticketing Agent', 'UAAP Secretariat', '09171110017', 2002, 6004),
('Therese', 'Co', 'Available', 'Host', 'UAAP Media Team', '09171110018', 2002, 6004),
('Allan', 'Soriano', 'Assigned', 'Referee', 'UAAP Officiating Pool', '09171110019', 2002, 6004),
('Fiona', 'Manansala', 'Standby', 'Band Member', 'FEU Drummers', '09171110020', 2002, 6004),
('Marcus', 'Limjoco', 'Assigned', 'Security', 'PhilSports Security', '09171110021', 2003, 6005),
('Regine', 'Quintos', 'Assigned', 'Usher', 'PhilSports Security', '09171110022', 2003, 6005),
('Darren', 'Salazar', 'Assigned', 'Referee', 'UAAP Officiating Pool', '09171110023', 2003, 6005),
('Ivy', 'Chan', 'Standby', 'Stat Crew', 'UAAP Stat Crew', '09171110024', 2003, 6005),
('Paolo', 'Guerrero', 'Assigned', 'Host', 'UAAP Media Team', '09171110025', 2003, 6005),
('Mika', 'Lazaro', 'Available', 'Cheerleader', 'Blue Babble Battalion', '09171110026', 2003, 6005),
('Troy', 'Valencia', 'Assigned', 'Security', 'PhilSports Security', '09171110027', 2003, 6005),
('Clarisse', 'Uy', 'Assigned', 'Halftime Entertainment', 'UAAP Performance Pool', '09171110028', 2003, 6005),
('Xavier', 'Flores', 'Standby', 'Ticketing Agent', 'UAAP Secretariat', '09171110029', 2003, NULL),
('Nina', 'Soriano', 'Standby', 'Usher', 'UAAP Volunteers', '09171110030', 2003, NULL);

-- ======= SAMPLE REPORTS =======
-- 1. Match results overview
SELECT e.event_name, m.match_id, t.team_name, mt.team_score
FROM match_team mt
JOIN team t ON mt.team_id = t.team_id
JOIN `match` m ON mt.match_id = m.match_id
JOIN event e ON m.event_id = e.event_id
ORDER BY m.match_id, mt.team_score DESC;

-- 2. Ticket sales summary
SELECT e.event_name, s.seat_type, COUNT(*) AS seats_sold, SUM(price_sold) AS revenue
FROM seat_and_ticket sat
JOIN seat s ON sat.seat_id = s.seat_id
JOIN event e ON sat.event_id = e.event_id
GROUP BY e.event_name, s.seat_type;

-- 3. Personnel deployment matrix
SELECT ep.personnel_id, ep.personnel_first_name, ep.personnel_last_name, ep.role, ep.availability_status,
       e.event_name, m.match_id
FROM event_personnel ep
LEFT JOIN event e ON ep.event_id = e.event_id
LEFT JOIN `match` m ON ep.match_id = m.match_id
ORDER BY ep.personnel_id;
