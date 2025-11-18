import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();

    public ReportsPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Season Standings", buildSeasonStandingsTab());
        tabs.addTab("Ticket Sales", buildTicketSalesTab());
        tabs.addTab("Venue Utilization", buildVenueUtilizationTab());
        tabs.addTab("Ticket Revenue", buildRevenueTab());
        tabs.addTab("Player Statistics", buildPlayerStatsTab());
        add(tabs, BorderLayout.CENTER);
    }
    

    private JPanel buildSeasonStandingsTab() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UAAPTheme.LIGHT_SURFACE);

        // Create nested tabs for different sports
        JTabbedPane sportTabs = new JTabbedPane();
        UAAPTheme.styleTabPane(sportTabs);
        
        sportTabs.addTab(" Men's Basketball", buildStandingsBySport("Basketball"));
        sportTabs.addTab(" Women's Volleyball", buildStandingsBySport("Volleyball"));
        mainPanel.add(sportTabs, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel buildStandingsBySport(String sportFilter) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UAAPTheme.LIGHT_SURFACE);

        // KPI Panel at the top
        JPanel kpiPanel = createKPIPanel();
        mainPanel.add(kpiPanel, BorderLayout.NORTH);

        // Split panel for table and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        String title = sportFilter != null ? sportFilter + " Standings" : "All Teams Standings";
        tablePanel.setBorder(BorderFactory.createTitledBorder(title));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Rank", "Team", "Wins", "Losses", "Games", "Win %"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UAAPTheme.styleTable(table);

        JButton refresh = new JButton("Refresh");
        UAAPTheme.styleInfoButton(refresh);
        refresh.addActionListener(e -> {
            loadStandingsData(model, kpiPanel, sportFilter);
        });

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(refresh);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(controlPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);

        // Chart panel
        JPanel chartPanel = createWinPercentageChartPanel(sportFilter);
        splitPane.setRightComponent(chartPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Load initial data
        loadStandingsData(model, kpiPanel, sportFilter);

        return mainPanel;
    }

    private JPanel createKPIPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // These will be updated with actual data
        panel.add(createKPICard("Total Teams", "8", UAAPTheme.PRIMARY_GREEN));
        panel.add(createKPICard("Total Games", "0", UAAPTheme.ACCENT_BLUE));
        panel.add(createKPICard("Top Team", "Loading...", UAAPTheme.SUCCESS));

        return panel;
    }

    private JPanel createKPICard(String label, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UAAPTheme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(UAAPTheme.TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void loadStandingsData(DefaultTableModel model, JPanel kpiPanel, String sportFilter) {
        model.setRowCount(0);
        try {
            List<ReportService.TeamStandingRow> rows = reportService.getSeasonStandingsBySport(sportFilter);
            
            int totalGames = 0;
            double totalWinPct = 0.0;
            String topTeam = "";
            double maxWinPct = 0.0;

            int rank = 1;
            for (ReportService.TeamStandingRow row : rows) {
                model.addRow(new Object[]{
                        rank++,
                        row.teamName(),
                        row.wins(),
                        row.losses(),
                        row.gamesPlayed(),
                        row.winPercentage() + "%"
                });
                
                totalGames += row.gamesPlayed();
                double winPct = row.winPercentage().doubleValue();
                totalWinPct += winPct;
                
                if (winPct > maxWinPct) {
                    maxWinPct = winPct;
                    topTeam = row.teamName();
                }
            }

            // Update KPI cards
            if (kpiPanel.getComponentCount() >= 3) {
                JPanel card1 = (JPanel) kpiPanel.getComponent(0);
                JLabel val1 = (JLabel) card1.getComponent(1);
                val1.setText(String.valueOf(rows.size()));

                JPanel card2 = (JPanel) kpiPanel.getComponent(1);
                JLabel val2 = (JLabel) card2.getComponent(1);
                val2.setText(String.valueOf(totalGames));

                JPanel card3 = (JPanel) kpiPanel.getComponent(2);
                JLabel val3 = (JLabel) card3.getComponent(1);
                val3.setText(topTeam.length() > 15 ? topTeam.substring(0, 15) + "..." : topTeam);
            }

        } catch (Exception ex) {
            showError("Unable to load standings:\n" + ex.getMessage());
        }
    }

    private JPanel createWinPercentageChartPanel(String sportFilter) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Win Percentage Chart"));

        WinPercentageChart chart = new WinPercentageChart(sportFilter);
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    // Custom component for win percentage visualization
    private class WinPercentageChart extends JPanel {
        private List<ReportService.TeamStandingRow> data;
        private String sportFilter;

        public WinPercentageChart(String sportFilter) {
            this.sportFilter = sportFilter;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 500));
            loadData();
        }

        private void loadData() {
            try {
                data = reportService.getSeasonStandingsBySport(sportFilter);
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (data == null || data.isEmpty()) {
                g.setColor(UAAPTheme.TEXT_SECONDARY);
                g.drawString("No data available", 50, 50);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            int barHeight = 30;
            int spacing = 10;

            // Title
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2d.setColor(UAAPTheme.TEXT_PRIMARY);
            g2d.drawString("Team Win Percentages", padding, 25);

            // Draw bars
            int y = padding + 20;
            int maxBarWidth = width - (2 * padding) - 100;

            for (int i = 0; i < Math.min(data.size(), 8); i++) {
                ReportService.TeamStandingRow row = data.get(i);
                double winPct = row.winPercentage().doubleValue();
                int barWidth = (int) (maxBarWidth * winPct / 100.0);

                // Draw bar background
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(padding, y, maxBarWidth, barHeight, 5, 5);

                // Draw actual bar
                Color barColor = getBarColor(i);
                g2d.setColor(barColor);
                g2d.fillRoundRect(padding, y, barWidth, barHeight, 5, 5);

                // Draw border
                g2d.setColor(UAAPTheme.CARD_BORDER);
                g2d.drawRoundRect(padding, y, maxBarWidth, barHeight, 5, 5);

                // Draw team name
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String teamName = row.teamName();
                if (teamName.length() > 25) {
                    teamName = teamName.substring(0, 22) + "...";
                }
                g2d.drawString(teamName, padding + 5, y + barHeight - 10);

                // Draw percentage
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("%.1f%%", winPct), maxBarWidth + padding + 10, y + barHeight - 10);

                y += barHeight + spacing;
            }
        }

        private Color getBarColor(int index) {
            Color[] colors = {
                    UAAPTheme.PRIMARY_GREEN,
                    UAAPTheme.ACCENT_BLUE,
                    UAAPTheme.ACCENT_GOLD,
                    UAAPTheme.SUCCESS,
                    UAAPTheme.INFO,
                    new Color(156, 39, 176),
                    new Color(255, 152, 0),
                    new Color(121, 85, 72)
            };
            return colors[index % colors.length];
        }
    }

    private JPanel buildTicketSalesTab() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Sale Day", "Event", "Sport", "Match ID", "Tickets Sold", "Revenue"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UAAPTheme.styleTable(table);

        JTextField fromField = new JTextField(10);
        fromField.setEditable(false);
        fromField.setToolTipText("Click the calendar button to select a date");
        UAAPTheme.styleTextField(fromField);
        
        JTextField toField = new JTextField(10);
        toField.setEditable(false);
        toField.setToolTipText("Click the calendar button to select a date");
        UAAPTheme.styleTextField(toField);

        JButton fromDateButton = new JButton("...");
        fromDateButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        fromDateButton.setToolTipText("Select From Date");
        UAAPTheme.styleInfoButton(fromDateButton);
        fromDateButton.addActionListener(e -> {
            LocalDate initialDate = LocalDate.now();
            String currentText = fromField.getText().trim();
            if (!currentText.isEmpty()) {
                try {
                    initialDate = LocalDate.parse(currentText);
                } catch (Exception ex) {
                    // Use current date if parse fails
                }
            }
            LocalDate selectedDate = DatePickerPanel.showDatePickerDialog(this, initialDate);
            if (selectedDate != null) {
                fromField.setText(selectedDate.toString());
            }
        });

        JButton toDateButton = new JButton("...");
        toDateButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        toDateButton.setToolTipText("Select To Date");
        UAAPTheme.styleInfoButton(toDateButton);
        toDateButton.addActionListener(e -> {
            LocalDate initialDate = LocalDate.now();
            String currentText = toField.getText().trim();
            if (!currentText.isEmpty()) {
                try {
                    initialDate = LocalDate.parse(currentText);
                } catch (Exception ex) {
                    // Use current date if parse fails
                }
            }
            LocalDate selectedDate = DatePickerPanel.showDatePickerDialog(this, initialDate);
            if (selectedDate != null) {
                toField.setText(selectedDate.toString());
            }
        });

        JButton refresh = new JButton("Generate");
        UAAPTheme.styleActionButton(refresh);
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                LocalDate from = parseDate(fromField.getText());
                LocalDate to = parseDate(toField.getText());
                List<ReportService.TicketSalesRow> rows = reportService.getTicketSalesSummary(from, to);
                for (ReportService.TicketSalesRow row : rows) {
                    model.addRow(new Object[]{
                            row.saleDay(),
                            row.eventName(),
                            row.sport(),
                            row.matchId() != null ? row.matchId() : "All",
                            row.ticketsSold(),
                            "₱" + row.revenue()
                    });
                }
            } catch (Exception ex) {
                showError("Unable to build ticket sales report:\n" + ex.getMessage());
            }
        });

        JPanel filter = new JPanel(new GridBagLayout());
        filter.setBorder(BorderFactory.createTitledBorder("Filters"));
        addFilterFieldWithButton(filter, 0, "From Date", fromField, fromDateButton);
        addFilterFieldWithButton(filter, 1, "To Date", toField, toDateButton);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(6, 2, 6, 2);
        gbc.anchor = GridBagConstraints.EAST;
        filter.add(refresh, gbc);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filter, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildVenueUtilizationTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UAAPTheme.LIGHT_SURFACE);

        // Split panel for table and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.65);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Venue Utilization Table"));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Event", "Venue", "Event Date", "Seats Sold", "Capacity", "Occupancy %"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UAAPTheme.styleTable(table);

        JTextField monthField = new JTextField(7);
        monthField.setToolTipText("Format: YYYY-MM");

        VenueOccupancyChart chartComponent = new VenueOccupancyChart();

        JButton refresh = new JButton("Generate");
        UAAPTheme.styleActionButton(refresh);
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                LocalDate month = parseYearMonth(monthField.getText());
                List<ReportService.VenueUtilizationRow> rows = reportService.getVenueUtilization(month);
                for (ReportService.VenueUtilizationRow row : rows) {
                    model.addRow(new Object[]{
                            row.eventName(),
                            row.venueAddress(),
                            row.eventDate(),
                            row.seatsSold(),
                            row.totalSeats(),
                            row.occupancyPercent() + "%"
                    });
                }
                // Update chart
                chartComponent.updateData(rows);
            } catch (Exception ex) {
                showError("Unable to build venue utilization report:\n" + ex.getMessage());
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filter.setBorder(BorderFactory.createTitledBorder("Filters"));
        filter.add(new JLabel("Month (YYYY-MM):"));
        filter.add(monthField);
        filter.add(refresh);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(filter, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(tableContainer, BorderLayout.CENTER);

        splitPane.setLeftComponent(tablePanel);

        // Chart panel
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Venue Occupancy Chart"));
        chartPanel.add(chartComponent, BorderLayout.CENTER);

        splitPane.setRightComponent(chartPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    // Custom component for venue occupancy visualization
    private class VenueOccupancyChart extends JPanel {
        private List<ReportService.VenueUtilizationRow> data;

        public VenueOccupancyChart() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 500));
        }

        public void updateData(List<ReportService.VenueUtilizationRow> newData) {
            this.data = newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (data == null || data.isEmpty()) {
                g.setColor(UAAPTheme.TEXT_SECONDARY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String msg = "Select a month and click Generate to view data";
                FontMetrics fm = g.getFontMetrics();
                int msgWidth = fm.stringWidth(msg);
                g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            int barHeight = 45;
            int spacing = 15;
            int labelWidth = 180;

            // Title
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2d.setColor(UAAPTheme.TEXT_PRIMARY);
            g2d.drawString("Venue Occupancy Rates", padding, 25);

            // Draw bars
            int y = padding + 30;
            int barStartX = padding + labelWidth + 10;
            int maxBarWidth = width - barStartX - padding - 80;

            for (int i = 0; i < Math.min(data.size(), 8); i++) {
                ReportService.VenueUtilizationRow row = data.get(i);
                double occupancy = row.occupancyPercent().doubleValue();
                int barWidth = (int) (maxBarWidth * occupancy / 100.0);

                // Draw event name (left side)
                g2d.setColor(UAAPTheme.TEXT_PRIMARY);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String eventName = row.eventName();
                if (eventName.length() > 25) {
                    eventName = eventName.substring(0, 22) + "...";
                }
                g2d.drawString(eventName, padding, y + 12);

                // Draw venue name (left side, below event)
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.setColor(UAAPTheme.TEXT_SECONDARY);
                String venueName = row.venueAddress();
                if (venueName.length() > 25) {
                    venueName = venueName.substring(0, 22) + "...";
                }
                g2d.drawString(venueName, padding, y + 26);

                // Draw bar background
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(barStartX, y + 5, maxBarWidth, barHeight - 15, 8, 8);

                // Draw actual bar with color based on occupancy
                Color barColor = getOccupancyColor(occupancy);
                g2d.setColor(barColor);
                g2d.fillRoundRect(barStartX, y + 5, barWidth, barHeight - 15, 8, 8);

                // Draw border
                g2d.setColor(UAAPTheme.CARD_BORDER);
                g2d.drawRoundRect(barStartX, y + 5, maxBarWidth, barHeight - 15, 8, 8);

                // Draw percentage inside or next to the bar
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (barWidth > 50) {
                    // Draw inside the bar if it's wide enough
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(String.format("%.2f%%", occupancy), barStartX + 8, y + 22);
                } else {
                    // Draw outside the bar if it's too narrow
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(String.format("%.2f%%", occupancy), barStartX + maxBarWidth + 8, y + 22);
                }

                // Draw occupancy info on the right
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2d.setColor(UAAPTheme.TEXT_SECONDARY);
                String info = String.format("%d/%d seats", row.seatsSold(), row.totalSeats());
                g2d.drawString(info, barStartX + maxBarWidth + 8, y + 35);

                y += barHeight + spacing;
            }
        }

        private Color getOccupancyColor(double occupancy) {
            if (occupancy >= 90) {
                return new Color(76, 175, 80);  // Green - Excellent
            } else if (occupancy >= 75) {
                return new Color(156, 204, 101);  // Light Green - Good
            } else if (occupancy >= 50) {
                return UAAPTheme.ACCENT_GOLD;  // Yellow - Fair
            } else if (occupancy >= 25) {
                return new Color(255, 152, 0);  // Orange - Low
            } else {
                return new Color(244, 67, 54);  // Red - Very Low
            }
        }
    }

    // Custom component for ticket revenue visualization
    private class TicketRevenueChart extends JPanel {
        private List<ReportService.TicketRevenueRow> data;

        public TicketRevenueChart() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 500));
        }

        public void updateData(List<ReportService.TicketRevenueRow> newData) {
            this.data = newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (data == null || data.isEmpty()) {
                g.setColor(UAAPTheme.TEXT_SECONDARY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String msg = "Select date range and click Generate to view data";
                FontMetrics fm = g.getFontMetrics();
                int msgWidth = fm.stringWidth(msg);
                g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            int barHeight = 35;
            int spacing = 10;

            // Title
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2d.setColor(UAAPTheme.TEXT_PRIMARY);
            g2d.drawString("Revenue by Seat Type", padding, 25);

            // Aggregate revenue by seat type
            java.util.Map<String, Double> revenueMap = new java.util.HashMap<>();
            for (ReportService.TicketRevenueRow row : data) {
                String seatType = row.seatType();
                double revenue = row.revenue().doubleValue();
                revenueMap.put(seatType, revenueMap.getOrDefault(seatType, 0.0) + revenue);
            }

            // Convert to sorted list
            java.util.List<java.util.Map.Entry<String, Double>> sortedEntries = 
                new java.util.ArrayList<>(revenueMap.entrySet());
            sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            // Find max revenue for scaling
            double maxRevenue = 0;
            for (java.util.Map.Entry<String, Double> entry : sortedEntries) {
                maxRevenue = Math.max(maxRevenue, entry.getValue());
            }

            // Draw bars
            int y = padding + 20;
            int maxBarWidth = width - (2 * padding) - 120;

            int index = 0;
            for (java.util.Map.Entry<String, Double> entry : sortedEntries) {
                if (index >= 10) break;  // Limit to top 10

                String seatType = entry.getKey();
                double revenue = entry.getValue();
                int barWidth = maxRevenue > 0 ? (int) (maxBarWidth * (revenue / maxRevenue)) : 0;

                // Draw bar background
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(padding, y, maxBarWidth, barHeight, 5, 5);

                // Draw actual bar
                Color barColor = getSeatTypeColor(index);
                g2d.setColor(barColor);
                g2d.fillRoundRect(padding, y, barWidth, barHeight, 5, 5);

                // Draw border
                g2d.setColor(UAAPTheme.CARD_BORDER);
                g2d.drawRoundRect(padding, y, maxBarWidth, barHeight, 5, 5);

                // Draw seat type name
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String displayName = seatType;
                if (displayName.length() > 15) {
                    displayName = displayName.substring(0, 12) + "...";
                }
                g2d.drawString(displayName, padding + 5, y + barHeight / 2 + 5);

                // Draw revenue
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("Php%.2f", revenue), maxBarWidth + padding + 10, y + barHeight / 2 + 5);

                y += barHeight + spacing;
                index++;
            }
        }

        private Color getSeatTypeColor(int index) {
            Color[] colors = {
                    UAAPTheme.PRIMARY_GREEN,
                    UAAPTheme.ACCENT_BLUE,
                    UAAPTheme.ACCENT_GOLD,
                    UAAPTheme.SUCCESS,
                    UAAPTheme.INFO,
                    new Color(156, 39, 176),
                    new Color(255, 152, 0),
                    new Color(121, 85, 72),
                    new Color(233, 30, 99),
                    new Color(0, 150, 136)
            };
            return colors[index % colors.length];
        }
    }

    private JPanel buildRevenueTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UAAPTheme.LIGHT_SURFACE);

        // Split panel for table and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.6);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Ticket Revenue Table"));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Event", "Match ID", "Seat Type", "Tickets Sold", "Revenue"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UAAPTheme.styleTable(table);

        JTextField fromField = new JTextField(10);
        fromField.setEditable(false);
        fromField.setToolTipText("Click the calendar button to select a date");
        UAAPTheme.styleTextField(fromField);
        
        JTextField toField = new JTextField(10);
        toField.setEditable(false);
        toField.setToolTipText("Click the calendar button to select a date");
        UAAPTheme.styleTextField(toField);

        JButton fromDateButton = new JButton("...");
        fromDateButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        fromDateButton.setToolTipText("Select From Date");
        UAAPTheme.styleInfoButton(fromDateButton);
        fromDateButton.addActionListener(e -> {
            LocalDate initialDate = LocalDate.now();
            String currentText = fromField.getText().trim();
            if (!currentText.isEmpty()) {
                try {
                    initialDate = LocalDate.parse(currentText);
                } catch (Exception ex) {
                    // Use current date if parse fails
                }
            }
            LocalDate selectedDate = DatePickerPanel.showDatePickerDialog(this, initialDate);
            if (selectedDate != null) {
                fromField.setText(selectedDate.toString());
            }
        });

        JButton toDateButton = new JButton("...");
        toDateButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        toDateButton.setToolTipText("Select To Date");
        UAAPTheme.styleInfoButton(toDateButton);
        toDateButton.addActionListener(e -> {
            LocalDate initialDate = LocalDate.now();
            String currentText = toField.getText().trim();
            if (!currentText.isEmpty()) {
                try {
                    initialDate = LocalDate.parse(currentText);
                } catch (Exception ex) {
                    // Use current date if parse fails
                }
            }
            LocalDate selectedDate = DatePickerPanel.showDatePickerDialog(this, initialDate);
            if (selectedDate != null) {
                toField.setText(selectedDate.toString());
            }
        });

        TicketRevenueChart chartComponent = new TicketRevenueChart();

        JButton refresh = new JButton("Generate");
        UAAPTheme.styleActionButton(refresh);
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                LocalDate from = parseDate(fromField.getText());
                LocalDate to = parseDate(toField.getText());
                List<ReportService.TicketRevenueRow> rows = reportService.getTicketRevenueSummary(from, to);
                for (ReportService.TicketRevenueRow row : rows) {
                    model.addRow(new Object[]{
                            row.eventName(),
                            row.matchId() != null ? row.matchId() : "All",
                            row.seatType(),
                            row.ticketsSold(),
                            "₱" + row.revenue()
                    });
                }
                // Update chart
                chartComponent.updateData(rows);
            } catch (Exception ex) {
                showError("Unable to build revenue report:\n" + ex.getMessage());
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filter.setBorder(BorderFactory.createTitledBorder("Filters"));
        filter.add(new JLabel("From Date:"));
        filter.add(fromField);
        filter.add(fromDateButton);
        filter.add(Box.createHorizontalStrut(10));
        filter.add(new JLabel("To Date:"));
        filter.add(toField);
        filter.add(toDateButton);
        filter.add(Box.createHorizontalStrut(10));
        filter.add(refresh);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(filter, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(tableContainer, BorderLayout.CENTER);

        splitPane.setLeftComponent(tablePanel);

        // Chart panel
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Revenue by Seat Type Chart"));
        chartPanel.add(chartComponent, BorderLayout.CENTER);

        splitPane.setRightComponent(chartPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel buildPlayerStatsTab() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UAAPTheme.LIGHT_SURFACE);

        // Create nested tabs for Men's Basketball and Women's Volleyball
        JTabbedPane sportTabs = new JTabbedPane();
        UAAPTheme.styleTabPane(sportTabs);
        
        sportTabs.addTab("Men's Basketball", buildSportSpecificStatsPanel("Basketball", "Men"));
        sportTabs.addTab("Women's Volleyball", buildSportSpecificStatsPanel("Volleyball", "Women"));
        mainPanel.add(sportTabs, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel buildSportSpecificStatsPanel(String sport, String gender) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(UAAPTheme.LIGHT_SURFACE);

        // Split panel for table and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.65);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        String titleLabel = (sport != null && gender != null) 
            ? gender + "'s " + sport + " Player Statistics" 
            : "Player Statistics";
        tablePanel.setBorder(BorderFactory.createTitledBorder(titleLabel));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Rank", "Player", "Team", "Position", "Total Points"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UAAPTheme.styleTable(table);

        JButton refresh = new JButton("Refresh");
        UAAPTheme.styleInfoButton(refresh);
        refresh.addActionListener(e -> {
            loadPlayerStatsDataBySport(model, sport);
        });

        JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        control.add(refresh);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(control, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);

        // Chart panel
        JPanel chartPanel = createPlayerScoreChartPanelBySport(sport);
        splitPane.setRightComponent(chartPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Load initial data
        loadPlayerStatsDataBySport(model, sport);

        return mainPanel;
    }

    private void loadPlayerStatsData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<ReportService.PlayerParticipationRow> rows = reportService.getPlayerParticipationStats();
            int rank = 1;
            for (ReportService.PlayerParticipationRow row : rows) {
                model.addRow(new Object[]{
                        rank++,
                        row.firstName() + " " + row.lastName(),
                        row.teamName(),
                        row.sport(),
                        row.position(),
                        row.totalPoints()
                });
            }
        } catch (Exception ex) {
            showError("Unable to load player statistics:\n" + ex.getMessage());
        }
    }

    private void loadPlayerStatsDataBySport(DefaultTableModel model, String sport) {
        model.setRowCount(0);
        try {
            List<ReportService.PlayerParticipationRow> rows = reportService.getPlayerParticipationStats();
            int rank = 1;
            for (ReportService.PlayerParticipationRow row : rows) {
                if (sport == null || row.sport().equalsIgnoreCase(sport)) {
                    model.addRow(new Object[]{
                            rank++,
                            row.firstName() + " " + row.lastName(),
                            row.teamName(),
                            row.position(),
                            row.totalPoints()
                    });
                }
            }
        } catch (Exception ex) {
            showError("Unable to load player statistics:\n" + ex.getMessage());
        }
    }

    private JPanel createPlayerScoreChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Top 10 Scorers"));

        PlayerScoreChart chart = new PlayerScoreChart(null);
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPlayerScoreChartPanelBySport(String sport) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Top 10 " + sport + " Scorers"));

        PlayerScoreChart chart = new PlayerScoreChart(sport);
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    // Custom component for player score visualization
    private class PlayerScoreChart extends JPanel {
        private List<ReportService.PlayerParticipationRow> data;
        private String sportFilter;

        public PlayerScoreChart(String sportFilter) {
            this.sportFilter = sportFilter;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 500));
            loadData();
        }

        private void loadData() {
            try {
                List<ReportService.PlayerParticipationRow> allData = reportService.getPlayerParticipationStats();
                if (sportFilter != null && !sportFilter.isEmpty()) {
                    data = allData.stream()
                            .filter(row -> row.sport().equalsIgnoreCase(sportFilter))
                            .collect(java.util.stream.Collectors.toList());
                } else {
                    data = allData;
                }
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (data == null || data.isEmpty()) {
                g.setColor(UAAPTheme.TEXT_SECONDARY);
                g.drawString("No data available", 50, 50);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            int barHeight = 25;
            int spacing = 8;

            // Title
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2d.setColor(UAAPTheme.TEXT_PRIMARY);
            g2d.drawString("Top Scorers", padding, 25);

            // Find max score for scaling
            int maxScore = 0;
            for (int i = 0; i < Math.min(data.size(), 10); i++) {
                maxScore = Math.max(maxScore, data.get(i).totalPoints());
            }

            // Draw bars
            int y = padding + 20;
            int maxBarWidth = width - (2 * padding) - 80;

            for (int i = 0; i < Math.min(data.size(), 10); i++) {
                ReportService.PlayerParticipationRow row = data.get(i);
                int score = row.totalPoints();
                int barWidth = maxScore > 0 ? (int) (maxBarWidth * ((double) score / maxScore)) : 0;

                // Draw bar background
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(padding, y, maxBarWidth, barHeight, 5, 5);

                // Draw actual bar
                Color barColor = i < 3 ? getTopThreeColor(i) : UAAPTheme.ACCENT_BLUE;
                g2d.setColor(barColor);
                g2d.fillRoundRect(padding, y, barWidth, barHeight, 5, 5);

                // Draw border
                g2d.setColor(UAAPTheme.CARD_BORDER);
                g2d.drawRoundRect(padding, y, maxBarWidth, barHeight, 5, 5);

                // Draw player name
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String playerName = row.firstName() + " " + row.lastName();
                if (playerName.length() > 20) {
                    playerName = playerName.substring(0, 17) + "...";
                }
                g2d.drawString((i + 1) + ". " + playerName, padding + 5, y + barHeight - 8);

                // Draw score
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(score), maxBarWidth + padding + 10, y + barHeight - 8);

                y += barHeight + spacing;
            }
        }

        private Color getTopThreeColor(int index) {
            switch (index) {
                case 0:
                    return UAAPTheme.ACCENT_GOLD;  // Gold for 1st
                case 1:
                    return new Color(192, 192, 192);  // Silver for 2nd
                case 2:
                    return new Color(205, 127, 50);  // Bronze for 3rd
                default:
                    return UAAPTheme.ACCENT_BLUE;
            }
        }
    }

    private void addFilterField(JPanel panel, int row, String label, JTextField field) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(4, 6, 4, 6);
        panel.add(new JLabel(label), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1;
        fieldGbc.insets = new Insets(4, 6, 4, 6);
        panel.add(field, fieldGbc);
    }

    private void addFilterFieldWithButton(JPanel panel, int row, String label, JTextField field, JButton button) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(4, 6, 4, 6);
        panel.add(new JLabel(label), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1;
        fieldGbc.insets = new Insets(4, 6, 4, 6);
        panel.add(field, fieldGbc);

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 2;
        buttonGbc.gridy = row;
        buttonGbc.insets = new Insets(4, 2, 4, 6);
        panel.add(button, buttonGbc);
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(raw.trim());
    }

    private LocalDate parseYearMonth(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        YearMonth ym = YearMonth.parse(raw.trim());
        return ym.atDay(1);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
