import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;

/**
 * Lightweight, vector-based UAAP crest inspired icon so we do not rely on external assets.
 */
public class UAAPLogoIcon implements Icon {

    private final int size;

    public UAAPLogoIcon(int size) {
        this.size = Math.max(32, size);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Twin-color halo inspired by the UAAP seal.
        GradientPaint halo = new GradientPaint(
                x, y, UAAPTheme.ACCENT_BLUE,
                x + size, y + size, UAAPTheme.ACCENT_GOLD
        );
        g2.setPaint(halo);
        g2.fillOval(x, y, size, size);

        g2.setColor(Color.WHITE);
        g2.fillOval(x + size / 5, y + size / 5, size * 3 / 5, size * 3 / 5);

        g2.setColor(UAAPTheme.PRIMARY_GREEN);
        GeneralPath star = new GeneralPath();
        double centerX = x + size / 2.0;
        double centerY = y + size / 2.0;
        double outerRadius = size * 0.33;
        double innerRadius = outerRadius * 0.45;

        for (int i = 0; i < 5; i++) {
            double outerAngle = Math.toRadians(-90 + i * 72);
            double innerAngle = Math.toRadians(-90 + 36 + i * 72);

            double outerX = centerX + Math.cos(outerAngle) * outerRadius;
            double outerY = centerY + Math.sin(outerAngle) * outerRadius;
            double innerX = centerX + Math.cos(innerAngle) * innerRadius;
            double innerY = centerY + Math.sin(innerAngle) * innerRadius;

            if (i == 0) {
                star.moveTo(outerX, outerY);
            } else {
                star.lineTo(outerX, outerY);
            }
            star.lineTo(innerX, innerY);
        }
        star.closePath();
        g2.fill(star);

        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
