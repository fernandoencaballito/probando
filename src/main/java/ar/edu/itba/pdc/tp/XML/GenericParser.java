package ar.edu.itba.pdc.tp.XML;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;


import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

import com.fasterxml.aalto.AsyncByteBufferFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.WFCException;
import com.fasterxml.aalto.stax.InputFactoryImpl;

public abstract class GenericParser {
	private static final String BODY = "body";
	private static final String STREAM = "stream";
	private static final String MESSAGE = "message";
	private static final String AUTH = "auth";
	protected ByteBuffer buffer;

	private boolean uncompletedRead = false;
	private static AsyncXMLInputFactory xmlInputFactory = new InputFactoryImpl();
	// aalto
	protected AsyncXMLStreamReader<AsyncByteBufferFeeder> asyncXMLStreamReader;
	private AsyncByteBufferFeeder feeder;

	
	private String lastEmptyElement=null;//string que se guarda para solucionar el caso en que Aalto interpreta un elemento vacio como uno que empieza y otro que termina.
	
	public GenericParser(ByteBuffer buf) throws XMLStreamException {

		this.buffer = buf;

//		asyncXMLStreamReader = xmlInputFactory.createAsyncFor(buf);
//		feeder = asyncXMLStreamReader.getInputFeeder();
		initAalto();

	}
	
	protected void reset(ByteBuffer newBuffer) throws XMLStreamException, FileNotFoundException{
		this.buffer=newBuffer;
		initAalto();
		uncompletedRead=false;

	}
	private void initAalto() throws XMLStreamException{
		asyncXMLStreamReader = xmlInputFactory.createAsyncFor(this.buffer);
		feeder = asyncXMLStreamReader.getInputFeeder();

	}
	
	public void feed() throws XMLStreamException {
		if (feeder.needMoreInput())
			feeder.feedInput(buffer);

	}

	public void parse(XMPPproxyState proxyState, Selector selector,
			XMPproxy protocol, AdminModule adminModule, TCPReactor reactor) throws ClosedChannelException {
		uncompletedRead = true;
		int type;
		try {
			while (!feeder.needMoreInput() && asyncXMLStreamReader.hasNext()) {
				uncompletedRead = false;
				System.out.println(this.toString());
				type = asyncXMLStreamReader.next();
				
				System.out.println("OCURRIO TIPO: " + type);
				switch (type) {

				case XMLEvent.START_DOCUMENT:{
					processStartDocuement(proxyState,selector);
				}
				case 257: {
					// IGNORAR
					break;
				}

				case XMLEvent.START_ELEMENT: {
					System.out.println("start element: "
							+ asyncXMLStreamReader.getName());
					processStartElement(proxyState, selector,adminModule);
					
					if(asyncXMLStreamReader.isEmptyElement())
						lastEmptyElement=asyncXMLStreamReader.getLocalName();
					break;
				}
				case XMLEvent.CHARACTERS: {
					String str = null;

					if (asyncXMLStreamReader.hasText())
						str = asyncXMLStreamReader.getText();

					if (str == null || str.length() == 0)
						break;// ignorar

					System.out.println("characters :" + str);
					processCharacters(str, proxyState, selector);
					break;
				}
				case XMLEvent.END_ELEMENT:
					System.out.println("end element: "
							+ asyncXMLStreamReader.getName());
					if(lastEmptyElement!=null && lastEmptyElement.equals(asyncXMLStreamReader.getLocalName())){//para solucionar problema de aalto con elementos vacios 
						lastEmptyElement=null;
						break;
					}
					processEndElement(proxyState, selector, protocol, adminModule,
							reactor);
					lastEmptyElement=null;
					break;
				case XMLEvent.END_DOCUMENT:
					System.out.println("end document");
					processEndDocument(proxyState, selector);
					break;
				default:
					break;

				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!uncompletedRead){
			buffer.clear();
					}
		this.finishPendingSends(proxyState,selector);

	}

	protected abstract void processStartDocuement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException ;
		


	protected abstract void finishPendingSends(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException;

	private void processEndDocument(XMPPproxyState state, Selector selector) {
		// TODO Auto-generated method stub

	}

	private void processEndElement(XMPPproxyState state, Selector selector,
			XMPproxy protocol, AdminModule adminModule, TCPReactor reactor)
			throws ClosedChannelException, XMLStreamException, FileNotFoundException {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElementEnd(state, selector, null);
			break;
		}
		case MESSAGE: {
			processMessageElementEnd(state, selector);
			break;
		}
		case AUTH: {
			processAuthElementEnd(state, selector, protocol, adminModule,
					reactor);
			break;
		}

		default: {
			processOtherEndElement(state, selector);
			break;
		}
		}
	}

	protected abstract void processOtherEndElement(XMPPproxyState state,
			Selector selector) throws ClosedChannelException, XMLStreamException, FileNotFoundException;

	private void processStartElement(XMPPproxyState proxyState,
			Selector selector, AdminModule adminModule) throws ClosedChannelException,
			XMLStreamException {
		QName qname = asyncXMLStreamReader.getName();
		String elementName = qname.getLocalPart();
		switch (elementName) {
		case STREAM: {
			processStreamElement(proxyState, selector);
			break;
		}
		case MESSAGE: {
			processMessageElementStart(proxyState, selector,adminModule);
			break;
		}
		case AUTH: {
			processAuthElementStart(proxyState, selector);
			break;
		}
		case BODY: {
			processMessage_bodyStart(proxyState, selector);
			break;
		}

		default: {
			processOtherStartElement(proxyState, selector);
			break;
		}
		}

	}

	protected abstract void processOtherStartElement(XMPPproxyState state,
			Selector selector) throws XMLStreamException,
			ClosedChannelException;

	// solo parar elemento STREAM:STREAM
	protected abstract void processStreamElement(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException;

	// solo parar elemento /STREAM:STREAM
	protected abstract void processStreamElementEnd(XMPPproxyState state,
			Selector selector, TCPReactor reactor) throws ClosedChannelException,
			XMLStreamException, FileNotFoundException;

	protected abstract void processAuthElementStart(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException;

	protected abstract void processAuthElementEnd(XMPPproxyState proxyState,
			Selector selector, XMPproxy protocol, AdminModule adminModule,
			TCPReactor reactor) throws ClosedChannelException,
			XMLStreamException;

	protected abstract void processMessageElementStart(XMPPproxyState state,
			Selector selector, AdminModule adminModule) throws ClosedChannelException,
			XMLStreamException;

	protected abstract void processMessageElementEnd(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException;

	protected abstract void processMessage_bodyStart(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException;

	protected abstract void processCharacters(String str,
			XMPPproxyState proxyState, Selector selector)
			throws ClosedChannelException, XMLStreamException;
	

}