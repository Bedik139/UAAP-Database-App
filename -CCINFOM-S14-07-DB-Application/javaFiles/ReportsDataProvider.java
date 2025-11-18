import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Provides report data to the Reports AI Assistant
 * Feeds comprehensive analytics data for intelligent insights and predictions
 */
public class ReportsDataProvider {
    
    private static final ReportService reportService = new ReportService();
    
    /**
     * Get comprehensive report context for AI analysis
     */
    public static Map<String, Object> getReportsContext() {
        Map<String, Object> context = new HashMap<>();
        
        try {
            // Get all report data
            context.put("team_standings_basketball", getTeamStandingsBySport("Basketball"));
            context.put("team_standings_volleyball", getTeamStandingsBySport("Volleyball"));
            context.put("recent_ticket_sales", getRecentTicketSales());
            context.put("venue_utilization", getRecentVenueUtilization());
            context.put("revenue_summary", getRevenueSummary());
            context.put("top_players", getTopPlayers());
            context.put("overall_stats", getOverallStats());
            
        } catch (Exception e) {
            System.err.println("Error fetching reports context: " + e.getMessage());
        }
        
        return context;
    }
    
    /**
     * Get team standings by sport
     */
    private static List<Map<String, Object>> getTeamStandingsBySport(String sport) throws SQLException {
        List<Map<String, Object>> standings = new ArrayList<>();
        List<ReportService.TeamStandingRow> rows = reportService.getSeasonStandingsBySport(sport);
        
        for (ReportService.TeamStandingRow row : rows) {
            Map<String, Object> team = new HashMap<>();
            team.put("team_name", row.teamName());
            team.put("wins", row.wins());
            team.put("losses", row.losses());
            team.put("games_played", row.gamesPlayed());
            team.put("win_percentage", row.winPercentage().doubleValue());
            standings.add(team);
        }
        
        return standings;
    }
    
    /**
     * Get recent ticket sales (last 30 days)
     */
    private static List<Map<String, Object>> getRecentTicketSales() throws SQLException {
        List<Map<String, Object>> sales = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        List<ReportService.TicketSalesRow> rows = reportService.getTicketSalesSummary(startDate, endDate);
        
        for (ReportService.TicketSalesRow row : rows) {
            Map<String, Object> sale = new HashMap<>();
            sale.put("event_name", row.eventName());
            sale.put("sport", row.sport());
            sale.put("event_date", row.eventDate().toString());
            sale.put("tickets_sold", row.ticketsSold());
            sale.put("revenue", row.revenue().doubleValue());
            sale.put("sale_day", row.saleDay() != null ? row.saleDay().toString() : "N/A");
            sales.add(sale);
        }
        
        return sales;
    }
    
    /**
     * Get recent venue utilization (last 30 days)
     */
    private static List<Map<String, Object>> getRecentVenueUtilization() throws SQLException {
        List<Map<String, Object>> utilization = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now();
        
        List<ReportService.VenueUtilizationRow> rows = reportService.getVenueUtilization(currentMonth);
        
        for (ReportService.VenueUtilizationRow row : rows) {
            Map<String, Object> venue = new HashMap<>();
            venue.put("event_name", row.eventName());
            venue.put("venue", row.venueAddress());
            venue.put("event_date", row.eventDate().toString());
            venue.put("seats_sold", row.seatsSold());
            venue.put("total_seats", row.totalSeats());
            venue.put("occupancy_percent", row.occupancyPercent().doubleValue());
            utilization.add(venue);
        }
        
        return utilization;
    }
    
    /**
     * Get revenue summary (last 30 days)
     */
    private static List<Map<String, Object>> getRevenueSummary() throws SQLException {
        List<Map<String, Object>> revenue = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        List<ReportService.TicketRevenueRow> rows = reportService.getTicketRevenueSummary(startDate, endDate);
        
        // Aggregate by seat type
        Map<String, Double> revenueByType = new HashMap<>();
        Map<String, Integer> ticketsByType = new HashMap<>();
        
        for (ReportService.TicketRevenueRow row : rows) {
            String seatType = row.seatType();
            double rev = row.revenue().doubleValue();
            int tickets = row.ticketsSold();
            
            revenueByType.put(seatType, revenueByType.getOrDefault(seatType, 0.0) + rev);
            ticketsByType.put(seatType, ticketsByType.getOrDefault(seatType, 0) + tickets);
        }
        
        for (String seatType : revenueByType.keySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("seat_type", seatType);
            item.put("revenue", revenueByType.get(seatType));
            item.put("tickets_sold", ticketsByType.get(seatType));
            revenue.add(item);
        }
        
        return revenue;
    }
    
    /**
     * Get top players
     */
    private static List<Map<String, Object>> getTopPlayers() throws SQLException {
        List<Map<String, Object>> players = new ArrayList<>();
        List<ReportService.PlayerParticipationRow> rows = reportService.getPlayerParticipationStats();
        
        // Get top 10
        for (int i = 0; i < Math.min(10, rows.size()); i++) {
            ReportService.PlayerParticipationRow row = rows.get(i);
            Map<String, Object> player = new HashMap<>();
            player.put("name", row.firstName() + " " + row.lastName());
            player.put("team", row.teamName());
            player.put("sport", row.sport());
            player.put("position", row.position());
            player.put("total_points", row.totalPoints());
            players.add(player);
        }
        
        return players;
    }
    
    /**
     * Get overall statistics
     */
    private static Map<String, Object> getOverallStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM team) as total_teams, " +
                    "(SELECT COUNT(*) FROM event WHERE event_status = 'Completed') as completed_events, " +
                    "(SELECT SUM(CASE WHEN sale_status = 'Sold' THEN quantity ELSE 0 END) FROM seat_and_ticket) as total_tickets_sold, " +
                    "(SELECT SUM(CASE WHEN sale_status = 'Sold' THEN total_price ELSE 0 END) FROM seat_and_ticket) as total_revenue";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                stats.put("total_teams", rs.getInt("total_teams"));
                stats.put("completed_events", rs.getInt("completed_events"));
                stats.put("total_tickets_sold", rs.getInt("total_tickets_sold"));
                stats.put("total_revenue", rs.getDouble("total_revenue"));
            }
        }
        
        return stats;
    }
    
    /**
     * Convert context to JSON string for Python LLM
     */
    public static String contextToJson(Map<String, Object> context) {
        StringBuilder json = new StringBuilder("{");
        
        try {
            // Team Standings - Basketball
            json.append("\"team_standings_basketball\":[");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> basketballTeams = (List<Map<String, Object>>) context.get("team_standings_basketball");
            if (basketballTeams != null) {
                for (int i = 0; i < basketballTeams.size(); i++) {
                    if (i > 0) json.append(",");
                    Map<String, Object> team = basketballTeams.get(i);
                    json.append("{");
                    json.append("\"team\":\"").append(escapeJson((String) team.get("team_name"))).append("\",");
                    json.append("\"wins\":").append(team.get("wins")).append(",");
                    json.append("\"losses\":").append(team.get("losses")).append(",");
                    json.append("\"win_pct\":").append(team.get("win_percentage"));
                    json.append("}");
                }
            }
            json.append("],");
            
            // Team Standings - Volleyball
            json.append("\"team_standings_volleyball\":[");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> volleyballTeams = (List<Map<String, Object>>) context.get("team_standings_volleyball");
            if (volleyballTeams != null) {
                for (int i = 0; i < volleyballTeams.size(); i++) {
                    if (i > 0) json.append(",");
                    Map<String, Object> team = volleyballTeams.get(i);
                    json.append("{");
                    json.append("\"team\":\"").append(escapeJson((String) team.get("team_name"))).append("\",");
                    json.append("\"wins\":").append(team.get("wins")).append(",");
                    json.append("\"losses\":").append(team.get("losses")).append(",");
                    json.append("\"win_pct\":").append(team.get("win_percentage"));
                    json.append("}");
                }
            }
            json.append("],");
            
            // Recent Sales
            json.append("\"recent_sales\":[");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sales = (List<Map<String, Object>>) context.get("recent_ticket_sales");
            if (sales != null) {
                for (int i = 0; i < Math.min(sales.size(), 10); i++) {
                    if (i > 0) json.append(",");
                    Map<String, Object> sale = sales.get(i);
                    json.append("{");
                    json.append("\"event\":\"").append(escapeJson((String) sale.get("event_name"))).append("\",");
                    json.append("\"sport\":\"").append(escapeJson((String) sale.get("sport"))).append("\",");
                    json.append("\"tickets\":").append(sale.get("tickets_sold")).append(",");
                    json.append("\"revenue\":").append(sale.get("revenue"));
                    json.append("}");
                }
            }
            json.append("],");
            
            // Venue Utilization
            json.append("\"venue_utilization\":[");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> venues = (List<Map<String, Object>>) context.get("venue_utilization");
            if (venues != null) {
                for (int i = 0; i < Math.min(venues.size(), 10); i++) {
                    if (i > 0) json.append(",");
                    Map<String, Object> venue = venues.get(i);
                    json.append("{");
                    json.append("\"event\":\"").append(escapeJson((String) venue.get("event_name"))).append("\",");
                    json.append("\"venue\":\"").append(escapeJson((String) venue.get("venue"))).append("\",");
                    json.append("\"occupancy\":").append(venue.get("occupancy_percent"));
                    json.append("}");
                }
            }
            json.append("],");
            
            // Revenue by Seat Type
            json.append("\"revenue_by_type\":[");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> revenue = (List<Map<String, Object>>) context.get("revenue_summary");
            if (revenue != null) {
                for (int i = 0; i < revenue.size(); i++) {
                    if (i > 0) json.append(",");
                    Map<String, Object> item = revenue.get(i);
                    json.append("{");
                    json.append("\"seat_type\":\"").append(escapeJson((String) item.get("seat_type"))).append("\",");
                    json.append("\"revenue\":").append(item.get("revenue")).append(",");
                    json.append("\"tickets\":").append(item.get("tickets_sold"));
                    json.append("}");
                }
            }
            json.append("],");
            
            // Top Players
            json.append("\"top_players\":[");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> players = (List<Map<String, Object>>) context.get("top_players");
            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    if (i > 0) json.append(",");
                    Map<String, Object> player = players.get(i);
                    json.append("{");
                    json.append("\"name\":\"").append(escapeJson((String) player.get("name"))).append("\",");
                    json.append("\"team\":\"").append(escapeJson((String) player.get("team"))).append("\",");
                    json.append("\"sport\":\"").append(escapeJson((String) player.get("sport"))).append("\",");
                    json.append("\"points\":").append(player.get("total_points"));
                    json.append("}");
                }
            }
            json.append("],");
            
            // Overall Stats
            json.append("\"overall_stats\":");
            @SuppressWarnings("unchecked")
            Map<String, Object> overallStats = (Map<String, Object>) context.get("overall_stats");
            if (overallStats != null) {
                json.append("{");
                json.append("\"total_teams\":").append(overallStats.get("total_teams")).append(",");
                json.append("\"completed_events\":").append(overallStats.get("completed_events")).append(",");
                json.append("\"total_tickets_sold\":").append(overallStats.get("total_tickets_sold")).append(",");
                json.append("\"total_revenue\":").append(overallStats.get("total_revenue"));
                json.append("}");
            } else {
                json.append("{}");
            }
            
            json.append("}");
            
        } catch (Exception e) {
            System.err.println("Error converting reports context to JSON: " + e.getMessage());
            e.printStackTrace();
            return "{}";
        }
        
        return json.toString();
    }
    
    /**
     * Escape special characters for JSON
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
