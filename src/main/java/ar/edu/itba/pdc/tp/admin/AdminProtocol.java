package ar.edu.itba.pdc.tp.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

public class AdminProtocol implements TCPProtocol {
    private int bufSize; // Size of I/O buffer

    private AdminModule reactorState;

    private final String ADMIN_PASSWORD = "password";
    private static final String CORRECT_LOGIN_MSG = "+OK ready\r\n";
    private static final String CORRECT_SILENCE_MSG = "+OK silence user\r\n";
    private static final String INCORRECT_LOGIN_MSG = "-ERR wrong password\r\n";
    private static final String INCORRECT_COMMAND = "-ERR invalid command\r\n";
    private static final String INCORRECT_INT = "-ERR invalid command not an int\r\n";
    private static final String CORRECT_OPERATION = "+OK \r\n";
    private static final String CORRECT_ORIGIN_CHANGED = "+OK changed origin server for user \r\n";
    private static final String TRANSFORMATION_ON_MSG = "+OK transformation of the subject is on \r\n";
    private static final String TRANSFORMATION_OFF_MSG = "+OK transformation of the subject is off \r\n";
    private static final String MULTIPLEXING_ON_MSG = "+OK accounts multiplexing is on\r\n";
    private static final String MULTIPLEXING_OFF_MSG = "+OK accounts multiplexing is off\r\n";
    private static final String silenceUser="SILENCE\\s.*\\r\\n";
    private static final String changeUserOriginServer="SET\\s.*\\s.*\\s.*\\r\\n";
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
    private static final Pattern patternSilenceUser = Pattern
            .compile(silenceUser);
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
    private static final Pattern patternChangeUserOriginServer = Pattern
            .compile(changeUserOriginServer);

    private static final Logger LOGGER = Logger.getLogger(AdminProtocol.class);

    private final TCPReactor reactor;

    public AdminProtocol(TCPReactor reactor, int bufSize,
            AdminModule reactorState) {
        this.bufSize = bufSize;
        this.reactorState = reactorState;
        this.reactor = reactor;
    }

    @Override
    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel())
                .accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ,
                new AdminProtocolState(bufSize));
        LOGGER.info("Connected: "
                + ((ServerSocketChannel) key.channel()).getLocalAddress());
        reactor.subscribeChannel(clientChannel, this);
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {
        // Client socket channel has pending data
        SocketChannel clientChannel = (SocketChannel) key.channel();
        AdminProtocolState adminState = (AdminProtocolState) key.attachment();
        long bytesRead = adminState.readFromChannel(clientChannel);
        if (bytesRead == -1) { // Did the other end close?
            clientChannel.close();
            reactor.unsubscribeChannel(clientChannel);
        } else if (bytesRead > 0) {
            // Indicate via key that reading/writing are both of interest now.
            key.interestOps(SelectionKey.OP_READ);

            if (adminState.isLineDone())
                key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    // respuestas al cliente
    @Override
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
        SocketChannel clientChannel = (SocketChannel) key.channel();
        clientChannel.write(buf);

        if (!buf.hasRemaining()) { // Buffer completely written?
            // Nothing left, so no longer interested in writes
            key.interestOps(SelectionKey.OP_READ);
        }
        buf.compact(); // Make room for more data to be read in
        if (!connected) {
            clientChannel.close();
        }
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

        String[] split = fromUser.split("\r\n");
        if (stateEnum == AdminStateEnum.EXPECT_PASS) {

            if (patternPass.matcher(fromUser).matches()) {
                String enteredPassword = (fromUser.split(" ")[1]).trim();
                // login correcto
                if (this.ADMIN_PASSWORD.equals(enteredPassword)) {
                    stateEnum = AdminStateEnum.TRANSACTION;
                    ans = CORRECT_LOGIN_MSG;
                    LOGGER.info("Correct login");
                } else {
                    ans = INCORRECT_LOGIN_MSG;
                    LOGGER.info("Incorrect login");
                }
            } else {
                // no se ingreso correctamente el comando PASS
                ans = INCORRECT_COMMAND;
                LOGGER.info("Incorrect command: "/* + split[0]*/);
            }
        } else if (stateEnum == AdminStateEnum.TRANSACTION) {
            LOGGER.info("Command: " + split[0]);
            List<String> userOriginUrl;
            // hay que cerrar la conexion
            if (patternQuit.matcher(fromUser).matches()) {
                connected = false;
                ans = CORRECT_OPERATION;
                LOGGER.info("Disconnected");
            } else if (patternMetricsAccesses.matcher(fromUser).matches()) {
                ans = buildMetrics1Msg();
            }  else if (patternSilenceUser.matcher(fromUser).matches()) {
            	String user=(fromUser.split(" ")[1]).trim();
                ans = SilenceUsermsg(user);
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
            }//else if ((userOriginUrl = (SetUserValidator.validate(fromUser))) != null) {
              //  reactorState.setOriginForUser(userOriginUrl.get(0),userOriginUrl.get(1),null);
             //   ans = CORRECT_OPERATION;
            //}
        else if (patternChangeUserOriginServer.matcher(fromUser).matches()) {
                String user=(fromUser.split(" ")[1]).trim();
                String origin=(fromUser.split(" ")[2]).trim();
                String port=(fromUser.split(" ")[3]).trim();
                ans=changeOrigin(user,origin,port);
                
            }else {

                ans = INCORRECT_COMMAND;
            }
        }
        split = ans.split("\r\n");
        LOGGER.info("Response: " + split[0]);
        buf.compact();
        buf.put(ans.getBytes());
        adminState.setState(stateEnum);
        adminState.clearLastLine();
        return connected;
    }

    private String changeOrigin(String user, String origin, String port) {
    	System.out.println("puerto especificado "+port);
		Integer originPort;
		try{
			originPort=Integer.valueOf(port);
		}catch(Exception e){
			return INCORRECT_INT;
		}
		reactorState.setOriginForUser(user,origin, originPort);
		return CORRECT_ORIGIN_CHANGED;
	}

	private String SilenceUsermsg(String user) {
		reactorState.silence(user);
		return CORRECT_SILENCE_MSG;
		
	
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

    
    //OJO: SOLO TOMA ASCII, ahora deberia tomar utf8
    private String prepareBuffer(ByteBuffer buffer) {
        String read = "";

        buffer.flip();
        int size = buffer.limit() - buffer.position();
        byte[] array = new byte[size];
        buffer.get(array);
        if (buffer.limit() != 0) {
		    read = new String(array, 0, size, Charset.forName("UTF-8"));
		}
        return read;
    }

    @Override
    public void handleConnect(SelectionKey key) throws IOException {
    	System.out.println("handle connect");
    }
}