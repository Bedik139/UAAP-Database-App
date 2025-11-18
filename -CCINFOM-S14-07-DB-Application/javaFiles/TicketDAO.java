import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    public List<Ticket> getAllTickets() throws SQLException {
        List<Ticket> tickets = new ArrayList<>();

        String sql = "SELECT ticket_id, default_price, price, ticket_status FROM ticket ORDER BY ticket_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ticket ticket = new Ticket(
                        rs.getInt("ticket_id"),
                        rs.getBigDecimal("default_price"),
                        rs.getBigDecimal("price"),
                        rs.getString("ticket_status")
                );
                tickets.add(ticket);
            }
        }

        return tickets;
    }
}
