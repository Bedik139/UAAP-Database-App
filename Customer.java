import java.sql.Date;

public class Customer {

    private int customerId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String organization;
    private Date registrationDate;
    private String preferredSport;
    private String status;
    private String paymentMethod;

    public Customer() {}

    public Customer(int customerId,
                    String firstName,
                    String lastName,
                    String phoneNumber,
                    String email,
                    String organization,
                    Date registrationDate,
                    String preferredSport,
                    String status,
                    String paymentMethod) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.organization = organization;
        this.registrationDate = registrationDate;
        this.preferredSport = preferredSport;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getPreferredSport() {
        return preferredSport;
    }

    public void setPreferredSport(String preferredSport) {
        this.preferredSport = preferredSport;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return String.format("#%d %s %s", customerId, firstName, lastName);
    }
}
