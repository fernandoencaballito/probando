package ar.edu.itba.pdc.tp.test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class LocalSingleClientTCPConnection {
	private TCPClient client;
	private LocalSingleClientTCPServer server;

	public LocalSingleClientTCPConnection(int port, int bufferSize,
			boolean blocking) throws IOException {
		this.server = new LocalSingleClientTCPServer(port, bufferSize, blocking);
		this.client = new TCPClient(new InetSocketAddress("localhost", port),
				bufferSize, blocking);

		new Thread(server).start();
		new Thread(client).start();
	}

	public TCPClient getClient() {
		return client;
	}

	public LocalSingleClientTCPServer getServer() {
		return server;
	}

	public boolean isReady() {
		return client.getToServerChannel() != null
				&& server.getToClientChannel() != null;
	}

	public void close() {
		client.close();
		server.close();
	}
}
