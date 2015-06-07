package ar.edu.itba.pdc.tp.util;

import static java.lang.Math.min;
import static org.apache.commons.lang3.ArrayUtils.subarray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOUtils {
	public static SocketChannel nonBlockingSocket(InetSocketAddress address)
			throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(address);
		return channel;
	}

	public static void closeQuietly(Channel channel) {
		try {
			channel.close();
		} catch (Exception e) {
		}
	}

	public static int getLocalPort(Channel channel) throws IOException {
		try {
			return getLocalPort((SocketChannel) channel);
		} catch (ClassCastException e) {
			return getLocalPort((ServerSocketChannel) channel);
		}
	}

	public static int getLocalPort(SocketChannel channel) throws IOException {
		InetSocketAddress address = (InetSocketAddress) channel
				.getLocalAddress();
		return address.getPort();
	}

	public static int getLocalPort(ServerSocketChannel channel)
			throws IOException {
		InetSocketAddress address = (InetSocketAddress) channel
				.getLocalAddress();
		return address.getPort();
	}

	public static int getRemotePort(SocketChannel channel) throws IOException {
		InetSocketAddress address = (InetSocketAddress) channel
				.getRemoteAddress();
		return address.getPort();
	}

	public static String readBuffer(ByteBuffer buffer) {
		String read = "";
		byte[] bytes = buffer.array();
		try {
			read = new String(bytes, 0, buffer.limit(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return read;
	}

	public static ByteBuffer append(ByteBuffer buffer, byte[] bytes) {
		return buffer.put(bytes, 0, bytes.length);
	}

	public static void transferBuffer(ByteBuffer fromBuffer, ByteBuffer toBuffer) {
		if (!toBuffer.hasRemaining() || !fromBuffer.hasRemaining()) {
			// either toBuffer is full or fromBuffer has nothing copyable
		}
		int toCopy = min(fromBuffer.limit(),
				toBuffer.remaining() - fromBuffer.remaining());
		byte[] bytes = subarray(fromBuffer.array(), fromBuffer.position(),
				toCopy);
		append(toBuffer, bytes);
		int newLimit = min(fromBuffer.limit() + toCopy, fromBuffer.capacity());
		fromBuffer.limit(newLimit);
		fromBuffer.compact();
	}
	
	public static String getFirstLine(ByteBuffer buffer){
	    String all = readBuffer(buffer);
	    String[] split = all.split("\r\n");
	    return split[0];
	}
}