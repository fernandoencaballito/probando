package ar.edu.itba.pdc.tp.XML;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.RuntimeErrorException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

public class FromClientParser extends GenericParser {
	private static enum ClientState {
		CONNECTION_STABLISHED, AUTH_EXPECTED, AUTH_VALUE_EXPECTED, AUTH_END_EXPECTED, CONNECTING_TO_ORIGIN, CONNECTED_TO_ORIGIN
	,IN_MUTED_MESSAGE};

	private ClientState state;
	private static String PROPERTIES_FILENAME = "./properties/clientParser.properties";
	private static String INITIAL_TAG;
	private static String MESSAGE_NOT_ACCEPTABLE;
	private List<String> toClientQueue;

	public FromClientParser(ByteBuffer buf) throws XMLStreamException,
			FileNotFoundException {
		super(buf);
		state = ClientState.CONNECTION_STABLISHED;
		if (INITIAL_TAG == null)
			loadPropertiesFile(PROPERTIES_FILENAME);

	}

	private void loadPropertiesFile(String fileName)
			throws FileNotFoundException {
		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);
		INITIAL_TAG = properties.getProperty("INITIAL_TAG");
		MESSAGE_NOT_ACCEPTABLE = properties
				.getProperty("MESSAGE_NOT_ACCEPTABLE");

	}

	@Override
	protected void processStreamElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == ClientState.CONNECTION_STABLISHED) {

			// XMPPlistener.writeToClient(INITIAL_TAG, proxyState, selector);
			enqueueForClient(INITIAL_TAG);
			state = ClientState.AUTH_EXPECTED;
		} else if (state == ClientState.CONNECTED_TO_ORIGIN) {
			passDirectlyToOriginServer(proxyState, selector);
			String str=XMLconstructor.constructXML(asyncXMLStreamReader);
//			enqueueForClient(str);
		}

	}

	@Override
	protected void processStreamElementEnd(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);

	}

	@Override
	protected void processAuthElementStart(XMPPproxyState proxyState,
			Selector selector) {
		if (state == ClientState.AUTH_EXPECTED) {
			state = ClientState.AUTH_VALUE_EXPECTED;
		} else {
			// TODO error
			// TODO ERROR!
			throw new RuntimeException();

		}

	}

	@Override
	protected void processAuthElementEnd(XMPPproxyState proxyState,
			Selector selector, XMPproxy protocol, AdminModule adminModule,
			TCPReactor reactor) {

		if (state == ClientState.AUTH_END_EXPECTED) {
			state = ClientState.CONNECTING_TO_ORIGIN;
			// conectar al origin server
			try {
				XMPPlistener.connectToOrigin(proxyState, selector, adminModule,
						protocol, reactor);
				// announceCorrectConnectToOrigin();
			} catch (IOException | XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void processMessageElementStart(XMPPproxyState proxyState,
			Selector selector, AdminModule adminModule)
			throws ClosedChannelException, XMLStreamException {

		if (state == ClientState.CONNECTED_TO_ORIGIN) {
			if (adminModule.isUserSilenced(proxyState.getUserName())) {
				state=ClientState.IN_MUTED_MESSAGE;
				
				String idNamespaceURI="";
				String idLocalName="id";
				int idAttributeIndex=asyncXMLStreamReader.getAttributeIndex(idNamespaceURI, idLocalName);
				String id=asyncXMLStreamReader.getAttributeValue(idAttributeIndex);
				proxyState.setId(id);
				
				System.out.println(id);
				
			} else {

				passDirectlyToOriginServer(proxyState, selector);
			}
		} else if (state == ClientState.CONNECTION_STABLISHED
				|| state == ClientState.AUTH_EXPECTED
				|| state == ClientState.AUTH_VALUE_EXPECTED
				|| state == ClientState.AUTH_END_EXPECTED
				|| state == ClientState.CONNECTING_TO_ORIGIN) {
			// TODO error!!!!
			// TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void processMessageElementEnd(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else if(state==ClientState.IN_MUTED_MESSAGE){
//			XMPPlistener.writeToClient(assembleMessageNotAcceptable(proxyState),
//										proxyState, selector);
			enqueueForClient(assembleMessageNotAcceptable(proxyState));
			state=ClientState.CONNECTED_TO_ORIGIN;
		}
		else {
			// TODO error!!!!
			// TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void processMessage_bodyStart(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		
		if (state == ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else if(state==ClientState.IN_MUTED_MESSAGE)
			return;
		else {
			// TODO error!!!!
			// TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void processCharacters(String str, XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == ClientState.AUTH_VALUE_EXPECTED) {
			// se tiene el usuario y la contraseña de autenticación
			User user = Base64.getUser(str);
			proxyState.setUser(user);
			state = ClientState.AUTH_END_EXPECTED;
			// conectar con el origin con el usuario especificado
		} else if (state == ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);

	}

	public void announceCorrectConnectToOrigin() {
		this.state = ClientState.CONNECTED_TO_ORIGIN;
	}

	@Override
	protected void processOtherStartElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else if(state==ClientState.IN_MUTED_MESSAGE)
			return;
		else {
			// TODO error!!!!
			// TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void processOtherEndElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else if(state==ClientState.IN_MUTED_MESSAGE)
			return;
		else {
			// TODO error!!!!
			// TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void finishPendingSends(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException {
		sendQueueForClient(proxySstate, selector);
		this.toClientQueue = null;

	}

	private void enqueueForClient() throws XMLStreamException{
		String data=XMLconstructor.constructXML(asyncXMLStreamReader);
		this.enqueueForClient(data);
	}
	
	private void enqueueForClient(String str) {
		if (toClientQueue == null)
			toClientQueue = new ArrayList<String>();
		toClientQueue.add(str);
	}

	private void sendQueueForClient(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException {
		if (toClientQueue != null) {
			for (String current : toClientQueue) {
				XMPPlistener.writeToClient(current, proxySstate, selector);
			}
		}
	}

	protected void passDirectlyToOriginServer(XMPPproxyState proxyState,
			Selector selector) throws XMLStreamException,
			ClosedChannelException {
		String toOrigin = XMLconstructor.constructXML(asyncXMLStreamReader);
		XMPPlistener.writeToOrigin(toOrigin, proxyState, selector);

	}

	@Override
	protected void processStartDocuement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		if (state == ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);

	}

	public void reset(ByteBuffer clientBuffer) throws FileNotFoundException,
			XMLStreamException {
		super.reset(clientBuffer);

		toClientQueue = null;
	}

	private String assembleMessageNotAcceptable(XMPPproxyState proxyState) {
		String id =proxyState.getId() ;
		String to = proxyState.getCompleteUserJID();
		String ans = null;
		String[] searchList = { "TO", "ID" };
		String[] replacementList = new String[2];
		replacementList[0] = to;
		replacementList[1] = id;

		ans=StringUtils.replaceEach(MESSAGE_NOT_ACCEPTABLE, searchList,
				replacementList);
		return ans;

	}

}
