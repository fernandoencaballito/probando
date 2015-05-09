package ar.edu.itba.pdc.tp.XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class ToServerParser extends GenericParser {

	public ToServerParser(InputStream in, OutputStream out) {
		super(in, out);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processStreamElement(StartElement startElement)
			throws XMLStreamException {
		// create an XMLOutputFactory
	    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	    // create XMLEventWriter
	    XMLEventWriter eventWriter = outputFactory
	        .createXMLEventWriter(out,"UTF-8");
	    
	    
	    eventWriter.add(startElement);
	    try {
			out.write(">".getBytes("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

}
