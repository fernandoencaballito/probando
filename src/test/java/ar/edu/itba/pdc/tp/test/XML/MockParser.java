package ar.edu.itba.pdc.tp.test.XML;

import java.nio.ByteBuffer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.XMPP.XMPPlistener;

public class MockParser extends GenericParser {

	private boolean streamElementStart = false;
	private boolean streamElementEnd = false;
	private boolean authElementStart = false;
	private boolean authElementEnd = false;
	private boolean messageElementStart = false;
	private boolean messageElementEnd = false;

	public MockParser(ByteBuffer buf) throws XMLStreamException {
		super(buf);
	}

	@Override
	protected void processStreamElement(XMPPlistener listener) {
		this.streamElementStart = true;

	}

	@Override
	protected void processStreamElementEnd(XMPPlistener listener) {
		streamElementEnd = true;

	}

	@Override
	protected void processAuthElementStart(XMPPlistener listener) {
		authElementStart = true;

	}

	@Override
	protected void processAuthElementEnd(XMPPlistener listener) {
		authElementEnd = true;
	}

	@Override
	protected void processMessageElementStart(XMPPlistener listener) {
		messageElementStart = true;
	}

	@Override
	protected void processMessageElementEnd(XMPPlistener listener) {
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

}
