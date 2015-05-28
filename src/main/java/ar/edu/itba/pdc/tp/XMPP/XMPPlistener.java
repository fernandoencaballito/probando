package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.NIOUtils.nonBlockingSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ar.edu.itba.pdc.tp.admin.AdminModule;


public class XMPPlistener  {

	
	
	public static void connectToOrigin(XMPPproxyState state,SelectionKey key) {
		// TODO Auto-generated method stub
		
	}

	
	public static void closeConnection(XMPPproxyState state,SelectionKey key) {
		// TODO Auto-generated method stub
		
	}

	
	public static void writeToOrigin(byte[] array, XMPPproxyState state) {
		// TODO Auto-generated method stub
		
	}

	
	public static void writeToClient(byte[] array, XMPPproxyState state) {
		ByteBuffer buffer=state.getClientBuffer();
		buffer.clear();
		buffer.put(array);
		
	}

	public static void writeToClient(String str, XMPPproxyState state) {
		writeToClient(str.getBytes(), state);
		
	}

	
	private void connectToOrigin(SelectionKey key, XMPPproxyState proxyState,
			String user,AdminModule adminModule) {
		try {
			InetSocketAddress originAddress = adminModule
					.getOriginAddressForUser(user);
			SocketChannel originChannel = nonBlockingSocket(originAddress);
			proxyState.setOriginChannel(originChannel);
			originChannel.register(key.selector(), SelectionKey.OP_CONNECT,
					proxyState);
//			reactor.subscribeChannel(originChannel, parent);
		} catch (IOException e) {
			// sendResponseToClient(proxyState, ERR);
		}

	}
}
