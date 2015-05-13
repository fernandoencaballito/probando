package ar.edu.itba.pdc.tp.XML;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.fasterxml.aalto.AsyncByteBufferFeeder;
import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

public class GenericParser {
	private static final String STREAM = "stream";
	private static final String MESSAGE = "message";

	protected ByteBuffer buffer;
	protected Element element;

	// aalto
	private AsyncXMLStreamReader<AsyncByteBufferFeeder> asyncXMLStreamReader;
	private AsyncByteBufferFeeder feeder;

	private int type;

	public GenericParser(ByteBuffer buf) throws XMLStreamException {

		this.buffer = buf;
		AsyncXMLInputFactory xmlInputFactory = new InputFactoryImpl();

		asyncXMLStreamReader = xmlInputFactory.createAsyncFor(buf);
		feeder = asyncXMLStreamReader.getInputFeeder();

		type = 0;
	}

	public void feed() throws XMLStreamException {
		feeder.feedInput(buffer);
	}

	public void parse() {

		try {
			while (asyncXMLStreamReader.hasNext()) {
				type = asyncXMLStreamReader.next();

				System.out.println("OCURRIO TIPO: " + type);
				switch (type) {

				case XMLEvent.START_DOCUMENT:
					System.out.println("start document");
					break;
				case XMLEvent.START_ELEMENT:
					System.out.println("start element: "
							+ asyncXMLStreamReader.getName());
					break;
				case XMLEvent.CHARACTERS:
					System.out.println("characters: "
							+ asyncXMLStreamReader.getText());
					break;
				case XMLEvent.END_ELEMENT:
					System.out.println("end element: "
							+ asyncXMLStreamReader.getName());
					break;
				case XMLEvent.END_DOCUMENT:
					System.out.println("end document");
					break;
				default:
					break;

				// case XMLEvent.START_ELEMENT: {
				// QName qname = asyncXMLStreamReader.getName();
				// if (qname.getLocalPart() == STREAM) {
				//
				// // processStreamElement(startElement);
				// System.out.println(qname);
				//
				// }
				//
				// break;
				// }

				}
			}

		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void processStreamElement(StartElement startElement)
			throws XMLStreamException {

	}

}
