package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.NIOUtils.closeQuietly;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ar.edu.itba.pdc.tp.XML.FromClientParser;
import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XML.User;
import ar.edu.itba.pdc.tp.email.EmailConverter;

public class XMPPproxyState {
	private static final int BUFF_SIZE = 4 * 1024;

	private  ByteBuffer originBuffer ;
	
	private  ByteBuffer clientBuffer = ByteBuffer.allocate(BUFF_SIZE);

	private final SocketChannel clientChannel;
	private SocketChannel originChannel = null;

	private GenericParser clientParser;
	
	private GenericParser serverParser;
	private User user;

	XMPPproxyState(final SocketChannel clientChannel) throws FileNotFoundException, XMLStreamException {
		this.clientChannel = clientChannel;
			}

	
	void closeChannels() throws IOException {
		closeQuietly(clientChannel);
		if (originChannel != null) {
			closeQuietly(originChannel);
		}
	}

	void setOriginChannel(SocketChannel originChannel) {
		if (this.originChannel != null) {
			throw new IllegalStateException();
		}
		this.originChannel = originChannel;
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


	public GenericParser getClientParser() throws FileNotFoundException, XMLStreamException {
		if(clientParser==null)
			this.clientParser=new FromClientParser(clientBuffer);
		
		return clientParser;
		
	}
	
	


	public GenericParser getServerParser() {
		return serverParser;
	}


	public void setUser(User user) {
		this.user=user;
		
	}
	
	public String getUserName(){
		return this.user.getUsername();
		
	}
}