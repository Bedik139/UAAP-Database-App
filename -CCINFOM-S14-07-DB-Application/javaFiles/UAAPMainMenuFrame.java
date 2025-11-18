import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Modern and immersive entry point with clean card-based design.
 */
public class UAAPMainMenuFrame extends JFrame {

    public UAAPMainMenuFrame() {
        super("UAAP Database Event Management");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UAAPTheme.LIGHT_SURFACE);

        HeroPanel heroPanel = new HeroPanel();
        root.add(heroPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private final class HeroPanel extends JPanel {

        private float glow;
        private PortalCard managerCard;
        private PortalCard customerCard;

        private HeroPanel() {
            setOpaque(true);
            setBackground(new Color(240, 244, 242));
            setLayout(new BorderLayout());

            // Header Section
            JPanel headerPanel = createHeaderPanel();
            add(headerPanel, BorderLayout.NORTH);

            // Center Content with Cards
            JPanel centerPanel = new JPanel();
            centerPanel.setOpaque(false);
            centerPanel.setLayout(new GridBagLayout());
            centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 60, 60));

            JPanel cardsContainer = new JPanel(new GridLayout(1, 2, 40, 0));
            cardsContainer.setOpaque(false);

            // Manager Portal Card
            managerCard = new PortalCard(
                "Manager Portal",
                "Complete Database Control",
                "Access all management features including events, teams, players, tickets, and comprehensive reports.",
                UAAPTheme.PRIMARY_GREEN,
                "🎯",
                () -> {
                    dispose();
                    ManagerDashboardFrame.showUI();
                }
            );

            // Customer Portal Card
            customerCard = new PortalCard(
                "Customer Portal",
                "Ticket Management",
                "Browse events, and purchase tickets.",
                UAAPTheme.ACCENT_BLUE,
                "🎫",
                () -> {
                    dispose();
                    CustomerPortalFrame.showUI();
                }
            );

            cardsContainer.add(managerCard);
            cardsContainer.add(customerCard);

            centerPanel.add(cardsContainer);
            add(centerPanel, BorderLayout.CENTER);

            // Footer
            JPanel footerPanel = createFooterPanel();
            add(footerPanel, BorderLayout.SOUTH);

            Timer timer = new Timer(60, e -> {
                glow += 0.02f;
                repaint();
            });
            timer.start();
        }

        private JPanel createHeaderPanel() {
            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBorder(BorderFactory.createEmptyBorder(50, 60, 20, 60));

            // Logo and Title Container
            JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            logoTitlePanel.setOpaque(false);

            JLabel crest = new JLabel(UAAPAssets.uaapCrest(100));
            logoTitlePanel.add(crest);

            JPanel titlePanel = new JPanel();
            titlePanel.setOpaque(false);
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("UAAP Event Management");
            title.setFont(new Font("Segoe UI", Font.BOLD, 40));
            title.setForeground(UAAPTheme.TEXT_PRIMARY);
            title.setAlignmentX(LEFT_ALIGNMENT);

            JLabel subtitle = new JLabel("Choose your portal to continue");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            subtitle.setForeground(UAAPTheme.TEXT_SECONDARY);
            subtitle.setAlignmentX(LEFT_ALIGNMENT);
            subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            titlePanel.add(title);
            titlePanel.add(subtitle);
            logoTitlePanel.add(titlePanel);

            header.add(logoTitlePanel);

            return header;
        }

        private JPanel createFooterPanel() {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

            JLabel footerLabel = new JLabel("UAAP Database Event Management Application for CCINFOM • A.Y. 2025 - 2026");
            footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            footerLabel.setForeground(UAAPTheme.TEXT_SECONDARY);

            footer.add(footerLabel);

            return footer;
        }
    }

    /**
     * Modern card component for portal selection with hover effects
     */
    private static class PortalCard extends JPanel {
        private boolean hovered = false;
        private final Color accentColor;
        private final Runnable action;
        private int shadowOffset = 8;

        public PortalCard(String title, String subtitle, String description, 
                         Color accentColor, String emoji, Runnable action) {
            this.accentColor = accentColor;
            this.action = action;

            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(420, 320));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Card content panel
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(35, 30, 35, 30));

            // Title
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(UAAPTheme.TEXT_PRIMARY);
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);

            // Subtitle
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            subtitleLabel.setForeground(accentColor);
            subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

            // Description
            JLabel descLabel = new JLabel("<html><body style='width: 350px'>" + description + "</body></html>");
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            descLabel.setForeground(UAAPTheme.TEXT_SECONDARY);
            descLabel.setAlignmentX(LEFT_ALIGNMENT);

            // Action Button
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

            JButton actionButton = new JButton("Launch Portal");
            actionButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            actionButton.setForeground(Color.WHITE);
            actionButton.setBackground(accentColor);
            actionButton.setFocusPainted(false);
            actionButton.setBorderPainted(false);
            actionButton.setOpaque(true);
            actionButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
            actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Button hover effect
            actionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    actionButton.setBackground(accentColor.brighter());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    actionButton.setBackground(accentColor);
                }
            });

            actionButton.addActionListener(e -> action.run());

            buttonPanel.add(actionButton);
            
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(5));
            contentPanel.add(subtitleLabel);
            contentPanel.add(Box.createVerticalStrut(18));
            contentPanel.add(descLabel);
            contentPanel.add(Box.createVerticalStrut(25));
            contentPanel.add(buttonPanel);

            add(contentPanel, BorderLayout.CENTER);

            // Hover effects
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    shadowOffset = 16;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    shadowOffset = 8;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = 20;

            // Shadow
            g2.setColor(new Color(0, 0, 0, hovered ? 35 : 20));
            g2.fillRoundRect(shadowOffset / 2, shadowOffset / 2, width - shadowOffset, 
                           height - shadowOffset, arc, arc);

            // Card background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, width - shadowOffset, height - shadowOffset, arc, arc);

            // Accent bar at top
            g2.setColor(accentColor);
            g2.fillRoundRect(0, 0, width - shadowOffset, 6, arc, arc);

            // Border
            if (hovered) {
                g2.setColor(accentColor);
                g2.setStroke(new java.awt.BasicStroke(2));
                g2.drawRoundRect(0, 0, width - shadowOffset - 1, height - shadowOffset - 1, arc, arc);
            } else {
                g2.setColor(new Color(230, 230, 230));
                g2.drawRoundRect(0, 0, width - shadowOffset - 1, height - shadowOffset - 1, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
