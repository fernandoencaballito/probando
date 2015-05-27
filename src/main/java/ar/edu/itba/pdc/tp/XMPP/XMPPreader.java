package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.NIOUtils.append;
import static ar.edu.itba.pdc.tp.util.NIOUtils.nonBlockingSocket;
import static ar.edu.itba.pdc.tp.util.POP3Utils.ERR;
import static ar.edu.itba.pdc.tp.util.POP3Utils.OK;
import static ar.edu.itba.pdc.tp.util.POP3Utils.asErrLine;
import static ar.edu.itba.pdc.tp.util.POP3Utils.asMultilines;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import ar.edu.itba.pdc.tp.XML.GenericParser;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.email.EmailConverter;
import ar.edu.itba.pdc.tp.tcp.TCPEventHandler;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.NIOUtils;

class XMPPreader implements TCPEventHandler {

	private static final Logger LOGGER = Logger.getLogger(XMPPreader.class);

	private final TCPReactor reactor;
	private final AdminModule adminModule;
	private final XMPproxy parent;

	XMPPreader(XMPproxy parent, TCPReactor reactor, AdminModule adminModule) {
		this.parent = parent;
		this.reactor = reactor;
		this.adminModule = adminModule;
	}

	@Override
	public void handle(SelectionKey key) throws IOException {

		final XMPPproxyState proxyState = (XMPPproxyState) key.attachment();

		final SocketChannel readChannel = (SocketChannel) key.channel();
		final ByteBuffer readBuffer = proxyState.getReadBuffer(readChannel);

		SocketChannel writeChannel = null;
		GenericParser parser = null;
		long bytesRead = readChannel.read(readBuffer);
		readBuffer.flip();
		if (readChannel == proxyState.getClientChannel()) {
			// cliente solicita lectura!!
			// writeChannel=proxyState.getOriginChannel();
			try {
				parser = proxyState.getClientParser();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// servidor solicita lectura
			writeChannel = proxyState.getClientChannel();
			parser = proxyState.getServerParser();

		}

		if (bytesRead == -1) { // Did the other end close?
			proxyState.closeChannels();
			reactor.unsubscribeChannel(proxyState.getClientChannel());
			reactor.unsubscribeChannel(proxyState.getOriginChannel());
		} else if (bytesRead > 0) {

			// processar lo leido con el parser
			try {
				parser.feed();
				parser.parse(proxyState);
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
			proxyState.updateSubscription(key.selector());
			// if(proxyState.getOriginChannel()==null){
			// connectToOrigin(key, proxyState,"hola");
			// }

		}

	}

	private void connectToOrigin(SelectionKey key, XMPPproxyState proxyState,
			String user) {
		try {
			InetSocketAddress originAddress = adminModule
					.getOriginAddressForUser(user);
			SocketChannel originChannel = nonBlockingSocket(originAddress);
			proxyState.setOriginChannel(originChannel);
			originChannel.register(key.selector(), SelectionKey.OP_CONNECT,
					proxyState);
			reactor.subscribeChannel(originChannel, parent);
		} catch (IOException e) {
			// sendResponseToClient(proxyState, ERR);
		}

	}

}
