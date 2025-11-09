import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Centralized palette and helper methods to keep the UAAP experience cohesive.
 */
public final class UAAPTheme {

    public static final Color PRIMARY_GREEN = new Color(6, 84, 51);
    public static final Color SECONDARY_GREEN = new Color(0, 48, 31);
    public static final Color ACCENT_GOLD = new Color(245, 196, 35);
    public static final Color ACCENT_BLUE = new Color(33, 119, 196);
    public static final Color LIGHT_SURFACE = new Color(245, 248, 246);
    public static final Color CARD_BORDER = new Color(213, 227, 218);
    public static final Color TEXT_PRIMARY = new Color(15, 32, 23);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

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
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(24, 110, 70));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(217, 233, 224));
        table.getTableHeader().setForeground(TEXT_PRIMARY);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0
                            ? new Color(226, 243, 232)
                            : Color.WHITE);
                }
                return comp;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}
