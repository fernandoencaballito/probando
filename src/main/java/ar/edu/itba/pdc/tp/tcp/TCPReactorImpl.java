package ar.edu.itba.pdc.tp.tcp;

import static ar.edu.itba.pdc.tp.util.NIOUtils.getLocalPort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class TCPReactorImpl implements TCPReactor {
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
	private static final Logger LOGGER = Logger.getLogger(TCPReactorImpl.class);

	private Map<Integer, TCPProtocol> handlersByPort;

	// since channels don't have either hashcode or equals
	private Map<SocketChannel, TCPProtocol> handlersByChannel = new IdentityHashMap<>();

	private final int timeout;
	private boolean stop = true;

	private String localIp;
	// IMPORTANT: just for tests
	public TCPReactorImpl(int timeout, Map<Integer, TCPProtocol> handlersByPort,String localIp) {
		if (timeout <= 0) {
			throw new IllegalArgumentException();
		}
		this.timeout = timeout;
		this.handlersByPort = handlersByPort;
		this.localIp=localIp;
	}

	public TCPReactorImpl(Map<Integer, TCPProtocol> handlersByPort,String localIp) {
		this(TIMEOUT, handlersByPort,localIp);
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
//			LOGGER.info("KEYS:");
//			for (SelectionKey key : selector.selectedKeys()) {
//				System.out.println(new ToStringBuilder(key)
//						.append("valid", key.isValid())
//						.append("acceptable", key.isAcceptable())
//						.append("readable", key.isReadable())
//						.append("writable", key.isWritable())
//						.append("connectable", key.isConnectable())
//						.append("channel", key.channel())
//						.append("attachment", key.attachment()).toString());
//			}
			handleEvents(selector.selectedKeys().iterator());
		}
	}

	// Create listening socket channel for each port and register selector
	protected void startListeners(Selector selector) throws IOException {
		for (Entry<Integer, TCPProtocol> entry : handlersByPort.entrySet()) {
			ServerSocketChannel listenChannel = ServerSocketChannel.open();
			listenChannel.socket().bind(
					new InetSocketAddress(localIp, entry.getKey()));
			// must be nonblocking to register
			listenChannel.configureBlocking(false);
			listenChannel.register(selector, SelectionKey.OP_ACCEPT);
			LOGGER.info("listener started on "
					+ listenChannel.getLocalAddress());
		}
	}

	// to be overriden by subclasses
	protected int waitForEvents(Selector selector) throws IOException {
		return selector.select(timeout);
	}

	protected void handleEvents(Iterator<SelectionKey> keyIter)
			throws IOException {
		while (keyIter.hasNext()) {
			SelectionKey key = keyIter.next();

			TCPProtocol protocolHandler = getProtocolHandler(key.channel());
			if (protocolHandler != null) {

				if (key.isValid() && key.isAcceptable()) {
					System.out.println("entro accept");
					protocolHandler.handleAccept(key);
				}

				if (key.isValid() && key.isConnectable()) {
					protocolHandler.handleConnect(key);
					System.out.println("entro connect");
				}

				if (key.isValid() && key.isReadable()) {
					protocolHandler.handleRead(key);
					System.out.println("entro read");
				}

				if (key.isValid() && key.isWritable()) {
					protocolHandler.handleWrite(key);
					System.out.println("entro write");
				}
			}
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

	@Override
	public void subscribeChannel(SocketChannel channel, TCPProtocol handler) {
		handlersByChannel.put(channel, handler);
	}

	@Override
	public void unsubscribeChannel(SocketChannel channel) {
		handlersByChannel.remove(channel);
	}
}