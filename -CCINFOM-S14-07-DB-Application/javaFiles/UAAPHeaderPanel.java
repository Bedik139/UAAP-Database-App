import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared hero panel that injects the UAAP branding, logo, and main menu navigation.
 */
public class UAAPHeaderPanel extends JPanel {

    private final Runnable mainMenuAction;
    private float shimmerPhase;

    public UAAPHeaderPanel(String title, String subtitle, Runnable mainMenuAction) {
        this.mainMenuAction = mainMenuAction;
        setOpaque(false);
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JLabel logoLabel = new JLabel(UAAPAssets.dlsuSeal(96));
        add(logoLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UAAPTheme.TITLE_FONT);
        titleLabel.setForeground(java.awt.Color.WHITE);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UAAPTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(new java.awt.Color(232, 245, 239));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        textPanel.add(Box.createVerticalGlue());

        add(textPanel, BorderLayout.CENTER);

        JButton mainMenuButton = new JButton("Main Menu");
        UAAPTheme.styleSecondaryButton(mainMenuButton);
        mainMenuButton.addActionListener(e -> {
            if (this.mainMenuAction != null) {
                this.mainMenuAction.run();
            }
        });
        add(mainMenuButton, BorderLayout.EAST);
        mainMenuButton.setPreferredSize(new Dimension(140, 44));
        mainMenuButton.setIcon(UAAPAssets.uaapCrest(36));
        mainMenuButton.setHorizontalTextPosition(JLabel.RIGHT);

        Timer timer = new Timer(70, e -> {
            shimmerPhase += 0.01f;
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Color start = UAAPTheme.PRIMARY_GREEN;
        java.awt.Color end = UAAPTheme.SECONDARY_GREEN.darker();
        double offset = (Math.sin(shimmerPhase) + 1) / 2;
        java.awt.GradientPaint paint = new java.awt.GradientPaint(
                0, 0, blend(start, UAAPTheme.ACCENT_GOLD, 0.1f + (float) offset * 0.2f),
                getWidth(), getHeight(), end
        );
        g2.setPaint(paint);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 36, 36));

        g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(0.15f));
        g2.setPaint(java.awt.Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(10, 10, getWidth() - 20, getHeight() - 20, 28, 28));
        g2.dispose();
        super.paintComponent(g);
    }

    private java.awt.Color blend(java.awt.Color c1, java.awt.Color c2, float ratio) {
        float ir = 1 - ratio;
        return new java.awt.Color(
                Math.min(255, Math.round(c1.getRed() * ir + c2.getRed() * ratio)),
                Math.min(255, Math.round(c1.getGreen() * ir + c2.getGreen() * ratio)),
                Math.min(255, Math.round(c1.getBlue() * ir + c2.getBlue() * ratio))
        );
    }
}
