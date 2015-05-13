package ar.edu.itba.pdc.tp.test.XML;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncByteBufferFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import static org.junit.Assert.*;

public class Borrarme {
	public static void main(String[] args) throws XMLStreamException,
			UnsupportedEncodingException {
		AsyncXMLInputFactory inputF = new InputFactoryImpl(); // sub-class of
																// XMLStreamReader2
		// two choices for input feeding: byte[] or ByteBuffer. Here we use
		// former:
		byte[] input_part1 = "<root>val".getBytes("UTF-8"); // would come from
															// File, over the
															// net etc
		// can construct with initial data, or without; here we initialize with
		// it

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.put(input_part1);
		buffer.flip();

		AsyncXMLStreamReader<AsyncByteBufferFeeder> parser = inputF
				.createAsyncFor(buffer);

		// now can access couple of events
		assertEquals(XMLStreamConstants.START_DOCUMENT, parser.next());
		assertEquals(XMLStreamConstants.START_ELEMENT, parser.next());
		assertEquals("root", parser.getLocalName());
		// since we have parts of CHARACTERS, we'll still get that first:
		assertEquals(XMLStreamConstants.CHARACTERS, parser.next());
		assertEquals("val", parser.getText());
		// but that's all data we had so:
		assertEquals(AsyncXMLStreamReader.EVENT_INCOMPLETE, parser.next());

		// at this point, must feed more data:
		// buffer.compact();
		// buffer.flip();
		buffer.clear();
		byte[] input_part2 = "ue</root>".getBytes("UTF-8");

		buffer.put(input_part2);
		buffer.flip();

		parser.getInputFeeder().feedInput(buffer);

		// and can parse that
		assertEquals(XMLStreamConstants.CHARACTERS, parser.next());
		assertEquals("ue", parser.getText());
		assertEquals(XMLStreamConstants.END_ELEMENT, parser.next());
		assertEquals("root", parser.getLocalName());
		assertEquals(AsyncXMLStreamReader.EVENT_INCOMPLETE, parser.next());

		// and if we now ran out of data need to indicate that too
		parser.getInputFeeder().endOfInput();
		// which lets us conclude parsing
		assertEquals(XMLStreamConstants.END_DOCUMENT, parser.next());
		parser.close();
	}
}
