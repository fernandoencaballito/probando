package ar.edu.itba.pdc.tp.pop3;

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

class POP3Reader implements TCPEventHandler {
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

    private static final Logger LOGGER = Logger.getLogger(POP3Reader.class);

    private static final String ERR_MSG = asErrLine("");

    private final TCPReactor reactor;
    private final AdminModule adminModule;
    private final POP3Proxy parent;

    POP3Reader(POP3Proxy parent, TCPReactor reactor, AdminModule adminModule) {
        this.parent = parent;
        this.reactor = reactor;
        this.adminModule = adminModule;
    }

    @Override
    public void handle(SelectionKey key) throws IOException {
       
    	final POP3ProxyState proxyState = (POP3ProxyState) key.attachment();

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
        
        writeChannel.read(readBuffer);
        writeChannel.register(key.selector(), SelectionKey.OP_WRITE,
                proxyState);
        reactor.subscribeChannel(writeChannel, parent);
        
    }

    
    
    
    
    
    
   
}
