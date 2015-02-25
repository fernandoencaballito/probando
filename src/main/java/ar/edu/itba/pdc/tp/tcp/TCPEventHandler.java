package ar.edu.itba.pdc.tp.tcp;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface TCPEventHandler {
	void handle(final SelectionKey key) throws IOException;
}
