package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.NIOUtils.closeQuietly;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HostnameVerifier;
import javax.print.attribute.standard.Severity;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ar.edu.itba.pdc.tp.XML.FromClientParser;
import ar.edu.itba.pdc.tp.XML.FromServerParser;
import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XML.User;

public class XMPPproxyState {
	private static final int BUFF_SIZE = 4 * 1024;

	private  ByteBuffer originBuffer= ByteBuffer.allocate(BUFF_SIZE); ;
	
	private  ByteBuffer clientBuffer = ByteBuffer.allocate(BUFF_SIZE);

	private final SocketChannel clientChannel;
	private SocketChannel originChannel = null;

	private FromClientParser clientParser;
	
	private FromServerParser serverParser;
	private User user;

	private boolean hasToResetStreams=false;
	private boolean hastToResetClientParser=false;

	private boolean hastToResetOriginParser=false;
	XMPPproxyState(final SocketChannel clientChannel) throws FileNotFoundException, XMLStreamException {
		this.clientChannel = clientChannel;
			}

	
	void closeChannels() throws IOException {
		closeQuietly(clientChannel);
		if (originChannel != null) {
			closeQuietly(originChannel);
		}
	}

	public boolean hasToReset(){
		return hasToResetStreams;
	}
	public void flagReset(){
		hasToResetStreams=true;
	}
	
	void setOriginChannel(SocketChannel originChannel) throws FileNotFoundException, XMLStreamException {
		if (this.originChannel != null) {
			throw new IllegalStateException();
		}
		this.originChannel = originChannel;
//		this.serverParser= new FromServerParser(originBuffer);
	}


	SocketChannel getClientChannel() {
		return clientChannel;
	}

	SocketChannel getOriginChannel() {
		return originChannel;
	}

	ByteBuffer getClientBuffer() {
		return clientBuffer;
	}

	ByteBuffer getOriginBuffer() {
		return originBuffer;
	}

	public String getUserPlainAuth(){
		return user.getPlainAuth();
	}
	





	boolean isConnectedToOrigin() {
		return originChannel != null && originChannel.isOpen()
				&& originChannel.isConnected();
	}

	
	enum States {
		EXPECT_USER_OK, EXPECT_PASS_OK, EXPECT_RETR_DATA, GREETING, TRANSACTION, QUITTING, AUTHENTICATION
	}

	
	ByteBuffer getReadBuffer(SocketChannel channel) {
		if (clientChannel == channel) {
			return clientBuffer;
		}
		if (originChannel == channel) {
			return originBuffer;
		}
		throw new IllegalArgumentException("Unknown socket");
	}
	
	

	// *proxy's* write, that is, where the other end will *read*
	ByteBuffer getWriteBuffer(final SocketChannel channel) {
		if (clientChannel == channel) {
			
				return clientBuffer;
			}
		
		if (originChannel == channel) {
			return originBuffer;
		}
		throw new IllegalArgumentException("Unknown socket");
	}
	
	
	void updateSubscription(Selector selector) throws ClosedChannelException {
		int originFlags = 0;
		int clientFlags = 0;

//		if (originBuffer!=null && originBuffer.hasRemaining()) {
//			originFlags |= SelectionKey.OP_READ;
//		}

		if (clientBuffer!=null && clientBuffer.hasRemaining()) {
			clientFlags |= SelectionKey.OP_READ;
		}

		if (clientBuffer!=null && clientBuffer.position() > 0) {
			clientFlags |= SelectionKey.OP_WRITE;
		}

//		if (originBuffer!=null && originBuffer.position() > 0 ) {
//			clientFlags |= SelectionKey.OP_WRITE;
//		}

		clientChannel.register(selector, clientFlags, this);
		if (isConnectedToOrigin()) {
			originChannel.register(selector, originFlags, this);
		}
	}


	public FromClientParser getClientParser() throws FileNotFoundException, XMLStreamException {
		if(clientParser==null)
			this.clientParser=new FromClientParser(clientBuffer);
		else if(hastToResetClientParser){
			hastToResetClientParser=false;
			clientParser.reset(clientBuffer);
		}
		
		return clientParser;
		
	}
	
	


	public FromServerParser getServerParser() throws FileNotFoundException, XMLStreamException {
		if(serverParser==null)
			serverParser=new FromServerParser(originBuffer);
		else if(hastToResetOriginParser){
			hastToResetOriginParser=false;
			serverParser.reset(originBuffer);
		}
		return serverParser;
	}


	public void setUser(User user) {
		this.user=user;
		
	}
	
	public String getUserName(){
		return this.user.getUsername();
		
	}


	public void resetStream() throws FileNotFoundException, XMLStreamException {
		if(hasToResetStreams){
			hasToResetStreams=false;
			hastToResetOriginParser=true;
			originBuffer.clear();
//			originBuffer= ByteBuffer.allocate(BUFF_SIZE); ;
//			serverParser=serverParser.reset(originBuffer);
			 
			hastToResetClientParser=true;
			clientBuffer.clear();
//			clientBuffer = ByteBuffer.allocate(BUFF_SIZE);
//			clientParser=clientParser.reset(clientBuffer);
				
		}
		
		
		
	}


	


	
}