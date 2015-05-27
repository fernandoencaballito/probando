package ar.edu.itba.pdc.tp.XMPP;

import java.nio.ByteBuffer;


public class XMPPlistener  {

	
	
	public static void connectToOrigin(XMPPproxyState state) {
		// TODO Auto-generated method stub
		
	}

	
	public static void closeConnection(XMPPproxyState state) {
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


}
