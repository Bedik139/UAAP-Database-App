import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

/**
 * Animated side panel that showcases the UAAP artwork of teams tied to a match.
 */
public class MatchSpotlightPanel extends JPanel {

    private final MatchTeamDAO matchTeamDAO = new MatchTeamDAO();
    private final JPanel teamStack = new JPanel();
    private final JLabel headlineLabel = new JLabel();
    private String headline = "Select a match";
    private float pulse;

    public MatchSpotlightPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(320, 0));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 12));

        teamStack.setOpaque(false);
        teamStack.setLayout(new BoxLayout(teamStack, BoxLayout.Y_AXIS));
        headlineLabel.setOpaque(false);
        headlineLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        headlineLabel.setForeground(new Color(240, 255, 250));
        headlineLabel.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        headlineLabel.setText(headline);

        // Wrap teamStack in a JScrollPane to make it scrollable
        JScrollPane scrollPane = new JScrollPane(teamStack);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(headlineLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        Timer timer = new Timer(80, e -> {
            pulse += 0.03f;
            repaint();
        });
        timer.start();
    }

    public void showcaseMatch(int matchId) {
        teamStack.removeAll();
        if (matchId <= 0) {
            headline = "Select a match to View the Teams";
            headlineLabel.setText(headline);
            revalidate();
            repaint();
            return;
        }

        try {
            List<MatchTeam> entries = matchTeamDAO.getMatchTeamsForMatch(matchId);
            if (entries.isEmpty()) {
                headline = "Link teams to this match to unveil their crests.";
            } else {
                headline = "Team home of: ";
                for (MatchTeam entry : entries) {
                    teamStack.add(buildTeamCard(entry));
                    teamStack.add(Box.createVerticalStrut(16));
                }
            }
        } catch (SQLException ex) {
            headline = "Unable to load team art (" + ex.getMessage() + ")";
        }
        headlineLabel.setText(headline);
        revalidate();
        repaint();
    }

    private JPanel buildTeamCard(MatchTeam entry) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel logoLabel = new JLabel(entry.getTeamName(), UAAPAssets.teamLogo(entry.getTeamName(), 96), JLabel.CENTER);
        logoLabel.setHorizontalTextPosition(JLabel.CENTER);
        logoLabel.setVerticalTextPosition(JLabel.BOTTOM);
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

        JLabel meta = new JLabel(
                (entry.isHomeTeam() ? "HOME" : "AWAY") + " :: " + entry.getTeamScore() + " pts",
                JLabel.CENTER
        );
        meta.setForeground(new Color(210, 255, 229));
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(logoLabel, BorderLayout.CENTER);
        card.add(meta, BorderLayout.SOUTH);
        card.setAlignmentX(0.5f);

        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        float offset = (float) (Math.sin(pulse) + 1) / 2f;

        GradientPaint paint = new GradientPaint(
                0, 0,
                blend(new Color(12, 64, 36, 200), UAAPTheme.ACCENT_GOLD, 0.15f + 0.2f * offset),
                width,
                height,
                blend(new Color(0, 96, 63, 180), UAAPTheme.ACCENT_BLUE, 0.1f + 0.15f * offset)
        );
        g2.setPaint(paint);
        g2.fillRoundRect(0, 0, width, height, 32, 32);

        g2.dispose();
        super.paintComponent(g);
    }

    private Color blend(Color a, Color b, float ratio) {
        float inv = 1 - ratio;
        return new Color(
                Math.round(a.getRed() * inv + b.getRed() * ratio),
                Math.round(a.getGreen() * inv + b.getGreen() * ratio),
                Math.round(a.getBlue() * inv + b.getBlue() * ratio),
                Math.round(a.getAlpha() * inv + b.getAlpha() * ratio)
        );
    }
}
