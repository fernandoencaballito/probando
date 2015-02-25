package ar.edu.itba.pdc.tp.tcp;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

public interface TCPProtocol {
	TCPConnectionData handleAccept(SelectionKey key) throws IOException;

	void handleConnect(SelectionKey key) throws IOException;

	void handleRead(SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel)
			throws IOException;

	void handleWrite(SelectionKey key) throws IOException;
}