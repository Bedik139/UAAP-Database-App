import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Visual stadium seat selector with interactive seat selection
 */
public class StadiumSeatSelectorPanel extends JPanel {
    
    private static final Color AVAILABLE_SEAT_COLOR = new Color(100, 100, 100);
    private static final Color RESERVED_SEAT_COLOR = new Color(40, 40, 40);
    private static final Color SELECTED_SEAT_COLOR = new Color(220, 53, 69);
    private static final Color LOWER_BOX_HIGHLIGHT = new Color(70, 130, 180, 50);
    private static final Color UPPER_BOX_HIGHLIGHT = new Color(120, 180, 120, 50);
    
    private static final int SEAT_SIZE = 15;
    private static final int SEAT_SPACING = 3;
    
    private Event selectedEvent;
    private Seat selectedSeat;
    private Map<Rectangle, Seat> seatMap = new HashMap<>();
    private Set<Integer> reservedSeatIds = new HashSet<>();
    private Map<String, SectionCapacityStats> capacityStats = new HashMap<>();
    private SeatSelectionListener listener;
    
    private final SeatDAO seatDAO = new SeatDAO();
    private final SeatAndTicketDAO seatAndTicketDAO = new SeatAndTicketDAO();
    
    public interface SeatSelectionListener {
        void onSeatSelected(Seat seat);
    }
    
    public StadiumSeatSelectorPanel() {
        setPreferredSize(new Dimension(800, 500));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleSeatClick(e.getPoint());
            }
        });
        
        // Add tooltip support
        setToolTipText("");
    }
    
    public void setSeatSelectionListener(SeatSelectionListener listener) {
        this.listener = listener;
    }
    
    public void setEvent(Event event) {
        this.selectedEvent = event;
        this.selectedSeat = null;
        loadSeatsForEvent();
        repaint();
    }
    
    public Seat getSelectedSeat() {
        return selectedSeat;
    }
    
    public void clearSelection() {
        this.selectedSeat = null;
        repaint();
    }
    
    private void loadSeatsForEvent() {
        seatMap.clear();
        reservedSeatIds.clear();
        capacityStats.clear();
        
        if (selectedEvent == null) {
            return;
        }
        
        try {
            // Load capacity statistics for each section
            capacityStats = seatDAO.getSectionCapacityStats(
                selectedEvent.getEventId(), 
                selectedEvent.getVenueAddress()
            );
            
            // Load all available seats for the event
            List<Seat> availableSeats = seatDAO.getAvailableSeatsForEvent(
                selectedEvent.getEventId(), 
                selectedEvent.getVenueAddress()
            );
            
            // Load reserved seats for this event
            List<SeatAndTicket> reservedRecords = seatAndTicketDAO.getAllRecords();
            for (SeatAndTicket record : reservedRecords) {
                if (record.getEventId() == selectedEvent.getEventId() 
                    && "Sold".equalsIgnoreCase(record.getSaleStatus())) {
                    reservedSeatIds.add(record.getSeatId());
                }
            }
            
            // Also try to load all seats from venue (including reserved ones for display)
            List<Seat> allSeats = seatDAO.getAllSeats();
            
            // Build the seat layout
            buildSeatLayout(availableSeats, allSeats);
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void buildSeatLayout(List<Seat> availableSeats, List<Seat> allSeats) {
        seatMap.clear();
        
        if (selectedEvent == null) {
            return;
        }
        
        // Group seats by their type (e.g., "Lower Box A", "Upper Box B")
        Map<String, List<Seat>> seatsBySection = new LinkedHashMap<>();
        for (Seat seat : allSeats) {
            if (selectedEvent.getVenueAddress().equals(seat.getVenueAddress())) {
                seatsBySection.computeIfAbsent(seat.getSeatType(), k -> new ArrayList<>()).add(seat);
            }
        }
        
        // Sort sections for consistent layout: Lower, Upper, etc.
        List<String> sectionNames = new ArrayList<>(seatsBySection.keySet());
        sectionNames.sort((s1, s2) -> {
            if (s1.contains("Lower") && !s2.contains("Lower")) return -1;
            if (s2.contains("Lower") && !s1.contains("Lower")) return 1;
            if (s1.contains("Upper") && !s2.contains("Upper")) return -1;
            if (s2.contains("Upper") && !s1.contains("Upper")) return 1;
            return s1.compareTo(s2);
        });
        
        // Dynamically lay out sections
        int totalSections = sectionNames.size();
        if (totalSections == 0) return;
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int sectionWidth = (panelWidth - 40) / totalSections; // -40 for padding
        int startX = 20;
        
        for (String sectionName : sectionNames) {
            List<Seat> sectionSeats = seatsBySection.get(sectionName);
            sectionSeats.sort(Comparator.comparingInt(Seat::getSeatId));
            
            // Simple grid layout: calculate cols and rows
            int numSeats = sectionSeats.size();
            int cols = Math.max(5, (int) Math.sqrt(numSeats));
            int rows = (int) Math.ceil((double) numSeats / cols);
            
            layoutSection(sectionSeats, startX, panelHeight / 2, cols, rows, sectionName);
            startX += sectionWidth;
        }
    }
    
    private void layoutSection(List<Seat> seats, int sectionCenterX, int sectionCenterY, int cols, int rows, String sectionName) {
        int totalWidth = cols * (SEAT_SIZE + SEAT_SPACING);
        int totalHeight = rows * (SEAT_SIZE + SEAT_SPACING);
        
        int startX = sectionCenterX - totalWidth / 2;
        int startY = sectionCenterY - totalHeight / 2;
        
        int index = 0;
        for (int row = 0; row < rows && index < seats.size(); row++) {
            for (int col = 0; col < cols && index < seats.size(); col++) {
                int x = startX + col * (SEAT_SIZE + SEAT_SPACING);
                int y = startY + row * (SEAT_SIZE + SEAT_SPACING);
                
                Rectangle rect = new Rectangle(x, y, SEAT_SIZE, SEAT_SIZE);
                seatMap.put(rect, seats.get(index));
                index++;
            }
        }
    }
    
    private void drawSectionCapacityLabel(Graphics2D g2d, String sectionName, int centerX, int labelY) {
        SectionCapacityStats stats = capacityStats.get(sectionName);
        if (stats == null) return;
        
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Determine color based on occupancy
        double occupancy = stats.getOccupancyPercentage();
        Color labelColor;
        if (occupancy >= 80) {
            labelColor = new Color(220, 53, 69); // Red - nearly full
        } else if (occupancy >= 50) {
            labelColor = new Color(255, 193, 7); // Yellow - filling up
        } else {
            labelColor = new Color(40, 167, 69); // Green - plenty available
        }
        
        String label = String.format("%s: %d/%d available", 
            sectionName, stats.getAvailableSeats(), stats.getTotalSeats());
        
        int labelWidth = fm.stringWidth(label);
        int labelX = centerX - labelWidth / 2;
        
        // Draw background
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(labelX - 5, labelY - fm.getAscent() - 2, labelWidth + 10, fm.getHeight() + 4, 5, 5);
        
        // Draw text
        g2d.setColor(labelColor);
        g2d.drawString(label, labelX, labelY);
        
        // Draw second line with booking info
        String bookingInfo = String.format("(%d booked)", stats.getBookedSeats());
        int bookingWidth = fm.stringWidth(bookingInfo);
        int bookingX = centerX - bookingWidth / 2;
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.GRAY);
        g2d.drawString(bookingInfo, bookingX, labelY + fm.getHeight());
    }
    
    private void layoutCenterSection(List<Seat> seats, int centerX, int centerY, int cols, int rows) {
        layoutSection(seats, centerX, centerY, cols, rows, "Lower Box");
    }
    
    private void layoutLeftSection(List<Seat> seats, int startX, int startY, int cols, int rows) {
        layoutSection(seats, startX + (cols * (SEAT_SIZE + SEAT_SPACING)) / 2, startY, cols, rows, "Upper Box Left");
    }
    
    private void layoutRightSection(List<Seat> seats, int startX, int startY, int cols, int rows) {
        layoutSection(seats, startX + (cols * (SEAT_SIZE + SEAT_SPACING)) / 2, startY, cols, rows, "Upper Box Right");
    }
    
    private void handleSeatClick(Point point) {
        for (Map.Entry<Rectangle, Seat> entry : seatMap.entrySet()) {
            if (entry.getKey().contains(point)) {
                Seat clickedSeat = entry.getValue();
                
                // Check if seat is available
                if (reservedSeatIds.contains(clickedSeat.getSeatId())) {
                    JOptionPane.showMessageDialog(this, 
                        "This seat is already reserved.", 
                        "Seat Unavailable", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (!clickedSeat.isAvailable()) {
                    JOptionPane.showMessageDialog(this, 
                        "This seat is not available for selection.", 
                        "Seat Unavailable", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Select the seat
                selectedSeat = clickedSeat;
                repaint();
                
                // Notify listener
                if (listener != null) {
                    listener.onSeatSelected(clickedSeat);
                }
                return;
            }
        }
    }
    
    @Override
    public String getToolTipText(MouseEvent event) {
        Point point = event.getPoint();
        for (Map.Entry<Rectangle, Seat> entry : seatMap.entrySet()) {
            if (entry.getKey().contains(point)) {
                Seat seat = entry.getValue();
                boolean isReserved = reservedSeatIds.contains(seat.getSeatId());
                String status = isReserved ? "Reserved" : 
                               (seat.isAvailable() ? "Available" : "Unavailable");
                return String.format("<html>Seat #%d<br>%s<br>%s<br>Price: PHP %s</html>",
                    seat.getSeatId(),
                    seat.getSeatType(),
                    status,
                    seat.getTicketPrice() != null ? seat.getTicketPrice().toPlainString() : "N/A");
            }
        }
        return null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (selectedEvent == null) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            String msg = "Select an event to view available seats";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
            return;
        }
        
        // Draw court indicator
        int courtWidth = 370;
        int courtHeight = 40;
        int courtX = (getWidth() - courtWidth) / 2;
        int courtY = 30;
        
        g2d.setColor(new Color(34, 139, 34)); // Green for court
        g2d.fillRect(courtX, courtY, courtWidth, courtHeight);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String courtText = "COURT";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(courtText);
        g2d.drawString(courtText, courtX + (courtWidth - textWidth) / 2, courtY + 25);
        
        // Draw legend
        drawLegend(g2d);
        
        // Draw all seats and collect section positions for labels
        Map<String, Integer> sectionCenters = new HashMap<>();
        Map<String, List<Rectangle>> seatsBySection = new HashMap<>();
        
        for (Map.Entry<Rectangle, Seat> entry : seatMap.entrySet()) {
            Rectangle rect = entry.getKey();
            Seat seat = entry.getValue();
            
            // Group seats by section for later label drawing
            seatsBySection.computeIfAbsent(seat.getSeatType(), k -> new ArrayList<>()).add(rect);
            
            boolean isReserved = reservedSeatIds.contains(seat.getSeatId());
            boolean isSelected = selectedSeat != null && selectedSeat.getSeatId() == seat.getSeatId();
            boolean isAvailable = seat.isAvailable() && !isReserved;
            
            // Determine seat color
            Color seatColor;
            if (isSelected) {
                seatColor = SELECTED_SEAT_COLOR;
            } else if (isReserved || !seat.isAvailable()) {
                seatColor = RESERVED_SEAT_COLOR;
            } else {
                seatColor = AVAILABLE_SEAT_COLOR;
            }
            
            // Draw seat
            g2d.setColor(seatColor);
            g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 4, 4);
            
            // Add border for selected seat
            if (isSelected) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 4, 4);
                g2d.setStroke(new BasicStroke(1));
            }
        }
        
        // Draw capacity labels for each section
        for (Map.Entry<String, List<Rectangle>> entry : seatsBySection.entrySet()) {
            String sectionName = entry.getKey();
            List<Rectangle> sectionRects = entry.getValue();
            
            if (!sectionRects.isEmpty()) {
                // Calculate center position of section
                int minX = sectionRects.stream().mapToInt(r -> r.x).min().orElse(0);
                int maxX = sectionRects.stream().mapToInt(r -> r.x + r.width).max().orElse(0);
                int minY = sectionRects.stream().mapToInt(r -> r.y).min().orElse(0);
                
                int centerX = (minX + maxX) / 2;
                int labelY = minY - 15; // Position above the section
                
                drawSectionCapacityLabel(g2d, sectionName, centerX, labelY);
            }
        }
        
    }
    
    private void drawLegend(Graphics2D g2d) {
        int legendX = 10;
        int legendY = getHeight() - 80;
        int boxSize = 14;
        int spacing = 10;
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Available seat
        g2d.setColor(AVAILABLE_SEAT_COLOR);
        g2d.fillRoundRect(legendX, legendY, boxSize, boxSize, 4, 4);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Available seat", legendX + boxSize + spacing, legendY + boxSize - 2);
        
        // Reserved seat
        legendY += boxSize + spacing;
        g2d.setColor(RESERVED_SEAT_COLOR);
        g2d.fillRoundRect(legendX, legendY, boxSize, boxSize, 4, 4);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Reserved seat", legendX + boxSize + spacing, legendY + boxSize - 2);
        
        // Selected seat
        legendY += boxSize + spacing;
        g2d.setColor(SELECTED_SEAT_COLOR);
        g2d.fillRoundRect(legendX, legendY, boxSize, boxSize, 4, 4);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Selected seat", legendX + boxSize + spacing, legendY + boxSize - 2);
    }
}
