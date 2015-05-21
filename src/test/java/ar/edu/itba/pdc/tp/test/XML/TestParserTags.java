package ar.edu.itba.pdc.tp.test.XML;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import ar.edu.itba.pdc.tp.XML.ByteBufferInputStream;
import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;

public class TestParserTags {
	private static final int BUFF_SIZE = 10 * 1024;
	private ByteBuffer buffer;
	private MockParser parser;
	private XMPPlistener mockListener;
	@Before
	public void init() throws XMLStreamException {
		buffer = ByteBuffer.allocate(BUFF_SIZE);
		buffer.flip();
		parser = new MockParser(buffer);
	}

	@Test
	public void testStreamElement() throws IOException, XMLStreamException {
		
		genericTagTestInit("./src/test/resources/startingStreamFromClient.in");
		assertTrue(parser.isStreamElementStart());
		assertFalse(parser.isStreamElementEnd());
		buffer.clear();

		genericTagTestInit("./src/test/resources/endingStreamExample");
		assertTrue(parser.isStreamElementEnd());
	}

	@Test
	public void testAuthElement() throws IOException, XMLStreamException {
		genericTagTestInit("./src/test/resources/authElementStart");
		assertFalse(parser.isAuthElementEnd());
		assertTrue(parser.isAuthElementStart());

		buffer.clear();

		genericTagTestInit("./src/test/resources/authElementEnd");
		assertTrue(parser.isAuthElementEnd());
	}

	@Test
	public void testMessageElement() throws IOException, XMLStreamException {
		genericTagTestInit("./src/test/resources/messageElementStart");
		assertTrue(parser.isMessageElementStart());
		assertFalse(parser.isMessageElementEnd());

		buffer.clear();

		genericTagTestInit("./src/test/resources/messageElementEnd");
		assertTrue(parser.isMessageElementEnd());
	}

	@Test
	public void testPartialRead() throws IOException, XMLStreamException {
		buffer.clear();
		String inFileName1 = "./src/test/resources/startingStreamFromClient.part1.in";
		readFileIntoBuffer(inFileName1, buffer);
		assertTrue(buffer.position() > 0);

		// SE TIENE QUE PASAR EL BUFFER A MODO LECTURA
		buffer.flip();
//		MockParser parser2 = new MockParser(buffer);
		parser.feed();
		parser.parse(mockListener);

		assertFalse(parser.isStreamElementStart());

		String inFileName2 = "./src/test/resources/startingStreamFromClient.part2.in";
		readFileIntoBuffer(inFileName2, buffer);

		// SE TIENE QUE PASAR EL BUFFER A MODO Lectura
		buffer.flip();
		parser.feed();
		parser.parse(mockListener);
		assertTrue(parser.isStreamElementStart());
	}

	@Test
	public void testPartialRead2() throws IOException, XMLStreamException {
		buffer.clear();
		int divideIndex = 10;
		String inFileName1 = "./src/test/resources/startingStreamFromClient.in";
		FileInputStream inputStream = new FileInputStream(inFileName1);
		byte[] byteArray = new byte[BUFF_SIZE];
		int count = inputStream.read(byteArray);
		byte[] byteArrayPart1 = Arrays.copyOfRange(byteArray, 0, divideIndex);
		byte[] byteArrayPart2 = Arrays.copyOfRange(byteArray, divideIndex,
				count);

		buffer.put(byteArrayPart1);

		assertTrue(buffer.position() > 0);

		// SE TIENE QUE PASAR EL BUFFER A MODO LECTURA
		buffer.flip();
//		MockParser parser = new MockParser(buffer);

		parser.parse(mockListener);

		assertFalse(parser.isStreamElementStart());
		
		
		buffer.compact();
		buffer.put(byteArrayPart2);

		// SE TIENE QUE PASAR EL BUFFER A MODO Lectura
		buffer.flip();
		parser.feed();
		parser.parse(mockListener);
		assertTrue(parser.isStreamElementStart());
	}
	
	@Test
	public void testPartialRead3() throws IOException, XMLStreamException {
		buffer.clear();
		String inFileName1 = "./src/test/resources/messageElementStart.part1";
		readFileIntoBuffer(inFileName1, buffer);
		assertTrue(buffer.position() > 0);

		// SE TIENE QUE PASAR EL BUFFER A MODO LECTURA
		buffer.flip();
//		MockParser parser2 = new MockParser(buffer);
		parser.feed();
		parser.parse(mockListener);

		assertTrue(parser.isMessageElementStart());
		assertFalse(parser.isMessage_bodySTART());
		String inFileName2 = "./src/test/resources/messageElementStart.part2";
		readFileIntoBuffer(inFileName2, buffer);

		// SE TIENE QUE PASAR EL BUFFER A MODO Lectura
		buffer.flip();
		parser.feed();
		parser.parse(mockListener);
		assertTrue(parser.isMessageElementStart());
		assertTrue(parser.isMessage_bodySTART());
	}

	private void genericTagTestInit(String fileName) throws IOException,
			XMLStreamException {
		buffer.clear();
		readFileIntoBuffer(fileName, buffer);
		assertTrue(buffer.position() > 0);

		
		// SE TIENE QUE PASAR EL BUFFER A MODO LECTURA
		buffer.flip();
		
		
		parser.feed();
		parser.parse(mockListener);
	}

	static void readFileIntoBuffer(String path, ByteBuffer buffer)
			throws IOException {
		RandomAccessFile aFile = new RandomAccessFile(path, "r");
		FileChannel inChannel = aFile.getChannel();
		inChannel.read(buffer);
	}
}
