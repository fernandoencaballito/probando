package ar.edu.itba.pdc.tp.test;

import static ar.edu.itba.pdc.tp.util.NIOUtils.closeQuietly;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class BlockingTCPClient implements Runnable {
	private SocketChannel toServerChannel;
	private InetSocketAddress serverAddress;
	private boolean blocking;

	public BlockingTCPClient(InetSocketAddress serverAddress, boolean blocking) {
		this.serverAddress = serverAddress;
		this.blocking = blocking;
	}

	public SocketChannel getToServerChannel() {
		return toServerChannel;
	}

	// connecting does block the client, but this is just for usability
	public void connect() throws IOException {
		toServerChannel = SocketChannel.open();
		toServerChannel.configureBlocking(blocking);

		System.out.println("Client: trying to connect - address: "
				+ serverAddress);
		if (!toServerChannel.connect(serverAddress)) {
			System.out.println("Client: connection failed - address: "
					+ serverAddress);
			while (!toServerChannel.finishConnect()) {
				System.out
						.println("Client: reattempting to connect - address: "
								+ serverAddress);
			}
		}
		System.out.println("Client: connected - channel " + toServerChannel);
		return;
	}

	@Override
	public void run() {
		try {
			connect();
		} catch (IOException e) {
			System.out.println("Client: error " + e);
			close();
			throw new RuntimeException(e);
		}
	}

	public void close() {
		closeQuietly(toServerChannel);
	}
}
