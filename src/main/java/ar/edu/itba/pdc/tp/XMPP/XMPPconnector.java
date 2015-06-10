package ar.edu.itba.pdc.tp.XMPP;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;

class XMPPconnector implements TCPEventHandler {
    private static final Logger LOGGER = Logger.getLogger(XMPPconnector.class);


    XMPPconnector() {
    }

    @Override
    public void handle(SelectionKey key) throws IOException {
        XMPPproxyState state = (XMPPproxyState) key.attachment();
        SocketChannel originChannel = (SocketChannel) key.channel();
        try {
            if (originChannel.finishConnect()) {
               
                LOGGER.info("Channel "
                        + state.getClientChannel().getRemoteAddress()
                        + " connected to origin server "+state.getOriginChannel().getRemoteAddress());
//                state.getServerParser().initiateStream(state,key.selector());
                XMPPlistener.finishConnectToOrigin(state, key.selector());
                state.getClientParser().announceCorrectConnectToOrigin();
            } else {
            	//TODO 
                state.closeChannels();
            }
        } catch (IOException | XMLStreamException e) {
            LOGGER.error("Failed to connect to origin server: "
                     + e.getMessage());
            
            XMPPlistener.announceFailedConnectionToOrigin(state,key.selector());
        }
        

       
    }

    
}
