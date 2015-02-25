package old;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.tcp.TCPConnectionData;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.util.NIOUtils;

public abstract class OLDTCPProxyProtocol implements TCPProtocol {
	private InetSocketAddress originAddress;
	private static org.apache.log4j.Logger log = Logger
			.getLogger(OLDTCPProxyProtocol.class);

	// private OriginResolver originResolver;

	// public TCPProxyProtocol(OriginResolver originResolver){
	// this.originResolver=originResolver;
	// }

	public OLDTCPProxyProtocol(InetSocketAddress originAddress) {
		this.originAddress = originAddress;
	}

	// TODO : ACA NO DEBERIA ESTAR ESTA EL METODO GET ORIGIN ADDRESS
	@Override
	public TCPConnectionData handleAccept(final SelectionKey key)
			throws IOException {
		// final SocketChannel originChannel = SocketChannel.open();

		SocketChannel clientChannel = ((ServerSocketChannel) key.channel())
				.accept();
		clientChannel.configureBlocking(false); // Must be nonblocking to
												// register
		// originChannel.configureBlocking(false);
		// no nos registramos a ningun evento porque primero tenemos que
		// establecer la conexion hacia el origin server

		// Initiate connection to server and repeatedly poll until complete
		// originChannel.connect(originAddress);
		// originServer.connect(originResolver.getInetForUser(null));
		// originChannel.register(key.selector(),
		// SelectionKey.OP_CONNECT,clientChannel);

		// return new TCPConnectionData(clientChannel, originChannel);
		final OLDTCPProxyState state = createProxyState(clientChannel, null);
		state.toClientBuffer.clear();
		state.toClientBuffer.put(new String("+OK ready\r\n").getBytes());
		state.updateSubscription(key.selector());
		return new TCPConnectionData(clientChannel, null);
	}

	// To be overriden by subclasses
	protected OLDTCPProxyState createProxyState(SocketChannel clientChannel,
			SocketChannel originChannel) {
		return new OLDTCPProxyState(clientChannel, originChannel);
	}

	protected OLDTCPProxyState getProxyState(SelectionKey key) {
		return (OLDTCPProxyState) key.attachment();
	}

	@Override
	public void handleConnect(final SelectionKey key) throws IOException {
		OLDTCPProxyState state = (OLDTCPProxyState) key.attachment();

		SocketChannel originChannel = (SocketChannel) key.channel();

		try {
			boolean ret = originChannel.finishConnect();
			if (ret) {
				// nos interesa cualquier cosa que venga de cualquiera de las
				// dos puntas
				state.updateSubscription(key.selector());
			} else {
				state.closeChannels();
			}
		} catch (IOException e) {
			log.info("Failed to connect to origin server: " + e.getMessage());
			// state.closeChannels();
			// throw e;
		}
	}

	@Override
	public final void handleRead(final SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel)
			throws IOException {
		log.info("read (Start)");
		final OLDTCPProxyState proxyState = getProxyState(key);
		final SocketChannel channel = (SocketChannel) key.channel();
		log.info("leyendo de " + channel);
		final ByteBuffer buffer = proxyState.readBufferFor(channel);
		log.info("en el buffer " + buffer);
		long bytesRead = channel.read(buffer);
		log.info("lei " + bytesRead + " bytes");
		if (bytesRead == -1) { // Did the other end close?
			proxyState.closeChannels(); // TODO: probablemente aca haya que
										// retornar que hay que des-suscribir al
										// channel ese
		} else if (bytesRead > 0) {
			ByteBuffer copy = (ByteBuffer) buffer.duplicate().flip();
			// viene de ser escrito el buffer, asi que para leerlo
			// tengo que hacerle flip
			log.info("lei '" + NIOUtils.readBuffer(copy) + "'");
			boolean connectionEstablished = doNotEmptyRead(key,
					handlersByChannel);
			if (connectionEstablished)
				proxyState.updateSubscription(key.selector());
		}
		log.info("read (End)");
	}

	// to be overriden by subclasses
	abstract protected boolean doNotEmptyRead(SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel);

	@Override
	public void handleWrite(final SelectionKey key) throws IOException {
		final OLDTCPProxyState proxyState = getProxyState(key);

		doWrite(key);

		proxyState.updateSubscription(key.selector());
	}

	// To be overriden by subclasses
	protected void doWrite(final SelectionKey key) throws IOException {
		final OLDTCPProxyState proxyState = getProxyState(key);
		final SocketChannel channel = (SocketChannel) key.channel();

		final ByteBuffer buffer = proxyState.writeBufferFor(channel);

		buffer.flip(); // Prepare buffer for writing
		channel.write(buffer);
		buffer.compact(); // Make room for more data to be read in
	}

}
