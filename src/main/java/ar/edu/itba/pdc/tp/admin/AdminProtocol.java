package ar.edu.itba.pdc.tp.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.tcp.TCPConnectionData;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;

public class AdminProtocol implements TCPProtocol {
	private int bufSize; // Size of I/O buffer

	private AdminModule reactorState;

	private final String ADMIN_PASSWORD = "password";
	private static final String CORRECT_LOGIN_MSG = "+OK ready\r\n";
	private static final String INCORRECT_LOGIN_MSG = "-ERR wrong password\r\n";
	private static final String INCORRECT_COMMAND = "-ERR invalid command\r\n";
	private static final String CORRECT_OPERATION = "+OK \r\n";
	private static final String TRANSFORMATION_ON_MSG = "+OK transformation of the subject is on \r\n";
	private static final String TRANSFORMATION_OFF_MSG = "+OK transformation of the subject is off \r\n";
	private static final String MULTIPLEXING_ON_MSG = "+OK accounts multiplexing is on\r\n";
	private static final String MULTIPLEXING_OFF_MSG = "+OK accounts multiplexing is off\r\n";

	private static final String transformationOffRegex = "TOFF\\r\\n";
	private static final String transformationOnRegex = "TON\\r\\n";
	private static final String multiplexingOnRegex = "MON\\r\\n";
	private static final String multiplexingOffRegex = "MOFF\\r\\n";
	private static final String passRegex = "PASS\\s.*\\r\\n";
	private static final String metricsAccessesRegex = "MET1\\r\\n";
	private static final String metricsTransferedRegex = "MET2\\r\\n";
	private static final String quitRegex = "QUIT\\r\\n";

	private static final Pattern patternPass = Pattern.compile(passRegex);
	private static final Pattern patternMetricsAccesses = Pattern
			.compile(metricsAccessesRegex);
	private static final Pattern patternMetricsTransfered = Pattern
			.compile(metricsTransferedRegex);
	private static final Pattern patternQuit = Pattern.compile(quitRegex);
	private static final Pattern patternTransformationOff = Pattern
			.compile(transformationOffRegex);
	private static final Pattern patternTransformationOn = Pattern
			.compile(transformationOnRegex);
	private static final Pattern patternMultiplexingOn = Pattern
			.compile(multiplexingOnRegex);
	private static final Pattern patternMultiplexingOff = Pattern
			.compile(multiplexingOffRegex);

	private static final Logger log = Logger.getLogger(AdminProtocol.class);

	public AdminProtocol(int bufSize, AdminModule reactorState) {
		this.bufSize = bufSize;
		this.reactorState = reactorState;
	}

	public TCPConnectionData handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false);
		clntChan.register(key.selector(), SelectionKey.OP_READ,
				new AdminProtocolState(bufSize));
		log.info("Connected: "
				+ ((ServerSocketChannel) key.channel()).getLocalAddress());
		return new TCPConnectionData(clntChan);
	}

	public void handleRead(SelectionKey key,
			Map<SocketChannel, TCPProtocol> handlersByChannel)
			throws IOException {
		// Client socket channel has pending data
		SocketChannel clntChan = (SocketChannel) key.channel();
		AdminProtocolState adminState = (AdminProtocolState) key.attachment();
		long bytesRead = adminState.readFromChannel(clntChan);
		if (bytesRead == -1) { // Did the other end close?
			clntChan.close();
		} else if (bytesRead > 0) {
			// Indicate via key that reading/writing are both of interest now.
			key.interestOps(SelectionKey.OP_READ);

			if (adminState.isLineDone())
				key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	// respuestas al cliente
	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier

		AdminProtocolState adminState = (AdminProtocolState) key.attachment();
		ByteBuffer buf = adminState.getClientBuffer();

		boolean connected = handleResponse(adminState);

		buf.flip();// prepare buffer for writing
		SocketChannel clntChan = (SocketChannel) key.channel();
		clntChan.write(buf);

		if (!buf.hasRemaining()) { // Buffer completely written?
			// Nothing left, so no longer interested in writes
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact(); // Make room for more data to be read in
		if (!connected)
			clntChan.close();
	}

	private boolean handleResponse(AdminProtocolState adminState)
			throws UnsupportedEncodingException {
		boolean connected = true;
		String ans = null;

		// Specify the appropriate encoding as the last argument
		ByteBuffer lastLine = adminState.getLastLine();
		String fromUser = prepareBuffer(lastLine);
		ByteBuffer buf = adminState.getBuffer();
		AdminStateEnum stateEnum = adminState.getState();

		if (stateEnum == AdminStateEnum.EXPECT_PASS) {

			if (patternPass.matcher(fromUser).matches()) {
				String enteredPassword = (fromUser.split(" ")[1]).trim();
				// login correcto
				if (this.ADMIN_PASSWORD.equals(enteredPassword)) {
					stateEnum = AdminStateEnum.TRANSACTION;
					ans = CORRECT_LOGIN_MSG;
					log.info("Correct login");
				} else {
					ans = INCORRECT_LOGIN_MSG;
					log.info("Incorrect login");
				}
			} else {
				// no se ingreso correctamente el comando PASS
				ans = INCORRECT_COMMAND;
				log.info("Incorrect command: " + fromUser);
			}
		} else if (stateEnum == AdminStateEnum.TRANSACTION) {
			log.info("Command: " + fromUser);
			List<String> userOriginUrl;
			// hay que cerrar la conexion
			if (patternQuit.matcher(fromUser).matches()) {
				connected = false;
				ans = CORRECT_OPERATION;
				log.info("Disconnected");
			} else if (patternMetricsAccesses.matcher(fromUser).matches()) {
				ans = buildMetrics1Msg();
			} else if (patternMetricsTransfered.matcher(fromUser).matches()) {
				ans = buildMetrics2Msg();
			} else if (patternTransformationOff.matcher(fromUser).matches()) {
				ans = TRANSFORMATION_OFF_MSG;
				reactorState.transformationOff();
			} else if (patternTransformationOn.matcher(fromUser).matches()) {
				ans = TRANSFORMATION_ON_MSG;
				reactorState.transformationOn();
			} else if (patternMultiplexingOn.matcher(fromUser).matches()) {
				ans = MULTIPLEXING_ON_MSG;
				reactorState.multiplexingOn();
			} else if (patternMultiplexingOff.matcher(fromUser).matches()) {
				ans = MULTIPLEXING_OFF_MSG;
				reactorState.multiplexingOff();
			} else if ((userOriginUrl = (SetUserValidator.validate(fromUser))) != null) {
				reactorState.setOriginForUser(userOriginUrl.get(0),
						userOriginUrl.get(1));
				ans = CORRECT_OPERATION;

			} else {

				ans = INCORRECT_COMMAND;
			}
		}

		log.info("Response: " + ans);
		buf.compact();
		buf.put(ans.getBytes());
		adminState.setState(stateEnum);
		adminState.clearLastLine();
		return connected;
	}

	// metricas, cantidad de accesos
	private String buildMetrics1Msg() {
		String ans = reactorState.getAccesses() + "\r\n";
		return ans;
	}

	// metricas, cantidad de bytes transferidos
	private String buildMetrics2Msg() {
		String ans = reactorState.getBytesTransfered() + "\r\n";
		return ans;
	}

	private String prepareBuffer(ByteBuffer buffer) {
		String read = "";

		buffer.flip();
		int size = buffer.limit() - buffer.position();
		byte[] array = new byte[size];
		buffer.get(array);
		try {
			if (buffer.limit() != 0) {
				read = new String(array, 0, size, "US-ASCII");
			}
		} catch (UnsupportedEncodingException e) {

		}
		return read;
	}

	@Override
	public void handleConnect(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub
	}
}