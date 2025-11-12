import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    public List<TeamStandingRow> getSeasonStandings() throws SQLException {
        String sql = "SELECT team_id, team_name, standing_wins, standing_losses, total_games_played, " +
                "CASE WHEN total_games_played = 0 THEN 0 " +
                "ELSE ROUND((standing_wins / total_games_played) * 100, 2) END AS win_percentage " +
                "FROM team ORDER BY standing_wins DESC, standing_losses ASC, team_name";

        List<TeamStandingRow> standings = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                standings.add(new TeamStandingRow(
                        rs.getInt("team_id"),
                        rs.getString("team_name"),
                        rs.getInt("standing_wins"),
                        rs.getInt("standing_losses"),
                        rs.getInt("total_games_played"),
                        rs.getBigDecimal("win_percentage")
                ));
            }
        }
        return standings;
    }

    public List<TicketSalesRow> getTicketSalesSummary(LocalDate start, LocalDate end) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.event_id, e.event_name, e.sport, e.match_date, ")
           .append("sat.match_id, SUM(CASE WHEN sat.sale_status = 'Sold' THEN sat.quantity ELSE 0 END) AS tickets_sold, ")
           .append("SUM(CASE WHEN sat.sale_status = 'Sold' THEN sat.total_price ELSE 0 END) AS revenue, ")
           .append("DATE(sat.sale_datetime) AS sale_day ")
           .append("FROM event e ")
           .append("LEFT JOIN seat_and_ticket sat ON sat.event_id = e.event_id ");

        List<Object> params = new ArrayList<>();
        if (start != null || end != null) {
            sql.append("WHERE 1 = 1 ");
            if (start != null) {
                sql.append("AND DATE(sat.sale_datetime) >= ? ");
                params.add(Date.valueOf(start));
            }
            if (end != null) {
                sql.append("AND DATE(sat.sale_datetime) <= ? ");
                params.add(Date.valueOf(end));
            }
        }

        sql.append("GROUP BY e.event_id, sat.match_id, sale_day ")
           .append("ORDER BY sale_day, e.event_name");

        List<TicketSalesRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new TicketSalesRow(
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("sport"),
                            rs.getDate("match_date"),
                            (Integer) rs.getObject("match_id"),
                            rs.getInt("tickets_sold"),
                            rs.getBigDecimal("revenue") != null ? rs.getBigDecimal("revenue") : BigDecimal.ZERO,
                            rs.getDate("sale_day")
                    ));
                }
            }
        }
        return rows;
    }

    public List<VenueUtilizationRow> getVenueUtilization(LocalDate forMonth) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.event_id, e.event_name, e.venue_address, e.match_date, ")
           .append("SUM(CASE WHEN sat.sale_status = 'Sold' THEN sat.quantity ELSE 0 END) AS seats_sold, ")
           .append("e.venue_capacity AS total_seats ")
           .append("FROM event e ")
           .append("LEFT JOIN seat_and_ticket sat ON sat.event_id = e.event_id AND sat.sale_status = 'Sold' ");

        List<Object> params = new ArrayList<>();
        if (forMonth != null) {
            LocalDate firstDay = forMonth.withDayOfMonth(1);
            LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);
            sql.append("WHERE e.match_date BETWEEN ? AND ? ");
            params.add(Date.valueOf(firstDay));
            params.add(Date.valueOf(lastDay));
        }

        sql.append("GROUP BY e.event_id, e.venue_address ")
           .append("ORDER BY e.match_date, e.event_name");

        List<VenueUtilizationRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int totalSeats = rs.getInt("total_seats");
                    int seatsSold = rs.getInt("seats_sold");
                    BigDecimal occupancy = totalSeats == 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(seatsSold)
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP);

                    rows.add(new VenueUtilizationRow(
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("venue_address"),
                            rs.getDate("match_date"),
                            seatsSold,
                            totalSeats,
                            occupancy
                    ));
                }
            }
        }
        return rows;
    }

    public List<TicketRevenueRow> getTicketRevenueSummary(LocalDate start, LocalDate end) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.event_id, e.event_name, sat.match_id, s.seat_type, ")
           .append("SUM(CASE WHEN sat.sale_status = 'Sold' THEN sat.total_price ELSE 0 END) AS revenue, ")
           .append("SUM(CASE WHEN sat.sale_status = 'Sold' THEN sat.quantity ELSE 0 END) AS tickets_sold ")
           .append("FROM seat_and_ticket sat ")
           .append("INNER JOIN event e ON sat.event_id = e.event_id ")
           .append("INNER JOIN seat s ON sat.seat_id = s.seat_id ");

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        conditions.add("sat.sale_status IN ('Sold','Refunded')");
        if (start != null) {
            conditions.add("DATE(sat.sale_datetime) >= ?");
            params.add(Date.valueOf(start));
        }
        if (end != null) {
            conditions.add("DATE(sat.sale_datetime) <= ?");
            params.add(Date.valueOf(end));
        }

        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions)).append(' ');
        }

        sql.append("GROUP BY e.event_id, sat.match_id, s.seat_type ")
           .append("ORDER BY e.event_name, s.seat_type");

        List<TicketRevenueRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new TicketRevenueRow(
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            (Integer) rs.getObject("match_id"),
                            rs.getString("seat_type"),
                            rs.getBigDecimal("revenue") != null ? rs.getBigDecimal("revenue") : BigDecimal.ZERO,
                            rs.getInt("tickets_sold")
                    ));
                }
            }
        }
        return rows;
    }

    public List<PlayerParticipationRow> getPlayerParticipationStats() throws SQLException {
        String sql = "SELECT p.player_id, p.player_first_name, p.player_last_name, t.team_name, " +
                "p.position, p.individual_score AS total_points, p.player_sport AS sport " +
                "FROM player p " +
                "INNER JOIN team t ON p.team_id = t.team_id " +
                "ORDER BY p.individual_score DESC, p.player_last_name";

        List<PlayerParticipationRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new PlayerParticipationRow(
                        rs.getInt("player_id"),
                        rs.getString("player_first_name"),
                        rs.getString("player_last_name"),
                        rs.getString("team_name"),
                        rs.getString("position"),
                        rs.getInt("total_points"),
                        rs.getString("sport")
                ));
            }
        }
        return rows;
    }

    public record TeamStandingRow(int teamId,
                                  String teamName,
                                  int wins,
                                  int losses,
                                  int gamesPlayed,
                                  BigDecimal winPercentage) {}

    public record TicketSalesRow(int eventId,
                                 String eventName,
                                 String sport,
                                 Date eventDate,
                                 Integer matchId,
                                 int ticketsSold,
                                 BigDecimal revenue,
                                 Date saleDay) {}

    public record VenueUtilizationRow(int eventId,
                                      String eventName,
                                      String venueAddress,
                                      Date eventDate,
                                      int seatsSold,
                                      int totalSeats,
                                      BigDecimal occupancyPercent) {}

    public record TicketRevenueRow(int eventId,
                                   String eventName,
                                   Integer matchId,
                                   String seatType,
                                   BigDecimal revenue,
                                   int ticketsSold) {}

    public record PlayerParticipationRow(int playerId,
                                         String firstName,
                                         String lastName,
                                         String teamName,
                                         String position,
                                         int totalPoints,
                                         String sport) {}
}
