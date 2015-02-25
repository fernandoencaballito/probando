package ar.edu.itba.pdc.tp.pop3;

import static ar.edu.itba.pdc.tp.util.NIOUtils.closeQuietly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.admin.AdminProtocolState;

public class POP3ProxyState {

	private static final int MAX_LINE_LEN = 1000;

	private SocketChannel clientChannel;
	private SocketChannel originChannel;
	private final int BUFF_SIZE = 4 * 1024;

	private final ByteBuffer toOriginBuffer = ByteBuffer.allocate(BUFF_SIZE);
	private final ByteBuffer toClientBuffer = ByteBuffer.allocate(BUFF_SIZE);

	public States state = States.AUTHENTICATION;

	private ByteBuffer lastLine = null;
	private int readCount = 0;
	private boolean lineDone = false;
	private boolean carriageReturn = false; // Indica si ya pase un por un '\r'
	private boolean sendingCapa = false;

	protected POP3ProxyState(final SocketChannel clientChannel,
			final SocketChannel originChannel) {
		this.clientChannel = clientChannel;
		this.originChannel = originChannel;
	}

	/* obtiene el buffer donde se deben dejar los bytes leidos */
	public ByteBuffer readBufferFor(final SocketChannel channel) {
		final ByteBuffer ret;

		// no usamos equals porque la comparacion es suficiente por instancia
		if (clientChannel == channel) {
			ret = toOriginBuffer;
		} else if (originChannel == channel) {
			ret = toClientBuffer;
		} else {
			throw new IllegalArgumentException("Unknown socket");
		}

		return ret;
	}

	public ByteBuffer writeBufferFor(SocketChannel channel) {
		final ByteBuffer ret;

		// no usamos equals porque la comparacion es suficiente por instancia
		if (clientChannel == channel) {
			ret = toClientBuffer;
		} else if (originChannel == channel) {
			ret = toOriginBuffer;
		} else {
			throw new IllegalArgumentException("Unknown socket");
		}

		return ret;
	}

	/* cierra los canales. */
	public void closeChannels() throws IOException {
		closeQuietly(clientChannel);
		closeQuietly(originChannel);
	}

	public void setChannels(SocketChannel clientChannel,
			SocketChannel originChannel) {
		this.clientChannel = clientChannel;
		this.originChannel = originChannel;
	}

	public void setOriginChannel(SocketChannel originChannel) {
		this.originChannel = originChannel;
	}

	public void updateSubscription(Selector selector)
			throws ClosedChannelException {
		int originFlags = 0;
		int clientFlags = 0;

		if (toOriginBuffer.hasRemaining()) {
			clientFlags |= SelectionKey.OP_READ;
		}
		if (toClientBuffer.hasRemaining()) {
			originFlags |= SelectionKey.OP_READ;
		}

		if (toOriginBuffer.position() > 0) {
			originFlags |= SelectionKey.OP_WRITE;
		}
		if (toClientBuffer.position() > 0) {
			clientFlags |= SelectionKey.OP_WRITE;
		}

		clientChannel.register(selector, clientFlags, this);
		if (originChannel != null && originChannel.isOpen()) {
			originChannel.register(selector, originFlags, this);
		}
	}

	public SocketChannel getClientChannel() {
		return clientChannel;
	}

	public SocketChannel getOriginChannel() {
		return originChannel;
	}

	public ByteBuffer getToClientBuffer() {
		return toClientBuffer;
	}

	public ByteBuffer getToOriginBuffer() {
		return toOriginBuffer;
	}

	public States getState() {
		return state;
	}

	public void setState(States state) {
		this.state = state;
	}

	public ByteBuffer getLastLine() {
		return lastLine;
	}

	public int getReadCount() {
		return readCount;
	}

	public boolean isLineDone() {
		return lineDone;
	}

	public ByteBuffer flushLastLine() {
		if (lastLine == null) {
			lastLine = ByteBuffer.allocate(MAX_LINE_LEN);
		} else {
			lastLine.clear();
		}
		readCount = 0;
		lineDone = false;
		return lastLine;
	}

	public void putChar(byte b) {
		// lastLine.putChar(1, c);
		char c = (char) b;
		lastLine.put(b);
		readCount++;

		if (carriageReturn && c == '\n') {
			lineDone = true;
			return;
		} else {
			carriageReturn = false;
		}

		if (c == '\r') {
			carriageReturn = true;
		}

		lineDone = (readCount == MAX_LINE_LEN);
	}

	public void setSendingCapa(boolean b) {
		this.sendingCapa = b;
	}

	public boolean getSendingCapa() {
		return this.sendingCapa;
	}

	enum States {
		EXPECT_USER_OK, EXPECT_PASS_OK, EXPECT_RETR_DATA, GREETING, TRANSACTION, AUTHENTICATION, QUITTING
	}
}
