package com.proftest.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(ClientConfig.class);
    private static volatile ClientConfig instance;

    private String serverHost = "localhost";
    private int serverPort = 12345;

    private ClientConfig() {
        loadFromClasspath();
    }

    public static ClientConfig getInstance() {
        if (instance == null) {
            synchronized (ClientConfig.class) {
                if (instance == null) {
                    instance = new ClientConfig();
                }
            }
        }
        return instance;
    }

    private void loadFromClasspath() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/client.properties")) {
            if (is != null) {
                props.load(is);
                if (props.containsKey("server.host")) serverHost = props.getProperty("server.host");
                if (props.containsKey("server.port")) serverPort = Integer.parseInt(props.getProperty("server.port"));
                logger.info("Client config loaded from classpath");
            }
        } catch (IOException e) {
            logger.warn("Could not load client.properties: {}", e.getMessage());
        }
    }

    public String getServerHost() { return serverHost; }
    public void setServerHost(String host) { this.serverHost = host; }
    public int getServerPort() { return serverPort; }
    public void setServerPort(int port) { this.serverPort = port; }
}
