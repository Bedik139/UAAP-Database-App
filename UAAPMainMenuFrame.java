import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Immersive entry point that replaces the plain dialog with a cinematic hero hub.
 */
public class UAAPMainMenuFrame extends JFrame {

    public UAAPMainMenuFrame() {
        super("UAAP Database Application");
        setSize(620,  360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UAAPTheme.LIGHT_SURFACE);
        root.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        HeroPanel heroPanel = new HeroPanel();
        root.add(heroPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private final class HeroPanel extends JPanel {

        private float glow;

        private HeroPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

            JLabel crest = new JLabel(UAAPAssets.uaapCrest(120));
            crest.setHorizontalAlignment(JLabel.CENTER);

            JPanel copyPanel = new JPanel();
            copyPanel.setOpaque(false);
            copyPanel.setLayout(new BoxLayout(copyPanel, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("UAAP Event Management App");
            title.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
            title.setForeground(UAAPTheme.TEXT_PRIMARY);
            title.setAlignmentX(CENTER_ALIGNMENT);

            JButton managerButton = buildPortalButton("Manager Portal", "Control the full Database", true, () -> {
                dispose();
                ManagerDashboardFrame.showUI();
            });
            JButton customerButton = buildPortalButton("Customer Portal", "Book or refund ticket(s)", false, () -> {
                dispose();
                CustomerPortalFrame.showUI();
            });

            managerButton.setAlignmentX(CENTER_ALIGNMENT);
            customerButton.setAlignmentX(CENTER_ALIGNMENT);

            copyPanel.add(title);
            copyPanel.add(Box.createVerticalStrut(12));
            copyPanel.add(managerButton);
            copyPanel.add(Box.createVerticalStrut(18));
            copyPanel.add(customerButton);

            GridBagConstraints left = new GridBagConstraints();
            left.gridx = 0;
            left.gridy = 0;
            left.insets = new Insets(0, 0, 0, 30);
            left.anchor = GridBagConstraints.CENTER;
            add(crest, left);

            GridBagConstraints right = new GridBagConstraints();
            right.gridx = 1;
            right.gridy = 0;
            right.anchor = GridBagConstraints.CENTER;
            add(copyPanel, right);

            Timer timer = new Timer(60, e -> {
                glow += 0.02f;
                repaint();
            });
            timer.start();
        }

        private JButton buildPortalButton(String title, String hashtag, boolean primary, Runnable action) {
            JButton button = new JButton("<html><b>" + title + "</b><br/><span style='font-size:11px;'>" + hashtag + "</span></html>");
            if (primary) {
                UAAPTheme.stylePrimaryButton(button);
                button.setBackground(new Color(245, 196, 35, 230));
                button.setForeground(UAAPTheme.TEXT_PRIMARY);
            } else {
                UAAPTheme.styleSecondaryButton(button);
                button.setBackground(Color.BLACK);
                button.setForeground(UAAPTheme.ACCENT_GOLD);
                button.setBorder(BorderFactory.createLineBorder(UAAPTheme.ACCENT_GOLD, 1, true));
            }
            button.setPreferredSize(new Dimension(320, 90));
            button.setAlignmentX(0);
            button.addActionListener(e -> action.run());
            return button;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color inner = UAAPTheme.PRIMARY_GREEN;
            Color outer = UAAPTheme.SECONDARY_GREEN.darker();
            int width = getWidth();
            int height = getHeight();

            java.awt.GradientPaint paint = new java.awt.GradientPaint(
                    0, 0,
                    UAAPTheme.PRIMARY_GREEN,
                    width,
                    height,
                    blend(outer, UAAPTheme.ACCENT_BLUE, 0.25f + 0.15f * (float) Math.sin(glow))
            );
            g2.setPaint(paint);
            g2.fillRoundRect(0, 0, width, height, 40, 40);

            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillOval(width - 260, -80, 320, 220);
            g2.dispose();
            super.paintComponent(g);
        }

        private Color blend(Color a, Color b, float ratio) {
            float inv = 1 - ratio;
            return new Color(
                    Math.round(a.getRed() * inv + b.getRed() * ratio),
                    Math.round(a.getGreen() * inv + b.getGreen() * ratio),
                    Math.round(a.getBlue() * inv + b.getBlue() * ratio)
            );
        }
    }
}
