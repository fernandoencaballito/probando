package ar.edu.itba.pdc.tp.pop3;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

class POP3Line {
	private static final int ORIG_CAPACITY = 512;
	private static final String CHARSET = "UTF-8";

	private static final byte CR_BYTE = (byte) '\r';
	private static final byte LN_BYTE = (byte) '\n';

	private List<Byte> bytes = new ArrayList<>(ORIG_CAPACITY);
	private LineReadingStates state = null;
	private int index = 0;

	POP3Line() {
	}

	void read(ByteBuffer readBuffer) {
		while (readBuffer.hasRemaining() && !this.isRead()) {
			byte last = readBuffer.get();

			switch (last) {
			case CR_BYTE:
				state = LineReadingStates.CR;
				break;
			case LN_BYTE:
				if (LineReadingStates.CR.equals(state)) {
					state = LineReadingStates.DONE;
				}
				break;
			default: // any other char
				state = LineReadingStates.OTHER;
			}

			bytes.add(index++, last);
		}
	}

	int length() {
		return index;
	}

	boolean isRead() {
		return LineReadingStates.DONE.equals(state);
	}

	byte[] getBytes() {
		List<Byte> bytesRead = bytes.subList(0, index);
		Byte[] bytesReadArr = bytesRead.toArray(new Byte[bytesRead.size()]);
		return ArrayUtils.toPrimitive(bytesReadArr);
	}

	String asText() throws UnsupportedEncodingException {
		return new String(getBytes(), CHARSET);
	}

	String[] getWords() throws UnsupportedEncodingException {
		return asText().split("\\s+");
	}

	void clear() {
		state = null;
		index = 0;
	}

	@Override
	public String toString() {
		try {
			return new ToStringBuilder(this).append("state", state)
					.append("index", index)
					.append("bytes", StringEscapeUtils.escapeJava(asText()))
					.toString();
		} catch (UnsupportedEncodingException e) {
			return "unsupported encoding";
		}
	}

	private enum LineReadingStates {
		OTHER, CR, DONE
	}
}
