package ar.edu.itba.pdc.tp.pop3;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

public class POP3Proxy implements TCPProtocol{
	private final POP3Acceptor acceptor;
	private final POP3Connector connector;
	private final POP3Reader reader;
	private final POP3Writer writer;

	public POP3Proxy(TCPReactor reactor, AdminModule adminModule) {
		this.acceptor = new POP3Acceptor(this, reactor,adminModule);
		this.connector = new POP3Connector();
		this.writer = new POP3Writer(adminModule);
		this.reader = new POP3Reader(this, reactor, adminModule);
	}

	@Override
	public void handleAccept(SelectionKey key) throws IOException {
		acceptor.handle(key);
	}

	@Override
	public void handleConnect(SelectionKey key) throws IOException {
		connector.handle(key);
	}

	@Override
	public void handleRead(SelectionKey key) throws IOException {
		reader.handle(key);
	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException {
		writer.handle(key);
	}
}
