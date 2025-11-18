===============================================================================
                    UAAP EVENT MANAGEMENT SYSTEM
===============================================================================

PURPOSE
-------
Replaces Excel spreadsheets with a comprehensive database system for:

- Event and Match Scheduling: Prevent double-booking with automated 
  venue/time validation
- Ticket Management: Real-time seat availability with transaction-safe booking
- Team and Player Tracking: Live scores, standings, and individual statistics
- Revenue Reporting: Comprehensive analytics on sales, occupancy, and 
  performance


===============================================================================
PREREQUISITES
===============================================================================

1. Java JDK 11 or higher (tested with JDK 17)
2. MySQL 8.0 or higher (local or network installation)
3. MySQL Connector/J 9.5.0 (already included in lib/ folder)


===============================================================================
DATABASE SETUP
===============================================================================

STEP 1: RUN THE SQL SCHEMA
---------------------------
Execute UAAPDB.sql in MySQL Workbench or command line:
  mysql -u root -p < UAAPDB.sql

This will:
- Drop and recreate database: UAAPDBSQL
- Create all tables with proper constraints and relationships

DATABASE TABLES
---------------
Core Tables:
  - event: Event details (sport, venue, dates, capacity, status)
  - match: Individual matches linked to events
  - match_team: Participating teams per match with scores
  - team: UAAP teams with standings (wins/losses/games played)
  - player: Player roster with biometrics and individual scores
  - venue: Venue information and capacities

Scoring Tables:
  - match_quarter_score: Basketball quarter-by-quarter breakdown
  - match_set_score: Volleyball set-by-set breakdown (1-5 sets)

Ticketing Tables:
  - seat: Physical seats with type, venue, status, pricing
  - ticket: Ticket tiers with pricing
  - seat_and_ticket: Sales transactions with auto-calculated total_price
  - customer: Customer profiles with preferred sport and payment methods
  - refund_audit: Refund logging and audit trail

Operations Tables:
  - event_personnel: Staff assignments (ushers, referees, entertainers, etc.)


===============================================================================
RUNNING THE APPLICATION
===============================================================================

SIMPLE METHOD (RECOMMENDED)
----------------------------
1. Double-click runProgram.bat
2. The batch file will:
   - Check if Java is installed
   - Automatically compile all Java files
   - Prompt you for MySQL credentials:
     * MySQL Username (default: root)
     * MySQL Password
   - Launch the application with your credentials

Note: The batch file compiles and runs the application automatically. 
No need to manually edit any configuration files!


MANUAL COMPILATION (ALTERNATIVE)
---------------------------------
If you prefer to compile manually:

Windows:
  cd javaFiles
  javac -d ../classes -cp "../lib/mysql-connector-j-9.5.0.jar" *.java
  cd ..
  java -Ddb.user=YOUR_USERNAME -Ddb.password=YOUR_PASSWORD ^
       -cp "classes;lib/*" UAAPApp

macOS/Linux:
  cd javaFiles
  javac -d ../classes -cp "../lib/mysql-connector-j-9.5.0.jar" *.java
  cd ..
  java -Ddb.user=YOUR_USERNAME -Ddb.password=YOUR_PASSWORD \
       -cp "classes:lib/*" UAAPApp


===============================================================================
APPLICATION STRUCTURE
===============================================================================

LAUNCH SCREEN
-------------
Two portals available:
  1. Manager Portal - Full administrative control
  2. Customer Portal - Ticket operations only


MANAGER DASHBOARD (11 TABS)
----------------------------

1. EVENTS TAB
   - Create/edit events with sport presets and venue dropdowns
   - Status workflow: Scheduled → Active → Completed
   - Auto-filled venue capacity from database

2. MATCHES TAB
   - Link matches to events
   - Set match type: Elimination/Semifinals/Finals
   - Match Spotlight displays team crests

3. MATCH TEAMS TAB
   - Assign home/away teams with live scoring
   - Team logos rendered inline
   - Real-time score updates

4. QUARTER SCORES TAB (Basketball)
   - Manage quarter-by-quarter breakdown (Q1-Q4)
   - Auto-generated performance reports
   - Running totals and aggregation

5. SET SCORES TAB (Volleyball)
   - Set-by-set scoring (1-5 sets)
   - Formatted breakdown display
   - Set winners tracked automatically

6. SEAT AND TICKET TAB
   - Central sales console
   - Process ticket purchases with seat selection
   - Handle refunds with automatic seat release
   - Match-specific ticket linking

7. EVENT PERSONNEL TAB
   - Staff management with role presets
   - Optional match assignments
   - Track personnel by event

8. TEAMS TAB
   - Manage team rosters
   - Auto-calculated W/L/Games statistics
   - Season standings tracking

9. PLAYERS TAB
   - Individual player profiles
   - Jersey numbers, biometrics, and scores
   - Running statistics aggregation

10. CUSTOMERS TAB
    - Customer database management
    - Purchase history tracking
    - Contact information and preferences

11. REPORTS TAB (5 DASHBOARDS)
    - Season Standings: Win percentage sorted rankings
    - Ticket Sales: Date-range filtered transactions
    - Venue Utilization: Monthly occupancy percentage
    - Ticket Revenue: Revenue breakdown by seat type
    - Player Statistics: Individual scoring leaders


CUSTOMER PORTAL (3 TABS)
-------------------------

1. PURCHASE TICKET TAB
   Workflow:
   - Select event from dropdown (auto-filters scheduled matches)
   - Choose available seats from interactive seat selector
   - Select ticket type (prices inherited from seat configuration)
   - Customer lookup or new entry
   - Transaction: Seat locked → Sale recorded → Confirmation displayed
   
   Restrictions:
   - Maximum 2 tickets per transaction
   - Only available seats can be selected
   - Automatic price calculation based on quantity

2. REQUEST REFUND TAB
   Workflow:
   - Filter sold tickets by event
   - Select specific sale
   - Provide refund reason and agent information
   - Validation: Event must not have started yet
   - Transaction: Status updated → Seat freed → Audit logged

3. MATCH SPOTLIGHT TAB
   - View upcoming matches
   - Display team information with logos
   - Match details and scheduling


===============================================================================
DATABASE TRANSACTIONS
===============================================================================

TRANSACTION 1: SCHEDULING A MATCH
----------------------------------
- Verifies event exists and is in 'Scheduled' status
- Validates both participating teams exist
- Checks venue availability (prevents time overlap)
- Inserts match with proper foreign key constraints
- Atomic operation with rollback on failure

TRANSACTION 2: PURCHASING A TICKET
-----------------------------------
- Confirms match is open for ticketing (event not completed)
- Locks available seat with SELECT FOR UPDATE
- Creates or retrieves customer record
- Records sale in seat_and_ticket with audit trail
- Updates seat status to 'Sold' (atomic operation)
- Auto-calculates total_price = quantity × unit_price
- Commits only if all steps succeed

TRANSACTION 3: PROCESSING A REFUND
-----------------------------------
- Validates ticket exists and event hasn't started
- Creates refund audit record with timestamp
- Updates seat_and_ticket status to 'Refunded'
- Releases seat (status changed to 'Available')
- Logs refund reason, agent, and customer details


===============================================================================
VENUE CAPACITIES
===============================================================================

Mall of Asia Arena       : 15,000 seats
Smart Araneta Coliseum   : 16,000 seats
PhilSports Arena         :  8,000 seats
Ynares Center           : 10,000 seats
Filoil EcoOil Centre    :  8,000 seats


===============================================================================
DEFAULT TICKET TIERS
===============================================================================

Ticket 1 - General Admission : P300
Ticket 2 - Upper Box         : P500
Ticket 3 - Lower Box         : P750
Ticket 4 - Courtside         : P1,200
Ticket 5 - Patron            : P400

Note: Prices are inherited from seat assignments and can be customized 
per venue/event.


===============================================================================
UAAP TEAMS (Season 87)
===============================================================================

- Ateneo Blue Eagles
- De La Salle Green Archers
- FEU Tamaraws
- UP Fighting Maroons
- UST Growling Tigers
- NU Bulldogs
- UE Red Warriors
- Adamson Soaring Falcons

Team logos are automatically loaded from assets/logos/ directory.


===============================================================================
PROJECT STRUCTURE
===============================================================================

-CCINFOM-S14-07-DB-Application/
├── README.md                       (Markdown version)
├── runProgram.bat                  (Automated compilation and execution)
├── UAAPDB.sql                      (Database schema and setup)
├── assets/
│   └── logos/                      (Team logos and UAAP branding)
├── lib/
│   └── mysql-connector-j-9.5.0.jar (MySQL JDBC driver)
├── javaFiles/                      (All source code)
│   ├── UAAPApp.java               (Application entry point)
│   ├── Database.java              (Database connection manager)
│   ├── UAAPMainMenuFrame.java     (Main menu interface)
│   ├── ManagerDashboardFrame.java (Manager portal)
│   ├── CustomerPortalFrame.java   (Customer portal)
│   ├── *DAO.java                  (Data Access Objects)
│   ├── *Panel.java                (UI Components)
│   └── *.java                     (Models and utilities)
└── classes/                        (Compiled .class files)


===============================================================================
FEATURES HIGHLIGHTS
===============================================================================

✓ Interactive seat selection with visual stadium layout
✓ Real-time seat availability tracking
✓ Transaction-safe ticket booking (no double-booking)
✓ Automatic price calculation and revenue tracking
✓ Comprehensive refund system with audit logging
✓ Live score tracking for basketball and volleyball
✓ Season standings with automatic W/L calculation
✓ Player statistics and performance analytics
✓ Date-range filtered reports and analytics
✓ Venue utilization and occupancy reports
✓ Event personnel management and scheduling
✓ Team roster and player profile management
✓ Customizable ticket tiers and pricing
✓ Modern UI with team branding and logos


===============================================================================
TECHNICAL SPECIFICATIONS
===============================================================================

Programming Language: Java (JDK 11+)
Database: MySQL 8.0+
UI Framework: Java Swing
Architecture: MVC Pattern with DAO Layer
JDBC Driver: MySQL Connector/J 9.5.0
Transaction Management: ACID-compliant MySQL transactions
Character Encoding: UTF-8
Database Timezone: UTC


===============================================================================
DEVELOPMENT TEAM
===============================================================================

CCINFOM S14 Group 07
Database Application Development Project
Academic Year 2025-2026, Term 1


===============================================================================
END OF README
===============================================================================
