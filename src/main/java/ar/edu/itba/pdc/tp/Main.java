package ar.edu.itba.pdc.tp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.admin.AdminProtocol;
import ar.edu.itba.pdc.tp.pop3.POP3Proxy;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactorImpl;

public class Main {
    private static final short ADMIN_PORT = 10001;
    private static final short PROXY_PORT = 10002;

    private static final int BUFFER_SIZE = 4 * 1024; // 4k

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Parameter: <origin-server> <port> <proxy-address>");
        }

        short pop3_port = Short.valueOf(args[1]);

        InetSocketAddress pop3_address = new InetSocketAddress(args[2],
                PROXY_PORT);
        InetSocketAddress admin_address = new InetSocketAddress(args[2],
                ADMIN_PORT);

        Map<Integer, TCPProtocol> protocolHandlers = new HashMap<>();

        AdminModule adminModule = new AdminModule(args[0], pop3_port);

        TCPReactorImpl reactor = new TCPReactorImpl(protocolHandlers, args[0]);

        POP3Proxy pop3Proxy = new POP3Proxy(reactor, adminModule);
        AdminProtocol admin = new AdminProtocol(reactor, BUFFER_SIZE,
                adminModule);

        protocolHandlers.put(pop3_address.getPort(), pop3Proxy);
        protocolHandlers.put(admin_address.getPort(), admin);
        
        LOGGER.info("Proxy POP3 started...");
        
        reactor.start();
    }
}