package ar.edu.itba.pdc.tp.XML;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringEscapeUtils;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.L33tConverter;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public class FromServerParser extends GenericParser {
	private enum OriginState {
		STREAM_START_EXPECTED, FEATURES_END_EXPECTED, CONNECTED, IN_MESSAGE, IN_BODY, 
		IN_MUTED_MESSAGE, IN_USER_JID, EXPECTING_JID_END,
		CLOSING, CLOSED
	};

	private OriginState state;
	private static String PROPERTIES_FILENAME = "./properties/serverParser.properties";

	private static String START_AUTH_TAG;
	private static String END_AUTH_TAG;
	private static final String FEATURES = "features";
	private static final String SUCCESS = "success";
	private static final String BODY = "body";
	private static final String JID = "jid";
	private List<String> queueToSever;

	public FromServerParser(ByteBuffer buf) throws XMLStreamException,
			FileNotFoundException {
		super(buf);
		state = OriginState.STREAM_START_EXPECTED;
		if (START_AUTH_TAG == null)
			loadPropertiesFile(PROPERTIES_FILENAME);
	}

	private void loadPropertiesFile(String fileName)
			throws FileNotFoundException {
		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);
		START_AUTH_TAG = properties.getProperty("START_AUTH_TAG");
		END_AUTH_TAG = properties.getProperty("END_AUTH_TAG");
	}

	@Override
	protected void processStreamElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == OriginState.STREAM_START_EXPECTED) {
			state = OriginState.FEATURES_END_EXPECTED;

		} else if (state == OriginState.CONNECTED
				|| state == OriginState.IN_MESSAGE) {
			// redirigir al cliente

			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
		}

	}

	@Override
	protected void processStreamElementEnd(XMPPproxyState proxyState,
			Selector selector, TCPReactor reactor) throws ClosedChannelException,
			XMLStreamException, FileNotFoundException {
		if(state!=OriginState.CLOSED){
			state=OriginState.CLOSING;
			 passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
			
			//el cliente origino el cierre del stream
			if(proxyState.getClientParser().isClosing()){
				XMPPlistener.closeConnections(proxyState, selector,reactor);
				state=OriginState.CLOSED;
			}
		}
	}

	@Override
	protected void processAuthElementStart(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state != OriginState.FEATURES_END_EXPECTED)
			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);

	}

	@Override
	protected void processAuthElementEnd(XMPPproxyState proxyState,
			Selector selector, XMPproxy protocol, AdminModule adminModule,
			TCPReactor reactor) throws ClosedChannelException,
			XMLStreamException {
		if (state != OriginState.FEATURES_END_EXPECTED)
			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
	}

	@Override
	protected void processMessageElementStart(XMPPproxyState proxyState,
			Selector selector, AdminModule adminModule)
			throws ClosedChannelException, XMLStreamException {
		if (state == OriginState.CONNECTED && adminModule.isTransformationOn()
				&& !adminModule.isUserSilenced(proxyState.getUserName()))
			state = OriginState.IN_MESSAGE;

		passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);

	}

	@Override
	protected void processMessageElementEnd(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);

	}

	@Override
	protected void processMessage_bodyStart(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {

		if (state == OriginState.IN_MESSAGE)
			state = OriginState.IN_BODY;
		passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);

	}

	@Override
	protected void processCharacters(String str, XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (!(state == OriginState.STREAM_START_EXPECTED || state == OriginState.FEATURES_END_EXPECTED)) {
			if (state == OriginState.IN_BODY) {
				String message_content = asyncXMLStreamReader.getText();
				String message_transformed = L33tConverter
						.leetify(message_content);
				message_transformed = StringEscapeUtils
						.escapeXml11(message_transformed);
				XMPPlistener.writeToClient(message_transformed, proxyState,
						selector);
				return;

			} else if (state == OriginState.IN_USER_JID) {
				proxyState.setCompleteUserJID(str);
			}
			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);

		}

	}

	@Override
	protected void processOtherStartElement(XMPPproxyState proxyState,
			Selector selector) throws XMLStreamException,
			ClosedChannelException {

		if (state == OriginState.FEATURES_END_EXPECTED) {
			return;
			// omitir
			// TODO revisar en caso de que no soporte autenticacion plana
		} else if (state == OriginState.CONNECTED) {
			// redirigir al cliente
			String elementName = asyncXMLStreamReader.getLocalName();

			switch (elementName) {
			case SUCCESS: {

				proxyState.flagReset();
				break;
			}
			case JID: {
				state = OriginState.IN_USER_JID;
			}
			}
			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
		} else if (state == OriginState.IN_MESSAGE)
			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);

	}

	protected void passDirectlyToClient(XMPPproxyState proxyState,
			Selector selector, AsyncXMLStreamReader reader)
			throws XMLStreamException, ClosedChannelException {

		String toClient = XMLconstructor.constructXML(asyncXMLStreamReader);
		XMPPlistener.writeToClient(toClient, proxyState, selector);
		// byte [] data=asyncXMLStreamReader.getElementAsBinary();
		// XMPPlistener.writeToClient(data, proxyState, selector);
	}

	@Override
	protected void processOtherEndElement(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException,
			XMLStreamException, FileNotFoundException {
		String elementName = asyncXMLStreamReader.getLocalName();

		switch (elementName) {
		case FEATURES: {

			if (state == OriginState.FEATURES_END_EXPECTED) {
				state = OriginState.CONNECTED;
				String autentication = START_AUTH_TAG
						+ proxySstate.getUserPlainAuth() + END_AUTH_TAG;
				// XMPPlistener
				// .writeToOrigin(autentication, proxySstate, selector);
				enqueueForOrigin(autentication);

			} else if (!(state == OriginState.STREAM_START_EXPECTED)) {
				passDirectlyToClient(proxySstate, selector,
						asyncXMLStreamReader);
			}
			break;

		}
		case SUCCESS: {

			if (state == OriginState.CONNECTED) {
				passDirectlyToClient(proxySstate, selector,
						asyncXMLStreamReader);
				proxySstate.flagReset();
			}

			break;
		}
		case BODY: {
			if (state == OriginState.IN_BODY) {
				state = OriginState.CONNECTED;
				passDirectlyToClient(proxySstate, selector,
						asyncXMLStreamReader);
				break;
			}

		}
		case JID:{
			if(state==OriginState.IN_USER_JID)
				state=OriginState.CONNECTED;
		}

		default: {
			if (!(state == OriginState.STREAM_START_EXPECTED || state == OriginState.FEATURES_END_EXPECTED))
				passDirectlyToClient(proxySstate, selector,
						asyncXMLStreamReader);

			break;
		}

		}
	}

	
		private void enqueueForOrigin() throws XMLStreamException {
			String str=XMLconstructor.constructXML(asyncXMLStreamReader);
			enqueueForOrigin(str);

		}
	
	// encola datos a enviar al origin server
	private void enqueueForOrigin(String str) {
		if (queueToSever == null)
			queueToSever = new ArrayList<String>();

		queueToSever.add(str);

	}

	// metodo que termina mandando las cosas que estaban encoladas para ser
	// enviadas
	public void finishPendingSends(XMPPproxyState proxySstate, Selector selector)
			throws ClosedChannelException {

		sendQueueForOrigin(proxySstate, selector);
		this.queueToSever = null;
	}

	private void sendQueueForOrigin(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException {
		if (queueToSever != null) {
			for (String current : queueToSever) {
				XMPPlistener.writeToOrigin(current, proxySstate, selector);
			}

		}

	}

	@Override
	protected void processStartDocuement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == OriginState.CONNECTED) {
			passDirectlyToClient(proxyState, selector, asyncXMLStreamReader);
		}

	}

	public void reset(ByteBuffer originBuffer) throws FileNotFoundException,
			XMLStreamException {
		super.reset(originBuffer);
		queueToSever = null;

	}

	public boolean isClosing() {
		return this.state==OriginState.CLOSING;
	}

	public void announceClosing() {
		this.state=OriginState.CLOSING;
		
	}

}
