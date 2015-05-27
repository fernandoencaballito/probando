package ar.edu.itba.pdc.tp.XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;

public class FromServerParser extends GenericParser {

	
	public FromServerParser(ByteBuffer buf) throws XMLStreamException {
		super(buf);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processStreamElement(XMPPproxyState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processStreamElementEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processAuthElementStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processAuthElementEnd() {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}
	

}
