package ar.edu.itba.pdc.tp.XML;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/*
 * Basado en el código del siguiente sitio :http://www.snippetit.com/2010/01/java-use-bytebuffer-as-inputstream/
 */
public class ByteBufferInputStream extends InputStream {

	private int bbisInitPos;
	private int bbisLimit;
	private ByteBuffer bbisBuffer;

	public ByteBufferInputStream(ByteBuffer buffer) {
		this(buffer, buffer.limit());
	}

	public ByteBufferInputStream(ByteBuffer buffer, int limit) {
		bbisBuffer = buffer;
		bbisLimit = limit;
		bbisInitPos = bbisBuffer.position();
	}

	@Override
	public int read() throws IOException {
		if ((bbisBuffer.position() - bbisInitPos) >= bbisBuffer.limit())
			return -1;
		byte currentByte = 0;

		currentByte = bbisBuffer.get();

		// bbisBuffer.compact();
		return currentByte;
	}
}
