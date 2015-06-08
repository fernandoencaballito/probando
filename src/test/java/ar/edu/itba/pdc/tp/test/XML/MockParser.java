package ar.edu.itba.pdc.tp.test.XML;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;

import javax.xml.stream.XMLStreamException;

import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

import com.fasterxml.aalto.AsyncXMLStreamReader;


public class MockParser extends GenericParser {

	private boolean streamElementStart = false;
	private boolean streamElementEnd = false;
	private boolean authElementStart = false;
	private boolean authElementEnd = false;
	private boolean messageElementStart = false;
	private boolean messageElementEnd = false;
	private boolean message_bodySTART=false;
	private boolean message_bodyEND=false;
	public MockParser(ByteBuffer buf) throws XMLStreamException {
		super(buf);
	}

	@Override
	protected void processStreamElement(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException{
		this.streamElementStart = true;

	}

	@Override
	protected void processStreamElementEnd(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		streamElementEnd = true;

	}

	@Override
	protected void processAuthElementStart(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		authElementStart = true;

	}

	@Override
	protected void processAuthElementEnd(XMPPproxyState proxyState,
			Selector selector, XMPproxy protocol, AdminModule adminModule,
			TCPReactor reactor) {
		authElementEnd = true;
	}

	@Override
	protected void processMessageElementStart(XMPPproxyState state,
			Selector selector, AdminModule adminModule) throws ClosedChannelException,
			XMLStreamException {
		messageElementStart = true;
	}

	public boolean isMessage_bodySTART() {
		return message_bodySTART;
	}

	public boolean isMessage_bodyEND() {
		return message_bodyEND;
	}

	@Override
	protected void processMessageElementEnd(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException{
		messageElementEnd = true;
	}

	public boolean isStreamElementStart() {
		return streamElementStart;
	}

	public boolean isStreamElementEnd() {
		return streamElementEnd;
	}

	public boolean isAuthElementStart() {
		return authElementStart;
	}

	public boolean isAuthElementEnd() {
		return authElementEnd;
	}

	public boolean isMessageElementStart() {
		return messageElementStart;
	}

	public boolean isMessageElementEnd() {
		return messageElementEnd;
	}

	@Override
	protected void processMessage_bodyStart(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException{
		message_bodySTART=true;
		
	}

	@Override
	protected void processStartDocuement(XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void finishPendingSends(XMPPproxyState proxySstate,
			Selector selector) throws ClosedChannelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processOtherEndElement(XMPPproxyState state,
			Selector selector) throws ClosedChannelException,
			XMLStreamException, FileNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processOtherStartElement(XMPPproxyState state,
			Selector selector) throws XMLStreamException,
			ClosedChannelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processCharacters(String str, XMPPproxyState proxyState,
			Selector selector) throws ClosedChannelException,
			XMLStreamException {
		// TODO Auto-generated method stub
		
	}

	

	

	

}
