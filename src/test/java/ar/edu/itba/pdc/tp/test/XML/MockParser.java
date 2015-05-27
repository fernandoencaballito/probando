package ar.edu.itba.pdc.tp.test.XML;

import java.nio.ByteBuffer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;
import ar.edu.itba.pdc.tp.XMPP.XMPPproxyState;

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
	protected void processStreamElement(XMPPproxyState state) {
		this.streamElementStart = true;

	}

	@Override
	protected void processStreamElementEnd() {
		streamElementEnd = true;

	}

	@Override
	protected void processAuthElementStart() {
		authElementStart = true;

	}

	@Override
	protected void processAuthElementEnd() {
		authElementEnd = true;
	}

	@Override
	protected void processMessageElementStart() {
		messageElementStart = true;
	}

	public boolean isMessage_bodySTART() {
		return message_bodySTART;
	}

	public boolean isMessage_bodyEND() {
		return message_bodyEND;
	}

	@Override
	protected void processMessageElementEnd() {
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
	protected void processMessage_bodyStart() {
		message_bodySTART=true;
		
	}

	@Override
	protected void processCharacters(String str, XMPPproxyState proxyState) {
		// TODO Auto-generated method stub
		
	}

}
