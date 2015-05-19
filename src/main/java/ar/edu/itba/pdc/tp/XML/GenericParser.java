package ar.edu.itba.pdc.tp.XML;

import java.nio.ByteBuffer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;

import com.fasterxml.aalto.AsyncByteBufferFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

public abstract class GenericParser {
	private static final String STREAM = "stream";
	private static final String MESSAGE = "message";
	private static final String AUTH = "auth";
	protected ByteBuffer buffer;
	protected Element element;
	
	private boolean uncompletedRead=false;
	
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

	public void parse(XMPPlistener listener) {
		uncompletedRead=true;
		try {
			while (!feeder.needMoreInput()) {
				uncompletedRead=false;
				type = asyncXMLStreamReader.next();

				System.out.println("OCURRIO TIPO: " + type);
				switch (type) {

				case XMLEvent.START_DOCUMENT:
				case 257: {
					// IGNORAR
					break;
				}

				case XMLEvent.START_ELEMENT: {
					System.out.println("start element: "
							+ asyncXMLStreamReader.getName());
					processStartElement(listener);
					break;
				}
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
					processEndElement(listener);
					break;
				case XMLEvent.END_DOCUMENT:
					System.out.println("end document");
					processEndDocument(listener);
					break;
				default:
					break;

				}
			}

		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!uncompletedRead)
			buffer.clear();

	}

	private void processEndDocument(XMPPlistener listener) {
		// TODO Auto-generated method stub

	}

	private void processEndElement(XMPPlistener listener) {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElementEnd(listener);
			break;
		}
		case MESSAGE: {
			processMessageElementEnd(listener);
		}
		case AUTH:{
			processAuthElementEnd(listener);
		}

		default:
			break;
		}
	}

	private void processStartElement(XMPPlistener listener) {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElement(listener);
			break;
		}
		case MESSAGE: {
			processMessageElementStart(listener);
		}
		case AUTH:{
			processAuthElementStart(listener);
		}

		default:
			break;
		}

	}

	// solo parar elemento STREAM:STREAM
	protected abstract void processStreamElement(XMPPlistener listener);

	// solo parar elemento /STREAM:STREAM
	protected abstract void processStreamElementEnd(XMPPlistener listener);

	protected abstract void processAuthElementStart(XMPPlistener listener);

	protected abstract void processAuthElementEnd(XMPPlistener listener);

	protected abstract void processMessageElementStart(XMPPlistener listener);

	protected abstract void processMessageElementEnd(XMPPlistener listener);

}