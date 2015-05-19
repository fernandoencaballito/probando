package ar.edu.itba.pdc.tp.XML;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;

public class FromClientParser extends GenericParser {
	private enum ClientState { CONNECTION_STABLISHED, AUTH_EXPECTED, AUTHENTICATED}
	
	
	private ClientState state;
	
	public FromClientParser(ByteBuffer buf) throws XMLStreamException {
		super(buf);
		state=ClientState.CONNECTION_STABLISHED;
	}

	@Override
	protected void processStreamElement(XMPPlistener listener) {
		if(state==ClientState.CONNECTION_STABLISHED){
			
			
			
		}else if(state==ClientState.AUTHENTICATED){
			//ignorar
		}
		
	}

	@Override
	protected void processStreamElementEnd(XMPPlistener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processAuthElementStart(XMPPlistener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processAuthElementEnd(XMPPlistener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMessageElementStart(XMPPlistener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processMessageElementEnd(XMPPlistener listener) {
		// TODO Auto-generated method stub
		
	}

	

	
	

}
