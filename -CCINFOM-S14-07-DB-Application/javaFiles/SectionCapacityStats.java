/**
 * Data class to hold capacity statistics for a venue section
 */
public class SectionCapacityStats {
    private String sectionName;
    private int totalSeats;
    private int bookedSeats;
    private int availableSeats;
    
    public SectionCapacityStats(String sectionName, int totalSeats, int bookedSeats) {
        this.sectionName = sectionName;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
        this.availableSeats = totalSeats - bookedSeats;
    }
    
    public String getSectionName() {
        return sectionName;
    }
    
    public int getTotalSeats() {
        return totalSeats;
    }
    
    public int getBookedSeats() {
        return bookedSeats;
    }
    
    public int getAvailableSeats() {
        return availableSeats;
    }
    
    public double getOccupancyPercentage() {
        if (totalSeats == 0) return 0.0;
        return (bookedSeats * 100.0) / totalSeats;
    }
    
    public String getCapacityLabel() {
        return String.format("%s: %d/%d available (%d booked)", 
            sectionName, availableSeats, totalSeats, bookedSeats);
    }
    
    @Override
    public String toString() {
        return getCapacityLabel();
    }
}
