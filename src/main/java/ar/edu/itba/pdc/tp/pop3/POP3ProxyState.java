package ar.edu.itba.pdc.tp.pop3;

import static ar.edu.itba.pdc.tp.util.NIOUtils.closeQuietly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ar.edu.itba.pdc.tp.email.EmailConverter;

class POP3ProxyState {
	private static final int BUFF_SIZE = 4 * 1024;

	private final ByteBuffer originBuffer = ByteBuffer.allocate(BUFF_SIZE);
	private final ByteBuffer convertedOriginBuffer = ByteBuffer
			.allocate(BUFF_SIZE);
	private final ByteBuffer clientBuffer = ByteBuffer.allocate(BUFF_SIZE);

	private final SocketChannel clientChannel;
	private SocketChannel originChannel = null;

	private States state = States.AUTHENTICATION;
	private POP3Line line = new POP3Line();

	private EmailConverter emailConverter = null;

	POP3ProxyState(final SocketChannel clientChannel) {
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

	POP3Line getLine() {
		return line;
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

	public ByteBuffer getConvertedOriginBuffer() {
		return convertedOriginBuffer;
	}

	States getState() {
		return state;
	}

	void setState(States state) {
		this.state = state;
	}

	void startEmailHeaderTransfer() {
		this.convertedOriginBuffer.clear();
		this.emailConverter = new EmailConverter();
	}

	EmailConverter getEmailConverter() {
		return emailConverter;
	}

	void endEmailHeaderTransfer() {
		this.emailConverter = null;
	}

	boolean isConnectedToOrigin() {
		return originChannel != null && originChannel.isOpen()
				&& originChannel.isConnected();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("clientBuffer", clientBuffer)
				.append("originBuffer", originBuffer)
				.append("convertedOriginBuffer", convertedOriginBuffer)
				.append("clientChannel", clientChannel)
				.append("originChannel", originChannel).append("state", state)
				.append("line", line).toString();
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
			if (convertedOriginBuffer.position() > 0) { 
				return convertedOriginBuffer;
			} else {
				return originBuffer;
			}
		}
		if (originChannel == channel) {
			return clientBuffer;
		}
		throw new IllegalArgumentException("Unknown socket");
	}
	
	
	void updateSubscription(Selector selector) throws ClosedChannelException {
		int originFlags = 0;
		int clientFlags = 0;

		if (originBuffer.hasRemaining()) {
			originFlags |= SelectionKey.OP_READ;
		}

		if (clientBuffer.hasRemaining()) {
			clientFlags |= SelectionKey.OP_READ;
		}

		if (clientBuffer.position() > 0) {
			originFlags |= SelectionKey.OP_WRITE;
		}

		if (originBuffer.position() > 0 ) {
			clientFlags |= SelectionKey.OP_WRITE;
		}

		clientChannel.register(selector, clientFlags, this);
		if (isConnectedToOrigin()) {
			originChannel.register(selector, originFlags, this);
		}
	}
}