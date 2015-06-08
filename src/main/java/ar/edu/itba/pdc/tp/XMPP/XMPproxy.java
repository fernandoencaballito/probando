package ar.edu.itba.pdc.tp.XMPP;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

public class XMPproxy implements TCPProtocol{
	private final XMPPAcceptor acceptor;
	private final XMPPconnector connector;
	private final XMPPreader reader;
	private final XMPPWriter writer;

	public XMPproxy(TCPReactor reactor, AdminModule adminModule, int bufferSize) {
		this.acceptor = new XMPPAcceptor(this, reactor,adminModule,bufferSize);
		this.connector = new XMPPconnector();
		this.writer = new XMPPWriter(adminModule);
		this.reader = new XMPPreader(this, reactor, adminModule);
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
