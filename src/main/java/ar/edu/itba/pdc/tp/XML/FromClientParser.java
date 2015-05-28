package ar.edu.itba.pdc.tp.XML;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Properties;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

public class FromClientParser extends GenericParser {
	private enum ClientState { CONNECTION_STABLISHED, AUTH_EXPECTED, AUTH_VALUE_EXPECTED,AUTH_END_EXPECTED,
		CONNECTING_TO_ORIGIN,CONNECTED_TO_ORIGIN};
	
	
	private ClientState state;
	private static String PROPERTIES_FILENAME = "./clientParser.properties";
	private static String INITIAL_TAG;
	
	
	public FromClientParser(ByteBuffer buf) throws XMLStreamException, FileNotFoundException {
		super(buf);
		state=ClientState.CONNECTION_STABLISHED;
		if(INITIAL_TAG==null)
			loadPropertiesFile(PROPERTIES_FILENAME);
		
	}

	private void loadPropertiesFile(String fileName) throws FileNotFoundException {
		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);
		INITIAL_TAG=properties.getProperty("INITIAL_TAG");
		
		
	}

	@Override
	protected void processStreamElement(XMPPproxyState proxyState) {
		if(state==ClientState.CONNECTION_STABLISHED){
			
			XMPPlistener.writeToClient(INITIAL_TAG, proxyState);
			state=ClientState.AUTH_EXPECTED;
		}else if(state==ClientState.CONNECTED_TO_ORIGIN){
			//ignorar
		}
		
	}

	@Override
	protected void processStreamElementEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processAuthElementStart() {
		if(state==ClientState.AUTH_EXPECTED){
			state=ClientState.AUTH_VALUE_EXPECTED;
		}else{
			//error
		}
		
	}
	
	
	@Override
	protected void processAuthElementEnd() {
		state=ClientState.CONNECTING_TO_ORIGIN;
		//conectar al origin server
		XMPPlistener.connectToOrigin(null, null);
		
	}

	@Override
	protected void processMessageElementStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMessageElementEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMessage_bodyStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processCharacters(String str, XMPPproxyState proxyState) {
		if(state==ClientState.AUTH_VALUE_EXPECTED){
			//se tiene el usuario y la contraseña de autenticación
			User user=Base64.getUser(str);
			proxyState.setUser(user);
			state=ClientState.AUTH_END_EXPECTED;
			//conectar con el origin con el usuario especificado
		}
		
	}

	

}
