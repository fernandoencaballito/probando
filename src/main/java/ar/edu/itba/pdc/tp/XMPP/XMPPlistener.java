package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.NIOUtils.nonBlockingSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import ar.edu.itba.pdc.tp.XML.User;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;

public class XMPPlistener {

	public static void connectToOrigin(XMPPproxyState state, Selector selector,
			 AdminModule adminModule, XMPproxy protocol,
			TCPReactor reactor) throws IOException {
		InetSocketAddress originAddress = adminModule
				.getOriginAddressForUser(state.getUserName());
		SocketChannel originChannel = nonBlockingSocket(originAddress);
		state.setOriginChannel(originChannel);
		originChannel.register(selector, SelectionKey.OP_CONNECT, state);
		reactor.subscribeChannel(originChannel, protocol);
	}

	public static void closeConnection(XMPPproxyState state, SelectionKey key) {
		// TODO Auto-generated method stub

	}

	public static void writeToOrigin(byte[] array, XMPPproxyState state) {
		
	}
	
	public static void writeToOrigin(String str, XMPPproxyState state,Selector selector) throws ClosedChannelException {
		writeToClient(str.getBytes(), state, selector);
	}

	public static void writeToClient(byte[] array, XMPPproxyState state,
			Selector selector) throws ClosedChannelException {
		ByteBuffer buffer = state.getClientBuffer();
		buffer.clear();
		buffer.put(array);
		state.getClientChannel().register(selector, SelectionKey.OP_WRITE,
				state);

	}

	public static void writeToClient(String str, XMPPproxyState state,
			Selector selector) throws ClosedChannelException {
		writeToClient(str.getBytes(), state, selector);

	}

	private void connectToOrigin(SelectionKey key, XMPPproxyState proxyState,
			String user, AdminModule adminModule) {
		try {
			InetSocketAddress originAddress = adminModule
					.getOriginAddressForUser(user);
			SocketChannel originChannel = nonBlockingSocket(originAddress);
			proxyState.setOriginChannel(originChannel);
			originChannel.register(key.selector(), SelectionKey.OP_CONNECT,
					proxyState);
			// reactor.subscribeChannel(originChannel, parent);
		} catch (IOException e) {
			// sendResponseToClient(proxyState, ERR);
		}

	}
}
