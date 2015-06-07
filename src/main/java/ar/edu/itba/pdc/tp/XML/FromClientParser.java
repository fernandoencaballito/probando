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

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

public class FromClientParser extends GenericParser {
	private enum ClientState {
		CONNECTION_STABLISHED, AUTH_EXPECTED, AUTH_VALUE_EXPECTED, AUTH_END_EXPECTED, CONNECTING_TO_ORIGIN, CONNECTED_TO_ORIGIN
	};

	private ClientState state;
	private static String PROPERTIES_FILENAME = "./properties/clientParser.properties";
	private static String INITIAL_TAG;
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

	}

	@Override
	protected void processStreamElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if (state == ClientState.CONNECTION_STABLISHED) {

//			XMPPlistener.writeToClient(INITIAL_TAG, proxyState, selector);
			enqueueForClient(INITIAL_TAG);
			state = ClientState.AUTH_EXPECTED;
		} else if (state == ClientState.CONNECTED_TO_ORIGIN) {
			passDirectlyToOriginServer(proxyState, selector);
		}

	}

	@Override
	protected void processStreamElementEnd(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);

	}

	@Override
	protected void processAuthElementStart(XMPPproxyState proxyState,
			Selector selector) {
		if (state == ClientState.AUTH_EXPECTED) {
			state = ClientState.AUTH_VALUE_EXPECTED;
		} else {
			//TODO error
			//TODO ERROR!
			throw new RuntimeException();
		
		}

	}

	@Override
	protected void processAuthElementEnd(XMPPproxyState proxyState,
			Selector selector, XMPproxy protocol, AdminModule adminModule,
			TCPReactor reactor) {
		
		if(state==ClientState.AUTH_END_EXPECTED){
			state = ClientState.CONNECTING_TO_ORIGIN;
			// conectar al origin server
			try {
				XMPPlistener.connectToOrigin(proxyState, selector, adminModule,
						protocol, reactor);
				//announceCorrectConnectToOrigin();
			} catch (IOException | XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			//TODO ERROR!
			throw new RuntimeException();
		}
		
	}

	@Override
	protected void processMessageElementStart(XMPPproxyState proxyState,
			Selector selector, AdminModule adminModule) throws ClosedChannelException, XMLStreamException {
		if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else if(state==ClientState.CONNECTION_STABLISHED || 
				state==ClientState.AUTH_EXPECTED
				|| state==ClientState.AUTH_VALUE_EXPECTED
				|| state==ClientState.AUTH_END_EXPECTED
				|| state==ClientState.CONNECTING_TO_ORIGIN){
			//TODO error!!!!
			//TODO ERROR!
			throw new RuntimeException();
		}
		


	}

	@Override
	protected void processMessageElementEnd(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else{
			//TODO error!!!!
			//TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void processMessage_bodyStart(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else{
			//TODO error!!!!
			//TODO ERROR!
			throw new RuntimeException();
		}

	}

	@Override
	protected void processCharacters(String str, XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if (state == ClientState.AUTH_VALUE_EXPECTED) {
			// se tiene el usuario y la contraseña de autenticación
			User user = Base64.getUser(str);
			proxyState.setUser(user);
			state = ClientState.AUTH_END_EXPECTED;
			// conectar con el origin con el usuario especificado
		}else if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		

	}
	
	 public void announceCorrectConnectToOrigin(){
		this.state=ClientState.CONNECTED_TO_ORIGIN;
	}

	@Override
	protected void processOtherStartElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else{
			//TODO error!!!!
			//TODO ERROR!
			throw new RuntimeException();
		}
		
	}

	@Override
	protected void processOtherEndElement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		else{
			//TODO error!!!!
			//TODO ERROR!
			throw new RuntimeException();
		}
		
	}

	@Override
	protected void finishPendingSends(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException {
		sendQueueForClient(proxySstate, selector);
		this.toClientQueue=null;
		
	}
	
	private void enqueueForClient(String str){
		if(toClientQueue ==null)
			toClientQueue=new ArrayList<String>();
		toClientQueue.add(str);
	}
	
	private void sendQueueForClient(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException{
		if(toClientQueue!=null){
			for(String current:toClientQueue){
				XMPPlistener.writeToClient(current, proxySstate, selector);
			}
		}
	}

	
	
		
	protected void passDirectlyToOriginServer(XMPPproxyState proxyState,
			Selector selector) throws XMLStreamException, ClosedChannelException{
		String toClient = XMLconstructor.constructXML(asyncXMLStreamReader);
		XMPPlistener.writeToOrigin(toClient, proxyState, selector);
		
		
		
	}

	@Override
	protected void processStartDocuement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException, XMLStreamException {
		if(state==ClientState.CONNECTED_TO_ORIGIN)
			passDirectlyToOriginServer(proxyState, selector);
		
		
	}

	public FromClientParser reset(ByteBuffer clientBuffer) throws FileNotFoundException, XMLStreamException {
		FromClientParser newParser=new FromClientParser(clientBuffer);
		newParser.state=this.state;
		return newParser;
	}

	
	

}
