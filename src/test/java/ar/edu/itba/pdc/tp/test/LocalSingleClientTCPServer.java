package ar.edu.itba.pdc.tp.test;

import static ar.edu.itba.pdc.tp.util.NIOUtils.closeQuietly;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class LocalSingleClientTCPServer extends TCPSocketUser implements
		Runnable {
	private InetSocketAddress address;
	private ServerSocketChannel listenChannel;
	private SocketChannel toClientChannel;
	private ByteBuffer buffer;
	private boolean connected = false;

	public LocalSingleClientTCPServer(int port, int bufferSize, boolean blocking)
			throws IOException {
		this.address = new InetSocketAddress("localhost", port);
		this.buffer = ByteBuffer.allocate(bufferSize);
		this.listenChannel = ServerSocketChannel.open();
		listenChannel.configureBlocking(blocking);
		listenChannel.socket().bind(address);
	}

	public SocketChannel getToClientChannel() {
		return toClientChannel;
	}

	@Override
	public void run() {
		try {
			int i = 0;
			System.out.println("Server: accepting - listen channel:"
					+ listenChannel);
			while ((toClientChannel = listenChannel.accept()) == null) {
				if ((i %= 3000) == 0) {
					System.out
							.println("Server: waiting on accept - listen channel:"
									+ listenChannel);
					// System.out.println("*");
				}
				i++;
			}
			toClientChannel.configureBlocking(false);
			connected = true;
			System.out
					.println("Server: connected - channel:" + toClientChannel);
			return;
		} catch (IOException e) {
			System.out.println("Server: error " + e);
			close();
			throw new RuntimeException(e);
		}
	}

	public void close() {
		closeQuietly(listenChannel);
		closeQuietly(toClientChannel);
	}

	@Override
	public ByteBuffer getBuffer() {
		return buffer;
	}

	@Override
	public SocketChannel getChannel() {
		return toClientChannel;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}
}
