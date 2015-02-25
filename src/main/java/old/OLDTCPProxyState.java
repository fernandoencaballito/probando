package old;

import static ar.edu.itba.pdc.tp.util.NIOUtils.closeQuietly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class OLDTCPProxyState {

	private SocketChannel clientChannel;
	private SocketChannel originChannel;
	private final int BUFF_SIZE = 4 * 1024;
	public final ByteBuffer toOriginBuffer = ByteBuffer.allocate(BUFF_SIZE);
	public final ByteBuffer toClientBuffer = ByteBuffer.allocate(BUFF_SIZE);

	protected OLDTCPProxyState(final SocketChannel clientChannel,
			final SocketChannel originChannel) {
		// if (clientChannel == null || originChannel == null) {
		// throw new IllegalArgumentException();
		// }
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
		if (originChannel != null && originChannel.isOpen())
			originChannel.register(selector, originFlags, this);
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
}
