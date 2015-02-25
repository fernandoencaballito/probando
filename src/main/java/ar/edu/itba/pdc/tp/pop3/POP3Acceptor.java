package ar.edu.itba.pdc.tp.pop3;

import static ar.edu.itba.pdc.tp.util.POP3Utils.asOkLine;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

class POP3Acceptor implements TCPEventHandler {
    private static final String GREETING_MSG = asOkLine("ready");

    private final POP3Proxy parent;
    private final TCPReactor reactor;
    private AdminModule adminModule;
    private static final Logger LOGGER = Logger.getLogger(POP3Acceptor.class);

    POP3Acceptor(POP3Proxy parent, TCPReactor reactor, AdminModule admMod) {
        this.reactor = reactor;
        this.parent = parent;
        this.adminModule = admMod;
    }

    @Override
    public void handle(SelectionKey key) throws IOException {
        final ServerSocketChannel serverSocket = (ServerSocketChannel) key
                .channel();
        SocketChannel clientChannel = serverSocket.accept();
        clientChannel.configureBlocking(false);

        LOGGER.info("Channel" + clientChannel.getRemoteAddress()
                + " connected.");

        final POP3ProxyState state = new POP3ProxyState(clientChannel);
        state.getOriginBuffer().put(GREETING_MSG.getBytes());
        state.updateSubscription(key.selector());

        adminModule.addAccess();
        reactor.subscribeChannel(clientChannel, parent);
    }
}
