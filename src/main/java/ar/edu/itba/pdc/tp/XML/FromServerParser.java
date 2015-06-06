package ar.edu.itba.pdc.tp.XML;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public class FromServerParser extends GenericParser {
	private enum OriginState {
		CONNECTION_STABLISHED, STREAM_START_EXPECTED, FEATURES_END_EXPECTED, CONNECTED
	};

	private OriginState state;
	private static String PROPERTIES_FILENAME = "./properties/serverParser.properties";
	private static String INITIAL_TAG;
	private static String START_AUTH_TAG;
	private static String END_AUTH_TAG;
	private static final String FEATURES = "features";

	private List<String> sendToSeverQueue;

	public FromServerParser(ByteBuffer buf) throws XMLStreamException,
			FileNotFoundException {
		super(buf);
		state = OriginState.CONNECTION_STABLISHED;
		if (INITIAL_TAG == null)
			loadPropertiesFile(PROPERTIES_FILENAME);
	}

	private void loadPropertiesFile(String fileName)
			throws FileNotFoundException {
		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);
		INITIAL_TAG = properties.getProperty("INITIAL_TAG");
		START_AUTH_TAG = properties.getProperty("START_AUTH_TAG");
		END_AUTH_TAG = properties.getProperty("END_AUTH_TAG");
	}

	@Override
	protected void processStreamElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == OriginState.STREAM_START_EXPECTED) {
			state = OriginState.FEATURES_END_EXPECTED;

		} else if (state == OriginState.CONNECTED) {
			// redirigir al cliente

			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
		}

	}

	@Override
	protected void processStreamElementEnd(XMPPproxyState proxyState,
			Selector selector) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processAuthElementStart(XMPPproxyState proxyState,
			Selector selector) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processAuthElementEnd(XMPPproxyState proxyState,
			Selector selector, XMPproxy protocol, AdminModule adminModule,
			TCPReactor reactor) throws ClosedChannelException,
			XMLStreamException {

		passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
	}

	@Override
	protected void processMessageElementStart(XMPPproxyState proxyState,
			Selector selector) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processMessageElementEnd(XMPPproxyState proxyState,
			Selector selector) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processMessage_bodyStart(XMPPproxyState proxyState,
			Selector selector) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processCharacters(String str, XMPPproxyState proxyState,
			Selector selector) {
		// TODO Auto-generated method stub

	}

	public void initiateStream(XMPPproxyState proxyState, Selector selector)
			throws ClosedChannelException {
		XMPPlistener.writeToOrigin(INITIAL_TAG, proxyState, selector);
		state = OriginState.STREAM_START_EXPECTED;
	}

	@Override
	protected void processOtherStartElement(XMPPproxyState proxyState,
			Selector selector) throws XMLStreamException,
			ClosedChannelException {

		if (state == OriginState.FEATURES_END_EXPECTED) {
			// omitir
			// TODO revisar en caso de que no soporte autenticacion plana
		} else if (state == OriginState.CONNECTED) {
			// redirigir al cliente

			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
		}

	}

	protected void passDirectlyToClient(XMPPproxyState proxyState,
			Selector selector, AsyncXMLStreamReader reader)
			throws XMLStreamException, ClosedChannelException {

		String toClient = XMLconstructor.constructXML(asyncXMLStreamReader);
		XMPPlistener.writeToClient(toClient, proxyState, selector);
	}

	@Override
	protected void processOtherEndElement(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException {
		String elementName = asyncXMLStreamReader.getLocalName();

		switch (elementName) {
		case FEATURES: {

			if (state == OriginState.FEATURES_END_EXPECTED) {
				state = OriginState.CONNECTED;
				String autentication = START_AUTH_TAG
						+ proxySstate.getUserPlainAuth() + END_AUTH_TAG;
//				XMPPlistener
//						.writeToOrigin(autentication, proxySstate, selector);
				enqueueForOrigin(autentication);

			}
			break;

		}
		default: {
			break;
		}

		}
	}

	// encola datos a enviar al origin server
	private void enqueueForOrigin(String str){
		if(sendToSeverQueue==null)
			sendToSeverQueue=new ArrayList<String>();
		
		sendToSeverQueue.add(str);
		
	}
	public void sendQueueForOrigin(XMPPproxyState proxySstate,
			Selector selector)
	
}
