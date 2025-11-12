import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Centralized palette and helper methods to keep the UAAP experience cohesive.
 * Enhanced with modern styling, hover effects, and visual polish.
 */
public final class UAAPTheme {

    // Modern Green Primary Colors
    public static final Color PRIMARY_GREEN = new Color(16, 185, 129);        // Vibrant emerald green
    public static final Color SECONDARY_GREEN = new Color(5, 150, 105);       // Deeper emerald
    public static final Color DARK_GREEN = new Color(4, 120, 87);             // Dark emerald
    public static final Color ACCENT_GOLD = new Color(251, 191, 36);          // Bright amber
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);          // Blue accent
    public static final Color ACCENT_MINT = new Color(110, 231, 183);         // Fresh mint
    public static final Color ACCENT_TEAL = new Color(20, 184, 166);          // Teal accent
    
    // Surface Colors - Modern & Clean
    public static final Color LIGHT_SURFACE = new Color(240, 253, 244);       // Very light green tint
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color CARD_BORDER = new Color(209, 250, 229);         // Light green border
    public static final Color DIVIDER = new Color(229, 231, 235);
    public static final Color HOVER_BG = new Color(236, 253, 245);            // Subtle green hover
    
    // Text Colors - High Contrast
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);           // Almost black
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);      // Gray
    public static final Color TEXT_DISABLED = new Color(156, 163, 175);
    public static final Color TEXT_ON_GREEN = Color.WHITE;
    
    // State Colors - Vibrant & Clear
    public static final Color SUCCESS = new Color(34, 197, 94);               // Bright green
    public static final Color WARNING = new Color(251, 146, 60);              // Orange
    public static final Color ERROR = new Color(239, 68, 68);                 // Red
    public static final Color INFO = new Color(59, 130, 246);                 // Blue
    
    // Interactive Colors
    public static final Color HOVER_OVERLAY = new Color(16, 185, 129, 10);
    public static final Color SELECTION_BG = new Color(5, 150, 105);
    public static final Color TABLE_ROW_EVEN = new Color(240, 253, 244);      // Light green alternating
    public static final Color TABLE_HEADER_BG = new Color(209, 250, 229);     // Green tint header
    
    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font TABLE_HEADER_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);

    private UAAPTheme() {
    }

    public static void applyModernDefaults() {
        javax.swing.UIManager.put("Panel.background", LIGHT_SURFACE);
        javax.swing.UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        javax.swing.UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
        javax.swing.UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        javax.swing.UIManager.put("TabbedPane.selected", LIGHT_SURFACE);
        javax.swing.UIManager.put("TabbedPane.focus", PRIMARY_GREEN);
    }

    public static void stylePrimaryButton(AbstractButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(ACCENT_GOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
    }

    public static void styleSecondaryButton(AbstractButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(PRIMARY_GREEN.darker());
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_GREEN, 1, true),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
    }

    public static void styleTabPane(JTabbedPane tabs) {
        tabs.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        tabs.setOpaque(false);
        tabs.setTabPlacement(SwingConstants.TOP);
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
    }

    public static void elevate(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        component.setOpaque(true);
        component.setBackground(Color.WHITE);
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setFont(TABLE_FONT);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(DIVIDER);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(TABLE_HEADER_FONT);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : Color.WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return comp;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    /**
     * Style an action button (Add, Update, Save, etc.)
     */
    public static void styleActionButton(AbstractButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_GREEN);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY_GREEN.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
    }

    /**
     * Style a danger/delete button
     */
    public static void styleDangerButton(AbstractButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(ERROR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ERROR.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(ERROR);
            }
        });
    }

    /**
     * Style a neutral button (Clear, Cancel, etc.)
     */
    public static void styleNeutralButton(AbstractButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(new Color(240, 240, 240));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(220, 220, 220));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(240, 240, 240));
            }
        });
    }

    /**
     * Style an info/refresh button
     */
    public static void styleInfoButton(AbstractButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(INFO);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(INFO.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(INFO);
            }
        });
    }

    /**
     * Style input fields with modern appearance
     */
    public static void styleTextField(javax.swing.text.JTextComponent field) {
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_GREEN, 2),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
    }

    /**
     * Style combo boxes with modern appearance
     */
    public static void styleComboBox(javax.swing.JComboBox<?> combo) {
        combo.setFont(LABEL_FONT);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    /**
     * Create a styled panel with title and content
     */
    public static javax.swing.JPanel createStyledPanel(String title) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI Semibold", Font.PLAIN, 14),
            TEXT_PRIMARY
        ));
        return panel;
    }
}
