## 📋 Table of Contents
- [Overview](#overview)
- [Team Members](#team-members)
- [Features](#features)
- [Technical Stack](#technical-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [User Portals](#user-portals)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## Overview

A comprehensive Java-based database application for managing UAAP (University Athletic Association of the Philippines) sports events. This system replaces traditional Excel spreadsheet workflows with an integrated solution for event scheduling, real-time ticketing, personnel management, and advanced analytics.

The application provides two distinct portals:
- **Manager Dashboard**: Complete administrative control over events, matches, teams, players, and personnel
- **Customer Portal**: Intuitive interface for ticket purchasing, refunds, and match information

---

## Team Members

- **SANTOS, John Benedict G.** (Team Leader)
- NAÑAWA, Alfred Brant A.
- REYES, Ivan Kenneth C.
- YU, Chrisander Jervin C.

**Course**: CCINFOM - Database Management  
**Section**: S14  
**Group**: 07

---

## Features

### Customer Chatbot
- Interactive AI-powered assistant to guide customers
- Answers frequently asked questions
- Provides real-time support for ticketing inquiries
- Helps navigate the system efficiently

### Manager Portal

#### Event & Match Management
- Create and schedule UAAP events across multiple sports
- Manage match details including date, time, and venue
- Track team participation and match outcomes
- View event history and upcoming schedules

#### Live Scoring System
- **Basketball**: Quarter-by-quarter scoring with real-time updates
- **Volleyball**: Set-by-set scoring with point tracking
- Automatic winner determination based on sport rules
- Score aggregation and final result computation

#### Ticketing System
- Real-time seat availability tracking
- Transaction-safe booking to prevent double-booking
- Multiple seat sections (VIP, Lower Box, Upper Box, General Admission)
- Dynamic pricing based on section and event
- Seat visualization with stadium layout

#### Personnel Management
- Assign event staff (ushers, referees, security, entertainers)
- Track personnel availability and assignments
- Manage staff details and roles
- Event-specific personnel allocation

#### Team & Player Management
- Maintain team rosters across all 8 UAAP universities
- Player profiles with statistics and participation tracking
- Team performance analytics
- Historical data management

#### Analytics & Reports
- **Season Standings**: Win/loss records, win percentages, ranking
- **Ticket Revenue**: Sales analysis per event and section
- **Venue Utilization**: Occupancy rates and capacity analysis
- **Player Statistics**: Individual performance metrics
- **Team Performance Charts**: Visual representation of standings
- **Revenue Trends**: Financial analytics over time

### Customer Portal

#### Ticket Purchase
- Browse upcoming events and matches
- Interactive seat selection with stadium visualization
- Real-time availability updates
- Secure transaction processing
- Instant confirmation and ticket details

#### Ticket Refund
- Submit refund requests for purchased tickets
- Automated validation and processing
- Refund audit trail for transparency
- Track refund status

#### Match Spotlight
- View upcoming matches with detailed information
- Team logos and information display
- Match schedules and venue details
- Quick access to popular events

---

## Technical Stack

| Component | Technology |
|-----------|-----------|
| **Programming Language** | Java (JDK 11+) |
| **Database** | MySQL 8.0+ |
| **GUI Framework** | Java Swing |
| **Database Connector** | MySQL Connector/J 9.5.0 |
| **UI Design** | Custom UAAPTheme with modern styling |
| **Build System** | Manual compilation via batch script |

### Key Technologies
- **JDBC**: Database connectivity and transaction management
- **Swing Components**: JFrame, JPanel, JTable, CardLayout
- **MVC Pattern**: Separation of data (DAO), logic (Service), and UI (Panels)
- **Transaction Safety**: ACID-compliant database operations

---

## Prerequisites

Before running the application, ensure you have the following installed:

1. **Java Development Kit (JDK)**
   - Version 11 or higher
   - Download from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://openjdk.java.net/)
   - Verify installation: `java -version`

2. **MySQL Server**
   - Version 8.0 or higher
   - Download from [MySQL Official Site](https://dev.mysql.com/downloads/mysql/)
   - MySQL Workbench (recommended for database management)
   - Verify installation: `mysql --version`

3. **System Requirements**
   - OS: Windows 10/11, macOS, or Linux
   - RAM: Minimum 4GB (8GB recommended)
   - Disk Space: 500MB free space

---

## Installation & Setup

### Step 1: Clone or Download the Project
```bash
cd "c:\Users\Benedict\Documents\TERM 1 AY 25'-26'\CCINFOM\DB Submission\UAAP-Database-App-main\UAAP-Database-App-main"
```

### Step 2: Database Setup

1. **Start MySQL Server**
   - Ensure MySQL service is running
   - Open MySQL Workbench or command line

2. **Create Database**
   ```sql
   CREATE DATABASE uaap_db;
   USE uaap_db;
   ```

3. **Import Schema**
   ```sql
   SOURCE CCINFOM-S14-07-FINAL-DB-Application/UAAPDB.sql;
   ```
   
   Or in MySQL Workbench:
   - Navigate to **Server** → **Data Import**
   - Select `UAAPDB.sql` file
   - Click **Start Import**

4. **Verify Tables**
   ```sql
   SHOW TABLES;
   ```
   You should see tables for: Events, Matches, Teams, Players, Tickets, Seats, etc.

### Step 3: Configure Database Connection

The application will prompt for MySQL credentials at runtime. Ensure you have:
- MySQL username (default: `root`)
- MySQL password
- Database name: `uaap_db` (created in Step 2)

---

## Running the Application

### Method 1: Using Batch Script (Windows)

1. Navigate to the application directory:
   ```cmd
   cd CCINFOM-S14-07-FINAL-DB-Application
   ```

2. Run the batch file:
   ```cmd
   runProgram.bat
   ```

3. Enter MySQL credentials when prompted:
   ```
   MySQL Username (default: root): root
   MySQL Password: ********
   ```

4. The application will:
   - Compile all Java source files
   - Connect to the database
   - Launch the main menu

### Method 2: Manual Compilation and Execution

```bash
# Navigate to application directory
cd CCINFOM-S14-07-FINAL-DB-Application

# Compile Java files
javac -d classes -cp "lib/mysql-connector-j-9.5.0.jar" javaFiles/*.java

# Run application
java -Ddb.user=root -Ddb.password=your_password -cp "classes;lib/*" UAAPApp
```

**Note**: Replace `your_password` with your actual MySQL password.

### First Launch

Upon successful launch, you'll see:
- **UAAP Main Menu** with two portal options:
  - Customer Portal
  - Manager Dashboard

---

## Project Structure

```
UAAP-Database-App-main/
├── CCINFOM-S14-07-FINAL-DB-Application/
│   ├── assets/
│   │   └── logos/              # Team logos and branding
│   ├── classes/                # Compiled .class files
│   ├── javaFiles/              # Java source code
│   │   ├── UAAPApp.java        # Main application entry point
│   │   ├── Database.java       # Database connection manager
│   │   ├── UAAPTheme.java      # UI theme and styling
│   │   │
│   │   ├── ManagerDashboardFrame.java
│   │   ├── CustomerPortalFrame.java
│   │   │
│   │   ├── *DAO.java           # Data Access Objects
│   │   ├── *Service.java       # Business logic services
│   │   ├── *Panel.java         # UI panels and components
│   │   └── *.java              # Entity classes
│   │
│   ├── lib/
│   │   └── mysql-connector-j-9.5.0.jar
│   ├── UAAPDB.sql             # Database schema and initial data
│   └── runProgram.bat         # Windows batch script
│
├── DB_Design_CCINFOM.mwb      # MySQL Workbench design file
├── README.md                   # This file
└── CCINFOM-S14-07-FINAL-DB-Application.zip
```

### Key Components

#### Entity Classes
- `Customer.java`, `Event.java`, `Match.java`, `Team.java`, `Player.java`
- `Ticket.java`, `Seat.java`, `Venue.java`, `EventPersonnel.java`
- `MatchTeam.java`, `MatchQuarterScore.java`, `MatchSetScore.java`

#### DAO (Data Access Objects)
- Database CRUD operations
- Query execution and result mapping
- Transaction management

#### Service Classes
- `TicketingService.java` - Ticket purchase and refund logic
- `MatchResultService.java` - Match outcome computation
- `ScoreAggregationService.java` - Score calculation
- `ReportService.java` - Analytics and reporting

#### UI Panels
- Manager: Event, Match, Team, Player, Personnel, Ticket, Seat, Score, Reports
- Customer: Purchase, Refund, Match Spotlight, Chatbot

---

## Database Schema

### Core Tables

| Table | Description |
|-------|-------------|
| `events` | UAAP events (Season 87, specific sports) |
| `matches` | Individual matches within events |
| `teams` | 8 UAAP university teams |
| `players` | Player roster and details |
| `match_teams` | Team participation in matches |
| `match_quarter_scores` | Basketball quarter scores |
| `match_set_scores` | Volleyball set scores |
| `venues` | Stadium/arena information |
| `seats` | Seat inventory per venue |
| `tickets` | Ticket sales and bookings |
| `customers` | Customer accounts |
| `event_personnel` | Staff assignments |
| `refund_audit` | Refund transaction history |

### Supported Sports
- Men'sBasketball
- Women's Volleyball

### Participating Universities
1. Ateneo de Manila University (ADMU)
2. De La Salle University (DLSU)
3. Far Eastern University (FEU)
4. University of the Philippines (UP)
5. University of Santo Tomas (UST)
6. National University (NU)
7. University of the East (UE)
8. Adamson University (AdU)

---

## 🖥️ User Portals

### Manager Dashboard (11 Tabs)

1. **Events** - Create and manage UAAP events
2. **Matches** - Schedule matches and assign venues
3. **Teams** - Manage team information and rosters
4. **Players** - Player profiles and statistics
5. **Match Teams** - Assign teams to matches
6. **Quarter Scores** - Record basketball scores
7. **Set Scores** - Record volleyball scores
8. **Personnel** - Assign staff to events
9. **Seats & Tickets** - Manage venue seating
10. **Record Results** - Finalize match outcomes
11. **Reports** - Analytics and insights

### Customer Portal (4 Features)

1. **Chatbot** - AI assistant for customer support
2. **Ticket Purchase** - Browse and buy tickets
3. **Refund Request** - Request ticket refunds
4. **Match Spotlight** - Upcoming match highlights



---

## License

This project is developed for academic purposes as part of the CCINFOM course at De La Salle University.

**Course**: CCINFOM - Database Management  
**Academic Year**: 2024-2025, Term 1  
**Institution**: De La Salle University - Manila

---

## Acknowledgments

- **UAAP** for inspiration and sports management context
- **Doc Raphael Gonda Wilwayco** for guidance and support
- **MySQL** for workbench database management
- **Java Swing** for rich desktop UI capabilities

---


