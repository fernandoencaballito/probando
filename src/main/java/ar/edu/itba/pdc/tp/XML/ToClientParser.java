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

public class ToClientParser extends GenericParser {

	
	public ToClientParser(ByteBuffer buf) throws XMLStreamException {
		super(buf);
	}

	@Override
	protected void processStreamElement() {
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

	

	
	

}
