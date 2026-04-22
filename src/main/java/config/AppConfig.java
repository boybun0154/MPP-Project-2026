package config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Loads and manages application configurations from environment variables.
 */
public class AppConfig {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getDatabaseUrl() {
        String url = dotenv.get("DATABASE_URL");

        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("...");
        }

        return url;
    }

    public static String getAppMode() {
        return dotenv.get("APP_MODE", "CLI").toUpperCase();
    }

    public static int getServerPort() {
        return Integer.parseInt(dotenv.get("SERVER_PORT", "8080"));
    }
}