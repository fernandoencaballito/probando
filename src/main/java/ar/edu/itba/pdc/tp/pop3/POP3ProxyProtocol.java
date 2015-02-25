package ar.edu.itba.pdc.tp.pop3;

import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.AUTHENTICATION;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.EXPECT_PASS_OK;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.EXPECT_RETR_DATA;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.EXPECT_USER_OK;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.QUITTING;
import static ar.edu.itba.pdc.tp.pop3.POP3ProxyState.States.TRANSACTION;
import static ar.edu.itba.pdc.tp.util.NIOUtils.readBuffer;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.email.EmailConverter;
import ar.edu.itba.pdc.tp.tcp.TCPConnectionData;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.util.NIOUtils;

public class POP3ProxyProtocol implements TCPProtocol {
	private static final int MAX_LINE_LEN = 1000;

	private static final Logger log = Logger.getLogger(POP3ProxyProtocol.class);
	private final AdminModule adminModule;

	private static final String CAPA = "capa";
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final String RETR = "retr";
	private static final String QUIT = "quit";
	private static final String AUTH = "auth";

	private static final String OK_RESP = "+ok";
	private static final String ERR_RESP = "-err";

	private static final String GREETING_MSG = "+OK ready\r\n";
	private static final String CLOSING_CONNECTION_MSG = "+OK closing connection.\r\n";
	private static final String UNABLE_TO_CONNECT_MSG = "-ERR unable to connect to your origin server\r\n";
	private static final String QUITTING_MSG = "QUIT\r\n";
	private static final String CAPA_TRANSACTION_MSG = "+OK\r\nCAPA\r\nUSER\r\nLIST\r\nRETR\r\nDELE\r\nRSET\r\nSTAT\r\nNOOP\r\n.\r\n";
	private static final String CAPA_AUTHENTICATION_MSG = "+OK\r\nCAPA\r\nUSER\r\n.\r\n";

	private static final String ERR_MSG = "-ERR\r\n";

	public POP3ProxyProtocol(AdminModule adminModule) {
		this.adminModule = adminModule;
	}

	@Override
	public TCPConnectionData handleAccept(final SelectionKey key)
			throws IOException {
		SocketChannel clientChannel = ((ServerSocketChannel) key.channel())
				.accept();
		// Must be nonblocking to register
		clientChannel.configureBlocking(false);
		final POP3ProxyState state = createProxyState(clientChannel, null);
		state.getToClientBuffer().clear();
		state.getToClientBuffer().put(GREETING_MSG.getBytes());
		state.updateSubscription(key.selector());
		adminModule.addAccess();
		return new TCPConnectionData(clientChannel, null);
	}

	@Override
	public void handleConnect(final SelectionKey key) throws IOException {
		POP3ProxyState state = (POP3ProxyState) key.attachment();
		SocketChannel originChannel = (SocketChannel) key.channel();

		try {
			if (originChannel.finishConnect()) {
				// nos interesa cualquier cosa que venga de cualquiera de las
				// dos puntas
				state.updateSubscription(key.selector());
			} else {
				state.closeChannels();
			}
		} catch (IOException e) {
			handleOriginConnectionFailure(state,key);
		}
	}

	private void handleOriginConnectionFailure(POP3ProxyState state, SelectionKey key) throws ClosedChannelException{
		log.info("Failed to connect to origin server: " );
		// state.closeChannels();
		// throw e;
		ByteBuffer clienBuffer = state.getToClientBuffer();
		writeMsg(clienBuffer, UNABLE_TO_CONNECT_MSG);
		state.setState(AUTHENTICATION);
		state.updateSubscription(key.selector());
	}
	
	@Override
	public final void handleRead(final SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel)
			throws IOException {
		log.info("read (Start)");
		final POP3ProxyState proxyState = getProxyState(key);
		final SocketChannel channel = (SocketChannel) key.channel();
		log.info("reading channel " + channel);
		final ByteBuffer buffer = proxyState.readBufferFor(channel);
		//log.info("en el buffer " + buffer);
		long bytesRead = channel.read(buffer);
		
		if (bytesRead == -1) { // Did the other end close?

			//se perdio la conexion al origin
			if(channel==proxyState.getOriginChannel() && proxyState.getClientChannel()!=null
					&& proxyState.getClientChannel().isOpen()){
				handleOriginConnectionFailure(proxyState, key);
				proxyState.getOriginChannel().close();
				proxyState.setState(AUTHENTICATION);
				
			}else{//se perdio la conexion al cliente
				proxyState.closeChannels(); 
			}
			
			
			
			
		} else if (bytesRead > 0) {
			log.info("bytes " + bytesRead + " read");
			ByteBuffer copy = (ByteBuffer) buffer.duplicate().flip();
			// viene de ser escrito el buffer, asi que para leerlo
			// tengo que hacerle flip
			log.info("lei '" + NIOUtils.readBuffer(copy) + "'");
			if (doNotEmptyRead(key, handlersByChannel)) {
				// connection established
				proxyState.updateSubscription(key.selector());
			}
			
		}
		log.info("read (End)");
	}

	private boolean doNotEmptyRead(SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel) {
		SocketChannel channel = (SocketChannel) key.channel();
		boolean connectionEstablished = true;
		POP3ProxyState proxyState = getProxyState(key);

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
				proxyState.putChar(bufferCopy.get()); // XXX
			}

//			log.info("lineBuffer: '"
//					+ readBuffer((ByteBuffer) proxyState.getLastLine()
//							.duplicate().flip()) + "'");
		}
		// ojo, aca NO va else, va como esta
		if (!proxyState.isLineDone()) { // TODO: check!
			return connectionEstablished;
		}

		String lastLine = readBuffer((ByteBuffer) lastLineBuffer.duplicate()
				.flip());
		log.info("state - before: " + proxyState.getState());
		if (proxyState.getClientChannel() == channel) {
			connectionEstablished = doClientNotEmptyRead(proxyState, lastLine,
					key, handlersByChannel);
		} else if (proxyState.getOriginChannel() == channel) {
			doOriginNotEmptyRead(proxyState, lastLine);
		} else {
			throw new IllegalArgumentException("Unknown socket");
		}
		proxyState.flushLastLine();
		log.info("state - after: " + proxyState.getState());
		return connectionEstablished;
	}

	private boolean doClientNotEmptyRead(POP3ProxyState proxyState,
			String lastLine, SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel) {
		String[] params = lastLine.split(" ");
		String command = params[0].trim();
		command = command.toLowerCase();
		//log.info("leo '" + lastLine + "'");
		log.info("command = '" + command + "'");

		if (QUIT.equals(command)) {
			// primero le responde al servidor
			writeMsg(proxyState.getToClientBuffer(), CLOSING_CONNECTION_MSG);

			// luego le pasa el commando quit al origin
			writeMsg(proxyState.getToOriginBuffer(), QUITTING_MSG);
			proxyState.setState(QUITTING);
			return true;
		}

		switch (proxyState.getState()) {
		case AUTHENTICATION:
			switch (command) {
			case CAPA:
				sendMessageToClient(proxyState, CAPA_AUTHENTICATION_MSG);
				break;
			case USER:
				// se conoce el nombre usuario por primera vez
				// XXX esta MAL que mandemos el user vacio a ver si esta, NUNCA
				// existe
				if (params.length == 1) {
					// solo mando "user", sin user
					sendMessageToClient(proxyState, ERR_MSG);
					return false;
				}
				InetSocketAddress originAddress = adminModule
						.getOriginAddressForUser(params[1]);

				try {
					final SocketChannel originChannel = SocketChannel.open();
					originChannel.configureBlocking(false);
					originChannel.connect(originAddress);

					proxyState.setOriginChannel(originChannel);
					originChannel.register(key.selector(),
							SelectionKey.OP_CONNECT, proxyState);

					handlersByChannel.put(originChannel, this);

					// proxyState.updateSubscription(key.selector());
				} catch (IOException e) {
					sendMessageToClient(proxyState, ERR_RESP);
				}
				proxyState.setState(EXPECT_USER_OK);
				return false;
			case PASS:
				SocketChannel originchannel = proxyState.getOriginChannel();
				if (originchannel != null && originchannel.isOpen()) {
					// si ya esta conectado, no contestar error
					proxyState.setState(EXPECT_PASS_OK);
				} else {
					// No esta conectado al origin, commando invalido
					sendMessageToClient(proxyState, ERR_MSG);
				}
				break;
			case AUTH:
				// No soportamos el metodo AUTH
				sendMessageToClient(proxyState, ERR_MSG);
				break;
			default: // Dejo pasar el resto de los comandos.
				break;
			}
			break;
		case TRANSACTION:
			switch (command) {
			case CAPA:
				sendMessageToClient(proxyState, CAPA_TRANSACTION_MSG);
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
		return true;
	}

	private void doOriginNotEmptyRead(POP3ProxyState proxyState, String lastLine) {
		String command = (lastLine.split(" ")[0]).trim().toLowerCase();

		log.info("leo '" + escapeJava(lastLine) + "'");
		log.info("command = '" + command + "'");

		switch (proxyState.getState()) {
		// Capturo las respuesta de los comandos no
		// contemplados en la maquina de estados.
		case AUTHENTICATION:
		case TRANSACTION:
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
			case ERR_RESP:
				proxyState.setState(TRANSACTION);
				break;
			case ".": // termino el mail, que el resto pase
				// TODO: chequear que no sea un '.' tramposo. Duda: Existen?
				proxyState.setState(TRANSACTION);
				break;
			// casos a convertir
			case OK_RESP:
			default:
				EmailConverter converter = new EmailConverter();
				String convertedLine = converter.convert(lastLine);
				ByteBuffer toClientBuffer = proxyState.getToClientBuffer();
				log.info("escribo '" + escapeJava(convertedLine) + "'");

				if (lastLine.length() == MAX_LINE_LEN) {
					// saco el \r\n de convertedLine, puesto que esta de mas (lo
					// puso el converter)
					convertedLine = convertedLine.substring(0, MAX_LINE_LEN);
				}

				toClientBuffer.flip();
				toClientBuffer.put(convertedLine.getBytes());
				proxyState.setState(TRANSACTION);
				break;
			}
			break;
		default:
			throw new UnsupportedOperationException(
					"Should never happen - all client states are contemplated here");
		}
	}

	@Override
	public void handleWrite(final SelectionKey key) throws IOException {
		final POP3ProxyState proxyState = getProxyState(key);

		doWritePOP3(key);
		switch (proxyState.getState()) {
		case QUITTING:
			key.channel().close();
			return;
		default:
			proxyState.updateSubscription(key.selector());
		}
	}

	// No borrar, este es el write a nivel POP3
	private void doWritePOP3(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		POP3ProxyState proxyState = getProxyState(key);

		if (proxyState.getClientChannel() != channel
				&& proxyState.getOriginChannel() != channel) {
			throw new IllegalArgumentException("Unknown socket");
		}
		log.info("state - before: " + proxyState.getState());
		final ByteBuffer buffer = proxyState.writeBufferFor(channel);

		buffer.flip(); // Prepare buffer for writing
		int amount=channel.write(buffer);
		if(channel==proxyState.getOriginChannel()){
			adminModule.addBytesTransfered(amount);
		}
		
		
		buffer.compact(); // Make room for more data to be read in
		log.info("state - after: " + proxyState.getState());
	}

	private void sendMessageToClient(POP3ProxyState proxyState, String msg) {// XXX
		ByteBuffer clientBuffer = (ByteBuffer) proxyState.getToClientBuffer()
				.clear();
		clientBuffer.put(msg.getBytes());
		// Limpio este buffer para no enviarle el comando del cliente al
		// origin.
		proxyState.getToOriginBuffer().clear();
	}

	private void writeMsg(ByteBuffer toClientBuffer, String msg) { // XXX
		toClientBuffer.clear();
		toClientBuffer.put(msg.getBytes());
	}

	private POP3ProxyState createProxyState(SocketChannel clientChannel,
			SocketChannel originChannel) {
		return new POP3ProxyState(clientChannel, originChannel);
	}

	private POP3ProxyState getProxyState(SelectionKey key) {
		return (POP3ProxyState) key.attachment();
	}
}
