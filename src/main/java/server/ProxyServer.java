package server;

import exception.ConfigException;
import config.loader.MetaConfig;
import config.loader.ServerConfig;
import nio.NIOAcceptor;
import nio.Processor;
import nio.RWReactor;

import java.io.IOException;

/**
 * Created by pengan on 16-9-23.
 */
public class ProxyServer {
    private static final ProxyServer INSTANCE = new ProxyServer();
    private ServerConfig systemConfig;
    private MetaDataServer metaServer;
    private MetaConfig metaConfig;
    private NIOAcceptor acceptor;
    private Processor[] processors;

    public void start(String config) throws IOException, ConfigException {
        this.systemConfig = new ServerConfig(config);
        this.metaConfig = new MetaConfig(this.systemConfig);
        this.processors = new Processor[this.systemConfig.getProcessors()];
        for (int index = 0; index < this.systemConfig.getProcessors(); index ++) {
            RWReactor reactor = new RWReactor("Processor" + index);
            this.processors[index] = new Processor(reactor);
            this.processors[index].start();
        }

        this.acceptor = new NIOAcceptor(systemConfig.getProxyPort());
        this.acceptor.start();

        this.metaServer = new MetaDataServer(systemConfig.getHttpPort());
        this.metaServer.start();
    }

    public static final ProxyServer getINSTANCE() {
        return INSTANCE;
    }

    public ServerConfig getSystemConfig() {
        return systemConfig;
    }

    public MetaConfig getMetaConfig() {
        return metaConfig;
    }

    public Processor[] getProcessors() {
        return processors;
    }
}
