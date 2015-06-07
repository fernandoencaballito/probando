package ar.edu.itba.pdc.tp.XMPP;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;
import ar.edu.itba.pdc.tp.util.NIOUtils;

class XMPPWriter implements TCPEventHandler {
    private static final Logger LOGGER = Logger.getLogger(XMPPWriter.class);
    private AdminModule adminModule;

    XMPPWriter(AdminModule adminMod) {
        this.adminModule = adminMod;
    }

    @Override
    public void handle(SelectionKey key)  {
    	  /*
         * Channel is available for writing, and key is valid (i.e., client
         * channel not closed).
         */
        // Retrieve data read earlier

    	final XMPPproxyState proxyState = (XMPPproxyState) key.attachment();
    	final SocketChannel channel = (SocketChannel) key.channel();
    	ByteBuffer buf = (ByteBuffer) proxyState.getWriteBuffer(channel);
        buf.flip(); // Prepare buffer for writing
        SocketChannel writeChannel = (SocketChannel) key.channel();
        try {
			LOGGER.info(writeChannel.getLocalAddress() + " writing to " + writeChannel.getRemoteAddress());
			adminModule.addBytesTransfered((buf.limit()-buf.position()));
			writeChannel.write(buf);
			
	        
	        if(proxyState.hasToReset()){
	        	proxyState.resetStream();
	        }else{
	        	buf.compact(); // Make room for more data to be read in
		        
	        }
	        
	        
//	        proxyState.updateSubscription(key.selector());
	        writeChannel.register(key.selector(), SelectionKey.OP_READ, proxyState);
			
		} catch (IOException | XMLStreamException e) {
			LOGGER.error("Can't write on selected channel");
		}
//        if (!buf.hasRemaining()) { // Buffer completely written?
//            // Nothing left, so no longer interested in writes
//            key.interestOps(SelectionKey.OP_READ);
//        }

    }
}
