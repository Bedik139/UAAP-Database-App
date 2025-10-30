# UAAP Management System

Lightweight Swing UI for managing UAAP events, matches, teams, tickets, and related records backed by a MySQL database.

## Prerequisites
- Java Development Kit (JDK) 11 or newer (tested with JDK 17) for `javac`, `java`, and Swing.
- MySQL Server 8.0+ running locally or reachable over the network.
- MySQL Connector/J 9.5.0 (already included in `lib/mysql-connector-j-9.5.0.jar`).
- An IDE or terminal that can compile Java with an external classpath.

## Required Files
- `UAAPApp.java` (main entry point that boots the Swing application).
- DAO/domain classes in the project root (`EventDAO.java`, `MatchDAO.java`, `TicketDAO.java`, etc.). Keep these source files in the same package-less directory when compiling.
- `Database.java` for JDBC URL, username, and password. Update the `URL`, `USER`, and `PASSWORD` constants to match your MySQL instance before compiling.
- `lib/mysql-connector-j-9.5.0.jar` must stay in `lib/` and be added to the runtime classpath.

## Database Setup
1. Create a schema named `UAAPDBSQL` (or adjust `Database.java`).
2. Provision the tables referenced by the DAOs with compatible columns and types:
   - `event(event_id, event_name, sport, match_date, event_time_start, event_time_end, venue_address)`
   - ``match``(match_id, event_id, match_type, match_date, match_time_start, match_time_end)
   - `match_team(match_id, team_id, is_home, team_score)`
   - `team(team_id, team_name, seasons_played, standing_wins, standing_losses, total_games_played)`
   - `player(player_id, team_id, player_first_name, player_last_name, player_number, age, position, weight, height, individual_score)`
   - `match_quarter_score(match_id, team_id, quarter_no, points)`
   - `match_set_score(match_id, team_id, set_no, points)`
   - `seat(seat_id, seat_type)`
   - `ticket(ticket_id, default_price, price, ticket_status)`
   - `seat_and_ticket(seat_and_ticket_rec_id, seat_id, event_id, customer_id, sale_datetime, price_sold, ticket_id, match_id)`
   - `customer(customer_id, customer_first_name, customer_last_name, phone_number, email, organization, registration_date, preferred_sport, customer_status, payment_method)`
   - `event_personnel(personnel_id, personnel_first_name, personnel_last_name, availability_status, role, affiliation, contact_no, event_id, match_id)`
3. Configure foreign keys to align with the relationships used in the DAOs (e.g., `match.event_id → event.event_id`, `player.team_id → team.team_id`, etc.).

## Compile and Run
Compile all sources from the project root, including the MySQL driver on the classpath:

```powershell
javac -cp "lib/mysql-connector-j-9.5.0.jar;." *.java
```

Launch the Swing app after compilation:

```powershell
java -cp "lib/mysql-connector-j-9.5.0.jar;." UAAPApp
```

> On macOS/Linux replace the classpath separator `;` with `:`.

## Troubleshooting
- If `ClassNotFoundException: com.mysql.cj.jdbc.Driver` appears, confirm the connector JAR path in the classpath.
- Connection failures typically mean the MySQL credentials or host in `Database.java` need to be updated or the `UAAPDBSQL` schema/tables are missing.
