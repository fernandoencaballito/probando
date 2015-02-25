package ar.edu.itba.pdc.tp.pop3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;
import ar.edu.itba.pdc.tp.util.NIOUtils;

class POP3Writer implements TCPEventHandler {
    private static final Logger LOGGER = Logger.getLogger(POP3Writer.class);
    private AdminModule adminModule;

    POP3Writer(AdminModule adminMod) {
        this.adminModule = adminMod;
    }

    @Override
    public void handle(SelectionKey key) throws IOException {
        final POP3ProxyState proxyState = (POP3ProxyState) key.attachment();
        final SocketChannel channel = (SocketChannel) key.channel();

        final ByteBuffer writeBuffer = proxyState.getWriteBuffer(channel);
        ByteBuffer duplicateBuffer = (ByteBuffer) writeBuffer.duplicate()
                .flip();
        if (proxyState.getClientChannel() == channel) {
            if (proxyState.getOriginChannel() != null
                    && (duplicateBuffer.get() == '+' || duplicateBuffer.get() == '-')) {
                LOGGER.info("Origin("
                        + proxyState.getOriginChannel().getRemoteAddress()
                        + ") wrote to client("
                        + channel.getRemoteAddress()
                        + "): "
                        + StringEscapeUtils.escapeJava(NIOUtils
                                .getFirstLine(duplicateBuffer)));

            } else if (duplicateBuffer.get() == '+'
                    || duplicateBuffer.get() == '-') {
                LOGGER.info("ProxyPOP3 wrote to client("
                        + channel.getRemoteAddress()
                        + "): "
                        + StringEscapeUtils.escapeJava(NIOUtils
                                .getFirstLine((ByteBuffer) writeBuffer
                                        .duplicate().flip())));
            }
        } else {
            LOGGER.info("Client("
                    + proxyState.getClientChannel().getRemoteAddress()
                    + ") wrote to origin("
                    + channel.getRemoteAddress()
                    + "): "
                    + StringEscapeUtils.escapeJava(NIOUtils
                            .getFirstLine((ByteBuffer) writeBuffer.duplicate()
                                    .flip())));
        }

        writeBuffer.flip(); // Prepare buffer for writing
        int amount = channel.write(writeBuffer);
        writeBuffer.compact(); // Make room for more data to be read in
        adminModule.addBytesTransfered(amount);
        proxyState.updateSubscription(key.selector());
    }
}
