package ar.edu.itba.pdc.tp.XML;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class GenericParser {
	private static final String STREAM = "stream";
	private static final String MESSAGE = "message";

	protected InputStream in;
	protected OutputStream out;
	protected Element element;

	private XMLInputFactory inputFactory;
	private XMLEventReader eventReader;

	public GenericParser(InputStream in, OutputStream out) {

		this.in = in;
		this.out = out;

		try {
			// First, create a new XMLInputFactory
			inputFactory = XMLInputFactory.newInstance();
			// Setup a new eventReader
			eventReader = inputFactory.createXMLEventReader(in);
			
			element = null;
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public void parse() {
		try {
			while (eventReader.hasNext()) {
				XMLEvent event=null;
				try{
				event = eventReader.nextEvent();
				}catch(NullPointerException e){
					e.printStackTrace();
				}
				if (event.isStartElement()) {
					
					StartElement startElement = event.asStartElement();
					System.out.println(startElement.getName().getLocalPart());
					// If we have an item element, we create a new element
					if (startElement.getName().getLocalPart() == (STREAM)) {

						processStreamElement(startElement);

					}

				}
				// If we reach the end of an item element, we add it to the list
				if (event.isEndElement()) {
					EndElement endElement = event.asEndElement();
					if (endElement.getName().getLocalPart() == (STREAM)) {

					}
				}

			}
		} catch (XMLStreamException e) {
			
		}
	}

	protected abstract void processStreamElement(StartElement startElement) throws XMLStreamException;

}
