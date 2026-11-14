package com.proftest.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
  Паттерн Singleton — конфигурация сервера.
  Читает настройки из server.properties или аргументов командной строки.
 */
public class ServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static volatile ServerConfig instance;

    private int port = 12345;
    private String dbUrl = "jdbc:mysql://localhost:3306/proftest?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true";
    private String dbUser = "root";
    private String dbPassword = "password";
    private boolean showSql = false;
    private int maxThreads = 50;

    private ServerConfig() {}

    public static ServerConfig getInstance() {
        if (instance == null) {
            synchronized (ServerConfig.class) {
                if (instance == null) {
                    instance = new ServerConfig();
                }
            }
        }
        return instance;
    }

    public void loadFromFile(String path) {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(path)) {
            props.load(is);
            applyProperties(props);
            logger.info("Config loaded from {}", path);
        } catch (IOException e) {
            logger.warn("Could not load config from {}: {}. Using defaults.", path, e.getMessage());
        }
    }

    public void loadFromClasspath() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/server.properties")) {
            if (is != null) {
                props.load(is);
                applyProperties(props);
                logger.info("Config loaded from classpath:server.properties");
            }
        } catch (IOException e) {
            logger.warn("Could not load classpath config: {}", e.getMessage());
        }
    }

    private void applyProperties(Properties props) {
        if (props.containsKey("server.port")) port = Integer.parseInt(props.getProperty("server.port"));
        if (props.containsKey("db.url")) dbUrl = props.getProperty("db.url");
        if (props.containsKey("db.user")) dbUser = props.getProperty("db.user");
        if (props.containsKey("db.password")) dbPassword = props.getProperty("db.password");
        if (props.containsKey("hibernate.show_sql")) showSql = Boolean.parseBoolean(props.getProperty("hibernate.show_sql"));
        if (props.containsKey("server.max_threads")) maxThreads = Integer.parseInt(props.getProperty("server.max_threads"));
    }

    public void applyArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port", "-p" -> { if (i + 1 < args.length) port = Integer.parseInt(args[++i]); }
                case "--db-url" -> { if (i + 1 < args.length) dbUrl = args[++i]; }
                case "--db-user" -> { if (i + 1 < args.length) dbUser = args[++i]; }
                case "--db-password" -> { if (i + 1 < args.length) dbPassword = args[++i]; }
                case "--show-sql" -> showSql = true;
                case "--config" -> { if (i + 1 < args.length) loadFromFile(args[++i]); }
            }
        }
    }

    public int getPort() { return port; }
    public String getDbUrl() { return dbUrl; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }
    public boolean isShowSql() { return showSql; }
    public int getMaxThreads() { return maxThreads; }

    @Override
    public String toString() {
        return "ServerConfig{port=" + port + ", dbUrl=" + dbUrl + ", dbUser=" + dbUser + "}";
    }
}
