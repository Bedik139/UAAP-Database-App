# UAAP Event Management System

## Overview

A comprehensive Java-based database application for managing UAAP (University Athletic Association of the Philippines) sports events, designed to replace traditional Excel spreadsheet workflows with an integrated system for event scheduling, ticketing, and analytics.

## Team Members
- **SANTOS, John Benedict G.** (Team Leader)
- NAÑAWA, Alfred Brant A.
- REYES, Ivan Kenneth C.
- YU, Chrisander Jervin C.

## Key Features

### Manager Portal
- **Event & Match Management** - Schedule events, manage matches, and track team performance
- **Live Scoring** - Quarter-by-quarter (basketball) and set-by-set (volleyball) scoring
- **Ticketing System** - Real-time seat availability with transaction-safe booking
- **Personnel Management** - Assign staff (ushers, referees, entertainers) to events
- **Analytics & Reports** - Season standings, ticket revenue, venue utilization, and player statistics

### Customer Portal
- **Ticket Purchase** - Browse events, select seats, and complete transactions
- **Refund Requests** - Request ticket refunds with automated validation
- **Match Spotlight** - View upcoming matches with team information

## Technical Stack

- **Backend**: Java (JDK 11+)
- **Database**: MySQL 8.0+
- **UI**: Java Swing
- **Connector**: MySQL Connector/J 9.5.0 (included)

## Quick Start

1. **Setup Database**
   ```sql
   CREATE DATABASE uaap_db;
   USE uaap_db;
   ```
   
2. **Import Schema**
   - Run `UAAPDB.sql` in MySQL to create all tables

3. **Launch Application**
   - Run `runProgram.bat` and enter your MySQL credentials

## Application Structure

The system features two main portals:

1. **Manager Dashboard** - 11 administrative tabs for complete event management
2. **Customer Portal** - 3 tabs for ticket operations and match viewing

## Supported Sports
Basketball(mens), Volleyball (womens)

## Participating Teams
Ateneo, De La Salle, FEU, UP, UST, NU, UE, Adamson

