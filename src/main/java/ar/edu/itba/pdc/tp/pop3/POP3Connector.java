package ar.edu.itba.pdc.tp.pop3;

import static ar.edu.itba.pdc.tp.util.POP3Utils.asErrLine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;

class POP3Connector implements TCPEventHandler {
    private static final Logger LOGGER = Logger.getLogger(POP3Connector.class);

    private static final String UNABLE_TO_CONNECT_MSG = asErrLine("unable to connect");

    POP3Connector() {
    }

    @Override
    public void handle(SelectionKey key) throws IOException {
        POP3ProxyState state = (POP3ProxyState) key.attachment();
        SocketChannel originChannel = (SocketChannel) key.channel();

        try {
            if (originChannel.finishConnect()) {
                // nos interesa cualquier cosa que venga de cualquiera de las
                // dos puntas
                LOGGER.info("Channel "
                        + state.getClientChannel().getRemoteAddress()
                        + " connected to origin server");
                state.updateSubscription(key.selector());
            } else {
                state.closeChannels();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to connect to origin server("
                    + originChannel.getRemoteAddress() + "): " + e.getMessage());
            // state.closeChannels();
            ByteBuffer clientBuffer = state.getClientBuffer();
            writeMsg(clientBuffer, UNABLE_TO_CONNECT_MSG);
            state.updateSubscription(key.selector());
        }
    }

    private void writeMsg(ByteBuffer toClientBuffer, String msg) { // XXX
        // toClientBuffer.clear();
        // toClientBuffer.put(msg.getBytes());
    }
}
