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
) AUTO_INCREMENT=1000;

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
) AUTO_INCREMENT=1000;

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
    ON DELETE RESTRICT ON UPDATE CASCADE
) AUTO_INCREMENT=1000;

CREATE TABLE `match` (
  match_id         INT          NOT NULL AUTO_INCREMENT,
  event_id         INT          NOT NULL,
  match_type       ENUM('Elimination_Round','Semifinals','Finals') NOT NULL,
  match_time_start TIME         NOT NULL,
  match_time_end   TIME         NOT NULL,
  status           ENUM('Scheduled','Completed','Cancelled','Postponed') NOT NULL DEFAULT 'Scheduled',
  score_summary    VARCHAR(255),
  PRIMARY KEY (match_id),
  KEY idx_match_event (event_id),
  CONSTRAINT fk_match_event
    FOREIGN KEY (event_id) REFERENCES event(event_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) AUTO_INCREMENT=1000;

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
) AUTO_INCREMENT=1000;

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
) AUTO_INCREMENT=1000;

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
) AUTO_INCREMENT=1000;

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
) AUTO_INCREMENT=1000;

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
) AUTO_INCREMENT=1000;

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
) AUTO_INCREMENT=1000;

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

-- Events (10 datasets)
INSERT INTO event (event_name, sport, match_date, event_time_start, event_time_end, venue_address, venue_capacity, event_status)
VALUES
('UAAP S87 Basketball - Opening Day', 'Basketball', '2025-11-15', '14:00:00', '18:30:00', 'Mall of Asia Arena', 15000, 'Completed'),
('UAAP S87 Basketball - Round 1 Week 2', 'Basketball', '2025-11-20', '16:00:00', '18:00:00', 'Smart Araneta Coliseum', 16000, 'Completed'),
('UAAP S87 Basketball - Round 1 Week 3', 'Basketball', '2025-11-25', '18:00:00', '20:00:00', 'PhilSports Arena', 8000, 'Completed'),
('UAAP S87 Basketball - Semifinals', 'Basketball', '2025-12-15', '18:00:00', '20:30:00', 'Smart Araneta Coliseum', 16000, 'Scheduled'),
('UAAP S87 Basketball - Finals', 'Basketball', '2026-01-10', '18:00:00', '21:00:00', 'Mall of Asia Arena', 15000, 'Scheduled'),
('UAAP S87 Volleyball - Opening Day', 'Volleyball', '2025-11-18', '14:00:00', '17:00:00', 'Mall of Asia Arena', 15000, 'Completed'),
('UAAP S87 Volleyball - Round 1 Week 2', 'Volleyball', '2025-11-22', '14:00:00', '17:00:00', 'Smart Araneta Coliseum', 16000, 'Completed'),
('UAAP S87 Volleyball - Semifinals', 'Volleyball', '2025-12-20', '14:00:00', '18:00:00', 'Mall of Asia Arena', 15000, 'Scheduled'),
('UAAP S87 Basketball - Round 1 Week 4', 'Basketball', '2025-11-28', '16:00:00', '20:00:00', 'Ynares Center', 10000, 'Completed'),
('UAAP S87 Volleyball - Finals', 'Volleyball', '2026-01-15', '14:00:00', '18:00:00', 'Filoil EcoOil Centre', 8000, 'Scheduled');

-- Players (Basketball - 5 per team, Volleyball - 6 per team)
INSERT INTO player (team_id, player_first_name, player_last_name, player_number, player_sport, age, position, weight, height, individual_score)
VALUES
-- DLSU Basketball (5 players)
(1000, 'Kevin', 'Quiambao', 5, 'Basketball', 22, 'SF', 88.0, 1.98, 285),
(1000, 'Evan', 'Nelle', 8, 'Basketball', 23, 'PG', 78.0, 1.85, 245),
(1000, 'Michael', 'Phillips', 15, 'Basketball', 24, 'PF', 92.0, 2.01, 198),
(1000, 'Joshua', 'David', 12, 'Basketball', 21, 'SG', 75.0, 1.83, 167),
(1000, 'Mark', 'Nonoy', 20, 'Basketball', 22, 'C', 95.0, 2.00, 156),
-- Ateneo Basketball (5 players)
(1001, 'Forthsky', 'Padrigao', 11, 'Basketball', 22, 'SG', 75.0, 1.82, 267),
(1001, 'Kai', 'Ballungay', 4, 'Basketball', 24, 'PF', 92.0, 2.03, 243),
(1001, 'Chris', 'Koon', 33, 'Basketball', 23, 'C', 98.0, 2.05, 189),
(1001, 'Sean', 'Quitevis', 7, 'Basketball', 21, 'PG', 74.0, 1.81, 156),
(1001, 'Jared', 'Bahay', 14, 'Basketball', 22, 'SF', 81.0, 1.90, 178),
-- UP Basketball (5 players)
(1002, 'Carl', 'Tamayo', 33, 'Basketball', 23, 'PF', 95.0, 2.05, 256),
(1002, 'Malick', 'Diouf', 11, 'Basketball', 22, 'C', 102.0, 2.08, 234),
(1002, 'Francis', 'Lopez', 7, 'Basketball', 24, 'SG', 77.0, 1.85, 198),
(1002, 'Terrence', 'Fortea', 8, 'Basketball', 21, 'PG', 73.0, 1.80, 167),
(1002, 'Aldous', 'Torculas', 10, 'Basketball', 23, 'SF', 85.0, 1.92, 145),
-- UST Basketball (5 players)
(1003, 'Soulemane', 'Chabi Yo', 15, 'Basketball', 25, 'C', 100.0, 2.01, 223),
(1003, 'Nic', 'Cabanero', 12, 'Basketball', 23, 'PG', 76.0, 1.83, 189),
(1003, 'Christian', 'Manaytay', 7, 'Basketball', 22, 'SF', 88.0, 1.95, 167),
(1003, 'Royce', 'Mantua', 24, 'Basketball', 21, 'SG', 75.0, 1.81, 145),
(1003, 'Paul', 'Manalang', 21, 'Basketball', 22, 'PF', 90.0, 1.98, 134),
-- FEU Basketball (5 players)
(1004, 'Royce', 'Alforque', 11, 'Basketball', 23, 'PG', 77.0, 1.84, 234),
(1004, 'Mo', 'Konateh', 33, 'Basketball', 24, 'C', 99.0, 2.04, 212),
(1004, 'Veejay', 'Pre', 5, 'Basketball', 22, 'SF', 87.0, 1.96, 178),
(1004, 'Jilson', 'Bautista', 7, 'Basketball', 21, 'SG', 74.0, 1.82, 156),
(1004, 'Patrick', 'Sleat', 4, 'Basketball', 23, 'PF', 91.0, 2.00, 145),
-- UE Basketball (5 players)
(1005, 'Precious', 'Momowei', 14, 'Basketball', 24, 'C', 101.0, 2.06, 198),
(1005, 'Ethan', 'Galang', 7, 'Basketball', 22, 'SG', 76.0, 1.83, 167),
(1005, 'Kyle', 'Paranada', 11, 'Basketball', 21, 'SF', 86.0, 1.95, 145),
(1005, 'Wello', 'Lingolingo', 5, 'Basketball', 23, 'PG', 73.0, 1.80, 123),
(1005, 'Rey', 'Remogat', 9, 'Basketball', 22, 'PF', 89.0, 1.97, 112),
-- NU Basketball (5 players)
(1006, 'Steve', 'Nash', 11, 'Basketball', 23, 'PG', 78.0, 1.86, 245),
(1006, 'Jake', 'Figueroa', 22, 'Basketball', 24, 'PF', 93.0, 2.00, 223),
(1006, 'PJ', 'Palacielo', 7, 'Basketball', 22, 'C', 97.0, 2.03, 189),
(1006, 'Kenshin', 'Padilla', 15, 'Basketball', 21, 'SG', 75.0, 1.82, 167),
(1006, 'Jake', 'Palaganas', 3, 'Basketball', 22, 'SF', 84.0, 1.93, 156),
-- Adamson Basketball (5 players)
(1007, 'Matty', 'Erolon', 7, 'Basketball', 22, 'PG', 77.0, 1.84, 212),
(1007, 'Matthew', 'Montebon', 23, 'Basketball', 23, 'SF', 90.0, 1.98, 189),
(1007, 'Didat', 'Hanapi', 11, 'Basketball', 24, 'C', 96.0, 2.02, 167),
(1007, 'Joshua', 'Barcelona', 5, 'Basketball', 21, 'SG', 74.0, 1.81, 145),
(1007, 'Joem', 'Sabandal', 13, 'Basketball', 23, 'PF', 88.0, 1.96, 134),
-- DLSU Volleyball (6 players)
(1000, 'Angel', 'Canino', 1, 'Volleyball', 21, 'S', 60.0, 1.75, 145),
(1000, 'Leila', 'Cruz', 3, 'Volleyball', 22, 'OH', 65.0, 1.80, 178),
(1000, 'Julia', 'Coronel', 9, 'Volleyball', 20, 'MB', 68.0, 1.85, 134),
(1000, 'Thea', 'Gagate', 14, 'Volleyball', 21, 'OH', 64.0, 1.78, 123),
(1000, 'Baby Love', 'Barbon', 7, 'Volleyball', 22, 'MB', 67.0, 1.84, 112),
(1000, 'Justine', 'Jazareno', 4, 'Volleyball', 20, 'L', 58.0, 1.68, 98),
-- Ateneo Volleyball (6 players)
(1001, 'Jhoana', 'Maraguinot', 2, 'Volleyball', 23, 'S', 61.0, 1.76, 156),
(1001, 'Faith', 'Nisperos', 6, 'Volleyball', 22, 'OH', 64.0, 1.79, 189),
(1001, 'Vanie', 'Gandler', 16, 'Volleyball', 21, 'MB', 69.0, 1.86, 145),
(1001, 'Lyann', 'DeGuzman', 9, 'Volleyball', 22, 'OH', 63.0, 1.77, 134),
(1001, 'AC', 'Miner', 11, 'Volleyball', 21, 'MB', 68.0, 1.83, 123),
(1001, 'Dani', 'Ravena', 5, 'Volleyball', 20, 'L', 59.0, 1.70, 106),
-- UP Volleyball (6 players)
(1002, 'Alyssa', 'Bertolano', 12, 'Volleyball', 22, 'S', 62.0, 1.74, 134),
(1002, 'Jewel', 'Lai', 18, 'Volleyball', 21, 'OH', 66.0, 1.81, 178),
(1002, 'Stephanie', 'Bustrillo', 21, 'Volleyball', 23, 'MB', 70.0, 1.87, 156),
(1002, 'Nina', 'Ytang', 7, 'Volleyball', 22, 'OH', 64.0, 1.79, 145),
(1002, 'Cassie', 'Lim', 14, 'Volleyball', 21, 'MB', 67.0, 1.84, 123),
(1002, 'Marist', 'Layug', 4, 'Volleyball', 20, 'L', 57.0, 1.67, 98),
-- UST Volleyball (6 players)
(1003, 'Cassie', 'Carballo', 4, 'Volleyball', 22, 'S', 60.0, 1.73, 123),
(1003, 'Eya', 'Laure', 19, 'Volleyball', 23, 'OH', 65.0, 1.82, 189),
(1003, 'Xyza', 'Gula', 13, 'Volleyball', 21, 'MB', 68.0, 1.84, 134),
(1003, 'Imee', 'Hernandez', 8, 'Volleyball', 22, 'OH', 63.0, 1.78, 145),
(1003, 'Angge', 'Poyos', 11, 'Volleyball', 21, 'MB', 67.0, 1.85, 112),
(1003, 'KC', 'De Luna', 2, 'Volleyball', 20, 'L', 58.0, 1.69, 95),
-- FEU Volleyball (6 players)
(1004, 'Kyle', 'Negrito', 3, 'Volleyball', 21, 'S', 61.0, 1.75, 145),
(1004, 'Jean', 'Asis', 6, 'Volleyball', 22, 'OH', 64.0, 1.78, 167),
(1004, 'Ivana', 'Agudo', 17, 'Volleyball', 23, 'MB', 69.0, 1.85, 134),
(1004, 'Tin', 'Ubaldo', 9, 'Volleyball', 22, 'OH', 63.0, 1.77, 123),
(1004, 'Shiela', 'Kiseo', 12, 'Volleyball', 21, 'MB', 66.0, 1.83, 112),
(1004, 'Buding', 'Duremdes', 5, 'Volleyball', 20, 'L', 57.0, 1.68, 98),
-- UE Volleyball (6 players)
(1005, 'KC', 'Galdones', 1, 'Volleyball', 22, 'S', 60.0, 1.74, 123),
(1005, 'Kath', 'Arado', 26, 'Volleyball', 21, 'OH', 66.0, 1.80, 145),
(1005, 'Mary Rhose', 'Dapol', 12, 'Volleyball', 23, 'MB', 68.0, 1.83, 134),
(1005, 'Judith', 'Abil', 7, 'Volleyball', 22, 'OH', 64.0, 1.79, 123),
(1005, 'Casiey', 'Dongallo', 14, 'Volleyball', 21, 'MB', 67.0, 1.84, 112),
(1005, 'Ja-Rone', 'Kaal', 3, 'Volleyball', 20, 'L', 58.0, 1.70, 95),
-- NU Volleyball (6 players)
(1006, 'Joyme', 'Cagande', 10, 'Volleyball', 22, 'S', 61.0, 1.76, 156),
(1006, 'Ivy', 'Lacsina', 25, 'Volleyball', 21, 'OH', 65.0, 1.79, 167),
(1006, 'Bella', 'Belen', 15, 'Volleyball', 23, 'MB', 69.0, 1.86, 145),
(1006, 'Alyssa', 'Solomon', 8, 'Volleyball', 22, 'OH', 64.0, 1.78, 134),
(1006, 'Rosel', 'Predenciado', 13, 'Volleyball', 21, 'MB', 68.0, 1.85, 123),
(1006, 'Jen', 'Nierva', 6, 'Volleyball', 20, 'L', 57.0, 1.67, 106),
-- Adamson Volleyball (6 players)
(1007, 'Louie', 'Romero', 2, 'Volleyball', 21, 'S', 60.0, 1.73, 134),
(1007, 'Trisha', 'Genesis', 22, 'Volleyball', 22, 'OH', 64.0, 1.77, 156),
(1007, 'Lorene', 'Toring', 11, 'Volleyball', 23, 'MB', 68.0, 1.84, 123),
(1007, 'Lucille', 'Almonte', 5, 'Volleyball', 22, 'OH', 63.0, 1.76, 112),
(1007, 'May', 'Roque', 9, 'Volleyball', 21, 'MB', 67.0, 1.83, 106),
(1007, 'Kate', 'Santiago', 4, 'Volleyball', 20, 'L', 58.0, 1.69, 95);

-- Matches (10 datasets)
INSERT INTO `match` (event_id, match_type, match_time_start, match_time_end, status, score_summary)
VALUES
(1000, 'Elimination_Round', '14:00:00', '16:00:00', 'Completed', 'DLSU 78 - ADMU 74'),
(1000, 'Elimination_Round', '16:30:00', '18:30:00', 'Completed', 'UP 82 - NU 79'),
(1001, 'Elimination_Round', '16:00:00', '18:00:00', 'Completed', 'UST 76 - FEU 72'),
(1002, 'Elimination_Round', '18:00:00', '20:00:00', 'Completed', 'ADMU 85 - UE 68'),
(1003, 'Semifinals', '18:00:00', '20:30:00', 'Scheduled', NULL),
(1004, 'Finals', '18:00:00', '21:00:00', 'Scheduled', NULL),
(1005, 'Elimination_Round', '14:00:00', '17:00:00', 'Completed', 'DLSU 3-1 UST'),
(1006, 'Elimination_Round', '14:00:00', '17:00:00', 'Completed', 'ADMU 3-2 UP'),
(1007, 'Semifinals', '14:00:00', '18:00:00', 'Scheduled', NULL),
(1008, 'Elimination_Round', '16:00:00', '20:00:00', 'Completed', 'NU 88 - FEU 81');

-- Match Teams
INSERT INTO match_team (match_id, team_id, is_home, team_score)
VALUES
(1000, 1000, TRUE, 78), (1000, 1001, FALSE, 74),
(1001, 1002, TRUE, 82), (1001, 1006, FALSE, 79),
(1002, 1003, TRUE, 76), (1002, 1004, FALSE, 72),
(1003, 1001, TRUE, 85), (1003, 1005, FALSE, 68),
(1004, 1000, TRUE, 0), (1004, 1001, FALSE, 0),
(1005, 1000, TRUE, 0), (1005, 1002, FALSE, 0),
(1006, 1000, TRUE, 3), (1006, 1003, FALSE, 1),
(1007, 1001, TRUE, 3), (1007, 1002, FALSE, 2),
(1008, 1000, TRUE, 0), (1008, 1001, FALSE, 0);

-- Basketball Quarter Scores
INSERT INTO match_quarter_score (match_id, team_id, quarter_no, quarter_points)
VALUES
-- Match 1000: DLSU 78 - ADMU 74
(1000, 1000, 1, 18), (1000, 1000, 2, 22), (1000, 1000, 3, 19), (1000, 1000, 4, 19),
(1000, 1001, 1, 20), (1000, 1001, 2, 18), (1000, 1001, 3, 17), (1000, 1001, 4, 19),
-- Match 1001: UP 82 - NU 79
(1001, 1002, 1, 21), (1001, 1002, 2, 19), (1001, 1002, 3, 22), (1001, 1002, 4, 20),
(1001, 1006, 1, 18), (1001, 1006, 2, 22), (1001, 1006, 3, 19), (1001, 1006, 4, 20),
-- Match 1002: UST 76 - FEU 72
(1002, 1003, 1, 19), (1002, 1003, 2, 18), (1002, 1003, 3, 20), (1002, 1003, 4, 19),
(1002, 1004, 1, 17), (1002, 1004, 2, 19), (1002, 1004, 3, 18), (1002, 1004, 4, 18),
-- Match 1003: ADMU 85 - UE 68
(1003, 1001, 1, 22), (1003, 1001, 2, 21), (1003, 1001, 3, 20), (1003, 1001, 4, 22),
(1003, 1005, 1, 16), (1003, 1005, 2, 18), (1003, 1005, 3, 17), (1003, 1005, 4, 17);

-- Volleyball Set Scores
INSERT INTO match_set_score (match_id, team_id, set_no, set_points)
VALUES
-- Match 1006: DLSU 3-1 UST
(1006, 1000, 1, 25), (1006, 1000, 2, 23), (1006, 1000, 3, 25), (1006, 1000, 4, 25),
(1006, 1003, 1, 22), (1006, 1003, 2, 25), (1006, 1003, 3, 20), (1006, 1003, 4, 18),
-- Match 1007: ADMU 3-2 UP
(1007, 1001, 1, 25), (1007, 1001, 2, 22), (1007, 1001, 3, 25), (1007, 1001, 4, 23), (1007, 1001, 5, 15),
(1007, 1002, 1, 23), (1007, 1002, 2, 25), (1007, 1002, 3, 20), (1007, 1002, 4, 25), (1007, 1002, 5, 12);

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
('Lower Box', 'Mall of Asia Arena', 'Sold', 1002),
('Lower Box', 'Mall of Asia Arena', 'Sold', 1002),
('Upper Box', 'Mall of Asia Arena', 'Available', 1001),
('Courtside', 'Smart Araneta Coliseum', 'Available', 1003),
('Courtside', 'Smart Araneta Coliseum', 'Available', 1003),
('Upper Box', 'Smart Araneta Coliseum', 'Available', 1001),
('Lower Box', 'PhilSports Arena', 'Available', 1002),
('General Admission', 'Mall of Asia Arena', 'Available', 1000),
('Patron', 'Mall of Asia Arena', 'Available', 1004),
('Patron', 'Smart Araneta Coliseum', 'Available', 1004);

-- Seat and Ticket Transactions (10 datasets)
INSERT INTO seat_and_ticket (seat_id, event_id, customer_id, sale_datetime, quantity, unit_price, ticket_id, match_id, sale_status)
VALUES
(1000, 1000, 1000, '2025-11-01 10:00:00', 1, 750.00, 1002, 1000, 'Sold'),
(1001, 1000, 1001, '2025-11-02 09:30:00', 1, 750.00, 1002, 1000, 'Sold'),
(1003, 1001, 1002, '2025-11-15 11:15:00', 1, 1200.00, 1003, 1002, 'Sold'),
(1002, 1001, 1003, '2025-11-16 14:20:00', 1, 500.00, 1001, 1002, 'Sold'),
(1004, 1002, 1004, '2025-11-18 08:45:00', 1, 1200.00, 1003, 1003, 'Sold'),
(1005, 1002, 1005, '2025-11-18 10:00:00', 1, 1200.00, 1003, 1003, 'Sold'),
(1006, 1005, 1006, '2025-11-10 15:30:00', 1, 500.00, 1001, 1006, 'Sold'),
(1007, 1005, 1007, '2025-11-11 16:00:00', 1, 750.00, 1002, 1006, 'Sold'),
(1008, 1006, 1008, '2025-11-12 12:00:00', 1, 300.00, 1000, 1007, 'Sold'),
(1009, 1008, 1009, '2025-11-22 13:30:00', 1, 400.00, 1004, 1008, 'Sold');

-- Ticket Refund Audit
INSERT INTO ticket_refund_audit (seat_and_ticket_rec_id, refund_amount, reason, processed_by)
VALUES
(1002, 1200.00, 'Bad Service!', 'Ticketing Desk Supervisor');

-- Event Personnel (10 datasets)
INSERT INTO event_personnel (personnel_first_name, personnel_last_name, availability_status, role, affiliation, contact_no, event_id, match_id)
VALUES
('Mariel', 'Flores', 'Confirmed', 'Usher', 'Mall of Asia Arena Events', '0917-000-1111', 1000, 1000),
('Paolo', 'Reyes', 'Confirmed', 'Referee', 'UAAP Officials Pool', '0917-000-2222', 1001, 1002),
('Angela', 'Santos', 'Confirmed', 'Host', 'Smart Communications', '0917-000-3333', 1002, 1003),
('Ricardo', 'Martinez', 'Confirmed', 'Security', 'PhilSports Security Services', '0917-000-4444', 1002, 1003),
('Linda', 'Villanueva', 'Confirmed', 'Scorekeeper', 'UAAP Stat Crew', '0917-000-5555', 1005, 1006),
('Carlos', 'Aquino', 'Confirmed', 'Ticketing Agent', 'SM Tickets', '0917-000-6666', 1005, 1006),
('Grace', 'Ramos', 'Confirmed', 'Cheerleader', 'DLSU Animo Squad', '0917-000-7777', 1000, 1001),
('Daniel', 'Torres', 'Confirmed', 'Stat Crew', 'UAAP Statistics Department', '0917-000-8888', 1006, 1007),
('Isabel', 'Fernandez', 'Confirmed', 'Singer', 'ABS-CBN Events', '0917-000-9999', 1000, 1000),
('Ramon', 'Garcia', 'Confirmed', 'Halftime Entertainment', 'UAAP Entertainment Group', '0917-001-0000', 1008, 1008);
