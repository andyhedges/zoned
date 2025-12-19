package io.hedges.zoned.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConfigPathResolver {
    private ConfigPathResolver() {
    }

    public static Path resolve(String configPath) {
        if (configPath != null && !configPath.isBlank()) {
            return Paths.get(configPath);
        }
        String envPath = System.getenv("ZONED_CONFIG");
        if (envPath != null && !envPath.isBlank()) {
            return Paths.get(envPath);
        }
        StringBuilder checked = new StringBuilder();

        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        String userHome = System.getProperty("user.home");
        Path xdgBase = (xdgConfigHome != null && !xdgConfigHome.isBlank())
                ? Paths.get(xdgConfigHome)
                : (userHome != null && !userHome.isBlank() ? Paths.get(userHome, ".config") : null);
        if (xdgBase != null) {
            Path resolved = checkDirectory(xdgBase.resolve("zoned"), checked);
            if (resolved != null) {
                return resolved;
            }
        }

        if (userHome != null && !userHome.isBlank()) {
            Path resolved = checkDirectory(Paths.get(userHome, ".zoned"), checked);
            if (resolved != null) {
                return resolved;
            }
        }

        Path resolved = checkDirectory(Paths.get("/etc/zoned"), checked);
        if (resolved != null) {
            return resolved;
        }
        resolved = checkDirectory(Paths.get("/usr/local/etc"), checked);
        if (resolved != null) {
            return resolved;
        }
        resolved = checkDirectory(Paths.get("."), checked);
        if (resolved != null) {
            return resolved;
        }

        throw new IllegalArgumentException("Config file not found. Checked: " + checked);
    }

    private static Path checkDirectory(Path baseDir, StringBuilder checked) {
        Path yaml = baseDir.resolve("zoned.yaml");
        if (Files.exists(yaml)) {
            return yaml;
        }
        appendChecked(checked, yaml);

        Path yml = baseDir.resolve("zoned.yml");
        if (Files.exists(yml)) {
            return yml;
        }
        appendChecked(checked, yml);
        return null;
    }

    private static void appendChecked(StringBuilder checked, Path path) {
        if (checked.length() > 0) {
            checked.append(", ");
        }
        checked.append(path.toAbsolutePath().normalize());
    }
}
