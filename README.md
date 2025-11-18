UAAP EVENT MANAGEMENT SYSTEM

PURPOSE

Replaces Excel spreadsheets with a database system for:
Event and Match Scheduling - Prevent double-booking with automated venue/time validation
Ticket Management - Real-time seat availability with transaction-safe booking
Team and Player Tracking - Live scores, standings, and individual statistics
Revenue Reporting - Comprehensive analytics on sales, occupancy, and performance

PREREQUISITES

Java JDK 11 or higher (tested with JDK 17)
MySQL 8.0 or higher (local or network)
MySQL Connector/J 9.5.0 (included in lib/)

DATABASE SETUP

1. Run Schema
Execute UAAPDB.sql which will:
- Drop and create database UAAPDBSQL
- Create all tables with proper constraints:

Core Tables:
event - Event details (sport, venue, dates, capacity, status)
match - Individual matches linked to events
match_team - Participating teams per match with scores
team - UAAP teams with standings (wins/losses/games played)
player - Player roster with biometrics and individual scores
venue - Venue information and capacities

Scoring Tables:
match_quarter_score - Basketball quarter-by-quarter breakdown
match_set_score - Volleyball set-by-set breakdown (1-5 sets)

Ticketing Tables:
seat - Physical seats with type, venue, status, pricing
ticket - Ticket tiers (General Admission P500, Lower Box P750, Courtside P1200)
seat_and_ticket - Sales transactions with auto-calculated total_price (quantity x unit_price)
customer - Customer profiles with preferred sport and payment methods
refund_audit - Refund logging and audit trail

Operations Tables:
event_personnel - Staff assignments (ushers, referees, entertainers, etc.)

2. Update Database Connection
Edit Database.java credentials:
private static final String URL = "jdbc:mysql://localhost:3306/UAAPDBSQL";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";

Note: The SQL file creates a database named UAAPDBSQL.

COMPILE AND RUN

Use the provided batch file (Windows):
compileAndRun.bat

Or manually compile from project root:

Compile (Windows)
cd javaFiles
javac -cp ".;../lib/mysql-connector-j-9.5.0.jar" -d ../classes *.java

Compile (macOS/Linux)
cd javaFiles
javac -cp ".:../lib/mysql-connector-j-9.5.0.jar" -d ../classes *.java

Run (from project root)
java -cp "classes;lib/mysql-connector-j-9.5.0.jar" UAAPApp

APPLICATION STRUCTURE

LAUNCH SCREEN
Two portals: Manager (full control) | Customer (ticket operations)

MANAGER DASHBOARD (11 Tabs)

1. Events
Create/edit events with sport presets, venue dropdowns
Status workflow: Scheduled → Active → Completed
Auto-filled venue capacity

2. Matches
Link matches to events, set type (Elimination/Semis/Finals)
Match Spotlight displays team crests

3. Match Teams
Assign home/away teams with live scoring
Team logos rendered inline

4. Match Results
Record final scores + optional summaries
Individual player point attribution
Cascading updates: match status, team standings, player totals

5. Quarter Scores (Basketball)
Manage quarter-by-quarter breakdown (Q1-Q4)
Auto-generated performance reports

6. Set Scores (Volleyball)
Set-by-set scoring (1-5 sets)
Formatted breakdown display

7. Seat and Ticket
Central sales console
Process refunds with automatic seat release
Match-specific ticket linking

8. Event Personnel
Staff management with role presets
Optional match assignments

9. Teams
Team rosters with auto-calculated W/L/Games
Season standings tracking

10. Players
Individual profiles: jersey number, biometrics, scores
Running statistics aggregation

11. Reports (5 Dashboards)
Season Standings - Win percentage sorted rankings
Ticket Sales - Date-range filtered transactions  
Venue Utilization - Monthly occupancy percentage
Ticket Revenue - Revenue breakdown by seat type
Player Statistics - Individual scoring leaders

CUSTOMER PORTAL (2 Tabs)

1. Purchase Ticket (Max 2 tickets)
Workflow:
Select event → auto-filters scheduled matches
Choose available seats
Select ticket type (prices inherited from seat)
Customer lookup or new entry
Transaction: Seat locked → Sale recorded → Confirmation

2. Request Refund
Workflow:
Filter sold tickets by event
Select sale → provide reason/agent
Validation: Event not started yet
Transaction: Status updated → Seat freed → Audit logged

DATABASE TRANSACTIONS

Transaction 1: Scheduling a Match
Verifies event exists and is scheduled
Validates both teams exist
Checks venue availability (prevents time overlap)
Inserts match with proper constraints

Transaction 2: Purchasing a Ticket
Confirms match is open for ticketing
Locks available seat with FOR UPDATE
Creates/retrieves customer record
Records sale with audit trail
Marks seat as sold (atomic operation)

VENUE CAPACITIES

Mall of Asia Arena: 15,000
Smart Araneta Coliseum: 16,000
PhilSports Arena: 8,000
Ynares Center: 10,000
Filoil EcoOil Centre: 8,000

DEFAULT TICKET TIERS

Ticket 1 - General Admission: P300
Ticket 2 - Upper Box: P500
Ticket 3 - Lower Box: P750
Ticket 4 - Courtside: P1,200
Ticket 5 - Patron: P400

Prices are inherited from seat assignments and can be customized per venue/event.

TROUBLESHOOTING

Verify lib/mysql-connector-j-9.5.0.jar is in classpath

Connection Failed
Update credentials in Database.java
Ensure MySQL server is running
Verify UAAPDBSQL database exists

Compilation Errors
Use correct classpath separator (semicolon Windows, colon Unix)
Navigate to javaFiles directory before compiling
Check JDK version (11+)

UAAP TEAMS
Ateneo, De La Salle, FEU, UP, UST, NU, UE, Adamson
