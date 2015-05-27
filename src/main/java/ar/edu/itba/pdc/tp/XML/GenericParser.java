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
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
public abstract class GenericParser {
	private static final String BODY = "body";
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
	
	
	private static final String  START_DOCUMENT="<?xml version='1.0' encoding='UTF-8'?>";
	

	public GenericParser(ByteBuffer buf) throws XMLStreamException {

		this.buffer = buf;
		AsyncXMLInputFactory xmlInputFactory = new InputFactoryImpl();

		asyncXMLStreamReader = xmlInputFactory.createAsyncFor(buf);
		feeder = asyncXMLStreamReader.getInputFeeder();

		type = 0;
	}

	public void feed() throws XMLStreamException {
		if(feeder.needMoreInput())
		feeder.feedInput(buffer);
		
	}

	public void parse(XMPPproxyState state) {
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
					processStartElement(state);
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
					processEndElement();
					break;
				case XMLEvent.END_DOCUMENT:
					System.out.println("end document");
					processEndDocument();
					break;
				default:
					break;

				}
			}

		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!uncompletedRead && buffer.position()==0)
			buffer.clear();

	}

	
	private void processEndDocument() {
		// TODO Auto-generated method stub

	}

	private void processEndElement() {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElementEnd();
			break;
		}
		case MESSAGE: {
			processMessageElementEnd();
		}
		case AUTH:{
			processAuthElementEnd();
		}

		default:
			break;
		}
	}

	private void processStartElement(XMPPproxyState state) {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElement(state);
			break;
		}
		case MESSAGE: {
			processMessageElementStart();
			break;
		}
		case AUTH:{
			processAuthElementStart();
			break;
		}
		case BODY:{
			processMessage_bodyStart();
			break;
		}

		default:
			break;
		}

	}

	// solo parar elemento STREAM:STREAM
	protected abstract void processStreamElement(XMPPproxyState state);

	// solo parar elemento /STREAM:STREAM
	protected abstract void processStreamElementEnd();

	protected abstract void processAuthElementStart();

	protected abstract void processAuthElementEnd();

	protected abstract void processMessageElementStart();

	protected abstract void processMessageElementEnd();
	protected abstract void processMessage_bodyStart();
}