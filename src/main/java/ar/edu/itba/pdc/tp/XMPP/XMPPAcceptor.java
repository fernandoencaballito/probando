package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.POP3Utils.asOkLine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

class XMPPAcceptor implements TCPEventHandler {
    private static final String GREETING_MSG = asOkLine("ready");
    private static final int BUFFER_SIZE = 4 * 1024;
    private final XMPproxy parent;
    private final TCPReactor reactor;
    private AdminModule adminModule;
    private static final Logger LOGGER = Logger.getLogger(XMPPAcceptor.class);

    XMPPAcceptor(XMPproxy parent, TCPReactor reactor, AdminModule admMod) {
        this.reactor = reactor;
        this.parent = parent;
        this.adminModule = admMod;
    }

    @Override
    public void handle(SelectionKey key) throws IOException {

    	
    	SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false); // Must be nonblocking to register
        // Register the selector with new channel for read and attach byte
        // buffer
        XMPPproxyState state=new XMPPproxyState(clntChan);
        clntChan.register(key.selector(), SelectionKey.OP_READ, state);
        clntChan.configureBlocking(false);
        //
        SocketChannel origin = SocketChannel.open();
        origin.configureBlocking(false);

        origin.connect(adminModule.getOriginAddressForUser(null));
        origin.finishConnect();
        state.setOriginChannel(origin);
        //
        state.updateSubscription(key.selector());

        reactor.subscribeChannel(clntChan, parent);
        reactor.subscribeChannel(origin, parent);
        
    }
}
