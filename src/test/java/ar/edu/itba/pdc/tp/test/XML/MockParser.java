package ar.edu.itba.pdc.tp.test.XML;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import com.fasterxml.aalto.AsyncXMLStreamReader;

import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;
import ar.edu.itba.pdc.tp.XMPP.XMPproxy;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

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
			Selector selector, AsyncXMLStreamReader reader) throws ClosedChannelException {
		this.streamElementStart = true;

	}

	@Override
	protected void processStreamElementEnd(XMPPproxyState state,
			Selector selector, AsyncXMLStreamReader reader) {
		streamElementEnd = true;

	}

	@Override
	protected void processAuthElementStart(XMPPproxyState state,
			Selector selector, AsyncXMLStreamReader reader) {
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
			Selector selector, AsyncXMLStreamReader reader) {
		messageElementStart = true;
	}

	public boolean isMessage_bodySTART() {
		return message_bodySTART;
	}

	public boolean isMessage_bodyEND() {
		return message_bodyEND;
	}

	@Override
	protected void processMessageElementEnd (XMPPproxyState state,
			Selector selector, AsyncXMLStreamReader reader){
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
	protected void processMessage_bodyStart (XMPPproxyState state,
			Selector selector, AsyncXMLStreamReader reader){
		message_bodySTART=true;
		
	}

	@Override
	protected void processCharacters(String str,
			XMPPproxyState proxyState, Selector selector, AsyncXMLStreamReader reader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processOtherEndElement(XMPPproxyState state,
			Selector selector, String elementName)
			throws ClosedChannelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processOtherStartElement(XMPPproxyState state,
			Selector selector, String elementName) {
		// TODO Auto-generated method stub
		
	}

}
