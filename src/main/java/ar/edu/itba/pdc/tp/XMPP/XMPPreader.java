package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.NIOUtils.append;
import static ar.edu.itba.pdc.tp.util.NIOUtils.nonBlockingSocket;
import static ar.edu.itba.pdc.tp.util.POP3Utils.ERR;
import static ar.edu.itba.pdc.tp.util.POP3Utils.OK;
import static ar.edu.itba.pdc.tp.util.POP3Utils.asErrLine;
import static ar.edu.itba.pdc.tp.util.POP3Utils.asMultilines;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.email.EmailConverter;
import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.NIOUtils;

class XMPPreader implements TCPEventHandler {
    private static final String CAPA = "CAPA";
    private static final String USER = "USER";
    private static final String PASS = "PASS";
    private static final String RETR = "RETR";
    private static final String QUIT = "QUIT";
    private static final String AUTH = "AUTH";

    private static final String CAPA_TRANSACTION_MSG = asMultilines(CAPA, USER,
            "LIST", RETR, "DELE", "RSET", "STAT", "NOOP");
    private static final String CAPA_AUTHENTICATION_MSG = asMultilines(CAPA,
            USER);

    private static final Logger LOGGER = Logger.getLogger(XMPPreader.class);

    private static final String ERR_MSG = asErrLine("");

    private final TCPReactor reactor;
    private final AdminModule adminModule;
    private final XMPproxy parent;

    XMPPreader(XMPproxy parent, TCPReactor reactor, AdminModule adminModule) {
        this.parent = parent;
        this.reactor = reactor;
        this.adminModule = adminModule;
    }

    @Override
    public void handle(SelectionKey key) throws IOException {
       
    	final XMPPproxyState proxyState = (XMPPproxyState) key.attachment();

        final SocketChannel readChannel = (SocketChannel) key.channel();
        final ByteBuffer readBuffer = proxyState.getReadBuffer(readChannel);
        
        
        SocketChannel writeChannel=null;
        
        if (readChannel == proxyState.getClientChannel()){
        	//cliente solicita lectura!!
        	writeChannel=proxyState.getOriginChannel();
        }else{
        	//servidor solicita lectura, se escribe a cliente
        	writeChannel=proxyState.getClientChannel();
        	
        	
        }
        
//        long bytesRead =writeChannel.read(readBuffer);
        long bytesRead =readChannel.read(readBuffer);
        
        if (bytesRead == -1) { // Did the other end close?
        	proxyState.closeChannels();
        	reactor.unsubscribeChannel(proxyState.getClientChannel());
            reactor.unsubscribeChannel(proxyState.getOriginChannel());
        } else if (bytesRead > 0) {
            // Indicate via key that reading are both of interest now.
            //key.interestOps(SelectionKey.OP_READ );
            proxyState.updateSubscription(key.selector());
            if(proxyState.getOriginChannel()==null){
            connectToOrigin(key, proxyState,"hola");
            }
            
        }
        
        
        
    }

	private void connectToOrigin(SelectionKey key, XMPPproxyState proxyState,
			String user) {
		try {
            InetSocketAddress originAddress = adminModule.getOriginAddressForUser(user);
            SocketChannel originChannel = nonBlockingSocket(originAddress);
            proxyState.setOriginChannel(originChannel);
            originChannel.register(key.selector(), SelectionKey.OP_CONNECT,
                    proxyState);
            reactor.subscribeChannel(originChannel, parent);
        } catch (IOException e) {
           // sendResponseToClient(proxyState, ERR);
        }
   
	}
		
	
	
}


    
    
    
    
    
    

