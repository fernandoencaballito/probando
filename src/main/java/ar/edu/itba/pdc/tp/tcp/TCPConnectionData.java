package ar.edu.itba.pdc.tp.tcp;

import java.nio.channels.SocketChannel;

public class TCPConnectionData {
	private SocketChannel clientChannel;
	private SocketChannel originChannel;

	/* for non proxy connections */
	public TCPConnectionData(SocketChannel clientChannel) {
		this(clientChannel, null);
	}

	/* for proxy connections */
	public TCPConnectionData(SocketChannel clientChannel,
			SocketChannel originChannel) {
		this.clientChannel = clientChannel;
		this.originChannel = originChannel;
	}

	public SocketChannel getClientChannel() {
		return clientChannel;
	}

	public SocketChannel getOriginChannel() {
		return originChannel;
	}
}
