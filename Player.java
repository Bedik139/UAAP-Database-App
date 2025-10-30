import java.math.BigDecimal;

public class Player {

    private int playerId;
    private int teamId;
    private String teamName;
    private String firstName;
    private String lastName;
    private int playerNumber;
    private Integer age;
    private String position;
    private BigDecimal weight;
    private BigDecimal height;
    private int individualScore;

    public Player() {}

    public Player(int playerId,
                  int teamId,
                  String teamName,
                  String firstName,
                  String lastName,
                  int playerNumber,
                  Integer age,
                  String position,
                  BigDecimal weight,
                  BigDecimal height,
                  int individualScore) {
        this.playerId = playerId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.playerNumber = playerNumber;
        this.age = age;
        this.position = position;
        this.weight = weight;
        this.height = height;
        this.individualScore = individualScore;
    }

    public Player(int teamId,
                  String firstName,
                  String lastName,
                  int playerNumber,
                  Integer age,
                  String position,
                  BigDecimal weight,
                  BigDecimal height,
                  int individualScore) {
        this(0, teamId, null, firstName, lastName, playerNumber, age, position, weight, height, individualScore);
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public int getIndividualScore() {
        return individualScore;
    }

    public void setIndividualScore(int individualScore) {
        this.individualScore = individualScore;
    }
}
