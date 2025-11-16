import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Lazy-loading image helper so we can splash authentic UAAP artwork without complex resource plumbing.
 */
public final class UAAPAssets {

    private static final String BASE = "assets" + File.separator + "logos" + File.separator;
    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    private UAAPAssets() {
    }

    public static ImageIcon uaapCrest(int size) {
        return scaled(load("UAAP LOGO.jpg"), size);
    }

    public static ImageIcon dlsuSeal(int size) {
        return scaled(load("DLSU-headerLogo.png"), size);
    }

    public static ImageIcon teamLogo(String teamName, int size) {
        if (teamName == null) {
            return uaapCrest(size);
        }
        String name = teamName.toLowerCase();
        String file = null;
        if (contains(name, "la salle", "dlsu", "archer")) {
            file = "DLSUArcher-logo.jpg";
        } else if (contains(name, "ateneo", "blue eagles")) {
            file = "Ateneo-logo.png";
        } else if (contains(name, "admu")) {
            file = "Ateneo-logo.png";
        } else if (contains(name, "adamson", "falcon")) {
            file = "Adamson-Logo.jpg";
        } else if (contains(name, "feu", "tamaraw")) {
            file = "FEU-Logo.jpg";
        } else if (contains(name, "nu", "bulldog")) {
            file = "NU-Logo.jpg";
        } else if (contains(name, "ue", "warrior")) {
            file = "UE-logo.jpg";
        } else if (contains(name, "up", "fighting maroons")) {
            file = "UP-logo.png";
        } else if (contains(name, "ust", "tiger")) {
            file = "UST.png";
        }
        ImageIcon icon = scaled(load(file), size);
        return icon != null ? icon : uaapCrest(size);
    }

    private static boolean contains(String name, String... tokens) {
        for (String token : tokens) {
            if (name.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static ImageIcon load(String fileName) {
        if (fileName == null) {
            return null;
        }
        return CACHE.computeIfAbsent(fileName, key -> {
            File file = new File(BASE + key);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
            return null;
        });
    }

    private static ImageIcon scaled(ImageIcon icon, int size) {
        if (icon == null || size <= 0) {
            return icon;
        }
        Image image = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }
}
