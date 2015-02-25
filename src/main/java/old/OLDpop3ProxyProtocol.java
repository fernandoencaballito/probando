package old;

import static ar.edu.itba.pdc.tp.util.NIOUtils.readBuffer;
import static old.OLDPOP3ProxyState.States.AUTHENTICATION;
import static old.OLDPOP3ProxyState.States.EXPECT_PASS_OK;
import static old.OLDPOP3ProxyState.States.EXPECT_RETR_DATA;
import static old.OLDPOP3ProxyState.States.EXPECT_USER_OK;
import static old.OLDPOP3ProxyState.States.TRANSACTION;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

import old.OLDPOP3ProxyState.States;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.email.EmailConverter;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;

public class OLDpop3ProxyProtocol extends OLDTCPProxyProtocol {

	private static final int DEFAULT_PORT = 110;

	private static final String CAPA = "capa";
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String RETR = "retr";
	private static final String QUIT = "quit";
	private static final String AUTH = "auth";

	private static final String OK_RESP = "+ok";
	private static final String ERR_RESP = "-err";

	private static final String CAPA_TRANSACTION_MSG = "+OK\r\nCAPA\r\nUSER\r\nLIST\r\nRETR\r\nDELE\r\nRSET\r\nSTAT\r\nNOOP\r\n.\r\n";
	private static final String CAPA_AUTHENTICATION_MSG = "+OK\r\nCAPA\r\nUSER\r\n.\r\n";
	private static final String ERR_MSG = "-ERR\r\n";

	private static final Logger log = Logger
			.getLogger(OLDpop3ProxyProtocol.class);

	private AdminModule reactorState;

	private static final String CRLF = "\r\n";

	private void writeMsg(ByteBuffer toClientBuffer, String msg) {
		toClientBuffer.clear();
		toClientBuffer.put(msg.getBytes());

	}

	private void handleQuit(OLDPOP3ProxyState proxyState) {
		// primero le responde al servidor
		String msg = OK_RESP.toUpperCase() + " closing connection.\r\n";
		writeMsg(proxyState.toClientBuffer, msg);

		// luego le pasa el commando quit al origin
		String msg2 = QUIT.toUpperCase() + "\r\n";
		writeMsg(proxyState.getToOriginBuffer(), msg2);
		proxyState.setState(States.QUITTING);

	}

	private void connectToOrigin(String username, OLDPOP3ProxyState proxyState,
			SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel, String lastLine)
			throws IOException {
		InetSocketAddress sockAddr = reactorState.getOriginAddressForUser(username);

		final SocketChannel originChannel = SocketChannel.open();

		originChannel.configureBlocking(false);

		originChannel.connect(sockAddr);

		proxyState.setOriginChannel(originChannel);
		originChannel.register(key.selector(), SelectionKey.OP_CONNECT,
				proxyState);
		TCPProtocol protocol = handlersByChannel.get(proxyState
				.getClientChannel());

		handlersByChannel.put(originChannel, protocol);

		// proxyState.updateSubscription(key.selector());
	}

	@Override
	protected void doWrite(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		OLDPOP3ProxyState proxyState = getProxyState(key);

		if (proxyState.getClientChannel() == channel) {
			log.info("state - before: " + proxyState.getState());
			doClientWrite(key);
			log.info("state - after: " + proxyState.getState());
		} else if (proxyState.getOriginChannel() == channel) {
			log.info("state - before: " + proxyState.getState());
			doOriginWrite(key);
			log.info("state - after: " + proxyState.getState());
		} else {
			throw new IllegalArgumentException("Unknown socket");
		}
	}

	private void doClientWrite(SelectionKey key) throws IOException {
		OLDPOP3ProxyState proxyState = getProxyState(key);

		if (proxyState.canConvertEmail()) {
			EmailConverter converter = proxyState.getEmailConverter();
			// convierte lo que haya recibido del mail y lo pasa del buffer de
			// origin al del client
			// converter.convert();

			// esta bien hacerlo aca, no deberia hacerlo el converter porque es
			// generico
			proxyState.getToClientBuffer().compact();
		} else {
			super.doWrite(key);
		}
	}

	@Override
	public void handleWrite(final SelectionKey key) throws IOException {
		final OLDPOP3ProxyState proxyState = getProxyState(key);

		doWrite(key);
		if (proxyState.getState() == States.QUITTING) {
			key.channel().close();
		} else {
			proxyState.updateSubscription(key.selector());

		}
	}

	@Override
	public void handleConnect(final SelectionKey key) throws IOException {
		OLDPOP3ProxyState state = (OLDPOP3ProxyState) key.attachment();

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
			handleNoConnectionToOrigin(state, key);
		}
	}

	private void handleNoConnectionToOrigin(OLDPOP3ProxyState state,
			SelectionKey key) throws ClosedChannelException {
		ByteBuffer clienBuffer = state.getToClientBuffer();
		String msg = ERR_RESP + " unable to connect to your origin server"
				+ CRLF;
		writeMsg(clienBuffer, msg);
		state.updateSubscription(key.selector());
	}

	// IMPORTANT: just for tests
	public OLDpop3ProxyProtocol(InetSocketAddress originAddress,
			AdminModule reactorState) {
		super(originAddress);
		this.reactorState = reactorState;
	}

	public OLDpop3ProxyProtocol(String origin, AdminModule reactorState) {
		this(new InetSocketAddress(origin, DEFAULT_PORT), reactorState);
	}

	@Override
	protected OLDPOP3ProxyState createProxyState(SocketChannel clientChannel,
			SocketChannel originChannel) {
		return new OLDPOP3ProxyState(clientChannel, originChannel);
	}

	@Override
	protected OLDPOP3ProxyState getProxyState(SelectionKey key) {
		return (OLDPOP3ProxyState) key.attachment();
	}

	@Override
	protected boolean doNotEmptyRead(SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel) {
		SocketChannel channel = (SocketChannel) key.channel();
		boolean connectionEstablished = true;
		OLDPOP3ProxyState proxyState = getProxyState(key);

		ByteBuffer lastLineBuffer = proxyState.getLastLine();
		if (lastLineBuffer == null || !lastLineBuffer.hasRemaining()) {
			lastLineBuffer = proxyState.flushLastLine();
		}

		if (!proxyState.isLineDone()) {
			ByteBuffer bufferCopy = null;

			if (proxyState.getClientChannel() == channel) {
				bufferCopy = proxyState.getToOriginBuffer().duplicate();
			} else if (proxyState.getOriginChannel() == channel) {
				bufferCopy = proxyState.getToClientBuffer().duplicate();
			} else {
				throw new IllegalArgumentException("Unknown socket");
			}

			bufferCopy.flip(); // viene de ser escrito, asi que para ser
								// leido tiene que flippearse

			while (bufferCopy.hasRemaining() && !proxyState.isLineDone()) {
				proxyState.putChar(bufferCopy.get());
			}

			log.info("lineBuffer: '"
					+ readBuffer((ByteBuffer) proxyState.getLastLine()
							.duplicate().flip()) + "'");
		}
		// ojo, aca NO va else, va como esta
		if (!proxyState.isLineDone()) { // TODO: check!
			return connectionEstablished;

		}

		String lastLine = readBuffer((ByteBuffer) lastLineBuffer.duplicate()
				.flip());
		if (proxyState.getClientChannel() == channel) {
			log.info("state - before: " + proxyState.getState());
			connectionEstablished = doClientNotEmptyRead(proxyState, lastLine,
					key, handlersByChannel);
			proxyState.flushLastLine();
			log.info("state - after: " + proxyState.getState());
		} else if (proxyState.getOriginChannel() == channel) {
			log.info("state - before: " + proxyState.getState());
			doOriginNotEmptyRead(proxyState, lastLine);
			proxyState.flushLastLine();
			log.info("state - after: " + proxyState.getState());
		} else {
			throw new IllegalArgumentException("Unknown socket");
		}
		return connectionEstablished;
	}

	private boolean doClientNotEmptyRead(OLDPOP3ProxyState proxyState,
			String lastLine, SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel) {
		boolean connectionEstablished = true;
		/*
		 * TODO: ver que los chars que saca trim sean los que hay que sacar en
		 * POP3
		 */
		String[] params = lastLine.split(" ");
		String command = params[0].trim();
		command = command.toLowerCase();
		log.info("leo '" + lastLine + "'");
		log.info("command = '" + command + "'");
		if (QUIT.equals(command)) {

			handleQuit(proxyState);

			return true;
		}

		SocketChannel originchannel = proxyState.getOriginChannel();
		switch (proxyState.getState()) {
		case AUTHENTICATION:
			switch (command) {
			case CAPA:
				sendMsg(proxyState, CAPA_AUTHENTICATION_MSG);
				break;
			case USER:
				// se conoce el nombre usuario por primera vez

				String username = (params.length > 1) ? params[1] : "";
				connectionEstablished = false;
				try {
					connectToOrigin(username, proxyState, key,
							handlersByChannel, lastLine);
				} catch (IOException e) {
					// handleOriginConnectionError(proxyState);
				}
				proxyState.setState(EXPECT_USER_OK);
				break;
			case PASS:
				if (originchannel != null && originchannel.isOpen()) {
					// si ya esta conectado, no contestar error
					proxyState.setState(EXPECT_PASS_OK);
				} else {
					sendMsg(proxyState, ERR_MSG);// no esta conectado al origin,
													// commando invalido
				}

				break;
			case AUTH:
				sendMsg(proxyState, ERR_MSG); // No soportamos el metodo AUTH.
				break;
			default: // Dejo pasar el resto de los comandos.
				break;
			}
			break;
		case TRANSACTION:
			switch (command) {
			case CAPA:
				sendMsg(proxyState, CAPA_TRANSACTION_MSG);
				break;
			case RETR:
				proxyState.setState(EXPECT_RETR_DATA);
				break;
			case QUIT:
				// Si el server no cierra la conexion tengo que volver al estado
				// AUTHENTICATION
				proxyState.setState(AUTHENTICATION);
				break;
			default: // Dejo pasar el resto de los comandos.
				break;
			}
			break;
		default:
			throw new UnsupportedOperationException(
					"Should never happen - all client states are contemplated here");
		}
		return connectionEstablished;
	}

	private void sendMsg(OLDPOP3ProxyState proxyState, String msg) {
		ByteBuffer clientBuffer = (ByteBuffer) proxyState.getToClientBuffer()
				.clear();
		clientBuffer.put(msg.getBytes());
		// Limpio este buffer para no enviarle el comando del cliente al
		// origin.
		proxyState.getToOriginBuffer().clear();
	}

	private void doOriginNotEmptyRead(OLDPOP3ProxyState proxyState,
			String lastLine) {
		/*
		 * TODO: ver que los chars que saca trim sean los que hay que sacar en
		 * POP3
		 */
		if (lastLine.length() == 0)
			return;
		// String command = lastLine.substring(0, 4).trim().toLowerCase();
		String command = (lastLine.split(" ")[0]).trim();
		command = command.toLowerCase();
		log.info("leo '" + lastLine + "'");
		log.info("command = '" + command + "'");

		switch (proxyState.getState()) {
		case AUTHENTICATION: // Capturo las respuesta de los comandos no
		case TRANSACTION: // contemplados en la maquina de estados.
			break;
		case EXPECT_USER_OK:
			switch (command) {
			case OK_RESP:
				proxyState.setState(AUTHENTICATION);
				break;
			case ERR_RESP:
				proxyState.setState(AUTHENTICATION);
				break;
			}
			break;
		case EXPECT_PASS_OK:
			switch (command) {
			case OK_RESP:
				proxyState.setState(TRANSACTION);
				break;
			case ERR_RESP:
				proxyState.setState(AUTHENTICATION);
				break;
			}
			break;
		case EXPECT_RETR_DATA:
			switch (command) {
			case OK_RESP:
				// proxyState.startEmailConversion(); //FIXME: NO ME FUNCIONA!
				proxyState.setState(TRANSACTION);
				break;
			case ERR_RESP:
				proxyState.setState(TRANSACTION);
				break;
			case ".": // termino el mail, que el resto pase
				proxyState.stopEmailConversion();
				// TODO: ver si el cambio de estado este esta bien
				proxyState.setState(TRANSACTION);
				break;
			default: // texto del mail, que pase
			}
			break;
		default:
			throw new UnsupportedOperationException(
					"Should never happen - all client states are contemplated here");
		}
	}

	private void doOriginWrite(SelectionKey key) throws IOException {
		super.doWrite(key);
	}
}
