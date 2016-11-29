package config.loader;

import exception.ConfigException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    private static final String HTTP_PORT = "http.port";
    private static final String PROXY_PORT = "proxy.port";
    private static final String PROXY_PROCESSORS = "proxy.processors";
    private static final String PROXY_MANAGER_URL = "proxy.manager.url";
    private int httpPort;
    private int proxyPort;
    private int processors;
    private String managerUrl;

    public ServerConfig(String config) throws ConfigException {
        this.load(config);
    }

    private void load(String config) throws ConfigException {
        InputStream conf = null;
        Properties pros = new Properties();
        try {
            conf = new FileInputStream(config);
            pros.load(conf);
            this.httpPort = Integer.parseInt(pros.getProperty(HTTP_PORT));
            this.proxyPort = Integer.parseInt(pros.getProperty(PROXY_PORT));
            this.managerUrl = pros.getProperty(PROXY_MANAGER_URL);
            this.processors = Integer.parseInt(pros.getProperty(PROXY_PROCESSORS));
        } catch (Throwable e) {
            throw new ConfigException(e);
        } finally {
            if (conf != null) {
                try {
                    conf.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public int getProxyPort() {
        return proxyPort;
    }

    public int getProcessors() {
        return processors;
    }

    public String getManagerUrl() {
        return managerUrl;
    }

    public int getHttpPort() {
        return httpPort;
    }
}
