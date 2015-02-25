package old;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import ar.edu.itba.pdc.tp.email.EmailConverter;

public class OLDPOP3ProxyState extends OLDTCPProxyState {

	private static final int LINE_SIZE = 512;

	public States state = States.AUTHENTICATION;

	private ByteBuffer lastLine = null;
	private int readCount = 0;
	private boolean lineDone = false;
	private boolean carriageReturn = false; // Indica si ya pase un por un '\r'
	private boolean sendingCapa = false;

	private EmailConverter emailConverter;

	OLDPOP3ProxyState(SocketChannel clientChannel, SocketChannel originChannel) {
		super(clientChannel, originChannel);
	}

	public States getState() {
		return state;
	}

	public void setState(States state) {
		this.state = state;
	}

	public ByteBuffer getLastLine() {
		return lastLine;
	}

	public int getReadCount() {
		return readCount;
	}

	public boolean isLineDone() {
		return lineDone;
	}

	public ByteBuffer flushLastLine() {
		if (lastLine == null) {
			lastLine = ByteBuffer.allocate(LINE_SIZE);
		} else {
			lastLine.clear();
		}
		readCount = 0;
		lineDone = false;
		return lastLine;
	}

	public void putChar(byte b) {

		// lastLine.putChar(1, c);
		char c = (char) b;
		lastLine.put(b);
		readCount++;

		if (carriageReturn && c == '\n') {
			lineDone = true;
			return;
		} else {
			carriageReturn = false;
		}

		if (c == '\r')
			carriageReturn = true;

		lineDone = (readCount == LINE_SIZE);
	}

//	public void startEmailConversion() {
//		this.emailConverter = new EmailConverter(getToOriginBuffer(),
//				getToClientBuffer());
//	}

	public void stopEmailConversion() {
		this.emailConverter = null;
	}

	public boolean canConvertEmail() {
		return emailConverter != null;
	}

	public EmailConverter getEmailConverter() {
		return emailConverter;
	}

	public void setSendingCapa(boolean b) {
		this.sendingCapa = b;
	}

	public boolean getSendingCapa() {
		return this.sendingCapa;
	}

	enum States {
		EXPECT_USER_OK, EXPECT_PASS_OK, EXPECT_RETR_DATA, GREETING, TRANSACTION, AUTHENTICATION, QUITTING
	}
}
