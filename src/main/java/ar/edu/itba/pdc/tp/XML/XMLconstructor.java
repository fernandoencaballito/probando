package ar.edu.itba.pdc.tp.XML;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.fasterxml.aalto.AsyncXMLStreamReader;

public class XMLconstructor {

	public static String constructXML(AsyncXMLStreamReader reader)
			throws XMLStreamException {
		String ans = null;
		int type = reader.getEventType();

		String attributes = null;
		String start = null;
		String end = ">";
		
		//
		switch (type) {
		case XMLEvent.CHARACTERS: {
			return reader.getText();
		}

		case XMLEvent.START_ELEMENT: {
			start = "<";

			break;

		}
		case XMLEvent.END_ELEMENT: {

			start = "</";
			break;
		}
		case XMLEvent.START_DOCUMENT: {

			start = "<?xml";

		}
		}
		ans = start;
		//
		
		//se agregan atributos y namespaces
		if (type == XMLEvent.START_ELEMENT) {
			ans += getElementName(reader);
			String nameSpaces = getNamespaces(reader);
			if (nameSpaces != null && !nameSpaces.isEmpty())
				ans += nameSpaces;

			attributes = getAttributes(reader);

			if (attributes != null && !attributes.isEmpty())
				ans += attributes;

		} else if (type == XMLEvent.END_ELEMENT) {
			ans += getElementName(reader);

		}

		else if (type == XMLEvent.START_DOCUMENT) {
			ans += " version=\"";
			ans += reader.getVersion();
			ans += "\"?";
		}

		if(reader.isEmptyElement())
			end="/>";
		
		ans += end;
		return ans;
	}

	private static String getNamespaces(AsyncXMLStreamReader reader) {
		String ans = "";
		int count = reader.getNamespaceCount();

		for (int i = 0; i < count; i++) {
			String prefix = reader.getNamespacePrefix(i);
			ans += " xmlns";
			if (prefix != null && !prefix.isEmpty()) {
				ans += ":" + prefix;
			}
			String value = reader.getNamespaceURI(i);
			ans += "=" + "\"" + value + "\"";

			System.out.println(ans);

		}

		return ans;
	}

	private static String getElementName(AsyncXMLStreamReader reader)
			throws XMLStreamException {
		String name;
		String prefix = reader.getPrefix();

		if (prefix != null && !prefix.isEmpty()) {
			name = prefix;
			name += ":";
		} else {
			name = "";
		}

		name += reader.getLocalName();

		return name;
	}

	private static String getAttributes(AsyncXMLStreamReader reader) {
		String ans = "";
		int count = reader.getAttributeCount();

		for (int i = 0; i < count; i++) {
			String name;
			String prefix = reader.getAttributeName(i).getPrefix();

			if (prefix != null && !prefix.isEmpty()) {
				name = " " + prefix + ":"
						+ reader.getAttributeName(i).getLocalPart();
			} else {

				name = " " + reader.getAttributeName(i).getLocalPart();
			}
			ans += name + "=" + "\"" + reader.getAttributeValue(i) + "\"";
		}

		return ans;
	}
}
