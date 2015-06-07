package ar.edu.itba.pdc.tp.XMPP;

import static ar.edu.itba.pdc.tp.util.NIOUtils.nonBlockingSocket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import ar.edu.itba.pdc.tp.XML.User;
import ar.edu.itba.pdc.tp.admin.AdminModule;
import ar.edu.itba.pdc.tp.tcp.TCPReactor;
import ar.edu.itba.pdc.tp.util.PropertiesFileLoader;

public class XMPPlistener {
	private static String INITIAL_TAG;
	private static String PROPERTIES_FILENAME = "./properties/XMPPlistener.properties";
	
	public static void connectToOrigin(XMPPproxyState state, Selector selector,
			AdminModule adminModule, XMPproxy protocol, TCPReactor reactor)
			throws IOException, XMLStreamException {
		InetSocketAddress originAddress = adminModule
				.getOriginAddressForUser(state.getUserName());
		SocketChannel originChannel = nonBlockingSocket(originAddress);
		state.setOriginChannel(originChannel);
		originChannel.register(selector, SelectionKey.OP_CONNECT, state);
		reactor.subscribeChannel(originChannel, protocol);
	}
	
	private static void loadPropertiesFile(String fileName)
			throws FileNotFoundException {
		Properties properties = PropertiesFileLoader
				.loadPropertiesFromFile(fileName);
		INITIAL_TAG = properties.getProperty("INITIAL_TAG");
	}


	public static void closeConnection(XMPPproxyState state, SelectionKey key) {
		// TODO Auto-generated method stub

	}

	public static void finishConnectToOrigin(XMPPproxyState proxyState, Selector selector) throws ClosedChannelException, FileNotFoundException{
		if(INITIAL_TAG==null)
			loadPropertiesFile(PROPERTIES_FILENAME);
		writeToOrigin(INITIAL_TAG, proxyState, selector);
	}
	
	public static void writeToOrigin(byte[] array, XMPPproxyState state,
			Selector selector) throws ClosedChannelException {
		ByteBuffer buffer = state.getOriginBuffer();
		if (!(buffer.limit()==buffer.capacity() && buffer.position()==0))
			buffer.compact();
		buffer.put(array);
		state.getOriginChannel().register(selector, SelectionKey.OP_WRITE,
				state);
	}

	public static void writeToOrigin(String str, XMPPproxyState state,
			Selector selector) throws ClosedChannelException {
		writeToOrigin(str.getBytes(), state, selector);
	}

	public static void writeToClient(byte[] array, XMPPproxyState state,
			Selector selector) throws ClosedChannelException {
		ByteBuffer buffer = state.getClientBuffer();
		if (!(buffer.limit()==buffer.capacity() && buffer.position()==0))//no se hizo un clear previamente
			buffer.compact();
		buffer.put(array);
		state.getClientChannel().register(selector, SelectionKey.OP_WRITE,
				state);

	}


	
	
	
	public static void writeToClient(String str, XMPPproxyState state,
			Selector selector) throws ClosedChannelException {
		writeToClient(str.getBytes(), state, selector);

	}

}
