package ar.edu.itba.pdc.tp.tcp;

import static ar.edu.itba.pdc.tp.util.NIOUtils.getLocalPort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TCPReactor {
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

	private Map<Integer, TCPProtocol> handlersByPort;
	private Map<SocketChannel, TCPProtocol> handlersByChannel = new HashMap<>();

	private final int timeout;
	private boolean stop = true;
	private static final Logger log = Logger.getLogger(TCPReactor.class);

	// IMPORTANT: just for tests
	public TCPReactor(int timeout, Map<Integer, TCPProtocol> handlersByPort) {
		if (timeout <= 0) {
			throw new IllegalArgumentException();
		}
		this.timeout = timeout;
		this.handlersByPort = handlersByPort;

		PropertyConfigurator.configure("src/main/resources/log4j.properties");
	}

	public TCPReactor(Map<Integer, TCPProtocol> handlersByPort) {
		this(TIMEOUT, handlersByPort);
	}

	public void start() throws IOException {
		stop = false;
		// Create a selector to multiplex listening sockets and connections
		Selector selector = Selector.open();

		startListeners(selector);

		while (!stop) {
			// Wait for some channel to be ready (or timeout)
			if (waitForEvents(selector) == 0) { // returns # of ready chans
				continue;
			}
			handleEvents(selector.selectedKeys().iterator());
		}
	}

	// Create listening socket channel for each port and register selector
	protected void startListeners(Selector selector) throws IOException {
		for (Entry<Integer, TCPProtocol> entry : handlersByPort.entrySet()) {
			ServerSocketChannel listenChannel = ServerSocketChannel.open();
			listenChannel.socket().bind(
					new InetSocketAddress("localhost", entry.getKey()));
			// must be nonblocking to register
			listenChannel.configureBlocking(false);
			listenChannel.register(selector, SelectionKey.OP_ACCEPT);
			log.info("listener started on " + listenChannel.getLocalAddress());
		}
	}

	// to be overriden by subclasses
	protected int waitForEvents(Selector selector) throws IOException {
		return selector.select(timeout);
	}

	protected void handleEvents(Iterator<SelectionKey> keyIter)
			throws IOException {
		while (keyIter.hasNext()) {
			SelectionKey key = keyIter.next(); // Key is bit mask
			// Server socket channel has pending connection requests?
			log.info("channel (" + key.channel() + ")");

			TCPProtocol protocolHandler = getProtocolHandler(key.channel());
			log.info("handler (" + key.channel() + ")");
			if (protocolHandler != null) {
				log.info("protocol " + protocolHandler);
				if (key.isValid() && key.isAcceptable()) {
					log.info("accept (Start)");
					TCPConnectionData connectionData = protocolHandler
							.handleAccept(key);
					handlersByChannel.put(connectionData.getClientChannel(),
							protocolHandler);
					// handlersByChannel.put(connectionData.getOriginChannel(),protocolHandler);
					log.info(handlersByChannel);
					log.info("accept (End)");
				}
				// Client socket channel has pending data?
				if (key.isValid() && key.isReadable()) {
					log.info("read (Start)");
					protocolHandler.handleRead(key, handlersByChannel);
					log.info("read (End)");
				}
				// Client socket channel is available for writing and
				// key is valid (i.e., channel not closed)?
				if (key.isValid() && key.isWritable()) {
					log.info("write (Start)");
					protocolHandler.handleWrite(key);
					log.info("write (End)");
				}
				if (key.isValid() && key.isConnectable()) {
					log.info("connect (start)");
					protocolHandler.handleConnect(key);
					log.info("connect (End)");
				}
			} else {
				log.info("no handler found for channel" + key.channel());
			}
			// FIXME: handlear caso que se desconecte o falle la conexion.
			// Deberia des-suscribir el puerto (si no voy a estar teniendo
			// puertos que ya no van mas asignados a handlers)
			keyIter.remove(); // remove from set of selected keys
		}
	}

	private TCPProtocol getProtocolHandler(SelectableChannel channel)
			throws IOException {
		try {
			return handlersByChannel.get((SocketChannel) channel);
		} catch (ClassCastException e) {
			return getProtocolHandler((ServerSocketChannel) channel);
		}
	}

	private TCPProtocol getProtocolHandler(ServerSocketChannel channel)
			throws IOException {
		int port = getLocalPort(channel);
		return handlersByPort.get(port);
	}

	protected void addProtocolHandler(SocketChannel channel,
			TCPProtocol protocol) {
		handlersByChannel.put(channel, protocol);
	}

	public void stop() {
		this.stop = true;
	}
}