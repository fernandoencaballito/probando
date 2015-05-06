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
    	  /*
         * Channel is available for writing, and key is valid (i.e., client
         * channel not closed).
         */
        // Retrieve data read earlier
    	
    	final POP3ProxyState proxyState = (POP3ProxyState) key.attachment();
        
    	final SocketChannel channel = (SocketChannel) key.channel();
    	
    	ByteBuffer buf = (ByteBuffer) proxyState.getWriteBuffer(channel);
        buf.flip(); // Prepare buffer for writing
        SocketChannel writeChannel = (SocketChannel) key.channel();
        writeChannel.write(buf);
//        if (!buf.hasRemaining()) { // Buffer completely written?
//            // Nothing left, so no longer interested in writes
//            key.interestOps(SelectionKey.OP_READ);
//        }
        buf.compact(); // Make room for more data to be read in
    
    }
}
