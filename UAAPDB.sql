DROP DATABASE IF EXISTS UAAPDBSQL;
CREATE DATABASE UAAPDBSQL;
USE UAAPDBSQL;

CREATE TABLE event (
  event_id          INT           NOT NULL AUTO_INCREMENT,
  event_name        VARCHAR(120)  NOT NULL,
  sport             ENUM('Basketball','Volleyball') NOT NULL,
  match_date        DATE          NOT NULL,
  event_time_start  TIME          NOT NULL,
  event_time_end    TIME          NOT NULL,
  venue_address     ENUM('Mall of Asia Arena','Smart Araneta Coliseum','PhilSports Arena','Ynares Center','Filoil EcoOil Centre')  NOT NULL,
  venue_capacity    INT           NOT NULL,
  event_status      ENUM('Scheduled','Active','Completed','Cancelled') NOT NULL DEFAULT 'Scheduled',
  PRIMARY KEY (event_id)
);

CREATE TABLE team (
  team_id             INT          NOT NULL AUTO_INCREMENT,
  team_name           ENUM(
                          'De La Salle Green Archers',
                          'Ateneo Blue Eagles',
                          'UP Fighting Maroons',
                          'UST Growling Tigers',
                          'Far Eastern University Tamaraws',
                          'University of the East Red Warriors',
                          'National University Bulldogs',
                          'Adamson Soaring Falcons'
                        ) NOT NULL,
  seasons_played      INT          NOT NULL DEFAULT 0,
  standing_wins       INT          NOT NULL DEFAULT 0,
  standing_losses     INT          NOT NULL DEFAULT 0,
  total_games_played  INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (team_id),
  UNIQUE KEY uq_team_name (team_name)
);

CREATE TABLE player (
  player_id         INT          NOT NULL AUTO_INCREMENT,
  team_id           INT          NOT NULL,
  player_first_name VARCHAR(60)  NOT NULL,
  player_last_name  VARCHAR(60)  NOT NULL,
  player_number     INT          NOT NULL,
  player_sport      ENUM('Basketball','Volleyball') NOT NULL,
  age               INT,
  position          VARCHAR(30),
  weight            DECIMAL(5,2),
  height            DECIMAL(5,2),
  individual_score  INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (player_id),
  KEY idx_player_team (team_id),
  KEY idx_player_sport (player_sport),
  CONSTRAINT fk_player_team
    FOREIGN KEY (team_id) REFERENCES team(team_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT uq_player_number_per_team UNIQUE (team_id,player_number)
);

CREATE TABLE `match` (
  match_id         INT          NOT NULL AUTO_INCREMENT,
  event_id         INT          NOT NULL,
  match_type       ENUM('Elimination_Round','Semifinals','Finals') NOT NULL,
  match_date       DATE         NOT NULL,
  match_time_start TIME         NOT NULL,
  match_time_end   TIME         NOT NULL,
  status           ENUM('Scheduled','Completed','Cancelled','Postponed') NOT NULL DEFAULT 'Scheduled',
  score_summary    VARCHAR(255),
  PRIMARY KEY (match_id),
  KEY idx_match_event (event_id),
  CONSTRAINT fk_match_event
    FOREIGN KEY (event_id) REFERENCES event(event_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE match_team (
  match_id    INT       NOT NULL,
  team_id     INT       NOT NULL,
  is_home     BOOLEAN   NOT NULL,
  team_score  INT       NOT NULL DEFAULT 0,
  PRIMARY KEY (match_id,team_id),
  KEY idx_mt_team (team_id),
  CONSTRAINT fk_mt_match
    FOREIGN KEY (match_id) REFERENCES `match`(match_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_mt_team
    FOREIGN KEY (team_id) REFERENCES team(team_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_mt_is_home CHECK (is_home IN (0,1))
);

CREATE TABLE match_quarter_score (
  match_id    INT NOT NULL,
  team_id     INT NOT NULL,
  quarter_no  INT NOT NULL,
  quarter_points INT NOT NULL,
  PRIMARY KEY (match_id,team_id,quarter_no),
  KEY idx_mqs_team (team_id),
  CONSTRAINT fk_mqs_match
    FOREIGN KEY (match_id) REFERENCES `match`(match_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_mqs_team
    FOREIGN KEY (team_id) REFERENCES team(team_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ck_quarter_range CHECK (quarter_no BETWEEN 1 AND 4)
);

CREATE TABLE match_set_score (
  match_id   INT NOT NULL,
  team_id    INT NOT NULL,
  set_no     INT NOT NULL,
  set_points INT NOT NULL,
  PRIMARY KEY (match_id,team_id,set_no),
  KEY idx_mss_team (team_id),
  CONSTRAINT fk_mss_match
    FOREIGN KEY (match_id) REFERENCES `match`(match_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_mss_team
    FOREIGN KEY (team_id) REFERENCES team(team_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ck_set_range CHECK (set_no BETWEEN 1 AND 5)
);

CREATE TABLE ticket (
  ticket_id     INT            NOT NULL AUTO_INCREMENT,
  default_price DECIMAL(10,2)  NOT NULL,
  price         DECIMAL(10,2)  NULL,
  ticket_status ENUM('Active','Inactive','Archived') NOT NULL DEFAULT 'Active',
  PRIMARY KEY (ticket_id)
);

CREATE TABLE customer (
  customer_id          INT          NOT NULL AUTO_INCREMENT,
  customer_first_name  VARCHAR(60)  NOT NULL,
  customer_last_name   VARCHAR(60)  NOT NULL,
  phone_number         VARCHAR(30),
  email                VARCHAR(120),
  organization         VARCHAR(120),
  registration_date    DATE         NOT NULL DEFAULT (CURRENT_DATE),
  preferred_team       ENUM(
                          'De La Salle Green Archers',
                          'Ateneo Blue Eagles',
                          'UP Fighting Maroons',
                          'UST Growling Tigers',
                          'Far Eastern University Tamaraws',
                          'University of the East Red Warriors',
                          'National University Bulldogs',
                          'Adamson Soaring Falcons'
                        ) DEFAULT NULL,
  customer_status      VARCHAR(20),
  payment_method       VARCHAR(30),
  CONSTRAINT ck_customer_contact CHECK (
          (phone_number IS NOT NULL AND phone_number <> '')
       OR (email IS NOT NULL AND email <> '')
  ),
  PRIMARY KEY (customer_id),
  UNIQUE KEY uq_customer_email (email)
);

CREATE TABLE seat (
  seat_id        INT          NOT NULL AUTO_INCREMENT,
  seat_type      VARCHAR(30)  NOT NULL,
  venue_address  ENUM('Mall of Asia Arena','Smart Araneta Coliseum','PhilSports Arena','Ynares Center','Filoil EcoOil Centre') NOT NULL,
  seat_status    ENUM('Available','OnHold','Sold') NOT NULL DEFAULT 'Available',
  ticket_id      INT          NOT NULL,
  PRIMARY KEY (seat_id),
  KEY idx_seat_venue (venue_address),
  KEY idx_seat_ticket (ticket_id),
  CONSTRAINT fk_seat_ticket
    FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE seat_and_ticket (
  seat_and_ticket_rec_id INT           NOT NULL AUTO_INCREMENT,
  seat_id                INT           NOT NULL,
  event_id               INT           NOT NULL,
  customer_id            INT           NOT NULL,
  sale_datetime          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  quantity               INT           NOT NULL DEFAULT 1,
  unit_price             DECIMAL(10,2) NOT NULL,
  total_price            DECIMAL(12,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
  ticket_id              INT           NOT NULL,
  match_id               INT           NOT NULL,
  sale_status            ENUM('Sold','Refunded') NOT NULL DEFAULT 'Sold',
  PRIMARY KEY (seat_and_ticket_rec_id),
  UNIQUE KEY uq_event_seat (event_id, seat_id),
  KEY idx_sat_seat (seat_id),
  KEY idx_sat_event (event_id),
  KEY idx_sat_customer (customer_id),
  KEY idx_sat_ticket (ticket_id),
  KEY idx_sat_match (match_id),
  CONSTRAINT fk_sat_seat
    FOREIGN KEY (seat_id) REFERENCES seat(seat_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sat_event
    FOREIGN KEY (event_id) REFERENCES event(event_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_sat_customer
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sat_ticket
    FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sat_match
    FOREIGN KEY (match_id) REFERENCES `match`(match_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE ticket_refund_audit (
  audit_id                INT           NOT NULL AUTO_INCREMENT,
  seat_and_ticket_rec_id  INT           NOT NULL,
  refund_amount           DECIMAL(10,2) NOT NULL,
  refund_datetime         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reason                  VARCHAR(255),
  processed_by            VARCHAR(120),
  PRIMARY KEY (audit_id),
  KEY idx_refund_sale (seat_and_ticket_rec_id),
  KEY idx_refund_datetime (refund_datetime),
  CONSTRAINT fk_refund_sale
    FOREIGN KEY (seat_and_ticket_rec_id) REFERENCES seat_and_ticket(seat_and_ticket_rec_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE event_personnel (
  personnel_id          INT          NOT NULL AUTO_INCREMENT,
  personnel_first_name  VARCHAR(60)  NOT NULL,
  personnel_last_name   VARCHAR(60)  NOT NULL,
  availability_status   VARCHAR(30),
  role                  ENUM(
                            'Usher','Ticketing Agent','Security',
                            'Referee','Scorekeeper','Stat Crew',
                            'Band Member','Cheerleader','Dance Troupe',
                            'Singer','Host','Halftime Entertainment'
                           ) NOT NULL,
  affiliation           VARCHAR(120) NOT NULL,
  contact_no            VARCHAR(40),
  event_id              INT          NOT NULL,
  match_id              INT          NOT NULL,
  PRIMARY KEY (personnel_id),
  KEY idx_ep_event (event_id),
  KEY idx_ep_match (match_id),
  CONSTRAINT fk_ep_event
    FOREIGN KEY (event_id) REFERENCES event(event_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ep_match
    FOREIGN KEY (match_id) REFERENCES `match`(match_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);

-- ========== DATA POPULATION ==========

-- Teams
INSERT INTO team (team_name, seasons_played, standing_wins, standing_losses, total_games_played)
VALUES
('De La Salle Green Archers', 15, 12, 3, 15),
('Ateneo Blue Eagles', 15, 11, 4, 15),
('UP Fighting Maroons', 15, 10, 5, 15),
('UST Growling Tigers', 15, 7, 8, 15),
('Far Eastern University Tamaraws', 15, 8, 7, 15),
('University of the East Red Warriors', 15, 4, 11, 15),
('National University Bulldogs', 15, 9, 6, 15),
('Adamson Soaring Falcons', 15, 5, 10, 15);

-- Events
INSERT INTO event (event_name, sport, match_date, event_time_start, event_time_end, venue_address, venue_capacity, event_status)
VALUES
('UAAP S87 Basketball - Opening Day', 'Basketball', '2025-11-15', '14:00:00', '18:30:00', 'Mall of Asia Arena', 15000, 'Completed'),
('UAAP S87 Basketball - Round 1 Week 2', 'Basketball', '2025-11-20', '16:00:00', '18:00:00', 'Smart Araneta Coliseum', 16000, 'Completed'),
('UAAP S87 Basketball - Round 1 Week 3', 'Basketball', '2025-11-25', '18:00:00', '20:00:00', 'PhilSports Arena', 8000, 'Completed'),
('UAAP S87 Basketball - Semifinals', 'Basketball', '2025-12-15', '18:00:00', '20:30:00', 'Smart Araneta Coliseum', 16000, 'Scheduled'),
('UAAP S87 Basketball - Finals', 'Basketball', '2026-01-10', '18:00:00', '21:00:00', 'Mall of Asia Arena', 15000, 'Scheduled'),
('UAAP S87 Volleyball - Opening Day', 'Volleyball', '2025-11-18', '14:00:00', '17:00:00', 'Mall of Asia Arena', 15000, 'Completed'),
('UAAP S87 Volleyball - Round 1 Week 2', 'Volleyball', '2025-11-22', '14:00:00', '17:00:00', 'Smart Araneta Coliseum', 16000, 'Completed'),
('UAAP S87 Volleyball - Semifinals', 'Volleyball', '2025-12-20', '14:00:00', '18:00:00', 'Mall of Asia Arena', 15000, 'Scheduled');

-- Players (Basketball - 5 per team, Volleyball - 6 per team)
INSERT INTO player (team_id, player_first_name, player_last_name, player_number, player_sport, age, position, weight, height, individual_score)
VALUES
-- DLSU Basketball (5 players)
(1, 'Kevin', 'Quiambao', 5, 'Basketball', 22, 'SF', 88.0, 1.98, 285),
(1, 'Evan', 'Nelle', 8, 'Basketball', 23, 'PG', 78.0, 1.85, 245),
(1, 'Michael', 'Phillips', 15, 'Basketball', 24, 'PF', 92.0, 2.01, 198),
(1, 'Joshua', 'David', 12, 'Basketball', 21, 'SG', 75.0, 1.83, 167),
(1, 'Mark', 'Nonoy', 20, 'Basketball', 22, 'C', 95.0, 2.00, 156),
-- Ateneo Basketball (5 players)
(2, 'Forthsky', 'Padrigao', 11, 'Basketball', 22, 'SG', 75.0, 1.82, 267),
(2, 'Kai', 'Ballungay', 4, 'Basketball', 24, 'PF', 92.0, 2.03, 243),
(2, 'Chris', 'Koon', 33, 'Basketball', 23, 'C', 98.0, 2.05, 189),
(2, 'Sean', 'Quitevis', 7, 'Basketball', 21, 'PG', 74.0, 1.81, 156),
(2, 'Jared', 'Bahay', 14, 'Basketball', 22, 'SF', 81.0, 1.90, 178),
-- UP Basketball (5 players)
(3, 'Carl', 'Tamayo', 33, 'Basketball', 23, 'PF', 95.0, 2.05, 256),
(3, 'Malick', 'Diouf', 11, 'Basketball', 22, 'C', 102.0, 2.08, 234),
(3, 'Francis', 'Lopez', 7, 'Basketball', 24, 'SG', 77.0, 1.85, 198),
(3, 'Terrence', 'Fortea', 8, 'Basketball', 21, 'PG', 73.0, 1.80, 167),
(3, 'Aldous', 'Torculas', 10, 'Basketball', 23, 'SF', 85.0, 1.92, 145),
-- UST Basketball (5 players)
(4, 'Soulemane', 'Chabi Yo', 15, 'Basketball', 25, 'C', 100.0, 2.01, 223),
(4, 'Nic', 'Cabanero', 12, 'Basketball', 23, 'PG', 76.0, 1.83, 189),
(4, 'Christian', 'Manaytay', 7, 'Basketball', 22, 'SF', 88.0, 1.95, 167),
(4, 'Royce', 'Mantua', 24, 'Basketball', 21, 'SG', 75.0, 1.81, 145),
(4, 'Paul', 'Manalang', 21, 'Basketball', 22, 'PF', 90.0, 1.98, 134),
-- FEU Basketball (5 players)
(5, 'Royce', 'Alforque', 11, 'Basketball', 23, 'PG', 77.0, 1.84, 234),
(5, 'Mo', 'Konateh', 33, 'Basketball', 24, 'C', 99.0, 2.04, 212),
(5, 'Veejay', 'Pre', 5, 'Basketball', 22, 'SF', 87.0, 1.96, 178),
(5, 'Jilson', 'Bautista', 7, 'Basketball', 21, 'SG', 74.0, 1.82, 156),
(5, 'Patrick', 'Sleat', 4, 'Basketball', 23, 'PF', 91.0, 2.00, 145),
-- UE Basketball (5 players)
(6, 'Precious', 'Momowei', 14, 'Basketball', 24, 'C', 101.0, 2.06, 198),
(6, 'Ethan', 'Galang', 7, 'Basketball', 22, 'SG', 76.0, 1.83, 167),
(6, 'Kyle', 'Paranada', 11, 'Basketball', 21, 'SF', 86.0, 1.95, 145),
(6, 'Wello', 'Lingolingo', 5, 'Basketball', 23, 'PG', 73.0, 1.80, 123),
(6, 'Rey', 'Remogat', 9, 'Basketball', 22, 'PF', 89.0, 1.97, 112),
-- NU Basketball (5 players)
(7, 'Steve', 'Nash', 11, 'Basketball', 23, 'PG', 78.0, 1.86, 245),
(7, 'Jake', 'Figueroa', 22, 'Basketball', 24, 'PF', 93.0, 2.00, 223),
(7, 'PJ', 'Palacielo', 7, 'Basketball', 22, 'C', 97.0, 2.03, 189),
(7, 'Kenshin', 'Padilla', 15, 'Basketball', 21, 'SG', 75.0, 1.82, 167),
(7, 'Jake', 'Palaganas', 3, 'Basketball', 22, 'SF', 84.0, 1.93, 156),
-- Adamson Basketball (5 players)
(8, 'Matty', 'Erolon', 7, 'Basketball', 22, 'PG', 77.0, 1.84, 212),
(8, 'Matthew', 'Montebon', 23, 'Basketball', 23, 'SF', 90.0, 1.98, 189),
(8, 'Didat', 'Hanapi', 11, 'Basketball', 24, 'C', 96.0, 2.02, 167),
(8, 'Joshua', 'Barcelona', 5, 'Basketball', 21, 'SG', 74.0, 1.81, 145),
(8, 'Joem', 'Sabandal', 13, 'Basketball', 23, 'PF', 88.0, 1.96, 134),
-- DLSU Volleyball (6 players)
(1, 'Angel', 'Canino', 1, 'Volleyball', 21, 'S', 60.0, 1.75, 145),
(1, 'Leila', 'Cruz', 3, 'Volleyball', 22, 'OH', 65.0, 1.80, 178),
(1, 'Julia', 'Coronel', 9, 'Volleyball', 20, 'MB', 68.0, 1.85, 134),
(1, 'Thea', 'Gagate', 14, 'Volleyball', 21, 'OH', 64.0, 1.78, 123),
(1, 'Baby Love', 'Barbon', 7, 'Volleyball', 22, 'MB', 67.0, 1.84, 112),
(1, 'Justine', 'Jazareno', 4, 'Volleyball', 20, 'L', 58.0, 1.68, 98),
-- Ateneo Volleyball (6 players)
(2, 'Jhoana', 'Maraguinot', 2, 'Volleyball', 23, 'S', 61.0, 1.76, 156),
(2, 'Faith', 'Nisperos', 6, 'Volleyball', 22, 'OH', 64.0, 1.79, 189),
(2, 'Vanie', 'Gandler', 16, 'Volleyball', 21, 'MB', 69.0, 1.86, 145),
(2, 'Lyann', 'DeGuzman', 9, 'Volleyball', 22, 'OH', 63.0, 1.77, 134),
(2, 'AC', 'Miner', 11, 'Volleyball', 21, 'MB', 68.0, 1.83, 123),
(2, 'Dani', 'Ravena', 5, 'Volleyball', 20, 'L', 59.0, 1.70, 106),
-- UP Volleyball (6 players)
(3, 'Alyssa', 'Bertolano', 12, 'Volleyball', 22, 'S', 62.0, 1.74, 134),
(3, 'Jewel', 'Lai', 18, 'Volleyball', 21, 'OH', 66.0, 1.81, 178),
(3, 'Stephanie', 'Bustrillo', 21, 'Volleyball', 23, 'MB', 70.0, 1.87, 156),
(3, 'Nina', 'Ytang', 7, 'Volleyball', 22, 'OH', 64.0, 1.79, 145),
(3, 'Cassie', 'Lim', 14, 'Volleyball', 21, 'MB', 67.0, 1.84, 123),
(3, 'Marist', 'Layug', 4, 'Volleyball', 20, 'L', 57.0, 1.67, 98),
-- UST Volleyball (6 players)
(4, 'Cassie', 'Carballo', 4, 'Volleyball', 22, 'S', 60.0, 1.73, 123),
(4, 'Eya', 'Laure', 19, 'Volleyball', 23, 'OH', 65.0, 1.82, 189),
(4, 'Xyza', 'Gula', 13, 'Volleyball', 21, 'MB', 68.0, 1.84, 134),
(4, 'Imee', 'Hernandez', 8, 'Volleyball', 22, 'OH', 63.0, 1.78, 145),
(4, 'Angge', 'Poyos', 11, 'Volleyball', 21, 'MB', 67.0, 1.85, 112),
(4, 'KC', 'De Luna', 2, 'Volleyball', 20, 'L', 58.0, 1.69, 95),
-- FEU Volleyball (6 players)
(5, 'Kyle', 'Negrito', 3, 'Volleyball', 21, 'S', 61.0, 1.75, 145),
(5, 'Jean', 'Asis', 6, 'Volleyball', 22, 'OH', 64.0, 1.78, 167),
(5, 'Ivana', 'Agudo', 17, 'Volleyball', 23, 'MB', 69.0, 1.85, 134),
(5, 'Tin', 'Ubaldo', 9, 'Volleyball', 22, 'OH', 63.0, 1.77, 123),
(5, 'Shiela', 'Kiseo', 12, 'Volleyball', 21, 'MB', 66.0, 1.83, 112),
(5, 'Buding', 'Duremdes', 5, 'Volleyball', 20, 'L', 57.0, 1.68, 98),
-- UE Volleyball (6 players)
(6, 'KC', 'Galdones', 1, 'Volleyball', 22, 'S', 60.0, 1.74, 123),
(6, 'Kath', 'Arado', 26, 'Volleyball', 21, 'OH', 66.0, 1.80, 145),
(6, 'Mary Rhose', 'Dapol', 12, 'Volleyball', 23, 'MB', 68.0, 1.83, 134),
(6, 'Judith', 'Abil', 7, 'Volleyball', 22, 'OH', 64.0, 1.79, 123),
(6, 'Casiey', 'Dongallo', 14, 'Volleyball', 21, 'MB', 67.0, 1.84, 112),
(6, 'Ja-Rone', 'Kaal', 3, 'Volleyball', 20, 'L', 58.0, 1.70, 95),
-- NU Volleyball (6 players)
(7, 'Joyme', 'Cagande', 10, 'Volleyball', 22, 'S', 61.0, 1.76, 156),
(7, 'Ivy', 'Lacsina', 25, 'Volleyball', 21, 'OH', 65.0, 1.79, 167),
(7, 'Bella', 'Belen', 15, 'Volleyball', 23, 'MB', 69.0, 1.86, 145),
(7, 'Alyssa', 'Solomon', 8, 'Volleyball', 22, 'OH', 64.0, 1.78, 134),
(7, 'Rosel', 'Predenciado', 13, 'Volleyball', 21, 'MB', 68.0, 1.85, 123),
(7, 'Jen', 'Nierva', 6, 'Volleyball', 20, 'L', 57.0, 1.67, 106),
-- Adamson Volleyball (6 players)
(8, 'Louie', 'Romero', 2, 'Volleyball', 21, 'S', 60.0, 1.73, 134),
(8, 'Trisha', 'Genesis', 22, 'Volleyball', 22, 'OH', 64.0, 1.77, 156),
(8, 'Lorene', 'Toring', 11, 'Volleyball', 23, 'MB', 68.0, 1.84, 123),
(8, 'Lucille', 'Almonte', 5, 'Volleyball', 22, 'OH', 63.0, 1.76, 112),
(8, 'May', 'Roque', 9, 'Volleyball', 21, 'MB', 67.0, 1.83, 106),
(8, 'Kate', 'Santiago', 4, 'Volleyball', 20, 'L', 58.0, 1.69, 95);

-- Matches
INSERT INTO `match` (event_id, match_type, match_date, match_time_start, match_time_end, status, score_summary)
VALUES
(1, 'Elimination_Round', '2025-11-15', '14:00:00', '16:00:00', 'Completed', 'DLSU 78 - ADMU 74'),
(1, 'Elimination_Round', '2025-11-15', '16:30:00', '18:30:00', 'Completed', 'UP 82 - NU 79'),
(2, 'Elimination_Round', '2025-11-20', '16:00:00', '18:00:00', 'Completed', 'UST 76 - FEU 72'),
(3, 'Elimination_Round', '2025-11-25', '18:00:00', '20:00:00', 'Completed', 'ADMU 85 - UE 68'),
(4, 'Semifinals', '2025-12-15', '18:00:00', '20:30:00', 'Scheduled', NULL),
(5, 'Finals', '2026-01-10', '18:00:00', '21:00:00', 'Scheduled', NULL),
(6, 'Elimination_Round', '2025-11-18', '14:00:00', '17:00:00', 'Completed', 'DLSU 3-1 UST'),
(7, 'Elimination_Round', '2025-11-22', '14:00:00', '17:00:00', 'Completed', 'ADMU 3-2 UP'),
(8, 'Semifinals', '2025-12-20', '14:00:00', '18:00:00', 'Scheduled', NULL);

-- Match Teams
INSERT INTO match_team (match_id, team_id, is_home, team_score)
VALUES
(1, 1, TRUE, 78), (1, 2, FALSE, 74),
(2, 3, TRUE, 82), (2, 7, FALSE, 79),
(3, 4, TRUE, 76), (3, 5, FALSE, 72),
(4, 2, TRUE, 85), (4, 6, FALSE, 68),
(5, 1, TRUE, 0), (5, 2, FALSE, 0),
(6, 1, TRUE, 0), (6, 3, FALSE, 0),
(7, 1, TRUE, 3), (7, 4, FALSE, 1),
(8, 2, TRUE, 3), (8, 3, FALSE, 2),
(9, 1, TRUE, 0), (9, 2, FALSE, 0);

-- Basketball Quarter Scores
INSERT INTO match_quarter_score (match_id, team_id, quarter_no, quarter_points)
VALUES
-- Match 1: DLSU 78 - ADMU 74
(1, 1, 1, 18), (1, 1, 2, 22), (1, 1, 3, 19), (1, 1, 4, 19),
(1, 2, 1, 20), (1, 2, 2, 18), (1, 2, 3, 17), (1, 2, 4, 19),
-- Match 2: UP 82 - NU 79
(2, 3, 1, 21), (2, 3, 2, 19), (2, 3, 3, 22), (2, 3, 4, 20),
(2, 7, 1, 18), (2, 7, 2, 22), (2, 7, 3, 19), (2, 7, 4, 20),
-- Match 3: UST 76 - FEU 72
(3, 4, 1, 19), (3, 4, 2, 18), (3, 4, 3, 20), (3, 4, 4, 19),
(3, 5, 1, 17), (3, 5, 2, 19), (3, 5, 3, 18), (3, 5, 4, 18),
-- Match 4: ADMU 85 - UE 68
(4, 2, 1, 22), (4, 2, 2, 21), (4, 2, 3, 20), (4, 2, 4, 22),
(4, 6, 1, 16), (4, 6, 2, 18), (4, 6, 3, 17), (4, 6, 4, 17);

-- Volleyball Set Scores
INSERT INTO match_set_score (match_id, team_id, set_no, set_points)
VALUES
-- Match 7: DLSU 3-1 UST
(7, 1, 1, 25), (7, 1, 2, 23), (7, 1, 3, 25), (7, 1, 4, 25),
(7, 4, 1, 22), (7, 4, 2, 25), (7, 4, 3, 20), (7, 4, 4, 18),
-- Match 8: ADMU 3-2 UP
(8, 2, 1, 25), (8, 2, 2, 22), (8, 2, 3, 25), (8, 2, 4, 23), (8, 2, 5, 15),
(8, 3, 1, 23), (8, 3, 2, 25), (8, 3, 3, 20), (8, 3, 4, 25), (8, 3, 5, 12);

-- Tickets
INSERT INTO ticket (default_price, price, ticket_status)
VALUES
(300.00, NULL, 'Active'),  -- General Admission
(500.00, NULL, 'Active'),  -- Upper Box
(750.00, NULL, 'Active'),  -- Lower Box
(1200.00, NULL, 'Active'), -- Courtside
(400.00, NULL, 'Active');  -- Patron

-- Customers
INSERT INTO customer (customer_first_name, customer_last_name, phone_number, email, organization, registration_date, preferred_team, customer_status, payment_method)
VALUES
('Carla', 'Reyes', '09170000001', 'carla.reyes@example.com', 'De La Salle University', '2025-09-01', 'De La Salle Green Archers', 'Active', 'GCash'),
('Miguel', 'Santos', '09170000002', 'miguel.santos@example.com', 'University of the Philippines', '2025-09-05', 'UP Fighting Maroons', 'Active', 'Credit Card'),
('Alyssa', 'Valdez', '09170000003', 'alyssa.valdez@example.com', 'Ateneo de Manila University', '2025-09-08', 'Ateneo Blue Eagles', 'Active', 'Debit Card'),
('Marco', 'Dela Cruz', '09170000004', 'marco.delacruz@example.com', 'UST', '2025-09-12', 'UST Growling Tigers', 'Active', 'GCash'),
('Sofia', 'Garcia', '09170000005', 'sofia.garcia@example.com', 'FEU', '2025-09-15', 'Far Eastern University Tamaraws', 'Active', 'PayMaya'),
('James', 'Tan', '09170000006', 'james.tan@example.com', 'National University', '2025-09-18', 'National University Bulldogs', 'Active', 'Credit Card'),
('Elena', 'Bautista', '09170000007', 'elena.bautista@example.com', 'Adamson University', '2025-09-20', 'Adamson Soaring Falcons', 'Active', 'GCash'),
('Robert', 'Cruz', '09170000008', 'robert.cruz@example.com', 'De La Salle University', '2025-09-25', 'De La Salle Green Archers', 'Active', 'Debit Card'),
('Maria', 'Lopez', '09170000009', 'maria.lopez@example.com', 'Ateneo de Manila University', '2025-09-28', 'Ateneo Blue Eagles', 'Active', 'Credit Card'),
('Juan', 'Rivera', '09170000010', 'juan.rivera@example.com', 'University of the Philippines', '2025-10-01', 'UP Fighting Maroons', 'Active', 'PayMaya');

-- Seats
INSERT INTO seat (seat_type, venue_address, seat_status, ticket_id)
VALUES
('Lower Box', 'Mall of Asia Arena', 'Sold', 3),
('Lower Box', 'Mall of Asia Arena', 'Sold', 3),
('Upper Box', 'Mall of Asia Arena', 'Available', 2),
('Courtside', 'Smart Araneta Coliseum', 'Available', 4),
('Courtside', 'Smart Araneta Coliseum', 'Available', 4),
('Upper Box', 'Smart Araneta Coliseum', 'Available', 2),
('Lower Box', 'PhilSports Arena', 'Available', 3),
('General Admission', 'Mall of Asia Arena', 'Available', 1),
('Patron', 'Mall of Asia Arena', 'Available', 5),
('Patron', 'Smart Araneta Coliseum', 'Available', 5);

-- Seat and Ticket Transactions
INSERT INTO seat_and_ticket (seat_id, event_id, customer_id, sale_datetime, quantity, unit_price, ticket_id, match_id, sale_status)
VALUES
(1, 1, 1, '2025-11-01 10:00:00', 1, 750.00, 3, 1, 'Sold'),
(2, 1, 2, '2025-11-02 09:30:00', 1, 750.00, 3, 1, 'Sold'),
(4, 2, 3, '2025-11-15 11:15:00', 1, 1200.00, 4, 3, 'Sold');

-- Ticket Refund Audit
INSERT INTO ticket_refund_audit (seat_and_ticket_rec_id, refund_amount, reason, processed_by)
VALUES
(3, 1200.00, 'Customer requested refund before event start.', 'Ticketing Desk Supervisor');

-- Event Personnel
INSERT INTO event_personnel (personnel_first_name, personnel_last_name, availability_status, role, affiliation, contact_no, event_id, match_id)
VALUES
('Mariel', 'Flores', 'Confirmed', 'Usher', 'Mall of Asia Arena Events', '0917-000-1111', 1, 1),
('Paolo', 'Reyes', 'Confirmed', 'Referee', 'UAAP Officials Pool', '0917-000-2222', 2, 3),
('Angela', 'Santos', 'Confirmed', 'Host', 'Smart Communications', '0917-000-3333', 3, 4);
