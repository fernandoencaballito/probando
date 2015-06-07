package ar.edu.itba.pdc.tp.tcp;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import javax.xml.stream.XMLStreamException;

public interface TCPEventHandler {
	void handle(final SelectionKey key) throws IOException, XMLStreamException;
}
