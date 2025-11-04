import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String BASE_SELECT =
            "SELECT customer_id, customer_first_name, customer_last_name, phone_number, email, " +
            "organization, registration_date, preferred_sport, customer_status, payment_method " +
            "FROM customer ";

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY customer_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        }

        return customers;
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE customer_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public Customer getCustomerByEmail(String email) throws SQLException {
        String sql = BASE_SELECT + "WHERE email = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public int insertCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customer " +
                "(customer_first_name, customer_last_name, phone_number, email, organization, " +
                "registration_date, preferred_sport, customer_status, payment_method) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getOrganization());
            Date registration = customer.getRegistrationDate();
            if (registration != null) {
                ps.setDate(6, registration);
            } else {
                ps.setDate(6, new Date(System.currentTimeMillis()));
            }
            ps.setString(7, customer.getPreferredSport());
            ps.setString(8, customer.getStatus());
            ps.setString(9, customer.getPaymentMethod());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    customer.setCustomerId(generatedId);
                    return generatedId;
                }
            }
        }

        throw new SQLException("Customer insert did not return a generated id.");
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
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
    }
}
