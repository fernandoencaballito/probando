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

public class ToServerParser extends GenericParser {

	
	public ToServerParser(ByteBuffer buf) throws XMLStreamException {
		super(buf);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processStreamElement(StartElement startElement)
			 {
		
		
	}

	

}
