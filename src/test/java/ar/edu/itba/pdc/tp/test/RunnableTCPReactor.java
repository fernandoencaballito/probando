package ar.edu.itba.pdc.tp.test;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Map;

import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactorImpl;

public class RunnableTCPReactor extends TCPReactorImpl implements Runnable {
	// private static final int TIMEOUT = 2000;

	private boolean listening = false;
	private int eventCount;

	public RunnableTCPReactor(Map<Integer, TCPProtocol> protocolsByPort) {
		super(3000, protocolsByPort,"localhost");
	}

	public boolean isListening() {
		return listening;
	}

	@Override
	protected void startListeners(Selector selector) throws IOException {
		super.startListeners(selector);
		listening = true;
	}

	@Override
	public void run() {
		try {
			start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected int waitForEvents(Selector selector) throws IOException {
		eventCount = super.waitForEvents(selector);
		return eventCount;
	}

	public boolean hasEvents() {
		return eventCount > 0;
	}
}
