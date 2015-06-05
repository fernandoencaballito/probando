package ar.edu.itba.pdc.tp.XML;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

public  class FromServerParser extends GenericParser {
	private enum OriginState{
		CONNECTION_STABLISHED, STREAM_START_EXPECTED,FEATURES_END_EXPECTED,
		CONNECTED
	};
	private OriginState state;
	private static String PROPERTIES_FILENAME = "./properties/serverParser.properties";
	private static String INITIAL_TAG;
	private static String START_AUTH_TAG;
	private static String END_AUTH_TAG;
	private static final String FEATURES="features";
	public FromServerParser(ByteBuffer buf) throws XMLStreamException, FileNotFoundException {
		super(buf);
		state= OriginState.CONNECTION_STABLISHED;
		if (INITIAL_TAG == null)
			loadPropertiesFile(PROPERTIES_FILENAME);
	}
	private void loadPropertiesFile(String fileName)
			throws FileNotFoundException {
		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);
		INITIAL_TAG = properties.getProperty("INITIAL_TAG");
		START_AUTH_TAG= properties.getProperty("START_AUTH_TAG");
		END_AUTH_TAG=properties.getProperty("END_AUTH_TAG");
	}
	@Override
	protected void processStreamElement(XMPPproxyState proxyState, Selector selector)
			throws ClosedChannelException {
		if(state==OriginState.STREAM_START_EXPECTED){
			state=OriginState.FEATURES_END_EXPECTED;
			
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
			TCPReactor reactor) {
		// TODO Auto-generated method stub
		
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
	public void initiateStream(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException {
		XMPPlistener.writeToOrigin(INITIAL_TAG, proxyState, selector);
		state=OriginState.STREAM_START_EXPECTED;
	}
	@Override
	protected void processOtherStartElement(XMPPproxyState proxyState,
			Selector selector,String elementName) {
		
		if(state==OriginState.FEATURES_END_EXPECTED){
			//escribir directamente
		}
		
	}
	@Override
	protected void processOtherEndElement(XMPPproxyState proxySstate,
			Selector selector,String elementName) throws ClosedChannelException {
		switch(elementName){
		case FEATURES:{
			state=OriginState.CONNECTED;
			String autentication=START_AUTH_TAG+proxySstate.getUserPlainAuth()+END_AUTH_TAG;
			XMPPlistener.writeToOrigin(autentication, proxySstate, selector);
			break;
			
		}
		default:{
			break;
		}
		
		}
	}
	

}
