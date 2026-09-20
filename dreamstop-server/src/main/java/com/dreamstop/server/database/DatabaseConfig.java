package com.dreamstop.server.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles database configuration by reading properties from {@code db.properties}
 * with flexible fallback support for environment variables and system properties.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public final class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String DEFAULT_PROPERTIES_FILE = "db.properties";

    private static volatile DatabaseConfig instance;

    private String driver;
    private String url;
    private String user;
    private String password;
    private int loginTimeout;
    private boolean autoInit;

    private DatabaseConfig() {
        loadProperties();
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
            } else {
                LOGGER.warning("Could not find " + DEFAULT_PROPERTIES_FILE + " in classpath. Using defaults.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading " + DEFAULT_PROPERTIES_FILE, e);
        }

        // Priority order: System Property > Environment Variable > db.properties > Hardcoded Default
        this.driver = getProperty(props, "db.driver", "DB_DRIVER", "com.mysql.cj.jdbc.Driver");
        this.url = getProperty(props, "db.url", "DB_URL",
                "jdbc:mysql://localhost:3306/dreamstop_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        this.user = getProperty(props, "db.user", "DB_USER", "root");
        this.password = getProperty(props, "db.password", "DB_PASSWORD", "root");

        String timeoutStr = getProperty(props, "db.timeout.login", "DB_TIMEOUT", "5");
        try {
            this.loginTimeout = Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            this.loginTimeout = 5;
        }

        String autoInitStr = getProperty(props, "db.auto.init", "DB_AUTO_INIT", "true");
        this.autoInit = Boolean.parseBoolean(autoInitStr);
    }

    private String getProperty(Properties props, String key, String envKey, String defaultValue) {
        String systemVal = System.getProperty(key);
        if (systemVal != null && !systemVal.trim().isEmpty()) {
            return systemVal.trim();
        }

        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }

        String fileVal = props.getProperty(key);
        if (fileVal != null && !fileVal.trim().isEmpty()) {
            return fileVal.trim();
        }

        return defaultValue;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public boolean isAutoInit() {
        return autoInit;
    }

    /**
     * Allows custom programmatic override (useful for integration test environments).
     */
    public synchronized void overrideConfig(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }
}
