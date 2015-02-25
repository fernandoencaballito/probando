package ar.edu.itba.pdc.tp.test;

import static ar.edu.itba.pdc.tp.util.NIOUtils.readBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class TCPSocketUser {
	private BufferModes mode = BufferModes.ANY;

	public abstract ByteBuffer getBuffer();

	public abstract SocketChannel getChannel();

	public abstract boolean isConnected();

	public void send(String data) throws IOException {
		ByteBuffer buffer = getBuffer();
		switchBufferMode(BufferModes.WRITE);
		for (char c : data.toCharArray()) {
			buffer.putChar(c);
		}
		switchBufferMode(BufferModes.READ);
		getChannel().write(buffer);
		buffer.compact();
		mode = BufferModes.ANY;
	}

	public String receive() throws IOException {
		ByteBuffer buffer = getBuffer();
		switchBufferMode(BufferModes.WRITE);
		getChannel().read(buffer);
		switchBufferMode(BufferModes.READ);
		String received = readBuffer(buffer);
		buffer.clear();
		mode = BufferModes.ANY;
		return received;
	}

	private void switchBufferMode(BufferModes newMode) {
		if (!newMode.equals(mode)) {
			switch (mode) {
			case ANY:
				break;
			case WRITE:
			case READ:
				getBuffer().flip();
			}
		}
		mode = newMode;
	}

	private enum BufferModes {
		WRITE, READ, ANY
	}
}
