package ar.edu.itba.pdc.tp.XML;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;

import com.fasterxml.aalto.AsyncByteBufferFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

public abstract class GenericParser {
	private static final String BODY = "body";
	private static final String STREAM = "stream";
	private static final String MESSAGE = "message";
	private static final String AUTH = "auth";
	protected ByteBuffer buffer;

	private boolean uncompletedRead = false;

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
		if (feeder.needMoreInput())
			feeder.feedInput(buffer);

	}

	public void parse(XMPPproxyState state, Selector selector,
			XMPproxy protocol, AdminModule adminModule,TCPReactor reactor) {
		uncompletedRead = true;

		try {
			while (!feeder.needMoreInput()) {
				uncompletedRead = false;
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
					processStartElement(state, selector);
					break;
				}
				case XMLEvent.CHARACTERS: {
					String str = asyncXMLStreamReader.getText().trim();
					if (str.length() == 0)
						break;// ignorar

					System.out.println("characters :" + str);
					processCharacters(str, state, selector);
					break;
				}
				case XMLEvent.END_ELEMENT:
					System.out.println("end element: "
							+ asyncXMLStreamReader.getName());
					processEndElement(state, selector, protocol, adminModule,reactor);
					break;
				case XMLEvent.END_DOCUMENT:
					System.out.println("end document");
					processEndDocument(state, selector);
					break;
				default:
					break;

				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!uncompletedRead && buffer.position() == 0)
			buffer.clear();

	}

	private void processEndDocument(XMPPproxyState state, Selector selector) {
		// TODO Auto-generated method stub

	}

	private void processEndElement(XMPPproxyState state, Selector selector,
			XMPproxy protocol, AdminModule adminModule, TCPReactor reactor) throws ClosedChannelException {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElementEnd(state, selector);
			break;
		}
		case MESSAGE: {
			processMessageElementEnd(state, selector);
		}
		case AUTH: {
			processAuthElementEnd(state, selector, protocol, adminModule,
					reactor);
		}

		default:{
			processOtherEndElement(state, selector,elementName);
			break;
		}
		}
	}

	protected abstract void processOtherEndElement(XMPPproxyState state, Selector selector, String elementName) throws ClosedChannelException ;

	private void processStartElement(XMPPproxyState state, Selector selector)
			throws ClosedChannelException {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElement(state, selector);
			break;
		}
		case MESSAGE: {
			processMessageElementStart(state, selector);
			break;
		}
		case AUTH: {
			processAuthElementStart(state, selector);
			break;
		}
		case BODY: {
			processMessage_bodyStart(state, selector);
			break;
		}

		default:{
			processOtherStartElement(state,selector);
			break;
		}
		}

	}

	protected abstract void processOtherStartElement(XMPPproxyState state,
			Selector selector);

	// solo parar elemento STREAM:STREAM
	protected abstract void processStreamElement(XMPPproxyState state,
			Selector selector) throws ClosedChannelException;

	// solo parar elemento /STREAM:STREAM
	protected abstract void processStreamElementEnd(XMPPproxyState state,
			Selector selector);

	protected abstract void processAuthElementStart(XMPPproxyState state,
			Selector selector);

	protected abstract void processAuthElementEnd(XMPPproxyState proxyState,
			Selector selector, XMPproxy protocol, AdminModule adminModule,
			TCPReactor reactor);

	protected abstract void processMessageElementStart(XMPPproxyState state,
			Selector selector);

	protected abstract void processMessageElementEnd(XMPPproxyState state,
			Selector selector);

	protected abstract void processMessage_bodyStart(XMPPproxyState state,
			Selector selector);

	protected abstract void processCharacters(String str,
			XMPPproxyState proxyState, Selector selector);

}