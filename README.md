===============================================================================
                    UAAP EVENT MANAGEMENT SYSTEM
===============================================================================
For Our CCINFOM Course: Team Leader: SANTOS, John Benedict G. 
                                     NAÑAWA, Alfred Brant A.
                                     REYES, Ivan Kenneth C. 
                                     YU, Chrisander Jervin C.

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

1. Create Database
CREATE DATABASE uaap_db;
USE uaap_db;

2. Run Schema
Execute UAAPDB.sql to create all tables with proper constraints:

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

Run via .bat File and enter your mysql pwd and username

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

3. Match Teams
Assign home/away teams with live scoring
Team logos rendered inline

4. Quarter Scores (Basketball)
Manage quarter-by-quarter breakdown (Q1-Q4)
Auto-generated performance reports

5. Set Scores (Volleyball)
Set-by-set scoring (1-5 sets)
Formatted breakdown display

6. Seat and Ticket
Central sales console
Process refunds with automatic seat release
Match-specific ticket linking

7. Event Personnel
Staff management with role presets
Optional match assignments

8. Teams
Team rosters with auto-calculated W/L/Games
Season standings tracking

9. Players
Individual profiles: jersey number, biometrics, scores
Running statistics aggregation

10. Reports (5 Dashboards)
Season Standings - Win percentage sorted rankings
Ticket Sales - Date-range filtered transactions  
Venue Utilization - Monthly occupancy percentage
Ticket Revenue - Revenue breakdown by seat type
Player Statistics - Individual scoring leaders

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


DEFAULT TICKET TIERS
===============================================================================

Ticket 1 - General Admission / Upper Box: P500
Ticket 2 - Lower Box: P750  

Prices are inherited from seat assignments and can be customized per venue/event.

UAAP TEAMS
Ateneo, De La Salle, FEU, UP, UST, NU, UE, Adamson
