DROP SCHEMA IF EXISTS UAAPDBSQL;
CREATE SCHEMA UAAPDBSQL;
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
  team_name           VARCHAR(120) NOT NULL,
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
  age               INT,
  position          VARCHAR(30),
  weight            DECIMAL(5,2),
  height            DECIMAL(5,2),
  individual_score  INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (player_id),
  KEY idx_player_team (team_id),
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
  CONSTRAINT ck_mt_is_home CHECK (is_home IN (0,1)),
  CONSTRAINT uq_match_home_slot UNIQUE (match_id,is_home)
);

CREATE TABLE match_quarter_score (
  match_id    INT NOT NULL,
  team_id     INT NOT NULL,
  quarter_no  INT NOT NULL,
  points      INT NOT NULL,
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
  points     INT NOT NULL,
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
  price         DECIMAL(10,2),
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
  preferred_sport      ENUM('Basketball','Volleyball'),
  customer_status      VARCHAR(20),
  payment_method       VARCHAR(30),
  PRIMARY KEY (customer_id),
  UNIQUE KEY uq_customer_email (email)
);

CREATE TABLE seat (
  seat_id        INT          NOT NULL AUTO_INCREMENT,
  seat_type      VARCHAR(30)  NOT NULL,
  seat_section   VARCHAR(60),
  venue_address  ENUM('Mall of Asia Arena','Smart Araneta Coliseum','PhilSports Arena','Ynares Center','Filoil EcoOil Centre') NOT NULL,
  seat_status    ENUM('Available','OnHold','Sold') NOT NULL DEFAULT 'Available',
  PRIMARY KEY (seat_id),
  KEY idx_seat_venue (venue_address)
);

CREATE TABLE seat_and_ticket (
  seat_and_ticket_rec_id INT           NOT NULL AUTO_INCREMENT,
  seat_id                INT           NOT NULL,
  event_id               INT           NOT NULL,
  customer_id            INT           NOT NULL,
  sale_datetime          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  price_sold             DECIMAL(10,2) NOT NULL,
  ticket_id              INT           NOT NULL,
  match_id               INT           NULL,
  sale_status            ENUM('Sold','Refunded') NOT NULL DEFAULT 'Sold',
  refund_datetime        TIMESTAMP     NULL,
  PRIMARY KEY (seat_and_ticket_rec_id),
  UNIQUE KEY uq_active_sale (event_id, seat_id, sale_status),
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
    ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE ticket_refund_audit (
  audit_id                INT           NOT NULL AUTO_INCREMENT,
  seat_and_ticket_rec_id  INT           NOT NULL,
  refund_datetime         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  refund_amount           DECIMAL(10,2) NOT NULL,
  reason                  VARCHAR(255),
  processed_by            VARCHAR(120),
  PRIMARY KEY (audit_id),
  KEY idx_refund_sale (seat_and_ticket_rec_id),
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
  match_id              INT          NULL,
  PRIMARY KEY (personnel_id),
  KEY idx_ep_event (event_id),
  KEY idx_ep_match (match_id),
  CONSTRAINT fk_ep_event
    FOREIGN KEY (event_id) REFERENCES event(event_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ep_match
    FOREIGN KEY (match_id) REFERENCES `match`(match_id)
    ON DELETE SET NULL ON UPDATE CASCADE
);

INSERT INTO team (team_name, seasons_played, standing_wins, standing_losses, total_games_played)
VALUES
('De La Salle Green Archers', 12, 8, 2, 10),
('Ateneo Blue Eagles', 12, 7, 3, 10),
('UP Fighting Maroons', 12, 6, 4, 10),
('UST Growling Tigers', 12, 4, 6, 10);

INSERT INTO event (event_name, sport, match_date, event_time_start, event_time_end, venue_address, venue_capacity, event_status)
VALUES
('UAAP Season 87 Basketball Opening', 'Basketball', '2025-11-15', '16:00:00', '18:00:00', 'Mall of Asia Arena', 15000, 'Active'),
('UAAP Season 87 Volleyball Semifinals', 'Volleyball', '2025-12-02', '14:00:00', '17:00:00', 'Smart Araneta Coliseum', 16000, 'Scheduled'),
('UAAP Season 87 Basketball Finals Game 1', 'Basketball', '2026-01-10', '18:00:00', '20:30:00', 'Mall of Asia Arena', 15000, 'Scheduled');

INSERT INTO ticket (default_price, price, ticket_status)
VALUES
(500.00, NULL, 'Active'),
(750.00, NULL, 'Active'),
(1200.00, NULL, 'Active');

INSERT INTO customer (customer_first_name, customer_last_name, phone_number, email, organization, registration_date, preferred_sport, customer_status, payment_method)
VALUES
('Carla', 'Reyes', '09170000000', 'carla.reyes@example.com', 'De La Salle University', '2025-10-01', 'Basketball', 'Active', 'GCash'),
('Miguel', 'Santos', '09170000001', 'miguel.santos@example.com', 'University of the Philippines', '2025-10-03', 'Basketball', 'Active', 'Credit Card'),
('Alyssa', 'Valdez', '09170000002', 'alyssa.valdez@example.com', 'Ateneo de Manila University', '2025-10-05', 'Volleyball', 'Active', 'Debit Card');

INSERT INTO seat (seat_type, seat_section, venue_address, seat_status)
VALUES
('Lower Box', 'LB-101', 'Mall of Asia Arena', 'Sold'),
('Lower Box', 'LB-102', 'Mall of Asia Arena', 'Sold'),
('Upper Box', 'UB-210', 'Mall of Asia Arena', 'Available'),
('Courtside', 'CS-01', 'Smart Araneta Coliseum', 'Available'),
('Courtside', 'CS-02', 'Smart Araneta Coliseum', 'Available'),
('Upper Box', 'UB-110', 'Smart Araneta Coliseum', 'Available'),
('Lower Box', 'LB-201', 'PhilSports Arena', 'Available'),
('General Admission', 'GA-305', 'Mall of Asia Arena', 'Available');

INSERT INTO player (team_id, player_first_name, player_last_name, player_number, age, position, weight, height, individual_score)
VALUES
(1, 'Kevin', 'Quiambao', 5, 22, 'Forward', 88.0, 1.98, 210),
(1, 'Evan', 'Nelle', 8, 23, 'Guard', 78.0, 1.85, 165),
(2, 'Forthsky', 'Padrigao', 11, 22, 'Guard', 75.0, 1.82, 188),
(2, 'Kai', 'Ballungay', 4, 24, 'Forward', 92.0, 2.03, 205),
(3, 'Carl', 'Tamayo', 33, 23, 'Forward', 95.0, 2.05, 198),
(4, 'Soulemane', 'Chabi Yo', 15, 25, 'Center', 100.0, 2.01, 172);

INSERT INTO `match` (event_id, match_type, match_date, match_time_start, match_time_end, status, score_summary)
VALUES
(1, 'Elimination_Round', '2025-11-15', '16:00:00', '18:00:00', 'Completed', 'DLSU 78 - ADMU 74'),
(1, 'Elimination_Round', '2025-11-18', '18:00:00', '20:00:00', 'Scheduled', NULL),
(2, 'Semifinals', '2025-12-02', '14:00:00', '17:00:00', 'Scheduled', NULL),
(3, 'Finals', '2026-01-10', '18:00:00', '20:30:00', 'Scheduled', NULL);

INSERT INTO match_team (match_id, team_id, is_home, team_score)
VALUES
(1, 1, TRUE, 78),
(1, 2, FALSE, 74),
(2, 1, TRUE, 0),
(2, 3, FALSE, 0),
(3, 3, TRUE, 0),
(3, 4, FALSE, 0),
(4, 1, TRUE, 0),
(4, 2, FALSE, 0);

INSERT INTO seat_and_ticket (seat_id, event_id, customer_id, sale_datetime, price_sold, ticket_id, match_id, sale_status, refund_datetime)
VALUES
(1, 1, 1, '2025-11-01 10:00:00', 750.00, 2, 1, 'Sold', NULL),
(2, 1, 2, '2025-11-02 09:30:00', 750.00, 2, 1, 'Sold', NULL),
(4, 2, 3, '2025-11-20 11:15:00', 1200.00, 3, 3, 'Refunded', '2025-11-25 08:30:00');

INSERT INTO ticket_refund_audit (seat_and_ticket_rec_id, refund_datetime, refund_amount, reason, processed_by)
VALUES
(3, '2025-11-25 08:35:00', 1200.00, 'Customer requested refund before event start.', 'Ticketing Desk Supervisor');

-- Align seat availability with sample transactions
UPDATE seat SET seat_status = 'Sold' WHERE seat_id IN (1, 2);
UPDATE seat SET seat_status = 'Available' WHERE seat_id = 4;


