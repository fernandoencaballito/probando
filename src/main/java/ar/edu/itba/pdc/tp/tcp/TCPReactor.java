package ar.edu.itba.pdc.tp.tcp;

import java.nio.channels.SocketChannel;

public interface TCPReactor {
	void subscribeChannel(SocketChannel channel, TCPProtocol handler);

	void unsubscribeChannel(SocketChannel channel);
}
