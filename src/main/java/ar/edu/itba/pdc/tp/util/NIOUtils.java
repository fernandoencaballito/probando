package ar.edu.itba.pdc.tp.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOUtils {
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
        byte[] array = new byte[buffer.limit() - buffer.position()];
        buffer.get(array);
        try {
            read = new String(array, 0, buffer.limit(), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return read;
    }

}