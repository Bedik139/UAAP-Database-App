import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();

        String sql = "SELECT customer_id, customer_first_name, customer_last_name, phone_number, email, " +
                "organization, registration_date, preferred_sport, customer_status, payment_method " +
                "FROM customer ORDER BY customer_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_first_name"),
                        rs.getString("customer_last_name"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("organization"),
                        rs.getDate("registration_date"),
                        rs.getString("preferred_sport"),
                        rs.getString("customer_status"),
                        rs.getString("payment_method")
                );
                customers.add(customer);
            }
        }

        return customers;
    }
}
