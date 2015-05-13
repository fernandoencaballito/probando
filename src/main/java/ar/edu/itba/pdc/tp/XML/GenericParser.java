package ar.edu.itba.pdc.tp.XML;

import java.nio.ByteBuffer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.fasterxml.aalto.AsyncByteBufferFeeder;
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
			while (!feeder.needMoreInput()) {
				type = asyncXMLStreamReader.next();

				System.out.println("OCURRIO TIPO: " + type);
				switch (type) {

				case 257: {
					// IGNORAR
					break;
				}
				case XMLEvent.START_DOCUMENT:
					System.out.println("start document");
					break;
				case XMLEvent.START_ELEMENT:
					System.out.println("start element: "
							+ asyncXMLStreamReader.getName());
					processStartElement();
					break;
				case XMLEvent.CHARACTERS: {
					String str = asyncXMLStreamReader.getText().trim();
					if (str.length() == 0)
						break;// ignorar

					System.out.println("characters :" + str);

					break;
				}
				case XMLEvent.END_ELEMENT:
					System.out.println("end element: "
							+ asyncXMLStreamReader.getName());
					processEndElement();
					break;
				case XMLEvent.END_DOCUMENT:
					System.out.println("end document");
					processEndDocument();
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

	private void processEndDocument() {
		// TODO Auto-generated method stub

	}

	private void processEndElement() {
		// TODO Auto-generated method stub

	}

	private void processStartElement() {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {

			break;
		}
		case MESSAGE:{
			
		}

		default:
			break;
		}

	}

	// solo parar elemento STREAM:STREAM
	protected void processStreamElement(StartElement startElement)
			throws XMLStreamException {

	}

}
