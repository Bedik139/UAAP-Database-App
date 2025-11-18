import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;

public class DatePickerPanel extends JPanel {
    
    public interface DateSelectionListener {
        void onDateSelected(LocalDate date);
    }
    
    private LocalDate selectedDate;
    private YearMonth currentMonth;
    private DateSelectionListener listener;
    private JLabel monthYearLabel;
    private JPanel calendarPanel;
    
    public DatePickerPanel() {
        this(LocalDate.now());
    }
    
    public DatePickerPanel(LocalDate initialDate) {
        this.selectedDate = initialDate;
        this.currentMonth = YearMonth.from(initialDate);
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(5, 5));
        setBackground(UAAPTheme.CARD_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UAAPTheme.CARD_BORDER, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Header with month/year and navigation
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Day labels (S M T W T F S)
        JPanel dayLabelsPanel = createDayLabelsPanel();
        add(dayLabelsPanel, BorderLayout.CENTER);
        
        // Calendar grid
        calendarPanel = new JPanel(new GridLayout(6, 7, 5, 5));
        calendarPanel.setBackground(UAAPTheme.CARD_BACKGROUND);
        add(calendarPanel, BorderLayout.SOUTH);
        
        updateCalendar();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UAAPTheme.CARD_BACKGROUND);
        
        // Previous month button
        JButton prevButton = new JButton("<");
        prevButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        prevButton.setFocusPainted(false);
        prevButton.setBorderPainted(false);
        prevButton.setContentAreaFilled(false);
        prevButton.setForeground(UAAPTheme.UAAP_BLUE);
        prevButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });
        
        // Next month button
        JButton nextButton = new JButton(">");
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nextButton.setFocusPainted(false);
        nextButton.setBorderPainted(false);
        nextButton.setContentAreaFilled(false);
        nextButton.setForeground(UAAPTheme.UAAP_BLUE);
        nextButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });
        
        // Month and year label
        monthYearLabel = new JLabel();
        monthYearLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        monthYearLabel.setForeground(UAAPTheme.TEXT_PRIMARY);
        monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateMonthYearLabel();
        
        panel.add(prevButton, BorderLayout.WEST);
        panel.add(monthYearLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createDayLabelsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 7, 5, 5));
        panel.setBackground(UAAPTheme.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        String[] days = {"S", "M", "T", "W", "T", "F", "S"};
        for (String day : days) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(UAAPTheme.TEXT_SECONDARY);
            panel.add(label);
        }
        
        return panel;
    }
    
    private void updateMonthYearLabel() {
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthYearLabel.setText(monthName + " " + currentMonth.getYear());
    }
    
    private void updateCalendar() {
        updateMonthYearLabel();
        calendarPanel.removeAll();
        
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        
        // Add empty cells for days before the first day of the month
        for (int i = 0; i < dayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        // Add day buttons
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton dayButton = createDayButton(date);
            calendarPanel.add(dayButton);
        }
        
        // Fill remaining cells
        int totalCells = calendarPanel.getComponentCount();
        int remainingCells = 42 - totalCells; // 6 rows * 7 columns
        for (int i = 0; i < remainingCells; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }
    
    private JButton createDayButton(LocalDate date) {
        JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Check if this is the selected date
        boolean isSelected = date.equals(selectedDate);
        boolean isToday = date.equals(LocalDate.now());
        
        if (isSelected) {
            // Green circle for selected date
            button.setBackground(UAAPTheme.UAAP_GREEN);
            button.setForeground(Color.BLACK);
            button.setOpaque(true);
            button.setBorderPainted(true);
            button.setBorder(BorderFactory.createLineBorder(UAAPTheme.UAAP_GREEN, 2));
        } else if (isToday) {
            // Blue outline for today
            button.setBackground(UAAPTheme.CARD_BACKGROUND);
            button.setForeground(UAAPTheme.UAAP_BLUE);
            button.setOpaque(true);
            button.setBorderPainted(true);
            button.setBorder(BorderFactory.createLineBorder(UAAPTheme.UAAP_BLUE, 2));
        } else {
            button.setBackground(UAAPTheme.CARD_BACKGROUND);
            button.setForeground(UAAPTheme.TEXT_PRIMARY);
            button.setOpaque(true);
        }
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!date.equals(selectedDate)) {
                    button.setBackground(UAAPTheme.HOVER_BACKGROUND);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!date.equals(selectedDate)) {
                    button.setBackground(UAAPTheme.CARD_BACKGROUND);
                }
            }
        });
        
        // Add click listener
        button.addActionListener(e -> {
            selectedDate = date;
            updateCalendar();
            if (listener != null) {
                listener.onDateSelected(date);
            }
        });
        
        return button;
    }
    
    public void setDateSelectionListener(DateSelectionListener listener) {
        this.listener = listener;
    }
    
    public LocalDate getSelectedDate() {
        return selectedDate;
    }
    
    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        this.currentMonth = YearMonth.from(date);
        updateCalendar();
    }
    
    public String getSelectedDateString() {
        return selectedDate != null ? selectedDate.toString() : "";
    }
    
    // Static method to show date picker in a popup dialog
    public static LocalDate showDatePickerDialog(Component parent, LocalDate initialDate) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Select Date", true);
        dialog.setLayout(new BorderLayout());
        
        DatePickerPanel picker = new DatePickerPanel(initialDate);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UAAPTheme.LIGHT_SURFACE);
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton todayButton = new JButton("Today");
        
        UAAPTheme.styleActionButton(okButton);
        UAAPTheme.styleNeutralButton(cancelButton);
        UAAPTheme.styleInfoButton(todayButton);
        
        final LocalDate[] result = {null};
        
        okButton.addActionListener(e -> {
            result[0] = picker.getSelectedDate();
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        todayButton.addActionListener(e -> picker.setSelectedDate(LocalDate.now()));
        
        buttonPanel.add(todayButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        
        dialog.add(picker, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        
        return result[0];
    }
}
