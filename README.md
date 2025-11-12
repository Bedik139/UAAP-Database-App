# UAAP Event Management System

Replaces error-prone spreadsheets with a robust database system for:
- **Event & Match Scheduling** - Prevent double-booking with automated venue/time validation
- **Ticket Management** - Real-time seat availability with transaction-safe booking
- **Team & Player Tracking** - Live scores, standings, and individual statistics
- **Revenue Reporting** - Comprehensive analytics on sales, occupancy, and performance

## Prerequisites

- **Java JDK 11+** (tested with JDK 17)
- **MySQL 8.0+** (local or network)
- **MySQL Connector/J 9.5.0** (included in `lib/`)

## Database Setup

### 1. Create Database
```sql
CREATE DATABASE uaap_db;
USE uaap_db;
```

### 2. Run Schema
Execute `UAAPDB.sql` to create all tables with proper constraints:

**Core Tables:**
- `event` - Event details (sport, venue, dates, capacity, status)
- `match` - Individual matches linked to events
- `match_team` - Participating teams per match with scores
- `team` - UAAP teams with standings (wins/losses/games played)
- `player` - Player roster with biometrics and individual scores
- `venue` - Venue information and capacities

**Scoring Tables:**
- `match_quarter_score` - Basketball quarter-by-quarter breakdown
- `match_set_score` - Volleyball set-by-set breakdown (1-5 sets)

**Ticketing Tables:**
- `seat` - Physical seats with type, venue, status, pricing
- `ticket` - Ticket tiers (General Admission ₱500, Lower Box ₱750, Courtside ₱1200)
- `seat_and_ticket` - Sales transactions with auto-calculated `total_price` (quantity × unit_price)
- `customer` - Customer profiles with preferred sport and payment methods
- `refund_audit` - Refund logging and audit trail

**Operations Tables:**
- `event_personnel` - Staff assignments (ushers, referees, entertainers, etc.)

### 3. Update Database Connection
Edit `Database.java` credentials:
```java
private static final String URL = "jdbc:mysql://localhost:3306/uaap_db";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
```

## Compile and Run

### Compile (Windows)
```powershell
javac -cp ".;lib/mysql-connector-j-9.5.0.jar" *.java
```

### Compile (macOS/Linux)
```bash
javac -cp ".:lib/mysql-connector-j-9.5.0.jar" *.java
```

### Run
```powershell
java -cp ".;lib/mysql-connector-j-9.5.0.jar" UAAPApp
```

## Application Structure

### **Launch Screen**
Two portals: **Manager** (full control) | **Customer** (ticket operations)

---

### **MANAGER DASHBOARD** (11 Tabs)

#### 1. **Events**
- Create/edit events with sport presets, venue dropdowns
- Status workflow: Scheduled → Active → Completed
- Auto-filled venue capacity

#### 2. **Matches**
- Link matches to events, set type (Elimination/Semis/Finals)
- Match Spotlight displays team crests

#### 3. **Match Teams**
- Assign home/away teams with live scoring
- Team logos rendered inline

#### 4. **Match Results**
- Record final scores + optional summaries
- Individual player point attribution
- Cascading updates: match status, team standings, player totals

#### 5. **Quarter Scores (Basketball)**
- Manage quarter-by-quarter breakdown (Q1-Q4)
- Auto-generated performance reports

#### 6. **Set Scores (Volleyball)**
- Set-by-set scoring (1-5 sets)
- Formatted breakdown display

#### 7. **Seat & Ticket**
- Central sales console
- Process refunds with automatic seat release
- Match-specific ticket linking

#### 8. **Event Personnel**
- Staff management with role presets
- Optional match assignments

#### 9. **Teams**
- Team rosters with auto-calculated W/L/Games
- Season standings tracking

#### 10. **Players**
- Individual profiles: jersey #, biometrics, scores
- Running statistics aggregation

#### 11. **Reports** (5 Dashboards)
- **Season Standings** - Win % sorted rankings
- **Ticket Sales** - Date-range filtered transactions  
- **Venue Utilization** - Monthly occupancy %
- **Ticket Revenue** - Revenue breakdown by seat type
- **Player Statistics** - Individual scoring leaders

---

### **CUSTOMER PORTAL** (2 Tabs)

#### 1. **Purchase Ticket** (Max 2 tickets)
**Workflow:**
1. Select event → auto-filters scheduled matches
2. Choose available seats
3. Select ticket type (prices inherited from seat)
4. Customer lookup or new entry
5. **Transaction**: Seat locked → Sale recorded → Confirmation

#### 2. **Request Refund**
**Workflow:**
1. Filter sold tickets by event
2. Select sale → provide reason/agent
3. **Validation**: Event not started yet
4. **Transaction**: Status updated → Seat freed → Audit logged

---


## Database Transactions

### Transaction 1: Scheduling a Match
- Verifies event exists and is scheduled
- Validates both teams exist
- Checks venue availability (prevents time overlap)
- Inserts match with proper constraints

### Transaction 2: Purchasing a Ticket
- Confirms match is open for ticketing
- Locks available seat with `FOR UPDATE`
- Creates/retrieves customer record
- Records sale with audit trail
- Marks seat as sold (atomic operation)

## Troubleshooting

**ClassNotFoundException: com.mysql.cj.jdbc.Driver**
- Verify `lib/mysql-connector-j-9.5.0.jar` is in classpath

**Connection Failed**
- Update credentials in `Database.java`
- Ensure MySQL server is running
- Verify `uaap_db` database exists

**Compilation Errors**
- Use correct classpath separator (`;` Windows, `:` Unix)
- Ensure all `.java` files are in project root
- Check JDK version (11+)

## Default Ticket Tiers

- **Ticket #1** - General Admission / Upper Box: ₱500
- **Ticket #2** - Lower Box: ₱750  
- **Ticket #3** - Courtside: ₱1200

Prices are inherited from seat assignments and can be customized per venue/event.

## UAAP Teams Supported

Ateneo, De La Salle, FEU, UP, UST, NU, UE, Adamson

---
