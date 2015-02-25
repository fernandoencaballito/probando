package ar.edu.itba.pdc.tp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.admin.AdminProtocol;
import ar.edu.itba.pdc.tp.pop3.POP3ProxyProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPProtocol;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

public class Main {
	private static final int bufferSize = 4 * 1024;// 4k

	private static final short POP3_PORT = 110; // 110;// puerto del origin
													// server de pop3
	private static final short ADMIN_PORT = 10001;
	private static final short PROXY_PORT = 10002;

	public static void main(String[] args) throws IOException {
		final InetSocketAddress POP3_ADDRESS = new InetSocketAddress(
				InetAddress.getLocalHost().getHostAddress(), PROXY_PORT);
		final InetSocketAddress ADMIN_ADDRESS = new InetSocketAddress(
				InetAddress.getLocalHost().getHostAddress(), ADMIN_PORT);
		Map<Integer, TCPProtocol> protocolHandlers = new HashMap<>();

		AdminModule adminModule = new AdminModule("localhost", POP3_PORT);

		protocolHandlers.put(POP3_ADDRESS.getPort(), new POP3ProxyProtocol(
				adminModule));
		protocolHandlers.put(ADMIN_ADDRESS.getPort(), new AdminProtocol(
				bufferSize, adminModule));

		TCPReactor reactor = new TCPReactor(protocolHandlers);
		reactor.start();
	}
}